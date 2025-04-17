package ru.yandex.practicum.filmorate.storage.feed;

import ru.yandex.practicum.filmorate.model.Feed;

import java.util.List;
import java.util.Optional;

public interface FeedStorage {
    Optional<Feed> add(Feed feed);

    List<Feed> findByUser(Long userId);
}
