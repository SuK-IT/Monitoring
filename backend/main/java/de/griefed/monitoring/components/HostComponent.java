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

import de.griefed.monitoring.models.InformationModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import oshi.SystemInfo;
import oshi.hardware.NetworkIF;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Class responsible for retrieving all interesting information about the host.
 * @author Griefed
 */
@Service
public class HostComponent implements InformationModel {

    private final SystemInfo SYSTEM_INFO = new SystemInfo();
    private final List<NetworkIF> INTERFACES_LIST = SYSTEM_INFO.getHardware().getNetworkIFs();

    private List<HashMap<String, String>> interfacesInformationList = new ArrayList<>(100);
    private String hostInformation;
    private String hostName;
    private String domainName;

    /**
     * Constructor responsible for DI.
     * @author Griefed
     */
    @Autowired
    public HostComponent() {
        updateValues();
    }

    @Override
    public void sendNotification() {

    }

    /**
     * Set information from previously gathered information
     * @author Griefed
     */
    @Override
    public void setValues() {
        if (hostName == null || domainName == null || interfacesInformationList.isEmpty()) {
            updateValues();
        }
        StringBuilder stringBuilder = new StringBuilder(10000);

        stringBuilder.append("\"host_name\": \"").append(hostName).append("\",");
        stringBuilder.append("\"domain_name\": \"").append(domainName).append("\",");
        stringBuilder.append("\"interfaces\": [");
        if (interfacesInformationList.size() > 1) {

            stringBuilder.append("{");
            stringBuilder.append("\"interface_name\": \"").append(interfacesInformationList.get(0).get("interface_name")).append("\",");
            stringBuilder.append("\"ip\": \"").append(interfacesInformationList.get(0).get("ip")).append("\",");
            stringBuilder.append("\"subnet_mask\": \"").append(interfacesInformationList.get(0).get("subnet_mask")).append("\",");
            stringBuilder.append("\"mac\": \"").append(interfacesInformationList.get(0).get("mac")).append("\"");
            stringBuilder.append("},");

            for (int i = 1; i < INTERFACES_LIST.size() - 1; i++) {

                stringBuilder.append("{");
                stringBuilder.append("\"interface_name\": \"").append(interfacesInformationList.get(i).get("interface_name")).append("\",");
                stringBuilder.append("\"ip\": \"").append(interfacesInformationList.get(i).get("ip")).append("\",");
                stringBuilder.append("\"subnet_mask\": \"").append(interfacesInformationList.get(i).get("subnet_mask")).append("\",");
                stringBuilder.append("\"mac\": \"").append(interfacesInformationList.get(i).get("mac")).append("\"");
                stringBuilder.append("},");

            }

            stringBuilder.append("{");
            stringBuilder.append("\"interface_name\": \"").append(interfacesInformationList.get(interfacesInformationList.size() - 1).get("interface_name")).append("\",");
            stringBuilder.append("\"ip\": \"").append(interfacesInformationList.get(interfacesInformationList.size() - 1).get("ip")).append("\",");
            stringBuilder.append("\"subnet_mask\": \"").append(interfacesInformationList.get(interfacesInformationList.size() - 1).get("subnet_mask")).append("\",");
            stringBuilder.append("\"mac\": \"").append(interfacesInformationList.get(interfacesInformationList.size() - 1).get("mac")).append("\"");
            stringBuilder.append("}");

        } else {

            stringBuilder.append("{");
            stringBuilder.append("\"interface_name\": \"").append(interfacesInformationList.get(0).get("interface_name")).append("\",");
            stringBuilder.append("\"ip\": \"").append(interfacesInformationList.get(0).get("ip")).append("\",");
            stringBuilder.append("\"subnet_mask\": \"").append(interfacesInformationList.get(0).get("subnet_mask")).append("\",");
            stringBuilder.append("\"mac\": \"").append(interfacesInformationList.get(0).get("mac")).append("\"");
            stringBuilder.append("}");

        }

        stringBuilder.append("]");

        this.hostInformation = stringBuilder.toString();
    }

