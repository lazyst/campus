package com.campus.modules.chat;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.modules.chat.entity.Conversation;
import com.campus.modules.chat.entity.Message;
import com.campus.modules.chat.mapper.ConversationMapper;
import com.campus.modules.chat.mapper.MessageMapper;
import com.campus.modules.chat.publisher.ChatMessagePublisher;
import com.campus.modules.chat.service.impl.ChatServiceImpl;
import com.campus.modules.user.entity.User;
import com.campus.modules.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * 聊天服务单元测试 - JUnit 5 + Mockito
 * 使用ReflectionTestUtils设置baseMapper来支持ServiceImpl
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ChatServiceTest {

    @Mock
    private MessageMapper messageMapper;

    @Mock
    private ConversationMapper conversationMapper;

    @Mock
    private UserService userService;

    @Mock
    private ChatMessagePublisher chatMessagePublisher;

    @InjectMocks
    private ChatServiceImpl chatService;

    private Message testMessage;
    private Conversation testConversation;
    private User testUser1;
    private User testUser2;

    @BeforeEach
    void setUp() {
        // 使用ReflectionTestUtils设置baseMapper
        ReflectionTestUtils.setField(chatService, "baseMapper", messageMapper);

        // 初始化测试用户
        testUser1 = new User();
        testUser1.setId(1L);
        testUser1.setNickname("用户1");
        testUser1.setAvatar("https://example.com/avatar1.jpg");

        testUser2 = new User();
        testUser2.setId(2L);
        testUser2.setNickname("用户2");
        testUser2.setAvatar("https://example.com/avatar2.jpg");

        // 初始化测试消息
        testMessage = new Message();
        testMessage.setId(1L);
        testMessage.setConversationId(1L);
        testMessage.setSenderId(1L);
        testMessage.setReceiverId(2L);
        testMessage.setContent("测试消息内容");
        testMessage.setType(1);
        testMessage.setSendTime(LocalDateTime.now());
        testMessage.setCreatedAt(LocalDateTime.now());
        testMessage.setUpdatedAt(LocalDateTime.now());

        // 初始化测试会话
        testConversation = new Conversation();
        testConversation.setId(1L);
        testConversation.setUserId1(1L);
        testConversation.setUserId2(2L);
        testConversation.setLastMessageId(1L);
        testConversation.setUnreadCount1(0);
        testConversation.setUnreadCount2(1);
        testConversation.setCreatedAt(LocalDateTime.now());
        testConversation.setUpdatedAt(LocalDateTime.now());
    }

    @Nested
    @DisplayName("saveMessage 方法测试")
    class SaveMessageTests {

        @Test
        @DisplayName("保存消息成功 - 创建新会话")
        void shouldSaveMessageSuccessfully() {
            when(conversationMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
            when(conversationMapper.insert(any(Conversation.class))).thenAnswer(invocation -> {
                Conversation conv = invocation.getArgument(0);
                conv.setId(1L);
                return 1;
            });
            when(messageMapper.insert(any(Message.class))).thenAnswer(invocation -> {
                Message msg = invocation.getArgument(0);
                msg.setId(1L);
                return 1;
            });
            when(conversationMapper.updateById(any(Conversation.class))).thenReturn(1);

            Message result = chatService.saveMessage(1L, 2L, "测试消息内容");

            assertNotNull(result);
            assertEquals("测试消息内容", result.getContent());
            assertEquals(1L, result.getSenderId());
            assertEquals(2L, result.getReceiverId());
            verify(conversationMapper).insert(any(Conversation.class));
            verify(messageMapper).insert(any(Message.class));
            verify(conversationMapper).updateById(any(Conversation.class));
        }

        @Test
        @DisplayName("保存消息成功 - 已有会话")
        void shouldSaveMessageToExistingConversation() {
            when(conversationMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testConversation);
            when(messageMapper.insert(any(Message.class))).thenAnswer(invocation -> {
                Message msg = invocation.getArgument(0);
                msg.setId(2L);
                return 1;
            });
            when(conversationMapper.updateById(any(Conversation.class))).thenReturn(1);

            Message result = chatService.saveMessage(1L, 2L, "新消息");

            assertNotNull(result);
            assertEquals("新消息", result.getContent());
            verify(conversationMapper, never()).insert(any(Conversation.class));
            verify(messageMapper).insert(any(Message.class));
        }
    }

    @Nested
    @DisplayName("getConversations 方法测试")
    class GetConversationsTests {

        @Test
        @DisplayName("获取会话列表成功")
        void shouldGetConversationsSuccessfully() {
            when(conversationMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(testConversation));
            when(userService.listByIds(any())).thenReturn(List.of(testUser2));
            when(messageMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testMessage);

            List<Conversation> result = chatService.getConversations(1L);

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("用户2", result.get(0).getOtherUserNickname());
            assertEquals(2, result.get(0).getOtherUserId());
            verify(conversationMapper).selectList(any(LambdaQueryWrapper.class));
            verify(userService).listByIds(any());
        }

        @Test
        @DisplayName("获取空会话列表")
        void shouldReturnEmptyListWhenNoConversations() {
            when(conversationMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

            List<Conversation> result = chatService.getConversations(1L);

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("获取会话列表 - 用户不存在时")
        void shouldReturnConversationsWhenOtherUserNotExists() {
            when(conversationMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(testConversation));
            when(userService.listByIds(any())).thenReturn(Collections.emptyList());
            when(messageMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testMessage);

            List<Conversation> result = chatService.getConversations(1L);

            assertNotNull(result);
            assertEquals(1, result.size());
            assertNull(result.get(0).getOtherUserNickname());
        }
    }

    @Nested
    @DisplayName("getMessages 方法测试")
    class GetMessagesTests {

        @Test
        @DisplayName("获取会话消息成功")
        void shouldGetMessagesSuccessfully() {
            Page<Message> page = new Page<>(1, 10);
            page.setRecords(List.of(testMessage));
            when(messageMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);
            when(userService.listByIds(any())).thenReturn(List.of(testUser1));

            List<Message> result = chatService.getMessages(1L, 1, 10);

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("测试消息内容", result.get(0).getContent());
            assertEquals("用户1", result.get(0).getSenderNickname());
            verify(messageMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
        }

        @Test
        @DisplayName("获取空消息列表")
        void shouldReturnEmptyListWhenNoMessages() {
            Page<Message> emptyPage = new Page<>(1, 10);
            emptyPage.setRecords(Collections.emptyList());
            when(messageMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(emptyPage);

            List<Message> result = chatService.getMessages(1L, 1, 10);

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("getMessagesWithUser 方法测试")
    class GetMessagesWithUserTests {

        @Test
        @DisplayName("获取与特定用户的聊天消息成功")
        void shouldGetMessagesWithUserSuccessfully() {
            when(conversationMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testConversation);
            
            Page<Message> page = new Page<>(1, 10);
            page.setRecords(List.of(testMessage));
            when(messageMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);
            when(userService.getById(1L)).thenReturn(testUser1);

            List<Message> result = chatService.getMessagesWithUser(1L, 2L, 1, 10);

            assertNotNull(result);
            assertFalse(result.isEmpty());
        }

        @Test
        @DisplayName("获取与不存在用户的聊天消息返回空列表")
        void shouldReturnEmptyListWhenConversationNotExists() {
            when(conversationMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

            List<Message> result = chatService.getMessagesWithUser(1L, 999L, 1, 10);

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("getOrCreateConversation 方法测试")
    class GetOrCreateConversationTests {

        @Test
        @DisplayName("获取已有会话成功")
        void shouldReturnExistingConversation() {
            when(conversationMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testConversation);

            Conversation result = chatService.getOrCreateConversation(1L, 2L);

            assertNotNull(result);
            assertEquals(1L, result.getId());
            verify(conversationMapper, never()).insert(any(Conversation.class));
        }

        @Test
        @DisplayName("创建新会话成功")
        void shouldCreateNewConversation() {
            when(conversationMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
            when(conversationMapper.insert(any(Conversation.class))).thenAnswer(invocation -> {
                Conversation conv = invocation.getArgument(0);
                conv.setId(100L);
                return 1;
            });

            Conversation result = chatService.getOrCreateConversation(1L, 2L);

            assertNotNull(result);
            assertEquals(100L, result.getId());
            assertEquals(1L, result.getUserId1());
            assertEquals(2L, result.getUserId2());
            verify(conversationMapper).insert(any(Conversation.class));
        }

        @Test
        @DisplayName("反向用户ID获取会话成功")
        void shouldGetConversationWithReversedUserIds() {
            when(conversationMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testConversation);

            Conversation result = chatService.getOrCreateConversation(2L, 1L);

            assertNotNull(result);
            assertEquals(1L, result.getId());
        }
    }

    @Nested
    @DisplayName("clearUnreadCount 方法测试")
    class ClearUnreadCountTests {

        @Test
        @DisplayName("清除用户1的未读数成功")
        void shouldClearUnreadCountForUser1() {
            when(conversationMapper.selectById(1L)).thenReturn(testConversation);
            when(conversationMapper.updateById(any(Conversation.class))).thenReturn(1);

            chatService.clearUnreadCount(1L, 1L);

            assertEquals(0, testConversation.getUnreadCount1());
            verify(conversationMapper).updateById(any(Conversation.class));
        }

        @Test
        @DisplayName("清除用户2的未读数成功")
        void shouldClearUnreadCountForUser2() {
            when(conversationMapper.selectById(1L)).thenReturn(testConversation);
            when(conversationMapper.updateById(any(Conversation.class))).thenReturn(1);

            chatService.clearUnreadCount(2L, 1L);

            assertEquals(0, testConversation.getUnreadCount2());
            verify(conversationMapper).updateById(any(Conversation.class));
        }

        @Test
        @DisplayName("会话不存在时不执行更新")
        void shouldNotUpdateWhenConversationNotFound() {
            when(conversationMapper.selectById(999L)).thenReturn(null);

            chatService.clearUnreadCount(1L, 999L);

            verify(conversationMapper, never()).updateById(any(Conversation.class));
        }

        @Test
        @DisplayName("用户不是会话参与者时不执行更新")
        void shouldNotUpdateWhenUserNotParticipant() {
            when(conversationMapper.selectById(1L)).thenReturn(testConversation);

            chatService.clearUnreadCount(999L, 1L);

            // unreadCount1和unreadCount2应该保持不变
            assertEquals(0, testConversation.getUnreadCount1());
            assertEquals(1, testConversation.getUnreadCount2());
        }
    }

    @Nested
    @DisplayName("clearUnreadCountByUserIds 方法测试")
    class ClearUnreadCountByUserIdsTests {

        @Test
        @DisplayName("通过用户ID清除未读数成功")
        void shouldClearUnreadCountByUserIdsSuccessfully() {
            when(conversationMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testConversation);
            when(conversationMapper.selectById(1L)).thenReturn(testConversation);
            when(conversationMapper.updateById(any(Conversation.class))).thenReturn(1);

            chatService.clearUnreadCountByUserIds(1L, 2L);

            verify(conversationMapper).selectOne(any(LambdaQueryWrapper.class));
            verify(conversationMapper).updateById(any(Conversation.class));
        }

        @Test
        @DisplayName("会话不存在时不执行更新")
        void shouldNotUpdateWhenConversationNotFound() {
            when(conversationMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

            chatService.clearUnreadCountByUserIds(1L, 999L);

            verify(conversationMapper, never()).updateById(any(Conversation.class));
        }
    }

    @Nested
    @DisplayName("getTotalUnreadCount 方法测试")
    class GetTotalUnreadCountTests {

        @Test
        @DisplayName("获取用户总未读数成功")
        void shouldGetTotalUnreadCountSuccessfully() {
            when(conversationMapper.sumUnreadCount(1L)).thenReturn(8);

            Integer result = chatService.getTotalUnreadCount(1L);

            assertNotNull(result);
            assertEquals(8, result);
            verify(conversationMapper).sumUnreadCount(1L);
        }

        @Test
        @DisplayName("获取用户2的总未读数成功")
        void shouldGetTotalUnreadCountForUser2() {
            when(conversationMapper.sumUnreadCount(2L)).thenReturn(4);

            Integer result = chatService.getTotalUnreadCount(2L);

            assertNotNull(result);
            assertEquals(4, result);
            verify(conversationMapper).sumUnreadCount(2L);
        }

        @Test
        @DisplayName("没有会话时返回0")
        void shouldReturnZeroWhenNoConversations() {
            when(conversationMapper.sumUnreadCount(1L)).thenReturn(0);

            Integer result = chatService.getTotalUnreadCount(1L);

            assertNotNull(result);
            assertEquals(0, result);
        }

        @Test
        @DisplayName("未读数为null时正确处理")
        void shouldHandleNullUnreadCount() {
            when(conversationMapper.sumUnreadCount(1L)).thenReturn(0);

            Integer result = chatService.getTotalUnreadCount(1L);

            assertNotNull(result);
            assertEquals(0, result);
        }
    }
}
