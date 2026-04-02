package task.device.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import task.common.exceptions.DeviceAlreadyRegisteredException;
import task.common.exceptions.DeviceNotFoundException;
import task.device.model.Device;
import task.device.model.DeviceType;
import task.device.model.MacAddress;

class InMemoryRepositoryTest {

  private InMemoryRepository repository;

  private final MacAddress mac1 = new MacAddress("00:11:22:33:44:55");
  private final MacAddress mac2 = new MacAddress("00:11:22:33:44:66");
  private final MacAddress mac3 = new MacAddress("00:11:22:33:44:77");
  private final MacAddress unknownMac = new MacAddress("00:11:22:33:44:99");

  @BeforeEach
  void setUp() {
    repository = new InMemoryRepository();
  }

  @Test
  void shouldSaveNewDeviceWhenDeviceNotExists() {
    Device device = new Device(mac1, DeviceType.GATEWAY, null);

    repository.save(device);

    assertThat(repository.exist(mac1)).isTrue();
  }

  @Test
  void shouldThrowDeviceAlreadyRegisteredExceptionWhenSavingDuplicate() {
    Device device1 = new Device(mac1, DeviceType.GATEWAY, null);
    Device device2 = new Device(mac1, DeviceType.SWITCH, null);
    repository.save(device1);

    assertThatThrownBy(() -> repository.save(device2))
        .isInstanceOf(DeviceAlreadyRegisteredException.class);
  }

  @Test
  void shouldReturnTrueWhenDeviceExists() {
    Device device = new Device(mac1, DeviceType.GATEWAY, null);
    repository.save(device);

    boolean result = repository.exist(mac1);

    assertThat(result).isTrue();
  }

  @Test
  void shouldReturnFalseWhenDeviceDoesNotExist() {
    boolean result = repository.exist(unknownMac);

    assertThat(result).isFalse();
  }

  @Test
  void shouldFindDeviceByMacAddressWhenDeviceExists() {
    Device device = new Device(mac1, DeviceType.GATEWAY, null);
    repository.save(device);

    Device result = repository.findByMac(mac1);

    assertThat(result)
        .extracting(Device::macAddress, Device::deviceType, Device::uplink)
        .containsExactly(mac1, DeviceType.GATEWAY, null);
  }

  @Test
  void shouldThrowDeviceNotFoundExceptionWhenDeviceDoesNotExist() {
    assertThatThrownBy(() -> repository.findByMac(unknownMac))
        .isInstanceOf(DeviceNotFoundException.class);
  }

  @Test
  void shouldReturnAllSavedDevices() {
    Device device1 = new Device(mac1, DeviceType.GATEWAY, null);
    Device device2 = new Device(mac2, DeviceType.SWITCH, device1);
    Device device3 = new Device(mac3, DeviceType.ACCESS_POINT, device2);
    repository.save(device1);
    repository.save(device2);
    repository.save(device3);

    List<Device> result = repository.findAll();

    assertThat(result)
        .hasSize(3)
        .extracting(Device::macAddress)
        .containsExactlyInAnyOrder(mac1, mac2, mac3);
  }

  @Test
  void shouldReturnEmptyListWhenNoDevicesSaved() {
    List<Device> result = repository.findAll();

    assertThat(result).isEmpty();
  }
}
