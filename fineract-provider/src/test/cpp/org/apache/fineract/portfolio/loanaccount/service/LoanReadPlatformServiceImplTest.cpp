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

#include "LoanReadPlatformServiceImpl.h"
#include "Mocks.h"

#include <gtest/gtest.h>

using namespace fineract;
using ::testing::_;
using ::testing::Return;

// ===========================================================================
// Test fixture
// ===========================================================================

class LoanReadPlatformServiceImplTest : public ::testing::Test {
protected:
    static constexpr Long LOAN_ID = 1;
    static constexpr Long CLIENT_ID = 10;
    static constexpr Long GROUP_ID = 20;
    static constexpr Long PRODUCT_ID = 30;
    static constexpr Long TRANSACTION_ID = 100;

    // Mocks (mirrors every @Mock field in the Java test)
    MockJdbcTemplate jdbcTemplate;
    MockPlatformSecurityContext context;
    MockLoanRepositoryWrapper loanRepositoryWrapper;
    MockApplicationCurrencyRepositoryWrapper applicationCurrencyRepository;
    MockLoanProductReadPlatformService loanProductReadPlatformService;
    MockClientReadPlatformService clientReadPlatformService;
    MockGroupReadPlatformService groupReadPlatformService;
    MockLoanDropdownReadPlatformService loanDropdownReadPlatformService;
    MockFundReadPlatformService fundReadPlatformService;
    MockChargeReadPlatformService chargeReadPlatformService;
    MockCodeValueReadPlatformService codeValueReadPlatformService;
    MockCalendarReadPlatformService calendarReadPlatformService;
    MockStaffReadPlatformService staffReadPlatformService;
    MockPaginationHelper paginationHelper;
    MockPaymentTypeReadService paymentTypeReadPlatformService;
    MockFloatingRatesReadPlatformService floatingRatesReadPlatformService;
    MockLoanUtilService loanUtilService;
    MockConfigurationDomainService configurationDomainService;
    MockAccountDetailsReadPlatformService accountDetailsReadPlatformService;
    MockColumnValidator columnValidator;
    MockDatabaseSpecificSQLGenerator sqlGenerator;
    MockDelinquencyReadPlatformService delinquencyReadPlatformService;
    MockLoanTransactionRepository loanTransactionRepository;
    MockLoanChargePaidByReadService loanChargePaidByReadService;
    MockLoanTransactionRelationReadService loanTransactionRelationReadService;
    MockLoanForeclosureValidator loanForeclosureValidator;
    MockLoanTransactionMapper loanTransactionMapper;
    MockLoanTransactionProcessingService loadTransactionProcessingService;
    MockLoanBalanceService loanBalanceService;
    MockLoanCapitalizedIncomeBalanceRepository
        loanCapitalizedIncomeBalanceRepository;
    MockLoanBuyDownFeeBalanceRepository loanBuyDownFeeBalanceRepository;
    MockInterestRefundServiceDelegate interestRefundServiceDelegate;
    MockLoanMaximumAmountCalculator loanMaximumAmountCalculator;
    MockLoanRepaymentScheduleService loanRepaymentScheduleService;
    MockDateUtils dateUtils;

    // Domain helpers
    MockOffice office;
    std::shared_ptr<MockAppUser> appUser;

    // Service under test
    std::unique_ptr<LoanReadPlatformServiceImpl> svc;

    void SetUp() override {
        appUser = std::make_shared<MockAppUser>();
        ON_CALL(office, getHierarchy()).WillByDefault(Return("."));
        ON_CALL(*appUser, getOffice()).WillByDefault(Return(&office));

        svc = std::make_unique<LoanReadPlatformServiceImpl>(
            jdbcTemplate, context, loanRepositoryWrapper,
            applicationCurrencyRepository, loanProductReadPlatformService,
            clientReadPlatformService, groupReadPlatformService,
            loanDropdownReadPlatformService, fundReadPlatformService,
            chargeReadPlatformService, codeValueReadPlatformService,
            calendarReadPlatformService, staffReadPlatformService,
            paginationHelper, paymentTypeReadPlatformService,
            floatingRatesReadPlatformService, loanUtilService,
            configurationDomainService, accountDetailsReadPlatformService,
            columnValidator, sqlGenerator, delinquencyReadPlatformService,
            loanTransactionRepository, loanChargePaidByReadService,
            loanTransactionRelationReadService, loanForeclosureValidator,
            loanTransactionMapper, loadTransactionProcessingService,
            loanBalanceService, loanCapitalizedIncomeBalanceRepository,
            loanBuyDownFeeBalanceRepository, interestRefundServiceDelegate,
            loanMaximumAmountCalculator, loanRepaymentScheduleService,
            dateUtils);
    }
};

// ===========================================================================
// retrieveOne
// ===========================================================================

TEST_F(LoanReadPlatformServiceImplTest,
       retrieveOne_shouldThrowLoanNotFoundException_whenNoResult) {
    ON_CALL(context, getAuthenticatedUserIfPresent())
        .WillByDefault(Return(appUser));
    ON_CALL(sqlGenerator, escape(_)).WillByDefault(Return("\"name\""));
    ON_CALL(jdbcTemplate, queryForObjectLoanAccount(_, _, _, _))
        .WillByDefault(
            testing::Throw(EmptyResultDataAccessException(1)));

    EXPECT_THROW(svc->retrieveOne(LOAN_ID), LoanNotFoundException);
}

