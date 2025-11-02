package se233.kellion.exception;

// The base exception for all Kellion errors
public class KellionException extends RuntimeException {
    public KellionException(String message) {
        super(message);
    }

    public KellionException(String message, Throwable cause) {
        super(message, cause);
    }

    public KellionException(Throwable cause) {
        super(cause);
    }

    public KellionException() {
        super();
    }
}
