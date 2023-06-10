INSERT INTO responsible_person (responsible_person_id, responsible_person_name, responsible_person_surname, responsible_person_mail, responsible_person_phone_number)
VALUES (1, 'Adil', 'Myktybek', 'test1@gmail.com', '80000000000');

INSERT INTO responsible_person (responsible_person_id, responsible_person_name, responsible_person_surname, responsible_person_mail, responsible_person_phone_number)
VALUES (2, 'WhoAmI', 'Hello', 'test2@gmail.com', '80000000000');

--

INSERT INTO gas_pumping_unit (gpu_id, gpu_name, gpu_state, gpu_length, responsible_person_id)
VALUES (1, 'GT-700-5', 'Regular Condition', 700, 1);

INSERT INTO gas_pumping_unit (gpu_id, gpu_name, gpu_state, gpu_length, responsible_person_id)
VALUES (2, 'GT-700-6', 'Regular Condition', 500, 2);

--
