CREATE TABLE IF NOT EXISTS users (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(250) NOT NULL,
    email VARCHAR(254) NOT NULL,
    CONSTRAINT UQ_USER_EMAIL UNIQUE (email)
);

CREATE TABLE IF NOT EXISTS categories (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    CONSTRAINT UQ_CATEGORIES_NAME UNIQUE (name)
);

CREATE TABLE IF NOT EXISTS locations (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    lat FLOAT NOT NULL,
    lon FLOAT NOT NULL
);

CREATE TABLE IF NOT EXISTS events (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    title VARCHAR(120) NOT NULL,
    annotation VARCHAR(2000) NOT NULL,
    description VARCHAR(7000) NOT NULL,
    event_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    category_id BIGINT NOT NULL,
    initiator_id BIGINT NOT NULL,
    location_id BIGINT NOT NULL,
    paid BOOLEAN DEFAULT FALSE,
    participant_limit BIGINT DEFAULT 0,
    confirmed_requests BIGINT NOT NULL,
    request_moderation BOOLEAN DEFAULT TRUE,
    state VARCHAR(100),
    views BIGINT DEFAULT 0,
    published_on TIMESTAMP WITHOUT TIME ZONE,
    created_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_category_id FOREIGN KEY (category_id) REFERENCES categories(id),
    CONSTRAINT fk_initiator_id FOREIGN KEY (initiator_id) REFERENCES users(id),
    CONSTRAINT fk_location_id FOREIGN KEY (location_id) REFERENCES locations(id)
);

CREATE TABLE IF NOT EXISTS participation_requests (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    requester_id BIGINT NOT NULL,
    event_id BIGINT NOT NULL,
    status VARCHAR(100),
    created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_requester_id FOREIGN KEY (requester_id) REFERENCES users(id),
    CONSTRAINT fk_event_id FOREIGN KEY (event_id) REFERENCES events(id)
);

CREATE TABLE IF NOT EXISTS compilations (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    title VARCHAR(125) NOT NULL,
    pinned BOOLEAN NOT NULL
);

CREATE TABLE IF NOT EXISTS compilation_events (
    compilation_id BIGINT NOT NULL,
    event_id BIGINT NOT NULL,
    CONSTRAINT fk_compilation_id FOREIGN KEY (compilation_id) REFERENCES compilations(id),
    CONSTRAINT fk_event_id FOREIGN KEY (event_id) REFERENCES events(id)
);

CREATE TABLE IF NOT EXISTS comments (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    author_id BIGINT NOT NULL,
    event_id BIGINT NOT NULL,
    message VARCHAR(2000) NOT NULL,
    created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_author_id FOREIGN KEY (author_id) REFERENCES users(id),
    CONSTRAINT fk_event_id FOREIGN KEY (event_id) REFERENCES events(id)
);