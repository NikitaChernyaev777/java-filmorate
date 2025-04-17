package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.model.event.FeedEventType;
import ru.yandex.practicum.filmorate.model.event.FeedOperation;
import ru.yandex.practicum.filmorate.storage.feed.FeedStorage;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@Repository
@RequiredArgsConstructor
public class FeedDbStorage implements FeedStorage {
    private static final String ADD = "INSERT INTO feed(user_id, entity_id, event_type, operation) VALUES (?, ?, ?, ?);";
    private static final String GET_BY_ID = "SELECT * FROM feed WHERE id = ?;";
    private static final String GET_ALL_BY_USER = "SELECT * FROM feed WHERE user_id = ?;";

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate njdbcTemplate;

    protected Long insert(String query, Object... params) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection
                    .prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            for (int idx = 0; idx < params.length; idx++) {
                ps.setObject(idx + 1, params[idx]);
            }
            return ps;
        }, keyHolder);

        List<Map<String, Object>> keys = keyHolder.getKeyList();
        int countKeys = keys.size();

        if (countKeys > 1) {
            return (Long) keys.get(0).entrySet().iterator().next().getValue();
        } else if (countKeys == 1) {
            return (Long) keys.get(0).entrySet().iterator().next().getValue();
        }
        throw new InternalServerException("Не удалось сохранить данные");
    }

    public Optional<Feed> findOne(String query, Object... params) {
        try {
            Optional<Feed> result = Optional.ofNullable(jdbcTemplate.queryForObject(query, this::mapRowToFeed, params));
            return result;
        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Feed> add(Feed feed) {
        long id = insert(ADD,
                feed.getUserId(),
                feed.getEntityId(),
                feed.getEventType().ordinal(),
                feed.getOperation().ordinal());

        return Optional.ofNullable(findOne(GET_BY_ID, id).orElse(null));
    }

    public List<Feed> findMany(String query, Object... params) {
        return njdbcTemplate.getJdbcOperations().query(query, this::mapRowToFeed, params);
    }

    @Override
    public List<Feed> findByUser(Long userId) {
        return findMany(GET_ALL_BY_USER, userId);
    }

    public Feed mapRowToFeed(ResultSet rs, int rowNum) throws SQLException {
        Feed feed = new Feed();
        feed.setEventId(rs.getLong("id"));
        feed.setUserId(rs.getLong("user_id"));
        feed.setTimestamp(rs.getLong("timestamp"));
        feed.setEntityId(rs.getLong("entity_id"));
        feed.setEventType(FeedEventType.values()[rs.getInt("event_type")]);
        feed.setOperation(FeedOperation.values()[rs.getInt("operation")]);
        return feed;
    }
}
