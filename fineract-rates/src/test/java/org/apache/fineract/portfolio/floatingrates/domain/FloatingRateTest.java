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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.apache.fineract.portfolio.floatingrates.data.FloatingRateDTO;
import org.apache.fineract.portfolio.floatingrates.data.FloatingRatePeriodData;
import org.junit.jupiter.api.Test;

class FloatingRateTest {

    @Test
    void testDefaultConstructor() {
        FloatingRate rate = new FloatingRate();
        assertNull(rate.getName());
        assertFalse(rate.isBaseLendingRate());
        assertFalse(rate.isActive());
        assertNull(rate.getFloatingRatePeriods());
    }

    @Test
    void testParameterizedConstructor() {
        List<FloatingRatePeriod> periods = new ArrayList<>();
        FloatingRatePeriod period = new FloatingRatePeriod(LocalDate.of(2025, 1, 1), new BigDecimal("5.0"), false, true);
        periods.add(period);

        FloatingRate rate = new FloatingRate("Test Rate", true, true, periods);

        assertEquals("Test Rate", rate.getName());
        assertTrue(rate.isBaseLendingRate());
        assertTrue(rate.isActive());
        assertNotNull(rate.getFloatingRatePeriods());
        assertEquals(1, rate.getFloatingRatePeriods().size());
        // Verify the period has its floatingRate set
        assertEquals(rate, period.getFloatingRate());
    }

    @Test
    void testConstructorWithNullPeriods() {
        FloatingRate rate = new FloatingRate("No Periods", false, true, null);

        assertEquals("No Periods", rate.getName());
        assertNull(rate.getFloatingRatePeriods());
    }

    @Test
    void testFetchInterestRatesReturnsEmptyWhenNoActivePeriods() {
        FloatingRatePeriod inactivePeriod = new FloatingRatePeriod(LocalDate.of(2025, 1, 1), new BigDecimal("5.0"), false, false);
        List<FloatingRatePeriod> periods = new ArrayList<>();
        periods.add(inactivePeriod);

        FloatingRate rate = new FloatingRate("Rate", false, true, periods);

        FloatingRateDTO dto = new FloatingRateDTO(false, LocalDate.of(2025, 6, 1), BigDecimal.ZERO, Collections.emptyList());

        Collection<FloatingRatePeriodData> result = rate.fetchInterestRates(dto);

        assertTrue(result.isEmpty());
    }

    @Test
    void testFetchInterestRatesWithActivePeriodsBeforeStartDate() {
        LocalDate periodDate = LocalDate.of(2025, 1, 1);
        FloatingRatePeriod period = new FloatingRatePeriod(periodDate, new BigDecimal("5.0"), false, true);
        List<FloatingRatePeriod> periods = new ArrayList<>();
        periods.add(period);

        FloatingRate rate = new FloatingRate("Rate", false, true, periods);

        // Start date is after the period date
        FloatingRateDTO dto = new FloatingRateDTO(false, LocalDate.of(2025, 6, 1), BigDecimal.ZERO, Collections.emptyList());

        Collection<FloatingRatePeriodData> result = rate.fetchInterestRates(dto);

        assertEquals(1, result.size());
    }

    @Test
    void testFetchInterestRatesWithMultipleActivePeriods() {
        LocalDate date1 = LocalDate.of(2025, 1, 1);
        LocalDate date2 = LocalDate.of(2025, 6, 1);
        LocalDate date3 = LocalDate.of(2025, 12, 1);

        FloatingRatePeriod period1 = new FloatingRatePeriod(date1, new BigDecimal("3.0"), false, true);
        FloatingRatePeriod period2 = new FloatingRatePeriod(date2, new BigDecimal("4.0"), false, true);
        FloatingRatePeriod period3 = new FloatingRatePeriod(date3, new BigDecimal("5.0"), false, true);

        List<FloatingRatePeriod> periods = new ArrayList<>();
        periods.add(period1);
        periods.add(period2);
        periods.add(period3);

        FloatingRate rate = new FloatingRate("Rate", false, true, periods);

        // Start date is between period1 and period2 — with isFloatingInterestRate = true
        FloatingRateDTO dto = new FloatingRateDTO(true, LocalDate.of(2025, 3, 1), BigDecimal.ZERO, Collections.emptyList());

        Collection<FloatingRatePeriodData> result = rate.fetchInterestRates(dto);

        // Should include period2 and period3 (those after startDate), plus period1 via previousPeriod
        assertFalse(result.isEmpty());
    }

    @Test
    void testFetchInterestRatesLastPeriodBeforeStartDate() {
        LocalDate periodDate = LocalDate.of(2025, 1, 1);
        FloatingRatePeriod period = new FloatingRatePeriod(periodDate, new BigDecimal("5.0"), false, true);

        List<FloatingRatePeriod> periods = new ArrayList<>();
        periods.add(period);

        FloatingRate rate = new FloatingRate("Rate", false, true, periods);

        // Start date well after the only period — previousPeriod fallback
        FloatingRateDTO dto = new FloatingRateDTO(false, LocalDate.of(2026, 1, 1), BigDecimal.ZERO, Collections.emptyList());

        Collection<FloatingRatePeriodData> result = rate.fetchInterestRates(dto);

        // Should return the last active period as fallback
        assertEquals(1, result.size());
    }
}
