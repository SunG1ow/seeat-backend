INSERT INTO member (email, password_hash, role, nickname, phone_number, is_withdrawn, created_at)
VALUES (
           'admin01@seeat.com',
           '$2b$10$/5tUa01gIhwctNpEHFNOPOZQd6kIrX.D.t2im1PYTk1MR4UnSx2wO',
           'ADMIN',
           '관리자',
           '010-0000-0000',
           false,
           NOW()
       );