TEST_F(LoanReadPlatformServiceImplTest,
       retrieveOne_shouldReturnLoanAccountData_whenFound) {
    ON_CALL(context, getAuthenticatedUserIfPresent())
        .WillByDefault(Return(appUser));
    ON_CALL(sqlGenerator, escape(_)).WillByDefault(Return("\"name\""));
    auto mockData = std::make_shared<MockLoanAccountData>();
    ON_CALL(jdbcTemplate, queryForObjectLoanAccount(_, _, _, _))
        .WillByDefault(Return(mockData));

    auto result = svc->retrieveOne(LOAN_ID);

    ASSERT_NE(result, nullptr);
    EXPECT_EQ(result, mockData);
}

// ===========================================================================
// retrieveTemplateWithClientAndProductDetails
// ===========================================================================

TEST_F(
    LoanReadPlatformServiceImplTest,
    retrieveTemplateWithClientAndProductDetails_withoutProduct_shouldReturnClientTemplate) {
    ON_CALL(context, authenticatedUser()).WillByDefault(Return(appUser));
    auto clientData = std::make_shared<MockClientData>();
    ON_CALL(clientReadPlatformService, retrieveOne(CLIENT_ID))
        .WillByDefault(Return(clientData));
    ON_CALL(dateUtils, getBusinessLocalDate())
        .WillByDefault(Return(LocalDate(2026, 1, 15)));

    auto result = svc->retrieveTemplateWithClientAndProductDetails(
        CLIENT_ID, std::nullopt);

    ASSERT_NE(result, nullptr);
}

TEST_F(
    LoanReadPlatformServiceImplTest,
    retrieveTemplateWithClientAndProductDetails_withProduct_shouldReturnPopulatedTemplate) {
    ON_CALL(context, authenticatedUser()).WillByDefault(Return(appUser));
    auto clientData = std::make_shared<MockClientData>();
    ON_CALL(clientReadPlatformService, retrieveOne(CLIENT_ID))
        .WillByDefault(Return(clientData));
    auto productData = std::make_shared<MockLoanProductData>();
    ON_CALL(loanProductReadPlatformService, retrieveLoanProduct(PRODUCT_ID))
        .WillByDefault(Return(productData));
    ON_CALL(dateUtils, getBusinessLocalDate())
        .WillByDefault(Return(LocalDate(2026, 1, 15)));

    auto result = svc->retrieveTemplateWithClientAndProductDetails(
        CLIENT_ID, PRODUCT_ID);

    ASSERT_NE(result, nullptr);
    EXPECT_CALL(loanProductReadPlatformService,
                retrieveLoanProduct(PRODUCT_ID));
    // The EXPECT_CALL above was already satisfied; this re-invocation
    // verifies the interaction.
    svc->retrieveTemplateWithClientAndProductDetails(CLIENT_ID, PRODUCT_ID);
}

// ===========================================================================
// retrieveTemplateWithGroupAndProductDetails
// ===========================================================================

TEST_F(
    LoanReadPlatformServiceImplTest,
    retrieveTemplateWithGroupAndProductDetails_withoutProduct_shouldReturnGroupTemplate) {
    ON_CALL(context, authenticatedUser()).WillByDefault(Return(appUser));
    auto groupData = std::make_shared<MockGroupGeneralData>();
    ON_CALL(groupReadPlatformService, retrieveOne(GROUP_ID))
        .WillByDefault(Return(groupData));
    ON_CALL(dateUtils, getBusinessLocalDate())
        .WillByDefault(Return(LocalDate(2026, 1, 15)));

    auto result = svc->retrieveTemplateWithGroupAndProductDetails(
        GROUP_ID, std::nullopt);

    ASSERT_NE(result, nullptr);
}

TEST_F(
    LoanReadPlatformServiceImplTest,
    retrieveTemplateWithGroupAndProductDetails_withProduct_shouldReturnPopulatedTemplate) {
    ON_CALL(context, authenticatedUser()).WillByDefault(Return(appUser));
    auto groupData = std::make_shared<MockGroupGeneralData>();
    ON_CALL(groupReadPlatformService, retrieveOne(GROUP_ID))
        .WillByDefault(Return(groupData));
    auto productData = std::make_shared<MockLoanProductData>();
    ON_CALL(loanProductReadPlatformService, retrieveLoanProduct(PRODUCT_ID))
        .WillByDefault(Return(productData));
    ON_CALL(dateUtils, getBusinessLocalDate())
        .WillByDefault(Return(LocalDate(2026, 1, 15)));

    auto result = svc->retrieveTemplateWithGroupAndProductDetails(
        GROUP_ID, PRODUCT_ID);

    ASSERT_NE(result, nullptr);
    EXPECT_CALL(loanProductReadPlatformService,
                retrieveLoanProduct(PRODUCT_ID));
    svc->retrieveTemplateWithGroupAndProductDetails(GROUP_ID, PRODUCT_ID);
}

