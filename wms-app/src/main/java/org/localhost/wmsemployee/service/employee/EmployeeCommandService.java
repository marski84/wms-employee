package org.localhost.wmsemployee.service.employee;

import auth.dto.registration.Auth0RegistrationDto;
import employee.dto.CreateUserDto;
import employee.dto.UserDto;
import employee.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Orchestrates employee registration across Auth0 and local database.
 * Implements the registration flow as per CLAUDE.md architecture:
 * 1. Register user in Auth0
 * 2. Create user in local database
 * 3. Link Auth0 identity with local user
 * 4. Rollback Auth0 if local creation fails
 */
@Service
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
}
