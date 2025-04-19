package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.model.enums.EventType;
import ru.yandex.practicum.filmorate.model.enums.EventOperation;
import ru.yandex.practicum.filmorate.storage.feed.FeedStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;

@Slf4j
@Component
@Repository
@RequiredArgsConstructor
public class FeedDbStorage implements FeedStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void addEvent(Long userId, Long entityId, EventOperation eventOperation, EventType eventType) {
        String addEventSql = "INSERT INTO feed (user_id, entity_id, timestamp, event_type, event_operation) " +
                "VALUES (?, ?, ?, ?, ?)";

        jdbcTemplate.update(addEventSql, userId, entityId, Instant.now().toEpochMilli(), eventType.name(),
                eventOperation.name());
    }

    @Override
    public List<Feed> findByUser(Long userId) {
        String findFeedSql = "SELECT * FROM feed WHERE user_id = ? ORDER BY timestamp";
        return jdbcTemplate.query(findFeedSql, this::mapToFeed, userId);
    }

    public Feed mapToFeed(ResultSet resultSet, int rowNum) throws SQLException {
        Feed feed = new Feed();
        feed.setEventId(resultSet.getLong("event_id"));
        feed.setUserId(resultSet.getLong("user_id"));
        feed.setEntityId(resultSet.getLong("entity_id"));
        feed.setEventType(EventType.valueOf(resultSet.getString("event_type")));
        feed.setEventOperation(EventOperation.valueOf(resultSet.getString("event_operation")));
        feed.setTimestamp(resultSet.getLong(("timestamp")));

        return feed;
    }
}