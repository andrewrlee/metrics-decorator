package uk.co.optimisticpanda.instrumentation.metrics;

import uk.co.optimisticpanda.instrumentation.Decorator;
import uk.co.optimisticpanda.instrumentation.InterceptorHandlerFactory;
import uk.co.optimisticpanda.instrumentation.metrics.Metered.MeteredInterceptor;
import uk.co.optimisticpanda.instrumentation.metrics.Timer.TimerInterceptor;

import com.codahale.metrics.MetricRegistry;

public class MetricsDecorator {

    private final Decorator decorator;
    
    public MetricsDecorator(MetricRegistry metricRegistry) {
        InterceptorHandlerFactory factory = new InterceptorHandlerFactory()
            .register(Metered.class, MeteredInterceptor.factory(metricRegistry))
            .register(Timer.class, TimerInterceptor.factory(metricRegistry));
        decorator = new Decorator(factory);
    }

    public <T> T decorate(T delegate) {
        return decorator.decorate(delegate);
    }
}
