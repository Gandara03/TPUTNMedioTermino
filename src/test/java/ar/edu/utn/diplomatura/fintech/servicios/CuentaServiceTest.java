package ar.edu.utn.diplomatura.fintech.servicios;

import ar.edu.utn.diplomatura.fintech.dto.CrearCuentaRequest;
import ar.edu.utn.diplomatura.fintech.entidades.CajaDeAhorro;
import ar.edu.utn.diplomatura.fintech.entidades.Cuenta;
import ar.edu.utn.diplomatura.fintech.entidades.CuentaCorriente;
import ar.edu.utn.diplomatura.fintech.entidades.TipoCuenta;
import ar.edu.utn.diplomatura.fintech.entidades.Usuario;
import ar.edu.utn.diplomatura.fintech.excepciones.CuentaNoEncontradaException;
import ar.edu.utn.diplomatura.fintech.excepciones.RecursoDuplicadoException;
import ar.edu.utn.diplomatura.fintech.excepciones.UsuarioNoEncontradoException;
import ar.edu.utn.diplomatura.fintech.repositorios.CuentaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests de CuentaService con Mockito: el repositorio y UsuarioService son mocks,
 * asi se prueba solo la logica del service sin base de datos.
 */
@ExtendWith(MockitoExtension.class)
class CuentaServiceTest {

    @Mock
    private CuentaRepository cuentaRepository;

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private CuentaService cuentaService;

    private Usuario titular;

    @BeforeEach
    void setUp() {
        titular = new Usuario("Sofía", "Martínez", "27356789011", "sofia.martinez@gmail.com");
        titular.setId(1L);
    }

    @Test
    @DisplayName("Crear: con tipo CUENTA_CORRIENTE instancia una CuentaCorriente con saldo y descubierto")
    void crearCuentaCorriente() {
        CrearCuentaRequest request = new CrearCuentaRequest(
                TipoCuenta.CUENTA_CORRIENTE, "0005-0002", 1L, new BigDecimal("15000"), new BigDecimal("50000"));
        when(cuentaRepository.existsByNumero("0005-0002")).thenReturn(false);
        when(usuarioService.buscarPorId(1L)).thenReturn(titular);
        when(cuentaRepository.save(any(Cuenta.class))).thenAnswer(inv -> inv.getArgument(0));

        Cuenta creada = cuentaService.crear(request);

        // ArgumentCaptor: captura la cuenta que se le paso a save()
        ArgumentCaptor<Cuenta> captor = ArgumentCaptor.forClass(Cuenta.class);
        verify(cuentaRepository).save(captor.capture());
        Cuenta guardada = captor.getValue();

        CuentaCorriente cc = assertInstanceOf(CuentaCorriente.class, guardada);
        assertSame(titular, cc.getTitular());
        assertEquals(0, new BigDecimal("15000").compareTo(cc.getSaldo()));
        assertEquals(0, new BigDecimal("50000").compareTo(cc.getDescubierto()));
        assertSame(guardada, creada);
    }

    @Test
    @DisplayName("Crear: si el numero ya existe lanza RecursoDuplicadoException y no guarda")
    void crearConNumeroRepetido() {
        CrearCuentaRequest request = new CrearCuentaRequest(
                TipoCuenta.CAJA_AHORRO, "0005-0001", 1L, null, null);
        when(cuentaRepository.existsByNumero("0005-0001")).thenReturn(true);

        assertThrows(RecursoDuplicadoException.class, () -> cuentaService.crear(request));
        // never(): save() no se llamo nunca
        verify(cuentaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Crear: si la base rechaza el INSERT (UNIQUE) el try/catch lo traduce a RecursoDuplicadoException")
    void crearCuandoLaBaseRechazaElNumero() {
        // la base rechaza el INSERT (dos pedidos con el mismo numero)
        CrearCuentaRequest request = new CrearCuentaRequest(
                TipoCuenta.CAJA_AHORRO, "0005-0001", 1L, null, null);
        when(cuentaRepository.existsByNumero("0005-0001")).thenReturn(false);
        when(usuarioService.buscarPorId(1L)).thenReturn(titular);
        when(cuentaRepository.save(any(Cuenta.class)))
                .thenThrow(new DataIntegrityViolationException("Duplicate entry '0005-0001'"));

        RecursoDuplicadoException ex =
                assertThrows(RecursoDuplicadoException.class, () -> cuentaService.crear(request));
        assertInstanceOf(DataIntegrityViolationException.class, ex.getCause());
    }

    @Test
    @DisplayName("Buscar por id: si no existe lanza CuentaNoEncontradaException")
    void buscarCuentaInexistente() {
        when(cuentaRepository.findByIdAndActivaTrue(99L)).thenReturn(Optional.empty());

        CuentaNoEncontradaException ex =
                assertThrows(CuentaNoEncontradaException.class, () -> cuentaService.buscarPorId(99L));
        assertEquals("No existe una cuenta con id 99", ex.getMessage());
    }

    @Test
    @DisplayName("Eliminar: no se puede eliminar una cuenta con saldo")
    void eliminarCuentaConSaldo() {
        CajaDeAhorro caja = new CajaDeAhorro("0005-0001", titular);
        caja.depositar(new BigDecimal("2500"));
        when(cuentaRepository.findByIdAndActivaTrue(1L)).thenReturn(Optional.of(caja));

        assertThrows(IllegalArgumentException.class, () -> cuentaService.eliminar(1L, "logico"));
        verify(cuentaRepository, never()).save(any());
        verify(cuentaRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Eliminar logico: marca la cuenta como inactiva y la guarda, sin borrarla")
    void eliminarLogico() {
        CajaDeAhorro caja = new CajaDeAhorro("0005-0001", titular);
        when(cuentaRepository.findByIdAndActivaTrue(1L)).thenReturn(Optional.of(caja));

        cuentaService.eliminar(1L, "logico");

        assertFalse(caja.isActiva());
        verify(cuentaRepository).save(caja);
        verify(cuentaRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Listar por usuario: devuelve solo las cuentas activas de ese usuario")
    void listarCuentasDeUnUsuario() {
        CajaDeAhorro caja = new CajaDeAhorro("0005-0001", titular);
        when(usuarioService.buscarPorId(1L)).thenReturn(titular);
        when(cuentaRepository.findByTitularIdAndActivaTrue(1L)).thenReturn(List.of(caja));

        List<Cuenta> cuentas = cuentaService.listarPorUsuario(1L);

        assertEquals(1, cuentas.size());
        assertSame(caja, cuentas.get(0));
    }

    @Test
    @DisplayName("Listar por usuario: si el usuario no existe lanza UsuarioNoEncontradoException")
    void listarCuentasDeUsuarioInexistente() {
        when(usuarioService.buscarPorId(99L)).thenThrow(new UsuarioNoEncontradoException(99L));

        assertThrows(UsuarioNoEncontradoException.class, () -> cuentaService.listarPorUsuario(99L));
        verify(cuentaRepository, never()).findByTitularIdAndActivaTrue(any());
    }
}
