package task.common.exceptions;

import task.device.model.DeviceType;

public class InvalidDeviceTypeException extends MappableRuntimeException {

    @Override
    public String getTitle() {
        return "Unsupported device type";
    }

    @Override
    public String getDetails() {
        return "Cannot parse device type. Supported values: %s".formatted(DeviceType.names());
    }

    @Override
    public String getErrorCode() {
        return "UNSUPPORTED_DEVICE_TYPE";
    }
}
