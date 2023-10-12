package com.stemcraft.core.exception;

import lombok.Getter;

/**
 * Represents a silent exception thrown then handling commands,
 * this will only send the command sender a message
 */
public class SMCommandException extends RuntimeException {
	/**
	 * The messages to send to the command sender
	 */
	@Getter
	private final String[] messages;

	/**
	 * Create a new command exception with messages for the command sender
	 *
	 * @param messages
	 */
	public SMCommandException(String... messages) {
		super("");

		this.messages = messages;
	}
}
