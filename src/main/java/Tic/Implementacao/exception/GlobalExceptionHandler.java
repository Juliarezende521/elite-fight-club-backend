package Tic.Implementacao.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String GENERIC_MESSAGE = "Não foi possível concluir a operação. Tente novamente.";

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> tratarValidacao(MethodArgumentNotValidException ex) {
        Map<String, String> campos = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> campos.put(error.getField(), error.getDefaultMessage()));

        return ResponseEntity.badRequest().body(new ApiErrorResponse(
                400,
                "VALIDATION_ERROR",
                "Dados inválidos",
                campos
        ));
    }

    @ExceptionHandler(NegocioException.class)
    public ResponseEntity<ApiErrorResponse> tratarNegocio(NegocioException ex) {
        return ResponseEntity.badRequest().body(new ApiErrorResponse(
                400,
                "BUSINESS_ERROR",
                ex.getMessage(),
                Map.of()
        ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> tratarArgumentoInvalido(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(new ApiErrorResponse(
                400,
                "BUSINESS_ERROR",
                ex.getMessage(),
                Map.of()
        ));
    }

    @ExceptionHandler(ConflitoException.class)
    public ResponseEntity<ApiErrorResponse> tratarConflito(ConflitoException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiErrorResponse(
                409,
                "CONFLICT",
                ex.getMessage(),
                Map.of()
        ));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> tratarAcessoNegado(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiErrorResponse(
                403,
                "FORBIDDEN",
                ex.getMessage(),
                Map.of()
        ));
    }

    @ExceptionHandler(AuthenticationCredentialsNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> tratarNaoAutenticado(AuthenticationCredentialsNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiErrorResponse(
                401,
                "UNAUTHORIZED",
                "Autenticação obrigatória.",
                Map.of()
        ));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiErrorResponse> tratarRuntime(RuntimeException ex) {
        logger.error("Erro inesperado na API", ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiErrorResponse(
                400,
                "UNEXPECTED_ERROR",
                GENERIC_MESSAGE,
                Map.of()
        ));
    }
}
