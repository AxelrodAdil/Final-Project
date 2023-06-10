CREATE TABLE responsible_person
(
    responsible_person_id BIGINT NOT NULL PRIMARY KEY,
    responsible_person_name VARCHAR(100) NOT NULL,
    responsible_person_surname VARCHAR(100) NOT NULL,
    responsible_person_mail VARCHAR(100) NOT NULL,
    responsible_person_phone_number VARCHAR(30) NOT NULL
);

CREATE SEQUENCE responsible_person_seq START WITH 1;

--

CREATE TABLE gas_pumping_unit
(
    gpu_id BIGINT NOT NULL PRIMARY KEY,
    gpu_name VARCHAR(100) NOT NULL,
    gpu_state VARCHAR(30) NOT NULL,
    gpu_length BIGINT NOT NULL,
    responsible_person_id BIGINT REFERENCES responsible_person(responsible_person_id)
);

CREATE SEQUENCE gas_pumping_unit_seq START WITH 1;

--

CREATE TABLE request_gas_pumping_unit
(
    request_id BIGINT NOT NULL PRIMARY KEY,
    gpu_id BIGINT REFERENCES gas_pumping_unit(gpu_id),
    status VARCHAR(30) NOT NULL,
    is_pressure_difference BOOLEAN NOT NULL,
    is_temperature_difference BOOLEAN NOT NULL,
    time_of_mail_message_report DATE,
    time_of_bot_message_report DATE
);

CREATE SEQUENCE request_gas_pumping_unit_seq START WITH 1;

--
