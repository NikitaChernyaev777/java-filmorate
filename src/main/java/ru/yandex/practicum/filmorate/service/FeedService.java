package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.model.event.FeedEventType;
import ru.yandex.practicum.filmorate.model.event.FeedOperation;
import ru.yandex.practicum.filmorate.storage.feed.FeedStorage;

import java.util.List;

@Slf4j
@Component
@Service
@RequiredArgsConstructor
public class FeedService {
    private final FeedStorage feedStorage;

    public void add(Long userId, Long entityId, FeedEventType eventType, FeedOperation operation) {
        Feed feed = new Feed(userId, entityId, eventType, operation);
        feedStorage.add(feed);
    }

    public List<Feed> findByUser(Long userId) {
        return feedStorage.findByUser(userId);
    }

}
