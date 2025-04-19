package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DirectorService {

    private final DirectorStorage directorStorage;

    public Director addDirector(Director director) {
        log.info("Добавление режиссера {}", director.getName());
        return directorStorage.addDirector(director);
    }

    public Director updateDirector(Director director) {
        log.info("Обновление данных режиссера {}", director.getName());
        validateExists(director.getId());
        return directorStorage.updateDirector(director);
    }

    public List<Director> findAll() {
        log.info("Получение списка всех режиссеров");
        return directorStorage.findAll();
    }

    public Director findById(Long id) {
        log.info("Получение режиссера с ID={}", id);

        Director director = directorStorage.findById(id);
        if (director == null) {
            throw new NotFoundException("Режиссер с ID=" + id + " не найден");
        }

        return director;
    }

    public void deleteDirector(Long id) {
        log.info("Удаление режиссера с ID={}", id);
        validateExists(id);
        directorStorage.deleteById(id);
    }

    private void validateExists(Long id) {
        if (!directorStorage.existsById(id)) {
            throw new NotFoundException("Режиссер с ID=" + id + " не найден");
        }
    }
}