package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationFilmException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;


@SpringBootTest
public class ValidationFilmTest {

    @Autowired
    private FilmController filmController;
    private Film film;

    @BeforeEach
    public void init(){
        film = new Film(1, "Свадьба в Малиновке", "Комедия",
                LocalDate.of(1967, 9, 25), 125);
    }

//    @Test
//    public void nameIsEmpty() {
//        film.setName("");
//        ValidationFilmException e = Assertions.assertThrows(
//                ValidationFilmException.class,
//                () -> filmController.create(film));
//        Assertions.assertEquals("Название не может быть пустым", e.getMessage());
//    }
//
//    @Test
//    public void nameIsNull() {
//        film.setName(null);
//        ValidationFilmException e = Assertions.assertThrows(
//                ValidationFilmException.class,
//                () -> filmController.create(film));
//        Assertions.assertEquals("Название не может быть пустым", e.getMessage());
//    }
//
//    @Test
//    public void nameIsBlank() {
//        film.setName("       ");
//        ValidationFilmException e = Assertions.assertThrows(
//                ValidationFilmException.class,
//                () -> filmController.create(film));
//        Assertions.assertEquals("Название не может быть пустым", e.getMessage());
//    }
//
//    @Test
//    public void descriptionIsLong() {
//        film.setDescription("очень длинное описание. очень длинное описание. очень длинное описание. " +
//                            "очень длинное описание. очень длинное описание. очень длинное описание. " +
//                            "очень длинное описание. очень длинное описание. очень длинное описание. " +
//                            "очень длинное описание. очень длинное описание. очень длинное описание. " +
//                            "очень длинное описание. очень длинное описание. очень длинное описание. " +
//                            "очень длинное описание. очень длинное описание. очень длинное описание. " +
//                            "очень длинное описание. очень длинное описание. очень длинное описание. ");
//        ValidationFilmException e = Assertions.assertThrows(
//                ValidationFilmException.class,
//                () -> filmController.create(film));
//        Assertions.assertEquals("Максимальная длина описания — 200 символов", e.getMessage());
//        Assertions.assertTrue(film.getDescription().length()>200);
//    }
//
    @Test
    public void releaseDateIsOld() {
        film.setReleaseDate(LocalDate.of(1800, 1, 1));
        ValidationFilmException e = Assertions.assertThrows(
                ValidationFilmException.class,
                () -> filmController.create(film));
        Assertions.assertEquals("дата релиза — не раньше 28 декабря 1895 года", e.getMessage());
    }

//    @Test
//    public void durationIsNegative() throws Exception {
//        film.setDuration(-12);
//        this.mockMvc.perform(MockMvcRequestBuilders.post("/films").
//                accept(MediaType.TEXT_HTML).
//                param("duration", "-12")).
//                andExpect(status().isBadRequest()).andDo(print());

        //filmController.create(film);
        // MethodArgumentNotValidException e = Assertions.assertThrows(
        //        MethodArgumentNotValidException.class,
        //        () -> filmController.create(film));
        //Assertions.assertEquals("продолжительность фильма должна быть положительной", e.getMessage());
//    }

    @Test
    public void unknownFilm() {
        film.setId(9999);
        FilmNotFoundException e = Assertions.assertThrows(
                FilmNotFoundException.class,
                () -> filmController.update(film));
        Assertions.assertEquals("Нет такого фильма в базе", e.getMessage());
    }

    @Test
    public void normalCreateAndUpdate() {
        film = filmController.create(film);
        film.setName("новое название фильма");
        film.setDescription("описание тоже поменяем");
        Film film2 = filmController.update(film);
        Assertions.assertEquals(film.getName(), film2.getName());
        Assertions.assertEquals(film.getDescription(), film2.getDescription());
        Assertions.assertEquals(1, filmController.findAll().size());
    }

}
