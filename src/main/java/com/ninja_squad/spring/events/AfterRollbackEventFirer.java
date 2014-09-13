package com.ninja_squad.spring.events;

import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;

/**
 * Event firer which registers an transaction synchronization in order to call an observing method afer the transaction
 * is rollbacked. If transaction synchronization is not active, this firer dosn't do anything.
 * @author JB Nizet
 */
class AfterRollbackEventFirer implements EventFirer {

    private final Object bean;
    private final Method method;

    public AfterRollbackEventFirer(Object bean, Method method) {
        this.bean = bean;
        this.method = method;
    }

    @Override
    public void fire(final Object event) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
                @Override
                public void afterCompletion(int status) {
                    if (status == TransactionSynchronization.STATUS_ROLLED_BACK) {
                        ReflectionUtils.invokeMethod(method, bean, event);
                    }
                }
            });
        }
    }
}
