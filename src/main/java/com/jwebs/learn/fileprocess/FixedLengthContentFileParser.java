package com.jwebs.learn.fileprocess;

import java.util.Collection;

import org.springframework.stereotype.Component;

@Component
public class FixedLengthContentFileParser implements FileContentParser<Customer, String> {
	@Override
	public Collection<Customer> parse(String source) {
		return null;
	}
}
