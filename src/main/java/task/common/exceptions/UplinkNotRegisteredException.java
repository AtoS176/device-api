package task.common.exceptions;

import task.device.model.MacAddress;

public class UplinkNotRegisteredException extends MappableRuntimeException {
    private final MacAddress macAddress;

    public UplinkNotRegisteredException(MacAddress macAddress) {
        this.macAddress = macAddress;
    }

    @Override
    public String getTitle() {
        return "Missing uplink device";
    }

    @Override
    public String getDetails() {
        return "Cannot register device, because uplink device with MAC address %s was not found".formatted(macAddress.value());
    }

    @Override
    public String getErrorCode() {
        return "MISSING_UPLINK_DEVICE";
    }
}
