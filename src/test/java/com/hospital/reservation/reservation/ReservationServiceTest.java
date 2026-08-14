package com.hospital.reservation.reservation;

import com.hospital.reservation.doctor.Department;
import com.hospital.reservation.doctor.Doctor;
import com.hospital.reservation.doctor.DoctorEmploymentStatus;
import com.hospital.reservation.doctor.DoctorRepository;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReservationServiceTest {

    private final ReservationRepository reservationRepository = mock(ReservationRepository.class);
    private final DoctorRepository doctorRepository = mock(DoctorRepository.class);
    private final ReservationService reservationService = new ReservationService(
            reservationRepository,
            doctorRepository
    );

    @Test
    void createsReservationAfterNormalizingInput() {
        Doctor doctor = doctor(DayOfWeek.MONDAY);
        LocalDate nextMonday = next(DayOfWeek.MONDAY);
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(reservationRepository.saveAndFlush(any(Reservation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ReservationResponse response = reservationService.create(request(
                Department.ENT,
                nextMonday,
                LocalTime.of(10, 30),
                "  머리가 아픕니다.  "
        ));

        assertThat(response.reservationNumber()).startsWith("RSV-");
        verify(reservationRepository).saveAndFlush(org.mockito.ArgumentMatchers.argThat(reservation ->
                reservation.getPhoneNumber().equals("01012345678")
                        && reservation.getSymptom().equals("머리가 아픕니다.")
        ));
    }

    @Test
    void rejectsDoctorWhoIsNotCurrentlyEmployed() {
        Doctor doctor = doctor(DayOfWeek.MONDAY);
        doctor.changeEmploymentStatus(DoctorEmploymentStatus.L);
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));

        assertRuleError(
                request(Department.ENT, next(DayOfWeek.MONDAY), LocalTime.of(10, 0), null),
                "DOCTOR_UNAVAILABLE"
        );
        verify(reservationRepository, never()).saveAndFlush(any());
    }

    @Test
    void rejectsDoctorFromAnotherDepartment() {
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor(DayOfWeek.MONDAY)));

        assertRuleError(
                request(Department.INTERNAL_MEDICINE, next(DayOfWeek.MONDAY), LocalTime.of(10, 0), null),
                "DEPARTMENT_DOCTOR_MISMATCH"
        );
    }

    @Test
    void rejectsDateOutsideDoctorsAvailableDays() {
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor(DayOfWeek.MONDAY)));

        assertRuleError(
                request(Department.ENT, next(DayOfWeek.TUESDAY), LocalTime.of(10, 0), null),
                "DOCTOR_NOT_AVAILABLE_ON_DATE"
        );
    }

    @Test
    void rejectsTimeOutsideThirtyMinuteClinicSlots() {
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor(DayOfWeek.MONDAY)));

        assertRuleError(
                request(Department.ENT, next(DayOfWeek.MONDAY), LocalTime.of(10, 10), null),
                "INVALID_APPOINTMENT_TIME"
        );
    }

    @Test
    void rejectsPastAppointment() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor(yesterday.getDayOfWeek())));

        assertRuleError(
                request(Department.ENT, yesterday, LocalTime.of(10, 0), null),
                "PAST_APPOINTMENT"
        );
    }

    @Test
    void rejectsSaturdayAppointmentAtClosingTime() {
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor(DayOfWeek.SATURDAY)));

        assertRuleError(
                request(Department.ENT, next(DayOfWeek.SATURDAY), LocalTime.of(13, 0), null),
                "INVALID_APPOINTMENT_TIME"
        );
    }

    @Test
    void rejectsAlreadyReservedSlotBeforeSaving() {
        Doctor doctor = doctor(DayOfWeek.MONDAY);
        LocalDate date = next(DayOfWeek.MONDAY);
        LocalTime time = LocalTime.of(10, 0);
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(reservationRepository.existsByDoctorIdAndAppointmentDateAndAppointmentTime(null, date, time))
                .thenReturn(true);

        assertRuleError(request(Department.ENT, date, time, null), "RESERVATION_SLOT_ALREADY_TAKEN");
        verify(reservationRepository, never()).saveAndFlush(any());
    }

    @Test
    void convertsDatabaseUniqueConstraintRaceToBusinessError() {
        Doctor doctor = doctor(DayOfWeek.MONDAY);
        LocalDate date = next(DayOfWeek.MONDAY);
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(reservationRepository.saveAndFlush(any(Reservation.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate slot"));

        assertRuleError(
                request(Department.ENT, date, LocalTime.of(10, 0), null),
                "RESERVATION_SLOT_ALREADY_TAKEN"
        );
    }

    private void assertRuleError(ReservationRequest request, String code) {
        assertThatThrownBy(() -> reservationService.create(request))
                .isInstanceOf(ReservationException.class)
                .extracting(exception -> ((ReservationException) exception).getCode())
                .isEqualTo(code);
    }

    private ReservationRequest request(
            Department department,
            LocalDate date,
            LocalTime time,
            String symptom
    ) {
        return new ReservationRequest(
                "홍길동",
                "010-1234-5678",
                department,
                1L,
                date,
                time,
                symptom,
                true
        );
    }

    private Doctor doctor(DayOfWeek availableDay) {
        return Doctor.create(
                Department.ENT,
                "테스트 의사",
                LocalDate.of(1980, 1, 1),
                Set.of(availableDay),
                1
        );
    }

    private LocalDate next(DayOfWeek dayOfWeek) {
        return LocalDate.now().with(TemporalAdjusters.next(dayOfWeek));
    }
}
