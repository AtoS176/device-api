package task.common.exceptions;

public abstract class MappableRuntimeException extends RuntimeException {
    public abstract String getTitle();
    public abstract String getDetails();
    public abstract String getErrorCode();
}
