# device-api

A Spring Boot REST API for managing network devices and their topology relationships.

## Requirements

- **Java 17+**
- **Gradle** (included via wrapper)

## Getting Started

### Build

```bash
./gradlew build
```

### Run Tests

Run all tests:
```bash
./gradlew test
```

### Run the Application

```bash
./gradlew bootRun
```

The API will be available at `http://localhost:8080`

### Run with Docker

Build the Docker image:
```bash
docker build -t device-api:latest .
```

Run the container:
```bash
docker run -p 8080:8080 device-api:latest
```

The API will be available at `http://localhost:8080`

## API Documentation

### Device Management

#### Register a Device
```
POST /api/v1/devices
```

**Request Body:**
```json
{
  "macAddress": "00:11:22:33:44:55",
  "deviceType": "ROUTER",
  "uplink": "00:11:22:33:44:33"
}
```

**Success Response:**
- **Code:** 201 Created
- **Body:** Empty

**Error Responses:**

| Status | Error Code | Title | Details |
|--------|-----------|-------|---------|
| 400 | `INVALID_MAC_ADDRESS` | Invalid MAC address | Provided MAC address has invalid format |
| 409 | `CANNOT_REGISTER_DEVICE` | Cannot register device | Device with this MAC address was already registered |
| 400 | `MISSING_UPLINK_DEVICE` | Missing uplink device | Specified uplink device (MAC address) was not found |
| 400 | `INVALID_REQUEST_CONTENT` | Validation Error | Request validation failed (field-level errors included) |

---

#### List All Devices
```
GET /api/v1/devices
```

**Success Response:**
- **Code:** 200 OK
- **Body:** Array of device objects, sorted by device type order
```json
[
  {
    "macAddress": "00:11:22:33:44:55",
    "deviceType": "ROUTER"
  },
  {
    "macAddress": "00:11:22:33:44:66",
    "deviceType": "SWITCH"
  }
]
```

---

#### Get a Specific Device
```
GET /api/v1/devices/{macAddress}
```

**Path Parameters:**
- `macAddress` (string, required): MAC address of the device (e.g., `00:11:22:33:44:55`)

**Success Response:**
- **Code:** 200 OK
- **Body:**
```json
{
  "macAddress": "00:11:22:33:44:55",
  "deviceType": "ROUTER"
}
```

**Error Responses:**

| Status | Error Code | Title | Details |
|--------|-----------|-------|---------|
| 404 | `DEVICE_NOT_FOUND` | Device not found | Device with the specified MAC address does not exist |
| 400 | `INVALID_MAC_ADDRESS` | Invalid MAC address | Provided MAC address has invalid format |

---

### Topology Endpoints

#### Get All Device Topologies
```
GET /api/v1/topology
```

**Success Response:**
- **Code:** 200 OK
- **Body:** Array of topology trees (one per root device)
```json
[
  {
    "macAddress": "00:11:22:33:44:55",
    "children": [
      {
        "macAddress": "00:11:22:33:44:66",
        "children": []
      }
    ]
  }
]
```

---

#### Get Topology for a Specific Root Device
```
GET /api/v1/topology/{macAddress}
```

**Path Parameters:**
- `macAddress` (string, required): MAC address of the root device

**Success Response:**
- **Code:** 200 OK
- **Body:** Topology tree rooted at the specified device
```json
{
  "macAddress": "00:11:22:33:44:55",
  "children": [
    {
      "macAddress": "00:11:22:33:44:66",
      "children": [
        {
          "macAddress": "00:11:22:33:44:77",
          "children": []
        }
      ]
    }
  ]
}
```

**Error Responses:**

| Status | Error Code | Title | Details |
|--------|-----------|-------|---------|
| 404 | `DEVICE_NOT_FOUND` | Device not found | Device with the specified MAC address does not exist |
| 400 | `INVALID_MAC_ADDRESS` | Invalid MAC address | Provided MAC address has invalid format |

---

## Error Response Format

All error responses follow the RFC 9457 Problem Detail format:

```json
{
  "type": "about:blank",
  "title": "Error title",
  "status": 400,
  "detail": "Detailed error message",
  "errorCode": "ERROR_CODE"
}
```

**Fields:**
- `title`: Human-readable error title
- `detail`: Detailed error description
- `status`: HTTP status code
- `errorCode`: Machine-readable error code for programmatic handling

For validation errors (400 Bad Request with `INVALID_REQUEST_CONTENT`), an additional `errors` field contains field-level validation errors:

```json
{
  "title": "Validation Error",
  "status": 400,
  "detail": "The request content contains invalid data",
  "errorCode": "INVALID_REQUEST_CONTENT",
  "errors": {
    "macAddress": ["must not be null"],
    "deviceType": ["must not be null"]
  }
}
```
