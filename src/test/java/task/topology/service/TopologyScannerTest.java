package task.topology.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import task.device.model.Device;
import task.device.model.DeviceType;
import task.device.model.MacAddress;
import task.device.repository.DeviceRepository;
import task.topology.web.TopologyResponse;

class TopologyScannerTest {

  private DeviceRepository deviceRepository;
  private TopologyScanner topologyScanner;

  private final MacAddress rootMac = new MacAddress("00:11:22:33:44:55");
  private final MacAddress child1Mac = new MacAddress("00:11:22:33:44:66");
  private final MacAddress child2Mac = new MacAddress("00:11:22:33:44:77");
  private final MacAddress orphanMac = new MacAddress("00:11:22:33:44:99");

  @BeforeEach
  void setUp() {
    deviceRepository = mock(DeviceRepository.class);
    topologyScanner = new TopologyScanner(deviceRepository);
  }

  @Test
  void shouldReturnEmptyListWhenNoDevicesExist() {
    when(deviceRepository.findAll()).thenReturn(List.of());

    List<TopologyResponse> result = topologyScanner.getTopology();

    assertThat(result).isEmpty();
  }

  @Test
  void shouldBuildTopologyWithMultipleRootDevices() {
    Device root1 = new Device(rootMac, DeviceType.GATEWAY, null);
    Device root2 = new Device(child2Mac, DeviceType.SWITCH, null);
    when(deviceRepository.findAll()).thenReturn(List.of(root1, root2));

    List<TopologyResponse> result = topologyScanner.getTopology();

    assertThat(result)
        .hasSize(2)
        .extracting(TopologyResponse::macAddress)
        .contains(rootMac.value(), child2Mac.value());
  }

  @Test
  void shouldIncludeChildDevicesInTopologyWhenUplinkIsRoot() {
    Device rootDevice = new Device(rootMac, DeviceType.GATEWAY, null);
    Device childDevice = new Device(child1Mac, DeviceType.SWITCH, rootDevice);
    when(deviceRepository.findAll()).thenReturn(List.of(rootDevice, childDevice));

    List<TopologyResponse> result = topologyScanner.getTopology();

    assertThat(result).hasSize(1);
    TopologyResponse rootResponse = result.get(0);
    assertThat(rootResponse.macAddress()).isEqualTo(rootMac.value());
    assertThat(rootResponse.children())
        .hasSize(1)
        .extracting(TopologyResponse::macAddress)
        .contains(child1Mac.value());
  }

  @Test
  void shouldBuildNestedTopologyWithMultipleLevels() {
    Device root = new Device(rootMac, DeviceType.GATEWAY, null);
    Device child = new Device(child1Mac, DeviceType.SWITCH, root);
    Device grandchild = new Device(child2Mac, DeviceType.SWITCH, child);
    when(deviceRepository.findAll()).thenReturn(List.of(root, child, grandchild));

    List<TopologyResponse> result = topologyScanner.getTopology();

    assertThat(result).hasSize(1);
    TopologyResponse rootResponse = result.get(0);
    assertThat(rootResponse.macAddress()).isEqualTo(rootMac.value());
    assertThat(rootResponse.children()).hasSize(1);

    TopologyResponse childResponse = rootResponse.children().get(0);
    assertThat(childResponse.macAddress()).isEqualTo(child1Mac.value());
    assertThat(childResponse.children()).hasSize(1);

    TopologyResponse grandchildResponse = childResponse.children().get(0);
    assertThat(grandchildResponse.macAddress()).isEqualTo(child2Mac.value());
    assertThat(grandchildResponse.children()).isEmpty();
  }

  @Test
  void shouldReturnTopologyWithMultipleChildrenUnderSameParent() {
    Device root = new Device(rootMac, DeviceType.GATEWAY, null);
    Device child1 = new Device(child1Mac, DeviceType.SWITCH, root);
    Device child2 = new Device(child2Mac, DeviceType.SWITCH, root);
    when(deviceRepository.findAll()).thenReturn(List.of(root, child1, child2));

    List<TopologyResponse> result = topologyScanner.getTopology();

    assertThat(result).hasSize(1);
    TopologyResponse rootResponse = result.get(0);
    assertThat(rootResponse.children())
        .hasSize(2)
        .extracting(TopologyResponse::macAddress)
        .contains(child1Mac.value(), child2Mac.value());
  }

  @Test
  void shouldReturnSpecificTopologyByRootMacAddress() {
    Device root = new Device(rootMac, DeviceType.GATEWAY, null);
    Device child = new Device(child1Mac, DeviceType.SWITCH, root);
    Device orphan = new Device(orphanMac, DeviceType.GATEWAY, null);
    when(deviceRepository.findByMac(rootMac)).thenReturn(root);
    when(deviceRepository.findAll()).thenReturn(List.of(root, child, orphan));

    TopologyResponse result = topologyScanner.getTopology(rootMac);

    assertThat(result.macAddress()).isEqualTo(rootMac.value());
    assertThat(result.children())
        .hasSize(1)
        .extracting(TopologyResponse::macAddress)
        .contains(child1Mac.value());
  }
}
