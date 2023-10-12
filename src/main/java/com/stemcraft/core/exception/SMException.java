package com.stemcraft.core.exception;

import com.stemcraft.core.SMDebugger;

/**
 * Represents our core exception.
 */
public class SMException extends RuntimeException {
    
    /**
     * Create a new exception.
     */
    public SMException() {
        SMDebugger.printError(this);
    }

    /**
     * Create a new exception.
     * @param t
     */
    public SMException(Throwable t) {
        super(t);
        SMDebugger.printError(t);
    }

    /**
     * Create a new exception.
     * @param message
     */
    public SMException(String message) {
        super(message);
        SMDebugger.printError(this, message);
    }

    /**
     * Create a new exception.
     * @param message
     * @param t
     */
    public SMException(String message, Throwable t) {
        super(message, t);
        SMDebugger.printError(t, message);
    }
}
