-- 创建测试用户（密码：123456）
INSERT INTO users (phone_number, password, nickname, created_at, updated_at, is_active) 
VALUES ('13800138000', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', '测试用户', NOW(), NOW(), true)
ON DUPLICATE KEY UPDATE updated_at = NOW(); 