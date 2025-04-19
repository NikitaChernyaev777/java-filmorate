package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.MpaRatingDbStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MpaRatingService {

    private final MpaRatingDbStorage mpaRatingDbStorage;

    public List<MpaRating> findAll() {
        return mpaRatingDbStorage.findAll();
    }

    public MpaRating findById(int id) {
        MpaRating mpmpaRating = mpaRatingDbStorage.findById(id);

        if (mpmpaRating == null) {
            throw new NotFoundException("Категория рейтинга с ID=" + id + " не найдена");
        }

        return mpmpaRating;
    }
}