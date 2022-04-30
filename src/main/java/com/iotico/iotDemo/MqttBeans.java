package com.iotico.iotDemo;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;

@Configuration
public class MqttBeans {
    //Mqtt client
    //connection configuration
    //channel inbound, channel outbound
    //message handler
    // handler receive messages

    public MqttPahoClientFactory mqttPahoClientFactory(){
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        // Authentication options
        MqttConnectOptions options = new MqttConnectOptions();
        options.setServerURIs(new String[] {"tcp://localhost:1883"});
        options.setUserName("admin");
        String pass = "12345678";
        options.setPassword(pass.toCharArray());
        options.setCleanSession(true);
        factory.setConnectionOptions(options);
        return factory;
    }

    @Bean
    public MessageChannel MqttInputChannel() {
        return new DirectChannel();
    }
    @Bean
    public MessageProducer Inbound(){
        // clientId, metodo, topic
        MqttPahoMessageDrivenChannelAdapter adapter = new MqttPahoMessageDrivenChannelAdapter("ServerIn", mqttPahoClientFactory(),"#");
        adapter.setCompletionTimeout(5000);
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setQos(2);
        adapter.setOutputChannel(MqttInputChannel());
        return adapter;
    }

    @Bean
    @ServiceActivator(inputChannel = "MqttInputChannel")
    public MessageHandler handler(){
        return new MessageHandler() {
            @Override
            public void handleMessage(Message<?> message) throws MessagingException {
                String topic = message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC).toString();
                if(topic.equals("mytopic")){
                    System.out.println("This is our topic");
                }
                System.out.println(message.getPayload());
            }
        };
    }

    @Bean
    public MessageChannel MqttOutputChannel(){
        return new DirectChannel();
    }

    @Bean
    @ServiceActivator(inputChannel = "MqttOutboundChannel")
    public MessageHandler MqttOutbound(){
        // clientId, metodo
        MqttPahoMessageHandler messageHandler = new MqttPahoMessageHandler("ServerOut", mqttPahoClientFactory());
        messageHandler.setAsync(true);
        messageHandler.setDefaultTopic("#");
        return messageHandler;
    }
}
