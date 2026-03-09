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
package org.apache.fineract.infrastructure.security.vote;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import java.util.function.Supplier;
import org.apache.fineract.useradministration.domain.AppUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

@ExtendWith(MockitoExtension.class)
class SelfServiceUserAuthorizationManagerTest {

    @Mock
    private Authentication authentication;

    @Mock
    private AppUser appUser;

    @Mock
    private HttpServletRequest request;

    private SelfServiceUserAuthorizationManager manager;

    @BeforeEach
    void setUp() {
        manager = new SelfServiceUserAuthorizationManager();
    }

    @Test
    void shouldGrantAccessForOptionsRequest() {
        when(request.getMethod()).thenReturn("OPTIONS");
        RequestAuthorizationContext context = new RequestAuthorizationContext(request);
        Supplier<Authentication> authSupplier = () -> authentication;

        AuthorizationDecision decision = manager.check(authSupplier, context);

        assertTrue(decision.isGranted());
    }

    @Test
    void shouldGrantAccessForRegularUserOnNonSelfServiceUrl() {
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURL()).thenReturn(new StringBuffer("https://example.com/api/v1/loans"));
        when(authentication.getPrincipal()).thenReturn(appUser);
        when(appUser.isSelfServiceUser()).thenReturn(false);

        RequestAuthorizationContext context = new RequestAuthorizationContext(request);
        Supplier<Authentication> authSupplier = () -> authentication;

        AuthorizationDecision decision = manager.check(authSupplier, context);

        assertTrue(decision.isGranted());
    }

    @Test
    void shouldGrantAccessForSelfServiceUserOnSelfServiceUrl() {
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURL()).thenReturn(new StringBuffer("https://example.com/api/v1/self/loans"));
        when(authentication.getPrincipal()).thenReturn(appUser);
        when(appUser.isSelfServiceUser()).thenReturn(true);

        RequestAuthorizationContext context = new RequestAuthorizationContext(request);
        Supplier<Authentication> authSupplier = () -> authentication;

        AuthorizationDecision decision = manager.check(authSupplier, context);

        assertTrue(decision.isGranted());
    }

    @Test
    void shouldDenyAccessForSelfServiceUserOnNonSelfServiceUrl() {
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURL()).thenReturn(new StringBuffer("https://example.com/api/v1/loans"));
        when(authentication.getPrincipal()).thenReturn(appUser);
        when(appUser.isSelfServiceUser()).thenReturn(true);

        RequestAuthorizationContext context = new RequestAuthorizationContext(request);
        Supplier<Authentication> authSupplier = () -> authentication;

        AuthorizationDecision decision = manager.check(authSupplier, context);

        assertFalse(decision.isGranted());
    }

    @Test
    void shouldDenyAccessForRegularUserOnSelfServiceUrl() {
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURL()).thenReturn(new StringBuffer("https://example.com/api/v1/self/loans"));
        when(authentication.getPrincipal()).thenReturn(appUser);
        when(appUser.isSelfServiceUser()).thenReturn(false);

        RequestAuthorizationContext context = new RequestAuthorizationContext(request);
        Supplier<Authentication> authSupplier = () -> authentication;

        AuthorizationDecision decision = manager.check(authSupplier, context);

        assertFalse(decision.isGranted());
    }

    @Test
    void shouldCreateInstanceViaStaticFactoryMethod() {
        SelfServiceUserAuthorizationManager result = SelfServiceUserAuthorizationManager.selfServiceUserAuthManager();
        assertNotNull(result);
    }
}
