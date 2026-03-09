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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Set;
import org.apache.fineract.infrastructure.core.service.database.DatabaseTypeResolver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SqlInjectionPreventerServiceImplTest {

    @Mock
    private DatabaseTypeResolver databaseTypeResolver;

    @InjectMocks
    private SqlInjectionPreventerServiceImpl service;

    // --- encodeSql tests ---

    @Test
    void encodeSqlShouldReturnNullStringForNullInput() {
        String result = service.encodeSql(null);
        assertEquals("NULL", result);
    }

    @Test
    void encodeSqlShouldEscapeSingleQuoteForMySQL() {
        when(databaseTypeResolver.isMySQL()).thenReturn(true);
        String result = service.encodeSql("test'value");
        assertEquals("test\\'value", result);
    }

    @Test
    void encodeSqlShouldEscapeDoubleQuoteForMySQL() {
        when(databaseTypeResolver.isMySQL()).thenReturn(true);
        String result = service.encodeSql("test\"value");
        assertEquals("test\\\"value", result);
    }

    @Test
    void encodeSqlShouldEscapeBackslashForMySQL() {
        when(databaseTypeResolver.isMySQL()).thenReturn(true);
        String result = service.encodeSql("test\\value");
        assertEquals("test\\\\value", result);
    }

    @Test
    void encodeSqlShouldEscapeNullByteForMySQL() {
        when(databaseTypeResolver.isMySQL()).thenReturn(true);
        String result = service.encodeSql("test\0value");
        assertEquals("test\\0value", result);
    }

    @Test
    void encodeSqlShouldEscapeNewlineForMySQL() {
        when(databaseTypeResolver.isMySQL()).thenReturn(true);
        String result = service.encodeSql("test\nvalue");
        assertEquals("test\\nvalue", result);
    }

    @Test
    void encodeSqlShouldEscapeCarriageReturnForMySQL() {
        when(databaseTypeResolver.isMySQL()).thenReturn(true);
        String result = service.encodeSql("test\rvalue");
        assertEquals("test\\rvalue", result);
    }

    @Test
    void encodeSqlShouldEscapeTabForMySQL() {
        when(databaseTypeResolver.isMySQL()).thenReturn(true);
        String result = service.encodeSql("test\tvalue");
        assertEquals("test\\tvalue", result);
    }

    @Test
    void encodeSqlShouldEscapeCtrlZForMySQL() {
        when(databaseTypeResolver.isMySQL()).thenReturn(true);
        String result = service.encodeSql("test\032value");
        assertEquals("test\\Zvalue", result);
    }

    @Test
    void encodeSqlShouldReturnSafeStringUnchangedForMySQL() {
        when(databaseTypeResolver.isMySQL()).thenReturn(true);
        String result = service.encodeSql("safestring123");
        assertEquals("safestring123", result);
    }

    @Test
    void encodeSqlShouldHandleEmptyStringForMySQL() {
        when(databaseTypeResolver.isMySQL()).thenReturn(true);
        String result = service.encodeSql("");
        assertEquals("", result);
    }

    @Test
    void encodeSqlShouldDelegateToPostgresUtilsForPostgreSQL() {
        when(databaseTypeResolver.isMySQL()).thenReturn(false);
        when(databaseTypeResolver.isPostgreSQL()).thenReturn(true);
        // PostgreSQL Utils.escapeLiteral escapes single quotes by doubling them
        String result = service.encodeSql("test'value");
        assertEquals("test''value", result);
    }

    @Test
    void encodeSqlShouldReturnInputUnchangedForUnknownDatabase() {
        when(databaseTypeResolver.isMySQL()).thenReturn(false);
        when(databaseTypeResolver.isPostgreSQL()).thenReturn(false);
        String result = service.encodeSql("test'value");
        assertEquals("test'value", result);
    }

    // --- quoteIdentifier tests ---

    @Test
    void quoteIdentifierShouldUseBackticksForMySQL() {
        when(databaseTypeResolver.isMySQL()).thenReturn(true);
        String result = service.quoteIdentifier("column_name");
        assertEquals("`column_name`", result);
    }

    @Test
    void quoteIdentifierShouldUseDoubleQuotesForPostgreSQL() {
        when(databaseTypeResolver.isMySQL()).thenReturn(false);
        when(databaseTypeResolver.isPostgreSQL()).thenReturn(true);
        String result = service.quoteIdentifier("column_name");
        assertEquals("\"column_name\"", result);
    }

    @Test
    void quoteIdentifierShouldReturnAsIsForUnknownDatabase() {
        when(databaseTypeResolver.isMySQL()).thenReturn(false);
        when(databaseTypeResolver.isPostgreSQL()).thenReturn(false);
        String result = service.quoteIdentifier("column_name");
        assertEquals("column_name", result);
    }

    @Test
    void quoteIdentifierShouldRejectBlankIdentifier() {
        assertThrows(IllegalArgumentException.class, () -> service.quoteIdentifier(""));
    }

    @Test
    void quoteIdentifierShouldRejectNullIdentifier() {
        assertThrows(IllegalArgumentException.class, () -> service.quoteIdentifier(null));
    }

    @Test
    void quoteIdentifierShouldRejectInvalidCharacters() {
        assertThrows(IllegalArgumentException.class, () -> service.quoteIdentifier("table; DROP TABLE"));
    }

    @Test
    void quoteIdentifierShouldRejectIdentifierStartingWithNumber() {
        assertThrows(IllegalArgumentException.class, () -> service.quoteIdentifier("1column"));
    }

    @Test
    void quoteIdentifierShouldAcceptIdentifierStartingWithUnderscore() {
        when(databaseTypeResolver.isMySQL()).thenReturn(true);
        String result = service.quoteIdentifier("_private_col");
        assertEquals("`_private_col`", result);
    }

    // --- quoteIdentifier with whitelist tests ---

    @Test
    void quoteIdentifierWithWhitelistShouldAcceptWhitelistedValue() {
        when(databaseTypeResolver.isMySQL()).thenReturn(true);
        Set<String> allowed = Set.of("col_a", "col_b");
        String result = service.quoteIdentifier("col_a", allowed);
        assertEquals("`col_a`", result);
    }

    @Test
    void quoteIdentifierWithWhitelistShouldRejectNonWhitelistedValue() {
        Set<String> allowed = Set.of("col_a", "col_b");
        assertThrows(IllegalArgumentException.class, () -> service.quoteIdentifier("col_c", allowed));
    }

    @Test
    void quoteIdentifierWithNullWhitelistShouldNotFilterByWhitelist() {
        when(databaseTypeResolver.isMySQL()).thenReturn(true);
        String result = service.quoteIdentifier("any_column", null);
        assertEquals("`any_column`", result);
    }
}