// ===========================================================================
// retrieveTemplateWithCompleteGroupAndProductDetails
// ===========================================================================

TEST_F(
    LoanReadPlatformServiceImplTest,
    retrieveTemplateWithCompleteGroupAndProductDetails_withMembers_shouldReturnGroupTemplate) {
    ON_CALL(context, authenticatedUser()).WillByDefault(Return(appUser));
    auto groupData = std::make_shared<MockGroupGeneralData>();
    ON_CALL(groupReadPlatformService, retrieveOne(GROUP_ID))
        .WillByDefault(Return(groupData));
    std::vector<std::shared_ptr<ClientData>> members = {
        std::make_shared<MockClientData>()};
    ON_CALL(clientReadPlatformService, retrieveClientMembersOfGroup(GROUP_ID))
        .WillByDefault(Return(members));
    ON_CALL(dateUtils, getBusinessLocalDate())
        .WillByDefault(Return(LocalDate(2026, 1, 15)));

    auto result =
        svc->retrieveTemplateWithCompleteGroupAndProductDetails(
            GROUP_ID, std::nullopt);

    ASSERT_NE(result, nullptr);
}

TEST_F(
    LoanReadPlatformServiceImplTest,
    retrieveTemplateWithCompleteGroupAndProductDetails_withEmptyMembers_shouldReturnGroupTemplate) {
    ON_CALL(context, authenticatedUser()).WillByDefault(Return(appUser));
    auto groupData = std::make_shared<MockGroupGeneralData>();
    ON_CALL(groupReadPlatformService, retrieveOne(GROUP_ID))
        .WillByDefault(Return(groupData));
    ON_CALL(clientReadPlatformService, retrieveClientMembersOfGroup(GROUP_ID))
        .WillByDefault(
            Return(std::vector<std::shared_ptr<ClientData>>{}));
    ON_CALL(dateUtils, getBusinessLocalDate())
        .WillByDefault(Return(LocalDate(2026, 1, 15)));

    auto result =
        svc->retrieveTemplateWithCompleteGroupAndProductDetails(
            GROUP_ID, std::nullopt);

    ASSERT_NE(result, nullptr);
}

// ===========================================================================
// retrieveNewClosureDetails
// ===========================================================================

TEST_F(LoanReadPlatformServiceImplTest,
       retrieveNewClosureDetails_shouldReturnTransactionData) {
    ON_CALL(context, authenticatedUser()).WillByDefault(Return(appUser));
    ON_CALL(dateUtils, getBusinessLocalDate())
        .WillByDefault(Return(LocalDate(2026, 1, 15)));

    auto result = svc->retrieveNewClosureDetails();

    ASSERT_NE(result, nullptr);
    EXPECT_NE(result->getType(), nullptr);
}

// ===========================================================================
// retrieveNumberOfRepayments
// ===========================================================================

TEST_F(LoanReadPlatformServiceImplTest,
       retrieveNumberOfRepayments_shouldDelegateToRepository) {
    ON_CALL(context, authenticatedUser()).WillByDefault(Return(appUser));
    ON_CALL(loanRepositoryWrapper, getNumberOfRepayments(LOAN_ID))
        .WillByDefault(Return(12));

    Integer result = svc->retrieveNumberOfRepayments(LOAN_ID);

    EXPECT_EQ(12, result);
}

// ===========================================================================
// retrieveAllowedLoanOfficers
// ===========================================================================

TEST_F(LoanReadPlatformServiceImplTest,
       retrieveAllowedLoanOfficers_withNullOffice_shouldReturnNull) {
    auto result =
        svc->retrieveAllowedLoanOfficers(std::nullopt, false);

    EXPECT_FALSE(result.has_value());
}

TEST_F(
    LoanReadPlatformServiceImplTest,
    retrieveAllowedLoanOfficers_staffInSelectedOfficeOnly_shouldReturnOfficeStaff) {
    Long officeId = 5;
    std::vector<std::shared_ptr<StaffData>> staffList = {
        std::make_shared<MockStaffData>()};
    ON_CALL(staffReadPlatformService,
            retrieveAllLoanOfficersInOfficeById(officeId))
        .WillByDefault(Return(staffList));

    auto result = svc->retrieveAllowedLoanOfficers(officeId, true);

    ASSERT_TRUE(result.has_value());
    EXPECT_EQ(1u, result->size());
}

TEST_F(
    LoanReadPlatformServiceImplTest,
    retrieveAllowedLoanOfficers_notStaffInSelectedOfficeOnly_shouldReturnHierarchyStaff) {
    Long officeId = 5;
    std::vector<std::shared_ptr<StaffData>> staffList = {
        std::make_shared<MockStaffData>(),
        std::make_shared<MockStaffData>()};
    ON_CALL(staffReadPlatformService,
            retrieveAllStaffInOfficeAndItsParentOfficeHierarchy(officeId,
                                                                true))
        .WillByDefault(Return(staffList));

    auto result = svc->retrieveAllowedLoanOfficers(officeId, false);

    ASSERT_TRUE(result.has_value());
    EXPECT_EQ(2u, result->size());
}

