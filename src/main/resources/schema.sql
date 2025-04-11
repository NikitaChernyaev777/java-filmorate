CREATE TABLE IF NOT EXISTS mpa_rating (
    mpa_id INTEGER PRIMARY KEY,
    name VARCHAR(25) NOT NULL
    );

CREATE TABLE IF NOT EXISTS film (
    film_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    release_date DATE NOT NULL,
    duration INTEGER NOT NULL,
    mpa_id INTEGER NOT NULL,
    FOREIGN KEY (mpa_id) REFERENCES mpa_rating(mpa_id)
    );

CREATE TABLE IF NOT EXISTS genre (
    genre_id INTEGER PRIMARY KEY,
    name VARCHAR(100) NOT NULL
    );

CREATE TABLE IF NOT EXISTS film_genre (
    film_id BIGINT,
    genre_id INTEGER,
    PRIMARY KEY (film_id, genre_id),
    FOREIGN KEY (film_id) REFERENCES film(film_id),
    FOREIGN KEY (genre_id) REFERENCES genre(genre_id)
    );

CREATE TABLE IF NOT EXISTS app_user (
    user_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(200) NOT NULL UNIQUE,
    login VARCHAR(200) NOT NULL,
    name VARCHAR(200),
    birthday DATE NOT NULL
    );

CREATE TABLE IF NOT EXISTS film_like (
    film_id BIGINT,
    user_id BIGINT,
    PRIMARY KEY (film_id, user_id),
    FOREIGN KEY (film_id) REFERENCES film(film_id),
    FOREIGN KEY (user_id) REFERENCES app_user(user_id)
    );

CREATE TABLE IF NOT EXISTS friendship (
    user_id BIGINT,
    friend_id BIGINT,
    status VARCHAR(25) DEFAULT 'UNCONFIRMED',
    PRIMARY KEY (user_id, friend_id),
    FOREIGN KEY (user_id) REFERENCES app_user(user_id),
    FOREIGN KEY (friend_id) REFERENCES app_user(user_id)
    );

CREATE TABLE IF NOT EXISTS director (
    director_id INTEGER PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR NOT NULL
    );

CREATE TABLE IF NOT EXISTS film_director (
    film_id BIGINT,
    director_id INTEGER,
    PRIMARY KEY (film_id, director_id),
    FOREIGN KEY (film_id) REFERENCES film(film_id),
    FOREIGN KEY (director_id) REFERENCES director(director_id)
    );

CREATE TABLE IF NOT EXISTS review (
    review_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    film_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    is_positive BOOLEAN NOT NULL,
    useful INT DEFAULT 0,
    FOREIGN KEY (film_id) REFERENCES film(film_id) ON UPDATE CASCADE ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES app_user(user_id) ON UPDATE CASCADE ON DELETE CASCADE
    );

CREATE TABLE IF NOT EXISTS review_like (
    review_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    is_useful BOOLEAN NOT NULL,
    PRIMARY KEY (review_id, user_id),
    FOREIGN KEY (review_id) REFERENCES review(review_id) ON UPDATE CASCADE ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES app_user(user_id) ON UPDATE CASCADE ON DELETE CASCADE
    );