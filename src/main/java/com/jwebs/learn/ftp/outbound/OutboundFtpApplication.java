package com.jwebs.learn.ftp.outbound;

import java.io.File;

import org.apache.commons.net.ftp.FTPFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.core.Pollers;
import org.springframework.integration.dsl.file.Files;
import org.springframework.integration.dsl.ftp.Ftp;
import org.springframework.integration.dsl.support.Transformers;
import org.springframework.integration.file.FileReadingMessageSource;
import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.integration.ftp.session.DefaultFtpSessionFactory;

import com.jwebs.learn.ftp.common.FtpSessionConfigProperties;

@SpringBootApplication
@IntegrationComponentScan
@EnableConfigurationProperties(FtpSessionConfigProperties.class)
public class OutboundFtpApplication {
	public static void main(String[] args) {
		ConfigurableApplicationContext applicationContext = new SpringApplicationBuilder(OutboundFtpApplication.class)
				.web(false).profiles("ftp").run(args);
		Runtime.getRuntime().addShutdownHook(new Thread(() -> applicationContext.close()));
	}

	@Autowired
	private FtpSessionConfigProperties ftpProperties;

	@Bean
	public SessionFactory<FTPFile> sessionFactory() {
		DefaultFtpSessionFactory sessionFactory = new DefaultFtpSessionFactory();
		sessionFactory.setHost(ftpProperties.host());
		sessionFactory.setPort(ftpProperties.port());
		sessionFactory.setUsername(ftpProperties.username());
		sessionFactory.setPassword(ftpProperties.password());
		return sessionFactory;
	}

	/**
	 * Integration flow definition using standard (component based) approach.
	 * 
	 * @author zakyalvan
	 */
	@Configuration
	@ConditionalOnProperty(prefix="jwebs.ftp.outbound", name="useJavaDslConfig", havingValue="false")
	public static class DefaultApproachConfig {
		@Value("${user.home}")
		private String homeDir;
		
		@Autowired
		private SessionFactory<FTPFile> sessionFactory;
		
		@Bean
		public MessageSource<File> fileMessageSource() {
			FileReadingMessageSource fileMessageSource = new FileReadingMessageSource();
			fileMessageSource.setDirectory(new File(homeDir + File.separator + "FTP" + File.separator + "outbound"));
			fileMessageSource.setAutoCreateDirectory(true);
			return fileMessageSource;
		}

		@Bean
		public IntegrationFlow outboundFlow() {
			return IntegrationFlows.from(fileMessageSource(), spec -> spec.poller(Pollers.fixedDelay(1000, 10000)))
					.handle(Ftp.outboundAdapter(sessionFactory).remoteDirectory("/home/zakyalvan")).get();
		}
	}
	
	/**
	 * Integration flow definition using spring integration java dsl configuration.
	 * 
	 * @author zakyalvan
	 */
	@Configuration
	@ConditionalOnProperty(prefix="jwebs.ftp.outbound", name="useJavaDslConfig", havingValue="true")
	public static class JavaDslApproachConfig {
		@Value("${user.home}")
		private String homeDir;
		
		@Autowired
		private SessionFactory<FTPFile> sessionFactory;
		
		@Bean
		public IntegrationFlow outboundFlow() {
			return IntegrationFlows.from(Files.inboundAdapter(new File(homeDir + File.separator + "FTP" + File.separator + "outbound")).autoCreateDirectory(true).preventDuplicates(), spec -> spec.poller(Pollers.fixedDelay(1000, 10000)))
					.transform(Transformers.fileToByteArray())
					.handle(Ftp.outboundAdapter(sessionFactory).remoteDirectory("/home/zakyalvan"))
					.get();
		}
	}
}
