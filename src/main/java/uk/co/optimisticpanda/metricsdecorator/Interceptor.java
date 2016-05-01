package uk.co.optimisticpanda.metricsdecorator;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Optional;

public interface Interceptor<T extends Annotation> {
    default Optional<Object> onBefore(Object delegate, Method method) throws Exception {
        return Optional.empty();
    }

    default Optional<Object> onAfter(Object delegate, Method method, Object returnValue) throws Exception {
        return Optional.empty();
    }

    default Optional<Object> onException(Object delegate, Method method, Exception exception) throws Exception {
        return Optional.empty();
    }
    
    default void onFinally(Object delegate, Method method) {
    }
}
