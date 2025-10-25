package ld.sa_backend.exception;

public class ExternalApiException extends RuntimeException {

    private final int externalStatusCode;
    private final String externalErrorMessage;

    public ExternalApiException(int externalStatusCode, String externalErrorMessage) {
        super("External API error : " + externalErrorMessage);
        this.externalStatusCode = externalStatusCode;
        this.externalErrorMessage = externalErrorMessage;
    }

    public int getExternalStatusCode() {
        return externalStatusCode;
    }

    public String getExternalErrorMessage() {
        return externalErrorMessage;
    }
}
