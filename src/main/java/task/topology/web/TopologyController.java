package task.topology.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import task.device.model.MacAddress;
import task.topology.service.TopologyScanner;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class TopologyController {
    private final TopologyScanner topologyScanner;

    public TopologyController(TopologyScanner topologyScanner) {
        this.topologyScanner = topologyScanner;
    }

    @GetMapping("/topology")
    public ResponseEntity<List<TopologyResponse>> getTopology() {
        return ResponseEntity.ok(topologyScanner.getTopology());
    }

    @GetMapping("/topology/{macAddress}")
    public ResponseEntity<TopologyResponse> getTopologyByRoot(@PathVariable(name = "macAddress") MacAddress macAddress) {
        return ResponseEntity.ok(topologyScanner.getTopology(macAddress));
    }
}
