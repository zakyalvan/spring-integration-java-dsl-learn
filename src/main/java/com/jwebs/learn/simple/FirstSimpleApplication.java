package com.jwebs.learn.simple;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.channel.MessageChannels;
import org.springframework.messaging.MessageChannel;

@SpringBootApplication
@IntegrationComponentScan
public class FirstSimpleApplication {
	public static void main(String[] args) {
		ConfigurableApplicationContext applicationContext = new SpringApplication(FirstSimpleApplication.class).run(args);
		WordProcessor processor = applicationContext.getBean(WordProcessor.class);
		List<String> words = Arrays.asList("foo", "bar");
		processor.upcase(words);
		applicationContext.close();
	}
	
	@MessagingGateway
	public static interface WordProcessor {
		@Gateway(requestChannel="upcase.input")
		void upcase(Collection<String> input);
	}
	
	@Bean
	public IntegrationFlow upcase() {
		return flow -> flow
				.publishSubscribeChannel(spec -> spec.applySequence(true)
						.subscribe(subFlow -> subFlow
								.<List<String>, Collection<String>>transform(source -> new ArrayList<>(source))
								.<List<String>>handle((payload, headers) -> {
									System.out.println("@@@@@@@2" + payload.toString());
									System.out.println("@@@@@@@2" + headers.toString());
									payload.add("Second");
									return payload;
								})
								.channel(aggregateChannel())
						)
						.subscribe(subFlow -> subFlow
								.<List<String>, Collection<String>>transform(source -> new ArrayList<>(source))
								.<List<String>>handle((payload, headers) -> {
									System.out.println("@@@@@@@3" + payload.toString());
									System.out.println("@@@@@@@3" + headers.toString());
									payload.add("Third");
									return payload;
								})
								.channel(aggregateChannel())
						)
						.subscribe(subFlow -> subFlow
								.<List<String>, Collection<String>>transform(source -> new ArrayList<>(source))
								.<List<String>>handle((payload, headers) -> {
									System.out.println("@@@@@@@4" + payload.toString());
									System.out.println("@@@@@@@4" + headers.toString());
									payload.add("Forth");
									return payload;
								})
								.channel(aggregateChannel())
						)
				)
				.<List<String>>handle((payload, headers) -> {
					System.out.println("@@@@@@@1" + payload.toString());
					System.out.println("@@@@@@@1" + headers.toString());
					return payload;
				})
				.channel(aggregateChannel())
				.resequence()
				.aggregate()
				.handle(message -> {
					System.out.println("********" + message.toString());
				});
	}
	
	@Bean
	public MessageChannel aggregateChannel() {
		return MessageChannels.direct().get();
	}
}
