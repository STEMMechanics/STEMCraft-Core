package com.stemcraft.core;

import com.stemcraft.core.exception.SMException;

public class SMValid {
    /**
	 * Throw an error if the given expression is false
	 *
	 * @param expression
	 */
	public static void checkBoolean(final boolean expression) {
		if (!expression) {
			throw new SMException();
		}
	}

	/**
	 * Throw an error with a custom message if the given expression is false
	 *
	 * @param expression
	 * @param falseMessage
	 * @param replacements
	 */
	public static void checkBoolean(final boolean expression, final String falseMessage, final Object... replacements) {
		if (!expression) {
			String message = falseMessage;

			try {
				message = String.format(falseMessage, replacements);

			} catch (final Throwable t) {
                /* empty */
			}

			throw new SMException(message);
		}
	}

	/**
	 * Throw an error with a custom message if the given expression is null
	 *
	 * @param expression
	 * @param falseMessage
	 * @param replacements
	 */
	public static void checkNotNull(final Object expression, final String falseMessage, final Object... replacements) {
		checkBoolean(expression != null, falseMessage, replacements);
	}
}
