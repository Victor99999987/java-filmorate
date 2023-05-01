package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exception.GenreNotFoundException;
import ru.yandex.practicum.filmorate.exception.MpaNotFoundException;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.Storage;
import ru.yandex.practicum.filmorate.storage.impl.db.DbUpdater;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@SpringBootTest
@AutoConfigureTestDatabase
class FilmorateApplicationTests {
    private final Storage<User> userStorage;
    private final Storage<Genre> genreStorage;
    private final Storage<Mpa> mpaStorage;
    private final Storage<Film> filmStorage;
    private final DbUpdater dbUpdater;

    FilmorateApplicationTests(@Qualifier("DbUserStorage") Storage<User> userStorage,
                              @Qualifier("DbGenreStorage") Storage<Genre> genreStorage,
                              @Qualifier("DbMpaStorage") Storage<Mpa> mpaStorage,
                              @Qualifier("DbFilmStorage") Storage<Film> filmStorage,
                              @Qualifier("DbUpdater") DbUpdater dbUpdater) {
        this.userStorage = userStorage;
        this.genreStorage = genreStorage;
        this.mpaStorage = mpaStorage;
        this.filmStorage = filmStorage;
        this.dbUpdater = dbUpdater;
    }

    @BeforeEach
    public void init() {
        dbUpdater.update();
    }

    @Test
    public void addUser() {
        User inputUser = User.builder()
                .login("darkula")
                .name("kid darkula")
                .email("darkula@mail.ru")
                .birthday(LocalDate.of(1960, 1, 1))
                .build();

        User resultUser = userStorage.add(inputUser);

        assertThat(resultUser)
                .isNotNull()
                .hasFieldOrPropertyWithValue("id", 1L)
                .hasFieldOrPropertyWithValue("login", "darkula");
    }

    @Test
    public void getUser() {
        User inputUser = User.builder()
                .login("darkula")
                .name("kid darkula")
                .email("darkula@mail.ru")
                .birthday(LocalDate.of(1960, 1, 1))
                .build();

        userStorage.add(inputUser);
        User resultUser = userStorage.getById(1L);

        assertThat(resultUser)
                .isNotNull()
                .hasFieldOrPropertyWithValue("login", "darkula");
    }

    @Test
    public void getUnknownUser() {
        assertThatExceptionOfType(UserNotFoundException.class).isThrownBy(
                () -> userStorage.getById(9999L));
    }

    @Test
    public void updateUser() {
        User inputUser = User.builder()
                .login("darkula")
                .name("kid darkula")
                .email("darkula@mail.ru")
                .birthday(LocalDate.of(1960, 1, 1))
                .build();

        inputUser = userStorage.add(inputUser);
        inputUser.setName("drakula-durakla");
        User resultUser = userStorage.update(inputUser);

        assertThat(resultUser)
                .isNotNull()
                .hasFieldOrPropertyWithValue("name", "drakula-durakla");
    }

    @Test
    public void deleteUser() {
        User inputUser = User.builder()
                .login("darkula")
                .name("kid darkula")
                .email("darkula@mail.ru")
                .birthday(LocalDate.of(1960, 1, 1))
                .build();

        userStorage.add(inputUser);
        userStorage.getById(1L);
        userStorage.remove(1L);
        assertThatExceptionOfType(UserNotFoundException.class).isThrownBy(
                () -> userStorage.getById(1L));
    }

    @Test
    public void getAllUser() {
        User inputUser = User.builder()
                .login("darkula")
                .name("kid darkula")
                .email("darkula@mail.ru")
                .birthday(LocalDate.of(1960, 1, 1))
                .build();

        inputUser = userStorage.add(inputUser);
        assertThat(userStorage.getAll())
                .isNotNull()
                .contains(inputUser);
        userStorage.remove(1L);
        assertThat(userStorage.getAll())
                .isNotNull()
                .isEmpty();
    }

    @Test
    public void addGenre() {
        Genre genre = Genre.builder()
                .name("комедия")
                .build();

        genre = genreStorage.add(genre);

        assertThat(genre)
                .isNotNull()
                .hasFieldOrPropertyWithValue("name", "комедия");
    }

    @Test
    public void getGenre() {
        Genre inputGenre = Genre.builder()
                .name("комедия")
                .build();

        genreStorage.add(inputGenre);
        Genre resultGenre = genreStorage.getById(1L);

        assertThat(resultGenre)
                .isNotNull()
                .hasFieldOrPropertyWithValue("name", "комедия");
    }

    @Test
    public void getUnknownGenre() {
        assertThatExceptionOfType(GenreNotFoundException.class).isThrownBy(
                () -> genreStorage.getById(9999L));
    }

    @Test
    public void updateGenre() {
        Genre inputGenre = Genre.builder()
                .name("комедия")
                .build();

        inputGenre = genreStorage.add(inputGenre);
        inputGenre.setName("ужасы");
        Genre resultGenre = genreStorage.update(inputGenre);

        assertThat(resultGenre)
                .isNotNull()
                .hasFieldOrPropertyWithValue("name", "ужасы");
    }

    @Test
    public void deleteGenre() {
        Genre inputGenre = Genre.builder()
                .name("комедия")
                .build();

        genreStorage.add(inputGenre);
        genreStorage.getById(1L);
        genreStorage.remove(1L);
        assertThatExceptionOfType(GenreNotFoundException.class).isThrownBy(
                () -> genreStorage.getById(1L));
    }

