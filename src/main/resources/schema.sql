DROP TABLE IF EXISTS reviews_likes;
DROP TABLE IF EXISTS friendship;
DROP TABLE IF EXISTS films_genres;
DROP TABLE IF EXISTS likes;
DROP TABLE IF EXISTS events;
DROP TABLE IF EXISTS film_director;
DROP TABLE IF EXISTS reviews;

DROP TABLE IF EXISTS films;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS mpa;
DROP TABLE IF EXISTS genres;
DROP TABLE IF EXISTS directors;

CREATE TABLE IF NOT EXISTS mpa (
  id integer  generated by default as identity PRIMARY KEY,
  name varchar(255) not null
);

CREATE TABLE IF NOT EXISTS genres (
  id integer generated by default as identity PRIMARY KEY,
  name varchar(255) not null
);

CREATE TABLE IF NOT EXISTS users (
  id integer generated by default as identity PRIMARY KEY,
  login varchar(255) not null,
  email varchar(255) not null,
  name varchar(255) not null,
  birthday date not null
);

CREATE TABLE IF NOT EXISTS films (
  id integer generated by default as identity PRIMARY KEY,
  name varchar(255) not null,
  description varchar(255) not null,
  releaseDate date not null,
  duration integer not null,
  mpa_id integer not null REFERENCES mpa(id)
);

CREATE TABLE IF NOT EXISTS likes (
  users_id integer not null REFERENCES users(id) on delete cascade,
  films_id integer not null REFERENCES films(id) on delete cascade
);

CREATE TABLE IF NOT EXISTS films_genres (
  films_id integer not null REFERENCES films(id) on delete cascade,
  genres_id integer not null REFERENCES genres(id) on delete cascade
);

CREATE TABLE IF NOT EXISTS friendship (
  users_id integer not null REFERENCES users(id) on delete cascade,
  friends_id integer not null REFERENCES users(id) on delete cascade,
  confirmed boolean not null
);

CREATE TABLE IF NOT EXISTS directors (
  director_id integer generated by default as identity PRIMARY KEY,
  name varchar(255) not null
  );

CREATE TABLE IF NOT EXISTS film_director (
  film_id     integer not null,
  director_id integer not null
);

CREATE TABLE IF NOT EXISTS REVIEWS
(
    REVIEW_ID   INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    FILM_ID     INTEGER       NOT NULL,
    USER_ID     INTEGER       NOT NULL,
    CONTENT     VARCHAR(1024) NOT NULL,
    IS_POSITIVE BOOLEAN       NOT NULL,
    USEFUL      INTEGER       NOT NULL DEFAULT 0,
    CONSTRAINT REVIEW_PK PRIMARY KEY (REVIEW_ID),
    CONSTRAINT FK_REVIEWS_USER_ID FOREIGN KEY (USER_ID) REFERENCES USERS (ID) ON DELETE CASCADE,
    CONSTRAINT FK_REVIEWS_FILM_ID FOREIGN KEY (FILM_ID) REFERENCES FILMS (ID) ON DELETE CASCADE
);
--CREATE UNIQUE INDEX IF NOT EXISTS REVIEWS_ID_INDEX ON REVIEWS (REVIEW_ID);

CREATE TABLE IF NOT EXISTS REVIEWS_LIKES
(
    REVIEW_ID INTEGER,
    USER_ID   INTEGER,
    IS_LIKE   BOOLEAN NOT NULL,
    CONSTRAINT PK_REVIEWS_LIKE PRIMARY KEY (REVIEW_ID, USER_ID),
    CONSTRAINT FK_REVIEWS_LIKE_REVIEWS_ID FOREIGN KEY (REVIEW_ID) REFERENCES REVIEWS (REVIEW_ID) ON DELETE CASCADE,
    CONSTRAINT FK_REVIEWS_LIKE_USER_ID FOREIGN KEY (USER_ID) REFERENCES USERS (ID) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS events (
  id integer generated by default as identity PRIMARY KEY,
  timestmp bigint not null,
  users_id integer not null REFERENCES users(id) on delete cascade,
  eventtype varchar(6) not null, -- одно из значениий LIKE, REVIEW или FRIEND
  operation varchar(6) not null, --одно из значениий REMOVE, ADD, UPDATE
  entitys_id integer not null
);
