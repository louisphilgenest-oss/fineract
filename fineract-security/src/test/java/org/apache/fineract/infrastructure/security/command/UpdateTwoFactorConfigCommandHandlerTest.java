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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.security.data.TwoFactorConfigurationValidator;
import org.apache.fineract.infrastructure.security.service.TwoFactorConfigurationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UpdateTwoFactorConfigCommandHandlerTest {

    @Mock
    private TwoFactorConfigurationService configurationService;

    @Mock
    private TwoFactorConfigurationValidator dataValidator;

    @Mock
    private JsonCommand command;

    @InjectMocks
    private UpdateTwoFactorConfigCommandHandler handler;

    @Test
    void shouldValidateAndUpdateConfiguration() {
        String json = "{\"otp-token-length\": 6}";
        when(command.json()).thenReturn(json);
        when(command.commandId()).thenReturn(1L);

        Map<String, Object> changes = new HashMap<>();
        changes.put("otp-token-length", 6);
        when(configurationService.update(command)).thenReturn(changes);

        CommandProcessingResult result = handler.processCommand(command);

        assertNotNull(result);
        verify(dataValidator).validateForUpdate(json);
        verify(configurationService).update(command);
    }
}
