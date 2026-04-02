package task.device.web;

import task.device.model.DeviceType;
import task.device.model.MacAddress;

public record DeviceResponse(String macAddress, DeviceType deviceType) {

    public DeviceResponse(MacAddress macAddress, DeviceType deviceType) {
        this(macAddress.value(), deviceType);
    }
}
