INSERT INTO employee (
  active,
  created_at,
  duty,
  email,
  late_count,
  loginid,
  name,
  password,
  phone,
  role
) VALUES (
  true,
  NOW(),
  'Staff',
  'admin@test.com',
  0,
  'admin',
  'manager',
  '$2a$10$6lVXdCOhMm57B2Uc5xYov.afKcIXxRB7Pg6EKyElkRhdJDqXrGv6.',
  '01000000000',
  'ADMIN'
);