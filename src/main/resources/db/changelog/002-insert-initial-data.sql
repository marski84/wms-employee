-- Changeset 002: db-init
-- Wstawianie danych - wersja dostosowana do encji Employee z auth0UserId i bez EmployeeCredentials

-- 1. Wstawienie administratora
INSERT INTO public.employees
(id, supervisor_id, username, name, surname, employee_role, employee_status, registration_date, auth0_user_id)
VALUES (1, 1, 'adminuser', 'Admin', 'System', 'ADMIN', 'ACTIVE', NOW(), 'auth0|admin_system_001');
-- Dodano auth0_user_id

-- Usunięto wstawienie danych uwierzytelniających administratora --

-- 3. Wstawienie danych kontaktowych administratora
INSERT INTO public.employee_contact_details
(employee_id, email, phone_number, address, city, postal_code, country, edit_date)
VALUES (1, 'admin@company.com', '+1234567890', 'Company Headquarters',
        'Corporate City', '12345', 'United States', NOW());

-- 4. Wstawienie managera podległego administratorowi
INSERT INTO public.employees
(id, supervisor_id, username, name, surname, employee_role, employee_status, registration_date, auth0_user_id)
VALUES (2, 1, 'BossyBoss', 'Manager', 'User', 'MANAGER', 'ACTIVE', NOW(), 'auth0|manager_user_002');
-- Dodano auth0_user_id

-- Usunięto wstawienie danych uwierzytelniających managera --

-- 6. Wstawienie danych kontaktowych managera
INSERT INTO public.employee_contact_details
(employee_id, email, phone_number, address, city, postal_code, country, edit_date)
VALUES (2, 'manager@company.com', '+1987654321', 'Branch Office 1',
        'Branch City', '54321', 'United States', NOW());

-- 7. Dodatkowy użytkownik 1 (Zwykły pracownik)
INSERT INTO public.employees
(id, supervisor_id, username, name, surname, employee_role, employee_status, registration_date, auth0_user_id)
VALUES (3, 2, 'JJ', 'John', 'Smith', 'EMPLOYEE', 'ACTIVE', NOW(), 'auth0|john_smith_003');
-- Dodano auth0_user_id

-- Usunięto wstawienie danych uwierzytelniających użytkownika 1 --

INSERT INTO public.employee_contact_details
(employee_id, email, phone_number, address, city, postal_code, country, edit_date)
VALUES (3, 'john.smith@company.com', '+4875923641', '123 Main St',
        'New York', '10001', 'United States', NOW());

-- 8. Dodatkowy użytkownik 2 (Zwykły pracownik)
INSERT INTO public.employees
(id, supervisor_id, username, name, surname, employee_role, employee_status, registration_date, auth0_user_id)
VALUES (4, 2, 'EJ', 'Emily', 'Johnson', 'EMPLOYEE', 'ACTIVE', NOW(), 'auth0|emily_johnson_004');
-- Dodano auth0_user_id

-- Usunięto wstawienie danych uwierzytelniających użytkownika 2 --

INSERT INTO public.employee_contact_details
(employee_id, email, phone_number, address, city, postal_code, country, edit_date)
VALUES (4, 'emily.johnson@company.com', '+3456789012', '456 Oak Avenue',
        'Chicago', '60601', 'United States', NOW());

-- 9. Dodatkowy użytkownik 3 (Dział HR)
INSERT INTO public.employees
(id, supervisor_id, username, name, surname, employee_role, employee_status, registration_date, auth0_user_id)
VALUES (5, 2, 'HR Man', 'Michael', 'Brown', 'HR', 'ACTIVE', NOW(), 'auth0|michael_brown_005');
-- Dodano auth0_user_id

-- Usunięto wstawienie danych uwierzytelniających użytkownika 3 --

INSERT INTO public.employee_contact_details
(employee_id, email, phone_number, address, city, postal_code, country, edit_date)
VALUES (5, 'michael.brown@company.com', '+5678901234', '789 Pine Street',
        'San Francisco', '94105', 'United States', NOW());

-- 10. Dodatkowy użytkownik 4 (Dział HR)
INSERT INTO public.employees
(id, supervisor_id, username, name, surname, employee_role, employee_status, registration_date, auth0_user_id)
VALUES (6, 2, 'Upper', 'Sofia', 'Garcia', 'HR', 'ACTIVE', NOW(), 'auth0|sofia_garcia_006');
-- Dodano auth0_user_id

-- Usunięto wstawienie danych uwierzytelniających użytkownika 4 --

INSERT INTO public.employee_contact_details
(employee_id, email, phone_number, address, city, postal_code, country, edit_date)
VALUES (6, 'sofia.garcia@company.com', '+6789012345', '101 Maple Drive',
        'Austin', '78701', 'United States', NOW());

SELECT setval('employees_id_seq', 6);
SELECT setval('employee_contact_details_id_seq', 6);