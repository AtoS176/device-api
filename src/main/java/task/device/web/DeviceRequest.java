package task.device.web;

import jakarta.validation.constraints.NotNull;
import task.device.model.DeviceType;
import task.device.model.MacAddress;

public record DeviceRequest(
        @NotNull(message = "MAC address is required") MacAddress macAddress,
        @NotNull(message = "Device type is required") DeviceType deviceType,
        MacAddress uplink) {
}
