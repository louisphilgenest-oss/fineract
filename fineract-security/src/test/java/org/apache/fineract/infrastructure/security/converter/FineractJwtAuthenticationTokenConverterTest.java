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
package org.apache.fineract.infrastructure.security.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.apache.fineract.infrastructure.security.data.FineractJwtAuthenticationToken;
import org.apache.fineract.infrastructure.security.service.TenantAwareJpaPlatformUserDetailsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.jwt.Jwt;

@ExtendWith(MockitoExtension.class)
class FineractJwtAuthenticationTokenConverterTest {

    @Mock
    private TenantAwareJpaPlatformUserDetailsService userDetailsService;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private FineractJwtAuthenticationTokenConverter converter;

    @Test
    void shouldConvertJwtToFineractAuthenticationToken() {
        Map<String, Object> headers = new HashMap<>();
        headers.put("alg", "RS256");
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "testuser");

        Jwt jwt = new Jwt("token-value", Instant.now(), Instant.now().plusSeconds(3600), headers, claims);

        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("testuser");

        FineractJwtAuthenticationToken result = converter.convert(jwt);

        assertNotNull(result);
        assertEquals(userDetails, result.getPrincipal());
    }

    @Test
    void shouldThrowOAuth2AuthenticationExceptionWhenUserNotFound() {
        Map<String, Object> headers = new HashMap<>();
        headers.put("alg", "RS256");
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "unknownuser");

        Jwt jwt = new Jwt("token-value", Instant.now(), Instant.now().plusSeconds(3600), headers, claims);

        when(userDetailsService.loadUserByUsername("unknownuser")).thenThrow(new UsernameNotFoundException("unknownuser: not found"));

        assertThrows(OAuth2AuthenticationException.class, () -> converter.convert(jwt));
    }
}
