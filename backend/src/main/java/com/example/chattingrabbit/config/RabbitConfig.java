package com.example.chattingrabbit.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;

@Configuration
public class RabbitConfig {

    private static final String CHAT_QUEUE_NAME = "chat.queue";
    private static final String CHAT_EXCHANGE_NAME = "chat.exchange";
    private static final String ROUTING_KEY = "chat.room.*";

    @Bean
    public Queue queue() {
        return new Queue(CHAT_QUEUE_NAME, true);
    }

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(CHAT_EXCHANGE_NAME);
    }

    @Bean
    public Binding binding(Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory factory = new CachingConnectionFactory();
        factory.setHost("host.docker.internal");
        factory.setPort(5672);
        factory.setUsername("admin");
        factory.setPassword("admin123");

        // RabbitMQ 4.1.3 호환 설정
        factory.setConnectionTimeout(30000); // 30초
        factory.setRequestedHeartBeat(60); // 60초
        factory.setChannelCacheSize(25); // 채널 캐시 크기

        // 연결 풀 설정
        factory.setChannelCheckoutTimeout(0);
        factory.setConnectionCacheSize(1);

        return factory;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setExchange(CHAT_EXCHANGE_NAME);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }

    @Bean
    public SimpleMessageListenerContainer container(ConnectionFactory connectionFactory) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(CHAT_QUEUE_NAME);

        // MessageListenerAdapter에 실제 메시지 핸들러 설정
        MessageListenerAdapter messageListener = new MessageListenerAdapter(new ChatMessageHandler(), "handleMessage");
        messageListener.setMessageConverter(jsonMessageConverter());
        container.setMessageListener(messageListener);

        return container;
    }

    // 메시지 핸들러 클래스
    public static class ChatMessageHandler {
        public void handleMessage(String message) {
            // RabbitMQ에서 받은 메시지 처리 로직
            System.out.println("Received message from RabbitMQ: " + message);
        }
    }
}
