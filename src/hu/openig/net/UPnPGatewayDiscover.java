/*
 * Copyright 2008-2014, David Karnok 
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
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * Alessandro Bahgat Shehata - ale dot bahgat at gmail dot com
 * Daniele Castagna - daniele dot castagna at gmail dot com
 *
 */
package hu.openig.net;

import hu.openig.utils.Exceptions;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

/**
 * Handles the discovery of GatewayDevices, via the {@link hu.openig.net.UPnPGatewayDiscover#discover()} method.
 */
public class UPnPGatewayDiscover {

    /**
     * The SSDP port.
     */
    protected int ssdpPort = 1900;

    /**
     * The broadcast address to use when trying to contact UPnP devices.
     */
    protected String broadcastIP = "239.255.255.250";

    /**
     * The timeout to set for the initial broadcast request.
     */
    protected int broadcastTimeout = 3000;


    /**
     * A map of the GatewayDevices discovered so far.
     * The assumption is that a machine is connected to up to a Gateway Device
     * per InetAddress
     */
    private final Map<InetAddress, UPnPGatewayDevice> devices = new HashMap<>();

    /**
     *  Thread class for sending a search datagram and process the response.
     */
    private class SendDiscoveryThread extends Thread {
    	/** The local IP address. */
        InetAddress ip;
        /** The message. */
        String searchMessage;
        /**
         * Constructor.
         * @param localIP the local IP address
         * @param searchMessage the search message
         */
        SendDiscoveryThread(InetAddress localIP, String searchMessage) {
            this.ip = localIP;
            this.searchMessage = searchMessage;
        }

        @Override
        public void run() {

            try (DatagramSocket ssdp = new DatagramSocket(new InetSocketAddress(ip, 0))) {
                // Create socket bound to specified local address

                byte[] searchMessageBytes = searchMessage.getBytes();
                DatagramPacket ssdpDiscoverPacket = new DatagramPacket(searchMessageBytes, searchMessageBytes.length);
                ssdpDiscoverPacket.setAddress(InetAddress.getByName(broadcastIP));
                ssdpDiscoverPacket.setPort(ssdpPort);

                ssdp.send(ssdpDiscoverPacket);
                ssdp.setSoTimeout(broadcastTimeout);


                boolean waitingPacket = true;
                while (waitingPacket) {
                    DatagramPacket receivePacket = new DatagramPacket(new byte[1536], 1536);
                    try {
                        ssdp.receive(receivePacket);
                        byte[] receivedData = new byte[receivePacket.getLength()];
                        System.arraycopy(receivePacket.getData(), 0, receivedData, 0, receivePacket.getLength());

                        // Create GatewayDevice from response
                        UPnPGatewayDevice gateDev = parseMSearchReplay(receivedData);

                        gateDev.setLocalAddress(ip);
                        gateDev.loadDescription();

                        synchronized (devices) {
                            devices.put(ip, gateDev);
                        }
                    } catch (SocketTimeoutException ste) {
                        waitingPacket = false;
                    }
                }

            } catch (Throwable e) {
            	Exceptions.add(e);
            }
        }
    }

    /**
     * Discovers Gateway Devices on the network(s) the executing machine is
     * connected to.
     * <p/>
     * The host may be connected to different networks via different network
     * interfaces.
     * Assumes that each network interface has a different InetAddress and
     * returns a map associating every GatewayDevice (responding to a broadcast
     * discovery message) with the InetAddress it is connected to.
     *
     * @return a map containing a GatewayDevice per InetAddress
     * @throws IOException on error
     * @throws SAXException on error
     * @throws ParserConfigurationException on error
     */
    public Map<InetAddress, UPnPGatewayDevice> discover() throws IOException, SAXException, ParserConfigurationException {

        Collection<InetAddress> ips = getLocalInetAddresses(true, false, false);

        // ST parameter: Search Targets
        String[] searchTypes = {
                "urn:schemas-upnp-org:device:InternetGatewayDevice:1",
                "urn:schemas-upnp-org:service:WANIPConnection:1",
                "urn:schemas-upnp-org:service:WANPPPConnection:1"
        };

        for (String st : searchTypes) {

            String searchMessage = "M-SEARCH * HTTP/1.1\r\n"
                    + "HOST: " + broadcastIP + ":" + ssdpPort + "\r\n"
                    + "ST: " + st + "\r\n"
                    + "MAN: \"ssdp:discover\"\r\n"
                    + "MX: 2\r\n"    // seconds to delay response
                    + "\r\n";

            // perform search requests for multiple network adapters concurrently
            Collection<SendDiscoveryThread> threads = new ArrayList<>();
            for (InetAddress ip : ips) {
                SendDiscoveryThread thread = new SendDiscoveryThread(ip, searchMessage);
                threads.add(thread);
                thread.start();
            }

            // wait for all search threads to finish
            for (SendDiscoveryThread thread : threads) {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    // continue with next thread
                }
            }
            // If a search type found devices, don't try with different search type
            if (!devices.isEmpty()) {
                break;
            }

        } // loop SEARCHTYPES

