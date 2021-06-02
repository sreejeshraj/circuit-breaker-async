package com.sreejesh.demo.route;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.ThrottlingExceptionRoutePolicy;
import org.apache.camel.spi.RoutePolicy;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Component
@ConfigurationProperties(prefix="camel-demo-route")
@Data
@EqualsAndHashCode(callSuper=true)

public class CamelDemoRoute extends RouteBuilder {


	
	@Override
	public void configure() throws Exception {

		// @formatter:off

		int threshold = 2;
		long failureWindow = 30000;
		long halfOpenAfter = 120000;
		RoutePolicy routePolicy = new ThrottlingExceptionRoutePolicy(threshold, failureWindow, halfOpenAfter, null);
		
//		errorHandler(deadLetterChannel("seda:errorQueue").useOriginalMessage().maximumRedeliveries(3).redeliveryDelay(1000));



		from("timer://myTimer?period=5s")
		.routeId("InputFolderToTestSedaRoute")
		.setBody(exchangeProperty(Exchange.TIMER_FIRED_TIME))
		.convertBodyTo(String.class)
		.to("seda://testSeda")
		.log("**** Input data published to  testSeda - ${body}***** :")
		;

		from("seda://testSeda")
		.routeId("TestSedaToOutputFolderRoute")
		.routePolicy(routePolicy)
		.to("file://{{outputFolder}}?autoCreate=false&fileName=TimerFile-${exchangeProperty.CamelTimerCounter}")
//		.to("file://{{outputFolder}}?fileName=TimerFile-${exchangeProperty.CamelTimerCounter}.txt")
		;
		
		//Error Handling route!
		
		from("seda:errorQueue")
		.routeId("ErrorHandlingRoute")
		.log("***** error body: ${body} *****")
		.to("file://{{errorFolder}}?fileName=TimerFile-${exchangeProperty.CamelTimerCounter}.txt")
		.log("***** Exception Caught: ${exception} *****")
		;
		
		
		// @formatter:on

	}

}
