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

/**
 * Class responsible for retrieving all interesting information about the OS.
 * @author Griefed
 */
@Service
public class OsComponent implements InformationModel {

    private final OperatingSystem OS_INFO = new SystemInfo().getOperatingSystem();

    private String osInformation;
    private String manufacturer;
    private String os;
    private String version;
    private String arch;
    private String uptime;

    /**
     * Constructor responisble for DI.
     * @author Griefed
     */
    @Autowired
    public OsComponent() {
        updateValues();
    }

    @Override
    public void sendNotification() {}

    /**
     * Set information from previously gathered information
     * @author Griefed
     */
    @Override
    public void setValues() {
        this.osInformation = "\"manufacturer\": \"" + manufacturer + "\"," +
                "\"os\": \"" + os + "\"," +
                "\"version\": \"" + version + "\"," +
                "\"arch\": \"" + arch + " bit\"," +
                "\"uptime\": \"" + uptime + "\"";
    }

    /**
     * Update os information.
     * @author Griefed
     */
    @Override
    public void updateValues() {
        this.manufacturer = OS_INFO.getManufacturer();
        this.os = OS_INFO.getFamily();
        this.version = OS_INFO.getVersionInfo().toString();
        this.arch = String.valueOf(OS_INFO.getBitness());
        this.uptime = OS_INFO.getSystemUptime() / 3600 + " h";

        setValues();
    }

    /**
     * Getter for the name of the component.
     * @author Griefed
     * @return String. Returns the name of the component.
     */
    @Override
    public String getName() {
        return "os";
    }

    /**
     * Getter for the information about the OS. Gathers information about the manufacturer, family, version, bitness and how long it's been up.
     * @author Griefed
     * @return String. Information about the OS in JSON format.
     */
    @Override
    public String getValues() {
        if (osInformation == null) {
            setValues();
        }

        return osInformation;
    }

    @Override
    public String toString() {
        return "\"" + getName() + "\": {" + getValues() + "}";
    }

}
