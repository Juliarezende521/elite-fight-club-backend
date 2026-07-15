package Tic.Implementacao.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

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

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiErrorResponse> tratarRuntime(RuntimeException ex) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        return ResponseEntity.status(status).body(new ApiErrorResponse(
                status.value(),
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
}
