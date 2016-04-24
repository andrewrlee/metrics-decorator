package uk.co.optimisticpanda.metricsdecorator;

import static net.bytebuddy.implementation.MethodDelegation.to;
import static net.bytebuddy.matcher.ElementMatchers.any;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.bind.annotation.Pipe;

import org.objenesis.ObjenesisBase;
import org.objenesis.strategy.StdInstantiatorStrategy;

import uk.co.optimisticpanda.metricsdecorator.interceptors.Metered;
import uk.co.optimisticpanda.metricsdecorator.interceptors.Metered.MeteredInterceptor;
import uk.co.optimisticpanda.metricsdecorator.interceptors.Timer;
import uk.co.optimisticpanda.metricsdecorator.interceptors.Timer.TimerInterceptor;

import com.codahale.metrics.MetricRegistry;

public class MetricsDecorator {

    private final InterceptorHandlerFactory factory;
    
    public MetricsDecorator(MetricRegistry metricRegistry) {
        this.factory = new InterceptorHandlerFactory()
            .register(Metered.class, delegate -> new MeteredInterceptor(metricRegistry))
            .register(Timer.class, delegate -> new TimerInterceptor(metricRegistry));
    }

    public <T> T decorate(T delegate) {
        Class<? extends T> clazz = new ByteBuddy().<T>subclass(delegate.getClass())
                .method(any())
                    .intercept(to(factory.create(delegate))
                            .appendParameterBinder(Pipe.Binder.install(Piper.class)))
                .make()
                .load(delegate.getClass().getClassLoader(), ClassLoadingStrategy.Default.WRAPPER)
                        .getLoaded();
        return new ObjenesisBase(new StdInstantiatorStrategy()).newInstance(clazz);
    }
}
