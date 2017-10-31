package uk.gov.ida.integrationtest;

import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.graphite.Graphite;
import com.codahale.metrics.graphite.GraphiteReporter;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.junit.ClassRule;
import org.junit.Test;
import uk.gov.ida.integrationtest.helpers.MatchingServiceAdapterAppRule;
import uk.gov.ida.matchingserviceadapter.MatchingServiceAdapterConfiguration;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

public class GraphiteReporterIntegrationTest {

    @ClassRule
    public static final DropwizardAppRule<MatchingServiceAdapterConfiguration> appRule = new MatchingServiceAdapterAppRule();

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
