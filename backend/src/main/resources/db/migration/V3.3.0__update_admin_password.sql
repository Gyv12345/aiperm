-- 更新 admin 用户密码为 admin123
-- BCrypt.hashpw("admin123") 生成的哈希值
UPDATE sys_user
SET password = '$2a$10$EqKcp1WFKVQISheBxkVJceXI1MPqGkKnMU7zD9hPA0X0Uy.Jb.eYy'
WHERE username = 'admin';
