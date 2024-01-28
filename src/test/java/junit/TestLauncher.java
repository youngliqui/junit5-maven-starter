package junit;

import junit.service.UserServiceTest;
import org.junit.jupiter.api.Tag;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.TagFilter;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;

import java.io.PrintWriter;

public class TestLauncher {
    public static void main(String[] args) {
        var launcher = LauncherFactory.create();

        var summaryGeneratingListener = new SummaryGeneratingListener();

        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder
                .request()
                //             .selectors(DiscoverySelectors.selectPackage("junit.service"))
                .selectors(DiscoverySelectors.selectClass(UserServiceTest.class))
                .filters(
                        TagFilter.excludeTags("login")
                )
                //             .listeners()
                .build();

        launcher.execute(request, summaryGeneratingListener);

        try (var writer = new PrintWriter(System.out)) {
            summaryGeneratingListener.getSummary().printTo(writer);
        }
    }
}
