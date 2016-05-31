package fr.vuzi.http.error;

/**
 * HTTP Exception
 */
public class HttpException extends Exception {

    private final int errorCode;

    public HttpException(int code, String message) {
        super(message);

        this.errorCode = code;
    }

    public HttpException(int code, String message, Throwable cause) {
        super(message, cause);

        this.errorCode = code;
    }

    public int getErrorCode() {
        return errorCode;
    }
}
