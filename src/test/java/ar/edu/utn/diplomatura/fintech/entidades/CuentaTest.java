package ar.edu.utn.diplomatura.fintech.entidades;

import ar.edu.utn.diplomatura.fintech.excepciones.SaldoInsuficienteException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests de la logica de negocio de las cuentas, sin Spring ni mocks.
 * Se compara BigDecimal con compareTo porque equals distingue 100 de 100.00.
 */
class CuentaTest {

    private Usuario titular;

    @BeforeEach
    void setUp() {
        titular = new Usuario("Sofía", "Martínez", "27356789011", "sofia.martinez@gmail.com");
    }

    @Test
    @DisplayName("Depositar suma el monto al saldo")
    void depositarSumaAlSaldo() {
        CajaDeAhorro caja = new CajaDeAhorro("0005-0001", titular);
        caja.depositar(new BigDecimal("25000"));
        assertEquals(0, new BigDecimal("25000").compareTo(caja.getSaldo()));
    }

    @Test
    @DisplayName("No se puede depositar un monto negativo o cero")
    void depositarMontoInvalidoLanzaExcepcion() {
        CajaDeAhorro caja = new CajaDeAhorro("0005-0001", titular);

        assertThrows(IllegalArgumentException.class, () -> caja.depositar(new BigDecimal("-500")));
        assertThrows(IllegalArgumentException.class, () -> caja.depositar(BigDecimal.ZERO));
    }

    @Test
    @DisplayName("Caja de ahorro: no se puede retirar mas que el saldo")
    void cajaDeAhorroNoPermiteRetirarMasQueElSaldo() {
        CajaDeAhorro caja = new CajaDeAhorro("0005-0001", titular);
        caja.depositar(new BigDecimal("80000"));

        assertThrows(SaldoInsuficienteException.class, () -> caja.retirar(new BigDecimal("80000.01")));
        assertEquals(0, new BigDecimal("80000").compareTo(caja.getSaldo()));
    }

    @Test
    @DisplayName("Cuenta corriente: puede quedar en negativo hasta el descubierto")
    void cuentaCorrientePermiteUsarElDescubierto() {
        // limite = 15000 + 50000 = 65000
        CuentaCorriente cc = new CuentaCorriente("0005-0002", titular, new BigDecimal("50000"));
        cc.depositar(new BigDecimal("15000"));

        cc.retirar(new BigDecimal("65000"));

        assertEquals(0, new BigDecimal("-50000").compareTo(cc.getSaldo()));
        assertThrows(SaldoInsuficienteException.class, () -> cc.retirar(new BigDecimal("1")));
    }

    @Test
    @DisplayName("Polimorfismo: cada tipo calcula su limite de retiro")
    void limiteDeRetiroSegunTipoDeCuenta() {
        Cuenta caja = new CajaDeAhorro("0005-0001", titular);
        Cuenta cc = new CuentaCorriente("0005-0002", titular, new BigDecimal("50000"));
        caja.depositar(new BigDecimal("120000"));
        cc.depositar(new BigDecimal("120000"));

        assertEquals(0, new BigDecimal("120000").compareTo(caja.calcularLimiteRetiro()));
        assertEquals(0, new BigDecimal("170000").compareTo(cc.calcularLimiteRetiro()));
    }

    @Test
    @DisplayName("Caja de ahorro: interes mensual = saldo x 20% / 12")
    void interesMensualDeCajaDeAhorro() {
        CajaDeAhorro caja = new CajaDeAhorro("0005-0001", titular);
        caja.depositar(new BigDecimal("600000"));

        caja.acreditarInteres();   // metodo default de GeneraInteres

        // 600000 x 0.20 / 12 = 10000 -> saldo 610000
        assertEquals(0, new BigDecimal("610000").compareTo(caja.getSaldo()));
    }
}
