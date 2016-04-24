package uk.co.optimisticpanda.metricsdecorator.interceptors;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;
import java.util.Optional;

import uk.co.optimisticpanda.metricsdecorator.Interceptor;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer.Context;

@Retention(RetentionPolicy.RUNTIME)
public @interface Timer {
    public class TimerInterceptor implements Interceptor{
        private MetricRegistry registry;
        private Context context;

        public TimerInterceptor(MetricRegistry metricRegistry) {
            registry = metricRegistry;
        }

        @Override
        public Optional<Object> onBefore(Object delegate, Method method) throws Exception {
            context = registry.timer(method.getDeclaringClass().getCanonicalName()+ "." + method.getName() + ".timer").time();
            return Optional.empty();
        }

        @Override
        public void onFinally(Object delegate, Method method) {
            context.close();
        }
        
    }
}