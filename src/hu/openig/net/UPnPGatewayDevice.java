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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * A <tt>GatewayDevice</tt> is a class that abstracts UPnP-compliant gateways
 * <p/>
 * It holds all the information that comes back as UPnP responses, and
 * provides methods to issue UPnP commands to a gateway.
 *
 * @author casta
 */
public class UPnPGatewayDevice {

	/**
	 * Receive timeout when requesting data from device.
	 */
    private static final int HTTP_RECEIVE_TIMEOUT = 7000;
    /** Device property. */
	private String st;
    /** Device property. */
    private String location;
    /** Device property. */
    private String serviceType;
    /** Device property. */
    private String serviceTypeCIF;
    /** Device property. */
    private String urlBase;
    /** Device property. */
    private String controlURL;
    /** Device property. */
    private String controlURLCIF;
    /** Device property. */
    private String eventSubURL;
    /** Device property. */
    private String eventSubURLCIF;
    /** Device property. */
    private String sCPDURL;
    /** Device property. */
    private String sCPDURLCIF;
    /** Device property. */
    private String deviceType;
    /** Device property. */
    private String deviceTypeCIF;

    // description data

    /**
     * The friendly (human readable) name associated with this device.
     */
    private String friendlyName;

    /**
     * The device manufacturer name.
     */
    private String manufacturer;

    /**
     * The model description as a string.
     */
    private String modelDescription;

    /**
     * The URL that can be used to access the IGD interface.
     */
    private String presentationURL;

    /**
     * The address used to reach this machine from the GatewayDevice.
     */
    private InetAddress localAddress;

    /**
     * The model number (used by the manufacturer to identify the product).
     */
    private String modelNumber;

    /**
     * The model name.
     */
    private String modelName;

    /**
     * Creates a new instance of GatewayDevice.
     */
    public UPnPGatewayDevice() {
    }

    /**
     * Retrieves the properties and description of the GatewayDevice.
     * <p/>
     * Connects to the device's {@link #location} and parses the response
     * using a {@link UPnPGatewayDeviceHandler} to populate the fields of this
     * class
     *
     * @throws SAXException if an error occurs while parsing the request
     * @throws IOException  on communication errors
     * @see hu.openig.net.UPnPGatewayDeviceHandler
     */
    public void loadDescription() throws SAXException, IOException {

        URLConnection urlConn = new URL(getLocation()).openConnection();
        urlConn.setReadTimeout(HTTP_RECEIVE_TIMEOUT);

        XMLReader parser = XMLReaderFactory.createXMLReader();
        parser.setContentHandler(new UPnPGatewayDeviceHandler(this));
        parser.parse(new InputSource(urlConn.getInputStream()));


        /* fix urls */
        String ipConDescURL;
        if (urlBase != null && urlBase.trim().length() > 0) {
            ipConDescURL = urlBase;
        } else {
            ipConDescURL = location;
        }

        int lastSlashIndex = ipConDescURL.indexOf('/', 7);
        if (lastSlashIndex > 0) {
            ipConDescURL = ipConDescURL.substring(0, lastSlashIndex);
        }


        sCPDURL = copyOrCatUrl(ipConDescURL, sCPDURL);
        controlURL = copyOrCatUrl(ipConDescURL, controlURL);
        controlURLCIF = copyOrCatUrl(ipConDescURL, controlURLCIF);
        presentationURL = copyOrCatUrl(ipConDescURL, presentationURL);
    }

