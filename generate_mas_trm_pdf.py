#!/usr/bin/env python3
"""Generate MAS TRM Compliance Analysis PDF for Apache Fineract."""

from fpdf import FPDF

TITLE = "MAS TRM Guidelines Compliance Analysis \u2014 Apache Fineract"
SUBTITLE = (
    "Assessment of application-level controls against MAS Technology Risk "
    "Management Guidelines (January 2021)"
)
DATE = "March 9, 2026"
OUTPUT_FILE = "MAS_TRM_Compliance_Analysis.pdf"

FONT = "DejaVu"
FONT_REG = "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf"
FONT_BOLD = "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf"
PAGE_W = 210
MARGIN = 20
CONTENT_W = PAGE_W - 2 * MARGIN


class PDF(FPDF):
    def __init__(self):
        super().__init__()
        self.section_number = 0
        self.toc_entries = []
        self.add_font(FONT, "", FONT_REG, uni=True)
        self.add_font(FONT, "B", FONT_BOLD, uni=True)
        self.add_font(FONT, "I", FONT_REG, uni=True)

    def header(self):
        if self.page_no() == 1:
            return
        self.set_font(FONT, "I", 8)
        self.set_text_color(120, 120, 120)
        self.cell(0, 8, TITLE, align="C", new_x="LMARGIN", new_y="NEXT")
        self.line(MARGIN, 14, PAGE_W - MARGIN, 14)
        self.ln(4)

    def footer(self):
        self.set_y(-15)
        self.set_font(FONT, "I", 8)
        self.set_text_color(120, 120, 120)
        self.cell(0, 10, f"Page {self.page_no()}/{{nb}}", align="C")

    def section_heading(self, title, level=1):
        if level == 1:
            self.section_number += 1
            num = f"{self.section_number}."
            self.set_font(FONT, "B", 14)
            self.set_text_color(0, 51, 102)
        else:
            num = ""
            self.set_font(FONT, "B", 11)
            self.set_text_color(0, 70, 130)
        full_title = f"{num} {title}" if num else title
        self.toc_entries.append((level, full_title, self.page_no()))
        self.ln(4)
        self.cell(0, 8, full_title, new_x="LMARGIN", new_y="NEXT")
        self.set_draw_color(0, 51, 102)
        if level == 1:
            self.line(MARGIN, self.get_y(), PAGE_W - MARGIN, self.get_y())
        self.ln(3)
        self.set_text_color(0, 0, 0)

    def body_text(self, text):
        self.set_font(FONT, "", 10)
        self.multi_cell(CONTENT_W, 5.5, text)
        self.ln(2)

    def bullet(self, text, indent=8):
        x = self.get_x()
        self.set_font(FONT, "", 10)
        self.set_x(x + indent)
        self.cell(5, 5.5, "\u2022")
        self.multi_cell(CONTENT_W - indent - 5, 5.5, text)
        self.ln(1)

    def bold_bullet(self, bold_part, rest, indent=8):
        x = self.get_x()
        self.set_x(x + indent)
        self.set_font(FONT, "", 10)
        self.cell(5, 5.5, "\u2022")
        self.set_font(FONT, "B", 10)
        self.write(5.5, bold_part)
        self.set_font(FONT, "", 10)
        self.write(5.5, rest)
        self.ln(6.5)


