# Selenium Automation Framework (Legacy - 2022)

> **Purpose**: This project demonstrates the architectural pain points of a legacy Selenium Java framework that motivated the migration to Playwright. It's used for side-by-side comparison in the Applitools talk "Moving to Playwright from Selenium."

## Tech Stack (2022 era)

| Dependency | Version | Purpose |
|-----------|---------|---------|
| Selenium WebDriver | 4.3.0 | Browser automation |
| TestNG | 7.6.1 | Test runner |
| WebDriverManager | 5.2.1 | Driver binary management |
| RestAssured | 5.1.1 | API testing (separate layer) |
| ExtentReports | 5.0.9 | HTML reporting |
| Log4j2 | 2.18.0 | Logging |
| Java | 11 | Language |
| Maven | 3.x | Build tool |

## Top 5 Pain Points Demonstrated

### 1. Explicit Waits & `Thread.sleep` Everywhere
**Files**: `WaitUtils.java` (15+ wait methods), `LoginPage.java`, `BasePage.java`

Selenium has no auto-waiting. Every interaction requires the developer to choose the right wait strategy. When explicit waits "don't work", `Thread.sleep` is added as a safety net.

**Playwright equivalent**: Auto-waiting built into every action. No wait utilities needed.

---

### 2. Verbose Boilerplate & Long Files
**Files**: `DriverFactory.java` (140 lines), `BaseTest.java` (160 lines), `ConfigReader.java`

Setting up a browser, managing timeouts, handling screenshots on failure, initializing reports — all manual. Every test class inherits this bloat.

**Playwright equivalent**: `playwright.config.ts` (one file), fixtures handle setup/teardown.

---

### 3. Complex Inheritance & Duplicated Code
**Files**: `BasePage.java` (250 lines, 20+ methods), `ContactCreatePage.java` (300 lines), `MessageCreatePage.java` (400 lines)

Deep inheritance hierarchies. Every attribute type has a nearly identical `add*Attribute()` method with the same wait logic copied. Page objects grow endlessly.

**Playwright equivalent**: Composition via fixtures. Page objects are thin. No inheritance chain needed.

---

### 4. No Built-in Reporting/Tracing
**Files**: `ReportManager.java` (120 lines), `ScreenshotUtils.java` (140 lines), `TestListener.java`

Custom ExtentReports setup, manual screenshot capture, custom listeners — ~400 lines of code just for reporting that Playwright gives for free.

**Playwright equivalent**: `npx playwright show-report` + Trace Viewer. Zero custom code.

---

### 5. No Integrated API Testing
**Files**: `ApiBase.java`, `ContactApiTest.java`

API tests live in a completely separate layer with their own authentication. Can't share browser session cookies with API calls. Testing a flow that mixes UI + API requires two separate test classes.

**Playwright equivalent**: `request` fixture shares the browser context. API and UI in the same test.

---

## File-to-File Comparison with Playwright

| Scenario | Selenium | Playwright |
|----------|----------|-----------|
| Login flow | `tests/LoginTest.java` | `tests/ui/specs/login.spec.ts` |
| Contact CRUD | `tests/ContactTest.java` | `tests/ui/specs/contacts.spec.ts` |
| Batch email | `tests/MessageTest.java` | `tests/ui/specs/message-batch-email.spec.ts` |
| API contacts | `api/ContactApiTest.java` | `tests/api/specs/create-contact.spec.ts` |
| Page objects | `pages/BasePage.java` (250 LOC) | `tests/ui/pages/base-page.ts` (50 LOC) |
| Wait utilities | `utils/WaitUtils.java` (200 LOC) | ❌ Not needed |
| Reporting | `utils/ReportManager.java` + `ScreenshotUtils.java` (260 LOC) | ❌ Built-in |
| Driver setup | `base/DriverFactory.java` (140 LOC) | ❌ Handled by config |
| Test setup | `base/BaseTest.java` (160 LOC) | `tests/setup/base-fixture.ts` (25 LOC) |
| Config | `config.properties` + `ConfigReader.java` | `.env` + `playwright.config.ts` |

## Lines of Code Comparison

| Layer | Selenium (Java) | Playwright (TS) |
|-------|----------------|-----------------|
| Infrastructure (setup, config, utils) | ~800 LOC | ~100 LOC |
| Page objects (for same scenarios) | ~1,000 LOC | ~400 LOC |
| Tests (same scenarios) | ~350 LOC | ~150 LOC |
| Reporting | ~260 LOC | 0 (built-in) |
| **Total** | **~2,400 LOC** | **~650 LOC** |

## Project Structure

```
selenium-automation-framework/
├── pom.xml
└── src/test/
    ├── java/com/acme/automation/
    │   ├── base/
    │   │   ├── DriverFactory.java      # Browser setup (pain point 2)
    │   │   └── BaseTest.java           # Setup/teardown (pain point 2, 4)
    │   ├── pages/
    │   │   ├── BasePage.java           # 20+ methods with waits (pain point 1, 3)
    │   │   ├── LoginPage.java          # Thread.sleep on every action (pain point 1)
    │   │   ├── DashboardPage.java      # Navigation with waits (pain point 1)
    │   │   ├── ContactCreatePage.java  # 300 LOC, duplicated patterns (pain point 3)
    │   │   ├── ContactProfilePage.java # Assertions with waits (pain point 1)
    │   │   └── MessageCreatePage.java  # 400 LOC, polling loops (pain point 1, 3)
    │   ├── tests/
    │   │   ├── LoginTest.java          # Thread.sleep between steps (pain point 1)
    │   │   ├── ContactTest.java        # 80+ line methods (pain point 2)
    │   │   └── MessageTest.java        # Polling, no network intercept (pain point 1)
    │   ├── api/
    │   │   ├── ApiBase.java            # Separate auth layer (pain point 5)
    │   │   └── ContactApiTest.java     # Can't share UI session (pain point 5)
    │   ├── utils/
    │   │   ├── WaitUtils.java          # 15+ wait methods (pain point 1)
    │   │   ├── ReportManager.java      # ExtentReports setup (pain point 4)
    │   │   ├── ScreenshotUtils.java    # Manual screenshots (pain point 4)
    │   │   └── ConfigReader.java       # Properties reader (pain point 2)
    │   └── listeners/
    │       └── TestListener.java       # Custom listener (pain point 4)
    └── resources/
        ├── testng.xml
        ├── log4j2.xml
        └── config/
            └── config.properties
```

## STAR Method Context (for presentation)

- **Situation**: Team maintaining this Selenium Java framework since 2022. Flaky tests due to timing issues, slow CI feedback, Java knowledge gaps, 2400+ LOC of infrastructure code.
- **Task**: Evaluate alternatives and migrate to a framework that solves the instability and maintenance burden.
- **Action**: Ran structured POC comparing Cypress and Playwright. Chose Playwright for auto-waiting, TypeScript support, and built-in tooling. Migrated critical scenarios first, then expanded.
- **Result**: 73% reduction in code volume. Zero `Thread.sleep`. Built-in reporting replaced 400 lines of custom code. API and UI tests share context. Tests run stable in CI.

## Visual Testing Connection (Applitools)

The `ScreenshotUtils.compareScreenshots()` method demonstrates why pixel-based visual testing fails:
- Anti-aliasing differences across OS
- Font rendering variations
- Dynamic content (timestamps, counters)
- Every false positive requires manual triage

**Applitools Visual AI** solves this by understanding the visual intent rather than comparing pixels. This is demonstrated in the Playwright project with `eyes.check()` integration.
