package uk.co.optimisticpanda.instrumentation.metrics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;

import java.util.SortedMap;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import uk.co.optimisticpanda.instrumentation.metrics.MetricsDecorator;
import uk.co.optimisticpanda.instrumentation.metrics.TestService.Publisher;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

@RunWith(MockitoJUnitRunner.class)
public class MetricsDecoratorTest {

    @Mock private Publisher publisher;
    private MetricRegistry metricRegistry = new MetricRegistry();
    private TestService service;

    @Before
    public void setUp() throws Exception {
        MetricsDecorator metricsDecorator = new MetricsDecorator(metricRegistry);
        service = metricsDecorator.decorate(new TestService(publisher));
    }

    @Test
    public void meterAnnotationPickedUpCorrectly() {
        service.doAVoidThing(1);
        service.doAVoidThing(2);
        service.doAVoidThing(3);
        service.doAVoidThing(4);
        
        Meter meter = metricRegistry.getMeters().get("uk.co.optimisticpanda.instrumentation.metrics.TestService.doAVoidThing.before");
        assertThat(meter.getCount()).isEqualTo(4);
        
        verify(publisher).accept(1);
        verify(publisher).accept(2);
        verify(publisher).accept(3);
        verify(publisher).accept(4);
    }
    
    @Test
    public void meterAnnotationFinallyWorksWhenSuccess() {
        service.returnGreeting(false);
        
        SortedMap<String, Meter> meters = metricRegistry.getMeters();

        assertThat(meters)
            .containsOnlyKeys(
                    "uk.co.optimisticpanda.instrumentation.metrics.TestService.returnGreeting.after",
                    "uk.co.optimisticpanda.instrumentation.metrics.TestService.returnGreeting.finally");
        
        assertThat(meters.get("uk.co.optimisticpanda.instrumentation.metrics.TestService.returnGreeting.after").getCount())
            .isEqualTo(1);
        
        assertThat(meters.get("uk.co.optimisticpanda.instrumentation.metrics.TestService.returnGreeting.finally").getCount())
            .isEqualTo(1);
    }
    
    @Test
    public void meterAnnotationFinallyWorksOnFailure() {
        
        assertThatThrownBy(() -> service.returnGreeting(true))
            .isInstanceOf(RuntimeException.class).hasMessage("error");
        
        SortedMap<String, Meter> meters = metricRegistry.getMeters();

        assertThat(meters)
            .containsOnlyKeys(
                    "uk.co.optimisticpanda.instrumentation.metrics.TestService.returnGreeting.exception",
                    "uk.co.optimisticpanda.instrumentation.metrics.TestService.returnGreeting.finally");
        
        assertThat(meters.get("uk.co.optimisticpanda.instrumentation.metrics.TestService.returnGreeting.exception").getCount())
            .isEqualTo(1);
        
        assertThat(meters.get("uk.co.optimisticpanda.instrumentation.metrics.TestService.returnGreeting.finally").getCount())
            .isEqualTo(1);
    }
    
    @Test
    public void timerAnnotationPickedUpCorrectly() {
        service.uppercase("whut",20);
        service.uppercase("whut",10);
        service.uppercase("whut",30);
        
        Timer timer = metricRegistry.getTimers().get("uk.co.optimisticpanda.instrumentation.metrics.TestService.uppercase.timer");
        assertThat(timer.getCount()).isEqualTo(3);
    }
    
    @Test
    public void worksForMethodsWithNoAnnotations() {
        service.nonAnnotatedDoAVoidThing(2);
        service.nonAnnotatedDoAVoidThing(1);
        
        assertThat(metricRegistry.getCounters()).isEmpty();
        assertThat(metricRegistry.getTimers()).isEmpty();
    }
}
