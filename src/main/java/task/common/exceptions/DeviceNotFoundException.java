package task.common.exceptions;

import task.device.model.MacAddress;

public class DeviceNotFoundException extends MappableRuntimeException {
    private final MacAddress macAddress;

    public DeviceNotFoundException(MacAddress macAddress) {
        this.macAddress = macAddress;
    }

    @Override
    public String getTitle() {
        return "Device not found";
    }

    @Override
    public String getDetails() {
        return "Device with mac address %s was not found".formatted(macAddress.value());
    }

    @Override
    public String getErrorCode() {
        return "DEVICE_NOT_FOUND";
    }
}
