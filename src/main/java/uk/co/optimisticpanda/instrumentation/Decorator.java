package uk.co.optimisticpanda.instrumentation;

import static net.bytebuddy.implementation.MethodDelegation.to;
import static net.bytebuddy.matcher.ElementMatchers.any;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.bind.annotation.Pipe;

import org.objenesis.ObjenesisBase;
import org.objenesis.strategy.StdInstantiatorStrategy;

public class Decorator {

   private final InterceptorHandlerFactory factory;
    
    public Decorator(InterceptorHandlerFactory factory) {
        this.factory = factory;
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
