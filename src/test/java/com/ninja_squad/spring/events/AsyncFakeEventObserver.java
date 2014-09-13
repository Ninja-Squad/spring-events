package com.ninja_squad.spring.events;

import org.springframework.scheduling.annotation.Async;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Semaphore;

/**
 * Observer of FakeEvent that allows testing that an asynchronous observing method is indeed called asynchronously
 * @author JB Nizet
 */
public class AsyncFakeEventObserver {

    private List<String> result = new CopyOnWriteArrayList<String>();

    private final Semaphore addSemaphore = new Semaphore(0);
    private final Semaphore getSemaphore = new Semaphore(0);

    @Observes
    @Async
    public void observe(FakeEvent event) throws InterruptedException {
        addSemaphore.acquire();
        result.add("async");
        getSemaphore.release();
    }

    public void unblock() throws InterruptedException {
        addSemaphore.release();
        getSemaphore.acquire();
    }

    public List<String> getResult() {
        return result;
    }
}
