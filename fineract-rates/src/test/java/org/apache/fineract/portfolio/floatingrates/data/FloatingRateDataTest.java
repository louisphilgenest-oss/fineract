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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.junit.jupiter.api.Test;

class FloatingRateDataTest {

    @Test
    void testConstructorAndGetters() {
        OffsetDateTime now = OffsetDateTime.now();
        List<FloatingRatePeriodData> ratePeriods = Collections.emptyList();

        FloatingRateData data = new FloatingRateData(1L, "Test Rate", true, true, "admin", now, "admin", now, ratePeriods, null);

        assertEquals(1L, data.getId());
        assertEquals("Test Rate", data.getName());
        assertTrue(data.getIsBaseLendingRate());
        assertTrue(data.getIsActive());
        assertEquals("admin", data.getCreatedBy());
        assertEquals(now, data.getCreatedOn());
        assertEquals("admin", data.getModifiedBy());
        assertEquals(now, data.getModifiedOn());
        assertEquals(ratePeriods, data.getRatePeriods());
        assertNull(data.getInterestRateFrequencyTypeOptions());
    }

    @Test
    void testCompareToWithNull() {
        FloatingRateData data = new FloatingRateData(1L, "Rate", true, true, null, null, null, null, null, null);

        assertEquals(-1, data.compareTo(null));
    }

    @Test
    void testCompareToEqual() {
        FloatingRateData data1 = new FloatingRateData(1L, "Rate", true, true, null, null, null, null, null, null);
        FloatingRateData data2 = new FloatingRateData(1L, "Rate", true, true, null, null, null, null, null, null);

        assertEquals(0, data1.compareTo(data2));
    }

    @Test
    void testCompareToDifferent() {
        FloatingRateData data1 = new FloatingRateData(1L, "Rate A", true, true, null, null, null, null, null, null);
        FloatingRateData data2 = new FloatingRateData(2L, "Rate B", false, true, null, null, null, null, null, null);

        assertTrue(data1.compareTo(data2) < 0);
    }

    @Test
    void testEqualsWithSameObject() {
        FloatingRateData data = new FloatingRateData(1L, "Rate", true, true, null, null, null, null, null, null);

        assertEquals(data, data);
    }

    @Test
    void testEqualsWithEqualObjects() {
        FloatingRateData data1 = new FloatingRateData(1L, "Rate", true, true, null, null, null, null, null, null);
        FloatingRateData data2 = new FloatingRateData(1L, "Rate", true, true, "admin", null, null, null, null, null);

        assertEquals(data1, data2);
    }

    @Test
    void testEqualsWithNull() {
        FloatingRateData data = new FloatingRateData(1L, "Rate", true, true, null, null, null, null, null, null);

        assertNotEquals(null, data);
    }

    @Test
    void testEqualsWithDifferentType() {
        FloatingRateData data = new FloatingRateData(1L, "Rate", true, true, null, null, null, null, null, null);

        assertNotEquals("string", data);
    }

    @Test
    void testEqualsWithDifferentIds() {
        FloatingRateData data1 = new FloatingRateData(1L, "Rate", true, true, null, null, null, null, null, null);
        FloatingRateData data2 = new FloatingRateData(2L, "Rate", true, true, null, null, null, null, null, null);

        assertNotEquals(data1, data2);
    }

    @Test
    void testHashCodeConsistency() {
        FloatingRateData data1 = new FloatingRateData(1L, "Rate", true, true, null, null, null, null, null, null);
        FloatingRateData data2 = new FloatingRateData(1L, "Rate", true, true, "admin", null, null, null, null, null);

        assertEquals(data1.hashCode(), data2.hashCode());
    }

    @Test
    void testHashCodeDifference() {
        FloatingRateData data1 = new FloatingRateData(1L, "Rate A", true, true, null, null, null, null, null, null);
        FloatingRateData data2 = new FloatingRateData(2L, "Rate B", false, false, null, null, null, null, null, null);

        assertNotEquals(data1.hashCode(), data2.hashCode());
    }

    @Test
    void testToTemplate() {
        List<EnumOptionData> options = Collections.emptyList();

        FloatingRateData template = FloatingRateData.toTemplate(options);

        assertNull(template.getId());
        assertNull(template.getName());
        assertFalse(template.getIsBaseLendingRate());
        assertTrue(template.getIsActive());
        assertEquals(options, template.getInterestRateFrequencyTypeOptions());
    }
}
