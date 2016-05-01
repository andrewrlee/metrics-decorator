package uk.co.optimisticpanda.metricsdecorator.interceptors;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;
import java.util.Optional;

import uk.co.optimisticpanda.metricsdecorator.Interceptor;
import uk.co.optimisticpanda.metricsdecorator.InterceptorHandlerFactory.InterceptorFactory;

import com.codahale.metrics.MetricRegistry;

@Retention(RetentionPolicy.RUNTIME)
public @interface Metered {
    public class MeteredInterceptor implements Interceptor<Metered> {
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
        public Optional<Object> onAfter(Object delegate, Method method, Object returnValue) throws Exception {
            registry.meter(method.getDeclaringClass().getCanonicalName()+ "." + method.getName() + ".count").mark();
            return Optional.empty();
        }
    }
}