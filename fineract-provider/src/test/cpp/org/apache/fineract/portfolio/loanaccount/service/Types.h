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

#include <cstdint>
#include <memory>
#include <optional>
#include <stdexcept>
#include <string>
#include <vector>

namespace fineract {

// ---------------------------------------------------------------------------
// Forward declarations & simple value types
// ---------------------------------------------------------------------------

using Long = int64_t;
using Integer = int32_t;

/** Mirrors java.math.BigDecimal (simplified). */
struct BigDecimal {
    double value{0.0};
    explicit BigDecimal(double v = 0.0) : value(v) {}
    bool operator==(const BigDecimal& o) const { return value == o.value; }
    bool operator!=(const BigDecimal& o) const { return !(*this == o); }
};

/** Mirrors java.time.LocalDate (simplified). */
struct LocalDate {
    int year{};
    int month{};
    int day{};
    LocalDate() = default;
    LocalDate(int y, int m, int d) : year(y), month(m), day(d) {}
    bool operator==(const LocalDate& o) const {
        return year == o.year && month == o.month && day == o.day;
    }
};

// ---------------------------------------------------------------------------
// Domain value / data classes  (stubs)
// ---------------------------------------------------------------------------

struct ExternalId {
    std::string value;
    ExternalId() = default;
    explicit ExternalId(const std::string& v) : value(v) {}
    bool empty() const { return value.empty(); }
    static ExternalId emptyId() { return ExternalId(); }
    bool operator==(const ExternalId& o) const { return value == o.value; }
};

struct Office {
    virtual ~Office() = default;
    virtual std::string getHierarchy() const = 0;
};

struct AppUser {
    virtual ~AppUser() = default;
    virtual Office* getOffice() const = 0;
};

struct MonetaryCurrency {
    virtual ~MonetaryCurrency() = default;
};

struct ApplicationCurrency {
    virtual ~ApplicationCurrency() = default;
};

struct LoanSummaryData {
    virtual ~LoanSummaryData() = default;
    virtual BigDecimal getTotalOutstanding() const = 0;
    virtual BigDecimal getPrincipalOutstanding() const = 0;
    virtual BigDecimal getInterestOutstanding() const = 0;
    virtual BigDecimal getFeeChargesOutstanding() const = 0;
    virtual BigDecimal getPenaltyChargesOutstanding() const = 0;
    virtual BigDecimal getPaidInAdvance() const = 0;
};

struct LoanTransactionType {
    virtual ~LoanTransactionType() = default;
};

struct LoanTransactionData {
    virtual ~LoanTransactionData() = default;
    virtual const LoanTransactionType* getType() const = 0;
};

struct LoanAccountData {
    virtual ~LoanAccountData() = default;
    virtual LoanSummaryData* getSummary() const = 0;
    virtual BigDecimal getNetDisbursalAmount() const = 0;
    virtual ExternalId getExternalId() const = 0;
};

struct ClientData {
    virtual ~ClientData() = default;
};

struct GroupGeneralData {
    virtual ~GroupGeneralData() = default;
};

struct LoanProductData {
    virtual ~LoanProductData() = default;
};

struct StaffData {
    virtual ~StaffData() = default;
};

struct CalendarData {
    virtual ~CalendarData() = default;
};

struct CodeValueData {
    virtual ~CodeValueData() = default;
};

struct DisbursementData {
    virtual ~DisbursementData() = default;
};

struct Loan {
    virtual ~Loan() = default;
    virtual MonetaryCurrency* getCurrency() const = 0;
    virtual BigDecimal getProposedPrincipal() const = 0;
    virtual BigDecimal getNetDisbursalAmount() const = 0;
    virtual BigDecimal getDisburseAmountForTemplate() const = 0;
    virtual BigDecimal getTotalWrittenOff() const = 0;
    virtual BigDecimal retriveLastEmiAmount() const = 0;
    virtual LocalDate getExpectedDisbursedOnLocalDateForTemplate() const = 0;
    virtual LocalDate getNextPossibleRepaymentDateForRescheduling() const = 0;
    virtual ExternalId getExternalId() const = 0;
};

struct PaidInAdvanceData {
    BigDecimal paidInAdvance;
    BigDecimal getPaidInAdvance() const { return paidInAdvance; }
};

// ---------------------------------------------------------------------------
// Exceptions
// ---------------------------------------------------------------------------

struct LoanNotFoundException : std::runtime_error {
    explicit LoanNotFoundException(Long id = 0)
        : std::runtime_error("Loan not found: " + std::to_string(id)) {}
};

struct LoanTransactionNotFoundException : std::runtime_error {
    explicit LoanTransactionNotFoundException(Long id = 0)
        : std::runtime_error("Loan transaction not found: " + std::to_string(id)) {}
};

struct EmptyResultDataAccessException : std::runtime_error {
    explicit EmptyResultDataAccessException(int expectedSize = 1)
        : std::runtime_error("Expected " + std::to_string(expectedSize) + " result(s)") {}
};

} // namespace fineract
