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
package org.apache.fineract.infrastructure.security.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class UUIDAccessTokenGenerationServiceTest {

    private final UUIDAccessTokenGenerationService service = new UUIDAccessTokenGenerationService();

    @Test
    void shouldGenerateNonNullToken() {
        String token = service.generateRandomToken();
        assertNotNull(token);
    }

    @Test
    void shouldGenerateTokenWithoutHyphens() {
        String token = service.generateRandomToken();
        assertFalse(token.contains("-"), "Token should not contain hyphens");
    }

    @Test
    void shouldGenerateTokenOfExpectedLength() {
        // UUID without hyphens is 32 hex characters
        String token = service.generateRandomToken();
        assertEquals(32, token.length());
    }

    @Test
    void shouldGenerateUniqueTokensOnEachCall() {
        String token1 = service.generateRandomToken();
        String token2 = service.generateRandomToken();
        assertNotEquals(token1, token2);
    }

    @Test
    void shouldGenerateHexadecimalToken() {
        String token = service.generateRandomToken();
        // UUID hex chars are [0-9a-f]
        assertTrue(token.matches("[0-9a-f]+"), "Token should be hexadecimal");
    }

    private static void assertTrue(boolean condition, String message) {
        org.junit.jupiter.api.Assertions.assertTrue(condition, message);
    }
}
