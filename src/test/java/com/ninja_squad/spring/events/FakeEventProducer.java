package com.ninja_squad.spring.events;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * A bean which fires FakeEvent instances
 * @author JB Nizet
 */
public class FakeEventProducer {

    @Autowired
    private EventFirer eventFirer;

    public void produce() {
        eventFirer.fire(new FakeEvent());
    }
}
