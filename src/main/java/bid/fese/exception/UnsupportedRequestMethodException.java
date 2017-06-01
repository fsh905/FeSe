package bid.fese.exception;

/**
 * Created by Feng on 17/01/26.
 */
public class UnsupportedRequestMethodException extends Exception {
    private String msg;
    /**
     * Constructs a new exception with the specified detail message.  The
     * cause is not initialized, and may subsequently be initialized by
     * a call to {@link #initCause}.
     *
     * @param message the detail message. The detail message is saved for
     *                later retrieval by the {@link #getMessage()} method.
     */
    public UnsupportedRequestMethodException(String message) {
        super(message);
        this.msg = message;
    }

    @Override
    public void printStackTrace() {
        System.err.print("cause by : " + msg);
        super.printStackTrace();

    }
}
