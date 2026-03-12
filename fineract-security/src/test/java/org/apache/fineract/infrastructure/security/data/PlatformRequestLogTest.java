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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PlatformRequestLogTest {

    @Mock
    private HttpServletRequest request;

    @Test
    void shouldCreateFromStopWatchAndRequest() throws IOException {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        stopWatch.stop();

        Map<String, String[]> params = new HashMap<>();
        params.put("name", new String[] { "value" });
        params.put("password", new String[] { "secret" });
        params.put("_", new String[] { "timestamp" });

        when(request.getRequestURL()).thenReturn(new StringBuffer("https://example.com/api/v1/loans"));
        when(request.getParameterMap()).thenReturn(params);
        when(request.getMethod()).thenReturn("GET");

        PlatformRequestLog log = PlatformRequestLog.from(stopWatch, request);

        assertNotNull(log);
        assertEquals("GET", log.getMethod());
        assertEquals("https://example.com/api/v1/loans", log.getUrl());
        // Password and _ parameters should be removed
        assertFalse(log.getParameters().containsKey("password"));
        assertFalse(log.getParameters().containsKey("_"));
        // But name should remain
        assertNotNull(log.getParameters().get("name"));
    }

    @Test
    void shouldPopulateTimingFields() throws IOException {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        stopWatch.stop();

        when(request.getRequestURL()).thenReturn(new StringBuffer("https://example.com/api"));
        when(request.getParameterMap()).thenReturn(new HashMap<>());
        when(request.getMethod()).thenReturn("POST");

        PlatformRequestLog log = PlatformRequestLog.from(stopWatch, request);

        assertNotNull(log);
        assertEquals("POST", log.getMethod());
    }
}
