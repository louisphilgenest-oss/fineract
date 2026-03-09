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
package org.apache.fineract.infrastructure.security.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Base64;
import org.junit.jupiter.api.Test;

class EncryptionUtilTest {

    private static final String AES_CBC = "AES/CBC/PKCS5Padding";
    private static final String MASTER_PASSWORD = "fineract";

    @Test
    public void testEncryptDecryptWorks() {
        // given
        String data = "postgres";
        String encrypted = EncryptionUtil.encryptToBase64(AES_CBC, MASTER_PASSWORD, data);
        assertThat(encrypted).isNotEqualTo(data);
        // when
        String result = EncryptionUtil.decryptFromBase64(AES_CBC, MASTER_PASSWORD, encrypted);
        // then
        assertThat(result).isEqualTo(data);
    }

    @Test
    public void testDecryptWorks() {
        // given
        String data = "Oy/ah382msZT9ZAYBmQgsIXJYOdkB5MoF+3XMkQVkcZTduNSam3+0VpGYOGyXojs";
        // when
        String result = EncryptionUtil.decryptFromBase64(AES_CBC, MASTER_PASSWORD, data);
        // then
        assertThat(result).isEqualTo("postgres");
    }

    @Test
    public void testEncryptProducesValidBase64Output() {
        // given
        String data = "testData";
        // when
        String encrypted = EncryptionUtil.encryptToBase64(AES_CBC, MASTER_PASSWORD, data);
        // then
        byte[] decoded = Base64.getDecoder().decode(encrypted);
        // IV (16) + salt (16) + at least one cipher block (16)
        assertThat(decoded.length).isGreaterThanOrEqualTo(48);
    }

    @Test
    public void testEncryptProducesDifferentOutputsForSameInput() {
        // given
        String data = "sameInput";
        // when
        String encrypted1 = EncryptionUtil.encryptToBase64(AES_CBC, MASTER_PASSWORD, data);
        String encrypted2 = EncryptionUtil.encryptToBase64(AES_CBC, MASTER_PASSWORD, data);
        // then - random salt and IV should produce different ciphertexts
        assertThat(encrypted1).isNotEqualTo(encrypted2);
        // both should still decrypt to the same value
        assertThat(EncryptionUtil.decryptFromBase64(AES_CBC, MASTER_PASSWORD, encrypted1)).isEqualTo(data);
        assertThat(EncryptionUtil.decryptFromBase64(AES_CBC, MASTER_PASSWORD, encrypted2)).isEqualTo(data);
    }

    @Test
    public void testEncryptDecryptWithEmptyString() {
        // given
        String data = "";
        // when
        String encrypted = EncryptionUtil.encryptToBase64(AES_CBC, MASTER_PASSWORD, data);
        String result = EncryptionUtil.decryptFromBase64(AES_CBC, MASTER_PASSWORD, encrypted);
        // then
        assertThat(result).isEqualTo(data);
    }

    @Test
    public void testEncryptDecryptWithSpecialCharacters() {
        // given
        String data = "p@$$w0rd!#%^&*()_+-=[]{}|;':\",./<>?";
        // when
        String encrypted = EncryptionUtil.encryptToBase64(AES_CBC, MASTER_PASSWORD, data);
        String result = EncryptionUtil.decryptFromBase64(AES_CBC, MASTER_PASSWORD, encrypted);
        // then
        assertThat(result).isEqualTo(data);
    }

    @Test
    public void testEncryptDecryptWithUnicodeCharacters() {
        // given
        String data = "日本語テスト données résumé";
        // when
        String encrypted = EncryptionUtil.encryptToBase64(AES_CBC, MASTER_PASSWORD, data);
        String result = EncryptionUtil.decryptFromBase64(AES_CBC, MASTER_PASSWORD, encrypted);
        // then
        assertThat(result).isEqualTo(data);
    }

    @Test
    public void testEncryptDecryptWithLongData() {
        // given
        String data = "a]b[c".repeat(1000);
        // when
        String encrypted = EncryptionUtil.encryptToBase64(AES_CBC, MASTER_PASSWORD, data);
        String result = EncryptionUtil.decryptFromBase64(AES_CBC, MASTER_PASSWORD, encrypted);
        // then
        assertThat(result).isEqualTo(data);
    }

    @Test
    public void testDecryptWithWrongPasswordThrowsException() {
        // given
        String data = "sensitiveData";
        String encrypted = EncryptionUtil.encryptToBase64(AES_CBC, MASTER_PASSWORD, data);
        // when / then
        assertThatThrownBy(() -> EncryptionUtil.decryptFromBase64(AES_CBC, "wrongPassword", encrypted))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Unable to decrypt data");
    }

    @Test
    public void testEncryptWithInvalidCipherTypeThrowsException() {
        // when / then
        assertThatThrownBy(() -> EncryptionUtil.encryptToBase64("INVALID/CIPHER", MASTER_PASSWORD, "data"))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Unable to encrypt data");
    }

    @Test
    public void testDecryptWithInvalidCipherTypeThrowsException() {
        // given
        String encrypted = EncryptionUtil.encryptToBase64(AES_CBC, MASTER_PASSWORD, "data");
        // when / then
        assertThatThrownBy(() -> EncryptionUtil.decryptFromBase64("INVALID/CIPHER", MASTER_PASSWORD, encrypted))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Unable to decrypt data");
    }

    @Test
    public void testDecryptWithCorruptedDataThrowsException() {
        // given - valid base64 but not valid encrypted data (too short for IV + salt + cipher)
        String corruptedData = Base64.getEncoder().encodeToString(new byte[33]);
        // when / then
        assertThatThrownBy(() -> EncryptionUtil.decryptFromBase64(AES_CBC, MASTER_PASSWORD, corruptedData))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Unable to decrypt data");
    }

    @Test
    public void testEncryptDecryptWithDifferentPasswords() {
        // given
        String data = "sharedData";
        String password1 = "password1";
        String password2 = "password2";
        // when
        String encrypted1 = EncryptionUtil.encryptToBase64(AES_CBC, password1, data);
        String encrypted2 = EncryptionUtil.encryptToBase64(AES_CBC, password2, data);
        // then - each password decrypts its own ciphertext
        assertThat(EncryptionUtil.decryptFromBase64(AES_CBC, password1, encrypted1)).isEqualTo(data);
        assertThat(EncryptionUtil.decryptFromBase64(AES_CBC, password2, encrypted2)).isEqualTo(data);
        // but cross-decryption should fail
        assertThatThrownBy(() -> EncryptionUtil.decryptFromBase64(AES_CBC, password1, encrypted2))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
