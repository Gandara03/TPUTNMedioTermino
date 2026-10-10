package ar.edu.utn.diplomatura.fintech.servicios;

import ar.edu.utn.diplomatura.fintech.dto.ActualizarCuentaRequest;
import ar.edu.utn.diplomatura.fintech.dto.CrearCuentaRequest;
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
import java.util.List;

/**
 * Logica de negocio de cuentas.
 */
@Service
public class CuentaService {

    private final CuentaRepository cuentaRepository;
    private final UsuarioService usuarioService;

    public CuentaService(CuentaRepository cuentaRepository, UsuarioService usuarioService) {
        this.cuentaRepository = cuentaRepository;
        this.usuarioService = usuarioService;
    }

    /** Solo las activas: las dadas de baja logica no se listan. */
    public List<Cuenta> listarTodas() {
        return cuentaRepository.findByActivaTrue();
    }

    /** Cuentas activas de un usuario. Si el usuario no existe -> 404. */
    public List<Cuenta> listarPorUsuario(Long usuarioId) {
        usuarioService.buscarPorId(usuarioId);
        return cuentaRepository.findByTitularIdAndActivaTrue(usuarioId);
    }

    /** Si no existe o fue dada de baja -> 404. */
    public Cuenta buscarPorId(Long id) {
        return cuentaRepository.findByIdAndActivaTrue(id)
                .orElseThrow(() -> new CuentaNoEncontradaException(id));
    }

    /** Segun el tipo del DTO se crea una CajaDeAhorro o una CuentaCorriente (Cuenta es abstracta). */
    public Cuenta crear(CrearCuentaRequest request) {
        if (cuentaRepository.existsByNumero(request.numero())) {
            throw new RecursoDuplicadoException("Ya existe una cuenta con numero " + request.numero());
        }

        Usuario titular = usuarioService.buscarPorId(request.usuarioId());

        Cuenta cuenta = switch (request.tipo()) {
            case CAJA_AHORRO -> new CajaDeAhorro(request.numero(), titular);
            case CUENTA_CORRIENTE -> new CuentaCorriente(request.numero(), titular, valorOCero(request.descubierto()));
        };

        // el saldo inicial entra como un deposito
        BigDecimal saldoInicial = valorOCero(request.saldoInicial());
        if (saldoInicial.compareTo(BigDecimal.ZERO) > 0) {
            cuenta.depositar(saldoInicial);
        }

        // try/catch: si dos pedidos llegan  con el mismo numero, la base rechaza el segundo -> 409
        try {
            return cuentaRepository.save(cuenta);
        } catch (DataIntegrityViolationException e) {
            throw new RecursoDuplicadoException("Ya existe una cuenta con numero " + request.numero(), e);
        }
    }

    /** PUT: actualiza numero y descubierto. El saldo no se modifica. */
    public Cuenta actualizar(Long id, ActualizarCuentaRequest request) {
        Cuenta cuenta = buscarPorId(id);

        boolean cambiaNumero = !cuenta.getNumero().equals(request.numero());
        if (cambiaNumero && cuentaRepository.existsByNumero(request.numero())) {
            throw new RecursoDuplicadoException("Ya existe una cuenta con numero " + request.numero());
        }
        cuenta.setNumero(request.numero());

        if (request.descubierto() != null) {
            // solo las cuentas corrientes tienen descubierto
            if (cuenta instanceof CuentaCorriente cc) {
                cc.setDescubierto(request.descubierto());
            } else {
                throw new IllegalArgumentException("Solo las cuentas corrientes tienen descubierto");
            }
        }

        return cuentaRepository.save(cuenta);
    }

    /** DELETE: baja logica (por defecto) o fisica. Solo cuentas con saldo 0. */
    public void eliminar(Long id, String modo) {
        if (!modo.equals("logico") && !modo.equals("fisico")) {
            throw new IllegalArgumentException("El modo debe ser 'logico' o 'fisico'");
        }

        Cuenta cuenta = buscarPorId(id);

        if (cuenta.getSaldo().compareTo(BigDecimal.ZERO) != 0) {
            throw new IllegalArgumentException(
                    "No se puede eliminar una cuenta con saldo " + cuenta.getSaldo() + ". Debe quedar en 0.");
        }

        if (modo.equals("logico")) {
            cuenta.setActiva(false);   // queda en la base como inactiva
            cuentaRepository.save(cuenta);
        } else {
            cuentaRepository.delete(cuenta);
        }
    }

    private BigDecimal valorOCero(BigDecimal valor) {
        return valor == null ? BigDecimal.ZERO : valor;
    }
}
