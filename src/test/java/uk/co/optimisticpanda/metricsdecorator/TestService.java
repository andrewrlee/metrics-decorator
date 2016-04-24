package uk.co.optimisticpanda.metricsdecorator;

import java.util.concurrent.TimeUnit;

import uk.co.optimisticpanda.metricsdecorator.interceptors.Metered;
import uk.co.optimisticpanda.metricsdecorator.interceptors.Timer;

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
    
    
    @Metered
    public void doAVoidThing(int count) {
        publisher.accept(count);
    }
    
    @Metered
    public String returnGreeting() {
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
