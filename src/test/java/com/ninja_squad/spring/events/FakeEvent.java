package com.ninja_squad.spring.events;

/**
 * An event type, which has a superclass and several interfaces, in order to test the inheritance mechanism
 * @author JB Nizet
 */
public class FakeEvent extends AbstractFakeEvent implements FakeEventInterface, FakeEventSuperInterface {
}
