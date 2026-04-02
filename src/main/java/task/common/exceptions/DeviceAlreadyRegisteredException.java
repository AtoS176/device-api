package task.common.exceptions;

import task.device.model.MacAddress;

public class DeviceAlreadyRegisteredException extends MappableRuntimeException {
    private final MacAddress macAddress;

    public DeviceAlreadyRegisteredException(MacAddress macAddress) {
        this.macAddress = macAddress;
    }

    @Override
    public String getTitle() {
        return "Cannot register device";
    }

    @Override
    public String getDetails() {
        return "Cannot register device with MAC address %s, because it was registered before".formatted(macAddress.value());
    }

    @Override
    public String getErrorCode() {
        return "CANNOT_REGISTER_DEVICE";
    }
}
