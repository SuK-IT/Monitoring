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
import oshi.hardware.CentralProcessor;

/**
 * Class responsible for retrieving all interesting values about the CPU.
 * @author Griefed
 */
@Service
public class CpuComponent implements InformationModel {

    private final StringBuilder CPU_INFORMATION = new StringBuilder();
    private final SystemInfo SYSTEM_INFO = new SystemInfo();
    private final CentralProcessor CPU = SYSTEM_INFO.getHardware().getProcessor();

    /**
     * Constructor responsible for DI.
     * @author Griefed
     */
    @Autowired
    public CpuComponent() {

    }

    /**
     * Getter for the name of this component.
     * @author Griefed
     * @return String. Returns the name of the component.
     */
    @Override
    public String getName() {
        return "cpu";
    }

    /**
     * Getter for the information about the cpu. Gathers information about the name, whether it's a 64bit cpu, the amount
     * of processes when queried, the number of physical cores and the number of logical cores.
     * @author Griefed
     * @return String. Information about the cpu in JSON format.
     */
    @Override
    public String getValue() {
        if (CPU_INFORMATION.length() > 0) {
            CPU_INFORMATION.delete(0, CPU_INFORMATION.length());
        }

        CPU_INFORMATION.append("\"model\": \"").append(CPU.getProcessorIdentifier().getName()).append("\",");
        CPU_INFORMATION.append("\"x64\": \"").append(SYSTEM_INFO.getHardware().getProcessor().getProcessorIdentifier().isCpu64bit()).append("\",");
        CPU_INFORMATION.append("\"processes\": ").append(SYSTEM_INFO.getOperatingSystem().getProcesses().size()).append(",");
        CPU_INFORMATION.append("\"physical_cores\": ").append(CPU.getPhysicalProcessorCount()).append(",");
        CPU_INFORMATION.append("\"logical_cores\": ").append(CPU.getLogicalProcessorCount());

        return CPU_INFORMATION.toString();
    }

    @Override
    public String toString() {
        return "\"" + getName() + "\": {" + getValue() + "}";
    }

}
