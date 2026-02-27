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

#include "Types.h"

#include <functional>
#include <optional>
#include <string>
#include <vector>

namespace fineract {

// ---------------------------------------------------------------------------
// Service / repository interfaces (mirrors of Java @Mock dependencies)
// ---------------------------------------------------------------------------

/** Mirrors org.springframework.jdbc.core.JdbcTemplate (simplified). */
class JdbcTemplate {
public:
    virtual ~JdbcTemplate() = default;

    // queryForObject overloads
    virtual std::shared_ptr<LoanAccountData> queryForObjectLoanAccount(
        const std::string& sql, const std::string& hierarchy,
        const std::string& escape1, const std::string& escape2) = 0;

    virtual Long queryForObjectLong(const std::string& sql,
                                    const std::string& param) = 0;
    virtual std::string queryForObjectString(const std::string& sql,
                                             Long param) = 0;
    virtual Integer queryForObjectInt(const std::string& sql) = 0;
    virtual std::optional<bool> queryForObjectBool(const std::string& sql,
                                                   Long param) = 0;
    virtual BigDecimal queryForObjectBigDecimal(const std::string& sql,
                                                Long param) = 0;

    // queryForList
    virtual std::vector<Long> queryForListLong(const std::string& sql,
                                               const LocalDate& date) = 0;

