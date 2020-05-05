package co.arago.hiro.client.util;

import java.io.Serializable;

/**
 * a base exception for HIRO, contains an error code
 */
public class HiroException extends RuntimeException implements Serializable {

    private static final long serialVersionUID = 42L;
    private final int code;
    private Object details;
    private final transient Throwable cause;

    public HiroException(final String message, final int code) {
        super(message);

        this.code = code;
        this.cause = null;
    }

    public HiroException(final String message, final int code, final Object details) {
        super(message);

        this.code = code;
        this.cause = null;

        setDetails(details);
    }

    public HiroException(final String message, final int code, final Throwable t, final Object details) {
        super(message);

        this.code = code;
        this.cause = t;

        setDetails(details);
    }

    /**
     * get the error code
     *
     * @return
     */
    public int getCode() {
        return code;
    }

    @Override
    public synchronized Throwable getCause() {
        return (cause == this ? null : cause);
    }

    public Object getDetails() {
        return details;
    }

    private void setDetails(Object details) {
        if (details == null)
            return;

        this.details = details;
    }
}
