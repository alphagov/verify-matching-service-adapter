package uk.gov.ida.integrationtest;

import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.graphite.Graphite;
import com.codahale.metrics.graphite.GraphiteReporter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import uk.gov.ida.integrationtest.helpers.MatchingServiceAdapterAppExtension;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

public class GraphiteReporterIntegrationTest {

    @RegisterExtension
    static MatchingServiceAdapterAppExtension applicationRule = new MatchingServiceAdapterAppExtension();

    @Test
    public void shouldBeAbleToCreateAGraphiteReporter() throws Exception {
        final Graphite graphite = new Graphite(new InetSocketAddress("graphite.example.com", 2003));
        final MetricRegistry registry = new MetricRegistry();
        final GraphiteReporter reporter = GraphiteReporter
                .forRegistry(registry)
                .prefixedWith("web1.example.com")
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .filter(MetricFilter.ALL)
                .build(graphite);
        reporter.start(1, TimeUnit.MINUTES);
    }
}