    // query (returns rows)
    virtual std::vector<std::shared_ptr<DisbursementData>> queryDisbursements(
        const std::string& sql, Long loanId) = 0;
};

class PlatformSecurityContext {
public:
    virtual ~PlatformSecurityContext() = default;
    virtual std::shared_ptr<AppUser> authenticatedUser() = 0;
    virtual std::shared_ptr<AppUser> getAuthenticatedUserIfPresent() = 0;
};

class LoanRepositoryWrapper {
public:
    virtual ~LoanRepositoryWrapper() = default;
    virtual Integer getNumberOfRepayments(Long loanId) = 0;
    virtual std::optional<Long> findIdByExternalId(const ExternalId& ext) = 0;
    virtual bool existsByLoanId(Long loanId) = 0;
    virtual std::shared_ptr<Loan> findOneWithNotFoundDetection(Long loanId,
                                                               bool flag) = 0;
    virtual std::vector<Long> findIdByExternalIds(
        const std::vector<ExternalId>& ids) = 0;
};

class ApplicationCurrencyRepositoryWrapper {
public:
    virtual ~ApplicationCurrencyRepositoryWrapper() = default;
    virtual std::shared_ptr<ApplicationCurrency> findOneWithNotFoundDetection(
        MonetaryCurrency* currency) = 0;
};

class LoanProductReadPlatformService {
public:
    virtual ~LoanProductReadPlatformService() = default;
    virtual std::shared_ptr<LoanProductData> retrieveLoanProduct(
        Long productId) = 0;
};

class ClientReadPlatformService {
public:
    virtual ~ClientReadPlatformService() = default;
    virtual std::shared_ptr<ClientData> retrieveOne(Long clientId) = 0;
    virtual std::vector<std::shared_ptr<ClientData>>
    retrieveClientMembersOfGroup(Long groupId) = 0;
};

class GroupReadPlatformService {
public:
    virtual ~GroupReadPlatformService() = default;
    virtual std::shared_ptr<GroupGeneralData> retrieveOne(Long groupId) = 0;
};

class LoanDropdownReadPlatformService {
public:
    virtual ~LoanDropdownReadPlatformService() = default;
};

class FundReadPlatformService {
public:
    virtual ~FundReadPlatformService() = default;
};

class ChargeReadPlatformService {
public:
    virtual ~ChargeReadPlatformService() = default;
};

class CodeValueReadPlatformService {
public:
    virtual ~CodeValueReadPlatformService() = default;
    virtual std::vector<std::shared_ptr<CodeValueData>>
    retrieveCodeValuesByCode(const std::string& code) = 0;
};

class CalendarReadPlatformService {
public:
    virtual ~CalendarReadPlatformService() = default;
    virtual std::vector<std::shared_ptr<CalendarData>>
    retrieveParentCalendarsByEntity(Long entityId, const std::string& a,
                                    const std::string& b) = 0;
    virtual std::vector<std::shared_ptr<CalendarData>>
    retrieveCalendarsByEntity(Long entityId, const std::string& a,
                              const std::string& b) = 0;
    virtual std::vector<std::shared_ptr<CalendarData>>
    updateWithRecurringDates(
        const std::vector<std::shared_ptr<CalendarData>>& cals) = 0;
};

class StaffReadPlatformService {
public:
    virtual ~StaffReadPlatformService() = default;
    virtual std::vector<std::shared_ptr<StaffData>>
    retrieveAllLoanOfficersInOfficeById(Long officeId) = 0;
    virtual std::vector<std::shared_ptr<StaffData>>
    retrieveAllStaffInOfficeAndItsParentOfficeHierarchy(Long officeId,
                                                        bool loanOfficersOnly) = 0;
};

class PaginationHelper {
public:
    virtual ~PaginationHelper() = default;
};

class PaymentTypeReadService {
public:
    virtual ~PaymentTypeReadService() = default;
    virtual std::vector<int> retrieveAllPaymentTypes() = 0;
};

class FloatingRatesReadPlatformService {
public:
    virtual ~FloatingRatesReadPlatformService() = default;
};

class LoanUtilService {
public:
    virtual ~LoanUtilService() = default;
};

class ConfigurationDomainService {
public:
    virtual ~ConfigurationDomainService() = default;
};

class AccountDetailsReadPlatformService {
public:
    virtual ~AccountDetailsReadPlatformService() = default;
};

class ColumnValidator {
public:
    virtual ~ColumnValidator() = default;
};

class DatabaseSpecificSQLGenerator {
public:
    virtual ~DatabaseSpecificSQLGenerator() = default;
    virtual std::string escape(const std::string& col) = 0;
    virtual std::string currentBusinessDate() = 0;
    virtual std::string inSql(const std::string& col,
                              const std::vector<Long>& ids) = 0;
    virtual std::string groupConcat(const std::string& col) = 0;
    virtual std::vector<Long> inParametersFor(
        const std::vector<Long>& ids) = 0;
};

class DelinquencyReadPlatformService {
public:
    virtual ~DelinquencyReadPlatformService() = default;
    virtual BigDecimal calculateAvailableDisbursementAmountWithOverApplied(
        Loan* loan) = 0;
};

class LoanTransactionRepository {
public:
    virtual ~LoanTransactionRepository() = default;
    virtual std::optional<Long> findIdByExternalId(
        const ExternalId& ext) = 0;
};

class LoanChargePaidByReadService {
public:
    virtual ~LoanChargePaidByReadService() = default;
};

class LoanTransactionRelationReadService {
public:
    virtual ~LoanTransactionRelationReadService() = default;
};

class LoanForeclosureValidator {
public:
    virtual ~LoanForeclosureValidator() = default;
};

class LoanTransactionMapper {
public:
    virtual ~LoanTransactionMapper() = default;
};

class LoanTransactionProcessingService {
public:
    virtual ~LoanTransactionProcessingService() = default;
};

class LoanBalanceService {
public:
    virtual ~LoanBalanceService() = default;
};

class LoanCapitalizedIncomeBalanceRepository {
public:
    virtual ~LoanCapitalizedIncomeBalanceRepository() = default;
};

class LoanBuyDownFeeBalanceRepository {
public:
    virtual ~LoanBuyDownFeeBalanceRepository() = default;
};

class InterestRefundServiceDelegate {
public:
    virtual ~InterestRefundServiceDelegate() = default;
};

class LoanMaximumAmountCalculator {
public:
    virtual ~LoanMaximumAmountCalculator() = default;
};

class LoanRepaymentScheduleService {
public:
    virtual ~LoanRepaymentScheduleService() = default;
    virtual Integer
    countInstallmentsByLoanIdWhereIsAdditionalFalseAndIsDownPaymentFalse(
        Long loanId) = 0;
};

// ---------------------------------------------------------------------------
// DateUtils  -- global business-date helper (replaces the static mock)
// ---------------------------------------------------------------------------

class DateUtils {
public:
    virtual ~DateUtils() = default;
    virtual LocalDate getBusinessLocalDate() = 0;
};

// ---------------------------------------------------------------------------
// The service under test
// ---------------------------------------------------------------------------

/**
 * Abstract interface that mirrors the public API of
 * LoanReadPlatformServiceImpl used in the Java test. The concrete
 * implementation would delegate to the injected dependencies.
 */
class LoanReadPlatformService {
public:
    virtual ~LoanReadPlatformService() = default;

