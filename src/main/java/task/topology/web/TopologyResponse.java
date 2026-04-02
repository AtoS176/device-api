package task.topology.web;

import task.device.model.MacAddress;

import java.util.ArrayList;
import java.util.List;

public record TopologyResponse(String macAddress, List<TopologyResponse> children) {

    public TopologyResponse(MacAddress macAddress){
        this(macAddress.value(), new ArrayList<>());
    }
}
