package ru.yandex.practicum.filmorate.storage.feed;

import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.model.enums.EventType;
import ru.yandex.practicum.filmorate.model.enums.EventOperation;

import java.util.List;

public interface FeedStorage {

    void addEvent(Long userId, Long entityId, EventOperation eventOperation, EventType eventType);

    List<Feed> findByUser(Long userId);
}