def draw_table_row(pdf, area, gap, col_w_area, col_w_gap, fill):
    row_h = 7.0
    pdf.set_font(FONT, "", 9)
    lines_area = max(1, int(pdf.get_string_width(area) // (col_w_area - 4)) + 1)
    lines_gap = max(1, int(pdf.get_string_width(gap) // (col_w_gap - 4)) + 1)
    max_lines = max(lines_area, lines_gap)
    cell_h = max(row_h, max_lines * 5.5)
    y_before = pdf.get_y()
    if y_before + cell_h > pdf.h - 20:
        pdf.add_page()
        y_before = pdf.get_y()
    if fill:
        pdf.set_fill_color(230, 237, 245)
    else:
        pdf.set_fill_color(255, 255, 255)
    line_h = cell_h / max_lines
    pdf.set_xy(MARGIN, y_before)
    pdf.multi_cell(col_w_area, line_h, f" {area}", border="LTB", fill=fill)
    pdf.set_xy(MARGIN + col_w_area, y_before)
    pdf.multi_cell(col_w_gap, line_h, f" {gap}", border="RTB", fill=fill,
                   new_x="LMARGIN", new_y="NEXT")
    pdf.set_y(y_before + cell_h)


def build_pdf():
    pdf = PDF()
    pdf.alias_nb_pages()
    pdf.set_auto_page_break(auto=True, margin=20)

    # Title page
    pdf.add_page()
    pdf.ln(50)
    pdf.set_font(FONT, "B", 24)
    pdf.set_text_color(0, 51, 102)
    pdf.multi_cell(CONTENT_W, 12, TITLE, align="C")
    pdf.ln(8)
    pdf.set_font(FONT, "", 12)
    pdf.set_text_color(80, 80, 80)
    pdf.multi_cell(CONTENT_W, 7, SUBTITLE, align="C")
    pdf.ln(12)
    pdf.set_font(FONT, "B", 12)
    pdf.set_text_color(0, 0, 0)
    pdf.cell(CONTENT_W, 7, DATE, align="C", new_x="LMARGIN", new_y="NEXT")
    pdf.ln(20)
    pdf.set_draw_color(0, 51, 102)
    pdf.set_line_width(0.8)
    pdf.line(60, pdf.get_y(), PAGE_W - 60, pdf.get_y())
    pdf.ln(10)
    pdf.set_font(FONT, "I", 10)
    pdf.set_text_color(100, 100, 100)
    pdf.multi_cell(CONTENT_W, 6,
        "Prepared for informational purposes. This document does not constitute "
        "legal or regulatory advice. Organisations deploying Apache Fineract "
        "should engage qualified professionals for a formal MAS TRM compliance "
        "assessment.", align="C")

    # TOC placeholder
    toc_page_number = pdf.page_no() + 1
    pdf.add_page()
    toc_y_start = pdf.get_y()

    # 1. Executive Summary
    pdf.add_page()
    pdf.section_heading("Executive Summary")
    pdf.body_text(
        "Apache Fineract is an open-source core banking platform maintained "
        "under the Apache Software Foundation. While Fineract does not "
        "explicitly reference or target MAS TRM compliance, it implements "
        "several security controls that align with various sections of the "
        "MAS Technology Risk Management Guidelines (January 2021).")
    pdf.body_text(
        "Full MAS TRM compliance requires organizational, operational, and "
        "infrastructure-level controls that extend well beyond the application "
        "layer. This document maps Fineract\u2019s application-level controls to "
        "the relevant TRM sections and identifies notable gaps.")

    # 2. Access Control
    pdf.section_heading("Access Control (TRM Section 9)")
    pdf.bold_bullet("Role-Based Access Control (RBAC): ",
        "Fineract implements granular RBAC through Role, Permission, and "
        "AppUser entities. Each API operation is guarded by permission "
        "checks at the service layer. Key classes include "
        "fineract-core/.../useradministration/domain/Role.java and AppUser.java.")
    pdf.bold_bullet("URL-level Authorization: ",
        "SecurityConfig enforces URL-level authorization rules via Spring "
        "Security filter chains "
        "(fineract-provider/.../infrastructure/core/config/SecurityConfig.java).")
    pdf.bold_bullet("Maker-Checker (4-Eye Principle): ",
        "Dual authorization is implemented via "
        "PortfolioCommandSourceWritePlatformServiceImpl and "
        "CommandSourceService. The system prevents the same user from acting "
        "as both maker and checker for the same command, enforcing separation "
        "of duties.")
    pdf.bold_bullet("Data Scope (Office Hierarchy): ",
        "Access is restricted based on the office hierarchy. "
        "SpringSecurityPlatformSecurityContext.validateAccessRights ensures "
        "users can only access data within their organizational scope.")

    # 3. Authentication
    pdf.section_heading("Authentication (TRM Section 9)")
    pdf.bold_bullet("Multi-Factor Authentication: ",
        "Fineract supports HTTP Basic Auth, OAuth2 (JWT), and optional "
        "two-factor authentication via SMS or email OTP. The 2FA "
        "implementation is in TwoFactorServiceImpl.")
    pdf.bold_bullet("Password Policies: ",
        "Configurable password complexity is enforced via "
        "PasswordValidationPolicy. The platform supports forced password "
        "reset on first login, password expiration, and prevention of "
        "password reuse.")
    pdf.bold_bullet("Stateless Sessions: ",
        "Spring Security is configured with "
        "SessionCreationPolicy.STATELESS, which prevents session fixation "
        "attacks by ensuring no server-side HTTP sessions are created.")

    # 4. Audit Trail
    pdf.section_heading("Audit Trail (TRM Section 11)")
    pdf.body_text(
        "Fineract maintains a comprehensive audit trail for every non-read "
        "API request:")
    pdf.bullet(
        "All mutating commands are persisted to the "
        "m_portfolio_command_source table, recording the maker, checker, "
        "timestamps, client IP address, full command JSON, and processing "
        "status.")
    pdf.bullet(
        "Audit records are exposed via the /v1/audits REST API "
        "(AuditsApiResource), enabling external systems and compliance "
        "officers to query the trail.")
    pdf.bullet(
        "Resilience4j retry mechanisms are used to ensure reliable audit "
        "persistence even under transient failures.")
    pdf.bullet(
        "IP tracking is performed by CallerIpTrackingFilter, and "
        "correlation IDs support distributed tracing across service "
        "boundaries.")

    # 5. Cryptography / Data Protection
    pdf.section_heading("Cryptography / Data Protection (TRM Section 10)")
    pdf.bold_bullet("AES-256 Encryption: ",
        "Tenant database passwords are encrypted using AES-256 with "
        "PBKDF2 key derivation, implemented in EncryptionUtil.")
    pdf.bold_bullet("Password Hashing: ",
        "User passwords are hashed using bcrypt via Spring Security\u2019s "
        "DelegatingPasswordEncoder, providing strong one-way hashing with "
        "salting.")
    pdf.bold_bullet("TLS / HSTS: ",
        "Transport Layer Security and HTTP Strict Transport Security are "
        "configurable in SecurityConfig, supporting encrypted "
        "communication channels.")

    # 6. Application Security
    pdf.section_heading("Application Security (TRM Sections 6/7)")
    pdf.bold_bullet("SQL Injection Prevention: ",
        "A multi-layered validation framework detects configurable attack "
        "patterns including blind injection, timing attacks, and stacked "
        "queries. Parameterized queries are enforced via the SQLBuilder "
        "utility class.")
    pdf.bold_bullet("Content Upload Whitelisting: ",
        "File uploads are restricted by configurable regex patterns and "
        "MIME type validation, preventing malicious file uploads.")
    pdf.bold_bullet("Idempotency: ",
        "The IdempotencyStoreFilter prevents duplicate request processing, "
        "ensuring each command is executed exactly once.")
    pdf.bold_bullet("CORS: ",
        "Fully configurable Cross-Origin Resource Sharing policies allow "
        "organisations to restrict API access to authorized origins.")

    # 7. Multi-Tenancy & Data Isolation
    pdf.section_heading("Multi-Tenancy & Data Isolation (TRM Section 10)")
    pdf.body_text(
        "Fineract achieves strong data isolation in multi-tenant deployments "
        "through the following mechanisms:")
    pdf.bullet(
        "Each tenant is assigned a separate database schema, routed at "
        "runtime via RoutingDataSource. This ensures that one tenant\u2019s "
        "data is never accessible to another.")
    pdf.bullet(
        "Isolated connection pools with encrypted credentials are managed "
        "by DataSourcePerTenantServiceFactory, preventing credential leakage "
        "across tenants.")

    # 8. Notable Gaps
    pdf.section_heading("Notable Gaps Relative to Full MAS TRM Compliance")
    pdf.body_text(
        "The following table summarises areas where Fineract\u2019s "
        "application-layer controls alone do not satisfy the full scope of "
        "MAS TRM requirements. Organisations must address these gaps through "
        "complementary governance, infrastructure, and operational measures.")

    gaps = [
        ("IT Governance & Risk Management", "No board-level risk framework"),
        ("IT Project Management", "No SDLC governance controls in codebase"),
        ("Business Continuity / Disaster Recovery", "No built-in DR orchestration"),
        ("Data Loss Prevention", "No DLP mechanisms at app layer"),
        ("Intrusion Detection / Security Monitoring", "No SIEM integration"),
        ("Penetration Testing", "Operational concern, not in codebase"),
        ("Vendor / Outsourcing Risk", "Not applicable at app level"),
        ("Account Lockout", "No account lockout after failed logins found"),
        ("Session Timeout", "Stateless API avoids sessions but no idle timeout for OAuth2 tokens"),
        ("Data Masking / PII Protection", "Limited evidence of data masking"),
    ]

    col_w_area = 65.0
    col_w_gap = CONTENT_W - col_w_area
    row_h = 7.0

    pdf.set_font(FONT, "B", 10)
    pdf.set_fill_color(0, 51, 102)
    pdf.set_text_color(255, 255, 255)
    pdf.cell(col_w_area, row_h, " TRM Area", border=1, fill=True)
    pdf.cell(col_w_gap, row_h, " Gap Description", border=1, fill=True,
             new_x="LMARGIN", new_y="NEXT")

    pdf.set_text_color(0, 0, 0)
    for i, (area, gap) in enumerate(gaps):
        draw_table_row(pdf, area, gap, col_w_area, col_w_gap, fill=(i % 2 == 1))

    pdf.ln(4)

    # 9. Conclusion
    pdf.section_heading("Conclusion")
    pdf.body_text(
        "Apache Fineract provides strong foundational controls for access "
        "control, audit logging, maker-checker dual authorization, "
        "encryption, two-factor authentication, password management, SQL "
        "injection prevention, and multi-tenant data isolation. These "
        "controls align well with several sections of the MAS TRM "
        "Guidelines.")
    pdf.body_text(
        "However, full MAS TRM compliance requires complementary "
        "organizational governance, infrastructure hardening, operational "
        "procedures, and third-party security tooling. Financial institutions "
        "deploying Fineract should conduct a comprehensive gap analysis "
        "covering IT governance, business continuity, data loss prevention, "
        "intrusion detection, penetration testing, and vendor risk management "
        "to achieve full regulatory compliance.")

    # Back-fill Table of Contents
    pdf.page = toc_page_number
    pdf.set_y(toc_y_start)
    pdf.set_font(FONT, "B", 18)
    pdf.set_text_color(0, 51, 102)
    pdf.cell(0, 12, "Table of Contents", new_x="LMARGIN", new_y="NEXT")
    pdf.ln(6)

    for level, title, page in pdf.toc_entries:
        if level == 1:
            pdf.set_font(FONT, "B", 11)
            indent = 0
        else:
            pdf.set_font(FONT, "", 10)
            indent = 10
        pdf.set_text_color(0, 0, 0)
        pdf.set_x(MARGIN + indent)
        title_w = pdf.get_string_width(title) + 2
        page_str = str(page)
        pdf.cell(title_w, 7, title)
        pdf.set_x(PAGE_W - MARGIN - 10)
        pdf.set_font(FONT, "", 11)
        pdf.cell(10, 7, page_str, align="R", new_x="LMARGIN", new_y="NEXT")
        pdf.ln(1)

    pdf.output(OUTPUT_FILE)
    print(f"PDF generated: {OUTPUT_FILE}")


if __name__ == "__main__":
    build_pdf()
