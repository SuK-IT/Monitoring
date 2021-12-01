/* MIT License
 *
 * Copyright (c) 2021 SUK-IT
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package de.griefed.monitoring.components;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.griefed.monitoring.Main;
import de.griefed.monitoring.models.InformationModel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import oshi.SystemInfo;
import oshi.hardware.NetworkIF;

import java.util.Arrays;
import java.util.List;

@Service
public class HostComponent implements InformationModel {

    private final StringBuilder HOST_INFORMATION = new StringBuilder();
    private final SystemInfo SYSTEM_INFO = new SystemInfo();
    private final List<NetworkIF> interfaces = SYSTEM_INFO.getHardware().getNetworkIFs();

    @Autowired
    public HostComponent() {

    }

    @Override
    public String getName() {
        return "host";
    }

    @Override
    public String getValue() {
        if (HOST_INFORMATION.length() > 0) {
            HOST_INFORMATION.delete(0, HOST_INFORMATION.length());
        }

        HOST_INFORMATION.append("\"host_name\": \"").append(SYSTEM_INFO.getOperatingSystem().getNetworkParams().getHostName()).append("\",");
        HOST_INFORMATION.append("\"domain_name\": \"").append(SYSTEM_INFO.getOperatingSystem().getNetworkParams().getDomainName()).append("\",");
        HOST_INFORMATION.append("\"interfaces\": [");
        if (interfaces.size() > 1) {

            HOST_INFORMATION.append("{");
            HOST_INFORMATION.append("\"interface_name\": \"").append(interfaces.get(0).getName()).append("\",");
            HOST_INFORMATION.append("\"ip\": \"").append(Arrays.toString(interfaces.get(0).getIPv4addr()).replace("[","").replace("]","")).append("\",");
            HOST_INFORMATION.append("\"subnet_mask\": \"").append(Arrays.toString(interfaces.get(0).getSubnetMasks()).replace("[","").replace("]","")).append("\",");
            HOST_INFORMATION.append("\"mac\": \"").append(interfaces.get(0).getMacaddr()).append("\"");
            HOST_INFORMATION.append("},");

            for (int i = 1; i < interfaces.size() - 1; i++) {

                HOST_INFORMATION.append("{");
                HOST_INFORMATION.append("\"interface_name\": \"").append(interfaces.get(i).getName()).append("\",");
                HOST_INFORMATION.append("\"ip\": \"").append(Arrays.toString(interfaces.get(i).getIPv4addr()).replace("[","").replace("]","")).append("\",");
                HOST_INFORMATION.append("\"subnet_mask\": \"").append(Arrays.toString(interfaces.get(i).getSubnetMasks()).replace("[","").replace("]","")).append("\",");
                HOST_INFORMATION.append("\"mac\": \"").append(interfaces.get(i).getMacaddr()).append("\"");
                HOST_INFORMATION.append("},");

            }

            HOST_INFORMATION.append("{");
            HOST_INFORMATION.append("\"interface_name\": \"").append(interfaces.get(interfaces.size() - 1).getName()).append("\",");
            HOST_INFORMATION.append("\"ip\": \"").append(Arrays.toString(interfaces.get(interfaces.size() - 1).getIPv4addr()).replace("[","").replace("]","")).append("\",");
            HOST_INFORMATION.append("\"subnet_mask\": \"").append(Arrays.toString(interfaces.get(interfaces.size() - 1).getSubnetMasks()).replace("[","").replace("]","")).append("\",");
            HOST_INFORMATION.append("\"mac\": \"").append(interfaces.get(interfaces.size() - 1).getMacaddr()).append("\"");
            HOST_INFORMATION.append("}");

        } else {

            HOST_INFORMATION.append("{");
            HOST_INFORMATION.append("\"interface_name\": \"").append(interfaces.get(0).getName()).append("\",");
            HOST_INFORMATION.append("\"ip\": \"").append(Arrays.toString(interfaces.get(0).getIPv4addr()).replace("[","").replace("]","")).append("\",");
            HOST_INFORMATION.append("\"subnet_mask\": \"").append(Arrays.toString(interfaces.get(0).getSubnetMasks()).replace("[","").replace("]","")).append("\",");
            HOST_INFORMATION.append("\"mac\": \"").append(interfaces.get(0).getMacaddr()).append("\"");
            HOST_INFORMATION.append("}");

        }

        HOST_INFORMATION.append("]");

        return HOST_INFORMATION.toString();
    }

    @Override
    public String toString() {
        return "\"" + getName() + "\": {" + getValue() + "}";
    }
}
