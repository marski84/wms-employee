package org.localhost.wmsemployee.service.login;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.localhost.wmsemployee.dto.login.Auth0UserDto;
import org.localhost.wmsemployee.exceptions.AuthenticationFailedException;
import org.localhost.wmsemployee.service.auth.service.Auth0ManagementTokenService;
import org.localhost.wmsemployee.service.employee.EmployeeDataService;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    private final String testEmail = "test@example.com";
    private final String testPassword = "Password123!";
    private final String testAuthToken = "test-auth-token";
    private final String testManagementToken = "test-management-token";
    private final String testUserId = "auth0|123456789";
    @Mock
    private EmployeeDataService employeeDataService;
    @Mock
    private Auth0ManagementTokenService auth0ManagementTokenService;
    @Mock
    private RestTemplate restTemplate;
    @InjectMocks
    private LoginService loginService;

    @BeforeEach
    void setUp() {
        // Set private fields using ReflectionTestUtils
        ReflectionTestUtils.setField(loginService, "domain", "test.auth0.com");
        ReflectionTestUtils.setField(loginService, "clientId", "test-client-id");
        ReflectionTestUtils.setField(loginService, "clientSecret", "test-client-secret");
        ReflectionTestUtils.setField(loginService, "usersEndpoint", "https://test.auth0.com/api/v2/users");
    }

    @Test
    void handleLogin_shouldReturnUserDetails_whenAuthenticationIsSuccessful() {
        // Arrange
        // Mock Auth0 authentication response
        Map<String, Object> authResponse = new HashMap<>();
        authResponse.put("access_token", testAuthToken);
        ResponseEntity<Map> authResponseEntity = new ResponseEntity<>(authResponse, HttpStatus.OK);
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(authResponseEntity);

        // Mock management token
        when(auth0ManagementTokenService.getAccessToken()).thenReturn(testManagementToken);

        // Mock user search response
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("user_id", testUserId);
        Map[] userArray = new Map[]{userMap};
        ResponseEntity<Map[]> userSearchResponse = new ResponseEntity<>(userArray, HttpStatus.OK);
        when(restTemplate.exchange(
                contains("?q=email:"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Map[].class)))
                .thenReturn(userSearchResponse);

        // Mock user details response
        Auth0UserDto expectedUserDto = createTestUserDto();
        ResponseEntity<Auth0UserDto> userDetailsResponse = new ResponseEntity<>(expectedUserDto, HttpStatus.OK);
        when(restTemplate.exchange(
                contains("/" + testUserId),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Auth0UserDto.class)))
                .thenReturn(userDetailsResponse);

        // Act
        Auth0UserDto result = loginService.handleLogin(testEmail, testPassword);

        // Assert
        assertNotNull(result);
        assertEquals(expectedUserDto, result);

        // Verify interactions
        verify(restTemplate).postForEntity(anyString(), any(HttpEntity.class), eq(Map.class));
        verify(auth0ManagementTokenService).getAccessToken();
        verify(restTemplate).exchange(contains("?q=email:"), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map[].class));
        verify(restTemplate).exchange(contains("/" + testUserId), eq(HttpMethod.GET), any(HttpEntity.class), eq(Auth0UserDto.class));
    }

    @Test
    void handleLogin_shouldThrowAuthenticationFailedException_whenCredentialsAreInvalid() {
        // Arrange
        // Mock Auth0 authentication failure
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        // Act & Assert
        Exception exception = assertThrows(AuthenticationFailedException.class, () -> {
            loginService.handleLogin(testEmail, testPassword);
        });

        assertEquals("Invalid credentials", exception.getMessage());

        // Verify interactions
        verify(restTemplate).postForEntity(anyString(), any(HttpEntity.class), eq(Map.class));
        verify(auth0ManagementTokenService, never()).getAccessToken();
    }

    @Test
    void handleLogin_shouldThrowAuthenticationFailedException_whenUserNotFound() {
        // Arrange
        // Mock Auth0 authentication response
        Map<String, Object> authResponse = new HashMap<>();
        authResponse.put("access_token", testAuthToken);
        ResponseEntity<Map> authResponseEntity = new ResponseEntity<>(authResponse, HttpStatus.OK);
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(authResponseEntity);

        // Mock management token
        when(auth0ManagementTokenService.getAccessToken()).thenReturn(testManagementToken);

        // Mock empty user search response
        Map[] emptyUserArray = new Map[0];
        ResponseEntity<Map[]> emptyUserSearchResponse = new ResponseEntity<>(emptyUserArray, HttpStatus.OK);
        when(restTemplate.exchange(
                contains("?q=email:"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Map[].class)))
                .thenReturn(emptyUserSearchResponse);

        // Act & Assert
        Exception exception = assertThrows(AuthenticationFailedException.class, () -> {
            loginService.handleLogin(testEmail, testPassword);
        });

        assertEquals("User not found", exception.getMessage());

        // Verify interactions
        verify(restTemplate).postForEntity(anyString(), any(HttpEntity.class), eq(Map.class));
        verify(auth0ManagementTokenService).getAccessToken();
        verify(restTemplate).exchange(contains("?q=email:"), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map[].class));
    }

    @Test
    void handleLogin_shouldThrowAuthenticationFailedException_whenUserDetailsCannotBeRetrieved() {
        // Arrange
        // Mock Auth0 authentication response
        Map<String, Object> authResponse = new HashMap<>();
        authResponse.put("access_token", testAuthToken);
        ResponseEntity<Map> authResponseEntity = new ResponseEntity<>(authResponse, HttpStatus.OK);
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(authResponseEntity);

        // Mock management token
        when(auth0ManagementTokenService.getAccessToken()).thenReturn(testManagementToken);

        // Mock user search response
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("user_id", testUserId);
        Map[] userArray = new Map[]{userMap};
        ResponseEntity<Map[]> userSearchResponse = new ResponseEntity<>(userArray, HttpStatus.OK);
        when(restTemplate.exchange(
                contains("?q=email:"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Map[].class)))
                .thenReturn(userSearchResponse);

        // Mock null user details response
        ResponseEntity<Auth0UserDto> nullUserDetailsResponse = new ResponseEntity<>(null, HttpStatus.OK);
        when(restTemplate.exchange(
                contains("/" + testUserId),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Auth0UserDto.class)))
                .thenReturn(nullUserDetailsResponse);

        // Act & Assert
        Exception exception = assertThrows(AuthenticationFailedException.class, () -> {
            loginService.handleLogin(testEmail, testPassword);
        });

        assertEquals("Failed to retrieve user details", exception.getMessage());

        // Verify interactions
        verify(restTemplate).postForEntity(anyString(), any(HttpEntity.class), eq(Map.class));
        verify(auth0ManagementTokenService).getAccessToken();
        verify(restTemplate).exchange(contains("?q=email:"), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map[].class));
        verify(restTemplate).exchange(contains("/" + testUserId), eq(HttpMethod.GET), any(HttpEntity.class), eq(Auth0UserDto.class));
    }

    @Test
    void handleLogin_shouldVerifyCorrectHeadersAreSent_whenGettingUserDetails() {
        // Arrange
        // Mock Auth0 authentication response
        Map<String, Object> authResponse = new HashMap<>();
        authResponse.put("access_token", testAuthToken);
        ResponseEntity<Map> authResponseEntity = new ResponseEntity<>(authResponse, HttpStatus.OK);
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(authResponseEntity);

        // Mock management token
        when(auth0ManagementTokenService.getAccessToken()).thenReturn(testManagementToken);

        // Mock user search response
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("user_id", testUserId);
        Map[] userArray = new Map[]{userMap};
        ResponseEntity<Map[]> userSearchResponse = new ResponseEntity<>(userArray, HttpStatus.OK);
        when(restTemplate.exchange(
                contains("?q=email:"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Map[].class)))
                .thenReturn(userSearchResponse);

        // Mock user details response
        Auth0UserDto expectedUserDto = createTestUserDto();
        ResponseEntity<Auth0UserDto> userDetailsResponse = new ResponseEntity<>(expectedUserDto, HttpStatus.OK);
        when(restTemplate.exchange(
                contains("/" + testUserId),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Auth0UserDto.class)))
                .thenReturn(userDetailsResponse);

        // Act
        loginService.handleLogin(testEmail, testPassword);

        // Assert & Verify
        // Verify authentication request
        ArgumentCaptor<HttpEntity> authRequestCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForEntity(contains("/oauth/token"), authRequestCaptor.capture(), eq(Map.class));

        HttpEntity<?> capturedAuthRequest = authRequestCaptor.getValue();
        Map<String, Object> requestBody = (Map<String, Object>) capturedAuthRequest.getBody();
        assertEquals("password", requestBody.get("grant_type"));
        assertEquals(testEmail, requestBody.get("username"));
        assertEquals(testPassword, requestBody.get("password"));

        // Verify user details request
        ArgumentCaptor<HttpEntity> userRequestCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(contains("?q=email:"), eq(HttpMethod.GET), userRequestCaptor.capture(), eq(Map[].class));

        HttpEntity<?> capturedUserRequest = userRequestCaptor.getValue();
        assertEquals("Bearer " + testManagementToken, capturedUserRequest.getHeaders().getFirst("Authorization"));
    }

    @Test
    void handleLogin_shouldPropagateException_whenUnexpectedErrorOccurs() {
        // Arrange
        // Mock Auth0 authentication response
        Map<String, Object> authResponse = new HashMap<>();
        authResponse.put("access_token", testAuthToken);
        ResponseEntity<Map> authResponseEntity = new ResponseEntity<>(authResponse, HttpStatus.OK);
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(authResponseEntity);

        // Mock unexpected error during management token retrieval
        when(auth0ManagementTokenService.getAccessToken())
                .thenThrow(new RuntimeException("Unexpected error"));

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            loginService.handleLogin(testEmail, testPassword);
        });

        assertEquals("Login process failed", exception.getMessage());
        assertNotNull(exception.getCause());
        assertEquals("Unexpected error", exception.getCause().getMessage());

        // Verify interactions
        verify(restTemplate).postForEntity(anyString(), any(HttpEntity.class), eq(Map.class));
        verify(auth0ManagementTokenService).getAccessToken();
    }

    @Test
    void handleLogin_shouldThrowIllegalArgumentException_whenEmailIsNull() {
        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            loginService.handleLogin(null, testPassword);
        });

        assertEquals("Email cannot be null or empty", exception.getMessage());
    }

    @Test
    void handleLogin_shouldThrowIllegalArgumentException_whenEmailIsEmpty() {
        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            loginService.handleLogin("", testPassword);
        });

        assertEquals("Email cannot be null or empty", exception.getMessage());
    }

    @Test
    void handleLogin_shouldThrowIllegalArgumentException_whenEmailFormatIsInvalid() {
        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            loginService.handleLogin("invalid-email", testPassword);
        });

        assertEquals("Invalid email format", exception.getMessage());
    }

    @Test
    void handleLogin_shouldThrowIllegalArgumentException_whenPasswordIsNull() {
        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            loginService.handleLogin(testEmail, null);
        });

        assertEquals("Password must be at least 6 characters long", exception.getMessage());
    }

    @Test
    void handleLogin_shouldThrowIllegalArgumentException_whenPasswordIsTooShort() {
        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            loginService.handleLogin(testEmail, "12345");
        });

        assertEquals("Password must be at least 6 characters long", exception.getMessage());
    }

    private Auth0UserDto createTestUserDto() {
        Auth0UserDto.UserMetadata userMetadata = Auth0UserDto.UserMetadata.builder()
                .phoneNumber("+1234567890")
                .address("123 Main St")
                .city("Anytown")
                .postalCode("12345")
                .country("USA")
                .roleId("1")
                .roleName(org.localhost.wmsemployee.model.eumeration.EmployeeRole.EMPLOYEE)
                .familyName("Doe")
                .build();

        return Auth0UserDto.builder()
                .userId(testUserId)
                .email(testEmail)
                .name("John Doe")
                .nickname("John")
                .username("john.doe")
                .createdAt(ZonedDateTime.now())
                .updatedAt(ZonedDateTime.now())
                .userMetadata(userMetadata)
                .build();
    }
}
