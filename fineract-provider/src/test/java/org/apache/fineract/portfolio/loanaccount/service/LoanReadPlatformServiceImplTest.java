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
package org.apache.fineract.portfolio.loanaccount.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.apache.fineract.infrastructure.codes.data.CodeValueData;
import org.apache.fineract.infrastructure.codes.service.CodeValueReadPlatformService;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.PaginationHelper;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.infrastructure.security.utils.ColumnValidator;
import org.apache.fineract.organisation.monetary.domain.ApplicationCurrency;
import org.apache.fineract.organisation.monetary.domain.ApplicationCurrencyRepositoryWrapper;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.office.domain.Office;
import org.apache.fineract.organisation.staff.data.StaffData;
import org.apache.fineract.organisation.staff.service.StaffReadPlatformService;
import org.apache.fineract.portfolio.accountdetails.service.AccountDetailsReadPlatformService;
import org.apache.fineract.portfolio.calendar.data.CalendarData;
import org.apache.fineract.portfolio.calendar.service.CalendarReadPlatformService;
import org.apache.fineract.portfolio.charge.service.ChargeReadPlatformService;
import org.apache.fineract.portfolio.client.data.ClientData;
import org.apache.fineract.portfolio.client.service.ClientReadPlatformService;
import org.apache.fineract.portfolio.delinquency.service.DelinquencyReadPlatformService;
import org.apache.fineract.portfolio.floatingrates.service.FloatingRatesReadPlatformService;
import org.apache.fineract.portfolio.fund.service.FundReadPlatformService;
import org.apache.fineract.portfolio.group.data.GroupGeneralData;
import org.apache.fineract.portfolio.group.service.GroupReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.data.DisbursementData;
import org.apache.fineract.portfolio.loanaccount.data.LoanAccountData;
import org.apache.fineract.portfolio.loanaccount.data.LoanSummaryData;
import org.apache.fineract.portfolio.loanaccount.data.LoanTransactionData;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRepository;
import org.apache.fineract.portfolio.loanaccount.exception.LoanNotFoundException;
import org.apache.fineract.portfolio.loanaccount.exception.LoanTransactionNotFoundException;
import org.apache.fineract.portfolio.loanaccount.mapper.LoanTransactionMapper;
import org.apache.fineract.portfolio.loanaccount.repository.LoanBuyDownFeeBalanceRepository;
import org.apache.fineract.portfolio.loanaccount.repository.LoanCapitalizedIncomeBalanceRepository;
import org.apache.fineract.portfolio.loanaccount.serialization.LoanForeclosureValidator;
import org.apache.fineract.portfolio.loanproduct.data.LoanProductData;
import org.apache.fineract.portfolio.loanproduct.service.LoanDropdownReadPlatformService;
import org.apache.fineract.portfolio.loanproduct.service.LoanProductReadPlatformService;
import org.apache.fineract.portfolio.paymenttype.service.PaymentTypeReadService;
import org.apache.fineract.useradministration.domain.AppUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LoanReadPlatformServiceImplTest {

    private static final Long LOAN_ID = 1L;
    private static final Long CLIENT_ID = 10L;
    private static final Long GROUP_ID = 20L;
    private static final Long PRODUCT_ID = 30L;
    private static final Long TRANSACTION_ID = 100L;

    @InjectMocks
    private LoanReadPlatformServiceImpl loanReadPlatformService;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private PlatformSecurityContext context;

    @Mock
    private LoanRepositoryWrapper loanRepositoryWrapper;

    @Mock
    private ApplicationCurrencyRepositoryWrapper applicationCurrencyRepository;

    @Mock
    private LoanProductReadPlatformService loanProductReadPlatformService;

    @Mock
    private ClientReadPlatformService clientReadPlatformService;

    @Mock
    private GroupReadPlatformService groupReadPlatformService;

    @Mock
    private LoanDropdownReadPlatformService loanDropdownReadPlatformService;

    @Mock
    private FundReadPlatformService fundReadPlatformService;

    @Mock
    private ChargeReadPlatformService chargeReadPlatformService;

    @Mock
    private CodeValueReadPlatformService codeValueReadPlatformService;

    @Mock
    private CalendarReadPlatformService calendarReadPlatformService;

    @Mock
    private StaffReadPlatformService staffReadPlatformService;

    @Mock
    private PaginationHelper paginationHelper;

    @Mock
    private PaymentTypeReadService paymentTypeReadPlatformService;

    @Mock
    private FloatingRatesReadPlatformService floatingRatesReadPlatformService;

    @Mock
    private LoanUtilService loanUtilService;

    @Mock
    private ConfigurationDomainService configurationDomainService;

    @Mock
    private AccountDetailsReadPlatformService accountDetailsReadPlatformService;

    @Mock
    private ColumnValidator columnValidator;

    @Mock
    private DatabaseSpecificSQLGenerator sqlGenerator;

    @Mock
    private DelinquencyReadPlatformService delinquencyReadPlatformService;

    @Mock
    private LoanTransactionRepository loanTransactionRepository;

    @Mock
    private LoanChargePaidByReadService loanChargePaidByReadService;

    @Mock
    private LoanTransactionRelationReadService loanTransactionRelationReadService;

    @Mock
    private LoanForeclosureValidator loanForeclosureValidator;

    @Mock
    private LoanTransactionMapper loanTransactionMapper;

    @Mock
    private LoanTransactionProcessingService loadTransactionProcessingService;

    @Mock
    private LoanBalanceService loanBalanceService;

    @Mock
    private LoanCapitalizedIncomeBalanceRepository loanCapitalizedIncomeBalanceRepository;

    @Mock
    private LoanBuyDownFeeBalanceRepository loanBuyDownFeeBalanceRepository;

    @Mock
    private InterestRefundServiceDelegate interestRefundServiceDelegate;

    @Mock
    private LoanMaximumAmountCalculator loanMaximumAmountCalculator;

    @Mock
    private LoanRepaymentScheduleService loanRepaymentScheduleService;

    private AppUser appUser;

    @BeforeEach
    void setUp() {
        appUser = mock(AppUser.class);
        Office office = mock(Office.class);
        when(office.getHierarchy()).thenReturn(".");
        when(appUser.getOffice()).thenReturn(office);
    }

    // ---- retrieveOne ----

    @Test
    void retrieveOne_shouldThrowLoanNotFoundException_whenNoResult() {
        when(context.getAuthenticatedUserIfPresent()).thenReturn(appUser);
        when(sqlGenerator.escape(anyString())).thenReturn("\"name\"");
        when(jdbcTemplate.queryForObject(anyString(), any(RowMapper.class), any(), any(), any()))
                .thenThrow(new EmptyResultDataAccessException(1));

        assertThrows(LoanNotFoundException.class, () -> loanReadPlatformService.retrieveOne(LOAN_ID));
    }

    @Test
    void retrieveOne_shouldReturnLoanAccountData_whenFound() {
        when(context.getAuthenticatedUserIfPresent()).thenReturn(appUser);
        when(sqlGenerator.escape(anyString())).thenReturn("\"name\"");
        LoanAccountData mockData = mock(LoanAccountData.class);
        when(jdbcTemplate.queryForObject(anyString(), any(RowMapper.class), any(), any(), any())).thenReturn(mockData);

        LoanAccountData result = loanReadPlatformService.retrieveOne(LOAN_ID);

        assertNotNull(result);
        assertEquals(mockData, result);
    }

    // ---- retrieveTemplateWithClientAndProductDetails ----

    @Test
    void retrieveTemplateWithClientAndProductDetails_withoutProduct_shouldReturnClientTemplate() {
        when(context.authenticatedUser()).thenReturn(appUser);
        ClientData clientData = mock(ClientData.class);
        when(clientReadPlatformService.retrieveOne(CLIENT_ID)).thenReturn(clientData);

        try (MockedStatic<DateUtils> mockedDateUtils = mockStatic(DateUtils.class)) {
            mockedDateUtils.when(DateUtils::getBusinessLocalDate).thenReturn(LocalDate.of(2026, 1, 15));
            LoanAccountData result = loanReadPlatformService.retrieveTemplateWithClientAndProductDetails(CLIENT_ID, null);

            assertNotNull(result);
        }
    }

    @Test
    void retrieveTemplateWithClientAndProductDetails_withProduct_shouldReturnPopulatedTemplate() {
        when(context.authenticatedUser()).thenReturn(appUser);
        ClientData clientData = mock(ClientData.class);
        when(clientReadPlatformService.retrieveOne(CLIENT_ID)).thenReturn(clientData);
        LoanProductData productData = mock(LoanProductData.class);
        when(loanProductReadPlatformService.retrieveLoanProduct(PRODUCT_ID)).thenReturn(productData);

        try (MockedStatic<DateUtils> mockedDateUtils = mockStatic(DateUtils.class)) {
            mockedDateUtils.when(DateUtils::getBusinessLocalDate).thenReturn(LocalDate.of(2026, 1, 15));
            LoanAccountData result = loanReadPlatformService.retrieveTemplateWithClientAndProductDetails(CLIENT_ID, PRODUCT_ID);

            assertNotNull(result);
            verify(loanProductReadPlatformService).retrieveLoanProduct(PRODUCT_ID);
        }
    }

    // ---- retrieveTemplateWithGroupAndProductDetails ----

    @Test
    void retrieveTemplateWithGroupAndProductDetails_withoutProduct_shouldReturnGroupTemplate() {
        when(context.authenticatedUser()).thenReturn(appUser);
        GroupGeneralData groupData = mock(GroupGeneralData.class);
        when(groupReadPlatformService.retrieveOne(GROUP_ID)).thenReturn(groupData);

        try (MockedStatic<DateUtils> mockedDateUtils = mockStatic(DateUtils.class)) {
            mockedDateUtils.when(DateUtils::getBusinessLocalDate).thenReturn(LocalDate.of(2026, 1, 15));
            LoanAccountData result = loanReadPlatformService.retrieveTemplateWithGroupAndProductDetails(GROUP_ID, null);

            assertNotNull(result);
        }
    }

    @Test
    void retrieveTemplateWithGroupAndProductDetails_withProduct_shouldReturnPopulatedTemplate() {
        when(context.authenticatedUser()).thenReturn(appUser);
        GroupGeneralData groupData = mock(GroupGeneralData.class);
        when(groupReadPlatformService.retrieveOne(GROUP_ID)).thenReturn(groupData);
        LoanProductData productData = mock(LoanProductData.class);
        when(loanProductReadPlatformService.retrieveLoanProduct(PRODUCT_ID)).thenReturn(productData);

        try (MockedStatic<DateUtils> mockedDateUtils = mockStatic(DateUtils.class)) {
            mockedDateUtils.when(DateUtils::getBusinessLocalDate).thenReturn(LocalDate.of(2026, 1, 15));
            LoanAccountData result = loanReadPlatformService.retrieveTemplateWithGroupAndProductDetails(GROUP_ID, PRODUCT_ID);

            assertNotNull(result);
            verify(loanProductReadPlatformService).retrieveLoanProduct(PRODUCT_ID);
        }
    }

    // ---- retrieveTemplateWithCompleteGroupAndProductDetails ----

    @Test
    void retrieveTemplateWithCompleteGroupAndProductDetails_withMembers_shouldReturnGroupTemplate() {
        when(context.authenticatedUser()).thenReturn(appUser);
        GroupGeneralData groupData = mock(GroupGeneralData.class);
        when(groupReadPlatformService.retrieveOne(GROUP_ID)).thenReturn(groupData);

        Collection<ClientData> members = List.of(mock(ClientData.class));
        when(clientReadPlatformService.retrieveClientMembersOfGroup(GROUP_ID)).thenReturn(members);

        try (MockedStatic<DateUtils> mockedDateUtils = mockStatic(DateUtils.class)) {
            mockedDateUtils.when(DateUtils::getBusinessLocalDate).thenReturn(LocalDate.of(2026, 1, 15));
            LoanAccountData result = loanReadPlatformService.retrieveTemplateWithCompleteGroupAndProductDetails(GROUP_ID, null);

            assertNotNull(result);
        }
    }

    @Test
    void retrieveTemplateWithCompleteGroupAndProductDetails_withEmptyMembers_shouldReturnGroupTemplate() {
        when(context.authenticatedUser()).thenReturn(appUser);
        GroupGeneralData groupData = mock(GroupGeneralData.class);
        when(groupReadPlatformService.retrieveOne(GROUP_ID)).thenReturn(groupData);
        when(clientReadPlatformService.retrieveClientMembersOfGroup(GROUP_ID)).thenReturn(Collections.emptyList());

        try (MockedStatic<DateUtils> mockedDateUtils = mockStatic(DateUtils.class)) {
            mockedDateUtils.when(DateUtils::getBusinessLocalDate).thenReturn(LocalDate.of(2026, 1, 15));
            LoanAccountData result = loanReadPlatformService.retrieveTemplateWithCompleteGroupAndProductDetails(GROUP_ID, null);

            assertNotNull(result);
        }
    }

    // ---- retrieveNewClosureDetails ----

    @Test
    void retrieveNewClosureDetails_shouldReturnTransactionData() {
        when(context.authenticatedUser()).thenReturn(appUser);

        try (MockedStatic<DateUtils> mockedDateUtils = mockStatic(DateUtils.class)) {
            mockedDateUtils.when(DateUtils::getBusinessLocalDate).thenReturn(LocalDate.of(2026, 1, 15));
            LoanTransactionData result = loanReadPlatformService.retrieveNewClosureDetails();

            assertNotNull(result);
            assertNotNull(result.getType());
        }
    }

    // ---- retrieveNumberOfRepayments ----

    @Test
    void retrieveNumberOfRepayments_shouldDelegateToRepository() {
        when(context.authenticatedUser()).thenReturn(appUser);
        when(loanRepositoryWrapper.getNumberOfRepayments(LOAN_ID)).thenReturn(12);

        Integer result = loanReadPlatformService.retrieveNumberOfRepayments(LOAN_ID);

        assertEquals(12, result);
        verify(loanRepositoryWrapper).getNumberOfRepayments(LOAN_ID);
    }

    // ---- retrieveAllowedLoanOfficers ----

    @Test
    void retrieveAllowedLoanOfficers_withNullOffice_shouldReturnNull() {
        Collection<StaffData> result = loanReadPlatformService.retrieveAllowedLoanOfficers(null, false);

        assertNull(result);
    }

    @Test
    void retrieveAllowedLoanOfficers_staffInSelectedOfficeOnly_shouldReturnOfficeStaff() {
        Long officeId = 5L;
        List<StaffData> staffList = List.of(mock(StaffData.class));
        when(staffReadPlatformService.retrieveAllLoanOfficersInOfficeById(officeId)).thenReturn(staffList);

        Collection<StaffData> result = loanReadPlatformService.retrieveAllowedLoanOfficers(officeId, true);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(staffReadPlatformService).retrieveAllLoanOfficersInOfficeById(officeId);
    }

    @Test
    void retrieveAllowedLoanOfficers_notStaffInSelectedOfficeOnly_shouldReturnHierarchyStaff() {
        Long officeId = 5L;
        List<StaffData> staffList = List.of(mock(StaffData.class), mock(StaffData.class));
        when(staffReadPlatformService.retrieveAllStaffInOfficeAndItsParentOfficeHierarchy(officeId, true)).thenReturn(staffList);

        Collection<StaffData> result = loanReadPlatformService.retrieveAllowedLoanOfficers(officeId, false);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(staffReadPlatformService).retrieveAllStaffInOfficeAndItsParentOfficeHierarchy(officeId, true);
    }

    // ---- getLoanIdByLoanExternalId ----

    @Test
    void getLoanIdByLoanExternalId_shouldReturnLoanId_whenFound() {
        String externalIdStr = "ext-123";
        ExternalId externalId = new ExternalId(externalIdStr);
        when(loanRepositoryWrapper.findIdByExternalId(any(ExternalId.class))).thenReturn(LOAN_ID);

        Long result = loanReadPlatformService.getLoanIdByLoanExternalId(externalIdStr);

        assertEquals(LOAN_ID, result);
    }

    @Test
    void getLoanIdByLoanExternalId_shouldThrowException_whenNotFound() {
        String externalIdStr = "ext-not-found";
        when(loanRepositoryWrapper.findIdByExternalId(any(ExternalId.class))).thenReturn(null);

        assertThrows(LoanNotFoundException.class, () -> loanReadPlatformService.getLoanIdByLoanExternalId(externalIdStr));
    }

    // ---- retrieveLoanIdByAccountNumber ----

    @Test
    void retrieveLoanIdByAccountNumber_shouldReturnLoanId_whenFound() {
        when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), anyString())).thenReturn(LOAN_ID);

        Long result = loanReadPlatformService.retrieveLoanIdByAccountNumber("000000001");

        assertEquals(LOAN_ID, result);
    }

    @Test
    void retrieveLoanIdByAccountNumber_shouldReturnNull_whenNotFound() {
        when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), anyString()))
                .thenThrow(new EmptyResultDataAccessException(1));

        Long result = loanReadPlatformService.retrieveLoanIdByAccountNumber("not-found");

        assertNull(result);
    }

    // ---- retrieveAccountNumberByAccountId ----

    @Test
    void retrieveAccountNumberByAccountId_shouldReturnAccountNumber_whenFound() {
        when(jdbcTemplate.queryForObject(anyString(), eq(String.class), anyLong())).thenReturn("000000001");

        String result = loanReadPlatformService.retrieveAccountNumberByAccountId(LOAN_ID);

        assertEquals("000000001", result);
    }

    @Test
    void retrieveAccountNumberByAccountId_shouldThrowLoanNotFoundException_whenNotFound() {
        when(jdbcTemplate.queryForObject(anyString(), eq(String.class), anyLong()))
                .thenThrow(new EmptyResultDataAccessException(1));

        assertThrows(LoanNotFoundException.class, () -> loanReadPlatformService.retrieveAccountNumberByAccountId(LOAN_ID));
    }

    // ---- retrieveNumberOfActiveLoans ----

    @Test
    void retrieveNumberOfActiveLoans_shouldReturnCount() {
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class))).thenReturn(42);

        Integer result = loanReadPlatformService.retrieveNumberOfActiveLoans();

        assertEquals(42, result);
    }

    // ---- isGuaranteeRequired ----

    @Test
    void isGuaranteeRequired_shouldReturnTrue_whenGuaranteeFlagIsTrue() {
        when(jdbcTemplate.queryForObject(anyString(), eq(Boolean.class), eq(LOAN_ID))).thenReturn(true);

        boolean result = loanReadPlatformService.isGuaranteeRequired(LOAN_ID);

        assertEquals(true, result);
    }

    @Test
    void isGuaranteeRequired_shouldReturnFalse_whenGuaranteeFlagIsFalse() {
        when(jdbcTemplate.queryForObject(anyString(), eq(Boolean.class), eq(LOAN_ID))).thenReturn(false);

        boolean result = loanReadPlatformService.isGuaranteeRequired(LOAN_ID);

        assertEquals(false, result);
    }

    @Test
    void isGuaranteeRequired_shouldReturnFalse_whenNull() {
        when(jdbcTemplate.queryForObject(anyString(), eq(Boolean.class), eq(LOAN_ID))).thenReturn(null);

        boolean result = loanReadPlatformService.isGuaranteeRequired(LOAN_ID);

        assertEquals(false, result);
    }

    // ---- existsByLoanId ----

    @Test
    void existsByLoanId_shouldDelegateToRepository() {
        when(loanRepositoryWrapper.existsByLoanId(LOAN_ID)).thenReturn(true);

        boolean result = loanReadPlatformService.existsByLoanId(LOAN_ID);

        assertEquals(true, result);
        verify(loanRepositoryWrapper).existsByLoanId(LOAN_ID);
    }

    @Test
    void existsByLoanId_shouldReturnFalse_whenLoanDoesNotExist() {
        when(loanRepositoryWrapper.existsByLoanId(999L)).thenReturn(false);

        boolean result = loanReadPlatformService.existsByLoanId(999L);

        assertEquals(false, result);
    }

    // ---- retrieveLoanIdByExternalId ----

    @Test
    void retrieveLoanIdByExternalId_shouldDelegateToRepository() {
        ExternalId externalId = new ExternalId("ext-456");
        when(loanRepositoryWrapper.findIdByExternalId(externalId)).thenReturn(LOAN_ID);

        Long result = loanReadPlatformService.retrieveLoanIdByExternalId(externalId);

        assertEquals(LOAN_ID, result);
    }

    // ---- retrieveLoanTransactionIdByExternalId ----

    @Test
    void retrieveLoanTransactionIdByExternalId_shouldDelegateToRepository() {
        ExternalId externalId = new ExternalId("tx-ext-789");
        when(loanTransactionRepository.findIdByExternalId(externalId)).thenReturn(TRANSACTION_ID);

        Long result = loanReadPlatformService.retrieveLoanTransactionIdByExternalId(externalId);

        assertEquals(TRANSACTION_ID, result);
    }

    // ---- getResolvedLoanId ----

    @Test
    void getResolvedLoanId_shouldReturnLoanId_whenFound() {
        ExternalId externalId = new ExternalId("resolve-ext-1");
        when(loanRepositoryWrapper.findIdByExternalId(externalId)).thenReturn(LOAN_ID);

        Long result = loanReadPlatformService.getResolvedLoanId(externalId);

        assertEquals(LOAN_ID, result);
    }

    @Test
    void getResolvedLoanId_shouldThrowLoanNotFoundException_whenNotFound() {
        ExternalId externalId = new ExternalId("resolve-not-found");
        when(loanRepositoryWrapper.findIdByExternalId(externalId)).thenReturn(null);

        assertThrows(LoanNotFoundException.class, () -> loanReadPlatformService.getResolvedLoanId(externalId));
    }

    // ---- getResolvedLoanTransactionId ----

    @Test
    void getResolvedLoanTransactionId_shouldReturnTransactionId_whenDirectIdProvided() {
        Long result = loanReadPlatformService.getResolvedLoanTransactionId(TRANSACTION_ID, ExternalId.empty());

        assertEquals(TRANSACTION_ID, result);
    }

    @Test
    void getResolvedLoanTransactionId_shouldResolveByExternalId_whenTransactionIdIsNull() {
        ExternalId externalId = new ExternalId("tx-ext-resolve");
        when(loanTransactionRepository.findIdByExternalId(externalId)).thenReturn(TRANSACTION_ID);

        Long result = loanReadPlatformService.getResolvedLoanTransactionId(null, externalId);

        assertEquals(TRANSACTION_ID, result);
    }

    @Test
    void getResolvedLoanTransactionId_shouldThrowException_whenExternalIdNotFound() {
        ExternalId externalId = new ExternalId("tx-ext-not-found");
        when(loanTransactionRepository.findIdByExternalId(externalId)).thenReturn(null);

        assertThrows(LoanTransactionNotFoundException.class,
                () -> loanReadPlatformService.getResolvedLoanTransactionId(null, externalId));
    }

    // ---- retrieveCalendars ----

    @Test
    void retrieveCalendars_shouldReturnCalendars() {
        List<CalendarData> parentCalendars = List.of(mock(CalendarData.class));
        List<CalendarData> entityCalendars = List.of(mock(CalendarData.class));
        List<CalendarData> updatedCalendars = new ArrayList<>();
        updatedCalendars.addAll(parentCalendars);
        updatedCalendars.addAll(entityCalendars);

        when(calendarReadPlatformService.retrieveParentCalendarsByEntity(eq(GROUP_ID), any(), any())).thenReturn(parentCalendars);
        when(calendarReadPlatformService.retrieveCalendarsByEntity(eq(GROUP_ID), any(), any())).thenReturn(entityCalendars);
        when(calendarReadPlatformService.updateWithRecurringDates(any())).thenReturn(updatedCalendars);

        Collection<CalendarData> result = loanReadPlatformService.retrieveCalendars(GROUP_ID);

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    // ---- retrieveApprovalTemplate ----

    @Test
    void retrieveApprovalTemplate_shouldReturnApprovalData() {
        Loan loan = mock(Loan.class);
        MonetaryCurrency currency = mock(MonetaryCurrency.class);
        ApplicationCurrency appCurrency = mock(ApplicationCurrency.class);

        when(loanRepositoryWrapper.findOneWithNotFoundDetection(LOAN_ID, true)).thenReturn(loan);
        when(loan.getCurrency()).thenReturn(currency);
        when(applicationCurrencyRepository.findOneWithNotFoundDetection(currency)).thenReturn(appCurrency);
        when(loan.getProposedPrincipal()).thenReturn(BigDecimal.valueOf(10000));
        when(loan.getNetDisbursalAmount()).thenReturn(BigDecimal.valueOf(9500));
        when(delinquencyReadPlatformService.calculateAvailableDisbursementAmountWithOverApplied(loan))
                .thenReturn(BigDecimal.valueOf(10000));

        try (MockedStatic<DateUtils> mockedDateUtils = mockStatic(DateUtils.class)) {
            mockedDateUtils.when(DateUtils::getBusinessLocalDate).thenReturn(LocalDate.of(2026, 1, 15));

            var result = loanReadPlatformService.retrieveApprovalTemplate(LOAN_ID);

            assertNotNull(result);
        }
    }

    // ---- retrieveRecoveryPaymentTemplate ----

    @Test
    void retrieveRecoveryPaymentTemplate_shouldReturnTransactionData() {
        Loan loan = mock(Loan.class);
        when(loanRepositoryWrapper.findOneWithNotFoundDetection(LOAN_ID, true)).thenReturn(loan);
        when(loan.getTotalWrittenOff()).thenReturn(BigDecimal.valueOf(500));
        when(loan.getNetDisbursalAmount()).thenReturn(BigDecimal.valueOf(9500));
        when(loan.getExternalId()).thenReturn(ExternalId.empty());
        when(paymentTypeReadPlatformService.retrieveAllPaymentTypes()).thenReturn(Collections.emptyList());

        LoanTransactionData result = loanReadPlatformService.retrieveRecoveryPaymentTemplate(LOAN_ID);

        assertNotNull(result);
    }

    // ---- retrieveTotalPaidInAdvance ----

    @Test
    void retrieveTotalPaidInAdvance_shouldReturnPaidInAdvanceData_whenFound() {
        when(sqlGenerator.currentBusinessDate()).thenReturn("CURRENT_DATE");
        when(jdbcTemplate.queryForObject(anyString(), eq(BigDecimal.class), eq(LOAN_ID)))
                .thenReturn(BigDecimal.valueOf(200));

        var result = loanReadPlatformService.retrieveTotalPaidInAdvance(LOAN_ID);

        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(200), result.getPaidInAdvance());
    }

    @Test
    void retrieveTotalPaidInAdvance_shouldReturnZero_whenNoData() {
        when(sqlGenerator.currentBusinessDate()).thenReturn("CURRENT_DATE");
        when(jdbcTemplate.queryForObject(anyString(), eq(BigDecimal.class), eq(LOAN_ID)))
                .thenThrow(new EmptyResultDataAccessException(1));

        var result = loanReadPlatformService.retrieveTotalPaidInAdvance(LOAN_ID);

        assertNotNull(result);
        assertEquals(new BigDecimal(0), result.getPaidInAdvance());
    }

    // ---- retrieveLoanWriteoffTemplate ----

    @Test
    void retrieveLoanWriteoffTemplate_shouldReturnTransactionDataWithWriteoffReasons() {
        LoanAccountData loanAccountData = mock(LoanAccountData.class);
        when(context.getAuthenticatedUserIfPresent()).thenReturn(appUser);
        when(sqlGenerator.escape(anyString())).thenReturn("\"name\"");
        when(jdbcTemplate.queryForObject(anyString(), any(RowMapper.class), any(), any(), any()))
                .thenReturn(loanAccountData);

        LoanSummaryData summary = mock(LoanSummaryData.class);
        when(loanAccountData.getSummary()).thenReturn(summary);
        when(summary.getTotalOutstanding()).thenReturn(BigDecimal.valueOf(1000));
        when(loanAccountData.getNetDisbursalAmount()).thenReturn(BigDecimal.valueOf(9500));
        when(loanAccountData.getExternalId()).thenReturn(ExternalId.empty());

        List<CodeValueData> writeoffReasons = List.of(mock(CodeValueData.class));
        when(codeValueReadPlatformService.retrieveCodeValuesByCode(anyString())).thenReturn(writeoffReasons);

        try (MockedStatic<DateUtils> mockedDateUtils = mockStatic(DateUtils.class)) {
            mockedDateUtils.when(DateUtils::getBusinessLocalDate).thenReturn(LocalDate.of(2026, 1, 15));

            LoanTransactionData result = loanReadPlatformService.retrieveLoanWriteoffTemplate(LOAN_ID);

            assertNotNull(result);
        }
    }

    // ---- countInstallmentsByLoanIdWhereIsAdditionalFalseAndIsDownPaymentFalse ----

    @Test
    void countInstallments_shouldDelegateToRepaymentScheduleService() {
        when(loanRepaymentScheduleService.countInstallmentsByLoanIdWhereIsAdditionalFalseAndIsDownPaymentFalse(LOAN_ID)).thenReturn(10);

        Integer result = loanReadPlatformService.countInstallmentsByLoanIdWhereIsAdditionalFalseAndIsDownPaymentFalse(LOAN_ID);

        assertEquals(10, result);
        verify(loanRepaymentScheduleService).countInstallmentsByLoanIdWhereIsAdditionalFalseAndIsDownPaymentFalse(LOAN_ID);
    }

    // ---- retrieveLoanChargeOffTemplate ----

    @Test
    void retrieveLoanChargeOffTemplate_shouldReturnTransactionDataWithChargeOffReasons() {
        LoanAccountData loanAccountData = mock(LoanAccountData.class);
        when(context.getAuthenticatedUserIfPresent()).thenReturn(appUser);
        when(sqlGenerator.escape(anyString())).thenReturn("\"name\"");
        when(jdbcTemplate.queryForObject(anyString(), any(RowMapper.class), any(), any(), any()))
                .thenReturn(loanAccountData);

        LoanSummaryData summary = mock(LoanSummaryData.class);
        when(loanAccountData.getSummary()).thenReturn(summary);
        when(summary.getTotalOutstanding()).thenReturn(BigDecimal.valueOf(1000));
        when(summary.getPrincipalOutstanding()).thenReturn(BigDecimal.valueOf(800));
        when(summary.getInterestOutstanding()).thenReturn(BigDecimal.valueOf(100));
        when(summary.getFeeChargesOutstanding()).thenReturn(BigDecimal.valueOf(50));
        when(summary.getPenaltyChargesOutstanding()).thenReturn(BigDecimal.valueOf(50));
        when(loanAccountData.getNetDisbursalAmount()).thenReturn(BigDecimal.valueOf(9500));
        when(loanAccountData.getExternalId()).thenReturn(ExternalId.empty());

        List<CodeValueData> chargeOffReasons = List.of(mock(CodeValueData.class));
        when(codeValueReadPlatformService.retrieveCodeValuesByCode(anyString())).thenReturn(chargeOffReasons);

        try (MockedStatic<DateUtils> mockedDateUtils = mockStatic(DateUtils.class)) {
            mockedDateUtils.when(DateUtils::getBusinessLocalDate).thenReturn(LocalDate.of(2026, 1, 15));

            LoanTransactionData result = loanReadPlatformService.retrieveLoanChargeOffTemplate(LOAN_ID);

            assertNotNull(result);
        }
    }

    // ---- retrieveDisbursalTemplate ----

    @Test
    void retrieveDisbursalTemplate_withPaymentDetails_shouldReturnTransactionData() {
        Loan loan = mock(Loan.class);
        MonetaryCurrency currency = mock(MonetaryCurrency.class);
        ApplicationCurrency appCurrency = mock(ApplicationCurrency.class);

        when(loanRepositoryWrapper.findOneWithNotFoundDetection(LOAN_ID, true)).thenReturn(loan);
        when(loan.getCurrency()).thenReturn(currency);
        when(applicationCurrencyRepository.findOneWithNotFoundDetection(currency)).thenReturn(appCurrency);
        when(loan.getExpectedDisbursedOnLocalDateForTemplate()).thenReturn(LocalDate.of(2026, 2, 1));
        when(loan.getDisburseAmountForTemplate()).thenReturn(BigDecimal.valueOf(10000));
        when(loan.getNetDisbursalAmount()).thenReturn(BigDecimal.valueOf(9500));
        when(loan.retriveLastEmiAmount()).thenReturn(BigDecimal.valueOf(500));
        when(loan.getNextPossibleRepaymentDateForRescheduling()).thenReturn(LocalDate.of(2026, 3, 1));
        when(paymentTypeReadPlatformService.retrieveAllPaymentTypes()).thenReturn(Collections.emptyList());
        when(delinquencyReadPlatformService.calculateAvailableDisbursementAmountWithOverApplied(loan))
                .thenReturn(BigDecimal.valueOf(10000));

        LoanTransactionData result = loanReadPlatformService.retrieveDisbursalTemplate(LOAN_ID, true);

        assertNotNull(result);
        verify(paymentTypeReadPlatformService).retrieveAllPaymentTypes();
    }

    @Test
    void retrieveDisbursalTemplate_withoutPaymentDetails_shouldReturnTransactionDataWithoutPaymentOptions() {
        Loan loan = mock(Loan.class);
        MonetaryCurrency currency = mock(MonetaryCurrency.class);
        ApplicationCurrency appCurrency = mock(ApplicationCurrency.class);

        when(loanRepositoryWrapper.findOneWithNotFoundDetection(LOAN_ID, true)).thenReturn(loan);
        when(loan.getCurrency()).thenReturn(currency);
        when(applicationCurrencyRepository.findOneWithNotFoundDetection(currency)).thenReturn(appCurrency);
        when(loan.getExpectedDisbursedOnLocalDateForTemplate()).thenReturn(LocalDate.of(2026, 2, 1));
        when(loan.getDisburseAmountForTemplate()).thenReturn(BigDecimal.valueOf(10000));
        when(loan.getNetDisbursalAmount()).thenReturn(BigDecimal.valueOf(9500));
        when(loan.retriveLastEmiAmount()).thenReturn(BigDecimal.valueOf(500));
        when(loan.getNextPossibleRepaymentDateForRescheduling()).thenReturn(LocalDate.of(2026, 3, 1));
        when(delinquencyReadPlatformService.calculateAvailableDisbursementAmountWithOverApplied(loan))
                .thenReturn(BigDecimal.valueOf(10000));

        LoanTransactionData result = loanReadPlatformService.retrieveDisbursalTemplate(LOAN_ID, false);

        assertNotNull(result);
    }

    // ---- retrieveLoanDisbursementDetails (single) ----

    @Test
    void retrieveLoanDisbursementDetails_shouldReturnEmptyList_whenNoDisbursements() {
        when(sqlGenerator.inParametersFor(any(List.class))).thenReturn(new Object[] { LOAN_ID });
        when(sqlGenerator.escape(anyString())).thenReturn("\"name\"");
        when(sqlGenerator.in(anyString(), any(List.class))).thenReturn("dd.loan_id IN (?)");
        when(sqlGenerator.groupConcat(anyString())).thenReturn("GROUP_CONCAT(lc.id)");
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), any(Object[].class))).thenReturn(Collections.emptyList());

        Collection<DisbursementData> result = loanReadPlatformService.retrieveLoanDisbursementDetails(LOAN_ID);

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    // ---- retrieveLoanIdsWithPendingIncomePostingTransactions ----

    @Test
    void retrieveLoanIdsWithPendingIncomePostingTransactions_shouldReturnLoanIds() {
        List<Long> expectedIds = List.of(1L, 2L, 3L);

        try (MockedStatic<DateUtils> mockedDateUtils = mockStatic(DateUtils.class)) {
            mockedDateUtils.when(DateUtils::getBusinessLocalDate).thenReturn(LocalDate.of(2026, 1, 15));
            when(jdbcTemplate.queryForList(anyString(), eq(Long.class), any(Object[].class))).thenReturn(expectedIds);

            Collection<Long> result = loanReadPlatformService.retrieveLoanIdsWithPendingIncomePostingTransactions();

            assertNotNull(result);
            assertEquals(3, result.size());
        }
    }

    @Test
    void retrieveLoanIdsWithPendingIncomePostingTransactions_shouldReturnNull_whenNoResults() {
        try (MockedStatic<DateUtils> mockedDateUtils = mockStatic(DateUtils.class)) {
            mockedDateUtils.when(DateUtils::getBusinessLocalDate).thenReturn(LocalDate.of(2026, 1, 15));
            when(jdbcTemplate.queryForList(anyString(), eq(Long.class), any(Object[].class)))
                    .thenThrow(new EmptyResultDataAccessException(1));

            Collection<Long> result = loanReadPlatformService.retrieveLoanIdsWithPendingIncomePostingTransactions();

            assertNull(result);
        }
    }

    // ---- retrieveLoanIdsByExternalIds ----

    @Test
    void retrieveLoanIdsByExternalIds_shouldDelegateToRepository() {
        List<ExternalId> externalIds = List.of(new ExternalId("ext-1"), new ExternalId("ext-2"));
        List<Long> expectedIds = List.of(1L, 2L);
        when(loanRepositoryWrapper.findIdByExternalIds(externalIds)).thenReturn(expectedIds);

        List<Long> result = loanReadPlatformService.retrieveLoanIdsByExternalIds(externalIds);

        assertEquals(expectedIds, result);
        verify(loanRepositoryWrapper).findIdByExternalIds(externalIds);
    }
}
