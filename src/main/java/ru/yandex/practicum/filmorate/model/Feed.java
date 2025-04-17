package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import ru.yandex.practicum.filmorate.model.event.FeedEventType;
import ru.yandex.practicum.filmorate.model.event.FeedOperation;

@Data
public class Feed {
    private Long timestamp;
    private Long userId;
    private FeedEventType eventType;
    private FeedOperation operation;
    private Long eventId;
    private Long entityId;

    public Feed() {
    }

    public Feed(Long userId, Long entityId, FeedEventType eventType, FeedOperation operation) {
        this.userId = userId;
        this.entityId = entityId;
        this.eventType = eventType;
        this.operation = operation;
    }
}
