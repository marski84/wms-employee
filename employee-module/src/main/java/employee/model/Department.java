package employee.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Represents a department in the organization.
 * Each department can have one manager and multiple employees.
 */
@Entity
@Table(name = "departments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    /**
     * The manager is a User assigned to lead this department.
     * Using @OneToOne implies a User can manage only ONE department.
     * If a User can manage multiple departments, change to @ManyToOne.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    @ToString.Exclude
    private User manager;

    /**
     * Bidirectional mapping - allows access to "department.getEmployees()"
     * FetchType.LAZY is crucial for performance.
     */
    @OneToMany(mappedBy = "department", fetch = FetchType.LAZY)
    @Builder.Default
    @ToString.Exclude
    private Set<User> employees = new HashSet<>();
}