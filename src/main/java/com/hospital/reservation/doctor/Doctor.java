package com.hospital.reservation.doctor;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(
        name = "doctors",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_doctors_department_name",
                columnNames = {"department", "name"}
        ),
        indexes = @Index(name = "idx_doctors_department_active", columnList = "department, active")
)
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private Department department;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false)
    private LocalDate birthDate;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "doctor_available_days",
            joinColumns = @JoinColumn(name = "doctor_id"),
            uniqueConstraints = @UniqueConstraint(
                    name = "uk_doctor_available_days",
                    columnNames = {"doctor_id", "day_of_week"}
            )
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false, length = 10)
    private Set<DayOfWeek> availableDays = EnumSet.noneOf(DayOfWeek.class);

    @Enumerated(EnumType.STRING)
    @Column(name = "active", nullable = false, length = 1)
    private DoctorEmploymentStatus employmentStatus;

    @Column(nullable = false)
    private int displayOrder;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    protected Doctor() {
    }

    private Doctor(
            Department department,
            String name,
            LocalDate birthDate,
            Set<DayOfWeek> availableDays,
            int displayOrder
    ) {
        this.department = Objects.requireNonNull(department);
        this.name = Objects.requireNonNull(name);
        this.birthDate = Objects.requireNonNull(birthDate);
        if (availableDays == null || availableDays.isEmpty()) {
            throw new IllegalArgumentException("진료 가능 요일은 한 개 이상이어야 합니다.");
        }
        this.availableDays = EnumSet.copyOf(availableDays);
        this.employmentStatus = DoctorEmploymentStatus.Y;
        this.displayOrder = displayOrder;
    }

    public static Doctor create(
            Department department,
            String name,
            LocalDate birthDate,
            Set<DayOfWeek> availableDays,
            int displayOrder
    ) {
        return new Doctor(department, name, birthDate, availableDays, displayOrder);
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

    public Department getDepartment() {
        return department;
    }

    public String getName() {
        return name;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public Set<DayOfWeek> getAvailableDays() {
        return Collections.unmodifiableSet(availableDays);
    }

    public DoctorEmploymentStatus getEmploymentStatus() {
        return employmentStatus;
    }

    public boolean isAvailableForReservation() {
        return employmentStatus == DoctorEmploymentStatus.Y;
    }

    public void changeEmploymentStatus(DoctorEmploymentStatus employmentStatus) {
        this.employmentStatus = Objects.requireNonNull(employmentStatus);
    }

    public void update(
            Department department,
            String name,
            LocalDate birthDate,
            Set<DayOfWeek> availableDays,
            DoctorEmploymentStatus employmentStatus,
            int displayOrder
    ) {
        if (availableDays == null || availableDays.isEmpty()) {
            throw new IllegalArgumentException("진료 가능 요일은 한 개 이상이어야 합니다.");
        }
        this.department = Objects.requireNonNull(department);
        this.name = Objects.requireNonNull(name).trim();
        this.birthDate = Objects.requireNonNull(birthDate);
        this.availableDays = EnumSet.copyOf(availableDays);
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
