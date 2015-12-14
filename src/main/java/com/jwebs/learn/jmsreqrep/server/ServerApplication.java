package com.jwebs.learn.jmsreqrep.server;

import javax.jms.ConnectionFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.jms.Jms;

@SpringBootApplication()
public class ServerApplication {
	public static void main(String[] args) throws Exception {
		ConfigurableApplicationContext applicationContext = new SpringApplicationBuilder(ServerApplication.class)
				.web(false)
				.run(args);
			
			Runtime.getRuntime().addShutdownHook(new Thread(() -> applicationContext.close()));
			
			System.out.println("Press enter key to exit...");
			System.in.read();
			System.exit(0);
	}
	
	@Autowired
	private ConnectionFactory connectionFactory;
	
	@Bean
	public IntegrationFlow responsePing() {
		return IntegrationFlows
				.from(Jms.inboundGateway(connectionFactory)
						.destination("service.ping.queue")
						.replyDeliveryPersistent(false)
						.configureListenerContainer(containerSpec -> containerSpec
								.acceptMessagesWhileStopping(false)
								.sessionTransacted(false)
						)
				)
				.<String, Boolean>route(payload -> payload.equalsIgnoreCase("ping"), 
						mapping -> mapping
							.subFlowMapping("true", flow -> flow.transform(String.class, payload -> "pong"))
							.subFlowMapping("false", flow -> flow.transform(String.class, payload -> "gnip"))
				)
				.handle((payload, headers) -> payload)
				.get();
	}
}
