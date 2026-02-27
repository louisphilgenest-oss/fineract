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

#include <memory>
#include <optional>
#include <string>
#include <vector>

namespace fineract {

/**
 * Minimal concrete implementation of LoanReadPlatformService that delegates
 * to injected dependencies -- mirrors the Java class under test.
 *
 * Only the methods exercised by the unit tests are implemented here.
 * A production port would flesh out the SQL queries; this file exists
 * solely so the C++ tests can compile and run.
 */
class LoanReadPlatformServiceImpl : public LoanReadPlatformService {
public:
    LoanReadPlatformServiceImpl(
        JdbcTemplate& jdbc, PlatformSecurityContext& ctx,
        LoanRepositoryWrapper& loanRepo,
        ApplicationCurrencyRepositoryWrapper& currencyRepo,
        LoanProductReadPlatformService& productSvc,
        ClientReadPlatformService& clientSvc,
        GroupReadPlatformService& groupSvc,
        LoanDropdownReadPlatformService& /*dropdownSvc*/,
        FundReadPlatformService& /*fundSvc*/,
        ChargeReadPlatformService& /*chargeSvc*/,
        CodeValueReadPlatformService& codeValueSvc,
        CalendarReadPlatformService& calendarSvc,
        StaffReadPlatformService& staffSvc, PaginationHelper& /*pageHelper*/,
        PaymentTypeReadService& paymentTypeSvc,
        FloatingRatesReadPlatformService& /*floatingSvc*/,
        LoanUtilService& /*loanUtilSvc*/,
        ConfigurationDomainService& /*configSvc*/,
        AccountDetailsReadPlatformService& /*acctDetailsSvc*/,
        ColumnValidator& /*colValidator*/,
        DatabaseSpecificSQLGenerator& sqlGen,
        DelinquencyReadPlatformService& delinquencySvc,
        LoanTransactionRepository& txnRepo,
        LoanChargePaidByReadService& /*chargePaidBySvc*/,
        LoanTransactionRelationReadService& /*txnRelSvc*/,
        LoanForeclosureValidator& /*foreclosureValidator*/,
        LoanTransactionMapper& /*txnMapper*/,
        LoanTransactionProcessingService& /*txnProcessingSvc*/,
        LoanBalanceService& /*balanceSvc*/,
        LoanCapitalizedIncomeBalanceRepository& /*capIncomeRepo*/,
        LoanBuyDownFeeBalanceRepository& /*buyDownRepo*/,
        InterestRefundServiceDelegate& /*interestRefundSvc*/,
        LoanMaximumAmountCalculator& /*maxAmtCalc*/,
        LoanRepaymentScheduleService& repaySchedSvc,
        DateUtils& dateUtils)
        : jdbc_(jdbc),
          ctx_(ctx),
          loanRepo_(loanRepo),
          currencyRepo_(currencyRepo),
          productSvc_(productSvc),
          clientSvc_(clientSvc),
          groupSvc_(groupSvc),
          codeValueSvc_(codeValueSvc),
          calendarSvc_(calendarSvc),
          staffSvc_(staffSvc),
          paymentTypeSvc_(paymentTypeSvc),
          sqlGen_(sqlGen),
          delinquencySvc_(delinquencySvc),
          txnRepo_(txnRepo),
          repaySchedSvc_(repaySchedSvc),
          dateUtils_(dateUtils) {}

    // ---- retrieveOne ----
    std::shared_ptr<LoanAccountData> retrieveOne(Long loanId) override {
        auto user = ctx_.getAuthenticatedUserIfPresent();
        std::string hierarchy =
            user ? user->getOffice()->getHierarchy() : ".";
        std::string escaped = sqlGen_.escape("name");
        try {
            return jdbc_.queryForObjectLoanAccount("sql", hierarchy, escaped,
                                                   escaped);
        } catch (const EmptyResultDataAccessException&) {
            throw LoanNotFoundException(loanId);
        }
    }

    // ---- retrieveTemplateWithClientAndProductDetails ----
    std::shared_ptr<LoanAccountData>
    retrieveTemplateWithClientAndProductDetails(
        Long clientId, std::optional<Long> productId) override {
        ctx_.authenticatedUser();
        auto clientData = clientSvc_.retrieveOne(clientId);
        dateUtils_.getBusinessLocalDate();
        if (productId.has_value()) {
            productSvc_.retrieveLoanProduct(productId.value());
        }
        return std::make_shared<MockLoanAccountDataImpl>();
    }

