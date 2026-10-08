package ar.edu.utn.diplomatura.fintech.entidades;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * Un usuario (cliente) de la fintech.
 *
 * @Entity le dice a JPA que esta clase se guarda en una tabla:
 * la clase es la tabla, cada atributo es una columna y cada objeto es una fila.
 *
 * @Data (Lombok) genera getters, setters, toString, equals y hashCode.
 * @NoArgsConstructor genera el constructor vacio que JPA necesita.
 */
@Schema(description = "Usuario titular de cuentas")
@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
public class Usuario {

    // El id lo genera la base sola. Por eso no se manda al crear un usuario.
    @Schema(description = "Id generado por la base. No se envia al crear", example = "1",
            accessMode = Schema.AccessMode.READ_ONLY)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // nullable = false: la columna es NOT NULL en la base.
    @Schema(description = "Nombre", example = "Ana", requiredMode = Schema.RequiredMode.REQUIRED)
    @Column(nullable = false)
    private String nombre;

    @Schema(description = "Apellido", example = "Gomez", requiredMode = Schema.RequiredMode.REQUIRED)
    @Column(nullable = false)
    private String apellido;

    // unique = true: no puede haber dos usuarios con el mismo DNI.
    @Schema(description = "DNI sin puntos, unico", example = "30111222", requiredMode = Schema.RequiredMode.REQUIRED)
    @Column(nullable = false, unique = true)
    private String dni;

    @Schema(description = "Email, unico", example = "ana@mail.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @Column(nullable = false, unique = true)
    private String email;

    /**
     * Un usuario tiene VARIAS cuentas (uno a muchos).
     *
     * mappedBy = "titular": la relacion la maneja el atributo "titular" de Cuenta,
     *   que es quien guarda la columna usuario_id. Aca no se crea ninguna columna.
     * cascade = ALL: si se guarda o borra el usuario, se guardan o borran sus cuentas.
     * orphanRemoval = true: si se saca una cuenta de esta lista, se borra de la base.
     *
     * @ToString.Exclude y @EqualsAndHashCode.Exclude: evitan un bucle infinito
     *   (Usuario -> Cuenta -> Usuario -> ...) en los metodos que genera @Data.
     * @JsonIgnore: el mismo bucle pero al armar el JSON. Las cuentas se consultan por /cuentas.
     */
    @OneToMany(mappedBy = "titular", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonIgnore
    private List<Cuenta> cuentas = new ArrayList<>();

    public Usuario(String nombre, String apellido, String dni, String email) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.dni = dni;
        this.email = email;
    }

    /** Agrega una cuenta y mantiene sincronizados los dos lados de la relacion. */
    public void agregarCuenta(Cuenta cuenta) {
        cuentas.add(cuenta);
        cuenta.setTitular(this);
    }
}
