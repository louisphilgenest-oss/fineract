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
package org.apache.fineract.infrastructure.security.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.apache.fineract.infrastructure.security.constants.TwoFactorConfigurationConstants;
import org.junit.jupiter.api.Test;

class TwoFactorConfigurationTest {

    @Test
    void shouldReturnIntegerForNumberParameter() {
        TwoFactorConfiguration config = new TwoFactorConfiguration();
        config.setName(TwoFactorConfigurationConstants.OTP_TOKEN_LENGTH);
        config.setValue("6");

        Object result = config.getObjectValue();

        assertInstanceOf(Integer.class, result);
        assertEquals(6, result);
    }

    @Test
    void shouldReturnBooleanForBooleanParameter() {
        TwoFactorConfiguration config = new TwoFactorConfiguration();
        config.setName(TwoFactorConfigurationConstants.ENABLE_EMAIL_DELIVERY);
        config.setValue("true");

        Object result = config.getObjectValue();

        assertInstanceOf(Boolean.class, result);
        assertEquals(true, result);
    }

    @Test
    void shouldReturnBooleanFalseForBooleanParameter() {
        TwoFactorConfiguration config = new TwoFactorConfiguration();
        config.setName(TwoFactorConfigurationConstants.ENABLE_SMS_DELIVERY);
        config.setValue("false");

        Object result = config.getObjectValue();

        assertInstanceOf(Boolean.class, result);
        assertEquals(false, result);
    }

    @Test
    void shouldReturnStringForStringParameter() {
        TwoFactorConfiguration config = new TwoFactorConfiguration();
        config.setName(TwoFactorConfigurationConstants.EMAIL_SUBJECT);
        config.setValue("OTP Code");

        Object result = config.getObjectValue();

        assertInstanceOf(String.class, result);
        assertEquals("OTP Code", result);
    }

    @Test
    void shouldReturnStringForUnknownParameterName() {
        TwoFactorConfiguration config = new TwoFactorConfiguration();
        config.setName("unknown-param");
        config.setValue("some-value");

        Object result = config.getObjectValue();

        assertEquals("some-value", result);
    }

    @Test
    void shouldSupportChainedSetters() {
        TwoFactorConfiguration config = new TwoFactorConfiguration();
        TwoFactorConfiguration result = config.setName("test").setValue("value");

        assertEquals(config, result);
        assertEquals("test", config.getName());
        assertEquals("value", config.getValue());
    }
}
