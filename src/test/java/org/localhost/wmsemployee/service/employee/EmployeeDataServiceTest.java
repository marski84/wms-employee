package org.localhost.wmsemployee.service.employee;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.localhost.wmsemployee.repository.EmployeeDataRepository;
import org.localhost.wmsemployee.service.auth.model.EmployeeData;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeDataServiceTest {

    @Mock
    private EmployeeDataRepository employeeDataRepository;

    @InjectMocks
    private EmployeeDataService employeeDataService;

    private EmployeeData testEmployee;

    @BeforeEach
    void setUp() {
        testEmployee = EmployeeData.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .userId("auth0|123456")
                .build();
    }

    @Test
    void findByUsername_WhenUserExists_ShouldReturnEmployee() {
        // Given
        String username = "testuser";
        when(employeeDataRepository.findByUsername(username)).thenReturn(Optional.of(testEmployee));

        // When
        Optional<EmployeeData> result = employeeDataService.findByUsername(username);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testEmployee, result.get());
    }

    @Test
    void findByUsername_WhenUserDoesNotExist_ShouldReturnEmpty() {
        // Given
        String username = "nonexistent";
        when(employeeDataRepository.findByUsername(username)).thenReturn(Optional.empty());

        // When
        Optional<EmployeeData> result = employeeDataService.findByUsername(username);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void findByUsername_WhenUsernameIsNull_ShouldReturnEmpty() {
        // When
        Optional<EmployeeData> result = employeeDataService.findByUsername(null);

        // Then
        assertTrue(result.isEmpty());
    }
}
