package com.stemcraft.core.exception;

public final class SMBridgeException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		public SMBridgeException(final String message) {
			super(message);
		}

		public SMBridgeException(final Throwable ex, final String message) {
			super(message, ex);
		}
	}
