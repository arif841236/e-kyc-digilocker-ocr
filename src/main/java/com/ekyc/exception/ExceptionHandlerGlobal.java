package com.ekyc.exception;

import java.net.ConnectException;
import java.util.List;
import org.springframework.beans.TypeMismatchException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;

/**
 * This class for handle all exception
 */
@ControllerAdvice
@Slf4j
public class ExceptionHandlerGlobal extends ResponseEntityExceptionHandler {

	@Autowired
	Gson gson;

	private static String source = "";

	public static void initSource(String s) {
		source = s;
	}

	@Value("${message.error}")
	private String message;

	@Value("${message.error2}")
	private String message2;

	@ExceptionHandler(KycException.class)
	public ResponseEntity<ErrorResponce> userExceptionHandler(KycException ue, WebRequest wb) {

		ErrorResponce error = new ErrorResponce(HttpStatus.UNPROCESSABLE_ENTITY.value(), ue.getMessage(),wb.getDescription(false).substring(4));

		log.error(message2+ source+" : "+ gson.toJson(error));
		return new ResponseEntity<>(error, HttpStatus.UNPROCESSABLE_ENTITY);
	}

	/**
	 * 
	 * @param nValid :its argument of validate.
	 * @return : its return response entity of Map.
	 */
	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request) {

		List<FieldError> error = ex.getBindingResult().getFieldErrors();
		ErrorResponce otpVException = new ErrorResponce(HttpStatus.BAD_REQUEST.value(),
				error.get(0).getDefaultMessage(),request.getDescription(false).substring(4));

		log.error(message2+ source+" : "+ gson.toJson(error));
		return new ResponseEntity<>(otpVException, HttpStatus.BAD_REQUEST);
	}

	@Override
	protected ResponseEntity<Object> handleTypeMismatch(TypeMismatchException ex, HttpHeaders headers,
			HttpStatus status, WebRequest request) {

		ErrorResponce error = new ErrorResponce(HttpStatus.BAD_REQUEST.value(), ex.getMessage(),request.getDescription(false).substring(4));

		log.error(message2+ source+" : "+ gson.toJson(error));
		return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(NullPointerException.class)
	public final ResponseEntity<Object> nullpointerExceptions(NullPointerException ex, WebRequest request) {

		ErrorResponce error = ErrorResponce.builder()
				.statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
				.path(request.getDescription(false).substring(4))
				.message(ex.getMessage())
				.build();

		log.error(message2+ source+" : "+ gson.toJson(error));
		return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
	}


	@ExceptionHandler(ResourceAccessException.class)
	public final ResponseEntity<Object> resourceAccessException(ResourceAccessException ex, WebRequest request) {

		ErrorResponce error = ErrorResponce.builder()
				.statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
				.path(request.getDescription(false).substring(4))
				.message(ex.getMessage())
				.build();

		log.error(message2+ source+" : "+ gson.toJson(error));
		return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler(ConnectException.class)
	public final ResponseEntity<Object> connectException(ConnectException ex, WebRequest request) {

		ErrorResponce error = ErrorResponce.builder()
				.statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
				.path(request.getDescription(false).substring(4))
				.message(ex.getMessage())
				.build();

		log.error(message2+ source+" : "+ gson.toJson(error));
		return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
	}


	@Override
	protected ResponseEntity<Object> handleHttpMediaTypeNotAcceptable(HttpMediaTypeNotAcceptableException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request) {
		ErrorResponce error = ErrorResponce.builder()
				.statusCode(HttpStatus.BAD_REQUEST.value())
				.path(request.getDescription(false).substring(4))
				.message(ex.getMessage())
				.build();

		log.error(message2+ source+" : "+ gson.toJson(error));
		return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(HttpClientErrorException.class)
	public final ResponseEntity<Object> httpClientErrorException(HttpClientErrorException ex, WebRequest request) {

		String responseBodyAsString = ex.getResponseBodyAsString();
		JsonObject fromJson = gson.fromJson(responseBodyAsString, JsonObject.class);

		String detail = "detail";
		String res = "";
		if(fromJson != null && fromJson.get("error_description") != null) {
			res = fromJson.get("error_description").getAsString();
		}
		else if(ex.getStatusCode().value()==401) {
			res = "The access token provided has expired or invalid";
		}
		else if(fromJson != null && fromJson.get(detail) != null && fromJson.get(detail).getAsJsonObject().get("message") != null ) {
			res = fromJson.get(detail).getAsJsonObject().get("message").getAsString();
		}
		else {
			res = "Some client issue.";
		}
		ErrorResponce error = ErrorResponce.builder()
				.statusCode(ex.getStatusCode().value())
				.path(request.getDescription(false).substring(4))
				.message(res)
				.build();

		log.error(message2+ source+" : "+ gson.toJson(error));

		return new ResponseEntity<>(error, ex.getStatusCode());
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public final ResponseEntity<Object> illegalArgumentExceptionHandler(IllegalArgumentException ex, WebRequest request) {
		ErrorResponce error = new ErrorResponce(HttpStatus.BAD_REQUEST.value(), ex.getMessage(),request.getDescription(false).substring(4));

		log.error(message2+ source+" : "+ gson.toJson(error));

		return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(Exception.class)
	public final ResponseEntity<Object> handleAllExceptions(Exception ex, WebRequest request) {

		ErrorResponce error = ErrorResponce.builder()
				.statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
				.path(request.getDescription(false).substring(4))
				.message(ex.getMessage())
				.build();

		log.error(message2+ source+" : "+ gson.toJson(error));

		return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
