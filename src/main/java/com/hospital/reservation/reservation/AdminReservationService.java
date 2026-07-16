package com.hospital.reservation.reservation;

import com.hospital.reservation.doctor.Department;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@Transactional(readOnly = true)
public class AdminReservationService {

    private final ReservationRepository reservationRepository;

    public AdminReservationService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    public AdminReservationPageResponse findAll(
            Department department,
            Long doctorId,
            LocalDate date,
            ReservationStatus status,
            String sortBy,
            Sort.Direction direction,
            int page,
            int size
    ) {
        Specification<Reservation> specification = (root, query, builder) -> builder.conjunction();
        if (department != null) {
            specification = specification.and((root, query, builder) ->
                    builder.equal(root.join("doctor", JoinType.INNER).get("department"), department));
        }
        if (doctorId != null) {
            specification = specification.and((root, query, builder) ->
                    builder.equal(root.get("doctor").get("id"), doctorId));
        }
        if (date != null) {
            specification = specification.and((root, query, builder) ->
                    builder.equal(root.get("appointmentDate"), date));
        }
        if (status != null) {
            specification = specification.and((root, query, builder) ->
                    builder.equal(root.get("status"), status));
        }

        PageRequest pageRequest = PageRequest.of(
                Math.max(page, 0),
                Math.clamp(size, 1, 100),
                createSort(sortBy, direction)
        );
        Page<Reservation> reservations = reservationRepository.findAll(specification, pageRequest);
        return new AdminReservationPageResponse(
                reservations.getContent().stream().map(AdminReservationResponse::from).toList(),
                reservations.getNumber(),
                reservations.getSize(),
                reservations.getTotalElements(),
                reservations.getTotalPages()
        );
    }

    @Transactional
    public AdminReservationResponse updateStatus(Long reservationId, ReservationStatusUpdateRequest request) {
        Reservation reservation = findReservation(reservationId);
        reservation.changeStatus(request.status());
        return AdminReservationResponse.from(reservation);
    }

    @Transactional
    public AdminReservationResponse updateAdminMemo(Long reservationId, AdminMemoUpdateRequest request) {
        Reservation reservation = findReservation(reservationId);
        reservation.changeAdminMemo(request.adminMemo());
        return AdminReservationResponse.from(reservation);
    }

    private Reservation findReservation(Long reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationException(
                        "RESERVATION_NOT_FOUND",
                        "예약을 찾을 수 없습니다.",
                        HttpStatus.NOT_FOUND
                ));
    }

    private Sort createSort(String sortBy, Sort.Direction direction) {
        Sort sort = switch (sortBy) {
            case "department" -> Sort.by(direction, "doctor.department");
            case "doctor" -> Sort.by(direction, "doctor.name");
            case "status" -> Sort.by(direction, "status");
            default -> Sort.by(direction, "appointmentDate", "appointmentTime");
        };
        return sort.and(Sort.by(Sort.Direction.DESC, "id"));
    }
}
