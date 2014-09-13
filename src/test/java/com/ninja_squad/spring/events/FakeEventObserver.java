package com.ninja_squad.spring.events;

import java.util.ArrayList;
import java.util.List;

/**
 * An event observer used to test that the various method visibilities and the inheritance is working correctly.
 * @author JB Nizet
 */
public class FakeEventObserver {

    private List<String> visibilities = new ArrayList<String>();
    private List<String> inheritance = new ArrayList<String>();

    @Observes
    public void publicInProgress(FakeEvent event) {
        visibilities.add("publicInProgress");
    }

    @Observes
    protected void protectedInProgress(FakeEvent event) {
        visibilities.add("protectedInProgress");
    }

    @Observes
    void packageInProgress(FakeEvent event) {
        visibilities.add("packageInProgress");
    }

    @Observes
    private void privateInProgress(FakeEvent event) {
        visibilities.add("privateInProgress");
    }

    @Observes
    public void withInterface(FakeEventInterface event) {
        inheritance.add("withInterface");
    }

    @Observes
    public void withSuperInterface(FakeEventSuperInterface event) {
        inheritance.add("withSuperInterface");
    }

    @Observes
    public void withSuperclass(AbstractFakeEvent event) {
        inheritance.add("withSuperclass");
    }

    @Observes
    public void withObject(Object event) {
        inheritance.add("withObject");
    }

    public List<String> getVisibilities() {
        return visibilities;
    }

    public List<String> getInheritance() {
        return inheritance;
    }
}