    // ---- retrieveTemplateWithGroupAndProductDetails ----
    std::shared_ptr<LoanAccountData>
    retrieveTemplateWithGroupAndProductDetails(
        Long groupId, std::optional<Long> productId) override {
        ctx_.authenticatedUser();
        groupSvc_.retrieveOne(groupId);
        dateUtils_.getBusinessLocalDate();
        if (productId.has_value()) {
            productSvc_.retrieveLoanProduct(productId.value());
        }
        return std::make_shared<MockLoanAccountDataImpl>();
    }

    // ---- retrieveTemplateWithCompleteGroupAndProductDetails ----
    std::shared_ptr<LoanAccountData>
    retrieveTemplateWithCompleteGroupAndProductDetails(
        Long groupId, std::optional<Long> productId) override {
        ctx_.authenticatedUser();
        groupSvc_.retrieveOne(groupId);
        clientSvc_.retrieveClientMembersOfGroup(groupId);
        dateUtils_.getBusinessLocalDate();
        if (productId.has_value()) {
            productSvc_.retrieveLoanProduct(productId.value());
        }
        return std::make_shared<MockLoanAccountDataImpl>();
    }

    // ---- retrieveNewClosureDetails ----
    std::shared_ptr<LoanTransactionData> retrieveNewClosureDetails() override {
        ctx_.authenticatedUser();
        dateUtils_.getBusinessLocalDate();
        return std::make_shared<MockLoanTransactionDataImpl>();
    }

    // ---- retrieveNumberOfRepayments ----
    Integer retrieveNumberOfRepayments(Long loanId) override {
        ctx_.authenticatedUser();
        return loanRepo_.getNumberOfRepayments(loanId);
    }

    // ---- retrieveAllowedLoanOfficers ----
    std::optional<std::vector<std::shared_ptr<StaffData>>>
    retrieveAllowedLoanOfficers(std::optional<Long> officeId,
                                bool staffInSelectedOfficeOnly) override {
        if (!officeId.has_value()) {
            return std::nullopt;
        }
        if (staffInSelectedOfficeOnly) {
            return staffSvc_.retrieveAllLoanOfficersInOfficeById(
                officeId.value());
        }
        return staffSvc_.retrieveAllStaffInOfficeAndItsParentOfficeHierarchy(
            officeId.value(), true);
    }

    // ---- getLoanIdByLoanExternalId ----
    Long getLoanIdByLoanExternalId(const std::string& extIdStr) override {
        ExternalId ext(extIdStr);
        auto id = loanRepo_.findIdByExternalId(ext);
        if (!id.has_value()) {
            throw LoanNotFoundException();
        }
        return id.value();
    }

    // ---- retrieveLoanIdByAccountNumber ----
    std::optional<Long> retrieveLoanIdByAccountNumber(
        const std::string& accountNumber) override {
        try {
            return jdbc_.queryForObjectLong("sql", accountNumber);
        } catch (const EmptyResultDataAccessException&) {
            return std::nullopt;
        }
    }

    // ---- retrieveAccountNumberByAccountId ----
    std::string retrieveAccountNumberByAccountId(Long accountId) override {
        try {
            return jdbc_.queryForObjectString("sql", accountId);
        } catch (const EmptyResultDataAccessException&) {
            throw LoanNotFoundException(accountId);
        }
    }

    // ---- retrieveNumberOfActiveLoans ----
    Integer retrieveNumberOfActiveLoans() override {
        return jdbc_.queryForObjectInt("sql");
    }

    // ---- isGuaranteeRequired ----
    bool isGuaranteeRequired(Long loanId) override {
        auto result = jdbc_.queryForObjectBool("sql", loanId);
        if (!result.has_value()) {
            return false;
        }
        return result.value();
    }

    // ---- existsByLoanId ----
    bool existsByLoanId(Long loanId) override {
        return loanRepo_.existsByLoanId(loanId);
    }

    // ---- retrieveLoanIdByExternalId ----
    std::optional<Long> retrieveLoanIdByExternalId(
        const ExternalId& ext) override {
        return loanRepo_.findIdByExternalId(ext);
    }

