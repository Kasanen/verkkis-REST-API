package verkkis.demo.exception;

/** 409 Conflict: pyyntö on oikein muotoiltu, mutta ristiriidassa nykyisen tilan kanssa */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
