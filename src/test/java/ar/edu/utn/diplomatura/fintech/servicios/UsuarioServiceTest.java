package ar.edu.utn.diplomatura.fintech.servicios;

import ar.edu.utn.diplomatura.fintech.entidades.Usuario;
import ar.edu.utn.diplomatura.fintech.excepciones.RecursoDuplicadoException;
import ar.edu.utn.diplomatura.fintech.excepciones.UsuarioNoEncontradoException;
import ar.edu.utn.diplomatura.fintech.repositorios.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests unitarios del UsuarioService con el repositorio mockeado.
 */
@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    @DisplayName("Buscar por id: devuelve el usuario si existe")
    void buscarUsuarioExistente() {
        // Given
        Usuario ana = new Usuario("Ana", "Gomez", "30111222", "ana@mail.com");
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(ana));

        // When
        Usuario encontrado = usuarioService.buscarPorId(1L);

        // Then
        assertSame(ana, encontrado);
    }

    @Test
    @DisplayName("Buscar por id: si no existe lanza UsuarioNoEncontradoException")
    void buscarUsuarioInexistente() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        UsuarioNoEncontradoException ex =
                assertThrows(UsuarioNoEncontradoException.class, () -> usuarioService.buscarPorId(99L));
        assertEquals("No existe un usuario con id 99", ex.getMessage());
    }

    @Test
    @DisplayName("Crear: ignora el id que venga en el JSON (lo genera la base)")
    void crearIgnoraElId() {
        // Given: viene un id "trucho" en el pedido
        Usuario nuevo = new Usuario("Ana", "Gomez", "30111222", "ana@mail.com");
        nuevo.setId(500L);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        usuarioService.crear(nuevo);

        // Then: se guardo con id null
        assertNull(nuevo.getId());
        verify(usuarioRepository).save(nuevo);
    }

    @Test
    @DisplayName("Crear: DNI o email repetido -> el try/catch lanza RecursoDuplicadoException")
    void crearUsuarioDuplicado() {
        // Given: la base rechaza el INSERT por la restriccion UNIQUE
        Usuario repetido = new Usuario("Ana", "Gomez", "30111222", "ana@mail.com");
        when(usuarioRepository.save(any(Usuario.class)))
                .thenThrow(new DataIntegrityViolationException("Duplicate entry '30111222'"));

        // When + Then
        assertThrows(RecursoDuplicadoException.class, () -> usuarioService.crear(repetido));
    }
}
