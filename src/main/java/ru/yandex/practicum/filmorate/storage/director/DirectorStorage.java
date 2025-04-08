package ru.yandex.practicum.filmorate.storage.director;

import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;
import java.util.Optional;

public interface DirectorStorage {

    Optional<Director> findById(long id);

    List<Director> findAll();

    Director addDirector(Director director);

    Director updateDirector(Director director);

    void deleteById(Long id);

    boolean existsById(Long id);
}