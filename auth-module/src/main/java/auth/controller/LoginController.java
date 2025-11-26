package auth.controller;

import auth.dto.login.EmailLoginRequest;
import auth.dto.login.TokenResponseDto;
import auth.service.LoginService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for handling API-based user authentication operations.
 * This controller handles password grant flow for API/mobile clients using
 * OAuth2 Resource Owner Password Credentials flow with Auth0.
 *
 * <p>Authentication Flow:
 * <ol>
 *   <li>Client sends email and password to /api/auth/token endpoint</li>
 *   <li>LoginService authenticates with Auth0 using password grant flow</li>
 *   <li>Auth0 returns JWT tokens (access_token, id_token, expires_in)</li>
 *   <li>Client uses access_token in Authorization header for subsequent requests</li>
 * </ol>
 * </p>
 *
 * <p>Security: All endpoints return identical error messages for invalid credentials
 * to prevent user enumeration attacks.
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "API authentication endpoints for obtaining JWT tokens")
@Slf4j
public class LoginController {

    private final LoginService loginService;

    public LoginController(LoginService loginService) {
        this.loginService = loginService;
    }

    /**
     * Authenticate user and obtain JWT tokens.
     *
     * <p>This endpoint implements OAuth2 Resource Owner Password Credentials flow.
     * It is designed for API clients, mobile applications, and single-page applications
     * that need to obtain tokens programmatically.
     *
     * <p>Response tokens:
     * <ul>
     *   <li><strong>access_token</strong>: JWT token used for API authorization (includes permissions as 'permissions' claim)</li>
     *   <li><strong>id_token</strong>: JWT token containing user identity information</li>
     *   <li><strong>token_type</strong>: Always "Bearer"</li>
     *   <li><strong>expires_in</strong>: Token expiration time in seconds</li>
     * </ul>
     *
     * <p>Usage: Include the access_token in the Authorization header of subsequent requests:
     * <code>Authorization: Bearer {access_token}</code>
     *
     * @param emailLoginRequest The login request containing email and password
     * @return ResponseEntity with TokenResponseDto containing JWT tokens
     */
    @PostMapping("/token")
    @Operation(
            summary = "Authenticate user and get JWT tokens",
            description = "Authenticates user with email and password using Auth0 password grant flow. " +
                    "Returns access token for API authorization and id token for user identity."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully authenticated. Returns JWT tokens.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TokenResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request format (missing fields or invalid data types)"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid credentials (email or password incorrect). " +
                            "Note: Same error message returned for both invalid email and password to prevent user enumeration."
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Auth0 service unavailable or internal server error"
            )
    })
    public ResponseEntity<TokenResponseDto> getToken(
            @Valid @RequestBody EmailLoginRequest emailLoginRequest) {

        log.debug("Token request received for email: {}", emailLoginRequest.getEmail());

        TokenResponseDto tokenResponse = loginService.handleApiLogin(
                emailLoginRequest.getEmail(),
                emailLoginRequest.getPassword()
        );

        return ResponseEntity.ok(tokenResponse);
    }
}