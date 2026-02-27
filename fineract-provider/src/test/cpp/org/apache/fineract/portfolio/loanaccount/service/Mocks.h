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
#pragma once

#include "Interfaces.h"
#include "Types.h"

#include <gmock/gmock.h>

namespace fineract {

// ---------------------------------------------------------------------------
// Domain-object mocks
// ---------------------------------------------------------------------------

class MockOffice : public Office {
public:
    MOCK_METHOD(std::string, getHierarchy, (), (const, override));
};

class MockAppUser : public AppUser {
public:
    MOCK_METHOD(Office*, getOffice, (), (const, override));
};

class MockLoan : public Loan {
public:
    MOCK_METHOD(MonetaryCurrency*, getCurrency, (), (const, override));
    MOCK_METHOD(BigDecimal, getProposedPrincipal, (), (const, override));
    MOCK_METHOD(BigDecimal, getNetDisbursalAmount, (), (const, override));
    MOCK_METHOD(BigDecimal, getDisburseAmountForTemplate, (), (const, override));
    MOCK_METHOD(BigDecimal, getTotalWrittenOff, (), (const, override));
    MOCK_METHOD(BigDecimal, retriveLastEmiAmount, (), (const, override));
    MOCK_METHOD(LocalDate, getExpectedDisbursedOnLocalDateForTemplate, (),
                (const, override));
    MOCK_METHOD(LocalDate, getNextPossibleRepaymentDateForRescheduling, (),
                (const, override));
    MOCK_METHOD(ExternalId, getExternalId, (), (const, override));
};

class MockLoanAccountData : public LoanAccountData {
public:
    MOCK_METHOD(LoanSummaryData*, getSummary, (), (const, override));
    MOCK_METHOD(BigDecimal, getNetDisbursalAmount, (), (const, override));
    MOCK_METHOD(ExternalId, getExternalId, (), (const, override));
};

class MockLoanSummaryData : public LoanSummaryData {
public:
    MOCK_METHOD(BigDecimal, getTotalOutstanding, (), (const, override));
    MOCK_METHOD(BigDecimal, getPrincipalOutstanding, (), (const, override));
    MOCK_METHOD(BigDecimal, getInterestOutstanding, (), (const, override));
    MOCK_METHOD(BigDecimal, getFeeChargesOutstanding, (), (const, override));
    MOCK_METHOD(BigDecimal, getPenaltyChargesOutstanding, (), (const, override));
    MOCK_METHOD(BigDecimal, getPaidInAdvance, (), (const, override));
};

class MockLoanTransactionData : public LoanTransactionData {
public:
    MOCK_METHOD(const LoanTransactionType*, getType, (), (const, override));
};

class MockMonetaryCurrency : public MonetaryCurrency {};
class MockApplicationCurrency : public ApplicationCurrency {};
class MockClientData : public ClientData {};
class MockGroupGeneralData : public GroupGeneralData {};
class MockLoanProductData : public LoanProductData {};
class MockStaffData : public StaffData {};
class MockCalendarData : public CalendarData {};
class MockCodeValueData : public CodeValueData {};
class MockDisbursementData : public DisbursementData {};

// ---------------------------------------------------------------------------
// Service / repository mocks
// ---------------------------------------------------------------------------

class MockJdbcTemplate : public JdbcTemplate {
public:
    MOCK_METHOD(std::shared_ptr<LoanAccountData>, queryForObjectLoanAccount,
                (const std::string&, const std::string&, const std::string&,
                 const std::string&),
                (override));
    MOCK_METHOD(Long, queryForObjectLong,
                (const std::string&, const std::string&), (override));
    MOCK_METHOD(std::string, queryForObjectString,
                (const std::string&, Long), (override));
    MOCK_METHOD(Integer, queryForObjectInt, (const std::string&), (override));
    MOCK_METHOD(std::optional<bool>, queryForObjectBool,
                (const std::string&, Long), (override));
    MOCK_METHOD(BigDecimal, queryForObjectBigDecimal,
                (const std::string&, Long), (override));
    MOCK_METHOD(std::vector<Long>, queryForListLong,
                (const std::string&, const LocalDate&), (override));
    MOCK_METHOD(std::vector<std::shared_ptr<DisbursementData>>,
                queryDisbursements, (const std::string&, Long), (override));
};

class MockPlatformSecurityContext : public PlatformSecurityContext {
public:
    MOCK_METHOD(std::shared_ptr<AppUser>, authenticatedUser, (), (override));
    MOCK_METHOD(std::shared_ptr<AppUser>, getAuthenticatedUserIfPresent, (),
                (override));
};

class MockLoanRepositoryWrapper : public LoanRepositoryWrapper {
public:
    MOCK_METHOD(Integer, getNumberOfRepayments, (Long), (override));
    MOCK_METHOD(std::optional<Long>, findIdByExternalId,
                (const ExternalId&), (override));
    MOCK_METHOD(bool, existsByLoanId, (Long), (override));
    MOCK_METHOD(std::shared_ptr<Loan>, findOneWithNotFoundDetection,
                (Long, bool), (override));
    MOCK_METHOD(std::vector<Long>, findIdByExternalIds,
                (const std::vector<ExternalId>&), (override));
};

class MockApplicationCurrencyRepositoryWrapper
    : public ApplicationCurrencyRepositoryWrapper {
public:
    MOCK_METHOD(std::shared_ptr<ApplicationCurrency>,
                findOneWithNotFoundDetection, (MonetaryCurrency*), (override));
};

class MockLoanProductReadPlatformService
    : public LoanProductReadPlatformService {
public:
    MOCK_METHOD(std::shared_ptr<LoanProductData>, retrieveLoanProduct, (Long),
                (override));
};

class MockClientReadPlatformService : public ClientReadPlatformService {
public:
    MOCK_METHOD(std::shared_ptr<ClientData>, retrieveOne, (Long), (override));
    MOCK_METHOD(std::vector<std::shared_ptr<ClientData>>,
                retrieveClientMembersOfGroup, (Long), (override));
};

class MockGroupReadPlatformService : public GroupReadPlatformService {
public:
    MOCK_METHOD(std::shared_ptr<GroupGeneralData>, retrieveOne, (Long),
                (override));
};

class MockCodeValueReadPlatformService : public CodeValueReadPlatformService {
public:
    MOCK_METHOD(std::vector<std::shared_ptr<CodeValueData>>,
                retrieveCodeValuesByCode, (const std::string&), (override));
};

class MockCalendarReadPlatformService : public CalendarReadPlatformService {
public:
    MOCK_METHOD(std::vector<std::shared_ptr<CalendarData>>,
                retrieveParentCalendarsByEntity,
                (Long, const std::string&, const std::string&), (override));
    MOCK_METHOD(std::vector<std::shared_ptr<CalendarData>>,
                retrieveCalendarsByEntity,
                (Long, const std::string&, const std::string&), (override));
    MOCK_METHOD(std::vector<std::shared_ptr<CalendarData>>,
                updateWithRecurringDates,
                (const std::vector<std::shared_ptr<CalendarData>>&),
                (override));
};

class MockStaffReadPlatformService : public StaffReadPlatformService {
public:
    MOCK_METHOD(std::vector<std::shared_ptr<StaffData>>,
                retrieveAllLoanOfficersInOfficeById, (Long), (override));
    MOCK_METHOD(std::vector<std::shared_ptr<StaffData>>,
                retrieveAllStaffInOfficeAndItsParentOfficeHierarchy,
                (Long, bool), (override));
};

class MockPaymentTypeReadService : public PaymentTypeReadService {
public:
    MOCK_METHOD(std::vector<int>, retrieveAllPaymentTypes, (), (override));
};

class MockDatabaseSpecificSQLGenerator : public DatabaseSpecificSQLGenerator {
public:
    MOCK_METHOD(std::string, escape, (const std::string&), (override));
    MOCK_METHOD(std::string, currentBusinessDate, (), (override));
    MOCK_METHOD(std::string, inSql,
                (const std::string&, const std::vector<Long>&), (override));
    MOCK_METHOD(std::string, groupConcat, (const std::string&), (override));
    MOCK_METHOD(std::vector<Long>, inParametersFor,
                (const std::vector<Long>&), (override));
};

class MockDelinquencyReadPlatformService
    : public DelinquencyReadPlatformService {
public:
    MOCK_METHOD(BigDecimal,
                calculateAvailableDisbursementAmountWithOverApplied, (Loan*),
                (override));
};

class MockLoanTransactionRepository : public LoanTransactionRepository {
public:
    MOCK_METHOD(std::optional<Long>, findIdByExternalId,
                (const ExternalId&), (override));
};

class MockLoanRepaymentScheduleService : public LoanRepaymentScheduleService {
public:
    MOCK_METHOD(
        Integer,
        countInstallmentsByLoanIdWhereIsAdditionalFalseAndIsDownPaymentFalse,
        (Long), (override));
};

class MockDateUtils : public DateUtils {
public:
    MOCK_METHOD(LocalDate, getBusinessLocalDate, (), (override));
};

// Unused-but-present mocks (matching Java @Mock fields)
class MockLoanDropdownReadPlatformService
    : public LoanDropdownReadPlatformService {};
class MockFundReadPlatformService : public FundReadPlatformService {};
class MockChargeReadPlatformService : public ChargeReadPlatformService {};
class MockPaginationHelper : public PaginationHelper {};
class MockFloatingRatesReadPlatformService
    : public FloatingRatesReadPlatformService {};
class MockLoanUtilService : public LoanUtilService {};
class MockConfigurationDomainService : public ConfigurationDomainService {};
class MockAccountDetailsReadPlatformService
    : public AccountDetailsReadPlatformService {};
class MockColumnValidator : public ColumnValidator {};
class MockLoanChargePaidByReadService : public LoanChargePaidByReadService {};
class MockLoanTransactionRelationReadService
    : public LoanTransactionRelationReadService {};
class MockLoanForeclosureValidator : public LoanForeclosureValidator {};
class MockLoanTransactionMapper : public LoanTransactionMapper {};
class MockLoanTransactionProcessingService
    : public LoanTransactionProcessingService {};
class MockLoanBalanceService : public LoanBalanceService {};
class MockLoanCapitalizedIncomeBalanceRepository
    : public LoanCapitalizedIncomeBalanceRepository {};
class MockLoanBuyDownFeeBalanceRepository
    : public LoanBuyDownFeeBalanceRepository {};
class MockInterestRefundServiceDelegate
    : public InterestRefundServiceDelegate {};
class MockLoanMaximumAmountCalculator : public LoanMaximumAmountCalculator {};

} // namespace fineract
