/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */
/*

 *              weupnp - Trivial upnp java library

 *
 * Copyright (C) 2008 Alessandro Bahgat Shehata, Daniele Castagna
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, FΩifth Floor, Boston, MA  02110-1301  USA
 *
 * Alessandro Bahgat Shehata - ale dot bahgat at gmail dot com
 * Daniele Castagna - daniele dot castagna at gmail dot com
 *
 */

/*
 * refer to miniupnpc-1.0-RC8
 */
package hu.openig.tools;

import hu.openig.net.UPnPGatewayDevice;
import hu.openig.net.UPnPGatewayDiscover;
import hu.openig.net.UPnPPortMappingEntry;

import java.net.InetAddress;
import java.text.DateFormat;
import java.util.Date;
import java.util.Map;

/**
 * This class contains a trivial main method that can be used to test whether
 * weupnp is able to manipulate port mappings on a IGD (Internet Gateway
 * Device) on the same network.
 *
 * @author Alessandro Bahgat Shehata
 */
public final class UPnPTestMain {
    /** Test program. */
    private UPnPTestMain() { }
    /** Test port. */
    private static final int SAMPLE_PORT = 6991;
    /** Keepalive mapping. */
    private static final short WAIT_TIME = 10;
    /** List all mappings? */
    private static final boolean LISTALLMAPPINGS = false;
    /**
     * Program.
     * @param args no arguments
     * @throws Exception on error
     */
    public static void main(String[] args) throws Exception {

        addLogline("Starting weupnp");

        UPnPGatewayDiscover gatewayDiscover = new UPnPGatewayDiscover();
        addLogline("Looking for Gateway Devices...");

        Map<InetAddress, UPnPGatewayDevice> gateways = gatewayDiscover.discover();

        if (gateways.isEmpty()) {
            addLogline("No gateways found");
            addLogline("Stopping weupnp");
            return;
        }
        addLogline(gateways.size() + " gateway(s) found\n");

        int counter = 0;
        for (UPnPGatewayDevice gw: gateways.values()) {
            counter++;
            addLogline("Listing gateway details of device #" + counter
                    + "\n\tFriendly name: " + gw.getFriendlyName()
                    + "\n\tPresentation URL: " + gw.getPresentationURL()
                    + "\n\tModel name: " + gw.getModelName()
                    + "\n\tModel number: " + gw.getModelNumber()
                    + "\n\tLocal interface address: " + gw.getLocalAddress().getHostAddress() + "\n");
        }

        // choose the first active gateway for the tests
        UPnPGatewayDevice activeGW = gatewayDiscover.getValidGateway();

        if (null != activeGW) {
            addLogline("Using gateway:" + activeGW.getFriendlyName());
        } else {
            addLogline("No active gateway device found");
            addLogline("Stopping weupnp");
            return;
        }

        // testing PortMappingNumberOfEntries
        Integer portMapCount = activeGW.getPortMappingNumberOfEntries();
        addLogline("GetPortMappingNumberOfEntries=" + (portMapCount != null ? portMapCount.toString() : "(unsupported)"));

        // testing getGenericPortMappingEntry
        UPnPPortMappingEntry portMapping0 = new UPnPPortMappingEntry();
        if (LISTALLMAPPINGS) {
            int pmCount = 0;
            do {
                if (activeGW.getGenericPortMappingEntry(pmCount, portMapping0)) {

                    addLogline("Portmapping #" + pmCount
                            + " successfully retrieved ("
                            + portMapping0.getPortMappingDescription()
                            + ":" + portMapping0.getExternalPort() + ")");
                } else {
                    addLogline("Portmapping #" + pmCount + " retrival failed");

                    break;
                }
                pmCount++;
            } while (portMapping0 != null);
        } else {
            if (activeGW.getGenericPortMappingEntry(0, portMapping0)) {
                addLogline("Portmapping #0 successfully retrieved ("
                        + portMapping0.getPortMappingDescription()
                        + ":" + portMapping0.getExternalPort() + ")");
            } else {
                addLogline("Portmapping #0 retrival failed");
            }
        }

        InetAddress localAddress = activeGW.getLocalAddress();
        addLogline("Using local address: " + localAddress.getHostAddress());
        String externalIPAddress = activeGW.getExternalIPAddress();
        addLogline("External address: " + externalIPAddress);

        addLogline("Querying device to see if a port mapping already exists for port " + SAMPLE_PORT);
        UPnPPortMappingEntry portMapping = new UPnPPortMappingEntry();

        if (activeGW.getSpecificPortMappingEntry(SAMPLE_PORT, "TCP", portMapping)) {
            addLogline("Port " + SAMPLE_PORT + " is already mapped. Aborting test.");
            return;
        }

        addLogline("Mapping free. Sending port mapping request for port " + SAMPLE_PORT);

        // test static lease duration mapping
        if (activeGW.addPortMapping(SAMPLE_PORT, SAMPLE_PORT, localAddress.getHostAddress(), "TCP", "test")) {
            addLogline("Mapping SUCCESSFUL. Waiting " + WAIT_TIME + " seconds before removing mapping...");
            Thread.sleep(1000 * WAIT_TIME);

            if (activeGW.deletePortMapping(SAMPLE_PORT, "TCP")) {
                addLogline("Port mapping removed, test SUCCESSFUL");
            } else {
                addLogline("Port mapping removal FAILED");
            }
        }

        addLogline("Stopping weupnp");
    }

    /**
     * Prints a timestamped log line.
     * @param line the line
     */
    static void addLogline(String line) {

        String timeStamp = DateFormat.getTimeInstance().format(new Date());
        String logline = timeStamp + ": " + line + "\n";
        System.out.print(logline);
    }

}
