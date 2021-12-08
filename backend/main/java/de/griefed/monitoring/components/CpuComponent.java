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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;

import javax.mail.MessagingException;

/**
 * Class responsible for retrieving all interesting values about the CPU.
 * @author Griefed
 */
@Service
public class CpuComponent implements InformationModel {

    private final SystemInfo SYSTEM_INFO = new SystemInfo();
    private final CentralProcessor CPU = SYSTEM_INFO.getHardware().getProcessor();
    private final MailNotification MAIL_NOTIFICATION;
    private final ApplicationProperties PROPERTIES;
    private final HostComponent HOST_COMPONENT;

    private String cpuInformation;
    private String model;
    private String x64;
    private int processes;
    private int physical_cores;
    private int logical_cores;

    /**
     * Constructor responsible for DI.
     * @author Griefed
     * @param injectedApplicationProperties Instance of {@link ApplicationProperties}.
     * @param injectedHostComponent Instance of {@link HostComponent}.
     * @param injectedMailNotification Instance of {@link MailNotification}.
     */
    @Autowired
    public CpuComponent(MailNotification injectedMailNotification, ApplicationProperties injectedApplicationProperties, HostComponent injectedHostComponent) {
        this.PROPERTIES = injectedApplicationProperties;
        this.MAIL_NOTIFICATION = injectedMailNotification;
        this.HOST_COMPONENT = injectedHostComponent;
        updateValues();
    }

    /**
     * If the number of processes exceeds <code>de.griefed.monitoring.schedule.email.notification.cpu.processes</code>.
     * @author Griefed
     * @throws MessagingException Exception thrown if a failure occurs when sending the email.
     */
    @Scheduled(cron = "${de.griefed.monitoring.schedule.email.notification.cpu}")
    @Override
    public void sendNotification() throws MessagingException {

        updateValues();
        setValues();

        if (processes >= Integer.parseInt(PROPERTIES.getProperty("de.griefed.monitoring.schedule.email.notification.cpu.processes", "500"))) {
            MAIL_NOTIFICATION.sendMailNotification(
                    "Processes on " + HOST_COMPONENT.getHostName() + " critical!",
                    "The number of processes on this host has reached " + processes + ". Check this system immediately!"
            );
        }
    }

    /**
     * Set information from previously gathered information
     * @author Griefed
     */
    @Override
    public void setValues() {
        if (model == null || x64 == null || processes == 0 || physical_cores == 0 || logical_cores == 0) {
            updateValues();
        }

        this.cpuInformation = "\"model\": \"" + model + "\"," +
                "\"x64\": \"" + x64 + "\"," +
                "\"processes\": " + processes + "," +
                "\"physical_cores\": " + physical_cores + "," +
                "\"logical_cores\": " + logical_cores;
    }

    /**
     * Update cpu information.
     * @author Griefed
     */
    @Override
    public void updateValues() {

        this.model = CPU.getProcessorIdentifier().getName();
        this.x64 = String.valueOf(SYSTEM_INFO.getHardware().getProcessor().getProcessorIdentifier().isCpu64bit());
        this.processes = SYSTEM_INFO.getOperatingSystem().getProcesses().size();
        this.physical_cores = SYSTEM_INFO.getOperatingSystem().getProcesses().size();
        this.logical_cores = CPU.getLogicalProcessorCount();

        setValues();
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
    public String getValues() {
        if (cpuInformation == null) {
            setValues();
        }

        return cpuInformation;
    }

    @Override
    public String toString() {
        return "\"" + getName() + "\": {" + getValues() + "}";
    }

}
