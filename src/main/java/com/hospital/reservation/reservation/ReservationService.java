package com.hospital.reservation.reservation;

import com.hospital.reservation.common.PhoneNumberNormalizer;
import com.hospital.reservation.doctor.Doctor;
import com.hospital.reservation.doctor.DoctorRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final DoctorRepository doctorRepository;

    public ReservationService(ReservationRepository reservationRepository, DoctorRepository doctorRepository) {
        this.reservationRepository = reservationRepository;
        this.doctorRepository = doctorRepository;
    }

    @Transactional
    public ReservationResponse create(ReservationRequest request) {
        Doctor doctor = doctorRepository.findById(request.doctorId())
                .orElseThrow(() -> new ReservationException(
                        "DOCTOR_NOT_FOUND",
                        "선택한 의료진을 찾을 수 없습니다.",
                        HttpStatus.NOT_FOUND
                ));

        validateDoctor(request, doctor);
        validateSchedule(request.date(), request.appointmentTime(), doctor);

        if (reservationRepository.existsByDoctorIdAndAppointmentDateAndAppointmentTime(
                doctor.getId(), request.date(), request.appointmentTime())) {
            throw slotAlreadyTaken();
        }

        Reservation reservation = Reservation.create(
                request.name().trim(),
                PhoneNumberNormalizer.normalize(request.phoneNumber()),
                doctor,
                request.date(),
                request.appointmentTime(),
                normalizeSymptom(request.symptom()),
                request.privacyAgreed()
        );

        try {
            reservationRepository.saveAndFlush(reservation);
        } catch (DataIntegrityViolationException exception) {
            throw slotAlreadyTaken();
        }

        return new ReservationResponse(
                reservation.getReservationNumber(),
                "예약 신청이 접수되었습니다. 예약번호는 " + reservation.getReservationNumber() + "입니다."
        );
    }

    private void validateDoctor(ReservationRequest request, Doctor doctor) {
        if (!doctor.isAvailableForReservation()) {
            throw new ReservationException(
                    "DOCTOR_UNAVAILABLE",
                    "현재 예약할 수 없는 의료진입니다.",
                    HttpStatus.CONFLICT
            );
        }
        if (doctor.getDepartment() != request.department()) {
            throw new ReservationException(
                    "DEPARTMENT_DOCTOR_MISMATCH",
                    "선택한 진료과와 의료진이 일치하지 않습니다.",
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    private void validateSchedule(LocalDate date, LocalTime time, Doctor doctor) {
        LocalDateTime appointment = LocalDateTime.of(date, time);
        if (!appointment.isAfter(LocalDateTime.now())) {
            throw new ReservationException(
                    "PAST_APPOINTMENT",
                    "현재 시각 이후의 예약 시간을 선택해 주세요.",
                    HttpStatus.BAD_REQUEST
            );
        }
        if (!doctor.getAvailableDays().contains(date.getDayOfWeek())) {
            throw new ReservationException(
                    "DOCTOR_NOT_AVAILABLE_ON_DATE",
                    "선택한 의료진의 진료 가능 요일이 아닙니다.",
                    HttpStatus.CONFLICT
            );
        }

        LocalTime closingTime = date.getDayOfWeek() == DayOfWeek.SATURDAY
                ? LocalTime.of(13, 0)
                : LocalTime.of(18, 0);
        boolean validMinute = time.getMinute() == 0 || time.getMinute() == 30;
        if (time.isBefore(LocalTime.of(9, 0)) || !time.isBefore(closingTime) || !validMinute || time.getSecond() != 0) {
            throw new ReservationException(
                    "INVALID_APPOINTMENT_TIME",
                    "진료시간 내 30분 단위의 시간을 선택해 주세요.",
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    private String normalizeSymptom(String symptom) {
        return symptom == null || symptom.isBlank() ? null : symptom.trim();
    }

    private ReservationException slotAlreadyTaken() {
        return new ReservationException(
                "RESERVATION_SLOT_ALREADY_TAKEN",
                "선택한 시간은 이미 예약되었습니다. 다른 시간을 선택해 주세요.",
                HttpStatus.CONFLICT
        );
    }
}
