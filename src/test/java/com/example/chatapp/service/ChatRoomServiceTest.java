package com.example.chatapp.service;

import com.example.chatapp.model.ChatMessage;
import com.example.chatapp.pubsub.RedisMessagePublisher;
import com.example.chatapp.repository.ChatMessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatRoomServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private HashOperations<String, Object, Object> hashOps;

    @Mock
    private RedisMessagePublisher publisher;

    @Mock
    private ChatMessageRepository repository;

    @InjectMocks
    private ChatRoomService service;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForHash()).thenReturn(hashOps);
    }

    // ---------- createChatRoom ----------

    @Test
    void createChatRoom_newRoom_storesRoomAndReturnsOk() {
        when(hashOps.hasKey("CHAT_ROOMS", "engineering")).thenReturn(false);

        ResponseEntity<?> response = service.createChatRoom("engineering");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(hashOps).put("CHAT_ROOMS", "engineering", "engineering");
    }

    @Test
    void createChatRoom_duplicate_returnsBadRequestAndDoesNotOverwrite() {
        when(hashOps.hasKey("CHAT_ROOMS", "engineering")).thenReturn(true);

        ResponseEntity<?> response = service.createChatRoom("engineering");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(hashOps, never()).put(any(), any(), any());
    }

    // ---------- sendMessage ----------

    @Test
    void sendMessage_roomMissing_returnsBadRequestAndDoesNothingElse() {
        when(hashOps.hasKey("CHAT_ROOMS", "ghost")).thenReturn(false);

        ResponseEntity<?> response = service.sendMessage("ghost", "Mohit", "hello");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verifyNoInteractions(repository);
        verifyNoInteractions(publisher);
    }

    @Test
    void sendMessage_savesToDatabaseFirst_thenPublishes() {
        when(hashOps.hasKey("CHAT_ROOMS", "engineering")).thenReturn(true);

        ResponseEntity<?> response = service.sendMessage("engineering", "Mohit", "hello");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        ArgumentCaptor<ChatMessage> saved = ArgumentCaptor.forClass(ChatMessage.class);
        InOrder order = inOrder(repository, publisher);
        order.verify(repository).save(saved.capture());
        order.verify(publisher).publish("chatroom", "Mohit: hello");

        assertThat(saved.getValue().getRoomId()).isEqualTo("engineering");
        assertThat(saved.getValue().getParticipant()).isEqualTo("Mohit");
        assertThat(saved.getValue().getContent()).isEqualTo("hello");
        assertThat(saved.getValue().getCreatedAt()).isNotNull();
    }

    @Test
    void sendMessage_whenPublishFails_messageWasStillSaved() {
        when(hashOps.hasKey("CHAT_ROOMS", "engineering")).thenReturn(true);
        doThrow(new RuntimeException("redis down"))
                .when(publisher).publish("chatroom", "Mohit: hello");

        assertThatThrownBy(() -> service.sendMessage("engineering", "Mohit", "hello"))
                .isInstanceOf(RuntimeException.class);

        verify(repository).save(any(ChatMessage.class));
    }

    // ---------- getChatHistory ----------

    @Test
    void getChatHistory_roomMissing_returnsBadRequestAndSkipsDatabase() {
        when(hashOps.hasKey("CHAT_ROOMS", "ghost")).thenReturn(false);

        ResponseEntity<?> response = service.getChatHistory("ghost", 10);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verifyNoInteractions(repository);
    }

    @Test
    @SuppressWarnings("unchecked")
    void getChatHistory_returnsOldestFirst() {
        when(hashOps.hasKey("CHAT_ROOMS", "engineering")).thenReturn(true);
        ChatMessage newer = new ChatMessage("engineering", "Mohit", "second");
        ChatMessage older = new ChatMessage("engineering", "Mohit", "first");
        // the repository returns newest first
        when(repository.findByRoomIdOrderByCreatedAtDesc(eq("engineering"), any()))
                .thenReturn(List.of(newer, older));

        ResponseEntity<?> response = service.getChatHistory("engineering", 10);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        List<Map<String, Object>> messages = (List<Map<String, Object>>) body.get("messages");
        assertThat(messages)
                .extracting(m -> m.get("message"))
                .containsExactly("first", "second");
    }

    @ParameterizedTest
    @CsvSource({"1000,100", "100,100", "10,10", "1,1", "0,1", "-5,1"})
    void getChatHistory_clampsLimitBetween1And100(int requested, int expectedPageSize) {
        when(hashOps.hasKey("CHAT_ROOMS", "engineering")).thenReturn(true);
        when(repository.findByRoomIdOrderByCreatedAtDesc(any(), any())).thenReturn(List.of());

        service.getChatHistory("engineering", requested);

        verify(repository).findByRoomIdOrderByCreatedAtDesc(
                "engineering", PageRequest.of(0, expectedPageSize));
    }
}