    /**
     * Issues UPnP commands to a GatewayDevice that can be reached at the
     * specified <tt>url</tt>.
     * <p/>
     * The command is identified by a <tt>service</tt> and an <tt>action</tt>
     * and can receive arguments
     *
     * @param url     the url to use to contact the device
     * @param service the service to invoke
     * @param action  the specific action to perform
     * @param args    the command arguments
     * @return the response to the performed command, as a name-value map.
     *         In case errors occur, the returned map will be <i>empty.</i>
     * @throws IOException  on communication errors
     * @throws SAXException if errors occur while parsing the response
     */
    public static Map<String, String> simpleUPnPcommand(String url,
                                                        String service, String action, Map<String, String> args)
            throws IOException, SAXException {
        String soapAction = "\"" + service + "#" + action + "\"";
        StringBuilder soapBody = new StringBuilder();

        soapBody.append("<?xml version=\"1.0\"?>\r\n")
                .append("<SOAP-ENV:Envelope ")
                .append("xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" ")
                .append("SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">")
                .append("<SOAP-ENV:Body>")
                .append("<m:")
                .append(action)
                .append(" xmlns:m=\"")
                .append(service)
                .append("\">");

        if (args != null && args.size() > 0) {

            Set<Map.Entry<String, String>> entrySet = args.entrySet();

            for (Map.Entry<String, String> entry : entrySet) {
                soapBody.append("<");
				soapBody.append(entry.getKey());
				soapBody.append(">");
				soapBody.append(entry.getValue());
				soapBody.append("</");
				soapBody.append(entry.getKey());
				soapBody.append(">");
            }

        }

        soapBody.append("</m:");
		soapBody.append(action);
		soapBody.append(">");
		soapBody.append("</SOAP-ENV:Body></SOAP-ENV:Envelope>");

        URL postUrl = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) postUrl.openConnection();

        conn.setRequestMethod("POST");
        conn.setReadTimeout(HTTP_RECEIVE_TIMEOUT);
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "text/xml");
        conn.setRequestProperty("SOAPAction", soapAction);
        conn.setRequestProperty("Connection", "Close");

        byte[] soapBodyBytes = soapBody.toString().getBytes();

        conn.setRequestProperty("Content-Length",
                String.valueOf(soapBodyBytes.length));

        conn.getOutputStream().write(soapBodyBytes);

        Map<String, String> nameValue = new HashMap<>();
        XMLReader parser = XMLReaderFactory.createXMLReader();
        parser.setContentHandler(new UPnPNameValueHandler(nameValue));
        if (conn.getResponseCode() == HttpURLConnection.HTTP_INTERNAL_ERROR) {
            try {
                // attempt to parse the error message
                parser.parse(new InputSource(conn.getErrorStream()));
            } catch (SAXException e) {
                // ignore the exception
                // FIXME We probably need to find a better way to return
                // significant information when we reach this point
            }
            conn.disconnect();
            return nameValue;
        }
        parser.parse(new InputSource(conn.getInputStream()));
        conn.disconnect();
        return nameValue;
    }

    /**
     * Retrieves the connection status of this device.
     *
     * @return true if connected, false otherwise
     * @throws IOException on error
     * @throws SAXException on error
     * @see #simpleUPnPcommand(java.lang.String, java.lang.String,
     *      java.lang.String, java.util.Map)
     */
    public boolean isConnected() throws IOException, SAXException {
        Map<String, String> nameValue = simpleUPnPcommand(controlURL,
                serviceType, "GetStatusInfo", null);

        String connectionStatus = nameValue.get("NewConnectionStatus");
        return connectionStatus != null
                && connectionStatus.equalsIgnoreCase("Connected");

    }

    /**
     * Retrieves the external IP address associated with this device.
     * <p/>
     * The external address is the address that can be used to connect to the
     * GatewayDevice from the external network
     *
     * @return the external IP
     * @throws IOException on error
     * @throws SAXException on error
     * @see #simpleUPnPcommand(java.lang.String, java.lang.String,
     *      java.lang.String, java.util.Map)
     */
    public String getExternalIPAddress() throws IOException, SAXException {
        Map<String, String> nameValue = simpleUPnPcommand(controlURL,
                serviceType, "GetExternalIPAddress", null);

        return nameValue.get("NewExternalIPAddress");
    }

