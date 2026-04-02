package task.device.web;

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
class DeviceControllerIntegrationTest {

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
  void shouldReturnEmptyDeviceListWhenNoDevicesExist() {
    given().when()
        .get("/api/v1/devices")
        .then()
        .statusCode(200)
        .body("$", empty());
  }

  @Test
  void shouldReturnSingleDevice() {
    MacAddress macAddress = new MacAddress("00:11:22:33:44:55");
    Device device = new Device(macAddress, DeviceType.GATEWAY, null);
    deviceRepository.save(device);

    given().when()
        .get("/api/v1/devices")
        .then()
        .statusCode(200)
        .body("$", hasSize(1))
        .body("[0].macAddress", equalTo(macAddress.value()))
        .body("[0].deviceType", equalTo("GATEWAY"));
  }

  @Test
  void shouldReturnMultipleDevicesSortedByDeviceType() {
    MacAddress switchMac = new MacAddress("00:11:22:33:44:55");
    MacAddress accessPointMac = new MacAddress("00:11:22:33:44:66");
    MacAddress gatewayMac = new MacAddress("00:11:22:33:44:77");
    deviceRepository.save(new Device(switchMac, DeviceType.SWITCH, null));
    deviceRepository.save(new Device(accessPointMac, DeviceType.ACCESS_POINT, null));
    deviceRepository.save(new Device(gatewayMac, DeviceType.GATEWAY, null));

    given().when()
        .get("/api/v1/devices")
        .then()
        .statusCode(200)
        .body("$", hasSize(3))
        .body("[0].deviceType", equalTo("GATEWAY"))
        .body("[1].deviceType", equalTo("SWITCH"))
        .body("[2].deviceType", equalTo("ACCESS_POINT"));
  }

  @Test
  void shouldGetSingleDeviceByMacAddress() {
    MacAddress macAddress = new MacAddress("00:11:22:33:44:55");
    Device device = new Device(macAddress, DeviceType.SWITCH, null);
    deviceRepository.save(device);

    given().when()
        .get("/api/v1/devices/" + macAddress.value())
        .then()
        .statusCode(200)
        .body("macAddress", equalTo(macAddress.value()))
        .body("deviceType", equalTo("SWITCH"));
  }

  @Test
  void shouldRegisterNewRootDevice() {
    String macAddress = "00:11:22:33:44:55";
    String requestBody = "{\"macAddress\": \"" + macAddress + "\", \"deviceType\": \"GATEWAY\", \"uplink\": null}";

    given()
        .contentType("application/json")
        .body(requestBody)
        .when()
        .post("/api/v1/devices")
        .then()
        .statusCode(201);

    given().when()
        .get("/api/v1/devices/" + macAddress)
        .then()
        .statusCode(200)
        .body("macAddress", equalTo(macAddress))
        .body("deviceType", equalTo("GATEWAY"));
  }

  @Test
  void shouldRegisterDeviceWithUplink() {
    String rootMac = "00:11:22:33:44:55";
    String childMac = "00:11:22:33:44:66";
    Device rootDevice = new Device(new MacAddress(rootMac), DeviceType.GATEWAY, null);
    deviceRepository.save(rootDevice);

    String childRequestBody =
        "{\"macAddress\": \"" + childMac + "\", \"deviceType\": \"SWITCH\", \"uplink\": \"" + rootMac + "\"}";

    given()
        .contentType("application/json")
        .body(childRequestBody)
        .when()
        .post("/api/v1/devices")
        .then()
        .statusCode(201);

    given().when()
        .get("/api/v1/devices/" + childMac)
        .then()
        .statusCode(200)
        .body("macAddress", equalTo(childMac))
        .body("deviceType", equalTo("SWITCH"));
  }

  @Test
  void shouldReturn400WhenRegisteringDeviceWithoutMacAddress() {
    String requestBody = "{\"deviceType\": \"GATEWAY\", \"uplink\": null}";

    given()
        .contentType("application/json")
        .body(requestBody)
        .when()
        .post("/api/v1/devices")
        .then()
        .statusCode(400)
        .body("title", equalTo("Validation Error"))
        .body("errorCode", equalTo("INVALID_REQUEST_CONTENT"))
        .body("errors", notNullValue());
  }

  @Test
  void shouldReturn400WhenRegisteringDeviceWithoutDeviceType() {
    String requestBody = "{\"macAddress\": \"00:11:22:33:44:55\", \"uplink\": null}";

    given()
        .contentType("application/json")
        .body(requestBody)
        .when()
        .post("/api/v1/devices")
        .then()
        .statusCode(400)
        .body("title", equalTo("Validation Error"))
        .body("errorCode", equalTo("INVALID_REQUEST_CONTENT"))
        .body("errors", notNullValue());
  }

  @Test
  void shouldReturn400WhenRegisteringDeviceWithInvalidMacAddress() {
    String invalidMacJson = "{\"macAddress\": \"invalid-mac\", \"deviceType\": \"GATEWAY\", \"uplink\": null}";

    given()
        .contentType("application/json")
        .body(invalidMacJson)
        .when()
        .post("/api/v1/devices")
        .then()
        .statusCode(400)
        .body("title", equalTo("Invalid MAC address"))
        .body("errorCode", equalTo("INVALID_MAC_ADDRESS"));
  }

  @Test
  void shouldReturn400WhenRegisteringDeviceWithNonExistentUplink() {
    String requestBody =
        "{\"macAddress\": \"00:11:22:33:44:55\", \"deviceType\": \"SWITCH\", \"uplink\": \"FF:FF:FF:FF:FF:FF\"}";

    given()
        .contentType("application/json")
        .body(requestBody)
        .when()
        .post("/api/v1/devices")
        .then()
        .statusCode(400)
        .body("title", equalTo("Missing uplink device"))
        .body("errorCode", equalTo("MISSING_UPLINK_DEVICE"));
  }

  @Test
  void shouldReturn404WhenGettingNonExistentDevice() {
    MacAddress nonExistentMac = new MacAddress("FF:FF:FF:FF:FF:FF");

    given().when()
        .get("/api/v1/devices/" + nonExistentMac.value())
        .then()
        .statusCode(404)
        .body("title", equalTo("Device not found"))
        .body("errorCode", equalTo("DEVICE_NOT_FOUND"));
  }

  @Test
  void shouldReturn400WhenGettingDeviceWithInvalidMacAddress() {
    String invalidMac = "invalid-mac-address";

    given().when()
        .get("/api/v1/devices/" + invalidMac)
        .then()
        .statusCode(400)
        .body("title", equalTo("Invalid MAC address"))
        .body("errorCode", equalTo("INVALID_MAC_ADDRESS"));
  }

  @Test
  void shouldReturn409WhenRegisteringDuplicateDevice() {
    String macAddress = "00:11:22:33:44:55";
    Device device = new Device(new MacAddress(macAddress), DeviceType.GATEWAY, null);
    deviceRepository.save(device);

    String duplicateRequestBody =
        "{\"macAddress\": \"" + macAddress + "\", \"deviceType\": \"SWITCH\", \"uplink\": null}";

    given()
        .contentType("application/json")
        .body(duplicateRequestBody)
        .when()
        .post("/api/v1/devices")
        .then()
        .statusCode(409)
        .body("title", equalTo("Cannot register device"))
        .body("errorCode", equalTo("CANNOT_REGISTER_DEVICE"));
  }
}
