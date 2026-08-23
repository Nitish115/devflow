package com.devflow.devflow.event;

import com.devflow.devflow.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(EventPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    public EventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishTaskAssigned(TaskAssignedEvent event) {
        publish(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.ROUTING_KEY_TASK_ASSIGNED, event);
    }

    public void publishTaskStatusChanged(TaskStatusChangedEvent event) {
        publish(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.ROUTING_KEY_TASK_STATUS_CHANGED, event);
    }

    public void publishCommentCreated(CommentCreatedEvent event) {
        publish(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.ROUTING_KEY_COMMENT_CREATED, event);
    }

    public void publishCommentMentioned(CommentMentionedEvent event) {
        publish(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.ROUTING_KEY_COMMENT_MENTIONED, event);
    }

    private void publish(String exchange, String routingKey, Object payload) {
        try {
            log.info("Publishing event to exchange '{}' with routing key '{}': {}", exchange, routingKey, payload);
            rabbitTemplate.convertAndSend(exchange, routingKey, payload);
        } catch (Exception e) {
            log.warn("Failed to publish event to RabbitMQ (exchange: {}, routingKey: {}): {}", exchange, routingKey, e.getMessage());
        }
    }
}
