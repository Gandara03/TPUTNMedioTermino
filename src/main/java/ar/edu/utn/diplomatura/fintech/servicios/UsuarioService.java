package ar.edu.utn.diplomatura.fintech.servicios;

import ar.edu.utn.diplomatura.fintech.entidades.Usuario;
import ar.edu.utn.diplomatura.fintech.excepciones.RecursoDuplicadoException;
import ar.edu.utn.diplomatura.fintech.excepciones.UsuarioNoEncontradoException;
import ar.edu.utn.diplomatura.fintech.repositorios.UsuarioRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Reglas del negocio de usuarios. Esta entre el controlador y el repositorio.
 */
@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    // Inyeccion por constructor: Spring pasa el repositorio solo.
    // Con un solo constructor, @Autowired no hace falta.
    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    /**
     * Devuelve el usuario o lanza UsuarioNoEncontradoException si el id no existe.
     * orElseThrow: si el Optional esta vacio, lanza la excepcion (Clase 10).
     */
    public Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNoEncontradoException(id));
    }

    /**
     * Crea un usuario.
     *
     * TRY/CATCH: el DNI y el email son UNIQUE en la tabla. Si ya existen, la base
     * rechaza el INSERT y Spring lanza DataIntegrityViolationException.
     * Sin el try/catch, el cliente recibiria un 500 con un mensaje de MySQL.
     * Con el try/catch la traducimos a RecursoDuplicadoException -> 409 con un mensaje claro.
     */
    public Usuario crear(Usuario usuario) {
        // El id lo pone la base: si viene uno en el JSON, lo ignoramos.
        usuario.setId(null);
        try {
            return usuarioRepository.save(usuario);
        } catch (DataIntegrityViolationException e) {
            throw new RecursoDuplicadoException(
                    "Ya existe un usuario con DNI " + usuario.getDni() + " o email " + usuario.getEmail(), e);
        }
    }
}
