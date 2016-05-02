package uk.co.optimisticpanda.instrumentation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

public class Interceptors implements Interceptor<Annotation> {
    
    private final List<Interceptor<? extends Annotation>> interceptors;

    public Interceptors(List<Interceptor<? extends Annotation>> interceptors) {
        this.interceptors = interceptors;
    }
    
    @Override
    public Optional<Object> onBefore(Object delegate, Method method) throws Exception {
        return forEach(interceptor -> interceptor.onBefore(delegate, method)); 
    }
    
    @Override
    public Optional<Object> onAfter(Object delegate, Method method, Object returnValue) throws Exception {
        return forEach(interceptor -> interceptor.onAfter(delegate, method, returnValue)); 
    }

    @Override
    public Optional<Object> onException(Object delegate, Method method, Exception exception) throws Exception {
        return forEach(interceptor -> interceptor.onException(delegate, method, exception)); 
    }

    @Override
    public void onFinally(Object delegate, Method method) {
        interceptors.forEach(i -> i.onFinally(delegate, method));
    }
    
    private Optional<Object> forEach(ThrowingSupplier supplier) throws Exception {
        for (Interceptor<? extends Annotation> interceptor : interceptors) {
                Optional<Object> result = supplier.perform(interceptor);
                if (result.isPresent()) {
                    return result;
                }
        }
        return Optional.empty();
    }

    @FunctionalInterface
    private interface ThrowingSupplier {
        Optional<Object> perform(Interceptor<? extends Annotation> interceptor) throws Exception;
    }
}
