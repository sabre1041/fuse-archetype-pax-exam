package ${package};

import static org.apache.karaf.tooling.exam.options.KarafDistributionOption.karafDistributionConfiguration;
import static org.apache.karaf.tooling.exam.options.KarafDistributionOption.logLevel;
import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.CoreOptions.scanFeatures;
import static org.ops4j.pax.exam.CoreOptions.streamBundle;
import static org.ops4j.pax.tinybundles.core.TinyBundles.bundle;

import java.io.File;

import javax.inject.Inject;

import org.apache.camel.CamelContext;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.apache.karaf.features.FeaturesService;
import org.apache.karaf.tooling.exam.options.LogLevelOption.LogLevel;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.ExamReactorStrategy;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.ops4j.pax.exam.spi.reactors.AllConfinedStagedReactorFactory;
import org.ops4j.pax.exam.util.Filter;
import org.osgi.framework.Constants;

@RunWith(JUnit4TestRunner.class)
@ExamReactorStrategy(AllConfinedStagedReactorFactory.class)
public class RouteTest extends CamelTestSupport {
    
    @Inject
    private FeaturesService featuresService;

    @Inject
    @Filter(value="(camel.context.name=blueprintContext)", timeout=10000)
    protected CamelContext camelContext;

    @SuppressWarnings("deprecation")
	@Configuration
    public static Option[] configure() throws Exception {
        return new Option[] {
        		
        		// define container
                karafDistributionConfiguration()
                .frameworkUrl(
                        maven().groupId("org.apache.karaf").artifactId("apache-karaf").type("zip")
                                .versionAsInProject()).useDeployFolder(false).karafVersion("2.3.0")
                        .unpackDirectory(new File("target/karaf/")),

                // container logging level
                logLevel(LogLevel.INFO),
                
                // add spring feature (required by camel-test)
                scanFeatures(
                        maven().groupId("org.apache.karaf.assemblies.features").artifactId("spring").type("xml")
                                .classifier("features").versionAsInProject(), "spring"),

                // add camel features
                scanFeatures(
                        maven().groupId("org.apache.camel.karaf").artifactId("apache-camel").type("xml")
                                .classifier("features").versionAsInProject(), "camel-blueprint","camel-test"),

                // build and start custom bundle                
            	streamBundle(bundle().add(Hello.class).add(HelloBean.class)
            			.add("OSGI-INF/blueprint/blueprint.xml",
    							new File(
    									"src/main/resources/OSGI-INF/blueprint/blueprint.xml")
    									.toURI().toURL())
    					.add("log4j.properties",
    							new File(
    									"src/main/resources/log4j.properties")
    									.toURI().toURL())			
						.set(Constants.BUNDLE_SYMBOLICNAME,
    										"${package}")
						.set(Constants.IMPORT_PACKAGE, "*")
						.build()).start()
        };
    }
    
    @Override
    public boolean isCreateCamelContextPerClass() {
        // we override this method and return true, to tell Camel test-kit that
        // it should only create CamelContext once (per class), so we will
        // re-use the CamelContext between each test method in this class
        return true;
    }
    
    @Before
    public void testSetup() throws Exception {
        // Assert Features Installed
    	assertTrue(featuresService.isInstalled(featuresService.getFeature("spring")));    
    	assertTrue(featuresService.isInstalled(featuresService.getFeature("camel-core")));
        assertTrue(featuresService.isInstalled(featuresService.getFeature("camel-blueprint")));
    }
    

    @Override
    protected void doPreSetup() throws Exception {
        assertNotNull(camelContext);
    }
	
    @Test
    public void testRoute() throws Exception {
    	
    	// get the mock endpoint
        MockEndpoint mockEndpoint = (MockEndpoint) camelContext.getEndpoint("mock:result");
    	
    	// the route is timer based, so every 5th second a message is send
        // we should then expect at least one message
        mockEndpoint.expectedMinimumMessageCount(1);

        // assert expectations
        mockEndpoint.assertIsSatisfied();
    }

}
