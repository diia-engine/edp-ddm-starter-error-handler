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

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

import com.epam.digital.data.platform.starter.errorhandling.dto.ErrorDetailDto;
import com.epam.digital.data.platform.starter.errorhandling.dto.ErrorsListDto;
import com.epam.digital.data.platform.starter.errorhandling.dto.SystemErrorDto;
import com.epam.digital.data.platform.starter.errorhandling.dto.ValidationErrorDto;
import com.epam.digital.data.platform.starter.errorhandling.exception.RestSystemException;
import com.epam.digital.data.platform.starter.errorhandling.exception.SoapSystemException;
import com.epam.digital.data.platform.starter.errorhandling.exception.ValidationException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;

@ExtendWith(MockitoExtension.class)
class BaseRestExceptionHandlerTest {

  @InjectMocks
  private BaseRestExceptionHandler exceptionHandler;

  @BeforeEach
  public void setUp() {
    MDC.put(BaseRestExceptionHandler.TRACE_ID_KEY, "traceId");
  }

  @Test
  void handleAccessDeniedException() {
    var response = exceptionHandler
        .handleAccessDeniedException(new AccessDeniedException("Denied"));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    Assertions.assertThat(response.getBody())
        .isEqualTo(SystemErrorDto.builder().traceId("traceId").code("403")
            .message(BaseRestExceptionHandler.ACCESS_IS_DENIED).build());
  }

  @Test
  void handleRuntimeException() {
    var response = exceptionHandler.handleRuntimeException(new RuntimeException());

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    Assertions.assertThat(response.getBody())
        .isEqualTo(SystemErrorDto.builder().traceId("traceId").code("500")
            .message(BaseRestExceptionHandler.INTERNAL_SERVER_ERROR).build());
  }

  @Test
  void handleValidationException() {
    var validationException = new ValidationException(
        new ValidationErrorDto("traceId", "422", "not valid",
            new ErrorsListDto(singletonList(new ErrorDetailDto("message", "field", "value")))));

    var response = exceptionHandler.handleValidationException(validationException);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    assertThat(response.getBody()).isEqualTo(ValidationErrorDto.builder().traceId("traceId")
        .code("422").message("not valid").details(
            new ErrorsListDto(singletonList(new ErrorDetailDto("message", "field", "value"))))
        .build());
  }

  @Test
  void handleRestSystemException() {
    var errorDto = SystemErrorDto.builder().traceId("traceId")
        .message("Not Found").code("404").localizedMessage("Не знайдено").build();
    var restSystemException = new RestSystemException(errorDto, HttpStatus.NOT_FOUND);

    var response = exceptionHandler.handleRestSystemException(restSystemException);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    Assertions.assertThat(response.getBody()).isEqualTo(errorDto);
  }

  @Test
  void handleSoapSystemException() {
    var errorDto = SystemErrorDto.builder().traceId("traceId")
        .message("No valid authentication certificate").code("500").build();
    var soapSystemException = new SoapSystemException(errorDto);

    var response = exceptionHandler.handleSoapSystemException(soapSystemException);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    Assertions.assertThat(response.getBody()).isEqualTo(errorDto);
  }
}

