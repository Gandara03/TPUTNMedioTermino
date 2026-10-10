package ar.edu.utn.diplomatura.fintech.repositorios;

import ar.edu.utn.diplomatura.fintech.entidades.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio de usuarios.
 */
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // SELECT COUNT(*) > 0 FROM usuarios WHERE cuit = ?
    boolean existsByCuit(String cuit);
}
