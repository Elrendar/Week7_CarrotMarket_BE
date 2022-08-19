package com.sparta.carrot.exception;

import com.sparta.carrot.exception.apierror.ApiError;
import com.sparta.carrot.exception.runtime.DuplicateUserInfoException;
import com.sparta.carrot.exception.runtime.InvalidJWTException;
import com.sparta.carrot.exception.runtime.RefreshTokenNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import static org.springframework.http.HttpStatus.*;

@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * 컨트롤러 메서드에 @RequestParam 어노테이션이 있는데 url에는 해당 파라미터가 없을 때 발생
     *
     * @param ex      MissingServletRequestParameterException
     * @param headers HttpHeaders
     * @param status  HttpStatus
     * @param request WebRequest
     * @return the ApiError object
     */
    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex, HttpHeaders headers,
            HttpStatus status, WebRequest request) {
        String error = ex.getParameterName() + " parameter is missing";
        return buildResponseEntity(new ApiError(BAD_REQUEST, error, ex));
    }


    /**
     * 요청의 ContentType이 x-www-form-urlencoded일 때, 컨트롤러 메서드의 매개변수가 Class 형태면 발생
     *
     * @param ex      HttpMediaTypeNotSupportedException
     * @param headers HttpHeaders
     * @param status  HttpStatus
     * @param request WebRequest
     * @return the ApiError object
     */
    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException ex,
            HttpHeaders headers,
            HttpStatus status,
            WebRequest request) {
        StringBuilder builder = new StringBuilder();
        builder.append(ex.getContentType());
        builder.append(" 타입은 지원하지 않습니다. ");
        ex.getSupportedMediaTypes().forEach(t -> builder.append(t).append(", "));
        builder.append("을 사용해 주십시오.  ");
        return buildResponseEntity(new ApiError(HttpStatus.UNSUPPORTED_MEDIA_TYPE, builder.substring(0, builder.length() - 2), ex));
    }

    /**
     * 컨트롤러에서 입력받는 값이 @Valid 검증을 통과하지 못했을 때 발생
     *
     * @param ex      the MethodArgumentNotValidException that is thrown when @Valid validation fails
     * @param headers HttpHeaders
     * @param status  HttpStatus
     * @param request WebRequest
     * @return the ApiError object
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatus status,
            WebRequest request) {
        ApiError apiError = new ApiError(BAD_REQUEST);
        apiError.setMessage("Validation error. 값이 올바르지 않습니다.");
        apiError.addValidationErrors(ex.getBindingResult().getFieldErrors());
        apiError.addValidationError(ex.getBindingResult().getGlobalErrors());
        return buildResponseEntity(apiError);
    }

    /**
     * 요청으로 받은 JSON의 형식이 잘못 되었을 때(ex. key가 누락된 json) 발생
     *
     * @param ex      HttpMessageNotReadableException
     * @param headers HttpHeaders
     * @param status  HttpStatus
     * @param request WebRequest
     * @return the ApiError object
     */
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        ServletWebRequest servletWebRequest = (ServletWebRequest) request;
        log.info("{} to {}", servletWebRequest.getHttpMethod(), servletWebRequest.getRequest().getServletPath());
        String error = "Malformed JSON request." +
                "정상적인 JSON 요청이 아닙니다. 형식이 올바른지 확인하십시오.";
        return buildResponseEntity(new ApiError(BAD_REQUEST, error, ex));
    }

    /**
     * Spring이 반환된 객체의 프로퍼티를 가져올 수 없을 때 발생
     *
     * @param ex      HttpMessageNotWritableException
     * @param headers HttpHeaders
     * @param status  HttpStatus
     * @param request WebRequest
     * @return the ApiError object
     */
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotWritable(HttpMessageNotWritableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        String error =
                "Error writing JSON output. " +
                        "기본 생성자, Getters, Jackson 의존성이 있는지 확인하십시오.";
        return buildResponseEntity(new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, error, ex));
    }

    /**
     * 존재하지 않는 주소로 HTTP 요청을 보낼 때 발생
     *
     * @param ex      NoHandlerFoundException
     * @param headers HttpHeaders
     * @param status  HttpStatus
     * @param request WebRequest
     * @return the ApiError object
     */
    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(
            NoHandlerFoundException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        ApiError apiError = new ApiError(BAD_REQUEST);
        apiError.setMessage(String.format(
                "URL %s 에 대한 %s Method를 찾을 수 없습니다.", ex.getRequestURL(), ex.getHttpMethod()));
        apiError.setDebugMessage(ex.getMessage());
        return buildResponseEntity(apiError);
    }

    /**
     * 인자값에 오류가 있을 때 발생
     *
     * @param ex the Exception
     * @return the ApiError object
     */
    @ExceptionHandler(IllegalArgumentException.class)
    protected ResponseEntity<Object> IllegalArgument(IllegalArgumentException ex) {
        ApiError apiError = new ApiError(NOT_FOUND);
        apiError.setMessage(ex.getMessage());
        apiError.setDebugMessage(ex.getMessage());
        return buildResponseEntity(apiError);
    }

    /**
     * 회원가입 시 DB에 중복된 username, email, nickname이 이미 존재할 경우 발생
     *
     * @param ex the Exception
     * @return the ApiError object
     */
    @ExceptionHandler(DuplicateUserInfoException.class)
    protected ResponseEntity<Object> handleDuplicateUser(DuplicateUserInfoException ex) {
        ApiError apiError = new ApiError(BAD_REQUEST);
        apiError.setMessage(ex.getMessage());
        apiError.setDebugMessage(ex.getMessage());
        return buildResponseEntity(apiError);
    }

    /**
     * DB에서 리프레쉬 토큰을 찾을 수 없을 때 발생
     *
     * @param ex the Exception
     * @return the ApiError object
     */
    @ExceptionHandler(RefreshTokenNotFoundException.class)
    protected ResponseEntity<Object> handleRefreshTokenNotFound(RefreshTokenNotFoundException ex) {
        ApiError apiError = new ApiError(NOT_FOUND);
        apiError.setMessage(ex.getMessage());
        apiError.setDebugMessage(ex.getMessage());
        return buildResponseEntity(apiError);
    }

    /**
     * JWT 토큰 유효성 검증에 실패했을 때 발생
     *
     * @param ex the Exception
     * @return the ApiError object
     */
    @ExceptionHandler(InvalidJWTException.class)
    protected ResponseEntity<Object> handleJWTVerification(InvalidJWTException ex) {
        ApiError apiError = new ApiError(UNAUTHORIZED);
        apiError.setMessage(ex.getMessage());
        apiError.setDebugMessage(ex.getMessage());
        return buildResponseEntity(apiError);
    }

    /**
     * 로그인 아이디, 혹은 비밀번호가 틀렸을 때 발생
     *
     * @param ex the Exception
     * @return the ApiError object
     */
    @ExceptionHandler(BadCredentialsException.class)
    protected ResponseEntity<Object> handleBadCredentials(BadCredentialsException ex) {
        ApiError apiError = new ApiError(BAD_REQUEST);
        apiError.setMessage(ex.getMessage());
        apiError.setDebugMessage(ex.getMessage());
        return buildResponseEntity(apiError);
    }

    /**
     * DB에서 유저 정보를 찾을 수 없을 때 발생
     *
     * @param ex the Exception
     * @return the ApiError object
     */
    @ExceptionHandler(UsernameNotFoundException.class)
    protected ResponseEntity<Object> handleUsernameNotFound(UsernameNotFoundException ex) {
        ApiError apiError = new ApiError(NOT_FOUND);
        apiError.setMessage(ex.getMessage());
        apiError.setDebugMessage(ex.getMessage());
        return buildResponseEntity(apiError);
    }

    private ResponseEntity<Object> buildResponseEntity(ApiError apiError) {
        return new ResponseEntity<>(apiError, apiError.getStatus());
    }

}
