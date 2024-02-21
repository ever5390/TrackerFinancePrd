package com.disqueprogrammer.app.trackerfinance.exception;

import com.disqueprogrammer.app.trackerfinance.dto.HttpResponse;

import com.disqueprogrammer.app.trackerfinance.exception.domain.*;
import com.disqueprogrammer.app.trackerfinance.exception.generic.CustomException;
import com.disqueprogrammer.app.trackerfinance.exception.generic.ObjectExistsException;
import com.disqueprogrammer.app.trackerfinance.exception.generic.ObjectNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.firewall.HttpStatusRequestRejectedHandler;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(AccountEqualsException.class)
    public ResponseEntity<HttpResponse> accountEqualsException (AccountEqualsException exception) {
        return createHttpResponse(exception.getMessage());
    }
    @ExceptionHandler(AccountExistsException.class)
    public ResponseEntity<HttpResponse> accountExistException (AccountExistsException exception) {
        return createHttpResponse(exception.getMessage());
    }

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<HttpResponse> accountNotFoundException (AccountNotFoundException exception) {
        return createHttpResponse(exception.getMessage());
    }

    @ExceptionHandler(CategoryExistsException.class)
    public ResponseEntity<HttpResponse> categoryExistException (CategoryExistsException exception) {
        return createHttpResponse(exception.getMessage());
    }

    @ExceptionHandler(CategoryNotFoundException.class)
    public ResponseEntity<HttpResponse> categoryNotFoundException (CategoryNotFoundException exception) {
        return createHttpResponse(exception.getMessage());
    }

    @ExceptionHandler(InsuficientFundsException.class)
    public ResponseEntity<HttpResponse> insuficientFundsException (InsuficientFundsException exception) {
        return createHttpResponse(exception.getMessage());
    }

    @ExceptionHandler(NotAllowedAccountBalanceException.class)
    public ResponseEntity<HttpResponse> notAllowedAccountBalanceException (NotAllowedAccountBalanceException exception) {
        return createHttpResponse(exception.getMessage());
    }

    @ExceptionHandler(UnspecifiedMemberException.class)
    public ResponseEntity<HttpResponse> unspecifiedMemberException (UnspecifiedMemberException exception) {
        return createHttpResponse(exception.getMessage());
    }

    @ExceptionHandler(ObjectExistsException.class)
    public ResponseEntity<HttpResponse> objectExistsException (ObjectExistsException exception) {
        return createHttpResponse(exception.getMessage());
    }

    @ExceptionHandler(ObjectNotFoundException.class)
    public ResponseEntity<HttpResponse> objectNotFoundException (ObjectNotFoundException exception) {
        return createHttpResponse(exception.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<HttpResponse> accessDeniedException (AccessDeniedException exception) {
        return createHttpResponse(exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<HttpResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        /*
        Map<String, List<String>> errors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();

            errors.computeIfAbsent(fieldName, key -> {
                return ex.getBindingResult().getFieldErrors(fieldName).stream()
                        .map(FieldError::getDefaultMessage)
                        .collect(Collectors.toList());
            }).add(errorMessage);
        });

        try {
            String jsonErrors = objectMapper.writeValueAsString(errors);
            return createHttpResponse(jsonErrors);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            return createHttpResponse("Ocurrió un error al intentar procesar la operación, revise los datos de su solicitud e intente nuevamente.");
        }

         */
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList());

        // Construye un mensaje de error
        String errorMessage = "Error de validación. Por favor, corrija los siguientes errores: " + errors;
        return createHttpResponse(errorMessage);


    }

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<HttpResponse> customException (CustomException exception) {
        return createHttpResponse(exception.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<HttpResponse> httpMessageNotReadableException (HttpMessageNotReadableException exception) {
        String message = "Ocurríó un problema al intentar procesar alguno de los datos ingresados, por favor corríjalo e intentelo nuevamente.";
        String value = extractValueFromError(exception.getMostSpecificCause().getLocalizedMessage());
        if(!value.isEmpty()) message = "No se pudo procesar el valor: [" + value + "], por favor corríjalo y vuelva a intentarlo.";
        return createHttpResponse(message);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<HttpResponse> methodArgumentTypeMismatchException (MethodArgumentTypeMismatchException exception) {
        String message = "Ocurríó un problema al intentar procesar los datos ingresados por argumento en el recurso solicitado, por favor corríjalo e intentelo nuevamente. En caso persista contáctese con su proveedor";
        String value = extractValueFromError(exception.getMostSpecificCause().getLocalizedMessage());
        if(!value.isEmpty()) message = "No se pudo procesar el valor: [" + value + "], por favor corríjalo y vuelva a intentarlo. En caso persista contáctese con su proveedor";
        return createHttpResponse(message);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<HttpResponse> internalServerError (Exception exception) {
        return createHttpResponse(exception.getMessage());
    }

    private ResponseEntity<HttpResponse> createHttpResponse(String message) {
        return new ResponseEntity<>(new HttpResponse(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase().toUpperCase(), message.toUpperCase()), HttpStatus.BAD_REQUEST);
    }

    private final ObjectMapper objectMapper;
    public GlobalExceptionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }


    public String extractValueFromError(String errorMessage) {
        LOGGER.error(errorMessage);
        // Definir el patrón regex para extraer los valores
        Pattern pattern = Pattern.compile("\\\"([^\"]*)\\\"");
        Matcher matcher = pattern.matcher(errorMessage);

        // Buscar y extraer todos los valores en la cadena
        while (matcher.find()) {
            return matcher.group(1);// Obtener el primer grupo capturado
        }

        return "";
    }

}