    // ---- retrieveLoanTransactionIdByExternalId ----
    std::optional<Long> retrieveLoanTransactionIdByExternalId(
        const ExternalId& ext) override {
        return txnRepo_.findIdByExternalId(ext);
    }

    // ---- getResolvedLoanId ----
    Long getResolvedLoanId(const ExternalId& ext) override {
        auto id = loanRepo_.findIdByExternalId(ext);
        if (!id.has_value()) {
            throw LoanNotFoundException();
        }
        return id.value();
    }

    // ---- getResolvedLoanTransactionId ----
    Long getResolvedLoanTransactionId(std::optional<Long> txnId,
                                      const ExternalId& ext) override {
        if (txnId.has_value()) {
            return txnId.value();
        }
        auto id = txnRepo_.findIdByExternalId(ext);
        if (!id.has_value()) {
            throw LoanTransactionNotFoundException();
        }
        return id.value();
    }

    // ---- retrieveCalendars ----
    std::vector<std::shared_ptr<CalendarData>> retrieveCalendars(
        Long groupId) override {
        auto parent =
            calendarSvc_.retrieveParentCalendarsByEntity(groupId, "", "");
        auto entity =
            calendarSvc_.retrieveCalendarsByEntity(groupId, "", "");
        std::vector<std::shared_ptr<CalendarData>> combined;
        combined.insert(combined.end(), parent.begin(), parent.end());
        combined.insert(combined.end(), entity.begin(), entity.end());
        return calendarSvc_.updateWithRecurringDates(combined);
    }

    // ---- retrieveApprovalTemplate ----
    std::shared_ptr<LoanAccountData> retrieveApprovalTemplate(
        Long loanId) override {
        auto loan = loanRepo_.findOneWithNotFoundDetection(loanId, true);
        auto* currency = loan->getCurrency();
        currencyRepo_.findOneWithNotFoundDetection(currency);
        loan->getProposedPrincipal();
        loan->getNetDisbursalAmount();
        delinquencySvc_.calculateAvailableDisbursementAmountWithOverApplied(
            loan.get());
        dateUtils_.getBusinessLocalDate();
        return std::make_shared<MockLoanAccountDataImpl>();
    }

    // ---- retrieveRecoveryPaymentTemplate ----
    std::shared_ptr<LoanTransactionData> retrieveRecoveryPaymentTemplate(
        Long loanId) override {
        auto loan = loanRepo_.findOneWithNotFoundDetection(loanId, true);
        loan->getTotalWrittenOff();
        loan->getNetDisbursalAmount();
        loan->getExternalId();
        paymentTypeSvc_.retrieveAllPaymentTypes();
        return std::make_shared<MockLoanTransactionDataImpl>();
    }

    // ---- retrieveTotalPaidInAdvance ----
    PaidInAdvanceData retrieveTotalPaidInAdvance(Long loanId) override {
        sqlGen_.currentBusinessDate();
        try {
            auto bd = jdbc_.queryForObjectBigDecimal("sql", loanId);
            return PaidInAdvanceData{bd};
        } catch (const EmptyResultDataAccessException&) {
            return PaidInAdvanceData{BigDecimal(0)};
        }
    }

    // ---- retrieveLoanWriteoffTemplate ----
    std::shared_ptr<LoanTransactionData> retrieveLoanWriteoffTemplate(
        Long loanId) override {
        auto loanData = retrieveOne(loanId);
        auto* summary = loanData->getSummary();
        summary->getTotalOutstanding();
        loanData->getNetDisbursalAmount();
        loanData->getExternalId();
        codeValueSvc_.retrieveCodeValuesByCode("WriteOffReasons");
        dateUtils_.getBusinessLocalDate();
        return std::make_shared<MockLoanTransactionDataImpl>();
    }

    // ---- countInstallmentsByLoanIdWhereIsAdditionalFalseAndIsDownPaymentFalse
    Integer
    countInstallmentsByLoanIdWhereIsAdditionalFalseAndIsDownPaymentFalse(
        Long loanId) override {
        return repaySchedSvc_
            .countInstallmentsByLoanIdWhereIsAdditionalFalseAndIsDownPaymentFalse(
                loanId);
    }

