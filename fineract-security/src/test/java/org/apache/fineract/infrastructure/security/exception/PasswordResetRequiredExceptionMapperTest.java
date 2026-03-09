/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.infrastructure.security.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.apache.fineract.infrastructure.core.serialization.ToApiJsonSerializer;
import org.apache.fineract.infrastructure.security.data.AuthenticatedUserData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PasswordResetRequiredExceptionMapperTest {

    @Mock
    private ToApiJsonSerializer<AuthenticatedUserData> apiJsonSerializer;

    @InjectMocks
    private PasswordResetRequiredExceptionMapper mapper;

    @Test
    void shouldReturnForbiddenResponse() {
        AuthenticatedUserData userData = new AuthenticatedUserData();
        userData.setShouldRenewPassword(true);
        PasswordResetRequiredException exception = new PasswordResetRequiredException(userData);
        when(apiJsonSerializer.serialize(any(AuthenticatedUserData.class))).thenReturn("{\"shouldRenewPassword\":true}");

        Response response = mapper.toResponse(exception);

        assertNotNull(response);
        assertEquals(Response.Status.FORBIDDEN.getStatusCode(), response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());
    }

    @Test
    void shouldSerializeAuthenticatedUserDataInResponseBody() {
        AuthenticatedUserData userData = new AuthenticatedUserData();
        userData.setUsername("testuser");
        userData.setShouldRenewPassword(true);
        PasswordResetRequiredException exception = new PasswordResetRequiredException(userData);
        String expectedJson = "{\"username\":\"testuser\",\"shouldRenewPassword\":true}";
        when(apiJsonSerializer.serialize(userData)).thenReturn(expectedJson);

        Response response = mapper.toResponse(exception);

        assertEquals(expectedJson, response.getEntity());
    }
}
