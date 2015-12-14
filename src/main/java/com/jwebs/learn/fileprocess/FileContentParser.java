package com.jwebs.learn.fileprocess;

import java.util.Collection;

/**
 * Contract for file parser.
 * 
 * @author zakyalvan
 *
 * @param <T>
 */
public interface FileContentParser<T, S> {
	Collection<T> parse(S source);
}
