package com.hospital.reservation.reservation;

import com.hospital.reservation.doctor.Doctor;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(
        name = "reservations",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_reservations_number", columnNames = "reservation_number"),
                @UniqueConstraint(
                        name = "uk_reservations_doctor_schedule",
                        columnNames = {"doctor_id", "appointment_date", "appointment_time"}
                )
        },
        indexes = {
                @Index(name = "idx_reservations_date_status", columnList = "appointment_date, status"),
                @Index(name = "idx_reservations_phone", columnList = "phone_number")
        }
)
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reservation_number", nullable = false, updatable = false, length = 20)
    private String reservationNumber;

    @Column(nullable = false, length = 50)
    private String patientName;

    @Column(nullable = false, length = 20)
    private String phoneNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @Column(nullable = false)
    private LocalDate appointmentDate;

    @Column(nullable = false)
    private LocalTime appointmentTime;

    @Column(columnDefinition = "TEXT")
    private String symptom;

    @Column(columnDefinition = "TEXT")
    private String adminMemo;

    @Column(nullable = false)
    private boolean privacyAgreed;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReservationStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    protected Reservation() {
    }

    private Reservation(
            String patientName,
            String phoneNumber,
            Doctor doctor,
            LocalDate appointmentDate,
            LocalTime appointmentTime,
            String symptom,
            boolean privacyAgreed
    ) {
        this.reservationNumber = generateReservationNumber();
        this.patientName = Objects.requireNonNull(patientName);
        this.phoneNumber = Objects.requireNonNull(phoneNumber);
        this.doctor = Objects.requireNonNull(doctor);
        this.appointmentDate = Objects.requireNonNull(appointmentDate);
        this.appointmentTime = Objects.requireNonNull(appointmentTime);
        this.symptom = symptom;
        if (!privacyAgreed) {
            throw new IllegalArgumentException("개인정보 수집 및 이용에 동의해야 합니다.");
        }
        this.privacyAgreed = true;
        this.status = ReservationStatus.REQUESTED;
    }

    public static Reservation create(
            String patientName,
            String phoneNumber,
            Doctor doctor,
            LocalDate appointmentDate,
            LocalTime appointmentTime,
            String symptom,
            boolean privacyAgreed
    ) {
        return new Reservation(
                patientName,
                phoneNumber,
                doctor,
                appointmentDate,
                appointmentTime,
                symptom,
                privacyAgreed
        );
    }

    private String generateReservationNumber() {
        return "RSV-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
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

    public String getReservationNumber() {
        return reservationNumber;
    }

    public String getPatientName() {
        return patientName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public LocalDate getAppointmentDate() {
        return appointmentDate;
    }

    public LocalTime getAppointmentTime() {
        return appointmentTime;
    }

    public String getSymptom() {
        return symptom;
    }

    public String getAdminMemo() {
        return adminMemo;
    }

    public boolean isPrivacyAgreed() {
        return privacyAgreed;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public void changeStatus(ReservationStatus status) {
        this.status = Objects.requireNonNull(status);
    }

    public void changeAdminMemo(String adminMemo) {
        this.adminMemo = adminMemo == null || adminMemo.isBlank() ? null : adminMemo.trim();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
