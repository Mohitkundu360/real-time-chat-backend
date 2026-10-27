package com.example.chatapp.service;

import com.example.chatapp.model.ChatMessage;
import com.example.chatapp.pubsub.RedisMessagePublisher;
import com.example.chatapp.repository.ChatMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ChatRoomService {

    private static final String CHAT_ROOMS_KEY = "CHAT_ROOMS";
    private static final int MAX_HISTORY_LIMIT = 100;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private RedisMessagePublisher redisMessagePublisher;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    /**
     * Create a chat room.
     */
    public ResponseEntity<?> createChatRoom(String roomName) {
        Boolean roomExists = redisTemplate.opsForHash().hasKey(CHAT_ROOMS_KEY, roomName);
        if (roomExists != null && roomExists) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Chat room '" + roomName + "' already exists.");
            response.put("status", "error");
            return ResponseEntity.badRequest().body(response);
        }

        // Create the chat room in Redis
        redisTemplate.opsForHash().put(CHAT_ROOMS_KEY, roomName, roomName);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Chat room '" + roomName + "' created successfully.");
        response.put("roomId", roomName);
        response.put("status", "success");

        return ResponseEntity.ok(response);
    }

    /**
     * Join a chat room.
     */
    public ResponseEntity<?> joinChatRoom(String roomId, String participant) {
        Boolean roomExists = redisTemplate.opsForHash().hasKey(CHAT_ROOMS_KEY, roomId);
        if (roomExists == null || !roomExists) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Chat room '" + roomId + "' does not exist.");
            response.put("status", "error");
            return ResponseEntity.badRequest().body(response);
        }

        // Add participant to the Redis Set for this room
        String participantsKey = "CHAT_ROOM:" + roomId + ":PARTICIPANTS";
        redisTemplate.opsForSet().add(participantsKey, participant);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "User '" + participant + "' joined chat room '" + roomId + "'.");
        response.put("status", "success");

        return ResponseEntity.ok(response);
    }

    /**
     * Retrieve chat history for a chat room from PostgreSQL.
     * Returns the latest {limit} messages in chronological order.
     */
    @Transactional(readOnly = true)
    public ResponseEntity<?> getChatHistory(String roomId, int limit) {
        Boolean roomExists = redisTemplate.opsForHash().hasKey(CHAT_ROOMS_KEY, roomId);
        if (roomExists == null || !roomExists) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Chat room '" + roomId + "' does not exist.");
            response.put("status", "error");
            return ResponseEntity.badRequest().body(response);
        }

        int safeLimit = Math.max(1, Math.min(limit, MAX_HISTORY_LIMIT));
        List<ChatMessage> newestFirst = chatMessageRepository
                .findByRoomIdOrderByCreatedAtDesc(roomId, PageRequest.of(0, safeLimit));

        // Reverse so the client receives oldest to newest
        List<Map<String, Object>> messages = new ArrayList<>();
        for (int i = newestFirst.size() - 1; i >= 0; i--) {
            ChatMessage row = newestFirst.get(i);
            Map<String, Object> item = new HashMap<>();
            item.put("participant", row.getParticipant());
            item.put("message", row.getContent());
            item.put("timestamp", row.getCreatedAt().toString());
            messages.add(item);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("messages", messages);

        return ResponseEntity.ok(response);
    }

    /**
     * Persist the message in PostgreSQL, then publish via Redis Pub/Sub.
     */
    public ResponseEntity<?> sendMessage(String roomId, String participant, String message) {
        Boolean roomExists = redisTemplate.opsForHash().hasKey(CHAT_ROOMS_KEY, roomId);
        if (roomExists == null || !roomExists) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Chat room '" + roomId + "' does not exist.");
            response.put("status", "error");
            return ResponseEntity.badRequest().body(response);
        }

        // Durable write first, so a Pub/Sub failure never loses history
        chatMessageRepository.save(new ChatMessage(roomId, participant, message));

        // Real-time delivery
        redisMessagePublisher.publish("chatroom", participant + ": " + message);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Message sent successfully.");
        response.put("status", "success");

        return ResponseEntity.ok(response);
    }
}