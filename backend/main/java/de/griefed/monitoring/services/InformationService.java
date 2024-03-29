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
import de.griefed.monitoring.utilities.MailNotification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.mail.MessagingException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.time.Duration;

/**
 * Class responsible for collecting information from all components and building a JSON string with them.
 * @author Griefed
 */
@Service
public class InformationService {

    private static final Logger LOG = LogManager.getLogger(InformationService.class);

    private final CpuComponent CPU_COMPONENT;
    private final DiskComponent DISK_COMPONENT;
    private final HostComponent HOST_COMPONENT;
    private final OsComponent OS_COMPONENT;
    private final RamComponent RAM_COMPONENT;
    private final ApplicationProperties PROPERTIES;
    private final RestTemplate REST_TEMPLATE;
    private final MailNotification MAIL_NOTIFICATION;
    private final String OK = "{\"status\": " + 0 + ",\"message\": \"Everything in order.\",";
    private final String AGENT_OK = "{\"agent\": \"%s\",\"dialog\": false,";
    private final String AGENT_DOWN = "{\"status\": " + 1 + ",\"message\": \"Host down or unreachable.\",\"agent\": \"%s\"}";
    private final String AGENT_UNREACHABLE = "{\"status\": " + 2 + ",\"message\": \"Host up, but agent not reachable.\",\"agent\": \"%s\"}";

    private String agentInformation = "";
    private String hostInformation = "";

    /**
     * Constructor responsible for DI.
     * @author Griefed
     * @param injectedCpuComponent Instance of {@link CpuComponent}.
     * @param injectedDiskComponent Instance of {@link DiskComponent}.
     * @param injectedHostComponent Instance of {@link HostComponent}.
     * @param injectedOsComponent Instance of {@link OsComponent}.
     * @param injectedRamComponent Instance of {@link RamComponent}.
     * @param injectedApplicationProperties Instance of {@link ApplicationProperties}.
     * @param injectedMailNotification Instance of {@link MailNotification}.
     */
    @Autowired
    public InformationService(CpuComponent injectedCpuComponent, DiskComponent injectedDiskComponent, HostComponent injectedHostComponent,
                              OsComponent injectedOsComponent, RamComponent injectedRamComponent, ApplicationProperties injectedApplicationProperties,
                              MailNotification injectedMailNotification
    ) {
        this.CPU_COMPONENT = injectedCpuComponent;
        this.DISK_COMPONENT = injectedDiskComponent;
        this.HOST_COMPONENT = injectedHostComponent;
        this.OS_COMPONENT = injectedOsComponent;
        this.RAM_COMPONENT = injectedRamComponent;
        this.PROPERTIES = injectedApplicationProperties;
        this.REST_TEMPLATE = new RestTemplateBuilder()
                .setConnectTimeout(Duration.ofSeconds(PROPERTIES.getTimeoutConnect()))
                .setReadTimeout(Duration.ofSeconds(PROPERTIES.getTimeoutRead()))
                .build();
        this.MAIL_NOTIFICATION = injectedMailNotification;
    }

    /**
     * Retrieve all information about the host.
     * @author Griefed
     */
    public void setHostInformation() {
        this.hostInformation = OK +
                HOST_COMPONENT.toString() + "," +
                OS_COMPONENT.toString() + "," +
                CPU_COMPONENT.toString() + "," +
                DISK_COMPONENT.toString() + "," +
                RAM_COMPONENT.toString() + "}";
    }

    /**
     * Update host information.
     * @author Griefed
     */
    @Scheduled(cron = "${de.griefed.monitoring.schedule.update}")
    private void updateHostInformation() {
        HOST_COMPONENT.updateValues();
        OS_COMPONENT.updateValues();
        CPU_COMPONENT.updateValues();
        DISK_COMPONENT.updateValues();
        RAM_COMPONENT.updateValues();

        setHostInformation();
    }

    /**
     * Retrieve all information about the host.
     * @author Griefed
     * @return String in JSON format. Returns all information about the host.
     */
    public String retrieveHostInformation() {

        if (hostInformation == null || hostInformation.length() == 0) {
            updateHostInformation();
        }

        return hostInformation;
    }

