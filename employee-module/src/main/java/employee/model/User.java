package employee.model;

import employee.model.enumeration.EmployeeRole;
import employee.model.enumeration.EmployeeStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Represents a user/employee in the system.
 * Contains both authentication data (password hash) and employee profile information.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 100)
    private String surname;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "phone_number")
    private String phoneNumber;

    /**
     * Security: Only password hash is stored, never plain text.
     * Should be hashed using BCrypt or similar algorithm before persistence.
     */
    @Column(nullable = false)
    private String password;

    @Column(name = "job_title")
    private String jobTitle;

    @Column(name = "auth_user_id")
    private String authUserID;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private EmployeeRole role = EmployeeRole.EMPLOYEE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private EmployeeStatus status = EmployeeStatus.AVAILABLE;

    /**
     * Relation: Employee belongs to a Department.
     * FetchType.LAZY is crucial for performance - department is loaded only when accessed.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    @ToString.Exclude
    private Department department;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private ZonedDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;
}