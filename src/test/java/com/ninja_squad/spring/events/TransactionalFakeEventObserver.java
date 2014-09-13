package com.ninja_squad.spring.events;

import java.util.ArrayList;
import java.util.List;

/**
 * Observer observing fake events in a transactional way (after commit and after rollback)
 * @author JB Nizet
 */
public class TransactionalFakeEventObserver {

    private List<String> result = new ArrayList<String>();

    @Observes(when = EventMoment.AFTER_COMMIT)
    public void afterCommit(FakeEvent event) {
        result.add("afterCommit");
    }

    @Observes(when = EventMoment.AFTER_ROLLBACK)
    public void afterRollback(FakeEvent event) {
        result.add("afterRollback");
    }

    public List<String> getResult() {
        return result;
    }
}
