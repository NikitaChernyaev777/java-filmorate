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

    public List<MpaRating> getAllMpaRatings() {
        return mpaRatingDbStorage.findAll();
    }

    public MpaRating getMpaRatingById(int id) {
        return mpaRatingDbStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Категория рейтинга с id = " + id + " не найдена!"));
    }
}