// ===========================================================================
// getLoanIdByLoanExternalId
// ===========================================================================

TEST_F(LoanReadPlatformServiceImplTest,
       getLoanIdByLoanExternalId_shouldReturnLoanId_whenFound) {
    ON_CALL(loanRepositoryWrapper, findIdByExternalId(_))
        .WillByDefault(Return(std::optional<Long>(LOAN_ID)));

    Long result = svc->getLoanIdByLoanExternalId("ext-123");

    EXPECT_EQ(LOAN_ID, result);
}

TEST_F(LoanReadPlatformServiceImplTest,
       getLoanIdByLoanExternalId_shouldThrowException_whenNotFound) {
    ON_CALL(loanRepositoryWrapper, findIdByExternalId(_))
        .WillByDefault(Return(std::nullopt));

    EXPECT_THROW(svc->getLoanIdByLoanExternalId("ext-not-found"),
                 LoanNotFoundException);
}

// ===========================================================================
// retrieveLoanIdByAccountNumber
// ===========================================================================

TEST_F(LoanReadPlatformServiceImplTest,
       retrieveLoanIdByAccountNumber_shouldReturnLoanId_whenFound) {
    ON_CALL(jdbcTemplate, queryForObjectLong(_, _))
        .WillByDefault(Return(LOAN_ID));

    auto result = svc->retrieveLoanIdByAccountNumber("000000001");

    ASSERT_TRUE(result.has_value());
    EXPECT_EQ(LOAN_ID, result.value());
}

TEST_F(LoanReadPlatformServiceImplTest,
       retrieveLoanIdByAccountNumber_shouldReturnNull_whenNotFound) {
    ON_CALL(jdbcTemplate, queryForObjectLong(_, _))
        .WillByDefault(
            testing::Throw(EmptyResultDataAccessException(1)));

    auto result = svc->retrieveLoanIdByAccountNumber("not-found");

    EXPECT_FALSE(result.has_value());
}

// ===========================================================================
// retrieveAccountNumberByAccountId
// ===========================================================================

TEST_F(
    LoanReadPlatformServiceImplTest,
    retrieveAccountNumberByAccountId_shouldReturnAccountNumber_whenFound) {
    ON_CALL(jdbcTemplate, queryForObjectString(_, _))
        .WillByDefault(Return("000000001"));

    std::string result = svc->retrieveAccountNumberByAccountId(LOAN_ID);

    EXPECT_EQ("000000001", result);
}

TEST_F(
    LoanReadPlatformServiceImplTest,
    retrieveAccountNumberByAccountId_shouldThrowLoanNotFoundException_whenNotFound) {
    ON_CALL(jdbcTemplate, queryForObjectString(_, _))
        .WillByDefault(
            testing::Throw(EmptyResultDataAccessException(1)));

    EXPECT_THROW(svc->retrieveAccountNumberByAccountId(LOAN_ID),
                 LoanNotFoundException);
}

// ===========================================================================
// retrieveNumberOfActiveLoans
// ===========================================================================

TEST_F(LoanReadPlatformServiceImplTest,
       retrieveNumberOfActiveLoans_shouldReturnCount) {
    ON_CALL(jdbcTemplate, queryForObjectInt(_)).WillByDefault(Return(42));

    Integer result = svc->retrieveNumberOfActiveLoans();

    EXPECT_EQ(42, result);
}

// ===========================================================================
// isGuaranteeRequired
// ===========================================================================

TEST_F(
    LoanReadPlatformServiceImplTest,
    isGuaranteeRequired_shouldReturnTrue_whenGuaranteeFlagIsTrue) {
    ON_CALL(jdbcTemplate, queryForObjectBool(_, LOAN_ID))
        .WillByDefault(Return(std::optional<bool>(true)));

    EXPECT_TRUE(svc->isGuaranteeRequired(LOAN_ID));
}

TEST_F(
    LoanReadPlatformServiceImplTest,
    isGuaranteeRequired_shouldReturnFalse_whenGuaranteeFlagIsFalse) {
    ON_CALL(jdbcTemplate, queryForObjectBool(_, LOAN_ID))
        .WillByDefault(Return(std::optional<bool>(false)));

    EXPECT_FALSE(svc->isGuaranteeRequired(LOAN_ID));
}

TEST_F(LoanReadPlatformServiceImplTest,
       isGuaranteeRequired_shouldReturnFalse_whenNull) {
    ON_CALL(jdbcTemplate, queryForObjectBool(_, LOAN_ID))
        .WillByDefault(Return(std::nullopt));

    EXPECT_FALSE(svc->isGuaranteeRequired(LOAN_ID));
}

// ===========================================================================
// existsByLoanId
// ===========================================================================

TEST_F(LoanReadPlatformServiceImplTest,
       existsByLoanId_shouldDelegateToRepository) {
    ON_CALL(loanRepositoryWrapper, existsByLoanId(LOAN_ID))
        .WillByDefault(Return(true));

    EXPECT_TRUE(svc->existsByLoanId(LOAN_ID));
}