    // ---- retrieveLoanChargeOffTemplate ----
    std::shared_ptr<LoanTransactionData> retrieveLoanChargeOffTemplate(
        Long loanId) override {
        auto loanData = retrieveOne(loanId);
        auto* summary = loanData->getSummary();
        summary->getTotalOutstanding();
        summary->getPrincipalOutstanding();
        summary->getInterestOutstanding();
        summary->getFeeChargesOutstanding();
        summary->getPenaltyChargesOutstanding();
        loanData->getNetDisbursalAmount();
        loanData->getExternalId();
        codeValueSvc_.retrieveCodeValuesByCode("ChargeOffReasons");
        dateUtils_.getBusinessLocalDate();
        return std::make_shared<MockLoanTransactionDataImpl>();
    }

    // ---- retrieveDisbursalTemplate ----
    std::shared_ptr<LoanTransactionData> retrieveDisbursalTemplate(
        Long loanId, bool paymentDetails) override {
        auto loan = loanRepo_.findOneWithNotFoundDetection(loanId, true);
        auto* currency = loan->getCurrency();
        currencyRepo_.findOneWithNotFoundDetection(currency);
        loan->getExpectedDisbursedOnLocalDateForTemplate();
        loan->getDisburseAmountForTemplate();
        loan->getNetDisbursalAmount();
        loan->retriveLastEmiAmount();
        loan->getNextPossibleRepaymentDateForRescheduling();
        delinquencySvc_.calculateAvailableDisbursementAmountWithOverApplied(
            loan.get());
        if (paymentDetails) {
            paymentTypeSvc_.retrieveAllPaymentTypes();
        }
        return std::make_shared<MockLoanTransactionDataImpl>();
    }

    // ---- retrieveLoanDisbursementDetails ----
    std::vector<std::shared_ptr<DisbursementData>>
    retrieveLoanDisbursementDetails(Long loanId) override {
        sqlGen_.inParametersFor({loanId});
        sqlGen_.escape("name");
        sqlGen_.inSql("dd.loan_id", {loanId});
        sqlGen_.groupConcat("lc.id");
        return jdbc_.queryDisbursements("sql", loanId);
    }

    // ---- retrieveLoanIdsWithPendingIncomePostingTransactions ----
    std::optional<std::vector<Long>>
    retrieveLoanIdsWithPendingIncomePostingTransactions() override {
        auto date = dateUtils_.getBusinessLocalDate();
        try {
            auto ids = jdbc_.queryForListLong("sql", date);
            return ids;
        } catch (const EmptyResultDataAccessException&) {
            return std::nullopt;
        }
    }

    // ---- retrieveLoanIdsByExternalIds ----
    std::vector<Long> retrieveLoanIdsByExternalIds(
        const std::vector<ExternalId>& ids) override {
        return loanRepo_.findIdByExternalIds(ids);
    }

private:
    // Tiny inner stubs returned by template / closure methods
    struct MockLoanAccountDataImpl : public LoanAccountData {
        LoanSummaryData* getSummary() const override { return nullptr; }
        BigDecimal getNetDisbursalAmount() const override {
            return BigDecimal();
        }
        ExternalId getExternalId() const override {
            return ExternalId::emptyId();
        }
    };

    struct MockLoanTransactionDataImpl : public LoanTransactionData {
        const LoanTransactionType* getType() const override {
            static LoanTransactionType type;
            return &type;
        }
    };

    JdbcTemplate& jdbc_;
    PlatformSecurityContext& ctx_;
    LoanRepositoryWrapper& loanRepo_;
    ApplicationCurrencyRepositoryWrapper& currencyRepo_;
    LoanProductReadPlatformService& productSvc_;
    ClientReadPlatformService& clientSvc_;
    GroupReadPlatformService& groupSvc_;
    CodeValueReadPlatformService& codeValueSvc_;
    CalendarReadPlatformService& calendarSvc_;
    StaffReadPlatformService& staffSvc_;
    PaymentTypeReadService& paymentTypeSvc_;
    DatabaseSpecificSQLGenerator& sqlGen_;
    DelinquencyReadPlatformService& delinquencySvc_;
    LoanTransactionRepository& txnRepo_;
    LoanRepaymentScheduleService& repaySchedSvc_;
    DateUtils& dateUtils_;
};

} // namespace fineract
