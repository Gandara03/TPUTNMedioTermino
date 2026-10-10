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
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * Usuario (cliente) de la fintech. Se identifica por su CUIT/CUIL, ya que DNI puede repetirse.
 * Un usuario puede tener varias cuentas.
 */
@Schema(description = "Usuario titular de cuentas")
@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
public class Usuario {

    @Schema(description = "Id generado por la base. No se envia al crear", example = "1",
            accessMode = Schema.AccessMode.READ_ONLY)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Schema(description = "Nombre", example = "Alexis", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "El nombre es obligatorio")
    @Column(nullable = false)
    private String nombre;

    @Schema(description = "Apellido", example = "Gandara", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "El apellido es obligatorio")
    @Column(nullable = false)
    private String apellido;

    // CUIT/CUIL: 11 digitos sin guiones, no se puede repetir
    @Schema(description = "CUIT/CUIL sin guiones (11 digitos), unico", example = "20450703398",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "El CUIT/CUIL es obligatorio")
    @Pattern(regexp = "\\d{11}", message = "El CUIT/CUIL debe tener 11 digitos, sin guiones")
    @Column(nullable = false, unique = true)
    private String cuit;

    @Schema(description = "Email, unico", example = "alexis20143@gmail.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email no tiene un formato valido")
    @Column(nullable = false, unique = true)
    private String email;

    /**
     * Un usuario tiene VARIAS cuentas (one to many).
     * mappedBy = "titular": la relacion la maneja Cuenta (columna usuario_id).
     * Exclude y JsonIgnore: evitan el bucle infinito Usuario -> Cuenta -> Usuario.
     */
    @OneToMany(mappedBy = "titular", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonIgnore
    private List<Cuenta> cuentas = new ArrayList<>();

    public Usuario(String nombre, String apellido, String cuit, String email) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.cuit = cuit;
        this.email = email;
    }

    /** Agrega la cuenta y le asigna este usuario como titular. */
    public void agregarCuenta(Cuenta cuenta) {
        cuentas.add(cuenta);
        cuenta.setTitular(this);
    }
}