TEST_F(LoanReadPlatformServiceImplTest,
       existsByLoanId_shouldReturnFalse_whenLoanDoesNotExist) {
    ON_CALL(loanRepositoryWrapper, existsByLoanId(999))
        .WillByDefault(Return(false));

    EXPECT_FALSE(svc->existsByLoanId(999));
}

// ===========================================================================
// retrieveLoanIdByExternalId
// ===========================================================================

TEST_F(LoanReadPlatformServiceImplTest,
       retrieveLoanIdByExternalId_shouldDelegateToRepository) {
    ExternalId ext("ext-456");
    ON_CALL(loanRepositoryWrapper, findIdByExternalId(_))
        .WillByDefault(Return(std::optional<Long>(LOAN_ID)));

    auto result = svc->retrieveLoanIdByExternalId(ext);

    ASSERT_TRUE(result.has_value());
    EXPECT_EQ(LOAN_ID, result.value());
}

// ===========================================================================
// retrieveLoanTransactionIdByExternalId
// ===========================================================================

TEST_F(
    LoanReadPlatformServiceImplTest,
    retrieveLoanTransactionIdByExternalId_shouldDelegateToRepository) {
    ExternalId ext("tx-ext-789");
    ON_CALL(loanTransactionRepository, findIdByExternalId(_))
        .WillByDefault(Return(std::optional<Long>(TRANSACTION_ID)));

    auto result = svc->retrieveLoanTransactionIdByExternalId(ext);

    ASSERT_TRUE(result.has_value());
    EXPECT_EQ(TRANSACTION_ID, result.value());
}

// ===========================================================================
// getResolvedLoanId
// ===========================================================================

TEST_F(LoanReadPlatformServiceImplTest,
       getResolvedLoanId_shouldReturnLoanId_whenFound) {
    ExternalId ext("resolve-ext-1");
    ON_CALL(loanRepositoryWrapper, findIdByExternalId(_))
        .WillByDefault(Return(std::optional<Long>(LOAN_ID)));

    Long result = svc->getResolvedLoanId(ext);

    EXPECT_EQ(LOAN_ID, result);
}

TEST_F(LoanReadPlatformServiceImplTest,
       getResolvedLoanId_shouldThrowLoanNotFoundException_whenNotFound) {
    ExternalId ext("resolve-not-found");
    ON_CALL(loanRepositoryWrapper, findIdByExternalId(_))
        .WillByDefault(Return(std::nullopt));

    EXPECT_THROW(svc->getResolvedLoanId(ext), LoanNotFoundException);
}

// ===========================================================================
// getResolvedLoanTransactionId
// ===========================================================================

TEST_F(
    LoanReadPlatformServiceImplTest,
    getResolvedLoanTransactionId_shouldReturnTransactionId_whenDirectIdProvided) {
    Long result = svc->getResolvedLoanTransactionId(
        TRANSACTION_ID, ExternalId::emptyId());

    EXPECT_EQ(TRANSACTION_ID, result);
}

TEST_F(
    LoanReadPlatformServiceImplTest,
    getResolvedLoanTransactionId_shouldResolveByExternalId_whenTransactionIdIsNull) {
    ExternalId ext("tx-ext-resolve");
    ON_CALL(loanTransactionRepository, findIdByExternalId(_))
        .WillByDefault(Return(std::optional<Long>(TRANSACTION_ID)));

    Long result =
        svc->getResolvedLoanTransactionId(std::nullopt, ext);

    EXPECT_EQ(TRANSACTION_ID, result);
}

TEST_F(
    LoanReadPlatformServiceImplTest,
    getResolvedLoanTransactionId_shouldThrowException_whenExternalIdNotFound) {
    ExternalId ext("tx-ext-not-found");
    ON_CALL(loanTransactionRepository, findIdByExternalId(_))
        .WillByDefault(Return(std::nullopt));

    EXPECT_THROW(
        svc->getResolvedLoanTransactionId(std::nullopt, ext),
        LoanTransactionNotFoundException);
}

// ===========================================================================
// retrieveCalendars
// ===========================================================================

TEST_F(LoanReadPlatformServiceImplTest,
       retrieveCalendars_shouldReturnCalendars) {
    std::vector<std::shared_ptr<CalendarData>> parentCals = {
        std::make_shared<MockCalendarData>()};
    std::vector<std::shared_ptr<CalendarData>> entityCals = {
        std::make_shared<MockCalendarData>()};

    std::vector<std::shared_ptr<CalendarData>> combined;
    combined.insert(combined.end(), parentCals.begin(), parentCals.end());
    combined.insert(combined.end(), entityCals.begin(), entityCals.end());

    ON_CALL(calendarReadPlatformService,
            retrieveParentCalendarsByEntity(GROUP_ID, _, _))
        .WillByDefault(Return(parentCals));
    ON_CALL(calendarReadPlatformService,
            retrieveCalendarsByEntity(GROUP_ID, _, _))
        .WillByDefault(Return(entityCals));
    ON_CALL(calendarReadPlatformService, updateWithRecurringDates(_))
        .WillByDefault(Return(combined));

    auto result = svc->retrieveCalendars(GROUP_ID);

    EXPECT_EQ(2u, result.size());
}

// ===========================================================================
// retrieveApprovalTemplate
// ===========================================================================

