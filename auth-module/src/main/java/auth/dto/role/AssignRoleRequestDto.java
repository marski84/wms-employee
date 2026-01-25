package auth.dto.role;

import java.util.List;

/**
 * DTO for assigning roles to a user in Auth0.
 * Used in POST /api/v2/users/{id}/roles requests.
 */
public record AssignRoleRequestDto(
        List<String> roles
) {
    /**
     * Creates a request to assign a single role to a user.
     *
     * @param roleId The Auth0 role ID to assign
     * @return AssignRoleRequestDto with single role
     */
    public static AssignRoleRequestDto of(String roleId) {
        return new AssignRoleRequestDto(List.of(roleId));
    }
}