    @Test
    public void getAllGenre() {
        Genre inputGenre = Genre.builder()
                .name("комедия")
                .build();

        inputGenre = genreStorage.add(inputGenre);
        assertThat(genreStorage.getAll())
                .isNotNull()
                .contains(inputGenre);
        genreStorage.remove(1L);
        assertThat(genreStorage.getAll())
                .isNotNull()
                .isEmpty();
    }

    @Test
    public void addMpa() {
        Mpa mpa = Mpa.builder()
                .name("P")
                .build();

        mpa = mpaStorage.add(mpa);

        assertThat(mpa)
                .isNotNull()
                .hasFieldOrPropertyWithValue("name", "P");
    }

    @Test
    public void getMpa() {
        Mpa inputMpa = Mpa.builder()
                .name("P")
                .build();

        mpaStorage.add(inputMpa);
        Mpa resultMpa = mpaStorage.getById(1L);

        assertThat(resultMpa)
                .isNotNull()
                .hasFieldOrPropertyWithValue("name", "P");
    }

    @Test
    public void getUnknownMpa() {
        assertThatExceptionOfType(MpaNotFoundException.class).isThrownBy(
                () -> mpaStorage.getById(9999L));
    }

    @Test
    public void updateMpa() {
        Mpa inputMpa = Mpa.builder()
                .name("P")
                .build();

        inputMpa = mpaStorage.add(inputMpa);
        inputMpa.setName("PG");
        Mpa resultMpa = mpaStorage.update(inputMpa);

        assertThat(resultMpa)
                .isNotNull()
                .hasFieldOrPropertyWithValue("name", "PG");
    }

    @Test
    public void deleteMpa() {
        Mpa inputMpa = Mpa.builder()
                .name("P")
                .build();

        mpaStorage.add(inputMpa);
        mpaStorage.getById(1L);
        mpaStorage.remove(1L);
        assertThatExceptionOfType(MpaNotFoundException.class).isThrownBy(
                () -> mpaStorage.getById(1L));
    }

    @Test
    public void getAllMpa() {
        Mpa inputMpa = Mpa.builder()
                .name("P")
                .build();

        inputMpa = mpaStorage.add(inputMpa);
        assertThat(mpaStorage.getAll())
                .isNotNull()
                .contains(inputMpa);
        mpaStorage.remove(1L);
        assertThat(mpaStorage.getAll())
                .isNotNull()
                .isEmpty();
    }

    @Test
    public void addFilm() {
        addMpa();
        Film film = Film.builder()
                .name("Робокоп")
                .description("Погибшего полицейского возвращают к жизни в качестве киборга")
                .releaseDate(LocalDate.of(1993, 12, 12))
                .duration(90)
                .mpa(mpaStorage.getById(1L))
                .build();

        film = filmStorage.add(film);

        assertThat(film)
                .isNotNull()
                .hasFieldOrPropertyWithValue("name", "Робокоп");
    }

    @Test
    public void getFilm() {
        addMpa();
        Film inputFilm = Film.builder()
                .name("Робокоп")
                .description("Погибшего полицейского возвращают к жизни в качестве киборга")
                .releaseDate(LocalDate.of(1993, 12, 12))
                .duration(90)
                .mpa(mpaStorage.getById(1L))
                .build();

        filmStorage.add(inputFilm);
        Film resultFilm = filmStorage.getById(1L);

        assertThat(resultFilm)
                .isNotNull()
                .hasFieldOrPropertyWithValue("name", "Робокоп");
    }

    @Test
    public void getUnknownFilm() {
        assertThatExceptionOfType(FilmNotFoundException.class).isThrownBy(
                () -> filmStorage.getById(9999L));
    }

    @Test
    public void updateFilm() {
        addMpa();
        Film inputFilm = Film.builder()
                .name("Робокоп")
                .description("Погибшего полицейского возвращают к жизни в качестве киборга")
                .releaseDate(LocalDate.of(1993, 12, 12))
                .duration(90)
                .mpa(mpaStorage.getById(1L))
                .build();

        inputFilm = filmStorage.add(inputFilm);
        inputFilm.setName("Иван Васильевич меняет профессию");
        Film resultFilm = filmStorage.update(inputFilm);

        assertThat(resultFilm)
                .isNotNull()
                .hasFieldOrPropertyWithValue("name", "Иван Васильевич меняет профессию")
                .hasFieldOrPropertyWithValue("description", "Погибшего полицейского возвращают к жизни в качестве киборга");
    }

    @Test
    public void deleteFilm() {
        addMpa();
        Film inputFilm = Film.builder()
                .name("Робокоп")
                .description("Погибшего полицейского возвращают к жизни в качестве киборга")
                .releaseDate(LocalDate.of(1993, 12, 12))
                .duration(90)
                .mpa(mpaStorage.getById(1L))
                .build();

        filmStorage.add(inputFilm);
        filmStorage.getById(1L);
        filmStorage.remove(1L);
        assertThatExceptionOfType(FilmNotFoundException.class).isThrownBy(
                () -> filmStorage.getById(1L));
    }

    @Test
    public void getAllFilm() {
        addMpa();
        Film inputFilm = Film.builder()
                .name("Робокоп")
                .description("Погибшего полицейского возвращают к жизни в качестве киборга")
                .releaseDate(LocalDate.of(1993, 12, 12))
                .duration(90)
                .mpa(mpaStorage.getById(1L))
                .build();

        inputFilm = filmStorage.add(inputFilm);
        assertThat(filmStorage.getAll())
                .isNotNull()
                .contains(inputFilm);
        filmStorage.remove(1L);
        assertThat(filmStorage.getAll())
                .isNotNull()
                .isEmpty();
    }

}
