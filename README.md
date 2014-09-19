# spring-events

Build status on Travis CI

[![Build Status](https://secure.travis-ci.org/Ninja-Squad/spring-events.png)](http://travis-ci.org/Ninja-Squad/spring-events)


## Disclaimer

This is in no way an official Spring project. I'd like it to become one though.

## What is spring-events?

It's a tiny little library that brings CDI-like transaction-bound events to Spring. The goals of this project are:

 - to be able to fire any kind of event in a Spring application. The events don't have to extend any specific class or
   to implement any specific interface.
 - to be able to listen to events in an extremely simple way, by simply annotating a method.
 - to be able to leverage the asynchronous method support of Spring to have events fired asynchronously and thus handled
   by a separate thread.
 - to be able to be notified as soon as the event is fired, or after the current transaction is committed or rollbacked.
   This is quite an important point if the event is handled asynchronously. For example, if you want to asynchronously
   archive an invoice every time an invoice has been created, you need to be sure that the transaction which has created
   the invoice has been committed before trying to load the invoice from the database to archive it.
 - to be able to be notified of several event types using a single method, using inheritance. For example, a method
   observing the base class InvoiceLifeCycleEvent would be notified of the events InvoiceCreated and InvoiceCanceled,
   extending InvoiceLifeCycleEvent.
 - to let the observer choose when to be notified, not the event producer or the event itself. Indeed, one observer
   might want to be notified of an invoice creation inside the transaction, in order for example to throw an exception
   if the invoice doesn't meet a condition, while other observers might only want to be notified after the transaction
   has been committed. The observers knows better when it should run than the event producer.

## Installation

spring-events is available in Maven Central. Add the following dependency to your gradle build file:

    compile 'com.ninja-squad:spring-events:1.0'

Or, if using Maven,

    <dependency>
        <groupId>com.ninja-squad</groupId>
        <artifactId>spring-events</artifactId>
        <version>1.0</version>
    </dependency>

spring-events requires Java 6 or later.

## Example usage

1. Annotate your configuration class with `@EnableEvents`

        @Configuration
        @EnableEvents
        public class AppConfig {
            ...
        }

2. Autowire the EventFirer bean added to the context by the `@EnableEvents` annotation, in order to be able to fire
   events:

        @Autowired
        private EventFirer eventFirer;

        @Transactional
        public void createInvoice(...) {
            ...
            eventFirer.fire(new InvoiceCreated(invoiceId);
        }

3. Annotate a singleton bean method to be notified of the event only when the transaction has been committed:

        @Component
        private class InvoiceArchiver {
            @Observes(when = EventMoment.AFTER_COMMIT)
            @Async
            public void archiveInvoice(InvoiceCreated event) {
                ...
            }
        }
