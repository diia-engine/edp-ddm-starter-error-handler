/*
 * Copyright 2021 EPAM Systems.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.digital.data.platform.starter.errorhandling;

import com.epam.digital.data.platform.starter.errorhandling.dto.SystemErrorDto;
import com.epam.digital.data.platform.starter.errorhandling.dto.ValidationErrorDto;
import com.epam.digital.data.platform.starter.errorhandling.exception.ConstraintViolationException;
import com.epam.digital.data.platform.starter.errorhandling.exception.ForbiddenOperationException;
import com.epam.digital.data.platform.starter.errorhandling.exception.RestSystemException;
import com.epam.digital.data.platform.starter.errorhandling.exception.SoapSystemException;
import com.epam.digital.data.platform.starter.errorhandling.exception.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@ControllerAdvice
public class BaseRestExceptionHandler extends ResponseEntityExceptionHandler {

  public static final String INTERNAL_SERVER_ERROR = "Internal server error";
  public static final String ACCESS_IS_DENIED = "Access is denied";
  public static final String TRACE_ID_KEY = "X-B3-TraceId";

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<SystemErrorDto> handleAccessDeniedException(AccessDeniedException ex) {
    var error = SystemErrorDto.builder()
        .traceId(MDC.get(BaseRestExceptionHandler.TRACE_ID_KEY))
        .code(String.valueOf(HttpStatus.FORBIDDEN.value()))
        .message(BaseRestExceptionHandler.ACCESS_IS_DENIED)
        .build();
    log.warn(BaseRestExceptionHandler.ACCESS_IS_DENIED, ex);
    return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
  }

  @ExceptionHandler(ForbiddenOperationException.class)
  public ResponseEntity<SystemErrorDto> handleForbiddenOperationException(ForbiddenOperationException ex) {
    var error = SystemErrorDto.builder()
            .traceId(MDC.get(BaseRestExceptionHandler.TRACE_ID_KEY))
            .code(String.valueOf(HttpStatus.FORBIDDEN.value()))
            .message(BaseRestExceptionHandler.ACCESS_IS_DENIED)
            .build();
    log.warn(BaseRestExceptionHandler.ACCESS_IS_DENIED, ex);
    return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<SystemErrorDto> handleConstraintViolationException(ConstraintViolationException ex) {
    var error = SystemErrorDto.builder()
        .traceId(MDC.get(BaseRestExceptionHandler.TRACE_ID_KEY))
        .code(ex.getCode())
        .message(ex.getMessage())
        .localizedMessage(ex.getLocalizedMessage())
        .build();
    log.warn("Constraint violation error", ex);
    return new ResponseEntity<>(error, HttpStatus.CONFLICT);
  }

  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<SystemErrorDto> handleRuntimeException(RuntimeException ex) {
    var error = SystemErrorDto.builder().traceId(MDC.get(TRACE_ID_KEY))
        .code(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()))
        .message(INTERNAL_SERVER_ERROR).build();
    log.error(INTERNAL_SERVER_ERROR, ex);
    return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler(ValidationException.class)
  public ResponseEntity<ValidationErrorDto> handleValidationException(ValidationException ex) {
    var error = ValidationErrorDto.builder()
        .traceId(MDC.get(TRACE_ID_KEY))
        .code(ex.getCode())
        .message(ex.getMessage())
        .details(ex.getDetails())
        .build();
    log.warn("User data validation is not passed", ex);
    return new ResponseEntity<>(error, HttpStatus.UNPROCESSABLE_ENTITY);
  }

  @ExceptionHandler(RestSystemException.class)
  public ResponseEntity<SystemErrorDto> handleRestSystemException(RestSystemException ex) {
    var error = SystemErrorDto.builder()
        .traceId(MDC.get(TRACE_ID_KEY))
        .code(ex.getCode())
        .message(ex.getMessage())
        .localizedMessage(ex.getLocalizedMessage())
        .build();
    log.error("Error occurs in internal REST communication", ex);
    return new ResponseEntity<>(error, ex.getHttpStatus());
  }

  @ExceptionHandler(SoapSystemException.class)
  public ResponseEntity<SystemErrorDto> handleSoapSystemException(SoapSystemException ex) {
    var error = SystemErrorDto.builder()
        .traceId(MDC.get(TRACE_ID_KEY))
        .code(ex.getCode())
        .message(ex.getMessage())
        .build();
    log.error("Trembita communication error", ex);
    return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
