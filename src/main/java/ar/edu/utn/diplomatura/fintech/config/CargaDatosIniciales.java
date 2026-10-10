package ar.edu.utn.diplomatura.fintech.config;

import ar.edu.utn.diplomatura.fintech.entidades.CajaDeAhorro;
import ar.edu.utn.diplomatura.fintech.entidades.CuentaCorriente;
import ar.edu.utn.diplomatura.fintech.entidades.Usuario;
import ar.edu.utn.diplomatura.fintech.repositorios.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Carga usuarios y cuentas de ejemplo al arrancar, para poder probar la API
 * sin tener que crear todo a mano.
 * Solo carga si la tabla de usuarios esta vacia
 * quedan guardados/persisten , y asi no se duplican en cada arranque.
 */
@Component
public class CargaDatosIniciales implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;

    public CargaDatosIniciales(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public void run(String... args) {
        if (usuarioRepository.count() > 0) {
            return;
        }

        Usuario alexis = new Usuario("Alexis", "Gandara", "20450703398", "alexis20143@gmail.com");
        Usuario lionel = new Usuario("Lionel", "Messi", "20289876546", "lionel.messi@gmail.com");
        Usuario enzo = new Usuario("Enzo", "Fernandez", "20354112230", "enzo.fernandez@gmail.com");

        // Alexis: una caja de ahorro y una cuenta corriente
        CajaDeAhorro cajaAlexis = new CajaDeAhorro("0001-0001", alexis);
        cajaAlexis.depositar(new BigDecimal("152300.50"));
        alexis.agregarCuenta(cajaAlexis);

        CuentaCorriente corrienteAlexis = new CuentaCorriente("0001-0002", alexis, new BigDecimal("50000"));
        corrienteAlexis.depositar(new BigDecimal("8400"));
        alexis.agregarCuenta(corrienteAlexis);

        // Lionel: una caja de ahorro
        CajaDeAhorro cajaLionel = new CajaDeAhorro("0002-0001", lionel);
        cajaLionel.depositar(new BigDecimal("1250000"));
        lionel.agregarCuenta(cajaLionel);

        // Enzo: una caja de ahorro sin saldo y una cuenta dada de baja (no aparece en GET /cuentas)
        enzo.agregarCuenta(new CajaDeAhorro("0003-0001", enzo));
        CajaDeAhorro cerrada = new CajaDeAhorro("0003-0002", enzo);
        cerrada.setActiva(false);
        enzo.agregarCuenta(cerrada);

        usuarioRepository.saveAll(List.of(alexis, lionel, enzo));

        System.out.println("=== Datos iniciales: " + usuarioRepository.count() + " usuarios con sus cuentas ===");
    }
}
