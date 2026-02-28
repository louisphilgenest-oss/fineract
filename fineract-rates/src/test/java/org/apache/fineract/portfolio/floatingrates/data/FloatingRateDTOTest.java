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
package org.apache.fineract.portfolio.floatingrates.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

class FloatingRateDTOTest {

    @Test
    void testConstructorAndGetters() {
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        BigDecimal interestRateDiff = new BigDecimal("1.5");
        List<FloatingRatePeriodData> basePeriods = Collections.emptyList();

        FloatingRateDTO dto = new FloatingRateDTO(true, startDate, interestRateDiff, basePeriods);

        assertTrue(dto.isFloatingInterestRate());
        assertEquals(startDate, dto.getStartDate());
        assertEquals(interestRateDiff, dto.getInterestRateDiff());
        assertEquals(basePeriods, dto.getBaseLendingRatePeriods());
    }

    @Test
    void testFetchBaseRateReturnsMatchingRate() {
        LocalDate periodDate1 = LocalDate.of(2025, 6, 1);
        LocalDate periodDate2 = LocalDate.of(2025, 1, 1);

        // Periods should be sorted descending by date for fetchBaseRate to work correctly
        FloatingRatePeriodData period1 = new FloatingRatePeriodData(1L, periodDate1, new BigDecimal("5.0"), false, true);
        FloatingRatePeriodData period2 = new FloatingRatePeriodData(2L, periodDate2, new BigDecimal("4.0"), false, true);

        List<FloatingRatePeriodData> basePeriods = new ArrayList<>();
        basePeriods.add(period1);
        basePeriods.add(period2);

        FloatingRateDTO dto = new FloatingRateDTO(true, LocalDate.of(2025, 1, 1), BigDecimal.ZERO, basePeriods);

        // Fetch rate for a date that is >= period1's date
        BigDecimal rate = dto.fetchBaseRate(LocalDate.of(2025, 7, 1));
        assertEquals(new BigDecimal("5.0"), rate);
    }

    @Test
    void testFetchBaseRateReturnsFallbackRate() {
        LocalDate periodDate1 = LocalDate.of(2025, 6, 1);
        LocalDate periodDate2 = LocalDate.of(2025, 1, 1);

        FloatingRatePeriodData period1 = new FloatingRatePeriodData(1L, periodDate1, new BigDecimal("5.0"), false, true);
        FloatingRatePeriodData period2 = new FloatingRatePeriodData(2L, periodDate2, new BigDecimal("4.0"), false, true);

        List<FloatingRatePeriodData> basePeriods = new ArrayList<>();
        basePeriods.add(period1);
        basePeriods.add(period2);

        FloatingRateDTO dto = new FloatingRateDTO(true, LocalDate.of(2025, 1, 1), BigDecimal.ZERO, basePeriods);

        // Fetch rate for a date between period2 and period1
        BigDecimal rate = dto.fetchBaseRate(LocalDate.of(2025, 3, 1));
        assertEquals(new BigDecimal("4.0"), rate);
    }

    @Test
    void testFetchBaseRateReturnsNullWhenNoMatch() {
        LocalDate periodDate = LocalDate.of(2025, 6, 1);
        FloatingRatePeriodData period = new FloatingRatePeriodData(1L, periodDate, new BigDecimal("5.0"), false, true);

        List<FloatingRatePeriodData> basePeriods = new ArrayList<>();
        basePeriods.add(period);

        FloatingRateDTO dto = new FloatingRateDTO(true, LocalDate.of(2025, 1, 1), BigDecimal.ZERO, basePeriods);

        // Fetch rate for a date before any period
        BigDecimal rate = dto.fetchBaseRate(LocalDate.of(2024, 1, 1));
        assertNull(rate);
    }

    @Test
    void testAddInterestRateDiff() {
        FloatingRateDTO dto = new FloatingRateDTO(true, LocalDate.of(2025, 1, 1), new BigDecimal("1.0"), Collections.emptyList());

        dto.addInterestRateDiff(new BigDecimal("0.5"));

        assertEquals(new BigDecimal("1.5"), dto.getInterestRateDiff());
    }

    @Test
    void testResetInterestRateDiff() {
        FloatingRateDTO dto = new FloatingRateDTO(true, LocalDate.of(2025, 1, 1), new BigDecimal("1.0"), Collections.emptyList());

        dto.addInterestRateDiff(new BigDecimal("0.5"));
        assertEquals(new BigDecimal("1.5"), dto.getInterestRateDiff());

        dto.resetInterestRateDiff();
        assertEquals(new BigDecimal("1.0"), dto.getInterestRateDiff());
    }
}