    /**
     * Adds a new port mapping to the GatewayDevices using the supplied
     * parameters.
     *
     * @param externalPort   the external associated with the new mapping
     * @param internalPort   the internal port associated with the new mapping
     * @param internalClient the internal client associated with the new mapping
     * @param protocol       the protocol associated with the new mapping
     * @param description    the mapping description
     * @return true if the mapping was succesfully added, false otherwise
     * @throws IOException on error
     * @throws SAXException on error
     * @see #simpleUPnPcommand(java.lang.String, java.lang.String,
     *      java.lang.String, java.util.Map)
     * @see UPnPPortMappingEntry
     */
    public boolean addPortMapping(int externalPort, int internalPort,
                                  String internalClient, String protocol, String description)
            throws IOException, SAXException {
        Map<String, String> args = new HashMap<>();
        args.put("NewRemoteHost", "");    // wildcard, any remote host matches
        args.put("NewExternalPort", Integer.toString(externalPort));
        args.put("NewProtocol", protocol);
        args.put("NewInternalPort", Integer.toString(internalPort));
        args.put("NewInternalClient", internalClient);
        args.put("NewEnabled", Integer.toString(1));
        args.put("NewPortMappingDescription", description);
        args.put("NewLeaseDuration", Integer.toString(0));

        Map<String, String> nameValue = simpleUPnPcommand(controlURL,
                serviceType, "AddPortMapping", args);

        return nameValue.get("errorCode") == null;
    }

    /**
     * Queries the GatewayDevice to retrieve a specific port mapping entry,
     * corresponding to specified criteria, if present.
     * <p/>
     * Retrieves the <tt>PortMappingEntry</tt> associated with
     * <tt>externalPort</tt> and <tt>protocol</tt>, if present.
     *
     * @param externalPort     the external port
     * @param protocol         the protocol (TCP or UDP)
     * @param portMappingEntry the entry containing the details, in any is
     *                         present, <i>null</i> otherwise. <i>(used as return value)</i>
     * @return true if a valid mapping is found
     * @throws IOException on error
     * @throws SAXException on error
     * @see #simpleUPnPcommand(java.lang.String, java.lang.String,
     *      java.lang.String, java.util.Map)
     * @see UPnPPortMappingEntry
     */
    public boolean getSpecificPortMappingEntry(int externalPort,
                                               String protocol, final UPnPPortMappingEntry portMappingEntry)
            throws IOException, SAXException {

        portMappingEntry.setExternalPort(externalPort);
        portMappingEntry.setProtocol(protocol);

        Map<String, String> args = new HashMap<>();
        args.put("NewRemoteHost", ""); // wildcard, any remote host matches
        args.put("NewExternalPort", Integer.toString(externalPort));
        args.put("NewProtocol", protocol);

        Map<String, String> nameValue = simpleUPnPcommand(controlURL,
                serviceType, "GetSpecificPortMappingEntry", args);

        if (nameValue.isEmpty() || nameValue.containsKey("errorCode")) {
            return false;
        }

        if (!nameValue.containsKey("NewInternalClient") 
        		|| !nameValue.containsKey("NewInternalPort")) {
            return false;
        }

        portMappingEntry.setProtocol(nameValue.get("NewProtocol"));
        portMappingEntry.setEnabled(nameValue.get("NewEnabled"));
        portMappingEntry.setInternalClient(nameValue.get("NewInternalClient"));
        portMappingEntry.setExternalPort(externalPort);
        portMappingEntry.setPortMappingDescription(nameValue.get("NewPortMappingDescription"));
        portMappingEntry.setRemoteHost(nameValue.get("NewRemoteHost"));

        try {
            portMappingEntry.setInternalPort(Integer.parseInt(nameValue.get("NewInternalPort")));
        } catch (NumberFormatException nfe) {
            // skip bad port
        }


        return true;
    }

