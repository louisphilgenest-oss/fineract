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
package org.apache.fineract.infrastructure.security.command;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.security.domain.TFAccessToken;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.infrastructure.security.service.TwoFactorService;
import org.apache.fineract.useradministration.domain.AppUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InvalidateTFAccessTokenCommandHandlerTest {

    @Mock
    private TwoFactorService twoFactorService;

    @Mock
    private PlatformSecurityContext securityContext;

    @Mock
    private FromJsonHelper fromJsonHelper;

    @Mock
    private JsonCommand command;

    @Mock
    private AppUser appUser;

    @Mock
    private TFAccessToken accessToken;

    @InjectMocks
    private InvalidateTFAccessTokenCommandHandler handler;

    @Test
    void shouldThrowInvalidJsonExceptionWhenJsonIsBlank() {
        when(command.json()).thenReturn("");

        assertThrows(InvalidJsonException.class, () -> handler.processCommand(command));
    }

    @Test
    void shouldThrowInvalidJsonExceptionWhenJsonIsNull() {
        when(command.json()).thenReturn(null);

        assertThrows(InvalidJsonException.class, () -> handler.processCommand(command));
    }
}
