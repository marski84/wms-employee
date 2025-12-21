# Employee Module

Moduł zarządzania użytkownikami i departamentami w systemie WMS.

## Odpowiedzialność modułu

Employee-module odpowiada za **operacje CRUD** na użytkownikach (pracownikach) i departamentach w systemie magazynowym.
Moduł jest częścią architektury modularnego monolitu (modular monolith).

## Architektura

### Model danych

#### User (Użytkownik/Pracownik)

- `id` (UUID) - unikalny identyfikator
- `name`, `surname` - imię i nazwisko
- `email` - adres email (unikalny)
- `phoneNumber` - numer telefonu
- `password` - hash hasła (BCrypt)
- `jobTitle` - stanowisko
- `role` - rola w systemie (EMPLOYEE, MANAGER, ADMIN)
- `status` - status dostępności (AVAILABLE, BUSY, SICK_LEAVE, HOLIDAY)
- `department` - powiązanie z departamentem
- `createdAt`, `updatedAt` - znaczniki czasowe

#### Department (Departament)

- `id` (UUID) - unikalny identyfikator
- `name` - nazwa departamentu (unikalna)
- `manager` - menadżer departamentu (relacja 1:1 z User)
- `employees` - pracownicy departamentu (relacja 1:N)

### Struktura pakietów

```
employee/
├── config/             # Konfiguracja (SecurityConfig, PasswordEncoder)
├── controller/         # Kontrolery REST (UserController, DepartmentController)
├── dto/                # Data Transfer Objects
│   └── validation/     # Custom validatory
├── exception/          # Custom wyjątki biznesowe
│   └── handler/        # Globalny handler wyjątków
├── model/              # Encje JPA
│   └── enumeration/    # Enumy (EmployeeRole, EmployeeStatus)
├── repository/         # Repozytoria Spring Data JPA
└── service/            # Warstwa logiki biznesowej
```

## API Endpoints

### User Management

#### Tworzenie użytkownika

```http
POST /api/users
Content-Type: application/json

{
  "name": "Jan",
  "surname": "Kowalski",
  "email": "jan.kowalski@example.com",
  "phoneNumber": "+48123456789",
  "password": "securePassword123",
  "confirmPassword": "securePassword123",
  "jobTitle": "Software Engineer",
  "role": "EMPLOYEE",
  "status": "AVAILABLE",
  "departmentId": "uuid-here"
}
```

#### Pobieranie wszystkich użytkowników

```http
GET /api/users
```

#### Pobieranie użytkownika po ID

```http
GET /api/users/{userId}
```

#### Aktualizacja użytkownika

```http
PUT /api/users/{userId}
Content-Type: application/json

{
  "name": "Jan",
  "jobTitle": "Senior Software Engineer",
  "role": "MANAGER",
  "status": "BUSY"
}
```

#### Usuwanie użytkownika

```http
DELETE /api/users/{userId}
```

### Department Management

#### Tworzenie departamentu

```http
POST /api/departments
Content-Type: application/json

{
  "name": "IT Department",
  "managerId": "uuid-here"
}
```

#### Pobieranie wszystkich departamentów

```http
GET /api/departments
```

#### Pobieranie departamentu po ID

```http
GET /api/departments/{departmentId}
```

#### Aktualizacja departamentu

```http
PUT /api/departments/{departmentId}
Content-Type: application/json

{
  "name": "IT & DevOps Department",
  "managerId": "uuid-here"
}
```

#### Usuwanie departamentu

```http
DELETE /api/departments/{departmentId}
```

## Migracje bazy danych

Moduł zawiera migrację Liquibase w `src/main/resources/db/changelog/001_initial_schema.sql`:

- Tworzenie typów ENUM: `employee_role_enum`, `employee_status_enum`
- Tabela `users` z wszystkimi polami użytkownika
- Tabela `departments` z powiązaniem do menadżera
- Foreign keys z ON DELETE SET NULL
- Indeksy dla wydajności zapytań

### Uruchomienie migracji

```bash
# Build Liquibase image
cd src/main/resources/db
docker build -f Dockerfile -t employee-module-db .

# Apply migrations
docker run --rm \
  --network=wms-employee_microservices-network \
  --name liquibase-employee-module-run \
  employee-module-db update \
  --defaultsFile=/liquibase/liquibase-employee.properties
```

