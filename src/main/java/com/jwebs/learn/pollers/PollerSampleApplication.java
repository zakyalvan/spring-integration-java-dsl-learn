package com.jwebs.learn.pollers;

import java.util.Random;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.dsl.AggregatorSpec;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.core.Pollers;
import org.springframework.integration.support.MessageBuilder;

@SpringBootApplication
@EnableIntegration
public class PollerSampleApplication {
	public static void main(String[] args) {
		
	}
	
	private Random random = new Random();
	
	@Bean
	public MessageSource<Integer> randomIntegerSource() {
		return () -> {
			Integer payload = this.random.nextInt();
			return MessageBuilder.withPayload(payload).setCorrelationId("1").build();
		};
	}
	
	@Bean
	public IntegrationFlow withPoolerFlow() {
		return IntegrationFlows.from(randomIntegerSource(), spec -> spec.poller(Pollers.fixedDelay(1000).maxMessagesPerPoll(10)))
				.filter(Integer.class, payload -> payload % 2 != 0, endpointSpec -> endpointSpec.discardFlow(discardedEvenPayload()))
				.handle((payload, headers) -> {
					System.out.println(payload);
					return payload;
				})
				.aggregate(aggregatorSpec -> ((AggregatorSpec) aggregatorSpec).releaseExpression("#this.size() == 12").expireGroupsUponCompletion(true), 
						endpointSpec -> endpointSpec.autoStartup(true))
				.handle(message -> System.out.println(message.getPayload()))
				.get();
	}
	
	@Bean
	public IntegrationFlow discardedEvenPayload() {
		return flow -> flow
				.handle(message -> System.err.println("Discarded message payload : " + message.getPayload()));
	}
}
