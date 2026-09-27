package com.example.jwt.core.exception;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import java.time.LocalDate;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class CustomGlobalExceptionHandler {


  @ExceptionHandler(HttpClientErrorException.NotFound.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public void handleModuleNotFound(HttpClientErrorException.NotFound ex) {
  }

  @ExceptionHandler(ResourceAccessException.class)
  @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
  public void handleModuleServiceUnavailable(ResourceAccessException ex) {
  }

  @ExceptionHandler(CallNotPermittedException.class)
  @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
  public void handleCircuitBreakerOpen(CallNotPermittedException ex) {
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(value = HttpStatus.BAD_REQUEST)
  public ResponseError handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
    return new ResponseError()
        .setTimeStamp(LocalDate.now())
        .setErrors(ex.getBindingResult().getFieldErrors().stream().collect(
            Collectors.toMap(error -> error.getField(), error -> error.getDefaultMessage())))
        .build();
  }

}


