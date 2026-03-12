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
package org.apache.fineract.infrastructure.security.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;

@ExtendWith(MockitoExtension.class)
class FineractJwtAuthenticationTokenTest {

    @Mock
    private UserDetails userDetails;

    @Test
    void shouldReturnUserDetailsAsPrincipal() {
        Map<String, Object> headers = new HashMap<>();
        headers.put("alg", "RS256");
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "testuser");

        Jwt jwt = new Jwt("token-value", Instant.now(), Instant.now().plusSeconds(3600), headers, claims);
        when(userDetails.getUsername()).thenReturn("testuser");

        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));

        FineractJwtAuthenticationToken token = new FineractJwtAuthenticationToken(jwt, authorities, userDetails);

        assertEquals(userDetails, token.getPrincipal());
    }

    @Test
    void shouldSetAuthoritiesCorrectly() {
        Map<String, Object> headers = new HashMap<>();
        headers.put("alg", "RS256");
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "testuser");

        Jwt jwt = new Jwt("token-value", Instant.now(), Instant.now().plusSeconds(3600), headers, claims);
        when(userDetails.getUsername()).thenReturn("testuser");

        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"), new SimpleGrantedAuthority("ROLE_ADMIN"));

        FineractJwtAuthenticationToken token = new FineractJwtAuthenticationToken(jwt, authorities, userDetails);

        assertNotNull(token.getAuthorities());
        assertEquals(2, token.getAuthorities().size());
    }

    @Test
    void shouldThrowNullPointerExceptionWhenUserIsNull() {
        Map<String, Object> headers = new HashMap<>();
        headers.put("alg", "RS256");
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "testuser");

        Jwt jwt = new Jwt("token-value", Instant.now(), Instant.now().plusSeconds(3600), headers, claims);

        assertThrows(NullPointerException.class, () -> new FineractJwtAuthenticationToken(jwt, Collections.emptyList(), null));
    }
}
