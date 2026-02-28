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
package org.apache.fineract.portfolio.floatingrates.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.portfolio.floatingrates.domain.FloatingRate;
import org.apache.fineract.portfolio.floatingrates.domain.FloatingRateRepositoryWrapper;
import org.apache.fineract.portfolio.floatingrates.serialization.FloatingRateDataValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
class FloatingRateWritePlatformServiceImplTest {

    @Mock
    private FloatingRateDataValidator fromApiJsonDeserializer;

    @Mock
    private FloatingRateRepositoryWrapper floatingRateRepository;

    @Mock
    private JsonCommand command;

    @InjectMocks
    private FloatingRateWritePlatformServiceImpl service;

    @Test
    void testCreateFloatingRateSuccess() {
        String json = "{\"name\":\"Test Rate\",\"isBaseLendingRate\":false,\"isActive\":true}";
        when(command.json()).thenReturn(json);
        // FloatingRate.createNew() calls these on the command
        lenient().when(command.stringValueOfParameterNamed("name")).thenReturn("Test Rate");
        lenient().when(command.parameterExists("isBaseLendingRate")).thenReturn(false);
        lenient().when(command.parameterExists("isActive")).thenReturn(false);
        lenient().when(command.parameterExists("ratePeriods")).thenReturn(false);
        lenient().when(command.commandId()).thenReturn(1L);
        doNothing().when(fromApiJsonDeserializer).validateForCreate(json);
        doNothing().when(floatingRateRepository).saveAndFlush(any(FloatingRate.class));

        CommandProcessingResult result = service.createFloatingRate(command);

        assertNotNull(result);
        verify(fromApiJsonDeserializer).validateForCreate(json);
        verify(floatingRateRepository).saveAndFlush(any(FloatingRate.class));
    }

    @Test
    void testCreateFloatingRateDataIntegrityViolationDuplicateName() {
        String json = "{\"name\":\"Duplicate Rate\"}";
        when(command.json()).thenReturn(json);
        doNothing().when(fromApiJsonDeserializer).validateForCreate(json);
        // FloatingRate.createNew() will call these
        lenient().when(command.stringValueOfParameterNamed("name")).thenReturn("Duplicate Rate");
        lenient().when(command.parameterExists("isBaseLendingRate")).thenReturn(false);
        lenient().when(command.parameterExists("isActive")).thenReturn(false);
        lenient().when(command.parameterExists("ratePeriods")).thenReturn(false);

        DataIntegrityViolationException dve = new DataIntegrityViolationException("unq_name",
                new RuntimeException("unq_name constraint violation"));
        doThrow(dve).when(floatingRateRepository).saveAndFlush(any(FloatingRate.class));

        assertThrows(PlatformDataIntegrityException.class, () -> service.createFloatingRate(command));
    }

    @Test
    void testUpdateFloatingRateSuccessNoChanges() {
        String json = "{\"name\":\"Existing Rate\"}";
        when(command.json()).thenReturn(json);
        when(command.entityId()).thenReturn(1L);

        FloatingRate existingRate = new FloatingRate("Existing Rate", false, true, null);
        when(floatingRateRepository.findOneWithNotFoundDetection(1L)).thenReturn(existingRate);
        doNothing().when(fromApiJsonDeserializer).validateForUpdate(eq(json), eq(existingRate));

        when(command.isChangeInStringParameterNamed("name", "Existing Rate")).thenReturn(false);
        when(command.isChangeInBooleanParameterNamed("isBaseLendingRate", false)).thenReturn(false);
        when(command.isChangeInBooleanParameterNamed("isActive", true)).thenReturn(false);
        when(command.parameterExists("ratePeriods")).thenReturn(false);
        when(command.commandId()).thenReturn(1L);

        CommandProcessingResult result = service.updateFloatingRate(command);

        assertNotNull(result);
        verify(fromApiJsonDeserializer).validateForUpdate(json, existingRate);
    }
}
