-- 테스트 데이터 초기화 스크립트

-- 회사 데이터
INSERT INTO company (name, industry, logo_url, website, created_at, updated_at) VALUES
('카카오', 'IT/인터넷', 'https://example.com/kakao_logo.png', 'https://www.kakaocorp.com', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('네이버', 'IT/인터넷', 'https://example.com/naver_logo.png', 'https://www.navercorp.com', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('삼성전자', '제조업', 'https://example.com/samsung_logo.png', 'https://www.samsung.com', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('LG전자', '제조업', 'https://example.com/lg_logo.png', 'https://www.lge.com', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('토스', 'IT/핀테크', 'https://example.com/toss_logo.png', 'https://toss.im', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 회원 데이터 (비밀번호: "password123" -> BCrypt 해시)
INSERT INTO members (email, password, nickname, role, created_at, updated_at) VALUES
('test@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iYqiSektzluKfOxbnXkjleu6OH6m', '테스트유저', 'GENERAL', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('verified@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iYqiSektzluKfOxbnXkjleu6OH6m', '인증유저', 'VERIFIED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('admin@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iYqiSektzluKfOxbnXkjleu6OH6m', '관리자', 'ADMIN', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);