    /**
     * Returns a specific port mapping entry, depending on a the supplied index.
     *
     * @param index            the index of the desired port mapping
     * @param portMappingEntry the entry containing the details, in any is
     *                         present, <i>null</i> otherwise. <i>(used as return value)</i>
     * @return true if a valid mapping is found
     * @throws IOException on error
     * @throws SAXException on error
     * @see #simpleUPnPcommand(java.lang.String, java.lang.String,
     *      java.lang.String, java.util.Map)
     * @see UPnPPortMappingEntry
     */
    public boolean getGenericPortMappingEntry(int index,
                                              final UPnPPortMappingEntry portMappingEntry)
            throws IOException, SAXException {
        Map<String, String> args = new HashMap<>();
        args.put("NewPortMappingIndex", Integer.toString(index));

        Map<String, String> nameValue = simpleUPnPcommand(controlURL,
                serviceType, "GetGenericPortMappingEntry", args);

        if (nameValue.isEmpty() || nameValue.containsKey("errorCode")) {
            return false;
        }

        portMappingEntry.setRemoteHost(nameValue.get("NewRemoteHost"));
        portMappingEntry.setInternalClient(nameValue.get("NewInternalClient"));
        portMappingEntry.setProtocol(nameValue.get("NewProtocol"));
        portMappingEntry.setEnabled(nameValue.get("NewEnabled"));
        portMappingEntry.setPortMappingDescription(
                nameValue.get("NewPortMappingDescription"));

        try {
            String n = nameValue.get("NewInternalPort");
            if (n != null) {
                portMappingEntry.setInternalPort(Integer.parseInt(n));
            }
        } catch (NumberFormatException e) {
            throw new IOException(e);
        }

        try {
            String n = nameValue.get("NewExternalPort");
            if (n != null) {
                portMappingEntry.setExternalPort(Integer.parseInt(n));
            }
        } catch (NumberFormatException e) {
            throw new IOException(e);
        }

        return true;
    }

    /**
     * Retrieves the number of port mappings that are registered on the
     * GatewayDevice.
     *
     * @return the number of port mappings
     * @throws IOException on error
     * @throws SAXException on error
     */
    public Integer getPortMappingNumberOfEntries()
            throws IOException, SAXException {
        Map<String, String> nameValue = simpleUPnPcommand(controlURL,
                serviceType, "GetPortMappingNumberOfEntries", null);

        Integer portMappingNumber = null;

        try {
            portMappingNumber = Integer.valueOf(
                    nameValue.get("NewPortMappingNumberOfEntries"));
        } catch (Exception e) {
        }

        return portMappingNumber;
    }

    /**
     * Deletes the port mapping associated to <tt>externalPort</tt> and
     * <tt>protocol</tt>.
     *
     * @param externalPort the external port
     * @param protocol     the protocol
     * @return true if removal was successful
     * @throws IOException on error
     * @throws SAXException on error
     */
    public boolean deletePortMapping(int externalPort, String protocol)
            throws IOException, SAXException {
        Map<String, String> args = new HashMap<>();
        args.put("NewRemoteHost", "");
        args.put("NewExternalPort", Integer.toString(externalPort));
        args.put("NewProtocol", protocol);
        Map<String, String> nameValue = simpleUPnPcommand(controlURL,
                serviceType, "DeletePortMapping", args);
        return !nameValue.containsKey("errorCode");
    }

    // getters and setters

    /**
     * Gets the local address to connect the gateway through.
     *
     * @return the {@link #localAddress}
     */
    public InetAddress getLocalAddress() {
        return localAddress;
    }

    /**
     * Sets the {@link #localAddress}.
     *
     * @param localAddress the address to set
     */
    public void setLocalAddress(InetAddress localAddress) {
        this.localAddress = localAddress;
    }

    /**
     * Returns the property.
     * @return the value
     */
    public String getSt() {
        return st;
    }

    /**
     * Sets the property.
     * @param st the value
     */
    public void setSt(String st) {
        this.st = st;
    }

    /**
     * Returns the property.
     * @return the value
     */
    public String getLocation() {
        return location;
    }

    /**
     * Sets the property.
     * @param location the value
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * Returns the property.
     * @return the value
     */
    public String getServiceType() {
        return serviceType;
    }

    /**
     * Sets the property.
     * @param serviceType the value
     */
    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    /**
     * Returns the property.
     * @return the value
     */
    public String getServiceTypeCIF() {
        return serviceTypeCIF;
    }

    /**
     * Sets the property.
     * @param serviceTypeCIF the value
     */
    public void setServiceTypeCIF(String serviceTypeCIF) {
        this.serviceTypeCIF = serviceTypeCIF;
    }

    /**
     * Returns the property.
     * @return the value
     */
    public String getControlURL() {
        return controlURL;
    }

    /**
     * Sets the property.
     * @param controlURL the value
     */
    public void setControlURL(String controlURL) {
        this.controlURL = controlURL;
    }

    /**
     * Returns the property.
     * @return the value
     */
    public String getControlURLCIF() {
        return controlURLCIF;
    }

