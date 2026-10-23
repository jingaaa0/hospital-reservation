package com.hospital.reservation.reservation;

import com.hospital.reservation.doctor.Doctor;
import org.springframework.http.HttpStatus;

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

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Entity
@Table(
        name = "reservations",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_reservations_number", columnNames = "reservation_number"),
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
        Objects.requireNonNull(status, "변경할 상태는 필수입니다.");
        if(this.status == ReservationStatus.REQUESTED) {
            // 신청 상태에서는 확정 또는 취소만 허용
            if(status != ReservationStatus.CONFIRMED && status != ReservationStatus.CANCELLED) {
                throw new ReservationException("INVALID_RESERVATION_STATUS", "신청 상태에서는 확정 또는 취소만 가능", HttpStatus.CONFLICT);
            }
        } else if (this.status == ReservationStatus.CONFIRMED) {
            // 확정 상태에서는 완료, 취소 또는 미방문만 허용
            if(status != ReservationStatus.COMPLETED && status != ReservationStatus.CANCELLED && status != ReservationStatus.NO_SHOW) {
                throw new ReservationException("INVALID_RESERVATION_STATUS", "확정 상태에서는 완료, 취소 또는 미방문만 가능", HttpStatus.CONFLICT);
            }
        } else {
            // 완료, 취소, 미방문 상태에서는 변경 불가
            throw new ReservationException("INVALID_RESERVATION_STATUS", "종료된 예약은 상태를 변경할 수 없습니다.", HttpStatus.CONFLICT);
        }

        log.info("예약 상태 변경: {} -> {}", this.status, status);
        this.status = status;
        
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
