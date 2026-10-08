package ar.edu.utn.diplomatura.fintech.servicios;

import ar.edu.utn.diplomatura.fintech.componentes.DolarApiClient;
import ar.edu.utn.diplomatura.fintech.dto.CotizacionDolar;
import ar.edu.utn.diplomatura.fintech.dto.CrearCuentaRequest;
import ar.edu.utn.diplomatura.fintech.dto.SaldoUsdResponse;
import ar.edu.utn.diplomatura.fintech.entidades.CajaDeAhorro;
import ar.edu.utn.diplomatura.fintech.entidades.Cuenta;
import ar.edu.utn.diplomatura.fintech.entidades.CuentaCorriente;
import ar.edu.utn.diplomatura.fintech.entidades.TipoCuenta;
import ar.edu.utn.diplomatura.fintech.entidades.Usuario;
import ar.edu.utn.diplomatura.fintech.excepciones.CuentaNoEncontradaException;
import ar.edu.utn.diplomatura.fintech.excepciones.RecursoDuplicadoException;
import ar.edu.utn.diplomatura.fintech.excepciones.ServicioExternoException;
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
 * Tests unitarios del CuentaService con Mockito (Clase 8).
 *
 * @ExtendWith(MockitoExtension.class): activa Mockito en JUnit 5.
 * @Mock: crea un objeto simulado. No toca la base: devuelve lo que le indiquemos con when().
 * @InjectMocks: crea el CuentaService real y le inyecta los mocks por constructor.
 *
 * Asi probamos SOLO la logica del service, aislada del repositorio y de MySQL.
 */
@ExtendWith(MockitoExtension.class)
class CuentaServiceTest {

    @Mock
    private CuentaRepository cuentaRepository;

    @Mock
    private UsuarioService usuarioService;

    // Mock de la API externa: los tests no salen a internet.
    @Mock
    private DolarApiClient dolarApiClient;

    @InjectMocks
    private CuentaService cuentaService;

    private Usuario titular;

    @BeforeEach
    void setUp() {
        titular = new Usuario("Ana", "Gomez", "30111222", "ana@mail.com");
        titular.setId(1L);
    }

