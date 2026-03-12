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

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.util.HashMap;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.businessdate.service.BusinessDateReadPlatformService;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BusinessDateFilterTest {

    @Mock
    private BusinessDateReadPlatformService businessDateReadPlatformService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private BusinessDateFilter filter;

    @BeforeEach
    void setUp() {
        filter = new BusinessDateFilter(businessDateReadPlatformService);
    }

    @AfterEach
    void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    @Test
    void shouldSetBusinessDatesWhenTenantIsPresent() throws Exception {
        FineractPlatformTenant tenant = new FineractPlatformTenant(1L, "default", "Default", "UTC", null);
        ThreadLocalContextUtil.setTenant(tenant);

        HashMap<BusinessDateType, LocalDate> businessDates = new HashMap<>();
        businessDates.put(BusinessDateType.BUSINESS_DATE, LocalDate.of(2026, 1, 15));
        when(businessDateReadPlatformService.getBusinessDates()).thenReturn(businessDates);

        filter.doFilterInternal(request, response, filterChain);

        verify(businessDateReadPlatformService).getBusinessDates();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldNotSetBusinessDatesWhenTenantIsNotPresent() throws Exception {
        ThreadLocalContextUtil.reset();

        filter.doFilterInternal(request, response, filterChain);

        verify(businessDateReadPlatformService, never()).getBusinessDates();
        verify(filterChain).doFilter(request, response);
    }
}
