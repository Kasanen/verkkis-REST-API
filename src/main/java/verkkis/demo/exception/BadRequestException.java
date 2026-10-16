package verkkis.demo.exception;

/** 400 Bad Request: virheellinen syöte */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
