package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.model.MpaRatingDto;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MpaRatingService {

    public List<MpaRatingDto> getAllMpaRatings() {
        return Arrays.stream(MpaRating.values())
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public MpaRatingDto getMpaRatingById(int id) {
        if (id < 1 || id > MpaRating.values().length) {
            throw new NotFoundException("Категория рейтинга не найдена!");
        }
        return toDto(MpaRating.values()[id - 1]);
    }

    private MpaRatingDto toDto(MpaRating mpaRating) {
        MpaRatingDto dto = new MpaRatingDto();
        dto.setId(mpaRating.ordinal() + 1);
        dto.setName(mpaRating.getRating());
        return dto;
    }
}