TEST_F(LoanReadPlatformServiceImplTest,
       retrieveApprovalTemplate_shouldReturnApprovalData) {
    auto loan = std::make_shared<MockLoan>();
    MockMonetaryCurrency currency;
    auto appCurrency = std::make_shared<MockApplicationCurrency>();

    ON_CALL(loanRepositoryWrapper,
            findOneWithNotFoundDetection(LOAN_ID, true))
        .WillByDefault(Return(loan));
    ON_CALL(*loan, getCurrency()).WillByDefault(Return(&currency));
    ON_CALL(applicationCurrencyRepository,
            findOneWithNotFoundDetection(&currency))
        .WillByDefault(Return(appCurrency));
    ON_CALL(*loan, getProposedPrincipal())
        .WillByDefault(Return(BigDecimal(10000)));
    ON_CALL(*loan, getNetDisbursalAmount())
        .WillByDefault(Return(BigDecimal(9500)));
    ON_CALL(delinquencyReadPlatformService,
            calculateAvailableDisbursementAmountWithOverApplied(loan.get()))
        .WillByDefault(Return(BigDecimal(10000)));
    ON_CALL(dateUtils, getBusinessLocalDate())
        .WillByDefault(Return(LocalDate(2026, 1, 15)));

    auto result = svc->retrieveApprovalTemplate(LOAN_ID);

    ASSERT_NE(result, nullptr);
}

// ===========================================================================
// retrieveRecoveryPaymentTemplate
// ===========================================================================

TEST_F(
    LoanReadPlatformServiceImplTest,
    retrieveRecoveryPaymentTemplate_shouldReturnTransactionData) {
    auto loan = std::make_shared<MockLoan>();
    ON_CALL(loanRepositoryWrapper,
            findOneWithNotFoundDetection(LOAN_ID, true))
        .WillByDefault(Return(loan));
    ON_CALL(*loan, getTotalWrittenOff())
        .WillByDefault(Return(BigDecimal(500)));
    ON_CALL(*loan, getNetDisbursalAmount())
        .WillByDefault(Return(BigDecimal(9500)));
    ON_CALL(*loan, getExternalId())
        .WillByDefault(Return(ExternalId::emptyId()));
    ON_CALL(paymentTypeReadPlatformService, retrieveAllPaymentTypes())
        .WillByDefault(Return(std::vector<int>{}));

    auto result = svc->retrieveRecoveryPaymentTemplate(LOAN_ID);

    ASSERT_NE(result, nullptr);
}

// ===========================================================================
// retrieveTotalPaidInAdvance
// ===========================================================================

TEST_F(
    LoanReadPlatformServiceImplTest,
    retrieveTotalPaidInAdvance_shouldReturnPaidInAdvanceData_whenFound) {
    ON_CALL(sqlGenerator, currentBusinessDate())
        .WillByDefault(Return("CURRENT_DATE"));
    ON_CALL(jdbcTemplate, queryForObjectBigDecimal(_, LOAN_ID))
        .WillByDefault(Return(BigDecimal(200)));

    auto result = svc->retrieveTotalPaidInAdvance(LOAN_ID);

    EXPECT_EQ(BigDecimal(200), result.getPaidInAdvance());
}

TEST_F(LoanReadPlatformServiceImplTest,
       retrieveTotalPaidInAdvance_shouldReturnZero_whenNoData) {
    ON_CALL(sqlGenerator, currentBusinessDate())
        .WillByDefault(Return("CURRENT_DATE"));
    ON_CALL(jdbcTemplate, queryForObjectBigDecimal(_, LOAN_ID))
        .WillByDefault(
            testing::Throw(EmptyResultDataAccessException(1)));

    auto result = svc->retrieveTotalPaidInAdvance(LOAN_ID);

    EXPECT_EQ(BigDecimal(0), result.getPaidInAdvance());
}

// ===========================================================================
// retrieveLoanWriteoffTemplate
// ===========================================================================

TEST_F(
    LoanReadPlatformServiceImplTest,
    retrieveLoanWriteoffTemplate_shouldReturnTransactionDataWithWriteoffReasons) {
    auto loanAccountData = std::make_shared<MockLoanAccountData>();
    ON_CALL(context, getAuthenticatedUserIfPresent())
        .WillByDefault(Return(appUser));
    ON_CALL(sqlGenerator, escape(_)).WillByDefault(Return("\"name\""));
    ON_CALL(jdbcTemplate, queryForObjectLoanAccount(_, _, _, _))
        .WillByDefault(Return(loanAccountData));

    MockLoanSummaryData summary;
    ON_CALL(*loanAccountData, getSummary())
        .WillByDefault(Return(&summary));
    ON_CALL(summary, getTotalOutstanding())
        .WillByDefault(Return(BigDecimal(1000)));
    ON_CALL(*loanAccountData, getNetDisbursalAmount())
        .WillByDefault(Return(BigDecimal(9500)));
    ON_CALL(*loanAccountData, getExternalId())
        .WillByDefault(Return(ExternalId::emptyId()));

    std::vector<std::shared_ptr<CodeValueData>> writeoffReasons = {
        std::make_shared<MockCodeValueData>()};
    ON_CALL(codeValueReadPlatformService, retrieveCodeValuesByCode(_))
        .WillByDefault(Return(writeoffReasons));
    ON_CALL(dateUtils, getBusinessLocalDate())
        .WillByDefault(Return(LocalDate(2026, 1, 15)));

    auto result = svc->retrieveLoanWriteoffTemplate(LOAN_ID);

    ASSERT_NE(result, nullptr);
}

