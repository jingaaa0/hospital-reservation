-- Local portfolio fixtures only. Run with psql -v ON_ERROR_STOP=1.
-- Existing reservations/inquiries cause a rollback; nothing is deleted.
BEGIN;
SET LOCAL TIME ZONE 'Asia/Seoul';
LOCK TABLE reservations, inquiries IN EXCLUSIVE MODE;
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM reservations) OR EXISTS (SELECT 1 FROM inquiries) THEN
        RAISE EXCEPTION 'Reservations and inquiries must be empty. No data was changed.';
    END IF;
END $$;

-- Sample unique slots, with extra weight on today for dashboard screenshots.
CREATE TEMP TABLE portfolio_slots ON COMMIT DROP AS
SELECT d.id AS doctor_id, d.department,
       CURRENT_DATE + n AS appointment_date,
       (TIME '09:00' + k * INTERVAL '30 minutes')::time AS appointment_time
FROM doctors d
CROSS JOIN generate_series(-14, 14) AS days(n)
CROSS JOIN generate_series(0, 17) AS times(k)
JOIN doctor_available_days a ON a.doctor_id = d.id
    AND a.day_of_week = (ARRAY['MONDAY','TUESDAY','WEDNESDAY','THURSDAY','FRIDAY','SATURDAY','SUNDAY'])[EXTRACT(ISODOW FROM CURRENT_DATE + n)::int]
WHERE d.active = 'Y'
  AND (EXTRACT(ISODOW FROM CURRENT_DATE + n) <> 6 OR k < 8)
ORDER BY -ln(GREATEST(random(), 0.000001)) / CASE WHEN n = 0 THEN 50 ELSE 1 END
LIMIT 152;

INSERT INTO reservations
    (reservation_number, patient_name, phone_number, doctor_id,
     appointment_date, appointment_time, symptom, admin_memo,
     privacy_agreed, status, created_at, updated_at)
SELECT 'DEMO-' || lpad(i::text, 12, '0'),
       (ARRAY['김','이','박','최','정','강','조','윤','장','임'])[1 + floor(random()*10)::int]
       || (ARRAY['서연','지우','민준','서준','하윤','도윤','수빈','지호','예린','현우','유진','지민','수현','은우','민서','지원'])[1 + floor(random()*16)::int],
       '0100000' || lpad(i::text, 4, '0'), doctor_id,
       appointment_date, appointment_time,
       CASE department
         WHEN 'ENT' THEN (ARRAY['목이 따갑고 기침이 납니다.','코막힘과 콧물이 지속됩니다.','귀가 먹먹해서 진료를 받고 싶습니다.'])[1 + floor(random()*3)::int]
         WHEN 'INTERNAL_MEDICINE' THEN (ARRAY['속쓰림이 반복됩니다.','정기 진료를 예약합니다.','소화가 잘되지 않아 상담을 원합니다.'])[1 + floor(random()*3)::int]
         WHEN 'ORTHOPEDICS' THEN (ARRAY['무릎 통증으로 진료를 원합니다.','손목이 뻐근합니다.','발목 통증이 지속됩니다.'])[1 + floor(random()*3)::int]
         WHEN 'GENERAL_SURGERY' THEN (ARRAY['상처 경과를 확인하고 싶습니다.','피부 아래 혹에 대해 상담을 원합니다.','이전 진료 후 재진을 예약합니다.'])[1 + floor(random()*3)::int]
         WHEN 'NEUROSURGERY' THEN (ARRAY['허리 통증으로 상담을 원합니다.','목과 어깨가 뻐근합니다.','손 저림 증상으로 진료를 원합니다.'])[1 + floor(random()*3)::int]
         ELSE (ARRAY['재활 치료 상담을 원합니다.','어깨 움직임이 불편합니다.','운동 후 회복 상태를 확인하고 싶습니다.'])[1 + floor(random()*3)::int]
       END,
       CASE WHEN status = 'CANCELLED' THEN '개인 일정으로 예약 취소'
            WHEN status = 'NO_SHOW' THEN '예약 시간 이후 미방문 확인'
            WHEN status = 'CONFIRMED' THEN '예약 일정 안내 완료'
            WHEN status = 'COMPLETED' THEN '방문 및 진료 완료 확인'
            ELSE NULL END,
       true, status,
       LEAST(LOCALTIMESTAMP - INTERVAL '1 day', appointment_date - INTERVAL '3 days' + INTERVAL '10 hours'),
       CASE WHEN status IN ('COMPLETED','NO_SHOW') THEN appointment_date + appointment_time
            ELSE LEAST(LOCALTIMESTAMP, appointment_date - INTERVAL '1 day' + INTERVAL '11 hours') END
FROM (
    SELECT s.*, row_number() OVER (ORDER BY appointment_date, appointment_time, doctor_id) AS i,
           CASE WHEN appointment_date + appointment_time < LOCALTIMESTAMP THEN
                    (ARRAY['COMPLETED','COMPLETED','COMPLETED','COMPLETED','NO_SHOW','CANCELLED'])[1 + floor(random()*6)::int]
                ELSE (ARRAY['REQUESTED','REQUESTED','CONFIRMED','CONFIRMED','CONFIRMED','CANCELLED'])[1 + floor(random()*6)::int]
           END AS status
    FROM portfolio_slots s
) fixtures;

INSERT INTO inquiries (name, phone_number, email, content, privacy_agreed, status, created_at)
SELECT (ARRAY['김','이','박','최','정','강','조','윤'])[1 + floor(random()*8)::int]
       || (ARRAY['민지','지훈','서영','도현','수진','현서','유나','정우'])[1 + floor(random()*8)::int],
       '0100000' || lpad((200+i)::text, 4, '0'),
       'portfolio' || i || '@example.com',
       (ARRAY[
         '처음 방문하는데 접수 시 준비해야 할 서류가 있나요?',
         '토요일 진료 시간과 접수 마감 시간을 알고 싶습니다.',
         '예약 시간을 변경하려면 어떻게 해야 하나요?',
         '주차장 위치와 주차 지원 여부를 문의합니다.',
         '진료확인서 발급 절차를 안내받고 싶습니다.',
         '보호자와 함께 방문할 예정인데 동행이 가능한가요?',
         '이전 검사 결과를 가져가도 되는지 문의합니다.',
         '예약 취소 후 다른 날짜로 다시 신청하고 싶습니다.',
         '휠체어를 이용하는데 출입구와 엘리베이터 위치가 궁금합니다.',
         '검사 예약 전 준비 사항은 어디에서 확인할 수 있나요?',
         '점심시간에도 접수할 수 있는지 문의합니다.',
         '진료비 영수증을 다시 발급받는 방법이 궁금합니다.'
       ])[1 + floor(random()*12)::int],
       true,
       CASE WHEN i <= 14 THEN 'RECEIVED' WHEN i <= 24 THEN 'IN_PROGRESS' ELSE 'ANSWERED' END,
       LOCALTIMESTAMP - random() * INTERVAL '14 days'
FROM generate_series(1,38) AS series(i);

DO $$
BEGIN
    IF (SELECT COUNT(*) FROM reservations) <> 152 OR (SELECT COUNT(*) FROM inquiries) <> 38 THEN
        RAISE EXCEPTION 'Fixture counts do not match (152 reservations / 38 inquiries). Rolling back.';
    END IF;
END $$;
COMMIT;

SELECT 'reservations' AS table_name, status, COUNT(*) FROM reservations GROUP BY status
UNION ALL
SELECT 'inquiries', status, COUNT(*) FROM inquiries GROUP BY status
ORDER BY table_name, status;
