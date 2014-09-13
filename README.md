# spring-events

## Disclaimer

This is in now way an official Spring project. I'd like it to become one though.

## What is spring-events?

It's a tiny little library that brings CDI-like events to Spring. The goals of this project are

 - to be able to fire any kind of event in a Spring application. The events don't have to extend any specific class or
   to implement any specific interface
 - to be able to listen to events in an extremely simple way, by simply annotating a method
 - to be able to leverage the asynchronous method support of Spring to have events fired asynchronously and thus handled
   by a separate thread
 - to be able to be notified as soon as the event is fired, or after the current transaction is committed or rollbacked.
   This is quite an important point if the event is handled asynchronously. For example, if you want to asynchronously
   archive an invoice every time an invoice has been created, you need to be sure that the transaction which has created
   the invoice has been committed before trying to load the invoice from the database to archive it.
 - The listener should choose when to be notified, not the event producer or the event itself. Indeed, one observer
   might want to be notified of an invoice creation inside the transaction, in order for example to throw an exception
   if the invoice doesn't meet a condition, while other observers might only want to be notified after the trnsaction
   has been committed. The observers knows better when it should run then the event producer.

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
