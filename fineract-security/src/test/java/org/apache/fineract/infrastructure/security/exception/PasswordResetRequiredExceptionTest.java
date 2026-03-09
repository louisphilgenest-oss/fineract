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

import org.apache.fineract.infrastructure.security.data.AuthenticatedUserData;
import org.junit.jupiter.api.Test;

class PasswordResetRequiredExceptionTest {

    @Test
    void shouldCarryAuthenticatedUserData() {
        AuthenticatedUserData userData = new AuthenticatedUserData();
        userData.setUsername("testuser");
        userData.setShouldRenewPassword(true);

        PasswordResetRequiredException exception = new PasswordResetRequiredException(userData);

        assertEquals(userData, exception.getAuthenticatedUserData());
        assertEquals("testuser", exception.getAuthenticatedUserData().getUsername());
        assertEquals(true, exception.getAuthenticatedUserData().isShouldRenewPassword());
    }

    @Test
    void shouldHaveCorrectMessage() {
        AuthenticatedUserData userData = new AuthenticatedUserData();

        PasswordResetRequiredException exception = new PasswordResetRequiredException(userData);

        assertEquals("Password reset required", exception.getMessage());
    }

    @Test
    void shouldBeAnAuthenticationException() {
        AuthenticatedUserData userData = new AuthenticatedUserData();

        PasswordResetRequiredException exception = new PasswordResetRequiredException(userData);

        assertNotNull(exception);
        // PasswordResetRequiredException extends AuthenticationException
        assertEquals(org.springframework.security.core.AuthenticationException.class, exception.getClass().getSuperclass());
    }
}
