<!--
Sync Impact Report
Version change: 1.0.2 → 1.0.3
Modified principles: none (no principle renamed or removed)
Added sections: none
Modified sections: Development Standards — resolved internal contradiction between "Testcontainers for
  integration tests" and the H2 Database standard (found by /speckit-analyze on feature
  001-reserva-cita-online); Testing Frameworks now specifies H2 for integration tests, with
  Testcontainers as an option only if a non-H2 production engine is later adopted.
Removed sections: none
Templates requiring updates:
  - plan-template.md ✅ no change needed (Constitution Check gate is generic)
  - spec-template.md ✅ no change needed (BDD scenarios unaffected)
  - tasks-template.md ✅ no change needed (task categorization unaffected)
Follow-up TODOs: none
-->
# Citasalud Service Constitution

## Core Principles

### I. Clean Architecture

The project must follow Clean Architecture principles as defined by Robert Martin. Code is organized into concentric layers:
- **Entities**: Core business logic and rules, independent of frameworks.
- **Use Cases**: Application-specific business rules, orchestrating entities.
- **Interface Adapters**: Controllers, presenters, gateways that adapt external interfaces.
- **Frameworks & Drivers**: Web frameworks, databases, external services—easily replaceable.

Dependencies point inward only. No outer layer should know about inner layers. This ensures high testability and maintainability.

### II. Behavior-Driven Development (BDD)

All code must follow a BDD approach with three levels of testing:
- **Unit Tests**: Isolated business logic using Given-When-Then format; no external dependencies.
- **Integration Tests**: Test interactions between layers and with external systems (databases, APIs).
- **Functional Tests**: End-to-end tests covering complete user workflows and acceptance criteria.

Test scenarios must be written in plain language (Gherkin), approved by stakeholders before implementation, and mapped to test code. Red-Green-Refactor cycle mandatory.

### III. Code Quality & Best Practices

All code must adhere to industry best practices:
- **SOLID Principles**: Single Responsibility, Open/Closed, Liskov Substitution, Interface Segregation, Dependency Inversion.
- **YAGNI (You Ain't Gonna Need It)**: No speculative features; build only what is currently required.
- **DRY (Don't Repeat Yourself)**: No duplicate code; extract common logic into reusable components.

Code reviews must verify compliance with these principles before merge.

### IV. API First & OpenAPI

All APIs must follow an API-first approach with OpenAPI specifications:
- Every API endpoint requires a formal OpenAPI 3.0 contract before implementation.
- Use `openapi-generator` to generate server stubs, client SDKs, and documentation automatically from contracts.
- API contracts are versioned and stored in version control alongside code.
- Breaking changes require new API versions and migration documentation.

### V. Code Quality Metrics & JaCoCo

Code coverage is a non-negotiable quality gate:
- **Per-class coverage**: Must exceed 80%.
- **Global coverage**: Must be >= 80%.
- Use JaCoCo Maven plugin to generate coverage reports on every build.
- Coverage gates are enforced in CI/CD pipelines; pull requests fail if thresholds are not met.
- Coverage reports are published with every release.

## Development Standards

All development must follow these standards:

- **Language**: Java 21+ (as defined in project configuration).
- **Framework**: Spring Boot 4.1.0+ for web and persistence layers.
- **Persistence**: Spring Data JPA MUST be used as the data access abstraction (repositories, entity management); avoid raw JDBC/manual SQL except for cases Spring Data cannot express (documented as justified exceptions).
- **Database**: H2 is the database engine for development and test environments (in-memory or file-based). Production database engine, if different, MUST be documented separately and validated against the same Spring Data JPA repository contracts.
- **Build Tool**: Gradle with dependency management plugin.
- **Testing Frameworks**: JUnit 5, Mockito for unit tests; H2 in-memory (via `@DataJpaTest`/`@SpringBootTest`) for integration tests, consistent with the Database standard below. Testcontainers MAY be introduced instead if a production database engine other than H2 is adopted in the future.
- **Boilerplate Reduction**: Lombok MUST be used to eliminate boilerplate code (getters, setters, constructors, builders, equals/hashCode). Use annotations judiciously—avoid `@Data` on JPA entities; prefer explicit `@Getter`/`@Setter`, `@Builder`, and `@RequiredArgsConstructor` for clarity and to prevent common entity pitfalls (equals/hashCode on mutable/lazy fields).
- **Code Style**: Follow Google Java Style Guide; enforce via CheckStyle or similar.
- **Logging**: Structured logging using SLF4J and Logback; log at appropriate levels (TRACE, DEBUG, INFO, WARN, ERROR).

## Quality & Compliance Gates

The following gates are mandatory before code is merged:

1. All tests pass (unit, integration, functional).
2. Code coverage thresholds met (per-class > 80%, global >= 80%).
3. OpenAPI contracts defined for all REST endpoints.
4. SOLID principles verified in code review.
5. No code duplication (DRY compliance).
6. Build succeeds without warnings (or documented exceptions).

## Governance

This constitution supersedes all other development practices in the Citasalud Service project. All contributors must understand and comply with these principles.

**Amendment Process**: Changes to this constitution require:
- Documented rationale and impact analysis.
- Discussion and approval by the development team lead.
- A new constitution version (semantic versioning applied).
- Git commit with clear amendment message and version bump.

**Compliance Review**: Constitution compliance is verified:
- In every pull request via automated checks and code review.
- In sprint retrospectives and architecture reviews.
- Violations trigger re-education or process improvements.

**Guidance File**: Developers refer to `.specify/development-guidance.md` for runtime implementation details and context-specific decisions.

**Version**: 1.0.3 | **Ratified**: 2026-07-05 | **Last Amended**: 2026-07-05
