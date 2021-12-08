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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import oshi.SystemInfo;
import oshi.software.os.OSFileStore;

import javax.mail.MessagingException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Class responsible for retrieving all interesting values about the disk drives.
 * @author Griefed
 */
@Service
public class DiskComponent implements InformationModel {

    private static final Logger LOG = LogManager.getLogger(DiskComponent.class);

    private final SystemInfo SYSTEM_INFO = new SystemInfo();
    private final List<OSFileStore> DISK_STORES = SYSTEM_INFO.getOperatingSystem().getFileSystem().getFileStores(true);
    private final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.00");
    private final ApplicationProperties PROPERTIES;
    private final HostComponent HOST_COMPONENT;
    private final MailNotification MAIL_NOTIFICATION;

    private List<HashMap<String, String>> diskInformationList = new ArrayList<>(100);
    private String diskInformation;

    /**
     * Constructor responsible for DI.
     * @author Griefed
     * @param injectedMailNotification Instance of {@link MailNotification}.
     * @param injectedHostComponent Instance of {@link HostComponent}.
     * @param injectedApplicationProperties Instance of {@link ApplicationProperties}.
     */
    @Autowired
    public DiskComponent(ApplicationProperties injectedApplicationProperties, MailNotification injectedMailNotification, HostComponent injectedHostComponent) {
        this.PROPERTIES = injectedApplicationProperties;
        this.MAIL_NOTIFICATION = injectedMailNotification;
        this.HOST_COMPONENT = injectedHostComponent;
        updateValues();
    }

    /**
     * If the usage of any disk exceeds <code>de.griefed.monitoring.schedule.email.notification.disk.usage</code>.
     * @author Griefed
     * @throws MessagingException Exception thrown if a failure occurs when sending the email.
     */
    @Scheduled(cron = "${de.griefed.monitoring.schedule.email.notification.disk}")
    @Override
    public void sendNotification() throws MessagingException {

        updateValues();
        setValues();

        List<HashMap<String, String>> disks = diskInformationList;

        for (HashMap<String, String> disk : disks) {

            if (Float.parseFloat(disk.get("used").replace(" %","").replace(",",".")) >= Float.parseFloat(PROPERTIES.getProperty("de.griefed.monitoring.schedule.email.notification.disk.usage"))) {
                MAIL_NOTIFICATION.sendMailNotification(
                        "Disk on " + HOST_COMPONENT.getHostName() + " at critical capacity!",
                        "The usage for disk " + disk.get("name") + " has reached critical usage levels of " + disk.get("used") + ".\n" +
                        "Free space remaining: " + disk.get("free") + "."
                );
            }
        }

        disks.clear();
    }

    /**
     * Set information from previously gathered information
     * @author Griefed
     */
    @Override
    public void setValues() {
        if (diskInformationList.isEmpty()) {
            updateValues();
        }

        StringBuilder stringBuilder = new StringBuilder(10000);

        if (diskInformationList.size() > 1) {

            stringBuilder.append("{");
            stringBuilder.append("\"name\": \"").append(diskInformationList.get(0).get("name")).append("\",");
            stringBuilder.append("\"size\": \"").append(diskInformationList.get(0).get("size")).append("\",");
            stringBuilder.append("\"free\": \"").append(diskInformationList.get(0).get("free")).append("\",");
            stringBuilder.append("\"used\": \"").append(diskInformationList.get(0).get("used")).append("\"");
            stringBuilder.append("},");

            for (int i = 1; i < DISK_STORES.size() -1; i++) {

                stringBuilder.append("{");
                stringBuilder.append("\"name\": \"").append(diskInformationList.get(i).get("name")).append("\",");
                stringBuilder.append("\"size\": \"").append(diskInformationList.get(i).get("size")).append("\",");
                stringBuilder.append("\"free\": \"").append(diskInformationList.get(i).get("free")).append("\",");
                stringBuilder.append("\"used\": \"").append(diskInformationList.get(i).get("used")).append("\"");
                stringBuilder.append("},");
            }

            stringBuilder.append("{");
            stringBuilder.append("\"name\": \"").append(diskInformationList.get(diskInformationList.size() - 1).get("name")).append("\",");
            stringBuilder.append("\"size\": \"").append(diskInformationList.get(diskInformationList.size() - 1).get("size")).append("\",");
            stringBuilder.append("\"free\": \"").append(diskInformationList.get(diskInformationList.size() - 1).get("free")).append("\",");
            stringBuilder.append("\"used\": \"").append(diskInformationList.get(diskInformationList.size() - 1).get("used")).append("\"");
            stringBuilder.append("}");

        } else {

            stringBuilder.append("{");
            stringBuilder.append("\"name\": \"").append(diskInformationList.get(0).get("name")).append("\",");
            stringBuilder.append("\"size\": \"").append(diskInformationList.get(0).get("size")).append("\",");
            stringBuilder.append("\"free\": \"").append(diskInformationList.get(0).get("free")).append("\",");
            stringBuilder.append("\"used\": \"").append(diskInformationList.get(0).get("used")).append("\"");
            stringBuilder.append("}");

        }

        this.diskInformation = stringBuilder.toString();
    }

