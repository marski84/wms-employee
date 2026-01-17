# Backend User Stories for Resource Management Application

This document outlines the backend user stories based on the Product Requirements Document (PRD) and the existing
codebase. The stories are broken down into epics, focusing on the API endpoints, data models, and service-layer logic
required for implementation.

---

## Epic 1: User & Team Management

*Corresponds to PRD Sections 3.1, 5.2 and extends existing `employee-module` functionality.*

### US-B01: Create a new employee with Auth0 integration

**As a** Manager,
**I want** the system to create a corresponding user in Auth0 when I add a new employee,
**So that** the new employee can log in using their credentials.

**Backend Tasks:**

1. **`POST /api/v1/users`**:
    - Extend the existing `UserService.createUser` method.
    - After successfully saving the `User` entity to the local database, make an API call to the Auth0 Management API to
      create a new user with the provided email and a temporary password.
    - Store the returned `auth0_user_id` in the `User` entity. The database schema needs to be updated to include this
      field (a migration script is required).
    - The interaction with Auth0 should be handled in a new service, e.g., `Auth0UserService`.

### US-B02: Update employee role and status

**As a** Manager,
**I want** to update the role and status of an employee in my team,
**So that** their permissions and status are accurately reflected in the system.

**Backend Tasks:**

1. **`PUT /api/v1/users/{id}`**:
    - Enhance the existing `UserService.updateUser` method.
    - Add business logic to validate role transitions (e.g., ensuring a Manager cannot assign the ADMIN role).
    - When a user's role is changed, the corresponding role in the Auth0 user profile should also be updated via the
      Auth0 Management API.
    - When a user's status is changed to `TERMINATED`, their account in Auth0 should be blocked.

### US-B03: Deactivate an employee

**As a** Manager,
**I want** to deactivate an employee by setting their status to `TERMINATED`,
**So that** they can no longer access the application, but their historical data is preserved.

**Backend Tasks:**

1. **`PUT /api/v1/users/{id}` (Status Change)**:
    - In `UserService.updateUser`, when status is updated to `TERMINATED`:
        - The user's access token should be invalidated if possible (e.g., via token revocation).
        - The user's account in Auth0 must be blocked.
    - Ensure that endpoints for data retrieval (e.g., reports) still include data from terminated users.

---

## Epic 2: Task Lifecycle Management

*Corresponds to PRD Sections 3.2, 5.3. This is a new feature set.*

### US-B04: Define a new task template

**As a** Manager or Admin,
**I want** to create a new task template with a title, description, and dynamic fields (text, number, date),
**So that** I can define the structure for daily reports.

**Backend Tasks:**

1. **Data Models:**
    - Create a `TaskTemplate` entity: `id`, `title`, `description`, `createdBy`, `createdAt`.
    - Create a `TaskField` entity: `id`, `label`, `fieldType` (enum: TEXT, NUMBER, DATE), `template` (ManyToOne
      relationship with `TaskTemplate`).
2. **API Endpoints:**
    - **`POST /api/v1/task-templates`**: Creates a new `TaskTemplate` and its associated `TaskField`s.
        - Request Body DTO: `CreateTaskTemplateDto` containing `title`, `description`, and a list of
          `CreateTaskFieldDto` (`label`, `fieldType`).
        - Returns the created `TaskTemplateDto`.
    - **`GET /api/v1/task-templates`**: Returns a list of all available task templates.

### US-B05: Assign a task to a department for a specific day

**As a** Manager or Admin,
**I want** to assign a task template to a department for a specific day,
**So that** employees in that department are notified of their daily task.

**Backend Tasks:**

1. **Data Model:**
    - Create an `AssignedTask` entity: `id`, `taskTemplate` (ManyToOne), `department` (ManyToOne), `assignedDate` (
      Date), `status` (enum: PENDING, LOCKED).
2. **API Endpoint:**
    - **`POST /api/v1/assigned-tasks`**:
        - Request Body DTO: `AssignTaskDto` (`templateId`, `departmentId`, `assignedDate`).
        - The service logic should prevent assigning a task to the same department and day more than once.
        - Returns the created `AssignedTaskDto`.

