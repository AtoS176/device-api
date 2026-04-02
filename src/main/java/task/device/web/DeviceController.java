package task.device.web;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import task.device.service.DeviceService;
import task.device.model.MacAddress;

import java.util.List;

@RestController
@RequestMapping("/api/v1/devices")
public class DeviceController {
    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @PostMapping
    public ResponseEntity<?> register(@Valid @RequestBody DeviceRequest deviceRequest) {
        deviceService.register(deviceRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping
    public ResponseEntity<List<DeviceResponse>> getDevices() {
        return ResponseEntity.ok(deviceService.getDevices());
    }

    @GetMapping("/{macAddress}")
    public ResponseEntity<DeviceResponse> getDevice(@PathVariable(name = "macAddress") MacAddress macAddress) {
        return ResponseEntity.ok(deviceService.getDevice(macAddress));
    }
}
