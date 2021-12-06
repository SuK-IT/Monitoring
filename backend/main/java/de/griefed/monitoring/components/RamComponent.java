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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import oshi.SystemInfo;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.PhysicalMemory;

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

    private final StringBuilder RAM_INFORMATION = new StringBuilder();
    private final SystemInfo SYSTEM_INFO = new SystemInfo();
    private final Map<String, String> vmVendor = new HashMap<>();
    private final String[] vmModelArray = new String[] {
            "Linux KVM",
            "Linux lguest",
            "OpenVZ",
            "Qemu",
            "Microsoft Virtual PC",
            "VMWare",
            "linux-vserver",
            "Xen",
            "FreeBSD Jail",
            "VirtualBox",
            "Parallels",
            "Linux Containers",
            "LXC"};
    private final String OSHI_VM_MAC_ADDR_PROPERTIES = "oshi.vmmacaddr.properties";

    /**
     * Constructor responsible for DI.
     * @author Griefed
     */
    @Autowired
    public RamComponent() {
        vmVendor.put("bhyve bhyve", "bhyve");
        vmVendor.put("KVMKVMKVM", "KVM");
        vmVendor.put("TCGTCGTCGTCG", "QEMU");
        vmVendor.put("Microsoft Hv", "Microsoft Hyper-V or Windows Virtual PC");
        vmVendor.put("lrpepyh vr", "Parallels");
        vmVendor.put("VMwareVMware", "VMware");
        vmVendor.put("XenVMMXenVMM", "Xen HVM");
        vmVendor.put("ACRNACRNACRN", "Project ACRN");
        vmVendor.put("QNXQVMBSQG", "QNX Hypervisor");
    }

    @Override
    public void sendNotification() {

    }

    @Override
    public void setValues() {

    }

    @Override
    public void updateValues() {

    }

    /**
     * Getter for the name of this compnent.
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
        if (RAM_INFORMATION.length() > 0) {
            RAM_INFORMATION.delete(0, RAM_INFORMATION.length());
        }

        RAM_INFORMATION.append("\"total\": \"").append(SYSTEM_INFO.getHardware().getMemory().getTotal() / 1073741824).append(" GB\",");
        RAM_INFORMATION.append("\"available\": \"").append(SYSTEM_INFO.getHardware().getMemory().getAvailable() / 1073741824).append(" GB\"");

        if (!identifyVM() && SYSTEM_INFO.getHardware().getMemory().getPhysicalMemory().size() >= 1) {

            List<PhysicalMemory> physicalMemoryList = SYSTEM_INFO.getHardware().getMemory().getPhysicalMemory();

            RAM_INFORMATION.append(",").append("\"physical_memory\": [");

            if (physicalMemoryList.size() > 1) {

                RAM_INFORMATION.append("{");
                RAM_INFORMATION.append("\"bank\": \"").append(physicalMemoryList.get(0).getBankLabel()).append("\",");
                RAM_INFORMATION.append("\"capacity\": \"").append(physicalMemoryList.get(0).getCapacity() / 1073741824).append(" GB\",");
                RAM_INFORMATION.append("\"type\": \"").append(physicalMemoryList.get(0).getMemoryType()).append("\"");
                RAM_INFORMATION.append("},");

                for (int i = 1; i < physicalMemoryList.size() - 1; i++) {
                    RAM_INFORMATION.append("{");
                    RAM_INFORMATION.append("\"bank\": \"").append(physicalMemoryList.get(i).getBankLabel()).append("\",");
                    RAM_INFORMATION.append("\"capacity\": \"").append(physicalMemoryList.get(i).getCapacity() / 1073741824).append(" GB\",");
                    RAM_INFORMATION.append("\"type\": \"").append(physicalMemoryList.get(i).getMemoryType()).append("\"");
                    RAM_INFORMATION.append("},");
                }

                RAM_INFORMATION.append("{");
                RAM_INFORMATION.append("\"bank\": \"").append(physicalMemoryList.get(physicalMemoryList.size() - 1).getBankLabel()).append("\",");
                RAM_INFORMATION.append("\"capacity\": \"").append(physicalMemoryList.get(physicalMemoryList.size() - 1).getCapacity() / 1073741824).append(" GB\",");
                RAM_INFORMATION.append("\"type\": \"").append(physicalMemoryList.get(physicalMemoryList.size() - 1).getMemoryType()).append("\"");
                RAM_INFORMATION.append("}");

            } else {

                RAM_INFORMATION.append("{");
                RAM_INFORMATION.append("\"bank\": \"").append(physicalMemoryList.get(0).getBankLabel()).append("\",");
                RAM_INFORMATION.append("\"capacity\": \"").append(physicalMemoryList.get(0).getCapacity() / 1073741824).append(" GB\",");
                RAM_INFORMATION.append("\"type\": \"").append(physicalMemoryList.get(0).getMemoryType()).append("\"");
                RAM_INFORMATION.append("}");

            }

            RAM_INFORMATION.append("]");

        }

        return RAM_INFORMATION.toString();
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

    @Override
    public String toString() {
        return "\"" + getName() + "\": {" + getValues() + "}";
    }
}
