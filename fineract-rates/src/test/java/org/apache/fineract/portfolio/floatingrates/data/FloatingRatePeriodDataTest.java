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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;

class FloatingRatePeriodDataTest {

    @Test
    void testFullConstructor() {
        LocalDate fromDate = LocalDate.of(2025, 1, 1);
        OffsetDateTime createdOn = OffsetDateTime.now();
        OffsetDateTime modifiedOn = OffsetDateTime.now();

        FloatingRatePeriodData data = new FloatingRatePeriodData(1L, fromDate, new BigDecimal("5.0"), true, true, "admin", createdOn,
                "admin", modifiedOn);

        assertEquals(1L, data.getId());
        assertEquals(fromDate, data.getFromDate());
        assertEquals(new BigDecimal("5.0"), data.getInterestRate());
        assertTrue(data.getIsDifferentialToBaseLendingRate());
        assertTrue(data.getIsActive());
        assertEquals("admin", data.getCreatedBy());
        assertEquals(createdOn, data.getCreatedOn());
        assertEquals("admin", data.getModifiedBy());
        assertEquals(modifiedOn, data.getModifiedOn());
    }

    @Test
    void testShortConstructor() {
        LocalDate fromDate = LocalDate.of(2025, 1, 1);

        FloatingRatePeriodData data = new FloatingRatePeriodData(1L, fromDate, new BigDecimal("5.0"), false, true);

        assertEquals(1L, data.getId());
        assertEquals(fromDate, data.getFromDate());
        assertEquals(new BigDecimal("5.0"), data.getInterestRate());
        assertFalse(data.getIsDifferentialToBaseLendingRate());
        assertTrue(data.getIsActive());
    }

    @Test
    void testGetFromDateAsLocalDate() {
        LocalDate fromDate = LocalDate.of(2025, 6, 15);
        FloatingRatePeriodData data = new FloatingRatePeriodData(1L, fromDate, BigDecimal.ONE, false, true);

        assertEquals(fromDate, data.getFromDateAsLocalDate());
    }

    @Test
    void testCompareToWithNull() {
        FloatingRatePeriodData data = new FloatingRatePeriodData(1L, LocalDate.of(2025, 1, 1), BigDecimal.ONE, false, true);

        assertEquals(-1, data.compareTo(null));
    }

    @Test
    void testCompareToEqual() {
        LocalDate date = LocalDate.of(2025, 1, 1);
        FloatingRatePeriodData data1 = new FloatingRatePeriodData(1L, date, BigDecimal.ONE, false, true);
        FloatingRatePeriodData data2 = new FloatingRatePeriodData(1L, date, BigDecimal.TEN, false, true);

        assertEquals(0, data1.compareTo(data2));
    }

    @Test
    void testCompareToDifferentIds() {
        LocalDate date = LocalDate.of(2025, 1, 1);
        FloatingRatePeriodData data1 = new FloatingRatePeriodData(1L, date, BigDecimal.ONE, false, true);
        FloatingRatePeriodData data2 = new FloatingRatePeriodData(2L, date, BigDecimal.ONE, false, true);

        assertTrue(data1.compareTo(data2) < 0);
    }

    @Test
    void testEqualsWithSameObject() {
        FloatingRatePeriodData data = new FloatingRatePeriodData(1L, LocalDate.of(2025, 1, 1), BigDecimal.ONE, false, true);

        assertEquals(data, data);
    }

    @Test
    void testEqualsWithEqualObjects() {
        LocalDate date = LocalDate.of(2025, 1, 1);
        FloatingRatePeriodData data1 = new FloatingRatePeriodData(1L, date, BigDecimal.ONE, false, true);
        FloatingRatePeriodData data2 = new FloatingRatePeriodData(1L, date, BigDecimal.TEN, false, true);

        assertEquals(data1, data2);
    }

    @Test
    void testEqualsWithNull() {
        FloatingRatePeriodData data = new FloatingRatePeriodData(1L, LocalDate.of(2025, 1, 1), BigDecimal.ONE, false, true);

        assertNotEquals(null, data);
    }

    @Test
    void testEqualsWithDifferentType() {
        FloatingRatePeriodData data = new FloatingRatePeriodData(1L, LocalDate.of(2025, 1, 1), BigDecimal.ONE, false, true);

        assertNotEquals("string", data);
    }

    @Test
    void testEqualsWithDifferentIds() {
        LocalDate date = LocalDate.of(2025, 1, 1);
        FloatingRatePeriodData data1 = new FloatingRatePeriodData(1L, date, BigDecimal.ONE, false, true);
        FloatingRatePeriodData data2 = new FloatingRatePeriodData(2L, date, BigDecimal.ONE, false, true);

        assertNotEquals(data1, data2);
    }

    @Test
    void testHashCodeConsistency() {
        LocalDate date = LocalDate.of(2025, 1, 1);
        FloatingRatePeriodData data1 = new FloatingRatePeriodData(1L, date, BigDecimal.ONE, false, true);
        FloatingRatePeriodData data2 = new FloatingRatePeriodData(1L, date, BigDecimal.TEN, false, true);

        assertEquals(data1.hashCode(), data2.hashCode());
    }

    @Test
    void testHashCodeDifference() {
        FloatingRatePeriodData data1 = new FloatingRatePeriodData(1L, LocalDate.of(2025, 1, 1), BigDecimal.ONE, false, true);
        FloatingRatePeriodData data2 = new FloatingRatePeriodData(2L, LocalDate.of(2025, 6, 1), BigDecimal.ONE, false, false);

        assertNotEquals(data1.hashCode(), data2.hashCode());
    }
}
