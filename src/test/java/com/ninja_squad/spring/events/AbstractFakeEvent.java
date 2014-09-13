package com.ninja_squad.spring.events;

/**
 * A superclass for FakeEvent, used to test that firing a FakeEvent will cause the observers of this superclass to
 * be called.
 * @author JB Nizet
 */
public abstract class AbstractFakeEvent implements FakeEventSuperInterface, FakeEventInterface {
}
