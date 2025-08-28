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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;

@Configuration
public class RabbitConfig {

    @Value("${spring.rabbitmq.host:localhost}")
    private String rabbitmqHost;

    @Value("${spring.rabbitmq.port:5672}")
    private int rabbitmqPort;

    @Value("${spring.rabbitmq.username:admin}")
    private String rabbitmqUsername;

    @Value("${spring.rabbitmq.password:admin123}")
    private String rabbitmqPassword;

    private static final String CHAT_QUEUE_NAME = "chat.queue";
    private static final String CHAT_EXCHANGE_NAME = "chat.exchange";
    private static final String ROUTING_KEY = "chat.room.*";

    @Bean
    public Queue queue() {
        // 큐 자동 생성 및 지속성 설정
        return new Queue(CHAT_QUEUE_NAME, true, false, false);
    }

    @Bean
    public TopicExchange exchange() {
        // 익스체인지 자동 생성 및 지속성 설정
        return new TopicExchange(CHAT_EXCHANGE_NAME, true, false);
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
        factory.setHost(rabbitmqHost);
        factory.setPort(rabbitmqPort);
        factory.setUsername(rabbitmqUsername);
        factory.setPassword(rabbitmqPassword);

        // RabbitMQ 연결 안정성 설정
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

        // 메시지 전송 실패 시 재시도 설정
        template.setRetryTemplate(null);

        // 연결 확인
        template.setConnectionFactory(connectionFactory);

        return template;
    }

    @Bean
    public SimpleMessageListenerContainer container(ConnectionFactory connectionFactory) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(CHAT_QUEUE_NAME);

        // 에러 처리 설정
        container.setDefaultRequeueRejected(false);
        container.setErrorHandler(throwable -> {
            System.err.println("RabbitMQ 메시지 처리 오류: " + throwable.getMessage());
        });

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