    /**
     * Retrieve all information about the configured agent(s) and stores it in memory for retrieval by {@link #retrieveAgentsInformation()}.
     * @author Griefed
     */
    public void setAgentsInformation() {
        StringBuilder stringBuilder = new StringBuilder();

        // If agent-configuration is default, do not retrieve anything.
        if (PROPERTIES.getAgents().get(0).split(",")[0].equals("127.0.0.1") && PROPERTIES.getAgents().size() == 1) {

            LOG.warn("WARNING! Agents are not configured! Not retrieving information.");

            this.agentInformation = "{\"status\": " + 1 + ",\"message\": \"Agents are not configured! Not retrieving information.\"}";

        } else {

            stringBuilder.append("{\"agents").append("\": [");

            // Retrieve all information for all agents if more than one is configured
            if (PROPERTIES.getAgents().size() > 1) {



                stringBuilder.append(getResponse(PROPERTIES.getAgents().get(0).split(",")[0])).append(",");

                for (int i = 1; i < PROPERTIES.getAgents().size() - 1; i++) {

                    stringBuilder.append(getResponse(PROPERTIES.getAgents().get(i).split(",")[0])).append(",");

                }

                stringBuilder.append(getResponse(PROPERTIES.getAgents().get(PROPERTIES.getAgents().size() - 1).split(",")[0]));



                // Retrieve information for agent if only one is configured
            } else {

                stringBuilder.append(getResponse(PROPERTIES.getAgents().get(0).split(",")[0]));

            }

            stringBuilder.append("]}");

            this.agentInformation = stringBuilder.toString();

        }

        LOG.info("Retrieved information.");

    }

    /**
     * Retrieve agents information.
     * @author Griefed
     * @return String in JSON format. Returns information about the configured agent(s).
     */
    public String retrieveAgentsInformation() {

        if (agentInformation == null || agentInformation.length() == 0) {
            setAgentsInformation();
        }

        return agentInformation;
    }

    /**
     * Get information from an agent. If the HttpStatus is OK, the response is returned. If it is not, status 1 is returned,
     * indicating that the agent has problems.
     * @author Griefed
     * @param agent The agent to query.
     * @return String in JSON format. Returns the information gathered from the agent.
     */
    private String getResponse(String agent) {
        // TODO: Implement token passing
        ResponseEntity<String> response;
        InetAddress address;

        try {

            String ping = agent.replace("http://","").replace("https://","");

            if (ping.contains(":")) {
                ping = ping.replace(ping.substring(ping.lastIndexOf(":")), "");
            }

            LOG.info("Ping address: " + ping);

            address = InetAddress.getByName(ping);

        } catch (UnknownHostException ex) {

            LOG.error("Host " + agent + " unreachable or down.", ex);
            return String.format(AGENT_DOWN, agent);

        }

        boolean reachable = false;

        if (address != null) {

            try {

                if (address.isReachable(PROPERTIES.getTimeoutConnect() * 1000)) {
                    reachable = true;
                }

            } catch (IOException ignored) {}

            if (!reachable) {

                for (int port : PROPERTIES.getPorts()) {

                    try (Socket soc = new Socket()) {

                        soc.connect(new InetSocketAddress(address, port), 1000);
                        reachable = true;

                    } catch (IOException ignored) {}

                }
            }

        } else {

            LOG.error("Host " + agent + " unreachable or down.");
            sendNotification(agent,1);
            return String.format(AGENT_DOWN, agent);

        }

        if (reachable) {

            try {

                LOG.info(String.format("Retrieving information for %s", agent));

                response = REST_TEMPLATE.getForEntity(agent + "/api/v1/agent", String.class);

                if (response.getStatusCode() == HttpStatus.OK) {

                    return String.format(AGENT_OK, agent) + response.getBody().substring(1);

                } else {

                    LOG.error("Host " + agent + " reachable, but agent not.");
                    sendNotification(agent,0);
                    return String.format(AGENT_UNREACHABLE, agent);

                }

            } catch (Exception ex) {

                LOG.error("Host " + agent + " reachable, but agent not.");
                sendNotification(agent,0);
                return String.format(AGENT_UNREACHABLE, agent);
            }

        } else {

            LOG.error("Host " + agent + " unreachable or down.");
            sendNotification(agent,1);
            return String.format(AGENT_DOWN, agent);

        }


    }

    /**
     * Sends a notification,
     * @author Griefed
     * @param agent String. The agent to mention in the notifications body.
     * @param status Integer. The statuscode of the agent.
     */
    private void sendNotification(String agent, int status) {

        try {

            switch (status) {
                // Host up, but agent down
                case 0:
                    MAIL_NOTIFICATION.sendMailNotification(
                            "WARNING! Agent unreachable!",
                            "The host " + agent + " is reachable, but the agent is not. Is an agent setup or down?"
                    );
                    break;

                // Host down
                case 1:
                    MAIL_NOTIFICATION.sendMailNotification(
                            "CRITICAL! Host down!",
                            "The host " + agent + " is unreachable or down!"
                    );
                    break;

                // you what mate?
                default:
                    LOG.debug("Unknown status: " + status);
                    break;
            }

        } catch (MessagingException ex) {

            LOG.error("Error sending notification for agent " + agent, ex);

        }

    }
}
