package Tic.Implementacao.dto.professor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ProfessorRequest(
        @NotBlank @Size(max = 255) String nome,
        @NotBlank @Pattern(regexp = "[0-9]{11}", message = "CPF deve conter 11 digitos") String cpf,
        @Size(max = 500) String especialidade,
        @Size(max = 500) String contato) {
}
