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

import de.griefed.monitoring.ApplicationProperties;
import de.griefed.monitoring.models.InformationModel;
import de.griefed.monitoring.utilities.MailNotification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import oshi.SystemInfo;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.PhysicalMemory;

import javax.mail.MessagingException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class responsible for retrieving all interesting values about the memory.
 * @author Griefed
 */
@Service
public class RamComponent implements InformationModel {

    private static final Logger LOG = LogManager.getLogger(RamComponent.class);

    private final SystemInfo SYSTEM_INFO = new SystemInfo();
    private final List<PhysicalMemory> PHYSICAL_MEMORY = SYSTEM_INFO.getHardware().getMemory().getPhysicalMemory();
    private final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.00");
    private final Map<String, String> vmVendor = new HashMap<String, String>() {
        {
            put("bhyve bhyve", "bhyve");
            put("KVMKVMKVM", "KVM");
            put("TCGTCGTCGTCG", "QEMU");
            put("Microsoft Hv", "Microsoft Hyper-V or Windows Virtual PC");
            put("lrpepyh vr", "Parallels");
            put("VMwareVMware", "VMware");
            put("XenVMMXenVMM", "Xen HVM");
            put("ACRNACRNACRN", "Project ACRN");
            put("QNXQVMBSQG", "QNX Hypervisor");
        }
    };
    private final String[] vmModelArray = new String[] {"Linux KVM","Linux lguest","OpenVZ","Qemu","Microsoft Virtual PC","VMWare","linux-vserver","Xen","FreeBSD Jail","VirtualBox","Parallels","Linux Containers","LXC"};
    private final ApplicationProperties PROPERTIES;
    private final HostComponent HOST_COMPONENT;
    private final MailNotification MAIL_NOTIFICATION;

    private List<HashMap<String, String>> ramInformationList = new ArrayList<>(100);
    private String ramInformation;
    private String total;
    private String available;
    private String used;

    /**
     * Constructor responsible for DI.
     * @author Griefed
     */
    @Autowired
    public RamComponent(ApplicationProperties injectedApplicationProperties, HostComponent injectedHostComponent, MailNotification injectedMailNotification) {
        this.PROPERTIES = injectedApplicationProperties;
        this.HOST_COMPONENT = injectedHostComponent;
        this.MAIL_NOTIFICATION = injectedMailNotification;
        updateValues();
    }

    @Override
    public void sendNotification() throws MessagingException {

        if (Float.parseFloat(used.replace(" %","").replace(",",".")) >= Float.parseFloat(PROPERTIES.getProperty("de.griefed.monitoring.schedule.email.notification.disk.usage"))) {
            MAIL_NOTIFICATION.sendMailNotification(
                    "Disk on " + HOST_COMPONENT.getHostName() + " at critical capacity!",
                    "Memory usage has reached critical usage levels of " + used + ".\n" +
                            "Memory remaining: " + available + "."
            );
        }

    }

    /**
     * Set information from previously gathered information
     * @author Griefed
     */
    @Override
    public void setValues() {
        if (ramInformationList.isEmpty()) {
            updateValues();
        }

        StringBuilder stringBuilder = new StringBuilder(10000);

        stringBuilder.append("\"total\": \"").append(total).append("\",");
        stringBuilder.append("\"available\": \"").append(available).append("\",");
        stringBuilder.append("\"used\": \"").append(used).append("\",");

        stringBuilder.append("\"physical_memory\": [");

        if (!identifyVM() && PHYSICAL_MEMORY.size() >= 1) {

            if (ramInformationList.size() > 1) {

                stringBuilder.append("{");
                stringBuilder.append("\"bank\": \"").append(ramInformationList.get(0).get("bank")).append("\",");
                stringBuilder.append("\"capacity\": \"").append(ramInformationList.get(0).get("capacity")).append("\",");
                stringBuilder.append("\"type\": \"").append(ramInformationList.get(0).get("type")).append("\"");
                stringBuilder.append("},");

                for (int i = 1; i < ramInformationList.size() - 1; i++) {
                    stringBuilder.append("{");
                    stringBuilder.append("\"bank\": \"").append(ramInformationList.get(i).get("bank")).append("\",");
                    stringBuilder.append("\"capacity\": \"").append(ramInformationList.get(i).get("capacity")).append("\",");
                    stringBuilder.append("\"type\": \"").append(ramInformationList.get(i).get("type")).append("\"");
                    stringBuilder.append("},");
                }

                stringBuilder.append("{");
                stringBuilder.append("\"bank\": \"").append(ramInformationList.get(ramInformationList.size() - 1).get("bank")).append("\",");
                stringBuilder.append("\"capacity\": \"").append(ramInformationList.get(ramInformationList.size() - 1).get("capacity")).append("\",");
                stringBuilder.append("\"type\": \"").append(ramInformationList.get(ramInformationList.size() - 1).get("type")).append("\"");
                stringBuilder.append("}");

            } else {

                stringBuilder.append("{");
                stringBuilder.append("\"bank\": \"").append(ramInformationList.get(0).get("bank")).append("\",");
                stringBuilder.append("\"capacity\": \"").append(ramInformationList.get(0).get("capacity")).append("\",");
                stringBuilder.append("\"type\": \"").append(ramInformationList.get(0).get("type")).append("\"");
                stringBuilder.append("}");

            }

        }
        
        stringBuilder.append("]");

        this.ramInformation = stringBuilder.toString();
    }

