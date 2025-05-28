# docker/init-db.sql
-- MES 데이터베이스 초기 설정 스크립트

-- 추가 스키마나 초기 데이터가 필요한 경우 여기에 작성
CREATE SCHEMA IF NOT EXISTS mes;

-- 예시: 초기 장비 데이터 삽입 (운영 환경에서는 제거)
-- INSERT INTO equipment (equipment_code, equipment_name, location, status)
-- VALUES ('LINE_PROD_001', 'Production Line 1', 'Factory Floor 1', 'IDLE');