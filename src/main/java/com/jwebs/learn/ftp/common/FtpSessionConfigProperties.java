package com.jwebs.learn.ftp.common;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix="jwebs.ftp.conn")
public class FtpSessionConfigProperties {
	private String host;
	private Integer port;
	private String username;
	private String password;
	
	public String host() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public Integer port() {
		return port;
	}
	public void setPort(Integer port) {
		this.port = port;
	}
	public String username() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String password() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
}