    /**
     * Sets the property.
     * @param controlURLCIF the value
     */
    public void setControlURLCIF(String controlURLCIF) {
        this.controlURLCIF = controlURLCIF;
    }

    /**
     * Returns the property.
     * @return the value
     */
    public String getEventSubURL() {
        return eventSubURL;
    }

    /**
     * Sets the property.
     * @param eventSubURL the value
     */
    public void setEventSubURL(String eventSubURL) {
        this.eventSubURL = eventSubURL;
    }

    /**
     * Returns the property.
     * @return the value
     */
    public String getEventSubURLCIF() {
        return eventSubURLCIF;
    }

    /**
     * Sets the property.
     * @param eventSubURLCIF the value
     */
    public void setEventSubURLCIF(String eventSubURLCIF) {
        this.eventSubURLCIF = eventSubURLCIF;
    }

    /**
     * Returns the property.
     * @return the value
     */
    public String getSCPDURL() {
        return sCPDURL;
    }

    /**
     * Sets the property.
     * @param sCPDURL the value
     */
    public void setSCPDURL(String sCPDURL) {
        this.sCPDURL = sCPDURL;
    }

    /**
     * Returns the property.
     * @return the value
     */
    public String getSCPDURLCIF() {
        return sCPDURLCIF;
    }

    /**
     * Sets the property.
     * @param sCPDURLCIF the value
     */
    public void setSCPDURLCIF(String sCPDURLCIF) {
        this.sCPDURLCIF = sCPDURLCIF;
    }

    /**
     * Returns the property.
     * @return the value
     */
    public String getDeviceType() {
        return deviceType;
    }

    /**
     * Sets the property.
     * @param deviceType the value
     */
    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    /**
     * Returns the property.
     * @return the value
     */
    public String getDeviceTypeCIF() {
        return deviceTypeCIF;
    }

    /**
     * Sets the property.
     * @param deviceTypeCIF the value
     */
    public void setDeviceTypeCIF(String deviceTypeCIF) {
        this.deviceTypeCIF = deviceTypeCIF;
    }

    /**
     * Returns the property.
     * @return the value
     */
    public String getURLBase() {
        return urlBase;
    }

    /**
     * Sets the property.
     * @param uRLBase the value
     */
    public void setURLBase(String uRLBase) {
        this.urlBase = uRLBase;
    }

    /**
     * Returns the property.
     * @return the value
     */
    public String getFriendlyName() {
        return friendlyName;
    }

    /**
     * Sets the property.
     * @param friendlyName the value
     */
    public void setFriendlyName(String friendlyName) {
        this.friendlyName = friendlyName;
    }

    /**
     * Returns the property.
     * @return the value
     */
    public String getManufacturer() {
        return manufacturer;
    }

    /**
     * Sets the property.
     * @param manufacturer the value
     */
    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    /**
     * Returns the property.
     * @return the value
     */
    public String getModelDescription() {
        return modelDescription;
    }

    /**
     * Sets the property.
     * @param modelDescription the value
     */
    public void setModelDescription(String modelDescription) {
        this.modelDescription = modelDescription;
    }

    /**
     * Returns the property.
     * @return the value
     */
    public String getPresentationURL() {
        return presentationURL;
    }

    /**
     * Sets the property.
     * @param presentationURL the value
     */
    public void setPresentationURL(String presentationURL) {
        this.presentationURL = presentationURL;
    }

    /**
     * Returns the property.
     * @return the value
     */
    public String getModelName() {
        return modelName;
    }

    /**
     * Sets the property.
     * @param modelName the value
     */
    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    /**
     * Returns the property.
     * @return the value
     */
    public String getModelNumber() {
        return modelNumber;
    }

    /**
     * Sets the property.
     * @param modelNumber the value
     */
    public void setModelNumber(String modelNumber) {
        this.modelNumber = modelNumber;
    }

    /**
     * Combines the two strings somehow.
     * @param dst the destination
     * @param src the source
     * @return the combined
     */
    private static String copyOrCatUrl(String dst, String src) {
        if (src != null) {
            if (src.startsWith("http://")) {
                dst = src;
            } else {
                if (!src.startsWith("/")) {
                    dst += "/";
                }
                dst += src;
            }
        }
        return dst;
    }
}
