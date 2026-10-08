package ar.edu.utn.diplomatura.fintech.entidades;

import ar.edu.utn.diplomatura.fintech.excepciones.SaldoInsuficienteException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests unitarios de la LOGICA DE NEGOCIO de las cuentas.
 *
 * No usan Spring, ni base de datos, ni mocks: se crean los objetos con "new"
 * y se prueban sus metodos. Por eso son los mas rapidos (base de la piramide, Clase 7).
 *
 * Cada test sigue la estructura Given - When - Then.
 * compareTo y no equals para BigDecimal: equals distingue 100 de 100.00.
 */
class CuentaTest {

    private Usuario titular;

    // @BeforeEach: se ejecuta antes de CADA test -> cada uno arranca limpio.
    @BeforeEach
    void setUp() {
        titular = new Usuario("Ana", "Gomez", "30111222", "ana@mail.com");
    }

    @Test
    @DisplayName("Depositar suma el monto al saldo")
    void depositarSumaAlSaldo() {
        // Given
        CajaDeAhorro caja = new CajaDeAhorro("0001", titular);
        // When
        caja.depositar(new BigDecimal("1500"));
        // Then
        assertEquals(0, new BigDecimal("1500").compareTo(caja.getSaldo()));
    }

    @Test
    @DisplayName("No se puede depositar un monto negativo o cero")
    void depositarMontoInvalidoLanzaExcepcion() {
        CajaDeAhorro caja = new CajaDeAhorro("0001", titular);

        assertThrows(IllegalArgumentException.class, () -> caja.depositar(new BigDecimal("-10")));
        assertThrows(IllegalArgumentException.class, () -> caja.depositar(BigDecimal.ZERO));
    }

    @Test
    @DisplayName("Caja de ahorro: no se puede retirar mas que el saldo")
    void cajaDeAhorroNoPermiteRetirarMasQueElSaldo() {
        // Given
        CajaDeAhorro caja = new CajaDeAhorro("0001", titular);
        caja.depositar(new BigDecimal("1000"));

        // When + Then: assertThrows ejecuta la lambda y verifica que lance esa excepcion
        assertThrows(SaldoInsuficienteException.class, () -> caja.retirar(new BigDecimal("1000.01")));
        // y el saldo no cambio
        assertEquals(0, new BigDecimal("1000").compareTo(caja.getSaldo()));
    }

    @Test
    @DisplayName("Cuenta corriente: puede quedar en negativo hasta el descubierto")
    void cuentaCorrientePermiteUsarElDescubierto() {
        // Given: saldo 1000 + descubierto 5000 -> limite 6000
        CuentaCorriente cc = new CuentaCorriente("0002", titular, new BigDecimal("5000"));
        cc.depositar(new BigDecimal("1000"));

        // When
        cc.retirar(new BigDecimal("6000"));

        // Then
        assertEquals(0, new BigDecimal("-5000").compareTo(cc.getSaldo()));
        assertThrows(SaldoInsuficienteException.class, () -> cc.retirar(new BigDecimal("1")));
    }

    @Test
    @DisplayName("Polimorfismo: cada tipo calcula su limite de retiro")
    void limiteDeRetiroSegunTipoDeCuenta() {
        // Given: misma plata en las dos cuentas
        Cuenta caja = new CajaDeAhorro("0001", titular);
        Cuenta cc = new CuentaCorriente("0002", titular, new BigDecimal("20000"));
        caja.depositar(new BigDecimal("5000"));
        cc.depositar(new BigDecimal("5000"));

        // When + Then: el mismo metodo da distinto resultado segun la subclase
        assertEquals(0, new BigDecimal("5000").compareTo(caja.calcularLimiteRetiro()));
        assertEquals(0, new BigDecimal("25000").compareTo(cc.calcularLimiteRetiro()));
    }

    @Test
    @DisplayName("Caja de ahorro: interes mensual = saldo x 30% / 12")
    void interesMensualDeCajaDeAhorro() {
        // Given
        CajaDeAhorro caja = new CajaDeAhorro("0001", titular);
        caja.depositar(new BigDecimal("10000"));

        // When
        caja.acreditarInteres();   // metodo default de la interfaz GeneraInteres

        // Then: 10000 x 0.30 / 12 = 250 -> saldo 10250
        assertEquals(0, new BigDecimal("10250").compareTo(caja.getSaldo()));
    }
}
