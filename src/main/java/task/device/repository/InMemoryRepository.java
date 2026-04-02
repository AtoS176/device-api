package task.device.repository;

import org.springframework.stereotype.Repository;
import task.device.model.Device;
import task.device.model.MacAddress;
import task.common.exceptions.DeviceAlreadyRegisteredException;
import task.common.exceptions.DeviceNotFoundException;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryRepository implements DeviceRepository {
    private final Map<MacAddress, Device> storage = new ConcurrentHashMap<>();

    @Override
    public boolean exist(MacAddress macAddress) {
        return storage.containsKey(macAddress);
    }

    @Override
    public Device findByMac(MacAddress macAddress) {
        return Optional.ofNullable(storage.get(macAddress))
                .orElseThrow(() -> new DeviceNotFoundException(macAddress));
    }

    @Override
    public List<Device> findAll() {
        return storage.values().stream().toList();
    }

    @Override
    public void save(Device device) {
        if (storage.putIfAbsent(device.macAddress(), device) != null) {
            throw new DeviceAlreadyRegisteredException(device.macAddress());
        }
    }
}
