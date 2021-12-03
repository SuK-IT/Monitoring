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
package de.griefed.monitoring.services;

import de.griefed.monitoring.ApplicationProperties;
import de.griefed.monitoring.components.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class InformationService {

    private static final Logger LOG = LogManager.getLogger(InformationService.class);

    private final CpuComponent CPU_COMPONENT;
    private final DiskComponent DISK_COMPONENT;
    private final HostComponent HOST_COMPONENT;
    private final OsComponent OS_COMPONENT;
    private final RamComponent RAM_COMPONENT;
    private final ApplicationProperties PROPERTIES;
    private final StringBuilder INFORMATION = new StringBuilder();
    private final StringBuilder AGENTS = new StringBuilder();
    private final StringBuilder AGENTS_INFORMATION = new StringBuilder();
    private final RestTemplate REST_TEMPLATE;

    @Autowired
    public InformationService(CpuComponent injectedCpuComponent, DiskComponent injectedDiskComponent, HostComponent injectedHostComponent, OsComponent injectedOsComponent, RamComponent injectedRamComponent, ApplicationProperties injectedApplicationProperties) {
        this.CPU_COMPONENT = injectedCpuComponent;
        this.DISK_COMPONENT = injectedDiskComponent;
        this.HOST_COMPONENT = injectedHostComponent;
        this.OS_COMPONENT = injectedOsComponent;
        this.RAM_COMPONENT = injectedRamComponent;
        this.PROPERTIES = injectedApplicationProperties;
        this.REST_TEMPLATE = new RestTemplateBuilder().build();

        if (PROPERTIES.getAgents().size() > 1) {

            AGENTS.append(PROPERTIES.getAgents().get(0).split(",")[0]).append(",");
            for (int i = 1; i < PROPERTIES.getAgents().size() - 1; i++) {
                AGENTS.append(PROPERTIES.getAgents().get(i).split(",")[0]).append(",");
            }
            AGENTS.append(PROPERTIES.getAgents().get(PROPERTIES.getAgents().size() - 1).split(",")[0]);

        } else {

            AGENTS.append(PROPERTIES.getAgents().get(0).split(",")[0]);

        }

    }

    public String retrieveHostInformation() {
        if (INFORMATION.length() > 0) {
            INFORMATION.delete(0, INFORMATION.length());
        }

        INFORMATION.append("{");

        INFORMATION.append(HOST_COMPONENT.toString()).append(",");
        INFORMATION.append(OS_COMPONENT.toString()).append(",");
        INFORMATION.append(CPU_COMPONENT.toString()).append(",");
        INFORMATION.append(DISK_COMPONENT.toString()).append(",");
        INFORMATION.append(RAM_COMPONENT.toString());

        INFORMATION.append("}");

        return INFORMATION.toString();
    }

    public String retrieveAgentsInformation() {
        if (AGENTS_INFORMATION.length() > 0) {
            AGENTS_INFORMATION.delete(0, AGENTS_INFORMATION.length());
        }

        if (PROPERTIES.getAgents().get(0).split(",")[0].equals("127.0.0.1") && PROPERTIES.getAgents().size() == 1) {

            LOG.warn("WARNING! Agents are not configured! Not retrieving information.");

            return "{\"message\": \"Agents are not configured! Not retrieving information.\"}";

        } else {

            LOG.info(String.format("Retrieving information for %s", AGENTS));

            if (PROPERTIES.getAgents().size() > 1) {


                AGENTS_INFORMATION.append(REST_TEMPLATE.getForObject(
                        PROPERTIES.getAgents().get(0).split(",")[0] + "/api/v1/agent",
                        String.class)
                ).append(",");

                for (int i = 1; i < PROPERTIES.getAgents().size() - 1; i++) {


                    AGENTS_INFORMATION.append(REST_TEMPLATE.getForObject(
                            PROPERTIES.getAgents().get(i).split(",")[0] + "/api/v1/agent",
                            String.class)
                    ).append(",");

                }

                AGENTS_INFORMATION.append(REST_TEMPLATE.getForObject(
                        PROPERTIES.getAgents().get(PROPERTIES.getAgents().size() - 1).split(",")[0] + "/api/v1/agent",
                        String.class)
                );

            } else {

                AGENTS_INFORMATION.append(REST_TEMPLATE.getForObject(
                        PROPERTIES.getAgents().get(0).split(",")[0] + "/api/v1/agent",
                        String.class)
                );

            }

            return AGENTS_INFORMATION.toString();
        }
    }
}
