package task.topology.web;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

import io.restassured.RestAssured;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.util.ReflectionTestUtils;
import task.device.model.Device;
import task.device.model.DeviceType;
import task.device.model.MacAddress;
import task.device.repository.DeviceRepository;

import java.util.Map;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TopologyControllerIntegrationTest {

  @LocalServerPort
  private int port;

  @Autowired
  private DeviceRepository deviceRepository;

  @BeforeEach
  public void setUp() {
    RestAssured.port = port;
    RestAssured.baseURI = "http://localhost";
  }

  @AfterEach
  void tearDown() {
    Object internalMap = ReflectionTestUtils.getField(deviceRepository, "storage");
    if (internalMap instanceof Map) {
      ((Map<?, ?>) internalMap).clear();
    }
  }

  @Test
  void shouldReturnEmptyTopologyWhenNoDevicesExist() {
    given().when()
        .get("/api/v1/topology")
        .then()
        .statusCode(200)
        .body("$", empty());
  }

  @Test
  void shouldReturnTopologyWithSingleRootDevice() {
    MacAddress rootMac = new MacAddress("00:11:22:33:44:55");
    Device rootDevice = new Device(rootMac, DeviceType.GATEWAY, null);
    deviceRepository.save(rootDevice);

    given().when()
        .get("/api/v1/topology")
        .then()
        .statusCode(200)
        .body("$", hasSize(1))
        .body("[0].macAddress", equalTo(rootMac.value()))
        .body("[0].children", empty());
  }

  @Test
  void shouldReturnMultipleRootDevices() {
    MacAddress root1Mac = new MacAddress("00:11:22:33:44:55");
    MacAddress root2Mac = new MacAddress("00:11:22:33:44:66");
    Device root1 = new Device(root1Mac, DeviceType.GATEWAY, null);
    Device root2 = new Device(root2Mac, DeviceType.SWITCH, null);
    deviceRepository.save(root1);
    deviceRepository.save(root2);

    given().when()
        .get("/api/v1/topology")
        .then()
        .statusCode(200)
        .body("$", hasSize(2))
        .body("macAddress", hasSize(2));
  }

  @Test
  void shouldReturnMultiLevelNestingTopology() {
    MacAddress rootMac = new MacAddress("00:11:22:33:44:55");
    MacAddress childMac = new MacAddress("00:11:22:33:44:66");
    MacAddress grandchildMac = new MacAddress("00:11:22:33:44:77");
    Device root = new Device(rootMac, DeviceType.GATEWAY, null);
    Device child = new Device(childMac, DeviceType.SWITCH, root);
    Device grandchild = new Device(grandchildMac, DeviceType.ACCESS_POINT, child);
    deviceRepository.save(root);
    deviceRepository.save(child);
    deviceRepository.save(grandchild);

    given().when()
        .get("/api/v1/topology")
        .then()
        .statusCode(200)
        .body("$", hasSize(1))
        .body("[0].macAddress", equalTo(rootMac.value()))
        .body("[0].children", hasSize(1))
        .body("[0].children[0].macAddress", equalTo(childMac.value()))
        .body("[0].children[0].children", hasSize(1))
        .body("[0].children[0].children[0].macAddress", equalTo(grandchildMac.value()));
  }

  @Test
  void shouldReturnTopologyBySpecificRootMacAddress() {
    MacAddress root1Mac = new MacAddress("00:11:22:33:44:55");
    MacAddress root2Mac = new MacAddress("00:11:22:33:44:66");
    MacAddress child1Mac = new MacAddress("00:11:22:33:44:77");
    Device root1 = new Device(root1Mac, DeviceType.GATEWAY, null);
    Device root2 = new Device(root2Mac, DeviceType.SWITCH, null);
    Device child1 = new Device(child1Mac, DeviceType.ACCESS_POINT, root1);
    deviceRepository.save(root1);
    deviceRepository.save(root2);
    deviceRepository.save(child1);

    given().when()
        .get("/api/v1/topology/" + root1Mac.value())
        .then()
        .statusCode(200)
        .body("macAddress", equalTo(root1Mac.value()))
        .body("children", hasSize(1))
        .body("children[0].macAddress", equalTo(child1Mac.value()));
  }

  @Test
  void shouldReturn404WhenTopologyRootDeviceNotFound() {
    MacAddress nonExistentMac = new MacAddress("FF:FF:FF:FF:FF:FF");

    given().when()
        .get("/api/v1/topology/" + nonExistentMac.value())
        .then()
        .statusCode(404)
        .body("title", equalTo("Device not found"))
        .body("errorCode", equalTo("DEVICE_NOT_FOUND"))
        .body("detail", notNullValue());
  }

  @Test
  void shouldReturn400WhenTopologyRootMacAddressIsInvalid() {
    String invalidMac = "invalid-mac-address";

    given().when()
        .get("/api/v1/topology/" + invalidMac)
        .then()
        .statusCode(400)
        .body("title", equalTo("Invalid MAC address"))
        .body("errorCode", equalTo("INVALID_MAC_ADDRESS"))
        .body("detail", notNullValue());
  }
}