// ===========================================================================
// countInstallmentsByLoanIdWhereIsAdditionalFalseAndIsDownPaymentFalse
// ===========================================================================

TEST_F(LoanReadPlatformServiceImplTest,
       countInstallments_shouldDelegateToRepaymentScheduleService) {
    ON_CALL(
        loanRepaymentScheduleService,
        countInstallmentsByLoanIdWhereIsAdditionalFalseAndIsDownPaymentFalse(
            LOAN_ID))
        .WillByDefault(Return(10));

    Integer result =
        svc->countInstallmentsByLoanIdWhereIsAdditionalFalseAndIsDownPaymentFalse(
            LOAN_ID);

    EXPECT_EQ(10, result);
}

// ===========================================================================
// retrieveLoanChargeOffTemplate
// ===========================================================================

TEST_F(
    LoanReadPlatformServiceImplTest,
    retrieveLoanChargeOffTemplate_shouldReturnTransactionDataWithChargeOffReasons) {
    auto loanAccountData = std::make_shared<MockLoanAccountData>();
    ON_CALL(context, getAuthenticatedUserIfPresent())
        .WillByDefault(Return(appUser));
    ON_CALL(sqlGenerator, escape(_)).WillByDefault(Return("\"name\""));
    ON_CALL(jdbcTemplate, queryForObjectLoanAccount(_, _, _, _))
        .WillByDefault(Return(loanAccountData));

    MockLoanSummaryData summary;
    ON_CALL(*loanAccountData, getSummary())
        .WillByDefault(Return(&summary));
    ON_CALL(summary, getTotalOutstanding())
        .WillByDefault(Return(BigDecimal(1000)));
    ON_CALL(summary, getPrincipalOutstanding())
        .WillByDefault(Return(BigDecimal(800)));
    ON_CALL(summary, getInterestOutstanding())
        .WillByDefault(Return(BigDecimal(100)));
    ON_CALL(summary, getFeeChargesOutstanding())
        .WillByDefault(Return(BigDecimal(50)));
    ON_CALL(summary, getPenaltyChargesOutstanding())
        .WillByDefault(Return(BigDecimal(50)));
    ON_CALL(*loanAccountData, getNetDisbursalAmount())
        .WillByDefault(Return(BigDecimal(9500)));
    ON_CALL(*loanAccountData, getExternalId())
        .WillByDefault(Return(ExternalId::emptyId()));

    std::vector<std::shared_ptr<CodeValueData>> chargeOffReasons = {
        std::make_shared<MockCodeValueData>()};
    ON_CALL(codeValueReadPlatformService, retrieveCodeValuesByCode(_))
        .WillByDefault(Return(chargeOffReasons));
    ON_CALL(dateUtils, getBusinessLocalDate())
        .WillByDefault(Return(LocalDate(2026, 1, 15)));

    auto result = svc->retrieveLoanChargeOffTemplate(LOAN_ID);

    ASSERT_NE(result, nullptr);
}

// ===========================================================================
// retrieveDisbursalTemplate
// ===========================================================================

TEST_F(
    LoanReadPlatformServiceImplTest,
    retrieveDisbursalTemplate_withPaymentDetails_shouldReturnTransactionData) {
    auto loan = std::make_shared<MockLoan>();
    MockMonetaryCurrency currency;
    auto appCurrency = std::make_shared<MockApplicationCurrency>();

    ON_CALL(loanRepositoryWrapper,
            findOneWithNotFoundDetection(LOAN_ID, true))
        .WillByDefault(Return(loan));
    ON_CALL(*loan, getCurrency()).WillByDefault(Return(&currency));
    ON_CALL(applicationCurrencyRepository,
            findOneWithNotFoundDetection(&currency))
        .WillByDefault(Return(appCurrency));
    ON_CALL(*loan, getExpectedDisbursedOnLocalDateForTemplate())
        .WillByDefault(Return(LocalDate(2026, 2, 1)));
    ON_CALL(*loan, getDisburseAmountForTemplate())
        .WillByDefault(Return(BigDecimal(10000)));
    ON_CALL(*loan, getNetDisbursalAmount())
        .WillByDefault(Return(BigDecimal(9500)));
    ON_CALL(*loan, retriveLastEmiAmount())
        .WillByDefault(Return(BigDecimal(500)));
    ON_CALL(*loan, getNextPossibleRepaymentDateForRescheduling())
        .WillByDefault(Return(LocalDate(2026, 3, 1)));
    ON_CALL(paymentTypeReadPlatformService, retrieveAllPaymentTypes())
        .WillByDefault(Return(std::vector<int>{}));
    ON_CALL(delinquencyReadPlatformService,
            calculateAvailableDisbursementAmountWithOverApplied(loan.get()))
        .WillByDefault(Return(BigDecimal(10000)));

    auto result = svc->retrieveDisbursalTemplate(LOAN_ID, true);

    ASSERT_NE(result, nullptr);
}

