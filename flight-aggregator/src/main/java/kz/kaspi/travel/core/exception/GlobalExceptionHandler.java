package kz.kaspi.travel.core.exception;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
@RestControllerAdvice
public class GlobalExceptionHandler {
@ExceptionHandler(Exception.class)
public ProblemDetail handleException(Exception e) {
return ProblemDetail.forStatusAndDetail(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
}
}
