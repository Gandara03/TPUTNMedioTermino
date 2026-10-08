package ar.edu.utn.diplomatura.fintech.repositorios;

import ar.edu.utn.diplomatura.fintech.entidades.Cuenta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Acceso a la tabla cuentas.
 *
 * Trabaja con la clase abstracta Cuenta: findAll() devuelve tanto
 * CajaDeAhorro como CuentaCorriente (JPA mira la columna tipo_cuenta).
 */
// No lleva @Repository: Spring Data detecta solo las interfaces que extienden JpaRepository.
public interface CuentaRepository extends JpaRepository<Cuenta, Long> {

    // SELECT * FROM cuentas WHERE usuario_id = ?
    // "Titular_Id" = atributo titular de Cuenta -> su atributo id.
    List<Cuenta> findByTitularId(Long usuarioId);

    // SELECT * FROM cuentas WHERE activa = true
    List<Cuenta> findByActivaTrue();

    // SELECT * FROM cuentas WHERE id = ? AND activa = true
    Optional<Cuenta> findByIdAndActivaTrue(Long id);

    // SELECT COUNT(*) > 0 FROM cuentas WHERE numero = ?
    boolean existsByNumero(String numero);
}
