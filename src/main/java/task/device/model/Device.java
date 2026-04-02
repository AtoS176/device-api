package task.device.model;

public record Device(MacAddress macAddress, DeviceType deviceType, Device uplink) {
}
