package com.stemcraft.core.exception;

import lombok.Getter;

/**
 * Thrown when we load data from data.db but they have a location with a world
 * that no longer exists
 */
public class SMInvalidWorldException extends RuntimeException {
	/**
	 * The world that was invalid
	 */
	@Getter
	private final String world;

	public SMInvalidWorldException(String message, String world) {
		super(message);

		this.world = world;
	}
}