    /**
     * Update memory information.
     * @author Griefed
     */
    @Override
    public void updateValues() {

        this.total = DECIMAL_FORMAT.format(SYSTEM_INFO.getHardware().getMemory().getTotal() / 1073741824F) + " GB";
        this.available = DECIMAL_FORMAT.format(SYSTEM_INFO.getHardware().getMemory().getAvailable() / 1073741824F) + " GB";
        this.used = DECIMAL_FORMAT.format(100F - ((100F / SYSTEM_INFO.getHardware().getMemory().getTotal()) * SYSTEM_INFO.getHardware().getMemory().getAvailable())) + " %";

        List<HashMap<String, String>> list = new ArrayList<>(100);

        if (!identifyVM() && PHYSICAL_MEMORY.size() >= 1) {
            if (PHYSICAL_MEMORY.size() > 1) {

                list.add(
                        new HashMap<String, String>() {
                            {
                                put("bank", PHYSICAL_MEMORY.get(0).getBankLabel());
                                put("capacity", (PHYSICAL_MEMORY.get(0).getCapacity() / 1073741824F) + " GB");
                                put("type", PHYSICAL_MEMORY.get(0).getMemoryType());
                            }
                        });

                for (int i = 1; i < PHYSICAL_MEMORY.size() -1; i++) {

                    int finalI = i;
                    list.add(
                            new HashMap<String, String>() {
                                {
                                    put("bank", PHYSICAL_MEMORY.get(finalI).getBankLabel());
                                    put("capacity", (PHYSICAL_MEMORY.get(finalI).getCapacity() / 1073741824F) + " GB");
                                    put("type", PHYSICAL_MEMORY.get(finalI).getMemoryType());
                                }
                            });

                }

                list.add(
                        new HashMap<String, String>() {
                            {
                                put("bank", PHYSICAL_MEMORY.get(PHYSICAL_MEMORY.size() - 1).getBankLabel());
                                put("capacity", (PHYSICAL_MEMORY.get(PHYSICAL_MEMORY.size() - 1).getCapacity() / 1073741824F) + " GB");
                                put("type", PHYSICAL_MEMORY.get(PHYSICAL_MEMORY.size() - 1).getMemoryType());
                            }
                        });


            } else {

                list.add(
                        new HashMap<String, String>() {
                            {
                                put("bank", PHYSICAL_MEMORY.get(0).getBankLabel());
                                put("capacity", (PHYSICAL_MEMORY.get(0).getCapacity() / 1073741824F) + " GB");
                                put("type", PHYSICAL_MEMORY.get(0).getMemoryType());
                            }
                        });

            }
        }


        this.ramInformationList = list;
    }

    /**
     * Getter for the name of this component.
     * @author Griefed
     * @return String. Returns the name of this component.
     */
    @Override
    public String getName() {
        return "memory";
    }

    /**
     * Getter for the information about the memory. Gathers information about the total available memory, free memory and,
     * if applicable, gathers additional information about the physical memory sticks bank, capacity and type.
     * @author Griefed
     * @return String. Information about the memory in JSON format.
     */
    @Override
    public String getValues() {
        if (ramInformation == null) {
            setValues();
        }

        return ramInformation;
    }

    @Override
    public String toString() {
        return "\"" + getName() + "\": {" + getValues() + "}";
    }

    private boolean identifyVM() {
        SystemInfo si = new SystemInfo();
        HardwareAbstractionLayer hw = si.getHardware();
        // Check CPU Vendor
        String vendor = hw.getProcessor().getProcessorIdentifier().getVendor().trim();
        if (vmVendor.containsKey(vendor)) {
            LOG.info("vendor: " + vendor);
            return true;
        }

        // Try well known models
        String model = hw.getComputerSystem().getModel();
        for (String vm : vmModelArray) {
            if (model.contains(vm)) {
                LOG.info("model: " + model);
                return true;
            }
        }
        String manufacturer = hw.getComputerSystem().getManufacturer();
        if ("Microsoft Corporation".equals(manufacturer) && "Virtual Machine".equals(model)) {
            LOG.info("manufacturer: " + manufacturer);
            return true;
        }

        // Couldn't find VM, return empty string
        return false;
    }
}
