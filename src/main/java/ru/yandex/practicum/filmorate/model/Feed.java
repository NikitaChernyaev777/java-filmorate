package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import ru.yandex.practicum.filmorate.model.enums.EventOperation;
import ru.yandex.practicum.filmorate.model.enums.EventType;

@Getter
@Setter
public class Feed {

    @NotNull
    private Long timestamp;

    @NotNull
    private Long userId;

    @NotNull
    private EventType eventType;

    @NotNull
    private Long eventId;

    @NotNull
    @JsonProperty("operation")
    private EventOperation eventOperation;

    @NotNull
    private Long entityId;
}