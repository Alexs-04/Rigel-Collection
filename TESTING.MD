# Unit Tests for Rigel Application

This document describes the unit tests created for the Rigel application.

## Overview

Comprehensive unit tests have been implemented for most of the application's services using:

- **Testing Framework**: JUnit 5 (Jupiter)
- **Mocking**: Mockito 5.7.1
- **Database Testing**: H2 (in-memory)
- **Build Tool**: Gradle

## Test Structure

```
src/test/java/com/korebit/rigel/
├── RigelApplicationTests.java          # context loads with the H2 "test" profile
├── integration/                        # full app + security filter chain + H2, via MockMvc
│   ├── IntegrationTestSupport.java     # login helpers, JSON builder, mocked JavaMailSender
│   ├── AuthFlowIntegrationTest.java    # JWT: login, 401 vs 403, refresh rotation, logout, deactivation
│   └── ApiResponseIntegrationTest.java # HTTP contract: 201/200/204, error body, POS stock flow
└── service/                            # Mockito unit tests (no Spring context)
    ├── jwt/JwtServiceTest.java
    ├── AuthServiceTest.java
    ├── AmountServiceTest.java
    ├── BatchServiceTest.java
    ├── ConsumerServiceTest.java
    ├── ProductServiceTest.java
    ├── PurchaseServiceTest.java
    └── SupplierServiceTest.java
```

### HTTP contract verified by the integration tests

| Operation | Status | Body |
|-----------|--------|------|
| Create (`POST .../add`, `POST /tickets`, `POST /consumer/api/users`) | `201` | created resource DTO |
| Update / state change (`PUT`, `PATCH`) | `200` | updated resource DTO |
| Delete | `204` | empty |
| Validation error | `400` | `{timestamp, status, error, message, correlationId}` |
| Missing/invalid/revoked token | `401` | same error body |
| Valid token, insufficient role | `403` | same error body |
| Not found | `404` | same error body |

## Test Database Configuration

**File**: `src/test/resources/application-test.properties`

- H2 in memory (`MODE=PostgreSQL`); the `rigel` schema is created on connect because the main
  configuration sets `hibernate.default_schema=rigel`.
- `ddl-auto=create-drop`, random port, Base64 test JWT secret.
- `JavaMailSender` is replaced by a Mockito bean, so no email is ever sent.
- `DataInit` seeds `root@root` / `root`, which the integration tests use to log in.

## Running Tests

### Run all tests

```bash
cd rigel
./gradlew test
```

### Run a subset

```bash
./gradlew test --tests ConsumerServiceTest        # one class
./gradlew test --tests "*ServiceTest"             # unit tests only
./gradlew test --tests "*IntegrationTest"         # H2 + MockMvc integration tests
```

### Run with detailed report

```bash
./gradlew test --info
```

### Generate HTML report

```bash
./gradlew test
# Report will be at: build/reports/tests/test/index.html
```

### Run only a specific test method

```bash
./gradlew test --tests ConsumerServiceTest.testCreateUser
```

## Test Features

### Isolation
- Each test is independent
- Mockito is used to isolate dependencies
- No data contamination between tests

### Coverage
- Successful cases (Happy Path)
- Error cases (Sad Path)
- Input validation
- Exception handling
- Edge cases

### Naming
- Format: `test<MethodName><SpecificCase>`
- Clear descriptions with `@DisplayName`

### Structure Example

```java
@Test
@DisplayName("Should throw exception when product name already exists")
void testSaveProductDuplicateName() {
    // Arrange
    when(productRepository.findByName("Test Product"))
        .thenReturn(Optional.of(testProduct));

    // Act & Assert
    assertThrows(IllegalArgumentException.class, 
        () -> productService.saveProduct(productRequest));
    verify(productRepository, never()).save(any());
}
```

## Test Dependencies

Mockito, JUnit 5, MockMvc and json-path come from the Spring Boot test starters declared in
`build.gradle`; H2 is declared as `testImplementation 'com.h2database:h2'`.

## Next Steps for Complete Coverage

The following services can still have additional tests:

1. **TicketService.kt** - unit tests for multi-batch allocation (covered end-to-end only)
2. **DashboardService.kt** - data aggregation
3. **SystemMovementService.kt** - movement logging and filters

## Best Practices Applied

**Descriptive names**: Test names clearly express what is being tested
**Triple AAA**: Arrange-Act-Assert in every test
**Isolation**: Extensive use of mocks
**No shared state**: setUp() reinitializes data in each test
**Verification**: Number of method calls is verified
**Exceptions**: Error cases are tested

## Troubleshooting

### Error: "H2 driver not found"
**Solution**: Run `./gradlew build` first to download dependencies

### Tests failing by timeout
**Solution**: Increase the timeout in gradle.properties:
```properties
org.gradle.jvmargs=-Xmx2048m
```

### Error: "Transaction has already been committed"
**Solution**: Verify that the `@Transactional` annotation is correctly applied in the tests

## Useful Commands

```bash
# Clean and run tests
./gradlew clean test

# Run tests with detailed output
./gradlew test --info

# View report in browser
open build/reports/tests/test/index.html

# Run a single test in isolation
./gradlew test --tests JwtServiceTest

# Run with debugging
./gradlew test --debug-jvm

# Run in parallel (if possible)
./gradlew test --max-workers=4
```

## Code Coverage

To generate a coverage report with JaCoCo:

```bash
./gradlew jacocoTestReport
# Open: build/reports/jacoco/test/html/index.html
```

(Note: JaCoCo requires additional configuration in build.gradle)

## Contributing New Tests

When adding new tests, follow these guidelines:

1. Create `<ServiceName>Test.java` in `src/test/java/com/korebit/rigel/service/`
2. Use `@ExtendWith(MockitoExtension.class)`
3. Apply `@DisplayName` to each test
4. Keep the AAA pattern (Arrange-Act-Assert)
5. Verify interactions with `verify()`
6. Cover both success AND error cases

## Referencias

- [JUnit 5 Documentation](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [H2 Database](https://www.h2database.com/)
- [Spring Boot Testing](https://spring.io/guides/gs/testing-web/)
