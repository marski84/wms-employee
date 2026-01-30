package org.localhost.wmsemployee.mapper;

import employee.model.enumeration.EmployeeRole;

/**
 * Utility class for mapping internal EmployeeRole enum values to Auth0 role names.
 * <p>
 * Auth0 role names must match the roles configured in the Auth0 dashboard.
 * Mappings:
 * <ul>
 *   <li>EMPLOYEE → user</li>
 *   <li>MANAGER → supervisor</li>
 *   <li>ADMIN → admin</li>
 * </ul>
 */
public class RoleMapper {

    private RoleMapper() {
        // Utility class - prevent instantiation
    }

    /**
     * Converts an internal EmployeeRole to the corresponding Auth0 role name.
     *
     * @param role The employee role to convert
     * @return The Auth0 role name (e.g., "user", "supervisor", "admin")
     * @throws IllegalArgumentException if the role is not supported for Auth0 sync
     */
    public static String toAuth0RoleName(EmployeeRole role) {
        return switch (role) {
            case EMPLOYEE -> "user";
            case MANAGER -> "supervisor";
            case ADMIN -> "admin";
        };
    }
}
