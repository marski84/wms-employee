     Based on the audit findings, here's a complete task list organized by priority and functional area. Your codebase is currently at 14% PRD 
     completion. This roadmap will take it to 100% MVP.

     ---
     📋 PHASE 1: CRITICAL REFACTORING & FIXES (Selected Items)

     1.1 Data Model Consistency

     - Fix EmployeeStatus enum mismatch
       - Change MEDICAL_LEAVE → SICK_LEAVE in /domain-module/src/main/java/org/localhost/wmsemployee/model/enumeration/EmployeeStatus.java
       - Update any references in DTOs and service classes
       - Keep REGISTERED status (useful for onboarding workflow)
     - Add missing columns to employee table
       - Create Liquibase migration: 002-add-employee-metadata-columns.sql
       - Add columns: role (VARCHAR/ENUM), status (VARCHAR/ENUM), supervisor_id (FK), team_id (FK to be added later)
       - Add column: terminated_at (TIMESTAMP) for audit trail
       - Create indexes on role, status, team_id for query performance
     - Persist role/status locally (hybrid approach)
       - Update EmployeeData entity to include role, status fields
       - Modify EmployeeCommandService.registerEmployee() to save role/status to both Auth0 AND local DB
       - Keep Auth0 as source of truth, sync to local DB for query performance
       - Document sync strategy in CLAUDE.md

     1.2 Complete Partial Implementations

     - Implement role assignment to Auth0
       - Complete EmployeeCommandService.assignRoleToUser() method (currently empty at line 124)
       - Use Auth0 Management API to assign roles
       - Remove if (true) placeholder condition
       - Add error handling for Auth0 API failures
       - Add rollback logic if role assignment fails
     - Add employee update endpoint (US-003)
       - Create PUT /api/employee/{id} endpoint in EmployeeCommandController
       - Implement EmployeeCommandService.updateEmployee() method
       - Support updating: name, email, role, status, phone, address
       - Sync changes to Auth0 user metadata
       - Add validation for allowed status transitions
       - Restrict managers to updating only their team members (add after teams implemented)
     - Add employee deactivation endpoint (US-004)
       - Create PUT /api/employee/{id}/deactivate endpoint
       - Set status to TERMINATED in local DB
       - Set terminated_at timestamp
       - Block Auth0 user account (prevent login)
       - Preserve historical data (soft delete)
       - Add reactivation endpoint for reversal

     1.3 Code Cleanup

     - Remove obsolete OAuth2 web client configuration
       - Delete OAuth2 client properties from application.properties (lines 42-52)
       - Remove unused spring-boot-starter-oauth2-client dependency if no longer needed
       - Remove Thymeleaf dependencies from pom.xml (if not used for email templates)
       - Update CLAUDE.md to reflect API-only authentication
     - Remove/clarify unused infrastructure
       - Decide on RabbitMQ/NATS usage: keep for notifications OR remove from Docker Compose
       - If keeping: document messaging strategy in CLAUDE.md
       - If removing: delete from docker-compose.yml, remove spring-boot-starter-amqp
       - Remove or implement empty user-module from multi-module structure

     ---
     📋 PHASE 2: TEAM MANAGEMENT FOUNDATION

     2.1 Team Entity & Database

     - Create team table schema
       - Liquibase migration: 003-create-team-tables.sql
       - Table: team (id, name, manager_id FK to employee, created_at, updated_at)
       - Table: team_member (team_id, employee_id, joined_at, left_at) - join table
       - Add unique constraint on team name
       - Create indexes for foreign keys
     - Create Team domain entity
       - New entity: /domain-module/src/main/java/org/localhost/wmsemployee/model/Team.java
       - Fields: id, name, managerId, createdAt, updatedAt
       - JPA annotations with proper relationships
       - Add @ManyToMany relationship to EmployeeData
     - Create TeamMember entity
       - New entity for explicit join table control
       - Fields: teamId, employeeId, joinedAt, leftAt
       - Composite primary key

     2.2 Team Service Layer

     - Create TeamRepository
       - Interface: /wms-app/src/main/java/org/localhost/wmsemployee/repository/TeamRepository.java
       - Extend JpaRepository<Team, Long>
       - Custom queries: findByManagerId(), findTeamMembers()
     - Create TeamService
       - New service: /wms-app/src/main/java/org/localhost/wmsemployee/service/team/TeamService.java
       - Methods: createTeam(), addMemberToTeam(), removeMemberFromTeam(), getTeamMembers()
       - Validation: manager can only manage their own team
       - Use @Transactional

     2.3 Team REST API

     - Create TeamController
       - New controller: /wms-app/src/main/java/org/localhost/wmsemployee/controller/TeamController.java
       - GET /api/team/{id} - Get team details
       - POST /api/team/{id}/members - Add member to team
       - DELETE /api/team/{id}/members/{employeeId} - Remove member
       - GET /api/team/{id}/members - List team members
       - Secure with @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
     - Update employee registration to assign team
       - Add teamId field to EmployeeRegistrationDto
       - Modify registration logic to create TeamMember record
       - Default to manager's team for manager-initiated registrations

     ---
     📋 PHASE 3: TASK MANAGEMENT CORE

     3.1 Task Entity & Dynamic Fields

     - Create task-related tables
       - Liquibase migration: 004-create-task-tables.sql
       - Table: task (id, title, description, created_by FK, created_at, updated_at, is_locked)
       - Table: task_field (id, task_id FK, field_label, field_type, field_order, is_required)
       - Table: task_assignment (id, task_id FK, team_id FK, assigned_date, created_at)
       - Create indexes and foreign key constraints
     - Create FieldType enum
       - New enum: /domain-module/src/main/java/org/localhost/wmsemployee/model/enumeration/FieldType.java
       - Values: TEXT, NUMBER, DATE
     - Create Task entity
       - New entity: /domain-module/src/main/java/org/localhost/wmsemployee/model/Task.java
       - Fields: id, title, description, createdBy, createdAt, updatedAt, isLocked
       - @OneToMany relationship to TaskField
       - @OneToMany relationship to TaskAssignment
     - Create TaskField entity
       - New entity: /domain-module/src/main/java/org/localhost/wmsemployee/model/TaskField.java
       - Fields: id, taskId, fieldLabel, fieldType, fieldOrder, isRequired
       - @ManyToOne back to Task
       - Ordered by fieldOrder
     - Create TaskAssignment entity
       - New entity: /domain-module/src/main/java/org/localhost/wmsemployee/model/TaskAssignment.java
       - Fields: id, taskId, teamId, assignedDate, createdAt
       - @ManyToOne relationships to Task and Team
       - Unique constraint on (taskId, teamId, assignedDate)

     3.2 Task Service Layer

     - Create TaskRepository
       - Interface: /wms-app/src/main/java/org/localhost/wmsemployee/repository/TaskRepository.java
       - Extend JpaRepository<Task, Long>
       - Custom query: findByCreatedByAndIsLocked()
     - Create TaskFieldRepository
       - Interface for task field CRUD
       - Custom query: findByTaskIdOrderByFieldOrder()
     - Create TaskAssignmentRepository
       - Interface for assignments
       - Custom query: findByTeamIdAndAssignedDate(), findByTaskId()
     - Create TaskService
       - New service: /wms-app/src/main/java/org/localhost/wmsemployee/service/task/TaskService.java
       - Method: createTask(TaskCreateDto) - Create task with dynamic fields (US-005)
       - Method: copyTask(Long taskId) - Copy existing task (US-006)
       - Method: assignTaskToTeam(Long taskId, Long teamId, LocalDate date) - Assign (US-007)
       - Method: lockTask(Long taskId) - Lock after first submission (US-008)
       - Method: updateTask(Long taskId, TaskUpdateDto) - Update (check not locked)
       - All methods use @Transactional

     3.3 Task REST API (US-005, US-006, US-007)

     - Create TaskController
       - New controller: /wms-app/src/main/java/org/localhost/wmsemployee/controller/TaskController.java
       - POST /api/task - Create new task with dynamic fields
       - PUT /api/task/{id} - Update task (validate not locked)
       - POST /api/task/{id}/copy - Copy task, append "(kopia)" to title
       - POST /api/task/{id}/assign - Assign task to team with date
       - GET /api/task/{id} - Get task details with fields
       - GET /api/task - List all tasks (filter by createdBy for managers)
       - Secure endpoints: MANAGER or ADMIN roles only
     - Create Task DTOs
       - TaskCreateDto - title, description, List
       - TaskFieldDto - fieldLabel, fieldType, fieldOrder, isRequired
       - TaskAssignDto - teamId, assignedDate
       - TaskResponseDto - full task details with fields and assignments
       - Add Bean Validation annotations

     3.4 Task Validation

     - Implement date field validation
       - Custom validator: @NoFutureDates annotation
       - Validator implementation checks date ≤ today
       - Apply to date-type task fields
       - Frontend: configure datepicker to disable future dates
     - Implement task lock validation
       - Before update/delete: check isLocked flag
       - Throw TaskLockedException if locked
       - Add global exception handler case
       - Return HTTP 409 Conflict with clear message

     ---
     📋 PHASE 4: TASK SUBMISSION SYSTEM

     4.1 Submission Entity & Database

     - Create submission tables
       - Liquibase migration: 005-create-submission-tables.sql
       - Table: task_submission (id, task_assignment_id FK, employee_id FK, submitted_at, status)
       - Table: task_submission_value (id, submission_id FK, task_field_id FK, value_text, value_number, value_date)
       - Indexes on foreign keys and submitted_at
       - Unique constraint on (task_assignment_id, employee_id) - one submission per employee per assignment
     - Create TaskSubmission entity
       - New entity: /domain-module/src/main/java/org/localhost/wmsemployee/model/TaskSubmission.java
       - Fields: id, taskAssignmentId, employeeId, submittedAt, status
       - @OneToMany to TaskSubmissionValue
       - @ManyToOne to TaskAssignment and EmployeeData
     - Create TaskSubmissionValue entity
       - New entity: /domain-module/src/main/java/org/localhost/wmsemployee/model/TaskSubmissionValue.java
       - Fields: id, submissionId, taskFieldId, valueText, valueNumber, valueDate
       - Store value in appropriate column based on field type
       - @ManyToOne back to TaskSubmission and TaskField

     4.2 Submission Service Layer

     - Create TaskSubmissionRepository
       - Interface: /wms-app/src/main/java/org/localhost/wmsemployee/repository/TaskSubmissionRepository.java
       - Custom query: findByEmployeeIdAndSubmittedAtBetween() for history
       - Custom query: findByTaskAssignmentId() for checking if task has submissions
       - Custom query: existsByTaskId() for lock check
     - Create TaskSubmissionService
       - New service: /wms-app/src/main/java/org/localhost/wmsemployee/service/task/TaskSubmissionService.java
       - Method: submitTask(SubmissionDto) - Single transaction submission (US-010)
           - Validate all required fields present
         - Validate field types match (number is numeric, date is valid)
         - Save TaskSubmission + all TaskSubmissionValue records
         - TRIGGER: Call TaskService.lockTask() if this is first submission (US-008)
       - Method: getEmployeeSubmissionHistory(employeeId, pagination) - History (US-011)
       - Method: getSubmissionDetails(submissionId) - Full details for modal
       - Use @Transactional

     4.3 Employee Dashboard (US-009, US-010, US-011)

     - Create EmployeeDashboardController
       - New controller: /wms-app/src/main/java/org/localhost/wmsemployee/controller/EmployeeDashboardController.java
       - GET /api/employee/dashboard/today - Daily tasks for logged-in employee
           - Query task_assignment by employee's team + today's date
         - Include submission status (completed/pending)
         - Return list of TaskResponseDto with submission status
       - GET /api/employee/submissions/history - Submission history with pagination
       - Secure with @PreAuthorize("hasRole('EMPLOYEE')") (or any authenticated user)
     - Create TaskSubmissionController
       - New controller: /wms-app/src/main/java/org/localhost/wmsemployee/controller/TaskSubmissionController.java
       - POST /api/submission - Submit task completion
           - Request body: taskAssignmentId, Map<fieldId, value>
         - Extract employeeId from JWT token (SecurityContext)
         - Validate employee is in assigned team
         - Call TaskSubmissionService.submitTask()
       - GET /api/submission/{id} - Get submission details
       - Secure appropriately (employee sees own, manager sees team's)
     - Create Submission DTOs
       - SubmissionCreateDto - taskAssignmentId, Map<Long, Object> fieldValues
       - SubmissionResponseDto - submission details with field values
       - DailyTaskDto - task info + submission status for dashboard

     ---
     📋 PHASE 5: MANAGER MONITORING & REPORTING

     5.1 Manager Dashboard (US-012)

     - Create ManagerDashboardController
       - New controller: /wms-app/src/main/java/org/localhost/wmsemployee/controller/ManagerDashboardController.java
       - GET /api/manager/team/submissions - List team submissions
           - Query parameters: employeeId, taskId, dateFrom, dateTo, status
         - Filter by manager's team (extract managerId from JWT)
         - Return paginated list with: task title, employee name, submitted date, status
       - GET /api/manager/team/submissions/{id} - Submission details for modal
       - Secure with @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
     - Create ManagerDashboardService
       - New service: /wms-app/src/main/java/org/localhost/wmsemployee/service/manager/ManagerDashboardService.java
       - Method: getTeamSubmissions(managerId, filters, pagination)
           - Build dynamic query based on filters
         - Use Specifications or Criteria API for flexible filtering
       - Method: getSubmissionDetails(submissionId, managerId)
           - Validate manager owns the employee who submitted
         - Return full submission with all field values

     5.2 Delayed Tasks Report (US-013)

     - Create ReportService
       - New service: /wms-app/src/main/java/org/localhost/wmsemployee/service/report/ReportService.java
       - Method: getDelayedTasksReport(managerId, dateFrom, dateTo)
           - Query: find task_assignments where assigned_date < today AND no submission exists
         - Filter by teams managed by this manager
         - Return: task title, employee name, assigned date
       - Method: getTeamProductivityReport() - Future enhancement
     - Add report endpoints to ManagerDashboardController
       - GET /api/manager/reports/delayed-tasks - Delayed tasks report
       - Query parameters: dateFrom, dateTo
       - Return list of DelayedTaskDto (taskTitle, employeeName, assignedDate)

     ---
     📋 PHASE 6: NOTIFICATION SYSTEM (US-014)

     6.1 Email Infrastructure

     - Configure email service
       - Add SMTP properties to application.properties:
           - spring.mail.host, spring.mail.port, spring.mail.username, spring.mail.password
       - Add dependency: spring-boot-starter-mail to pom.xml
       - Configure for Gmail/SendGrid/AWS SES (choose provider)
     - Create NotificationService
       - New service: /wms-app/src/main/java/org/localhost/wmsemployee/service/notification/NotificationService.java
       - Method: sendMissedTaskEmail(employeeEmail, taskTitle, assignedDate)
       - Use JavaMailSender to send emails
       - Create HTML email template with link to application
       - Add error handling and logging
     - Create notification log table
       - Liquibase migration: 006-create-notification-log.sql
       - Table: notification_log (id, employee_id FK, notification_type, sent_at, email, status, error_message)
       - Track all sent notifications for audit
     - Create NotificationLog entity and repository
       - Entity for audit trail
       - Repository to query notification history

     6.2 Scheduled Job (US-014)

     - Enable Spring Scheduling
       - Add @EnableScheduling to main application class
       - Configure timezone in application.properties: spring.task.scheduling.timezone
     - Create ScheduledTaskService
       - New service: /wms-app/src/main/java/org/localhost/wmsemployee/service/scheduled/ScheduledTaskService.java
       - Method: checkMissedTasksAndNotify()
           - Annotate with @Scheduled(cron = "0 0 18 * * MON-FRI", zone = "Europe/Warsaw")
         - Query: find task_assignments for today with no submission
         - For each missing submission, send email via NotificationService
         - Log all notifications to notification_log table
       - Add configuration property for notification time (externalize "18:00")
     - Add notification preferences (future)
       - Placeholder: add email_notifications_enabled to employee table
       - Default to true, allow employees to opt out
       - Check preference before sending

     6.3 Alternative: Message Queue Implementation (Optional)

     - If using RabbitMQ for notifications:
       - Create NotificationQueue configuration
       - Publish message to queue from scheduled job
       - Create consumer to process emails asynchronously
       - Benefit: better fault tolerance, retry logic
       - Requires: keeping RabbitMQ infrastructure from Phase 1 cleanup decision

     ---
     📋 PHASE 7: SECURITY & AUTHORIZATION

     7.1 Team-Based Access Control

     - Create TeamSecurityService
       - New service: /wms-app/src/main/java/org/localhost/wmsemployee/service/security/TeamSecurityService.java
       - Method: isManagerOfTeam(managerId, teamId) - Validate manager owns team
       - Method: isEmployeeInTeam(employeeId, teamId) - Check team membership
       - Method: canManagerAccessEmployee(managerId, employeeId) - Check via team
       - Used in all manager endpoints to enforce team boundaries
     - Add team-based authorization to endpoints
       - Update EmployeeCommandController.updateEmployee() - managers only update their team
       - Update ManagerDashboardController - filter by manager's teams
       - Update TaskController - managers assign tasks only to their teams
       - Use TeamSecurityService in service layer before operations

     7.2 Role-Based Method Security

     - Add method-level security annotations
       - @PreAuthorize on all controller methods
       - Common expressions:
           - hasRole('ADMIN') - Admin only
         - hasAnyRole('MANAGER', 'ADMIN') - Manager or Admin
         - hasRole('EMPLOYEE') - Any employee
       - Enable with @EnableMethodSecurity in SecurityConfig
     - Update SecurityConfig for new endpoints
       - Current config denies all non-API requests (keep this)
       - Add specific matchers for new endpoints:
           - /api/task/** - MANAGER, ADMIN
         - /api/employee/dashboard/** - EMPLOYEE (all roles)
         - /api/manager/** - MANAGER, ADMIN
         - /api/submission/** - EMPLOYEE (all roles)

     ---
     📋 PHASE 8: TESTING & QUALITY

     8.1 Unit Tests

     - Test TeamService
       - Test team creation, member addition/removal
       - Test manager validation
       - Mock TeamRepository
     - Test TaskService
       - Test task creation with dynamic fields
       - Test task copy functionality
       - Test task assignment to team
       - Test task locking after first submission
       - Mock repositories
     - Test TaskSubmissionService
       - Test successful submission with all field types
       - Test validation failures (missing required fields, invalid types)
       - Test transaction rollback on error
       - Test first submission triggers task lock
       - Mock repositories
     - Test NotificationService
       - Test email sending (use mock JavaMailSender)
       - Test error handling for failed sends
       - Test notification logging
     - Test ScheduledTaskService
       - Test identification of missed tasks
       - Test notification triggering
       - Don't rely on cron timing (use manual method call)

     8.2 Integration Tests

     - Test employee registration flow
       - POST to /api/employee
       - Verify Auth0 user created (mock Auth0 client)
       - Verify database record created with role/status
       - Verify team assignment
     - Test task creation and assignment flow
       - Create task with dynamic fields
       - Assign to team
       - Verify assignments visible on employee dashboard
     - Test submission flow
       - Employee submits task
       - Verify submission saved
       - Verify task is locked
       - Verify appears in manager dashboard
     - Test manager dashboard filtering
       - Create multiple submissions
       - Test filtering by employee, date, task
       - Verify manager sees only their team

     8.3 Data Migration Testing

     - Test all Liquibase migrations
       - Run migrations on fresh database
       - Verify all tables created
       - Verify indexes and constraints
       - Test rollback scenarios
       - Test migration on database with existing employee data

     ---
     📋 PHASE 9: DOCUMENTATION & POLISH

     9.1 API Documentation

     - Add Swagger/OpenAPI
       - Add dependency: springdoc-openapi-starter-webmvc-ui
       - Configure OpenAPI info (title, version, description)
       - Add @Operation, @ApiResponse annotations to controllers
       - Document authentication requirements
       - Available at /swagger-ui.html
     - Update CLAUDE.md
       - Add task management architecture section
       - Add team structure explanation
       - Document all new endpoints with curl examples
       - Update technology stack section
       - Add data model diagram (consider using Mermaid)
       - Remove obsolete information (OAuth2 web login)

     9.2 Frontend Integration Guide

     - Create API usage examples
       - Document task creation workflow
       - Document employee submission workflow
       - Document manager monitoring workflow
       - Provide JavaScript/TypeScript examples
       - Document WebSocket integration for real-time updates (future enhancement)

     9.3 Deployment & Operations

     - Update docker-compose.yml
       - Add mail service (MailHog for development testing)
       - Document required environment variables
       - Update README with setup instructions
     - Create deployment checklist
       - Database migration runbook
       - Auth0 configuration requirements
       - SMTP configuration
       - Timezone configuration for scheduled jobs
       - Environment variable list

     ---
     🎯 ESTIMATED EFFORT BY PHASE

     Phase 1: Critical Refactoring - 1 weekPhase 2: Team Management - 1 weekPhase 3: Task Management Core - 2 weeksPhase 4: Submission System - 1.5 
     weeksPhase 5: Monitoring & Reporting - 1 weekPhase 6: Notifications - 1 weekPhase 7: Security - 0.5 weeksPhase 8: Testing - 1 weekPhase 9: 
     Documentation - 0.5 weeks  

     Total: ~9.5 weeks for complete MVP

     ---
     📊 SUCCESS CRITERIA

     You'll know you're done when:
     ✅ All 14 user stories (US-001 through US-014) are implemented✅ Employee can see daily tasks and submit them✅ Manager can create tasks, assign to
      team, and monitor submissions✅ Automated emails sent for missed tasks at 18:00 daily✅ All critical refactoring items completed✅ Integration 
     tests pass for complete workflows✅ API documentation available via Swagger  

     This list represents the complete gap between your current 14% implementation and the 100% MVP defined in the PRD.

⎿

⏺ Perfect! I've created a comprehensive TODO list with 9 phases and ~100 specific tasks that will take you from 14% to
100% PRD completion.

Let me now create a structured task tracking list for you:

⏺ ✅ TODO List Complete

I've structured 9 implementation phases with high-level tracking. You now have:

What You've Received:

📋 Comprehensive TODO List with:

- 9 phases covering all missing features
- ~100 specific, actionable tasks (not vague goals)
- File references and line numbers for where changes go
- Effort estimates: ~9.5 weeks total (assuming 1 person, full-time)
- Dependencies mapped between phases
- Success criteria for completion

Key Points:

✨ Your hybrid approach (keep Auth0 primary, cache role/status locally):

- Will make the system performant while maintaining Auth0 as source of truth
- Reduces coupling to external service

✨ Addressing all refactoring items upfront:

- Fix status enum mismatch (SICK_LEAVE)
- Complete role assignment implementation
- Persist role/status locally
- Remove dead code

✨ Realistic sequencing:

- Start with data model and refactoring (foundation)
- Build team structure (organizational foundation)
- Implement task management (core value)
- Add submission workflow (employee feature)
- Build monitoring (manager feature)
- Add notifications (compliance feature)
