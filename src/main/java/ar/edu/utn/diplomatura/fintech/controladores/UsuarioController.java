package ar.edu.utn.diplomatura.fintech.controladores;

import ar.edu.utn.diplomatura.fintech.dto.ErrorResponse;
import ar.edu.utn.diplomatura.fintech.entidades.Usuario;
import ar.edu.utn.diplomatura.fintech.servicios.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Puerta de entrada HTTP para usuarios. Recibe la peticion, llama al service
 * y devuelve el resultado. No lleva reglas de negocio.
 *
 * Anotaciones de Swagger:
 * @Tag: agrupa los endpoints bajo "Usuarios" en la UI.
 * @Operation: titulo y descripcion de cada endpoint.
 * @ApiResponse: cada codigo HTTP posible y que devuelve.
 * @Parameter: describe un parametro de la URL.
 */
@Tag(name = "Usuarios")
@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @Operation(summary = "Listar usuarios", description = "Devuelve todos los usuarios registrados.")
    @ApiResponse(responseCode = "200", description = "Lista de usuarios (puede estar vacia)")
    @GetMapping
    public List<Usuario> listar() {
        return usuarioService.listarTodos();
    }

    @Operation(summary = "Buscar usuario por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario encontrado"),
            @ApiResponse(responseCode = "404", description = "No existe un usuario con ese id",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public Usuario buscarPorId(@Parameter(description = "Id del usuario", example = "1") @PathVariable Long id) {
        return usuarioService.buscarPorId(id);
    }

    @Operation(summary = "Crear usuario",
            description = "Registra un usuario nuevo. El id lo genera la base; si se envia, se ignora.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuario creado"),
            @ApiResponse(responseCode = "409", description = "Ya existe un usuario con ese DNI o email",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Usuario crear(@RequestBody Usuario usuario) {
        return usuarioService.crear(usuario);
    }
}
