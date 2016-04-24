package uk.co.optimisticpanda.metricsdecorator.interceptors;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;
import java.util.Optional;

import uk.co.optimisticpanda.metricsdecorator.Interceptor;

import com.codahale.metrics.MetricRegistry;

@Retention(RetentionPolicy.RUNTIME)
public @interface Metered {
    public class MeteredInterceptor implements Interceptor {
        private MetricRegistry registry;

        public MeteredInterceptor(MetricRegistry metricRegistry) {
            registry = metricRegistry;
        }

        @Override
        public Optional<Object> onAfter(Object delegate, Method method, Object returnValue) throws Exception {
            registry.meter(method.getDeclaringClass().getCanonicalName()+ "." + method.getName() + ".count").mark();
            return Optional.empty();
        }
    }
}