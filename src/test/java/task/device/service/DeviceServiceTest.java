package task.device.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import task.common.exceptions.UplinkNotRegisteredException;
import task.device.model.Device;
import task.device.model.DeviceType;
import task.device.model.MacAddress;
import task.device.repository.DeviceRepository;
import task.device.web.DeviceRequest;
import task.device.web.DeviceResponse;

class DeviceServiceTest {

  private DeviceRepository deviceRepository;
  private DeviceService deviceService;

  private final MacAddress rootMac = new MacAddress("00:11:22:33:44:55");
  private final MacAddress childMac = new MacAddress("00:11:22:33:44:66");

  @BeforeEach
  void setUp() {
    deviceRepository = mock(DeviceRepository.class);
    deviceService = new DeviceService(deviceRepository);
  }

  @Test
  void shouldRegisterDeviceWithoutUplink() {
    DeviceRequest request = new DeviceRequest(rootMac, DeviceType.GATEWAY, null);

    deviceService.register(request);

    verify(deviceRepository).save(new Device(rootMac, DeviceType.GATEWAY, null));
  }

  @Test
  void shouldRegisterDeviceWithValidUplink() {
    Device uplinkDevice = new Device(rootMac, DeviceType.GATEWAY, null);
    when(deviceRepository.exist(rootMac)).thenReturn(true);
    when(deviceRepository.findByMac(rootMac)).thenReturn(uplinkDevice);
    DeviceRequest request = new DeviceRequest(childMac, DeviceType.SWITCH, rootMac);

    deviceService.register(request);

    verify(deviceRepository).save(new Device(childMac, DeviceType.SWITCH, uplinkDevice));
  }

  @Test
  void shouldThrowUplinkNotRegisteredExceptionWhenUplinkDoesNotExist() {
    when(deviceRepository.exist(rootMac)).thenReturn(false);
    DeviceRequest request = new DeviceRequest(childMac, DeviceType.SWITCH, rootMac);

    assertThatThrownBy(() -> deviceService.register(request))
        .isInstanceOf(UplinkNotRegisteredException.class);
  }

  @Test
  void shouldReturnAllDevicesSortedByDeviceTypeOrder() {
    Device gateway = new Device(rootMac, DeviceType.GATEWAY, null);
    Device switch1 = new Device(childMac, DeviceType.SWITCH, gateway);
    MacAddress apMac = new MacAddress("00:11:22:33:44:77");
    Device accessPoint = new Device(apMac, DeviceType.ACCESS_POINT, switch1);
    when(deviceRepository.findAll()).thenReturn(List.of(accessPoint, switch1, gateway));

    List<DeviceResponse> result = deviceService.getDevices();

    assertThat(result)
        .hasSize(3)
        .extracting(DeviceResponse::deviceType)
        .containsExactly(DeviceType.GATEWAY, DeviceType.SWITCH, DeviceType.ACCESS_POINT);
    assertThat(result)
        .extracting(DeviceResponse::macAddress)
        .containsExactly(rootMac.value(), childMac.value(), apMac.value());
  }

  @Test
  void shouldReturnSingleDeviceByMacAddress() {
    Device device = new Device(rootMac, DeviceType.GATEWAY, null);
    when(deviceRepository.findByMac(rootMac)).thenReturn(device);

    DeviceResponse result = deviceService.getDevice(rootMac);

    assertThat(result)
        .extracting(DeviceResponse::macAddress, DeviceResponse::deviceType)
        .containsExactly(rootMac.value(), DeviceType.GATEWAY);
  }
}
