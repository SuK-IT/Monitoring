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
import oshi.software.os.OperatingSystem;

@Service
public class OsComponent implements InformationModel {

    private final StringBuilder OS_INFORMATION = new StringBuilder();
    private final OperatingSystem OS_INFO = new SystemInfo().getOperatingSystem();

    @Autowired
    public OsComponent() {

    }

    @Override
    public String getName() {
        return "os";
    }

    @Override
    public String getValue() {
        if (OS_INFORMATION.length() > 0) {
            OS_INFORMATION.delete(0, OS_INFORMATION.length());
        }

        OS_INFORMATION.append("\"manufacturer\": \"").append(OS_INFO.getManufacturer()).append("\",");
        OS_INFORMATION.append("\"os\": \"").append(OS_INFO.getFamily()).append("\",");
        OS_INFORMATION.append("\"version\": \"").append(OS_INFO.getVersionInfo()).append("\",");
        OS_INFORMATION.append("\"arch\": \"").append(OS_INFO.getBitness()).append(" bit\",");
        OS_INFORMATION.append("\"uptime\": \"").append(OS_INFO.getSystemUptime() / 3600).append(" h\"");

        return OS_INFORMATION.toString();
    }

    @Override
    public String toString() {
        return "\"" + getName() + "\": {" + getValue() + "}";
    }

}
