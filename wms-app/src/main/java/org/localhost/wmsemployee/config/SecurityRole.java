package org.localhost.wmsemployee.config;

import lombok.Getter;

/**
 * Defines user roles as they appear in Auth0 JWT token 'user_roles' claim.
 * Each role is prefixed with 'SCOPE_' when used in Spring Security authorities.
 * <p>
 * Usage in @PreAuthorize:
 * - @PreAuthorize("hasAuthority('" + SecurityRole.ADMIN_AUTHORITY + "')")
 * - @PreAuthorize(SecurityRole.ADMIN_OR_SUPERVISOR)
 */
@Getter
public enum SecurityRole {
    ADMIN("admin", "SCOPE_admin"),
    SUPERVISOR("supervisor", "SCOPE_supervisor"),
    EMPLOYEE("employee", "SCOPE_user"),
    HR("hr", "SCOPE_hr");

    // Pre-built authority constants for common use cases
    public static final String ADMIN_AUTHORITY = "SCOPE_admin";
    public static final String SUPERVISOR_AUTHORITY = "SCOPE_supervisor";
    public static final String EMPLOYEE_AUTHORITY = "SCOPE_user";
    public static final String HR_AUTHORITY = "SCOPE_hr";
    // Pre-built SpEL expressions for @PreAuthorize
    public static final String ADMIN_OR_SUPERVISOR =
            "hasAuthority('" + ADMIN_AUTHORITY + "') || hasAuthority('" + SUPERVISOR_AUTHORITY + "')";
    public static final String ADMIN_OR_HR =
            "hasAuthority('" + ADMIN_AUTHORITY + "') || hasAuthority('" + HR_AUTHORITY + "')";
    public static final String ADMIN_ONLY =
            "hasAuthority('" + ADMIN_AUTHORITY + "')";
    public static final String SUPERVISOR_OR_ADMIN =
            "hasAuthority('" + SUPERVISOR_AUTHORITY + "') || hasAuthority('" + ADMIN_AUTHORITY + "')";
    private final String auth0RoleName;  // Value in Auth0 'user_roles' claim
    private final String springAuthority; // Value for Spring Security @PreAuthorize

    SecurityRole(String auth0RoleName, String springAuthority) {
        this.auth0RoleName = auth0RoleName;
        this.springAuthority = springAuthority;
    }

    /**
     * Check if user has a specific role (case-insensitive).
     */
    public static SecurityRole fromAuth0Role(String roleName) {
        for (SecurityRole role : SecurityRole.values()) {
            if (role.auth0RoleName.equalsIgnoreCase(roleName)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Unknown role: " + roleName);
    }

    /**
     * Get the Spring Security authority string for use in @PreAuthorize.
     * Use this when building dynamic SpEL expressions.
     */
    public String getAuthority() {
        return springAuthority;
    }
}
