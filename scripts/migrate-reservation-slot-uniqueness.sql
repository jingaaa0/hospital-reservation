BEGIN;

CREATE UNIQUE INDEX IF NOT EXISTS uk_reservations_non_cancelled_schedule
    ON public.reservations (doctor_id, appointment_date, appointment_time)
    WHERE status <> 'CANCELLED';

ALTER TABLE public.reservations
    DROP CONSTRAINT IF EXISTS uk_reservations_doctor_schedule;

COMMIT;