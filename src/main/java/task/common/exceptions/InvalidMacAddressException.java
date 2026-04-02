package task.common.exceptions;

public class InvalidMacAddressException extends MappableRuntimeException {
    private final String macAddress;

    public InvalidMacAddressException(String macAddress) {
        this.macAddress = macAddress;
    }

    @Override
    public String getTitle() {
        return "Invalid MAC address";
    }

    @Override
    public String getDetails() {
        return "Provided MAC address %s has invalid format".formatted(macAddress);
    }

    @Override
    public String getErrorCode() {
        return "INVALID_MAC_ADDRESS";
    }
}
