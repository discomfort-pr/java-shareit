DROP TABLE IF EXISTS users, items, bookings, requests, comments;

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE requests (
    id BIGSERIAL PRIMARY KEY,
    description VARCHAR(255),
    requestor_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    created_date TIMESTAMP
);

CREATE TABLE items (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255),
    description VARCHAR(255),
    is_available BOOLEAN,
    owner_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    request_id BIGINT REFERENCES requests(id) ON DELETE CASCADE
);

CREATE TABLE bookings (
    id BIGSERIAL PRIMARY KEY,
    start_date TIMESTAMP,
    end_date TIMESTAMP,
    item_id BIGINT REFERENCES items(id) ON DELETE CASCADE,
    booker_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    status VARCHAR(255)
);

CREATE TABLE comments (
    id BIGSERIAL PRIMARY KEY,
    text VARCHAR(255),
    item_id BIGINT REFERENCES items(id) ON DELETE CASCADE,
    author_id BIGINT REFERENCES users(id) ON DELETE CASCADE
);