### US-B06: Lock a task from being edited

**As a** System,
**I want** to lock an `AssignedTask` from being edited once the first submission is received,
**So that** data integrity is maintained across all submissions for that task.

**Backend Tasks:**

1. **Service Logic:**
    - When the first `TaskSubmission` (see US-B07) for a given `AssignedTask` is created, the status of that
      `AssignedTask` must be changed from `PENDING` to `LOCKED`.
    - The `TaskTemplate` associated with a locked `AssignedTask` should no longer be editable or deletable. Add checks
      in the corresponding `TaskTemplate` service methods.

---

## Epic 3: Task Submission & Monitoring

*Corresponds to PRD Sections 3.3, 3.4, 5.4, 5.5. This is a new feature set.*

### US-B07: Submit answers for an assigned task

**As an** Employee,
**I want** to submit my answers for a task assigned to me for the current day,
**So that** my manager can see my completed work.

**Backend Tasks:**

1. **Data Models:**
    - Create a `TaskSubmission` entity: `id`, `assignedTask` (ManyToOne), `submittedBy` (ManyToOne with `User`),
      `submissionDate`.
    - Create a `TaskAnswer` entity: `id`, `submission` (ManyToOne), `taskField` (ManyToOne), `value` (String, to hold
      serialized data for any type).
2. **API Endpoints:**
    - **`GET /api/v1/assigned-tasks/today`**: Returns the list of `AssignedTaskDto` for the logged-in employee's
      department for the current date. The DTO should include submission status (e.g., "PENDING", "SUBMITTED").
    - **`POST /api/v1/submissions`**:
        - Request Body DTO: `CreateSubmissionDto` (`assignedTaskId`, list of `CreateAnswerDto` (`fieldId`, `value`)).
        - The service must validate that the user belongs to the correct department for the `assignedTask` and that a
          submission for that task by this user doesn't already exist.
        - This service will trigger the task locking mechanism (US-B06).

### US-B08: View team's task submissions

**As a** Manager,
**I want** to view a list of all submissions from my team members, with filtering options,
**So that** I can monitor their work.

**Backend Tasks:**

1. **API Endpoint:**
    - **`GET /api/v1/submissions`**:
        - Returns a paginated list of `TaskSubmissionDto` for the manager's department.
        - Support query parameters for filtering: `employeeId`, `assignedTaskId`, `submissionDate`.
        - The `TaskSubmissionDto` should include employee details and the answers provided.

### US-B09: Generate a report of late submissions

**As a** Manager,
**I want** a report of employees who have not submitted their assigned tasks on time,
**So that** I can follow up with them.

**Backend Tasks:**

1. **API Endpoint:**
    - **`GET /api/v1/reports/late-submissions`**:
        - The service logic should find all `AssignedTask`s for the manager's department with an `assignedDate` in the
          past.
        - For each of these, it checks which employees in the department have NOT created a `TaskSubmission`.
        - Returns a list of `LateSubmissionDto` (`employeeName`, `taskTitle`, `assignedDate`).

---

## Epic 4: Notifications

*Corresponds to PRD Sections 3.4, 5.6. This is a new feature set.*

### US-B10: Notify employees of late submissions

**As a** System,
**I want** to send an email notification to employees who did not submit their assigned task by a set time,
**So that** they are reminded to complete their duties.

**Backend Tasks:**

1. **Scheduled Job:**
    - Create a scheduled task (e.g., using `@Scheduled` in Spring) that runs daily (e.g., at 6 PM).
2. **Notification Logic:**
    - The job will invoke a service that finds all late submissions for the current day (similar logic to US-B09).
    - For each late employee, it will construct and send an email. This should be an asynchronous operation.
    - Leverage the existing `MessagingConfig` and a message queue (e.g., RabbitMQ) to decouple the notification sending
      from the main application logic. An `EmailService` will listen to the queue and use `JavaMailSender` or a similar
      utility to send the emails.
