package com.hospital.reservation.employee;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(
        name = "employees",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_employees_department_name",
                columnNames = {"department", "name"}
        ),
        indexes = @Index(
                name = "idx_employees_department_active",
                columnList = "department, active"
        )
)
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private EmployeeDepartment department;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 50)
    private String position;

    @Column(nullable = false)
    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "active", nullable = false, length = 1)
    private EmployeeEmploymentStatus employmentStatus;

    @Column(nullable = false)
    private int displayOrder;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    protected Employee() {
    }

    private Employee(
            EmployeeDepartment department,
            String name,
            String position,
            LocalDate birthDate,
            int displayOrder
    ) {
        this.department = Objects.requireNonNull(department);
        this.name = Objects.requireNonNull(name).trim();
        this.position = Objects.requireNonNull(position).trim();
        this.birthDate = Objects.requireNonNull(birthDate);
        this.employmentStatus = EmployeeEmploymentStatus.Y;
        this.displayOrder = displayOrder;
    }

    public static Employee create(
            EmployeeDepartment department,
            String name,
            String position,
            LocalDate birthDate,
            int displayOrder
    ) {
        return new Employee(department, name, position, birthDate, displayOrder);
    }

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public EmployeeDepartment getDepartment() {
        return department;
    }

    public String getName() {
        return name;
    }

    public String getPosition() {
        return position;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public EmployeeEmploymentStatus getEmploymentStatus() {
        return employmentStatus;
    }

    public void changeEmploymentStatus(EmployeeEmploymentStatus employmentStatus) {
        this.employmentStatus = Objects.requireNonNull(employmentStatus);
    }

    public void update(
            EmployeeDepartment department,
            String name,
            String position,
            LocalDate birthDate,
            EmployeeEmploymentStatus employmentStatus,
            int displayOrder
    ) {
        this.department = Objects.requireNonNull(department);
        this.name = Objects.requireNonNull(name).trim();
        this.position = Objects.requireNonNull(position).trim();
        this.birthDate = Objects.requireNonNull(birthDate);
        this.employmentStatus = Objects.requireNonNull(employmentStatus);
        this.displayOrder = displayOrder;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