    /**
     * Update disk information.
     * @author Griefed
     */
    @Override
    public void updateValues() {
        List<HashMap<String, String>> list = new ArrayList<>(1000);

        if (DISK_STORES.size() > 1) {

            list.add(
                    new HashMap<String, String>() {
                        {
                            put("name", DISK_STORES.get(0).getName() + " " + DISK_STORES.get(0).getLabel());
                            put("size", (DISK_STORES.get(0).getTotalSpace() / 1073741824) + " GB");
                            put("free", DECIMAL_FORMAT.format(DISK_STORES.get(0).getFreeSpace() / 1073741824F) + " GB");
                            put("used", DECIMAL_FORMAT.format(100F - ((100F / DISK_STORES.get(0).getTotalSpace()) * DISK_STORES.get(0).getFreeSpace())) + " %");
                        }
            });

            for (int i = 1; i < DISK_STORES.size() -1; i++) {

                int finalI = i;
                list.add(
                        new HashMap<String, String>() {
                            {
                                put("name", DISK_STORES.get(finalI).getName() + " " + DISK_STORES.get(finalI).getLabel());
                                put("size", (DISK_STORES.get(finalI).getTotalSpace() / 1073741824) + " GB");
                                put("free", DECIMAL_FORMAT.format(DISK_STORES.get(finalI).getFreeSpace() / 1073741824F) + " GB");
                                put("used", DECIMAL_FORMAT.format(100F - ((100F / DISK_STORES.get(finalI).getTotalSpace()) * DISK_STORES.get(finalI).getFreeSpace())) + " %");
                            }
                        });

            }

            list.add(
                    new HashMap<String, String>() {
                        {
                            put("name", DISK_STORES.get(DISK_STORES.size() - 1).getName() + " " + DISK_STORES.get(DISK_STORES.size() - 1).getLabel());
                            put("size", (DISK_STORES.get(DISK_STORES.size() - 1).getTotalSpace() / 1073741824) + " GB");
                            put("free", DECIMAL_FORMAT.format(DISK_STORES.get(DISK_STORES.size() - 1).getFreeSpace() / 1073741824F) + " GB");
                            put("used", DECIMAL_FORMAT.format(100F - ((100F / DISK_STORES.get(DISK_STORES.size() - 1).getTotalSpace()) * DISK_STORES.get(DISK_STORES.size() - 1).getFreeSpace())) + " %");
                        }
                    });


        } else {

            list.add(
                    new HashMap<String, String>() {
                        {
                            put("name", DISK_STORES.get(0).getName() + " " + DISK_STORES.get(0).getLabel());
                            put("size", (DISK_STORES.get(0).getTotalSpace() / 1073741824) + " GB");
                            put("free", DECIMAL_FORMAT.format(DISK_STORES.get(0).getFreeSpace() / 1073741824F) + " GB");
                            put("used", DECIMAL_FORMAT.format(100F - ((100F / DISK_STORES.get(0).getTotalSpace()) * DISK_STORES.get(0).getFreeSpace())) + " %");
                        }
                    });

        }

        this.diskInformationList = list;
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
    public String getValues() {
        if (diskInformation == null) {
            setValues();
        }

        return diskInformation;
    }

    @Override
    public String toString() {
        return "\"" + getName() + "\": [" + getValues() + "]";
    }
}
