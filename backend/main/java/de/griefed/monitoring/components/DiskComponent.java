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
import oshi.software.os.OSFileStore;

import java.util.List;

/**
 * Class responsible for retrieving all interesting values about the disk drives.
 * @author Griefed
 */
@Service
public class DiskComponent implements InformationModel {

    private final StringBuilder DISK_INFORMATION = new StringBuilder();
    private final SystemInfo SYSTEM_INFO = new SystemInfo();
    private final List<OSFileStore> DISK_STORES = SYSTEM_INFO.getOperatingSystem().getFileSystem().getFileStores(true);

    /**
     * Cosntructor responsible for DI.
     * @author Griefed
     */
    @Autowired
    public DiskComponent() {

    }

    /**
     * Getter for the name of this component.
     * @author Griefed
     * @return String. Returns the name of the component.
     */
    @Override
    public String getName() {
        return "disks";
    }

    /**
     * Getter for the information about the disk drives. Gathers information, for each disk, about the name, total space
     * and free space left.
     * @author Griefed
     * @return String. Information about the disk drives in JSON format.
     */
    @Override
    public String getValue() {
        if (DISK_INFORMATION.length() > 0) {
            DISK_INFORMATION.delete(0, DISK_INFORMATION.length());
        }

        if (DISK_STORES.size() > 1) {

            DISK_INFORMATION.append("{");
            DISK_INFORMATION.append("\"name\": \"").append(DISK_STORES.get(0).getName()).append(" ").append(DISK_STORES.get(0).getLabel()).append("\",");
            DISK_INFORMATION.append("\"size\": \"").append(DISK_STORES.get(0).getTotalSpace() / 1073741824).append(" GB\",");
            DISK_INFORMATION.append("\"free\": \"").append(DISK_STORES.get(0).getFreeSpace() / 1073741824).append(" GB\"");
            DISK_INFORMATION.append("},");

            for (int i = 1; i < DISK_STORES.size() -1; i++) {
                DISK_INFORMATION.append("{");
                DISK_INFORMATION.append("\"name\": \"").append(DISK_STORES.get(i).getName()).append(" ").append(DISK_STORES.get(i).getLabel()).append("\",");
                DISK_INFORMATION.append("\"size\": \"").append(DISK_STORES.get(i).getTotalSpace() / 1073741824).append(" GB\",");
                DISK_INFORMATION.append("\"free\": \"").append(DISK_STORES.get(i).getFreeSpace() / 1073741824).append(" GB\"");
                DISK_INFORMATION.append("},");
            }

            DISK_INFORMATION.append("{");
            DISK_INFORMATION.append("\"name\": \"").append(DISK_STORES.get(DISK_STORES.size() - 1).getName()).append(" ").append(DISK_STORES.get(DISK_STORES.size() - 1).getLabel()).append("\",");
            DISK_INFORMATION.append("\"size\": \"").append(DISK_STORES.get(DISK_STORES.size() - 1).getTotalSpace() / 1073741824).append(" GB\",");
            DISK_INFORMATION.append("\"free\": \"").append(DISK_STORES.get(DISK_STORES.size() - 1).getFreeSpace() / 1073741824).append(" GB\"");
            DISK_INFORMATION.append("}");

        } else {

            DISK_INFORMATION.append("{");
            DISK_INFORMATION.append("\"name\": \"").append(DISK_STORES.get(0).getName()).append(" ").append(DISK_STORES.get(0).getLabel()).append("\",");
            DISK_INFORMATION.append("\"size\": \"").append(DISK_STORES.get(0).getTotalSpace() / 1073741824).append(" GB\",");
            DISK_INFORMATION.append("\"free\": \"").append(DISK_STORES.get(0).getFreeSpace() / 1073741824).append(" GB\"");
            DISK_INFORMATION.append("},");

        }

        return DISK_INFORMATION.toString();
    }

    @Override
    public String toString() {
        return "\"" + getName() + "\": [" + getValue() + "]";
    }
}
