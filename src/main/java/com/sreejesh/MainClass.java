package com.sreejesh;

import org.apache.camel.impl.ThrottlingExceptionRoutePolicy;
import org.apache.camel.spi.RoutePolicy;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.ImportResource;

@SpringBootApplication
//@ImportResource("classpath:META-INF/spring/applicationContext.xml") 
public class MainClass {

	public static void main(String[] args) {
		SpringApplication.run(MainClass.class, args);

	}


	/*
	Create separate Routepolicy for each endpoint wherever applicable. FOr the same endpoint, one should
	be enough and can be used commonly. But for different endpoints, separate Routepolicy instances should be
	created.
	 */
	@Bean
	public RoutePolicy routePolicy()
	{
		int threshold = 2;
		long failureWindow = 10000;
		long halfOpenAfter = 30000;
		RoutePolicy routePolicy = new ThrottlingExceptionRoutePolicy(threshold, failureWindow, halfOpenAfter, null);
		return routePolicy;
	}

}
