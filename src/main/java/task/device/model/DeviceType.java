package task.device.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import task.common.exceptions.InvalidDeviceTypeException;

import java.util.Arrays;
import java.util.List;

public enum DeviceType {
    ACCESS_POINT, SWITCH, GATEWAY;

    public static List<DeviceType> deviceOrder() {
        return List.of(GATEWAY, SWITCH, ACCESS_POINT);
    }

    @JsonCreator
    public static DeviceType fromString(String value) {
        return Arrays.stream(DeviceType.values())
                .filter(type -> type.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(InvalidDeviceTypeException::new);
    }

    public static List<String> names() {
        return Arrays.stream(DeviceType.values())
                .map(Enum::toString)
                .toList();
    }
}
