package com.jwebs.learn.fileprocess;

import java.io.File;

import org.apache.commons.net.ftp.FTPFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.core.Pollers;
import org.springframework.integration.dsl.ftp.Ftp;
import org.springframework.integration.dsl.support.Transformers;
import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.integration.ftp.session.DefaultFtpSessionFactory;

/**
 * File retrieval flow configuration.
 * 
 * @author zakyalvan
 */
@Configuration
public class FtpFileProcessingFlow {
	@Value("${user.home}")
	private String localHomeDir;
	
	@Autowired
	private FtpConnectionConfigProperties ftpConnection;
	
	@Bean
	public SessionFactory<FTPFile> sessionFactory() {
		DefaultFtpSessionFactory sessionFactory = new DefaultFtpSessionFactory();
		sessionFactory.setHost(ftpConnection.getHost());
		sessionFactory.setPort(ftpConnection.getPort());
		sessionFactory.setUsername(ftpConnection.getUsername());
		sessionFactory.setPassword(ftpConnection.getPassword());
		return sessionFactory;
	}
	
	@Bean
	public IntegrationFlow retrieveFlow() {
		return IntegrationFlows
				.from(Ftp.inboundAdapter(sessionFactory())
								.autoCreateLocalDirectory(true)
								.localDirectory(new File(localHomeDir + File.pathSeparator + "FTP" + File.pathSeparator + "import"))
								.remoteDirectory("/home/zakyalvan/import"), 
						spec -> spec.poller(Pollers.fixedDelay(5)))
				.transform(Transformers.fileToString())
				.handle(message -> System.out.println(message.getPayload()))
				.get();
	}
}