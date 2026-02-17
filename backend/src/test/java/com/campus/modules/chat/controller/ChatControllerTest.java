package com.campus.modules.chat.controller;

import com.campus.modules.auth.service.AuthService;
import com.campus.modules.chat.entity.Conversation;
import com.campus.modules.chat.entity.Message;
import com.campus.modules.chat.service.ChatService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ChatService chatService;

    @MockBean
    private AuthService authService;

    @MockBean
    private SimpMessagingTemplate messagingTemplate;

    private static final String BASE_URL = "/api";

    @Nested
    @DisplayName("GET /api/conversations - 获取会话列表")
    class GetConversationsTests {

        @Test
        @DisplayName("获取会话列表成功")
        void shouldGetConversationsSuccessfully() throws Exception {
            Conversation conversation = createTestConversation(1L, 2L);
            
            when(authService.getUserIdFromToken(anyString())).thenReturn(1L);
            when(chatService.getConversations(1L)).thenReturn(Collections.singletonList(conversation));

            mockMvc.perform(get(BASE_URL + "/conversations")
                    .header("Authorization", "Bearer mock-token"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isArray());
        }

        @Test
        @DisplayName("获取空会话列表")
        void shouldGetEmptyConversationsList() throws Exception {
            when(authService.getUserIdFromToken(anyString())).thenReturn(1L);
            when(chatService.getConversations(1L)).thenReturn(Collections.emptyList());

            mockMvc.perform(get(BASE_URL + "/conversations")
                    .header("Authorization", "Bearer mock-token"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data").isEmpty());
        }
    }

    @Nested
    @DisplayName("GET /api/conversations/{conversationId}/messages - 获取会话消息历史")
    class GetMessagesTests {

        @Test
        @DisplayName("获取会话消息历史成功")
        void shouldGetMessagesSuccessfully() throws Exception {
            Message message = createTestMessage(1L, 1L, 2L, "你好");
            
            when(authService.getUserIdFromToken(anyString())).thenReturn(1L);
            when(chatService.getMessages(anyLong(), anyInt(), anyInt()))
                    .thenReturn(Collections.singletonList(message));

            mockMvc.perform(get(BASE_URL + "/conversations/1/messages")
                    .header("Authorization", "Bearer mock-token")
                    .param("page", "1")
                    .param("size", "20"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isArray());
        }
    }

    @Nested
    @DisplayName("GET /api/messages/{userId} - 获取与特定用户的聊天消息")
    class GetMessagesWithUserTests {

        @Test
        @DisplayName("获取与特定用户的聊天消息成功")
        void shouldGetMessagesWithUserSuccessfully() throws Exception {
            Message message = createTestMessage(1L, 1L, 2L, "测试消息");
            
            when(authService.getUserIdFromToken(anyString())).thenReturn(1L);
            when(chatService.getMessagesWithUser(anyLong(), anyLong(), anyInt(), anyInt()))
                    .thenReturn(Collections.singletonList(message));

            mockMvc.perform(get(BASE_URL + "/messages/2")
                    .header("Authorization", "Bearer mock-token")
                    .param("page", "1")
                    .param("size", "20"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isArray());
        }

        @Test
        @DisplayName("获取与用户的聊天消息返回空列表")
        void shouldGetEmptyMessagesWithUser() throws Exception {
            when(authService.getUserIdFromToken(anyString())).thenReturn(1L);
            when(chatService.getMessagesWithUser(anyLong(), anyLong(), anyInt(), anyInt()))
                    .thenReturn(Collections.emptyList());

            mockMvc.perform(get(BASE_URL + "/messages/2")
                    .header("Authorization", "Bearer mock-token"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isEmpty());
        }
    }

    @Nested
    @DisplayName("POST /api/messages/{userId} - 发送消息给特定用户")
    class SendMessageToUserTests {

        @Test
        @DisplayName("发送消息成功")
        void shouldSendMessageSuccessfully() throws Exception {
            Map<String, String> request = new HashMap<>();
            request.put("content", "你好，这是一条测试消息");

            Message savedMessage = createTestMessage(1L, 1L, 2L, "你好，这是一条测试消息");
            savedMessage.setId(1L);

            when(authService.getUserIdFromToken(anyString())).thenReturn(1L);
            when(chatService.saveMessage(anyLong(), anyLong(), anyString())).thenReturn(savedMessage);

            mockMvc.perform(post(BASE_URL + "/messages/2")
                    .header("Authorization", "Bearer mock-token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.content").value("你好，这是一条测试消息"));
        }

        @Test
        @DisplayName("发送消息失败 - 消息内容为空")
        void shouldFailSendMessageWhenContentEmpty() throws Exception {
            Map<String, String> request = new HashMap<>();
            request.put("content", "");

            when(authService.getUserIdFromToken(anyString())).thenReturn(1L);

            mockMvc.perform(post(BASE_URL + "/messages/2")
                    .header("Authorization", "Bearer mock-token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(500))
                    .andExpect(jsonPath("$.message").value("消息内容不能为空"));
        }

        @Test
        @DisplayName("发送消息失败 - 消息内容仅包含空白")
        void shouldFailSendMessageWhenContentBlank() throws Exception {
            Map<String, String> request = new HashMap<>();
            request.put("content", "   ");

            when(authService.getUserIdFromToken(anyString())).thenReturn(1L);

            mockMvc.perform(post(BASE_URL + "/messages/2")
                    .header("Authorization", "Bearer mock-token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(500))
                    .andExpect(jsonPath("$.message").value("消息内容不能为空"));
        }
    }

    @Nested
    @DisplayName("POST /api/conversations - 创建会话")
    class CreateConversationTests {

        @Test
        @DisplayName("创建会话成功")
        void shouldCreateConversationSuccessfully() throws Exception {
            Map<String, Long> request = new HashMap<>();
            request.put("userId", 2L);

            Conversation conversation = createTestConversation(1L, 2L);

            when(authService.getUserIdFromToken(anyString())).thenReturn(1L);
            when(chatService.getOrCreateConversation(anyLong(), anyLong())).thenReturn(conversation);

            mockMvc.perform(post(BASE_URL + "/conversations")
                    .header("Authorization", "Bearer mock-token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }
    }

    @Nested
    @DisplayName("POST /api/conversations/{userId}/read - 清除未读消息数")
    class ClearUnreadCountTests {

        @Test
        @DisplayName("清除未读消息数成功")
        void shouldClearUnreadCountSuccessfully() throws Exception {
            when(authService.getUserIdFromToken(anyString())).thenReturn(1L);
            doNothing().when(chatService).clearUnreadCountByUserIds(anyLong(), anyLong());

            mockMvc.perform(post(BASE_URL + "/conversations/2/read")
                    .header("Authorization", "Bearer mock-token"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }
    }

    @Nested
    @DisplayName("GET /api/conversations/unread/count - 获取总未读消息数")
    class GetTotalUnreadCountTests {

        @Test
        @DisplayName("获取总未读消息数成功")
        void shouldGetTotalUnreadCountSuccessfully() throws Exception {
            when(authService.getUserIdFromToken(anyString())).thenReturn(1L);
            when(chatService.getTotalUnreadCount(1L)).thenReturn(5);

            mockMvc.perform(get(BASE_URL + "/conversations/unread/count")
                    .header("Authorization", "Bearer mock-token"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").value(5));
        }

        @Test
        @DisplayName("获取总未读消息数为零")
        void shouldGetZeroTotalUnreadCount() throws Exception {
            when(authService.getUserIdFromToken(anyString())).thenReturn(1L);
            when(chatService.getTotalUnreadCount(1L)).thenReturn(0);

            mockMvc.perform(get(BASE_URL + "/conversations/unread/count")
                    .header("Authorization", "Bearer mock-token"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").value(0));
        }
    }

    private Conversation createTestConversation(Long userId1, Long userId2) {
        Conversation conversation = new Conversation();
        conversation.setId(1L);
        conversation.setUserId1(userId1);
        conversation.setUserId2(userId2);
        conversation.setLastMessageContent("最后一条消息");
        conversation.setLastMessageTime(LocalDateTime.now());
        conversation.setUnreadCount1(0);
        conversation.setUnreadCount2(0);
        conversation.setCreatedAt(LocalDateTime.now());
        conversation.setUpdatedAt(LocalDateTime.now());
        return conversation;
    }

    private Message createTestMessage(Long id, Long senderId, Long receiverId, String content) {
        Message message = new Message();
        message.setId(id);
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setContent(content);
        message.setType(1);
        message.setCreatedAt(LocalDateTime.now());
        return message;
    }
}
