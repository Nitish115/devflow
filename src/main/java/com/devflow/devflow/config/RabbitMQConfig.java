package com.devflow.devflow.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "devflow.exchange";

    public static final String NOTIFICATION_QUEUE = "notification.queue";
    public static final String ACTIVITY_LOG_QUEUE = "activity-log.queue";

    public static final String ROUTING_KEY_TASK_ASSIGNED = "task.assigned";
    public static final String ROUTING_KEY_TASK_STATUS_CHANGED = "task.status-changed";
    public static final String ROUTING_KEY_COMMENT_CREATED = "comment.created";
    public static final String ROUTING_KEY_COMMENT_MENTIONED = "comment.mentioned";

    @Bean
    public TopicExchange devflowExchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable(NOTIFICATION_QUEUE).build();
    }

    @Bean
    public Queue activityLogQueue() {
        return QueueBuilder.durable(ACTIVITY_LOG_QUEUE).build();
    }

    @Bean
    public Binding notificationTaskAssignedBinding(Queue notificationQueue, TopicExchange devflowExchange) {
        return BindingBuilder.bind(notificationQueue).to(devflowExchange).with(ROUTING_KEY_TASK_ASSIGNED);
    }

    @Bean
    public Binding notificationTaskStatusChangedBinding(Queue notificationQueue, TopicExchange devflowExchange) {
        return BindingBuilder.bind(notificationQueue).to(devflowExchange).with(ROUTING_KEY_TASK_STATUS_CHANGED);
    }

    @Bean
    public Binding notificationCommentMentionedBinding(Queue notificationQueue, TopicExchange devflowExchange) {
        return BindingBuilder.bind(notificationQueue).to(devflowExchange).with(ROUTING_KEY_COMMENT_MENTIONED);
    }

    @Bean
    public Binding activityLogTaskBinding(Queue activityLogQueue, TopicExchange devflowExchange) {
        return BindingBuilder.bind(activityLogQueue).to(devflowExchange).with("task.*");
    }

    @Bean
    public Binding activityLogCommentBinding(Queue activityLogQueue, TopicExchange devflowExchange) {
        return BindingBuilder.bind(activityLogQueue).to(devflowExchange).with("comment.*");
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}
