package task.topology.service;

import org.springframework.stereotype.Service;
import task.device.model.MacAddress;
import task.device.model.Device;
import task.device.repository.DeviceRepository;
import task.topology.web.TopologyResponse;

import java.util.List;

@Service
public class TopologyScanner {
    private final DeviceRepository deviceRepository;

    public TopologyScanner(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    public List<TopologyResponse> getTopology() {
        List<Device> allDevices = deviceRepository.findAll();
        return deviceRepository.findAll()
                .stream()
                .filter(device -> device.uplink() == null)
                .map(rootDevice -> buildTopology(rootDevice, allDevices))
                .toList();
    }

    public TopologyResponse getTopology(MacAddress rootMacAddress) {
        Device rootDevice = deviceRepository.findByMac(rootMacAddress);
        List<Device> allDevices = deviceRepository.findAll();
        return buildTopology(rootDevice, allDevices);
    }

    private TopologyResponse buildTopology(Device rootDevice, List<Device> allDevices) {
        TopologyResponse root = new TopologyResponse(rootDevice.macAddress());
        List<Device> children = allDevices.stream()
                .filter(device -> isConnected(device, rootDevice))
                .toList();
        for (Device child : children) {
            root.children().add(buildTopology(child, allDevices));
        }
        return root;
    }

    private boolean isConnected(Device device, Device rootDevice) {
        return device.uplink() != null && device.uplink().macAddress().equals(rootDevice.macAddress());
    }
}
