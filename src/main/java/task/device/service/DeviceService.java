package task.device.service;

import org.springframework.stereotype.Service;
import task.device.web.DeviceRequest;
import task.device.web.DeviceResponse;
import task.device.model.Device;
import task.device.model.MacAddress;
import task.device.repository.DeviceRepository;
import task.common.exceptions.UplinkNotRegisteredException;

import java.util.Comparator;
import java.util.List;

import static task.device.model.DeviceType.deviceOrder;

@Service
public class DeviceService {
    private final DeviceRepository deviceRepository;

    public DeviceService(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    public void register(DeviceRequest request) {
        if (request.uplink() == null) {
            deviceRepository.save(new Device(request.macAddress(), request.deviceType(), null));
        } else {
            Device uplinkDevice = resolveUplink(request.uplink());
            deviceRepository.save(new Device(request.macAddress(), request.deviceType(), uplinkDevice));
        }
    }

    private Device resolveUplink(MacAddress uplinkMac) {
        if (deviceRepository.exist(uplinkMac)) {
            return deviceRepository.findByMac(uplinkMac);
        } else {
            throw new UplinkNotRegisteredException(uplinkMac);
        }
    }

    public List<DeviceResponse> getDevices() {
        return deviceRepository.findAll()
                .stream()
                .map(device -> new DeviceResponse(device.macAddress(), device.deviceType()))
                .sorted(Comparator.comparing(device -> deviceOrder().indexOf(device.deviceType())))
                .toList();
    }

    public DeviceResponse getDevice(MacAddress macAddress) {
        var device = deviceRepository.findByMac(macAddress);
        return new DeviceResponse(device.macAddress(), device.deviceType());
    }
}
