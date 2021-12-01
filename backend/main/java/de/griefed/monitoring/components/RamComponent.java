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
import oshi.hardware.PhysicalMemory;

import java.util.List;

@Service
public class RamComponent implements InformationModel {

    private final StringBuilder RAM_INFORMATION = new StringBuilder();
    private final SystemInfo SYSTEM_INFO = new SystemInfo();

    @Autowired
    public RamComponent() {

    }

    @Override
    public String getName() {
        return "memory";
    }

    @Override
    public String getValue() {
        if (RAM_INFORMATION.length() > 0) {
            RAM_INFORMATION.delete(0, RAM_INFORMATION.length());
        }

        RAM_INFORMATION.append("\"total\": \"").append(SYSTEM_INFO.getHardware().getMemory().getTotal() / 1073741824).append(" GB\",");
        RAM_INFORMATION.append("\"available\": \"").append(SYSTEM_INFO.getHardware().getMemory().getAvailable() / 1073741824).append(" GB\",");

        List<PhysicalMemory> physicalMemoryList = SYSTEM_INFO.getHardware().getMemory().getPhysicalMemory();

        RAM_INFORMATION.append("\"physical_memory\": [");

        if (physicalMemoryList.size() > 1) {

            RAM_INFORMATION.append("{");
            RAM_INFORMATION.append("\"bank\": \"").append(physicalMemoryList.get(0).getBankLabel()).append("\",");
            RAM_INFORMATION.append("\"capacity\": \"").append(physicalMemoryList.get(0).getCapacity() / 1073741824).append(" GB\",");
            RAM_INFORMATION.append("\"type\": \"").append(physicalMemoryList.get(0).getMemoryType()).append("\"");
            RAM_INFORMATION.append("},");

            for (int i = 1; i < physicalMemoryList.size() -1; i++) {
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
            RAM_INFORMATION.append("},");

        }

        RAM_INFORMATION.append("]");

        return RAM_INFORMATION.toString();
    }

    @Override
    public String toString() {
        return "\"" + getName() + "\": {" + getValue() + "}";
    }
}
