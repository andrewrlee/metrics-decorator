package uk.co.optimisticpanda.instrumentation.metrics;

import static uk.co.optimisticpanda.instrumentation.metrics.Metered.Type.AFTER;
import static uk.co.optimisticpanda.instrumentation.metrics.Metered.Type.BEFORE;
import static uk.co.optimisticpanda.instrumentation.metrics.Metered.Type.EXCEPTION;
import static uk.co.optimisticpanda.instrumentation.metrics.Metered.Type.FINALLY;

import java.util.concurrent.TimeUnit;

import uk.co.optimisticpanda.instrumentation.metrics.Metered;
import uk.co.optimisticpanda.instrumentation.metrics.Timer;

public class TestService {

    public static interface Publisher {
        void accept(int count);
    }
    
    private Publisher publisher;

    public TestService(Publisher publisher) {
        this.publisher = publisher;
    }
    
    public void nonAnnotatedDoAVoidThing(int count) {
        publisher.accept(count);
    }
    
    
    @Metered(when = BEFORE)
    public void doAVoidThing(int count) {
        publisher.accept(count);
    }
    
    @Metered(when = {AFTER, EXCEPTION, FINALLY})
    public String returnGreeting(boolean error) {
        if (error) {
            throw new RuntimeException("error");
        }
        return "hello";
    }
    
    @Metered @Timer
    public String uppercase(String returnValue, int waitInMillis) {
        try {
            TimeUnit.MILLISECONDS.sleep(waitInMillis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return returnValue.toUpperCase();
    }
}
