package ar.edu.utn.diplomatura.fintech.servicios;

import ar.edu.utn.diplomatura.fintech.componentes.DolarApiClient;
import ar.edu.utn.diplomatura.fintech.dto.ActualizarCuentaRequest;
import ar.edu.utn.diplomatura.fintech.dto.CotizacionDolar;
import ar.edu.utn.diplomatura.fintech.dto.CrearCuentaRequest;
import ar.edu.utn.diplomatura.fintech.dto.SaldoUsdResponse;
import ar.edu.utn.diplomatura.fintech.entidades.CajaDeAhorro;
import ar.edu.utn.diplomatura.fintech.entidades.Cuenta;
import ar.edu.utn.diplomatura.fintech.entidades.CuentaCorriente;
import ar.edu.utn.diplomatura.fintech.entidades.Usuario;
import ar.edu.utn.diplomatura.fintech.excepciones.CuentaNoEncontradaException;
import ar.edu.utn.diplomatura.fintech.excepciones.RecursoDuplicadoException;
import ar.edu.utn.diplomatura.fintech.repositorios.CuentaRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Reglas del negocio de cuentas.
 */
@Service
public class CuentaService {

    private final CuentaRepository cuentaRepository;
    private final UsuarioService usuarioService;
    private final DolarApiClient dolarApiClient;

    // Usamos el UsuarioService (y no el repositorio) para reutilizar
    // buscarPorId(), que ya lanza UsuarioNoEncontradoException.
    public CuentaService(CuentaRepository cuentaRepository, UsuarioService usuarioService,
                         DolarApiClient dolarApiClient) {
        this.cuentaRepository = cuentaRepository;
        this.usuarioService = usuarioService;
        this.dolarApiClient = dolarApiClient;
    }

    /** Solo las cuentas activas: las dadas de baja logica no se listan. */
    public List<Cuenta> listarTodas() {
        return cuentaRepository.findByActivaTrue();
    }

    /** Una cuenta dada de baja logica se considera inexistente -> 404. */
    public Cuenta buscarPorId(Long id) {
        return cuentaRepository.findByIdAndActivaTrue(id)
                .orElseThrow(() -> new CuentaNoEncontradaException(id));
    }

    /**
     * Crea una cuenta a partir del DTO.
     * Aca se resuelve la clase abstracta: segun el "tipo" se instancia una subclase u otra.
     */
    public Cuenta crear(CrearCuentaRequest request) {
        if (cuentaRepository.existsByNumero(request.numero())) {
            throw new RecursoDuplicadoException("Ya existe una cuenta con numero " + request.numero());
        }

        // Si el usuario no existe, se lanza UsuarioNoEncontradoException -> 404.
        Usuario titular = usuarioService.buscarPorId(request.usuarioId());

        // switch expression (Java 14+): devuelve un valor segun el tipo.
        Cuenta cuenta = switch (request.tipo()) {
            case CAJA_AHORRO -> new CajaDeAhorro(request.numero(), titular);
            case CUENTA_CORRIENTE -> new CuentaCorriente(request.numero(), titular, valorOCero(request.descubierto()));
        };

        // El saldo inicial entra como un deposito, asi pasa por las mismas validaciones.
        BigDecimal saldoInicial = valorOCero(request.saldoInicial());
        if (saldoInicial.compareTo(BigDecimal.ZERO) > 0) {
            cuenta.depositar(saldoInicial);
        }

        // TRY/CATCH: el existsByNumero de arriba no alcanza si llegan dos pedidos
        // al mismo tiempo con el mismo numero (los dos pasan el chequeo). En ese caso
        // la restriccion UNIQUE de la base rechaza el segundo INSERT con una
        // DataIntegrityViolationException. La atrapamos y la traducimos a nuestra
        // excepcion, para responder 409 en vez de un 500.
        try {
            return cuentaRepository.save(cuenta);
        } catch (DataIntegrityViolationException e) {
            throw new RecursoDuplicadoException("Ya existe una cuenta con numero " + request.numero(), e);
        }
    }

    /**
     * PUT: actualiza numero y descubierto. El saldo no se toca.
     */
    public Cuenta actualizar(Long id, ActualizarCuentaRequest request) {
        Cuenta cuenta = buscarPorId(id);

        // Si cambia el numero, no puede chocar con el de otra cuenta.
        boolean cambiaNumero = !cuenta.getNumero().equals(request.numero());
        if (cambiaNumero && cuentaRepository.existsByNumero(request.numero())) {
            throw new RecursoDuplicadoException("Ya existe una cuenta con numero " + request.numero());
        }
        cuenta.setNumero(request.numero());

        if (request.descubierto() != null) {
            // Pattern matching: solo las cuentas corrientes tienen descubierto.
            if (cuenta instanceof CuentaCorriente cc) {
                cc.setDescubierto(request.descubierto());
            } else {
                throw new IllegalArgumentException("Solo las cuentas corrientes tienen descubierto");
            }
        }

        // save() sobre un objeto que ya tiene id genera un UPDATE, no un INSERT.
        return cuentaRepository.save(cuenta);
    }

    /**
     * DELETE: baja logica (por defecto) o fisica.
     * Regla de negocio: no se puede eliminar una cuenta con saldo distinto de 0.
     */
    public void eliminar(Long id, String modo) {
        if (!modo.equals("logico") && !modo.equals("fisico")) {
            throw new IllegalArgumentException("El modo debe ser 'logico' o 'fisico'");
        }

        Cuenta cuenta = buscarPorId(id);   // 404 si no existe o ya estaba dada de baja

        // compareTo != 0: el saldo no es exactamente cero (positivo o negativo).
        if (cuenta.getSaldo().compareTo(BigDecimal.ZERO) != 0) {
            throw new IllegalArgumentException(
                    "No se puede eliminar una cuenta con saldo " + cuenta.getSaldo() + ". Debe quedar en 0.");
        }

        if (modo.equals("logico")) {
            cuenta.setActiva(false);           // la fila queda en la base, marcada como inactiva
            cuentaRepository.save(cuenta);
        } else {
            cuentaRepository.delete(cuenta);   // DELETE FROM cuentas WHERE id = ?
        }
    }

    /**
     * Convierte el saldo de la cuenta a dolares usando la API externa DolarApi.
     * Se usa la cotizacion de VENTA: es lo que se paga por cada dolar.
     */
    public SaldoUsdResponse consultarSaldoEnDolares(Long id, String casa) {
        Cuenta cuenta = buscarPorId(id);                                    // 404 si no existe
        CotizacionDolar cotizacion = dolarApiClient.obtenerCotizacion(casa); // 400 o 503 si falla

        // divide con 2 decimales y redondeo bancario (HALF_EVEN)
        BigDecimal saldoUsd = cuenta.getSaldo().divide(cotizacion.venta(), 2, RoundingMode.HALF_EVEN);

        return new SaldoUsdResponse(
                cuenta.getId(),
                cuenta.getNumero(),
                cuenta.getSaldo(),
                cotizacion.casa(),
                cotizacion.venta(),
                saldoUsd,
                cotizacion.fechaActualizacion());
    }

    private BigDecimal valorOCero(BigDecimal valor) {
        return valor == null ? BigDecimal.ZERO : valor;
    }
}
