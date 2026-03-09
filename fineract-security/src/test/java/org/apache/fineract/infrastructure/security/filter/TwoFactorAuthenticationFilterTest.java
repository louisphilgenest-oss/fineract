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
package org.apache.fineract.infrastructure.security.filter;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Collection;
import org.apache.fineract.infrastructure.security.domain.TFAccessToken;
import org.apache.fineract.infrastructure.security.service.TwoFactorService;
import org.apache.fineract.useradministration.domain.AppUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class TwoFactorAuthenticationFilterTest {

    @Mock
    private TwoFactorService twoFactorService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private AppUser appUser;

    private TwoFactorAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        filter = new TwoFactorAuthenticationFilter(twoFactorService);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldContinueFilterChainWhenNotAuthenticated() throws Exception {
        SecurityContextHolder.clearContext();

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldAddTwoFactorAuthorityWhenUserCanBypass() throws Exception {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(appUser, "password", authorities);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        when(appUser.hasSpecificPermissionTo("BYPASS_TWOFACTOR")).thenReturn(true);

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        // After filter, the authentication should have TWOFACTOR_AUTHENTICATED authority
        assertTrue(SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("TWOFACTOR_AUTHENTICATED")));
    }

    @Test
    void shouldReturnUnauthorizedWhenTokenIsInvalid() throws Exception {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(appUser, "password", authorities);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        when(appUser.hasSpecificPermissionTo("BYPASS_TWOFACTOR")).thenReturn(false);
        when(request.getHeader("Fineract-Platform-TFA-Token")).thenReturn("invalid-token");
        when(twoFactorService.fetchAccessTokenForUser(appUser, "invalid-token")).thenReturn(null);

        filter.doFilter(request, response, filterChain);

        verify(response).sendError(eq(HttpServletResponse.SC_UNAUTHORIZED), anyString());
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void shouldContinueWithoutTwoFactorWhenNoTokenProvided() throws Exception {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(appUser, "password", authorities);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        when(appUser.hasSpecificPermissionTo("BYPASS_TWOFACTOR")).thenReturn(false);
        when(request.getHeader("Fineract-Platform-TFA-Token")).thenReturn(null);

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldAddTwoFactorAuthorityWhenValidTokenProvided() throws Exception {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(appUser, "password", authorities);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        when(appUser.hasSpecificPermissionTo("BYPASS_TWOFACTOR")).thenReturn(false);
        when(request.getHeader("Fineract-Platform-TFA-Token")).thenReturn("valid-token");
        TFAccessToken accessToken = mock(TFAccessToken.class);
        when(accessToken.isValid()).thenReturn(true);
        when(twoFactorService.fetchAccessTokenForUser(appUser, "valid-token")).thenReturn(accessToken);

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertTrue(SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("TWOFACTOR_AUTHENTICATED")));
    }

    @Test
    void shouldReturnUnauthorizedWhenTokenExistsButIsNotValid() throws Exception {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(appUser, "password", authorities);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        when(appUser.hasSpecificPermissionTo("BYPASS_TWOFACTOR")).thenReturn(false);
        when(request.getHeader("Fineract-Platform-TFA-Token")).thenReturn("expired-token");
        TFAccessToken accessToken = mock(TFAccessToken.class);
        when(accessToken.isValid()).thenReturn(false);
        when(twoFactorService.fetchAccessTokenForUser(appUser, "expired-token")).thenReturn(accessToken);

        filter.doFilter(request, response, filterChain);

        verify(response).sendError(eq(HttpServletResponse.SC_UNAUTHORIZED), anyString());
    }
}
