package task.device.repository;

import task.device.model.Device;
import task.device.model.MacAddress;

import java.util.List;

public interface DeviceRepository {
    boolean exist(MacAddress macAddress);
    Device findByMac(MacAddress macAddress);
    List<Device> findAll();
    void save(Device device);
}

