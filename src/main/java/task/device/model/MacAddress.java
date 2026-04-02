package task.device.model;

import inet.ipaddr.MACAddressString;
import org.apache.commons.lang3.StringUtils;
import task.common.exceptions.InvalidMacAddressException;

import java.util.Objects;

public class MacAddress {
    private final String mac;

    public MacAddress(String mac) {
        validate(mac);
        this.mac = mac;
    }

    private void validate(String mac) {
        boolean isValid = new MACAddressString(mac).isValid();
        if (StringUtils.isBlank(mac) || !isValid) {
            throw new InvalidMacAddressException(mac);
        }
    }

    public String value() {
        return mac;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        MacAddress that = (MacAddress) o;
        return Objects.equals(mac, that.mac);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(mac);
    }
}
