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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.RedirectStrategy;

@ExtendWith(MockitoExtension.class)
class CustomAuthenticationFailureHandlerTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private RedirectStrategy redirectStrategy;

    private CustomAuthenticationFailureHandler handler;

    private final AuthenticationException exception = new BadCredentialsException("Bad credentials");

    @BeforeEach
    void setUp() {
        handler = new CustomAuthenticationFailureHandler();
    }

    @Test
    void shouldSend401WhenNoFailureUrlIsSet() throws Exception {
        handler.onAuthenticationFailure(request, response, exception);

        verify(response).sendError(eq(HttpServletResponse.SC_UNAUTHORIZED), anyString());
    }

    @Test
    void shouldRedirectToFailureUrlWithOauthToken() throws Exception {
        handler.setDefaultFailureUrl("/login?error");
        handler.setRedirectStrategy(redirectStrategy);
        HttpSession session = mock(HttpSession.class);
        when(request.getSession(false)).thenReturn(session);
        when(request.getSession()).thenReturn(session);
        when(request.getParameter("oauth_token")).thenReturn("testToken123");

        handler.onAuthenticationFailure(request, response, exception);

        verify(redirectStrategy).sendRedirect(eq(request), eq(response), eq("/login?error?oauth_token=testToken123"));
    }

    @Test
    void shouldForwardWhenForwardToDestinationIsSet() throws Exception {
        handler.setDefaultFailureUrl("/login?error");
        handler.setUseForward(true);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);
        when(request.getRequestDispatcher("/login?error")).thenReturn(dispatcher);

        handler.onAuthenticationFailure(request, response, exception);

        verify(dispatcher).forward(request, response);
    }

    @Test
    void shouldSaveExceptionToSessionOnRedirect() throws Exception {
        handler.setDefaultFailureUrl("/login?error");
        handler.setRedirectStrategy(redirectStrategy);
        HttpSession session = mock(HttpSession.class);
        when(request.getSession(false)).thenReturn(session);
        when(request.getSession()).thenReturn(session);
        when(request.getParameter("oauth_token")).thenReturn("tok");

        handler.onAuthenticationFailure(request, response, exception);

        verify(session).setAttribute(anyString(), eq(exception));
    }

    @Test
    void shouldSaveExceptionToRequestOnForward() throws Exception {
        handler.setDefaultFailureUrl("/login?error");
        handler.setUseForward(true);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);
        when(request.getRequestDispatcher("/login?error")).thenReturn(dispatcher);

        handler.onAuthenticationFailure(request, response, exception);

        verify(request).setAttribute(anyString(), eq(exception));
    }

    @Test
    void shouldReturnCorrectUseForwardValue() {
        assertFalse(handler.isUseForward());
        handler.setUseForward(true);
        assertTrue(handler.isUseForward());
    }

    @Test
    void shouldReturnCorrectAllowSessionCreationValue() {
        assertTrue(handler.isAllowSessionCreation());
        handler.setAllowSessionCreation(false);
        assertFalse(handler.isAllowSessionCreation());
    }

    @Test
    void shouldReturnRedirectStrategy() {
        handler.setRedirectStrategy(redirectStrategy);
        assertEquals(redirectStrategy, handler.getRedirectStrategy());
    }

    @Test
    void shouldHaveDefaultRedirectStrategy() {
        assertNotNull(handler.getRedirectStrategy());
    }

    @Test
    void shouldCreateSessionIfAllowedWhenNoExistingSession() throws IOException {
        handler.setDefaultFailureUrl("/login?error");
        handler.setRedirectStrategy(redirectStrategy);
        handler.setAllowSessionCreation(true);
        HttpSession session = mock(HttpSession.class);
        when(request.getSession(false)).thenReturn(null);
        when(request.getSession()).thenReturn(session);
        when(request.getParameter("oauth_token")).thenReturn("tok");

        try {
            handler.onAuthenticationFailure(request, response, exception);
        } catch (Exception e) {
            // ignore
        }

        verify(request).getSession();
    }
}
