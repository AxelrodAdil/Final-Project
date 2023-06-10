CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL
);

CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE user_roles (
    user_id BIGINT NOT NULL REFERENCES users(id),
    role_id BIGINT NOT NULL REFERENCES roles(id),
    PRIMARY KEY (user_id, role_id)
);

--

INSERT INTO users (username, email, password) VALUES ('adil.myktybek', 'admin@mail.com', '$2a$12$AU951iduBdXO/x/njEyfsuVgXvwuF9qBSsMGpvjP2fuLZtE4gnQF2');
INSERT INTO users (username, email, password) VALUES ('saule.secret', 'user@mail.com', '$2a$12$dkJVG4i4yoe03fj9AvkweO9.8S3Jo0reJatVSmv96gt2sHNuIFPt6');

INSERT INTO roles (name) VALUES ('ROLE_ADMIN');
INSERT INTO roles (name) VALUES ('ROLE_USER');

INSERT INTO user_roles (user_id, role_id) VALUES (1, 1); -- Adil Myktybek is an admin
INSERT INTO user_roles (user_id, role_id) VALUES (1, 2); -- Adil Myktybek is also a regular user
INSERT INTO user_roles (user_id, role_id) VALUES (2, 2); -- Saule is a regular user

--