TEST_F(
    LoanReadPlatformServiceImplTest,
    retrieveDisbursalTemplate_withoutPaymentDetails_shouldReturnTransactionDataWithoutPaymentOptions) {
    auto loan = std::make_shared<MockLoan>();
    MockMonetaryCurrency currency;
    auto appCurrency = std::make_shared<MockApplicationCurrency>();

    ON_CALL(loanRepositoryWrapper,
            findOneWithNotFoundDetection(LOAN_ID, true))
        .WillByDefault(Return(loan));
    ON_CALL(*loan, getCurrency()).WillByDefault(Return(&currency));
    ON_CALL(applicationCurrencyRepository,
            findOneWithNotFoundDetection(&currency))
        .WillByDefault(Return(appCurrency));
    ON_CALL(*loan, getExpectedDisbursedOnLocalDateForTemplate())
        .WillByDefault(Return(LocalDate(2026, 2, 1)));
    ON_CALL(*loan, getDisburseAmountForTemplate())
        .WillByDefault(Return(BigDecimal(10000)));
    ON_CALL(*loan, getNetDisbursalAmount())
        .WillByDefault(Return(BigDecimal(9500)));
    ON_CALL(*loan, retriveLastEmiAmount())
        .WillByDefault(Return(BigDecimal(500)));
    ON_CALL(*loan, getNextPossibleRepaymentDateForRescheduling())
        .WillByDefault(Return(LocalDate(2026, 3, 1)));
    ON_CALL(delinquencyReadPlatformService,
            calculateAvailableDisbursementAmountWithOverApplied(loan.get()))
        .WillByDefault(Return(BigDecimal(10000)));

    auto result = svc->retrieveDisbursalTemplate(LOAN_ID, false);

    ASSERT_NE(result, nullptr);
}

// ===========================================================================
// retrieveLoanDisbursementDetails
// ===========================================================================

TEST_F(
    LoanReadPlatformServiceImplTest,
    retrieveLoanDisbursementDetails_shouldReturnEmptyList_whenNoDisbursements) {
    ON_CALL(sqlGenerator, inParametersFor(_))
        .WillByDefault(Return(std::vector<Long>{LOAN_ID}));
    ON_CALL(sqlGenerator, escape(_)).WillByDefault(Return("\"name\""));
    ON_CALL(sqlGenerator, inSql(_, _))
        .WillByDefault(Return("dd.loan_id IN (?)"));
    ON_CALL(sqlGenerator, groupConcat(_))
        .WillByDefault(Return("GROUP_CONCAT(lc.id)"));
    ON_CALL(jdbcTemplate, queryDisbursements(_, _))
        .WillByDefault(
            Return(std::vector<std::shared_ptr<DisbursementData>>{}));

    auto result = svc->retrieveLoanDisbursementDetails(LOAN_ID);

    EXPECT_EQ(0u, result.size());
}

// ===========================================================================
// retrieveLoanIdsWithPendingIncomePostingTransactions
// ===========================================================================

TEST_F(
    LoanReadPlatformServiceImplTest,
    retrieveLoanIdsWithPendingIncomePostingTransactions_shouldReturnLoanIds) {
    std::vector<Long> expectedIds = {1, 2, 3};
    ON_CALL(dateUtils, getBusinessLocalDate())
        .WillByDefault(Return(LocalDate(2026, 1, 15)));
    ON_CALL(jdbcTemplate, queryForListLong(_, _))
        .WillByDefault(Return(expectedIds));

    auto result =
        svc->retrieveLoanIdsWithPendingIncomePostingTransactions();

    ASSERT_TRUE(result.has_value());
    EXPECT_EQ(3u, result->size());
}

TEST_F(
    LoanReadPlatformServiceImplTest,
    retrieveLoanIdsWithPendingIncomePostingTransactions_shouldReturnNull_whenNoResults) {
    ON_CALL(dateUtils, getBusinessLocalDate())
        .WillByDefault(Return(LocalDate(2026, 1, 15)));
    ON_CALL(jdbcTemplate, queryForListLong(_, _))
        .WillByDefault(
            testing::Throw(EmptyResultDataAccessException(1)));

    auto result =
        svc->retrieveLoanIdsWithPendingIncomePostingTransactions();

    EXPECT_FALSE(result.has_value());
}

// ===========================================================================
// retrieveLoanIdsByExternalIds
// ===========================================================================

TEST_F(LoanReadPlatformServiceImplTest,
       retrieveLoanIdsByExternalIds_shouldDelegateToRepository) {
    std::vector<ExternalId> externalIds = {ExternalId("ext-1"),
                                           ExternalId("ext-2")};
    std::vector<Long> expectedIds = {1, 2};
    ON_CALL(loanRepositoryWrapper, findIdByExternalIds(_))
        .WillByDefault(Return(expectedIds));

    auto result = svc->retrieveLoanIdsByExternalIds(externalIds);

    EXPECT_EQ(expectedIds, result);
}
