package com.ustglobal.demo.route;

import static org.junit.Assert.assertFalse;

import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.model.ModelCamelContext;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.test.spring.CamelSpringBootRunner;
import org.apache.camel.test.spring.UseAdviceWith;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;

@RunWith(CamelSpringBootRunner.class)
//@MockEndpoints
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
@UseAdviceWith
@SpringBootApplication
@SpringBootTest(classes = CamelDemoRouteTest.class)
public class CamelDemoRouteTest {

	@Autowired
	ProducerTemplate producerTemplate;

	@Autowired
	ModelCamelContext camelContext;

	@Test
	public void testInputFolderToTestSedaRoute() throws Exception {
		// context should not be started because we enabled @UseAdviceWith
		assertFalse(camelContext.getStatus().isStarted());

		RouteDefinition routeToAdvice = camelContext.getRouteDefinition("InputFolderToTestSedaRoute");

		routeToAdvice.adviceWith(camelContext, new AdviceWithRouteBuilder() {
			@Override
			public void configure() throws Exception {
				//Producer template not required if file:// is enabled as it will pick the test file from the location specified
				//	replaceFromWith("file://<YOUR_TEST_DATA_FOLDER>");
				replaceFromWith("seda:start");
				weaveAddLast().to("mock:result");
			}
		});
		// manual start camel
		camelContext.start();
		MockEndpoint mockResult = camelContext.getEndpoint("mock:result", MockEndpoint.class);

		// Given
		String message = "sampleMessage";
		mockResult.expectedBodiesReceived(message);

		// When
		producerTemplate.sendBody("seda:start", message);

		// Then
		mockResult.assertIsSatisfied();

	}
	
	
	@Test
	public void testTestSedaToOutputFolderRoute() throws Exception {
		// context should not be started because we enabled @UseAdviceWith
		assertFalse(camelContext.getStatus().isStarted());

		RouteDefinition routeToAdvice = camelContext.getRouteDefinition("TestSedaToOutputFolderRoute");

		routeToAdvice.adviceWith(camelContext, new AdviceWithRouteBuilder() {
			@Override
			public void configure() throws Exception {
				//replaceFromWith("seda:start");
				//weaveAddLast().to("mock:result");
				//mockEndpointsAndSkip("file://{{outputFolder}}");
				
				interceptSendToEndpoint("file://*")
				.skipSendToOriginalEndpoint()
				.to("mock:result");
				/*.process(new Processor() {
					
					@Override
					public void process(Exchange exchange) throws Exception {
						// TODO Auto-generated method stub
						
					}
				});*/
				
			}
		});
		// manual start camel
		camelContext.start();
		MockEndpoint mockResult = camelContext.getEndpoint("mock:result", MockEndpoint.class);

		// Given
		String message = "sampleMessage";
		mockResult.expectedBodiesReceived(message);

		// When
		producerTemplate.sendBody("seda:testSeda", message);

		// Then
		mockResult.assertIsSatisfied();

	}


}
