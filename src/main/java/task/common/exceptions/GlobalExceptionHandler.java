package task.common.exceptions;

import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    public static final String ERROR_CODE_PROPERTY = "errorCode";

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        ProblemDetail details = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        details.setTitle("Validation Error");
        details.setDetail("The request content contains invalid data");
        Map<String, List<String>> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            errors.computeIfAbsent(error.getField(), k -> new ArrayList<>())
                    .add(error.getDefaultMessage());
        });
        details.setProperty("errors", errors);
        details.setProperty(ERROR_CODE_PROPERTY, "INVALID_REQUEST_CONTENT");
        return details;
    }

    @ExceptionHandler(InvalidMacAddressException.class)
    public ProblemDetail handleInvalidMac(InvalidMacAddressException ex) {
        return createDetails(HttpStatus.BAD_REQUEST, ex);
    }

    @ExceptionHandler(DeviceAlreadyRegisteredException.class)
    public ProblemDetail handleMacRegistered(DeviceAlreadyRegisteredException ex) {
        return createDetails(HttpStatus.CONFLICT, ex);
    }

    @ExceptionHandler(DeviceNotFoundException.class)
    public ProblemDetail handleDeviceNotFound(DeviceNotFoundException ex) {
        return createDetails(HttpStatus.NOT_FOUND, ex);
    }

    @ExceptionHandler(UplinkNotRegisteredException.class)
    public ProblemDetail handleUplinkNotRegistered(UplinkNotRegisteredException ex) {
        return createDetails(HttpStatus.BAD_REQUEST, ex);
    }

    private static ProblemDetail createDetails(HttpStatus status, MappableRuntimeException ex) {
        var details = ProblemDetail.forStatusAndDetail(status, ex.getDetails());
        details.setTitle(ex.getTitle());
        details.setProperty(ERROR_CODE_PROPERTY, ex.getErrorCode());
        return details;
    }
}
