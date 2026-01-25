package org.localhost.wmsemployee.service.employee;

import auth.dto.registration.Auth0RegistrationDto;
import employee.dto.CreateUserDto;
import employee.dto.UpdateUserDto;
import employee.dto.UserDto;
import employee.model.enumeration.EmployeeRole;
import employee.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Orchestrates employee registration across Auth0 and local database.
 * Implements the registration flow as per CLAUDE.md architecture:
 * 1. Register user in Auth0
 * 2. Create user in local database
 * 3. Link Auth0 identity with local user
 * 4. Rollback Auth0 if local creation fails
 */
@Slf4j
public class EmployeeCommandService {

    private final Auth0CommandService auth0CommandService;
    private final UserService userService;

    public EmployeeCommandService(Auth0CommandService auth0CommandService, UserService userService) {
        this.auth0CommandService = auth0CommandService;
        this.userService = userService;
    }

    /**
     * Registers a new employee in both Auth0 and local database.
     * Flow:
     * 1. Create user in Auth0 (get auth0UserId)
     * 2. Create user in local DB
     * 3. Link auth0UserId to local user
     * 4. If local DB fails, Auth0 user should be deleted (TODO: implement rollback)
     *
     * @param createUserDto User creation data
     * @return UserDto with created user details
     */
    @Transactional
    public UserDto registerEmployee(CreateUserDto createUserDto) {
        log.info("Starting employee registration for email: {}", createUserDto.email());

        // Step 1: Register in Auth0
        Auth0RegistrationDto auth0Response = auth0CommandService.registerInAuth0(createUserDto);
        String auth0UserId = auth0Response.userId();
        log.info("Auth0 registration successful. Auth0 User ID: {}", auth0UserId);

        try {
            // Step 2: Create user in local database
            UserDto localUser = userService.createUser(createUserDto);
            log.info("Local user created with ID: {}", localUser.id());

            // Step 3: Link Auth0 identity with local user
            UserDto linkedUser = userService.setAuth0UserId(localUser.id(), auth0UserId);
            log.info("Employee registration completed successfully. Local ID: {}, Auth0 ID: {}",
                    linkedUser.id(), auth0UserId);

            return linkedUser;

        } catch (Exception e) {
            log.error("Failed to create local user after Auth0 registration. Auth0 User ID: {}", auth0UserId, e);
            // TODO: Implement Auth0 user deletion on rollback
            // auth0CommandService.deleteAuth0User(auth0UserId);
            throw e;
        }
    }

    /**
     * Updates an employee's role in both Auth0 and the local database.
     * <p>
     * This method coordinates the role update across two systems:
     * 1. Updates the role in Auth0 (affects JWT permissions)
     * 2. Updates the role in local database (affects application data)
     * <p>
     * The update is performed in Auth0 first to ensure permissions are correct
     * before updating the database. If Auth0 fails, the database is not modified.
     * If the database update fails after Auth0 succeeds, the transaction is rolled back.
     * <p>
     * This method can be called from:
     * - REST controllers for manager actions
     * - Future batch operations
     * - Admin user management features
     *
     * @param userId  The local user ID
     * @param newRole The new employee role to assign
     * @return Updated UserDto with new role
     * @throws employee.exception.UserNotFoundException if user not found
     * @throws IllegalStateException                    if user doesn't have Auth0 ID linked
     * @throws auth.exceptions.Auth0PermissionException if Auth0 operation fails
     * @throws auth.exceptions.Auth0ConnectionException if unable to connect to Auth0
     */
    @Transactional
    public UserDto updateEmployeeRole(UUID userId, EmployeeRole newRole) {
        log.info("Starting role update for user {} to role {}", userId, newRole);

        // Step 1: Get Auth0 user ID from local database
        String auth0UserId = userService.getAuth0UserId(userId);
        log.debug("Found Auth0 user ID: {} for local user: {}", auth0UserId, userId);

        // Step 2: Update role in Auth0 first
        // If this fails, the transaction will roll back and database won't be modified
        try {
            auth0CommandService.updateUserRole(auth0UserId, newRole);
            log.info("Successfully updated role in Auth0 for user {}", auth0UserId);
        } catch (Exception e) {
            log.error("Failed to update role in Auth0 for user {}. Database will not be modified.", auth0UserId, e);
            throw e; // Transaction will roll back
        }

        // Step 3: Update role in local database
        // If this fails, @Transactional will roll back the database changes
        // Note: Auth0 role will remain updated - consider implementing compensating transaction
        try {
            UpdateUserDto updateDto = new UpdateUserDto(
                    null,  // name - not updating
                    null,  // surname - not updating
                    null,  // email - not updating
                    null,  // phoneNumber - not updating
                    null,  // jobTitle - not updating
                    newRole,
                    null,  // status - not updating
                    null   // departmentId - not updating
            );

            UserDto updatedUser = userService.updateUser(userId, updateDto);
            log.info("Successfully updated employee role. User: {}, New role: {}", userId, newRole);

            return updatedUser;

        } catch (Exception e) {
            log.error("Failed to update role in local database for user {} after Auth0 update succeeded. " +
                            "INCONSISTENT STATE: Auth0 has role {}, but database update failed. " +
                            "Manual intervention may be required.",
                    userId, newRole, e);
            throw e; // Transaction will roll back database, but Auth0 change persists
        }
    }
}
