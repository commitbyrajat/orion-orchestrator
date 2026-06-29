# MSME Overdraft Eligibility System

This Maven project was generated with `mvn archetype:generate` and converted into a Spring Boot 3 application. It simulates a bank-style MSME onboarding and overdraft eligibility engine using PAN, UDYAM, GST, bank account, and transaction data.

## Architecture

Layered flow:

```text
controllers -> services -> repositories -> HSQLDB
```

Packages:

```text
controller   REST APIs
service      business workflows and eligibility scoring
repository   Spring Data JPA repositories
entity       normalized onboarding domain model
dto          API response records
mapper       entity-to-DTO mapping
config       OpenAPI, clock, HSQL console hook
exception    ControllerAdvice and domain exceptions
util         PAN validation
```

Controllers never access repositories directly.

## Entity Relationship Diagram

```text
PanHolder 1 ── 0..1 UdyamRegistration
PanHolder 1 ── 0..N BankAccount
UdyamRegistration 1 ── 0..N GstRegistration
UdyamRegistration 1 ── 0..N BusinessLocation
BankAccount 1 ── 0..N AccountTransaction
```

PAN is modeled as the primary onboarding identity. UDYAM is linked 1:1 to the PAN holder, GST registrations are linked through UDYAM, and bank accounts are linked directly to the PAN holder.

## Run

```bash
mvn spring-boot:run
```

Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

OpenAPI JSON:

```text
http://localhost:8080/v3/api-docs
```

## Maven Commands

```bash
mvn test
mvn package
mvn spring-boot:run
```

## APIs

### KYC

```bash
curl -s http://localhost:8080/api/kyc/pan/ABCDE1234F | jq
```

Returns complete UDYAM details for the PAN. Use `kycId` from this response when calling overdraft eligibility.

### GST

```bash
curl -s http://localhost:8080/api/gst/pan/ABCDE1234F | jq
```

Returns GST registrations discovered through UDYAM details. Use `gstNumber` from this response when calling overdraft eligibility.

### Accounts

```bash
curl -s http://localhost:8080/api/accounts/pan/ABCDE1234F | jq
```

Returns all bank accounts and all transactions. Use one or more `accountId` values from this response when calling overdraft eligibility.

### Overdraft Eligibility

```bash
curl -s -X POST http://localhost:8080/api/overdraft/evaluate \
  -H "Content-Type: application/json" \
  -d '{
    "kycId": 1,
    "gstNumber": "27ABCDE1234F1Z5",
    "accountIds": [1, 2]
  }' | jq
```

Low-score and missing-data scenarios:

```bash
curl -s -X POST http://localhost:8080/api/overdraft/evaluate \
  -H "Content-Type: application/json" \
  -d '{"kycId":3,"gstNumber":"07LOWTR1234A1Z7","accountIds":[5]}' | jq

curl -s -X POST http://localhost:8080/api/overdraft/evaluate \
  -H "Content-Type: application/json" \
  -d '{"kycId":5,"gstNumber":"27ABCDE1234F1Z5","accountIds":[7]}' | jq

curl -s -X POST http://localhost:8080/api/overdraft/evaluate \
  -H "Content-Type: application/json" \
  -d '{"kycId":6,"gstNumber":"29NOGST1234E1Z5","accountIds":[8]}' | jq

curl -s -X POST http://localhost:8080/api/overdraft/evaluate \
  -H "Content-Type: application/json" \
  -d '{"kycId":7,"gstNumber":"33LOWBA1234F1Z9","accountIds":[9]}' | jq
```

Sample eligible response:

```json
{
  "eligible": true,
  "score": 100,
  "maximumEligibleAmount": 7150000.00,
  "reasons": [
    "+20 average monthly balance is at least INR 2,00,000.",
    "+20 active GST annual turnover is at least INR 50 lakh.",
    "+10 business age is at least 2 years.",
    "+10 active current account found.",
    "+15 more than 50 credit transactions in the last 6 months.",
    "+10 no bank account has a negative balance.",
    "+15 average monthly credits are at least INR 5 lakh."
  ]
}
```

## Eligibility Rules

Immediate rejection:

- No UDYAM registration.
- No GST registration.

Weighted score:

| Rule | Points |
| --- | ---: |
| Average monthly balance >= INR 2,00,000 | 20 |
| Annual GST turnover >= INR 50 lakh | 20 |
| Business age >= 2 years | 10 |
| Active current account | 10 |
| Credit transactions in last 6 months > 50 | 15 |
| No account with negative balance | 10 |
| Average monthly credits >= INR 5 lakh | 15 |

Eligibility threshold:

```text
score >= 70
```

Maximum overdraft amount:

```text
min(20% annual turnover, 6 * average monthly credits)
```

## Seed Data

Seed data is loaded with Spring Boot's basic SQL script initialization from:

```text
src/main/resources/data.sql
```

Hibernate creates the in-memory schema first, then Spring Boot runs `data.sql` because the application sets:

```yaml
spring.jpa.defer-datasource-initialization: true
spring.sql.init.mode: always
```

Eligible PANs:

- `ABCDE1234F`
- `PQRST6789L`

Not eligible, complete but weak financials:

- `LOWTR1234A`
- `POORB5678C`

Missing or failing data:

- `NOUSE1234D` no UDYAM.
- `NOGST1234E` UDYAM but no GST.
- `LOWBA1234F` complete data but weak average balance and credit profile.

Each account is seeded with 20 to 50 realistic transactions using narrations such as GST Payment, Salary, NEFT, RTGS, Vendor Payment, Cash Deposit, UPI Collection, Loan EMI, and Interest Credit.

## Database

HSQLDB runs in memory:

```yaml
spring.datasource.url: jdbc:hsqldb:mem:msmedb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
spring.jpa.hibernate.ddl-auto: create-drop
spring.jpa.show-sql: true
```

SQL and bind values are logged through:

```yaml
logging.level.org.hibernate.SQL: DEBUG
logging.level.org.hibernate.orm.jdbc.bind: TRACE
```

## HSQL Console

HSQLDB does not provide a Spring Boot web console like H2. This app includes a local GUI console hook for developer machines:

```bash
mvn spring-boot:run -Dspring-boot.run.arguments=--app.hsqldb.console.enabled=true
```

The GUI is disabled by default and skipped automatically in headless environments. Swagger and curl work without enabling it.

## Validation

PAN path variables are validated with:

```text
[A-Z]{5}[0-9]{4}[A-Z]{1}
```

Invalid PANs return HTTP 400.