    @Test
    @DisplayName("Crear: con tipo CUENTA_CORRIENTE instancia una CuentaCorriente con saldo y descubierto")
    void crearCuentaCorriente() {
        // Given
        CrearCuentaRequest request = new CrearCuentaRequest(
                TipoCuenta.CUENTA_CORRIENTE, "0001-0002", 1L, new BigDecimal("5000"), new BigDecimal("20000"));
        when(cuentaRepository.existsByNumero("0001-0002")).thenReturn(false);
        when(usuarioService.buscarPorId(1L)).thenReturn(titular);
        // save() devuelve el mismo objeto que recibe
        when(cuentaRepository.save(any(Cuenta.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        Cuenta creada = cuentaService.crear(request);

        // Then: ArgumentCaptor "atrapa" lo que se le paso a save() para inspeccionarlo
        ArgumentCaptor<Cuenta> captor = ArgumentCaptor.forClass(Cuenta.class);
        verify(cuentaRepository).save(captor.capture());
        Cuenta guardada = captor.getValue();

        CuentaCorriente cc = assertInstanceOf(CuentaCorriente.class, guardada);
        assertSame(titular, cc.getTitular());
        assertEquals(0, new BigDecimal("5000").compareTo(cc.getSaldo()));
        assertEquals(0, new BigDecimal("20000").compareTo(cc.getDescubierto()));
        assertSame(guardada, creada);
    }

    @Test
    @DisplayName("Crear: si el numero ya existe lanza RecursoDuplicadoException y no guarda")
    void crearConNumeroRepetido() {
        // Given
        CrearCuentaRequest request = new CrearCuentaRequest(
                TipoCuenta.CAJA_AHORRO, "0001-0001", 1L, null, null);
        when(cuentaRepository.existsByNumero("0001-0001")).thenReturn(true);

        // When + Then
        assertThrows(RecursoDuplicadoException.class, () -> cuentaService.crear(request));
        // verify + never(): comprobamos que NUNCA se llamo a save()
        verify(cuentaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Crear: si la base rechaza el INSERT (UNIQUE) el try/catch lo traduce a RecursoDuplicadoException")
    void crearCuandoLaBaseRechazaElNumero() {
        // Given: el chequeo previo pasa, pero la base tira la excepcion (pedidos simultaneos)
        CrearCuentaRequest request = new CrearCuentaRequest(
                TipoCuenta.CAJA_AHORRO, "0001-0001", 1L, null, null);
        when(cuentaRepository.existsByNumero("0001-0001")).thenReturn(false);
        when(usuarioService.buscarPorId(1L)).thenReturn(titular);
        when(cuentaRepository.save(any(Cuenta.class)))
                .thenThrow(new DataIntegrityViolationException("Duplicate entry '0001-0001'"));

        // When + Then
        RecursoDuplicadoException ex =
                assertThrows(RecursoDuplicadoException.class, () -> cuentaService.crear(request));
        // la causa original se conserva dentro de nuestra excepcion
        assertInstanceOf(DataIntegrityViolationException.class, ex.getCause());
    }

    @Test
    @DisplayName("Buscar por id: si no existe lanza CuentaNoEncontradaException")
    void buscarCuentaInexistente() {
        // Given
        when(cuentaRepository.findByIdAndActivaTrue(99L)).thenReturn(Optional.empty());

        // When + Then
        CuentaNoEncontradaException ex =
                assertThrows(CuentaNoEncontradaException.class, () -> cuentaService.buscarPorId(99L));
        assertEquals("No existe una cuenta con id 99", ex.getMessage());
    }

    @Test
    @DisplayName("Eliminar: no se puede eliminar una cuenta con saldo")
    void eliminarCuentaConSaldo() {
        // Given
        CajaDeAhorro caja = new CajaDeAhorro("0001", titular);
        caja.depositar(new BigDecimal("100"));
        when(cuentaRepository.findByIdAndActivaTrue(1L)).thenReturn(Optional.of(caja));

        // When + Then
        assertThrows(IllegalArgumentException.class, () -> cuentaService.eliminar(1L, "logico"));
        verify(cuentaRepository, never()).save(any());
        verify(cuentaRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Eliminar logico: marca la cuenta como inactiva y la guarda, sin borrarla")
    void eliminarLogico() {
        // Given: cuenta con saldo 0
        CajaDeAhorro caja = new CajaDeAhorro("0001", titular);
        when(cuentaRepository.findByIdAndActivaTrue(1L)).thenReturn(Optional.of(caja));

        // When
        cuentaService.eliminar(1L, "logico");

        // Then
        assertFalse(caja.isActiva());
        verify(cuentaRepository).save(caja);
        verify(cuentaRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Saldo en USD: divide el saldo por la cotizacion de venta de DolarApi")
    void saldoEnDolares() {
        // Given: saldo 154000 y dolar a 1540 -> 100 USD
        CajaDeAhorro caja = new CajaDeAhorro("0001", titular);
        caja.depositar(new BigDecimal("154000"));
        when(cuentaRepository.findByIdAndActivaTrue(1L)).thenReturn(Optional.of(caja));
        when(dolarApiClient.obtenerCotizacion("oficial")).thenReturn(new CotizacionDolar(
                "USD", "oficial", "Oficial", new BigDecimal("1490"), new BigDecimal("1540"), "2026-10-07T16:00:00.000Z"));

        // When
        SaldoUsdResponse respuesta = cuentaService.consultarSaldoEnDolares(1L, "oficial");

        // Then
        assertEquals(0, new BigDecimal("100.00").compareTo(respuesta.saldoUsd()));
        assertEquals(0, new BigDecimal("1540").compareTo(respuesta.cotizacionVenta()));
    }

    @Test
    @DisplayName("Saldo en USD: si DolarApi falla, se propaga ServicioExternoException (503)")
    void saldoEnDolaresConApiExternaCaida() {
        // Given
        CajaDeAhorro caja = new CajaDeAhorro("0001", titular);
        when(cuentaRepository.findByIdAndActivaTrue(1L)).thenReturn(Optional.of(caja));
        when(dolarApiClient.obtenerCotizacion("oficial"))
                .thenThrow(new ServicioExternoException("DolarApi no responde", null));

        // When + Then
        assertThrows(ServicioExternoException.class, () -> cuentaService.consultarSaldoEnDolares(1L, "oficial"));
    }
}
