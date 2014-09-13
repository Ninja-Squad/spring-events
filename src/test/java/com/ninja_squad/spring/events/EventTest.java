package com.ninja_squad.spring.events;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

import static org.fest.assertions.api.Assertions.*;

/**
 * Test for the event observing/firing mechanism
 * @author JB Nizet
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = EventTestConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class EventTest {

    @Autowired
    private FakeEventProducer producer;

    @Autowired
    private FakeEventObserver observer;

    @Autowired
    private TransactionalFakeEventObserver transactionalObserver;

    @Autowired
    private AsyncFakeEventObserver asyncObserver;

    @Autowired
    private PlatformTransactionManager txManager;

    @Test
    public void shouldSupportAllMethodVisibilities() {
        producer.produce();
        List<String> result = observer.getVisibilities();
        assertThat(result).contains("publicInProgress", "protectedInProgress", "packageInProgress", "privateInProgress");
    }

    @Test
    public void shouldSupportEventSuperClassesAndInterfaces() {
        producer.produce();
        List<String> result = observer.getInheritance();
        assertThat(result).hasSize(4)
                          .containsOnly("withInterface", "withSuperInterface", "withSuperclass", "withObject");
    }

    @Test
    public void shouldNotFireAfterCommitAndAfterRollbackIfNoTransaction() {
        producer.produce();
        assertThat(transactionalObserver.getResult()).isEmpty();
    }

    @Test
    public void shouldFireAfterCommit() {
        new TransactionTemplate(txManager).execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                producer.produce();
                assertThat(transactionalObserver.getResult()).isEmpty();
            }
        });
        assertThat(transactionalObserver.getResult()).containsOnly("afterCommit");
    }

    @Test
    public void shouldFireAfterRollback() {
        try {
            new TransactionTemplate(txManager).execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(TransactionStatus status) {
                    producer.produce();
                    assertThat(transactionalObserver.getResult()).isEmpty();

                    // cause a rollback
                    throw new RuntimeException();
                }
            });
        }
        catch (Exception e) {
            // ignore
        }
        assertThat(transactionalObserver.getResult()).containsOnly("afterRollback");
    }

    @Test
    public void shouldSupportAsyncObservers() throws InterruptedException {
        producer.produce();
        assertThat(asyncObserver.getResult()).isEmpty();
        asyncObserver.unblock();
        assertThat(asyncObserver.getResult()).containsOnly("async");
    }
}
