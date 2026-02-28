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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.apache.fineract.portfolio.floatingrates.data.FloatingRateDTO;
import org.apache.fineract.portfolio.floatingrates.data.FloatingRatePeriodData;
import org.junit.jupiter.api.Test;

class FloatingRatePeriodTest {

    @Test
    void testDefaultConstructor() {
        FloatingRatePeriod period = new FloatingRatePeriod();
        assertNull(period.getFromDate());
        assertNull(period.getInterestRate());
        assertFalse(period.isDifferentialToBaseLendingRate());
        assertFalse(period.isActive());
    }

    @Test
    void testParameterizedConstructor() {
        LocalDate fromDate = LocalDate.of(2025, 1, 1);
        BigDecimal interestRate = new BigDecimal("5.5");

        FloatingRatePeriod period = new FloatingRatePeriod(fromDate, interestRate, true, true);

        assertEquals(fromDate, period.getFromDate());
        assertEquals(interestRate, period.getInterestRate());
        assertTrue(period.isDifferentialToBaseLendingRate());
        assertTrue(period.isActive());
    }

    @Test
    void testUpdateFloatingRate() {
        FloatingRatePeriod period = new FloatingRatePeriod();
        FloatingRate floatingRate = new FloatingRate();

        period.updateFloatingRate(floatingRate);

        assertEquals(floatingRate, period.getFloatingRate());
    }

    @Test
    void testSetActive() {
        FloatingRatePeriod period = new FloatingRatePeriod(LocalDate.now(), BigDecimal.TEN, false, true);
        assertTrue(period.isActive());

        period.setActive(false);
        assertFalse(period.isActive());
    }

    @Test
    void testFetchFromDate() {
        LocalDate fromDate = LocalDate.of(2025, 6, 15);
        FloatingRatePeriod period = new FloatingRatePeriod(fromDate, BigDecimal.ONE, false, true);

        assertEquals(fromDate, period.fetchFromDate());
    }

    @Test
    void testToDataWithoutDifferential() {
        LocalDate fromDate = LocalDate.of(2025, 1, 1);
        BigDecimal interestRate = new BigDecimal("5.0");
        FloatingRatePeriod period = new FloatingRatePeriod(fromDate, interestRate, false, true);

        FloatingRateDTO dto = mock(FloatingRateDTO.class);
        when(dto.getInterestRateDiff()).thenReturn(new BigDecimal("1.0"));

        FloatingRatePeriodData data = period.toData(dto);

        assertEquals(fromDate, data.getFromDate());
        assertEquals(new BigDecimal("6.0"), data.getInterestRate());
        assertFalse(data.getIsDifferentialToBaseLendingRate());
        assertTrue(data.getIsActive());
    }

    @Test
    void testToDataWithDifferentialToBaseLendingRate() {
        LocalDate fromDate = LocalDate.of(2025, 1, 1);
        BigDecimal interestRate = new BigDecimal("2.0");
        FloatingRatePeriod period = new FloatingRatePeriod(fromDate, interestRate, true, true);

        FloatingRateDTO dto = mock(FloatingRateDTO.class);
        when(dto.getInterestRateDiff()).thenReturn(new BigDecimal("1.0"));
        when(dto.fetchBaseRate(fromDate)).thenReturn(new BigDecimal("3.0"));

        FloatingRatePeriodData data = period.toData(dto);

        // interest = interestRate(2.0) + interestRateDiff(1.0) + baseRate(3.0) = 6.0
        assertEquals(new BigDecimal("6.0"), data.getInterestRate());
        assertTrue(data.getIsDifferentialToBaseLendingRate());
    }
}
