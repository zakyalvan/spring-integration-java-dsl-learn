package com.jwebs.learn.ftp.inbound;

import java.io.File;

import org.apache.commons.net.ftp.FTPFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.core.Pollers;
import org.springframework.integration.dsl.ftp.Ftp;
import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.integration.ftp.session.DefaultFtpSessionFactory;

import com.jwebs.learn.ftp.common.FtpSessionConfigProperties;

@SpringBootApplication
@IntegrationComponentScan
@EnableConfigurationProperties(FtpSessionConfigProperties.class)
public class InboundFtpApplication {
	public static void main(String[] args) {
		ConfigurableApplicationContext applicationContext = new SpringApplicationBuilder(InboundFtpApplication.class)
				.web(false)
				.profiles("ftp")
				.run(args);
		
		Runtime.getRuntime().addShutdownHook(new Thread(() -> applicationContext.close()));
	}

	@Value("${user.home}")
	private String homeDir;
	
	@Autowired
	private FtpSessionConfigProperties ftpConfigProperties;
	
	@Bean
	public SessionFactory<FTPFile> sessionFactory() {
		DefaultFtpSessionFactory sessionFactory = new DefaultFtpSessionFactory();
		sessionFactory.setHost(ftpConfigProperties.host());
		sessionFactory.setPort(ftpConfigProperties.port());
		sessionFactory.setUsername(ftpConfigProperties.username());
		sessionFactory.setPassword(ftpConfigProperties.password());
		return sessionFactory;
	}
	
	@Bean
	public IntegrationFlow inboundFlow() {
		return IntegrationFlows.from(Ftp.inboundAdapter(sessionFactory())
										.autoCreateLocalDirectory(true)
										.deleteRemoteFiles(false)
										.remoteDirectory("/home/zakyalvan")
										.localDirectory(new File(homeDir + File.separator + "FTP" + File.separator + "inbound")),
						spec -> spec.poller(Pollers.fixedDelay(1000))
				)
				.handle(message -> System.out.println("Message payload : " + message.getPayload()))
				.get();
	}
}
