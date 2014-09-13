package com.ninja_squad.spring.events;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;

/**
 * Configuration used by the EventTest.
 * @author JB Nizet
 */
@Configuration
@EnableEvents
@EnableAsync
public class EventTestConfig {
    @Bean
    public FakeEventObserver fakeEventObserver() {
        return new FakeEventObserver();
    }

    @Bean
    public FakeEventProducer fakeEventProducer() {
        return new FakeEventProducer();
    };

    @Bean
    public TransactionalFakeEventObserver transactionalFakeEventObserver() {
        return new TransactionalFakeEventObserver();
    }

    @Bean
    public AsyncFakeEventObserver asyncFakeEventObserver() {
        return new AsyncFakeEventObserver();
    }

    /**
     * Creates a fake Spring transaction manager
     */
    @Bean
    public PlatformTransactionManager transactionManager() {
        PlatformTransactionManager txManager = new AbstractPlatformTransactionManager() {

            @Override
            protected Object doGetTransaction() throws TransactionException {
                return new Object();
            }

            @Override
            protected void doBegin(Object transaction, TransactionDefinition definition) throws TransactionException {
                // nothing
            }

            @Override
            protected void doCommit(DefaultTransactionStatus status) throws TransactionException {
                // nothing
            }

            @Override
            protected void doRollback(DefaultTransactionStatus status) throws TransactionException {
                // nothing
            }
        };
        return txManager;
    }
}
