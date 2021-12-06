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
package de.griefed.monitoring;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * Class responsible for handling our properties. Loaded at boot in {@link Main}.
 * @author Griefed
 */
@Component
public class ApplicationProperties extends Properties {

    private static final Logger LOG = LogManager.getLogger(ApplicationProperties.class);

    /**
     * Whether this instance is a monitor or an agent.
     */
    @Value("${de.griefed.monitoring.agent}")
    private boolean agent = false;

    /**
     * The agents, with their access tokens, to gather information form. Format is <code>agent_ip1,access_token1;agent_ip2,access_token2;agent_ip3,access_token3</code> etc..
     */
    @Value("${de.griefed.monitoring.agents}")
    private String agents = "127.0.0.1,123456789";

    /**
     * Seconds to wait until a connection timeout is triggered for getting information from agents.
     */
    @Value("${de.griefed.monitoring.timeout.connect}")
    private int timeoutConnect = 5;

    /**
     * Seconds to wait until a read timeout is triggered for getting information from agents.
     */
    @Value("${de.griefed.monitoring.timeout.read}")
    private int timeoutRead = 5;

    /**
     * Constructor for our properties. Sets a couple of default values for use in Monitoring.
     * @author Griefed
     */
    @Autowired
    public ApplicationProperties() {

        try (InputStream inputStream = new FileInputStream("application.properties")) {
            new Properties();
            load(inputStream);
        } catch (
                IOException ex) {
            LOG.error("Couldn't read properties file.", ex);
        }
    }

    /**
     * Getter for whether this instance is a monitor or an agent.
     * @author Griefed
     * @return boolean. <code>true</code> = agent. <code>false</code> = monitor.
     */
    public boolean isAgent() {
        return agent;
    }

    /**
     * Getter for a list of agents to gather information from.
     * @author Griefed
     * @return List String. Each entry is a combination of ip and access token in the format <code>agent_ip1,access_token1</code>.
     */
    public List<String> getAgents() {

        if (!isAgent()) {

            if (
                    new ArrayList<String>(Arrays.asList(agents.split(";"))).size() == 1 &&
                    new ArrayList<String>(Arrays.asList(agents.split(";"))).get(0).equals("127.0.0.1,123456789")
            ) {

                LOG.warn("WARNING! Instance is configured as host, but agents aren't configured correctly!");
                LOG.warn("Check your property de.griefed.monitoring.agents in your application.properties!");

            }

        }
        return new ArrayList<String>(Arrays.asList(agents.split(";")));
    }

    /**
     * Getter for the number of seconds to wait until a connection timeout is triggered for getting information from agents.
     * @author Griefed
     * @return Integer. Returns the number of seconds as an int.
     */
    public int getTimeoutConnect() {
        return timeoutConnect;
    }

    /**
     * Getter for the number of seconds to wait until a read timeout is triggered for getting information from agents.
     * @author Griefed
     * @return Integer. Returns the number of seconds as an int.
     */
    public int getTimeoutRead() {
        return timeoutRead;
    }
}
