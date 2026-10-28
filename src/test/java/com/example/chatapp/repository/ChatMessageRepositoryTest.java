package com.example.chatapp.repository;

import com.example.chatapp.model.ChatMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class ChatMessageRepositoryTest {

    static {
        // The JVM's legacy zone id (Asia/Calcutta) is rejected by the Postgres container
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired
    private ChatMessageRepository repository;

    @Autowired
    private JdbcTemplate jdbc;

    @BeforeEach
    void cleanTable() {
        jdbc.update("DELETE FROM chat_messages");
    }

    private void insert(String room, String participant, String content, Instant at) {
        jdbc.update(
                "INSERT INTO chat_messages (room_id, participant, content, created_at) VALUES (?, ?, ?, ?)",
                room, participant, content, Timestamp.from(at));
    }

    @Test
    void findsNewestFirst_andHonoursPageSize() {
        Instant base = Instant.parse("2026-01-01T10:00:00Z");
        for (int i = 0; i < 5; i++) {
            insert("room-a", "Mohit", "m" + i, base.plusSeconds(i));
        }

        List<ChatMessage> page =
                repository.findByRoomIdOrderByCreatedAtDesc("room-a", PageRequest.of(0, 3));

        assertThat(page).extracting(ChatMessage::getContent)
                .containsExactly("m4", "m3", "m2");
    }

    @Test
    void onlyReturnsMessagesFromTheRequestedRoom() {
        Instant base = Instant.parse("2026-01-01T10:00:00Z");
        insert("room-a", "Mohit", "a1", base);
        insert("room-a", "Mohit", "a2", base.plusSeconds(1));
        insert("room-b", "Mohit", "b1", base.plusSeconds(2));

        List<ChatMessage> page =
                repository.findByRoomIdOrderByCreatedAtDesc("room-b", PageRequest.of(0, 10));

        assertThat(page).extracting(ChatMessage::getContent).containsExactly("b1");
    }

    @Test
    void saveAssignsIdAndStoresMessage() {
        ChatMessage saved = repository.saveAndFlush(new ChatMessage("room-c", "Mohit", "hello"));

        assertThat(saved.getId()).isNotNull();
        assertThat(repository.findByRoomIdOrderByCreatedAtDesc("room-c", PageRequest.of(0, 1)))
                .singleElement()
                .satisfies(m -> {
                    assertThat(m.getContent()).isEqualTo("hello");
                    assertThat(m.getParticipant()).isEqualTo("Mohit");
                    assertThat(m.getCreatedAt()).isNotNull();
                });
    }

    @Test
    void compositeIndexExistsOnRoomAndCreatedAt() {
        Integer count = jdbc.queryForObject(
                "SELECT count(*) FROM pg_indexes "
                        + "WHERE tablename = 'chat_messages' AND indexname = 'idx_chat_messages_room_created'",
                Integer.class);

        assertThat(count).isEqualTo(1);
    }
}