        return devices;
    }

    /**
     * Parses the reply from UPnP devices.
     *
     * @param reply the raw bytes received as a reply
     * @return the representation of a GatewayDevice
     */
    private static UPnPGatewayDevice parseMSearchReplay(byte[] reply) {

    	UPnPGatewayDevice device = new UPnPGatewayDevice();

        String replyString = new String(reply);
        StringTokenizer st = new StringTokenizer(replyString, "\n");

        while (st.hasMoreTokens()) {
            String line = st.nextToken().trim();

            if (line.isEmpty()) {
                continue;
            }

            if (line.startsWith("HTTP/1.")) {
                continue;
            }

            String key = line.substring(0, line.indexOf(':'));
            String value = line.length() > key.length() + 1 ? line.substring(key.length() + 1) : null;

            key = key.trim();
            if (value != null) {
                value = value.trim();
            }

            if (key.compareToIgnoreCase("location") == 0) {
                device.setLocation(value);

            } else if (key.compareToIgnoreCase("st") == 0) {    // Search Target
                device.setSt(value);
            }
        }

        return device;
    }

    /**
     * Gets the first connected gateway.
     *
     * @return the first GatewayDevice which is connected to the network, or
     *         null if none present
     */
    public UPnPGatewayDevice getValidGateway() {

        for (UPnPGatewayDevice device : devices.values()) {
            try {
                if (device.isConnected()) {
                    return device;
                }
            } catch (IOException | SAXException e) {
                System.out.println(e);
            }
        }

        return null;
    }

    /**
     * Returns list of all discovered gateways. Is empty when no gateway is found.
     * @return the device map
     */
    public Map<InetAddress, UPnPGatewayDevice> getAllGateways() {
        return devices;
    }

    /**
     * Retrieves all local IP addresses from all present network devices.
     *
     * @param getIPv4            boolean flag if IPv4 addresses shall be retrieved
     * @param getIPv6            boolean flag if IPv6 addresses shall be retrieved
     * @param sortIPv4BeforeIPv6 if true, IPv4 addresses will be sorted before IPv6 addresses
     * @return Collection if {@link InetAddress}es
     */
    private static List<InetAddress> getLocalInetAddresses(boolean getIPv4, boolean getIPv6, boolean sortIPv4BeforeIPv6) {
        List<InetAddress> arrayIPAddress = new ArrayList<>();
        int lastIPv4Index = 0;

        // Get all network interfaces
        Enumeration<NetworkInterface> networkInterfaces;
        try {
            networkInterfaces = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            return arrayIPAddress;
        }

        if (networkInterfaces == null) {
            return arrayIPAddress;
        }

        // For every suitable network interface, get all IP addresses
        while (networkInterfaces.hasMoreElements()) {
            NetworkInterface card = networkInterfaces.nextElement();

            try {
                // skip devices, not suitable to search gateways for
                if (card.isLoopback() || card.isPointToPoint() 
                		|| card.isVirtual() || !card.isUp()) {
                    continue;
                }
            } catch (SocketException e) {
                continue;
            }

            Enumeration<InetAddress> addresses = card.getInetAddresses();

            if (addresses == null) {
                continue;
            }

            while (addresses.hasMoreElements()) {
                InetAddress inetAddress = addresses.nextElement();
                int index = arrayIPAddress.size();

                if (!getIPv4 || !getIPv6) {
                    if (getIPv4 && !Inet4Address.class.isInstance(inetAddress)) {
                        continue;
                    }

                    if (getIPv6 && !Inet6Address.class.isInstance(inetAddress)) {
                        continue;
                    }
                } else if (sortIPv4BeforeIPv6 && Inet4Address.class.isInstance(inetAddress)) {
                    index = lastIPv4Index++;
                }

                arrayIPAddress.add(index, inetAddress);
            }
        }

        return arrayIPAddress;
    }
    /**
     * Returns the current service discovery port.
     * @return the port number
     */
    public int getSSDPPort() {
    	return ssdpPort;
    }
    /**
     * Returns the current discovery broadcast IP.
     * @return the IP address
     */
    public String getBroadcastIP() {
    	return broadcastIP;
    }
    /**
     * Returns the current discovery timeout in milliseconds.
     * @return the discovery timeout
     */
    public int getBroadcastTimeout() {
    	return broadcastTimeout;
    }
    /**
     * Set a new service discovery port.
     * @param newSSDPPort the new port
     */
    public void setSSDPPort(int newSSDPPort) {
    	this.ssdpPort = newSSDPPort;
    }
    /**
     * Set a new broadcast IP address.
     * @param newBroadcastIP the new IP address
     */
    public void setBroadcastIP(String newBroadcastIP) {
    	this.broadcastIP = newBroadcastIP;
    }
    /**
     * Set a new broadcast timeout.
     * @param newBroadcastTimeout the new timeout value
     */
    public void setBroadcastTimeout(int newBroadcastTimeout) {
    	this.broadcastTimeout = newBroadcastTimeout;
    }
}