    virtual std::shared_ptr<LoanAccountData> retrieveOne(Long loanId) = 0;

    virtual std::shared_ptr<LoanAccountData>
    retrieveTemplateWithClientAndProductDetails(
        Long clientId, std::optional<Long> productId) = 0;

    virtual std::shared_ptr<LoanAccountData>
    retrieveTemplateWithGroupAndProductDetails(
        Long groupId, std::optional<Long> productId) = 0;

    virtual std::shared_ptr<LoanAccountData>
    retrieveTemplateWithCompleteGroupAndProductDetails(
        Long groupId, std::optional<Long> productId) = 0;

    virtual std::shared_ptr<LoanTransactionData>
    retrieveNewClosureDetails() = 0;

    virtual Integer retrieveNumberOfRepayments(Long loanId) = 0;

    virtual std::optional<std::vector<std::shared_ptr<StaffData>>>
    retrieveAllowedLoanOfficers(std::optional<Long> officeId,
                                bool staffInSelectedOfficeOnly) = 0;

    virtual Long getLoanIdByLoanExternalId(const std::string& extId) = 0;
    virtual std::optional<Long> retrieveLoanIdByAccountNumber(
        const std::string& accountNumber) = 0;
    virtual std::string retrieveAccountNumberByAccountId(Long accountId) = 0;
    virtual Integer retrieveNumberOfActiveLoans() = 0;
    virtual bool isGuaranteeRequired(Long loanId) = 0;
    virtual bool existsByLoanId(Long loanId) = 0;

    virtual std::optional<Long> retrieveLoanIdByExternalId(
        const ExternalId& ext) = 0;
    virtual std::optional<Long> retrieveLoanTransactionIdByExternalId(
        const ExternalId& ext) = 0;

    virtual Long getResolvedLoanId(const ExternalId& ext) = 0;
    virtual Long getResolvedLoanTransactionId(
        std::optional<Long> txnId, const ExternalId& ext) = 0;

    virtual std::vector<std::shared_ptr<CalendarData>> retrieveCalendars(
        Long groupId) = 0;

    virtual std::shared_ptr<LoanAccountData> retrieveApprovalTemplate(
        Long loanId) = 0;
    virtual std::shared_ptr<LoanTransactionData>
    retrieveRecoveryPaymentTemplate(Long loanId) = 0;

    virtual PaidInAdvanceData retrieveTotalPaidInAdvance(Long loanId) = 0;

    virtual std::shared_ptr<LoanTransactionData>
    retrieveLoanWriteoffTemplate(Long loanId) = 0;

    virtual Integer
    countInstallmentsByLoanIdWhereIsAdditionalFalseAndIsDownPaymentFalse(
        Long loanId) = 0;

    virtual std::shared_ptr<LoanTransactionData>
    retrieveLoanChargeOffTemplate(Long loanId) = 0;

    virtual std::shared_ptr<LoanTransactionData> retrieveDisbursalTemplate(
        Long loanId, bool paymentDetails) = 0;

    virtual std::vector<std::shared_ptr<DisbursementData>>
    retrieveLoanDisbursementDetails(Long loanId) = 0;

    virtual std::optional<std::vector<Long>>
    retrieveLoanIdsWithPendingIncomePostingTransactions() = 0;

    virtual std::vector<Long> retrieveLoanIdsByExternalIds(
        const std::vector<ExternalId>& ids) = 0;
};

} // namespace fineract
