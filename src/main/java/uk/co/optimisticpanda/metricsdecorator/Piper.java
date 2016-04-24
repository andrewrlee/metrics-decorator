package uk.co.optimisticpanda.metricsdecorator;

@FunctionalInterface
public interface Piper {
    Object to(Object target);
}