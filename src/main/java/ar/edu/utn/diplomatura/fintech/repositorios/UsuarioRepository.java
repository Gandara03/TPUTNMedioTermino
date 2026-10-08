package ar.edu.utn.diplomatura.fintech.repositorios;

import ar.edu.utn.diplomatura.fintech.entidades.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Acceso a la tabla usuarios.
 *
 * Al extender JpaRepository, Spring genera la implementacion solo y ya vienen
 * hechos findAll(), findById(), save(), deleteById(), existsById() y count().
 *
 * Los genericos son <Entidad, TipoDelId>.
 */
// No lleva @Repository: Spring Data detecta solo las interfaces que extienden JpaRepository.
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Spring arma el SQL leyendo el nombre del metodo:
    // SELECT COUNT(*) > 0 FROM usuarios WHERE dni = ?
    boolean existsByDni(String dni);
}
