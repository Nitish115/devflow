package com.devflow.devflow.event;

import com.devflow.devflow.config.RabbitMQConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EventPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private EventPublisher eventPublisher;

    @Test
    void testPublishTaskAssigned() {
        TaskAssignedEvent event = new TaskAssignedEvent(1L, 10L, 100L, "Task 1", 2L, 1L, "owner@example.com");
        eventPublisher.publishTaskAssigned(event);

        verify(rabbitTemplate).convertAndSend(eq(RabbitMQConfig.EXCHANGE_NAME), eq(RabbitMQConfig.ROUTING_KEY_TASK_ASSIGNED), eq(event));
    }

    @Test
    void testPublishTaskStatusChanged() {
        TaskStatusChangedEvent event = new TaskStatusChangedEvent(1L, 10L, 100L, "Task 1", "TODO", "IN_PROGRESS", 1L, "owner@example.com", 2L, 1L);
        eventPublisher.publishTaskStatusChanged(event);

        verify(rabbitTemplate).convertAndSend(eq(RabbitMQConfig.EXCHANGE_NAME), eq(RabbitMQConfig.ROUTING_KEY_TASK_STATUS_CHANGED), eq(event));
    }

    @Test
    void testPublishCommentCreated() {
        CommentCreatedEvent event = new CommentCreatedEvent(50L, 1L, 10L, 100L, 1L, "owner@example.com", "Hello");
        eventPublisher.publishCommentCreated(event);

        verify(rabbitTemplate).convertAndSend(eq(RabbitMQConfig.EXCHANGE_NAME), eq(RabbitMQConfig.ROUTING_KEY_COMMENT_CREATED), eq(event));
    }

    @Test
    void testPublishCommentMentioned() {
        CommentMentionedEvent event = new CommentMentionedEvent(50L, 1L, 10L, 100L, 1L, "owner@example.com", 2L, "Hello @dev");
        eventPublisher.publishCommentMentioned(event);

        verify(rabbitTemplate).convertAndSend(eq(RabbitMQConfig.EXCHANGE_NAME), eq(RabbitMQConfig.ROUTING_KEY_COMMENT_MENTIONED), eq(event));
    }
}
