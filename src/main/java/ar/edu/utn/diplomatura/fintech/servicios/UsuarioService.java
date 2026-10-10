package ar.edu.utn.diplomatura.fintech.servicios;

import ar.edu.utn.diplomatura.fintech.entidades.Usuario;
import ar.edu.utn.diplomatura.fintech.excepciones.RecursoDuplicadoException;
import ar.edu.utn.diplomatura.fintech.excepciones.UsuarioNoEncontradoException;
import ar.edu.utn.diplomatura.fintech.repositorios.UsuarioRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Logica de negocio de usuarios.
 */
@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    /** orElseThrow: si no existe lanza UsuarioNoEncontradoException (404). */
    public Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNoEncontradoException(id));
    }

    /**
     * try/catch: si el CUIT/CUIL o el email ya existen, la base rechaza el INSERT.
     * Se traduce a RecursoDuplicadoException (409) en vez de un error 500.
     */
    public Usuario crear(Usuario usuario) {
        // el id lo genera la base
        usuario.setId(null);
        try {
            return usuarioRepository.save(usuario);
        } catch (DataIntegrityViolationException e) {
            throw new RecursoDuplicadoException(
                    "Ya existe un usuario con CUIT/CUIL " + usuario.getCuit() + " o email " + usuario.getEmail(), e);
        }
    }
}
