package task.device.model;

import java.util.List;

public enum DeviceType {
    ACCESS_POINT, SWITCH, GATEWAY;

    public static List<DeviceType> deviceOrder() {
        return List.of(GATEWAY, SWITCH, ACCESS_POINT);
    }
}
