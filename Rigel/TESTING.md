# Unit Tests for Rigel Application

This document describes the unit tests created for the Rigel application.

## Overview

Comprehensive unit tests have been implemented for most of the application's services using:

- **Testing Framework**: JUnit 5 (Jupiter)
- **Mocking**: Mockito 5.7.1
- **Database Testing**: H2 (in-memory)
- **Build Tool**: Gradle

## Test Structure

The tests are located in: `src/test/java/com/korebit/rigel/service/`

### Services Covered

1. **JwtServiceTest.java** (8 tests)
   - Generation of JWT and refresh tokens
   - Token validation (valid, expired, invalid)
   - Extract claims
   - Edge case management (consumer without ID)

2. **ConsumerServiceTest.java** (13 tests)
   - CRUD for consumers
   - Search by email and name
   - Validation of duplicate emails
   - Update of consumer status
   - Filter and search functionality

3. **AuthServiceTest.java** (6 tests)
   - Login success
   - Validation of credentials
   - Inactive consumer login
   - Revoque of tokens
   - Management of token expiration

4. **SupplierServiceTest.java** (11 tests)
   - Complete CRUD for suppliers
   - Search by name
   - Validation of duplicate names
   - Exception handling

5. **ProductServiceTest.java** (13 tests)
   - Product CRUD
   - Supplier relationships
   - Add/remove relationships
   - Category validation
   - Duplicate management

6. **AmountServiceTest.java** (15 tests)
   - Creation of amounts
   - Updates and returns
   - Expired amount purchases
   - Date and quantity validation
   - Filtering by folio

7. **BatchServiceTest.java** (11 tests)
   - Batch CRUD
   - Search by product
   - Expiration date validation
   - Quantity management

8. **PurchaseServiceTest.java** (13 tests)
   - Purchase creation
   - Search by product/supplier
   - Batch updates
   - Batch availability logic

## Test Database Configuration

An H2 test profile has been created:

**File**: `src/test/resources/application-test.properties`

Features:
- In-memory database (cleared after each test)
- Automatic DDL: `create-drop`
- SQL logging disabled for better performance
- Random port (0) to avoid conflicts

## Running Tests

### Run all tests

```bash
cd Rigel
./gradlew test
```

### Run tests for a specific service

```bash
# Example: ConsumerService tests
./gradlew test --tests ConsumerServiceTest

# Example: AuthService tests
./gradlew test --tests AuthServiceTest

# Example: Pattern matching
./gradlew test --tests "*ServiceTest"
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

The following dependencies were added to `build.gradle`:

```groovy
testImplementation 'com.h2database:h2'
testImplementation 'org.mockito:mockito-core:5.7.1'
testImplementation 'org.mockito:mockito-junit-jupiter:5.7.1'
```

## Next Steps for Complete Coverage

The following services can still have additional tests:

1. **TicketService.kt** - Tests for complex ticket operations
2. **DashboardService.kt** - Tests for data aggregation
3. **SystemMovementService.kt** - Tests for movement logging
4. **REST Controllers** - Integration tests

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

