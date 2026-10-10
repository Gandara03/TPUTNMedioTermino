package ar.edu.utn.diplomatura.fintech.repositorios;

import ar.edu.utn.diplomatura.fintech.entidades.Cuenta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio de cuentas
 */
public interface CuentaRepository extends JpaRepository<Cuenta, Long> {

    // SELECT * FROM cuentas WHERE activa = true
    List<Cuenta> findByActivaTrue();

    // SELECT * FROM cuentas WHERE id = ? AND activa = true
    Optional<Cuenta> findByIdAndActivaTrue(Long id);

    // SELECT * FROM cuentas WHERE usuario_id = ? AND activa = true
    List<Cuenta> findByTitularIdAndActivaTrue(Long usuarioId);

    // SELECT COUNT(*) > 0 FROM cuentas WHERE numero = ?
    boolean existsByNumero(String numero);
}
