package com.ninja_squad.spring.events;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration enabling the needed bean post-processing to support events
 * @author JB Nizet
 */
@Configuration
public class EventConfiguration {
    @Bean
    public EventObserverBeanPostProcessor eventFirer() {
        return new EventObserverBeanPostProcessor();
    }
}
