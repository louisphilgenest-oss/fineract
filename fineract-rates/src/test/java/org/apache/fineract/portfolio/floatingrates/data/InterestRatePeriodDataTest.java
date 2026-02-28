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
import org.junit.jupiter.api.Test;

class InterestRatePeriodDataTest {

    @Test
    void testConstructorAndGetters() {
        LocalDate fromDate = LocalDate.of(2025, 1, 1);
        BigDecimal interestRate = new BigDecimal("5.0");
        LocalDate blrFromDate = LocalDate.of(2024, 12, 1);
        BigDecimal blrInterestRate = new BigDecimal("3.0");

        InterestRatePeriodData data = new InterestRatePeriodData(fromDate, interestRate, true, blrFromDate, blrInterestRate);

        assertEquals(fromDate, data.getFromDate());
        assertEquals(interestRate, data.getInterestRate());
        assertTrue(data.isDifferentialToBLR());
        assertTrue(data.isIsDifferentialToBLR());
        assertEquals(blrFromDate, data.getBlrFromDate());
        assertEquals(blrInterestRate, data.getBlrInterestRate());
    }

    @Test
    void testConstructorWithNullBlrFields() {
        LocalDate fromDate = LocalDate.of(2025, 1, 1);
        BigDecimal interestRate = new BigDecimal("5.0");

        InterestRatePeriodData data = new InterestRatePeriodData(fromDate, interestRate, false, null, null);

        assertEquals(fromDate, data.getFromDate());
        assertEquals(interestRate, data.getInterestRate());
        assertNull(data.getBlrFromDate());
        assertNull(data.getBlrInterestRate());
    }

    @Test
    void testSetFromDate() {
        InterestRatePeriodData data = new InterestRatePeriodData(LocalDate.of(2025, 1, 1), BigDecimal.ONE, false, null, null);

        LocalDate newDate = LocalDate.of(2025, 6, 1);
        data.setFromDate(newDate);

        assertEquals(newDate, data.getFromDate());
    }

    @Test
    void testSetLoanDifferentialInterestRate() {
        InterestRatePeriodData data = new InterestRatePeriodData(LocalDate.of(2025, 1, 1), BigDecimal.ONE, false, null, null);

        assertNull(data.getLoanDifferentialInterestRate());

        BigDecimal diff = new BigDecimal("1.5");
        data.setLoanDifferentialInterestRate(diff);

        assertEquals(diff, data.getLoanDifferentialInterestRate());
    }

    @Test
    void testSetLoanProductDifferentialInterestRate() {
        InterestRatePeriodData data = new InterestRatePeriodData(LocalDate.of(2025, 1, 1), BigDecimal.ONE, false, null, null);

        assertNull(data.getLoanProductDifferentialInterestRate());

        BigDecimal diff = new BigDecimal("2.0");
        data.setLoanProductDifferentialInterestRate(diff);

        assertEquals(diff, data.getLoanProductDifferentialInterestRate());
    }

    @Test
    void testSetEffectiveInterestRate() {
        InterestRatePeriodData data = new InterestRatePeriodData(LocalDate.of(2025, 1, 1), BigDecimal.ONE, false, null, null);

        assertNull(data.getEffectiveInterestRate());

        BigDecimal effective = new BigDecimal("7.5");
        data.setEffectiveInterestRate(effective);

        assertEquals(effective, data.getEffectiveInterestRate());
    }
}
