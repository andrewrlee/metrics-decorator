package uk.co.optimisticpanda.metricsdecorator;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.Pipe;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;

public class InterceptorHandlerFactory {
    private final Map<Class<? extends Annotation>, Function<Object, ? extends Interceptor>> interceptorProviders = new HashMap<>();

    public InterceptorHandlerFactory() {
    }

    public InterceptorHandlerFactory register(Class<? extends Annotation> type, Function<Object, ? extends Interceptor> supplier) {
        interceptorProviders.put(type, supplier);
        return this;
    }

    public Type[] getRegsteredAnnotationTypes() {
        return interceptorProviders.keySet().toArray(new Type[interceptorProviders.keySet().size()]);
    }
    
    public InterceptorHandler create(Object delegate) {
        return new InterceptorHandler(interceptorProviders, delegate);
    }

    public static class InterceptorHandler {
        private final Map<Class<? extends Annotation>, Function<Object, ? extends Interceptor>> interceptorFactories;
        private final ConcurrentHashMap<Class<? extends Annotation>, Interceptor> allInterceptors = new ConcurrentHashMap<>();
        private final Object delegate;

        private InterceptorHandler(Map<Class<? extends Annotation>, Function<Object, ? extends Interceptor>> interceptorProviders, Object delegate) {
            this.interceptorFactories = interceptorProviders;
            this.delegate = delegate;
        }

        @RuntimeType
        public Object intercept(@Pipe Piper pipe, @Origin Method method) throws Exception {
            Interceptors interceptors = gatherInterceptors(method);
            try {
                Optional<Object> result = interceptors.onBefore(delegate, method);
                if (result.isPresent()) {
                    return result.get();
                }
                Object actualResult = pipe.to(delegate);
                return interceptors.onAfter(delegate, method, actualResult).orElse(actualResult);
            } catch (Exception e) {
                Optional<Object> result = interceptors.onException(delegate, method, e);
                if (result.isPresent()) {
                    return result.get();
                }
                throw e;
            } finally {
                interceptors.onFinally(delegate, method);
            }
        }

        @SuppressWarnings("unchecked")
        private Interceptors gatherInterceptors(Method method) {
            return stream(method.getAnnotations())
                    .map(Object::getClass)
                    .flatMap(a-> stream(a.getInterfaces()))
                    .map(a -> (Class<? extends Annotation>) a)
                    .filter(clazz -> interceptorFactories.containsKey(clazz))
                    .map(clazz -> allInterceptors.computeIfAbsent(clazz, createInterceptor(delegate)))
                    .collect(collectingAndThen(toList(), Interceptors::new));
        }

        private Function<Class<? extends Annotation>, ? extends Interceptor> createInterceptor(Object delegate) {
            return annotation -> interceptorFactories.get(annotation).apply(delegate);
        }
    }
}
