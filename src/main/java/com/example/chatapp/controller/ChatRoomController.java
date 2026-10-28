package com.example.chatapp.controller;

import com.example.chatapp.service.ChatRoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/chatapp/chatrooms")
@Tag(name = "Chat rooms", description = "Create rooms, join them, send messages and read history")
public class ChatRoomController {

    @Autowired
    private ChatRoomService chatRoomService;

    @Operation(summary = "Create a chat room",
            description = "Rooms are stored in Redis. Returns 400 if the room already exists.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(value = "{\"roomName\":\"engineering\"}")))
    @PostMapping
    public ResponseEntity<?> createChatRoom(@RequestBody Map<String, String> request) {
        String roomName = request.get("roomName");
        return chatRoomService.createChatRoom(roomName);
    }

    @Operation(summary = "Join a chat room",
            description = "Adds the participant to the room's participant set in Redis. Returns 400 if the room does not exist.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(value = "{\"participant\":\"Mohit\"}")))
    @PostMapping("/{roomId}/join")
    public ResponseEntity<?> joinChatRoom(@PathVariable String roomId, @RequestBody Map<String, String> request) {
        String participant = request.get("participant");
        return chatRoomService.joinChatRoom(roomId, participant);
    }

    @Operation(summary = "Get chat history",
            description = "Reads the latest messages from PostgreSQL, returned oldest to newest. The limit is clamped to 1-100.")
    @GetMapping("/{roomId}/messages")
    public ResponseEntity<?> getChatHistory(
            @PathVariable String roomId,
            @Parameter(description = "Number of latest messages (1-100)", example = "10")
            @RequestParam(defaultValue = "10") int limit) {
        return chatRoomService.getChatHistory(roomId, limit);
    }

    @Operation(summary = "Send a message",
            description = "Saves the message to PostgreSQL first, then publishes it on the Redis Pub/Sub channel.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(value = "{\"participant\":\"Mohit\",\"message\":\"Hello team\"}")))
    @PostMapping("/{roomId}/messages")
    public ResponseEntity<?> sendMessage(@PathVariable String roomId, @RequestBody Map<String, String> request) {
        String participant = request.get("participant");
        String message = request.get("message");
        return chatRoomService.sendMessage(roomId, participant, message);
    }
}
