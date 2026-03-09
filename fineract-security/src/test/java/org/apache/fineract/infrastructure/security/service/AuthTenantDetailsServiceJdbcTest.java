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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import javax.sql.DataSource;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.security.exception.InvalidTenantIdentifierException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AuthTenantDetailsServiceJdbcTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private DataSource dataSource;

    private AuthTenantDetailsServiceJdbc service;

    @BeforeEach
    void setUp() {
        service = new AuthTenantDetailsServiceJdbc(dataSource);
        // Replace the internally created JdbcTemplate with our mock
        ReflectionTestUtils.setField(service, "jdbcTemplate", jdbcTemplate);
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldThrowInvalidTenantIdentifierExceptionWhenTenantNotFound() {
        when(jdbcTemplate.queryForObject(anyString(), any(RowMapper.class), any(Object[].class)))
                .thenThrow(new EmptyResultDataAccessException(1));

        assertThrows(InvalidTenantIdentifierException.class, () -> service.loadTenantById("nonexistent", false));
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldReturnTenantWhenFound() {
        FineractPlatformTenant expectedTenant = new FineractPlatformTenant(1L, "default", "Default Tenant", "UTC", null);

        when(jdbcTemplate.queryForObject(anyString(), any(RowMapper.class), any(Object[].class))).thenReturn(expectedTenant);

        FineractPlatformTenant result = service.loadTenantById("default", false);

        assertEquals(expectedTenant, result);
    }
}
