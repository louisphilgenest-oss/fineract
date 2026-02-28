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
package org.apache.fineract.portfolio.floatingrates.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.apache.fineract.portfolio.floatingrates.exception.FloatingRateNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FloatingRateRepositoryWrapperTest {

    @Mock
    private FloatingRateRepository floatingRateRepository;

    @InjectMocks
    private FloatingRateRepositoryWrapper wrapper;

    @Test
    void testRetrieveBaseLendingRate() {
        FloatingRate expectedRate = new FloatingRate("BLR", true, true, null);
        when(floatingRateRepository.retrieveBaseLendingRate()).thenReturn(expectedRate);

        FloatingRate result = wrapper.retrieveBaseLendingRate();

        assertEquals(expectedRate, result);
        verify(floatingRateRepository).retrieveBaseLendingRate();
    }

    @Test
    void testFindOneWithNotFoundDetectionSuccess() {
        FloatingRate expectedRate = new FloatingRate("Test Rate", false, true, null);
        when(floatingRateRepository.findById(1L)).thenReturn(Optional.of(expectedRate));

        FloatingRate result = wrapper.findOneWithNotFoundDetection(1L);

        assertNotNull(result);
        assertEquals(expectedRate, result);
    }

    @Test
    void testFindOneWithNotFoundDetectionThrowsException() {
        when(floatingRateRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(FloatingRateNotFoundException.class, () -> wrapper.findOneWithNotFoundDetection(999L));
    }

    @Test
    void testSave() {
        FloatingRate rate = new FloatingRate("Test Rate", false, true, null);

        wrapper.save(rate);

        verify(floatingRateRepository).save(rate);
    }

    @Test
    void testSaveAndFlush() {
        FloatingRate rate = new FloatingRate("Test Rate", false, true, null);

        wrapper.saveAndFlush(rate);

        verify(floatingRateRepository).saveAndFlush(rate);
    }
}
