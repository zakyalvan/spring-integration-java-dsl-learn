package com.jwebs.learn.threadbarrier;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

@SuppressWarnings("serial")
public class Command implements Serializable {
	private final String id;
	private final String content;
	private final Date timestamp;

	public Command(String content) {
		this.id = UUID.randomUUID().toString();
		this.content = content;
		this.timestamp = Calendar.getInstance().getTime();
	}

	public String getId() {
		return id;
	}

	public String getContent() {
		return content;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	@Override
	public String toString() {
		return "Command [id=" + id + ", content=" + content + ", timestamp=" + timestamp + "]";
	}
}