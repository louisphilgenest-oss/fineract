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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class RandomOTPGeneratorTest {

    @Test
    void shouldGenerateTokenOfSpecifiedLength() {
        RandomOTPGenerator generator = new RandomOTPGenerator(6);
        String token = generator.generate();
        assertEquals(6, token.length());
    }

    @Test
    void shouldGenerateTokenOfLengthOne() {
        RandomOTPGenerator generator = new RandomOTPGenerator(1);
        String token = generator.generate();
        assertEquals(1, token.length());
    }

    @Test
    void shouldGenerateTokenOfLargeLength() {
        RandomOTPGenerator generator = new RandomOTPGenerator(100);
        String token = generator.generate();
        assertEquals(100, token.length());
    }

    @Test
    void shouldOnlyContainAllowedCharacters() {
        String allowedCharacters = "0123456789ABCDEFGHIJKLMNOPQRSTUVQXYZ";
        RandomOTPGenerator generator = new RandomOTPGenerator(1000);
        String token = generator.generate();
        for (char c : token.toCharArray()) {
            assertTrue(allowedCharacters.indexOf(c) >= 0, "Character '" + c + "' not in allowed set");
        }
    }

    @Test
    void shouldGenerateDifferentTokensOnSubsequentCalls() {
        RandomOTPGenerator generator = new RandomOTPGenerator(32);
        String token1 = generator.generate();
        String token2 = generator.generate();
        // With 32 characters from a 35-char alphabet, collision is astronomically unlikely
        assertNotEquals(token1, token2);
    }

    @Test
    void shouldGenerateEmptyTokenForZeroLength() {
        RandomOTPGenerator generator = new RandomOTPGenerator(0);
        String token = generator.generate();
        assertEquals(0, token.length());
    }
}