## Bezpieczeństwo

- **Hashowanie haseł**: Używa BCrypt z domyślną siłą (10 rounds)
- **Walidacja**: Bean Validation z custom validatorami (np. @PasswordMatches)
- **Ochrona przed duplikatami**: Sprawdzanie unikalności email i nazw departamentów
- **Brak eksponowania haseł**: UserDto nie zawiera pola password

## Walidacja

### CreateUserDto

- `password` i `confirmPassword` muszą być identyczne (@PasswordMatches)
- Email musi być prawidłowy (@Email)
- Hasło min. 8 znaków (@Size)
- Numer telefonu zgodny z formatem międzynarodowym (@Pattern)

### Bean Validation

Wszystkie DTOs używają adnotacji Jakarta Validation:

- `@NotBlank` - pole nie może być puste
- `@Email` - walidacja formatu email
- `@Size` - ograniczenie długości
- `@Pattern` - regex dla numeru telefonu

## Testowanie

### Unit testy

```bash
mvn test -Dtest=UserServiceTest
```

Testy obejmują:

- ✅ Tworzenie użytkownika - happy path
- ✅ Obsługa duplikatu email
- ✅ Walidacja nieistniejącego departamentu
- ✅ Pobieranie użytkownika po ID
- ✅ Aktualizacja użytkownika
- ✅ Usuwanie użytkownika
- ✅ Obsługa wyjątków

## Obsługa błędów

Moduł używa globalnego handlera wyjątków (`GlobalExceptionHandler`) zwracającego spójne odpowiedzi błędów:

### Przykładowa odpowiedź błędu

```json
{
  "timestamp": "2025-11-29T16:30:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "User not found with ID: 123e4567-e89b-12d3-a456-426614174000",
  "path": "/api/users/123e4567-e89b-12d3-a456-426614174000"
}
```

### Kody HTTP

- `200 OK` - Sukces (GET, PUT)
- `201 CREATED` - Zasób utworzony (POST)
- `204 NO CONTENT` - Sukces bez treści (DELETE)
- `400 BAD REQUEST` - Błąd walidacji
- `404 NOT FOUND` - Zasób nie znaleziony
- `409 CONFLICT` - Konflikt (duplikat email/nazwy)
- `500 INTERNAL SERVER ERROR` - Nieoczekiwany błąd

## Build

```bash
# Kompilacja
mvn clean compile

# Testy
mvn test

# Pakowanie
mvn package
```

## Integracja z główną aplikacją

Aby zintegrować ten moduł z aplikacją główną (`wms-app`), dodaj zależność do `wms-app/pom.xml`:

```xml
<dependency>
    <groupId>org.localhost</groupId>
    <artifactId>employee-module</artifactId>
    <version>${project.version}</version>
</dependency>
```

## Technologie

- **Java 17**
- **Spring Boot 3.1.4**
- **Spring Data JPA** - warstwa dostępu do danych
- **Spring Security** - BCrypt password encoding
- **Jakarta Validation** - walidacja DTOs
- **Liquibase** - migracje bazy danych
- **PostgreSQL 17** - baza danych
- **Lombok** - redukcja boilerplate
- **JUnit 5** - framework testowy
- **Mockito** - mockowanie w testach
- **AssertJ** - fluent assertions

## Best Practices zastosowane w module

1. ✅ **Command-Query Separation**: Osobne metody dla odczytu (readOnly=true) i zapisu
2. ✅ **Constructor Injection**: Używanie `@RequiredArgsConstructor` zamiast field injection
3. ✅ **Immutable DTOs**: Używanie Java `record` types
4. ✅ **N+1 Prevention**: Fetch joins w repository queries
5. ✅ **Transaction Management**: `@Transactional` na poziomie serwisu
6. ✅ **Bean Validation**: `@Valid` zamiast ręcznej walidacji
7. ✅ **Exception Handling**: Centralizacja w `@RestControllerAdvice`
8. ✅ **Password Security**: BCrypt hashing, brak eksponowania w API
9. ✅ **Logging**: SLF4J z odpowiednimi poziomami (info, debug, error)
10. ✅ **Unit Testing**: Mockito dla izolacji testów