    /**
     * Update host information.
     * @author Griefed
     */
    @Override
    public void updateValues() {

        this.hostName = SYSTEM_INFO.getOperatingSystem().getNetworkParams().getHostName();
        this.domainName = SYSTEM_INFO.getOperatingSystem().getNetworkParams().getDomainName();

        List<HashMap<String, String>> list = new ArrayList<>();

        if (INTERFACES_LIST.size() > 1) {

            list.add(
                    new HashMap<String, String>() {
                        {
                            put("interface_name", INTERFACES_LIST.get(0).getName());
                            put("ip", getIpAddress(0));
                            put("subnet_mask", getSubnetMask(0));
                            put("mac", INTERFACES_LIST.get(0).getMacaddr());
                        }
                    });

            for (int i = 1; i < INTERFACES_LIST.size() -1; i++) {

                int finalI = i;
                list.add(
                        new HashMap<String, String>() {
                            {
                                put("interface_name", INTERFACES_LIST.get(finalI).getName());
                                put("ip", getIpAddress(finalI));
                                put("subnet_mask", getSubnetMask(finalI));
                                put("mac", INTERFACES_LIST.get(finalI).getMacaddr());
                            }
                        });

            }

            list.add(
                    new HashMap<String, String>() {
                        {
                            put("interface_name", INTERFACES_LIST.get(INTERFACES_LIST.size() - 1).getName());
                            put("ip", getIpAddress(INTERFACES_LIST.size() - 1));
                            put("subnet_mask", getSubnetMask(INTERFACES_LIST.size() - 1));
                            put("mac", INTERFACES_LIST.get(INTERFACES_LIST.size() - 1).getMacaddr());
                        }
                    });


        } else {

            list.add(
                    new HashMap<String, String>() {
                        {
                            put("interface_name", INTERFACES_LIST.get(0).getName());
                            put("ip", getIpAddress(0));
                            put("subnet_mask", getSubnetMask(0));
                            put("mac", INTERFACES_LIST.get(0).getMacaddr());
                        }
                    });

        }

        this.interfacesInformationList = list;
    }

    /**
     * Retrieve the IP address for a given entry in the list of interfaces.
     * @author Griefed
     * @param entry Int. THe entry to retrieve the IP for.
     * @return String. Returns the IP address of a network interface.
     */
    private String getIpAddress(int entry) {
        if (INTERFACES_LIST.size() >= 1) {
            return Arrays.toString(INTERFACES_LIST.get(entry).getIPv4addr()).replace("[","").replace("]","");
        } else {
            return "";
        }
    }

    /**
     * Retrieve the subnet mask for a given entry in the list of interfaces.
     * @author Griefed
     * @param entry Int. The entry to retrieve the subnet mask for.
     * @return String. Returns the subnet mask in CIDR notation.
     */
    private String getSubnetMask(int entry) {
        if (INTERFACES_LIST.size() >= 1) {
            return Arrays.toString(INTERFACES_LIST.get(entry).getSubnetMasks()).replace("[","").replace("]","");
        } else {
            return "";
        }
    }

    /**
     * Getter for the name of this component.
     * @author Griefed
     * @return String. Returns the name of the component.
     */
    @Override
    public String getName() {
        return "host";
    }

    /**
     * Getter for the information about the host. Gathers information about the host name, domain name, and for all the network
     * interfaces respectively, it gathers the name, ipv4 address, subnet mask in CIDR notation and the mac address.
     * @author Griefed
     * @return String. Information about the host in JSON format.
     */
    @Override
    public String getValues() {
        if (hostInformation == null) {
            setValues();
        }

        return hostInformation;
    }

    public String getHostName() {
        return hostName;
    }

    @Override
    public String toString() {
        return "\"" + getName() + "\": {" + getValues() + "}";
    }
}
