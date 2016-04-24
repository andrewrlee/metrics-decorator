package uk.co.optimisticpanda.metricsdecorator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import uk.co.optimisticpanda.metricsdecorator.TestService.Publisher;

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
        
        Meter meter = metricRegistry.getMeters().get("uk.co.optimisticpanda.metricsdecorator.TestService.doAVoidThing.count");
        assertThat(meter.getCount()).isEqualTo(4);
        
        verify(publisher).accept(1);
        verify(publisher).accept(2);
        verify(publisher).accept(3);
        verify(publisher).accept(4);
    }
    
    @Test
    public void timerAnnotationPickedUpCorrectly() {
        service.uppercase("whut",20);
        service.uppercase("whut",10);
        service.uppercase("whut",30);
        
        Timer timer = metricRegistry.getTimers().get("uk.co.optimisticpanda.metricsdecorator.TestService.uppercase.timer");
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
