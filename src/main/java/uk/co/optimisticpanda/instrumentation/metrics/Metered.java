package uk.co.optimisticpanda.instrumentation.metrics;

import static com.codahale.metrics.MetricRegistry.name;
import static java.util.Arrays.asList;
import static uk.co.optimisticpanda.instrumentation.metrics.Metered.Type.AFTER;
import static uk.co.optimisticpanda.instrumentation.metrics.Metered.Type.BEFORE;
import static uk.co.optimisticpanda.instrumentation.metrics.Metered.Type.EXCEPTION;
import static uk.co.optimisticpanda.instrumentation.metrics.Metered.Type.FINALLY;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;
import java.util.Optional;

import uk.co.optimisticpanda.instrumentation.Interceptor;
import uk.co.optimisticpanda.instrumentation.InterceptorHandlerFactory.InterceptorFactory;

import com.codahale.metrics.MetricRegistry;

@Retention(RetentionPolicy.RUNTIME)
public @interface Metered {
    
    Type[] when() default {};
    
    public enum Type {
        BEFORE, AFTER, EXCEPTION, FINALLY;
    }
    
    public static class MeteredInterceptor implements Interceptor<Metered> {
        private final MetricRegistry registry;
        private final Metered metered;

        public static InterceptorFactory factory(MetricRegistry metricRegistry) {
            return (annotation, delegate) -> new MeteredInterceptor((Metered) annotation, metricRegistry);
        }
        
        private MeteredInterceptor(Metered metered, MetricRegistry metricRegistry) {
            this.metered = metered;
            this.registry = metricRegistry;
        }

        @Override
        public Optional<Object> onBefore(Object delegate, Method method) throws Exception {
            return meter(BEFORE, method);
        }
        
        @Override
        public Optional<Object> onAfter(Object delegate, Method method, Object returnValue) throws Exception {
            return meter(AFTER, method);
        }
        
        @Override
        public Optional<Object> onException(Object delegate, Method method, Exception exception) throws Exception {
            return meter(EXCEPTION, method);
        }

        @Override
        public void onFinally(Object delegate, Method method) {
            meter(FINALLY, method);
        }

        
        private Optional<Object> meter(Type type, Method method) {
            if (isPresent(type)) {
                String name = name(method.getDeclaringClass(), method.getName(), type.name().toLowerCase());
                registry.meter(name).mark();
            }
            return Optional.empty();
        }
        
        private boolean isPresent(Type type) {
            return asList(metered.when()).contains(type);
        }
    }
}