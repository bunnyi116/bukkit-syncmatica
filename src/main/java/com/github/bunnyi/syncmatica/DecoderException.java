package com.github.bunnyi.syncmatica;

public class DecoderException extends Exception {
    public DecoderException() { }

    public DecoderException(String message, Throwable cause) {
        super(message, cause);
    }

    public DecoderException(String message) {
        super(message);
    }

    public DecoderException(Throwable cause) {
        super(cause);
    }
}
