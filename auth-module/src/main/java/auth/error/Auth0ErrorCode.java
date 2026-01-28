package auth.error;

import lombok.Getter;

/**
 * Enum defining standardized error codes and messages for Auth0 integration.
 * Provides consistent error messaging across the application.
 */
@Getter
public enum Auth0ErrorCode {

    USER_ALREADY_EXISTS(
            "USER_ALREADY_EXISTS",
            "User with email %s already exists in Auth0"
    ),

    VALIDATION_ERROR(
            "VALIDATION_ERROR",
            "Failed to create Auth0 user: Invalid data or password requirements not met"
    ),

    PERMISSION_DENIED(
            "PERMISSION_DENIED",
            "Insufficient permissions to create Auth0 user. Check M2M token scopes."
    ),

    SERVICE_UNAVAILABLE(
            "SERVICE_UNAVAILABLE",
            "Auth0 service temporarily unavailable. Please try again later."
    ),

    NETWORK_ERROR(
            "NETWORK_ERROR",
            "Cannot reach Auth0 service. Check network connectivity."
    ),

    UNEXPECTED_ERROR(
            "UNEXPECTED_ERROR",
            "Unexpected error while creating Auth0 user: %s"
    );

    private final String code;
    private final String messageTemplate;

    Auth0ErrorCode(String code, String messageTemplate) {
        this.code = code;
        this.messageTemplate = messageTemplate;
    }

    /**
     * Formats the error message with the provided parameters.
     *
     * @param params Parameters to inject into the message template
     * @return Formatted error message
     */
    public String formatMessage(Object... params) {
        return String.format(messageTemplate, params);
    }

    /**
     * Returns the raw message template without formatting.
     *
     * @return The message template
     */
    public String getMessage() {
        return messageTemplate;
    }
}