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

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * A SAX handler used to parse XML data representing a GatewayDevice.
 *
 * @see org.xml.sax.helpers.DefaultHandler
 */
public class UPnPGatewayDeviceHandler extends DefaultHandler {

    /**
     * The device that should be populated with data coming from the stream
     * being parsed.
     */
    private UPnPGatewayDevice device;

    /**
     * Creates a new instance of GatewayDeviceHandler that will populate the
     * fields of the supplied device.
     *
     * @param device the device to configure
     */
    public UPnPGatewayDeviceHandler(final UPnPGatewayDevice device) {
        this.device = device;
    }

    /** State variables. */
    private String currentElement;
    /** The state. */
    private short state = 0;

    @Override
    public void startElement(String uri, String localName, String qName,
            Attributes attributes) throws SAXException {
        currentElement = localName;
        if (state < 1 && "serviceList".equals(currentElement)) {
            state = 1;
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        currentElement = "";
        if ("service".equals(localName)) {
            if (device.getServiceTypeCIF() != null 
            		&& device.getServiceTypeCIF().compareTo("urn:schemas-upnp-org:service:WANCommonInterfaceConfig:1") == 0) {
                state = 2;
            }
            if (device.getServiceType() != null 
            		&& device.getServiceType().compareTo("urn:schemas-upnp-org:service:WANIPConnection:1") == 0) {
                state = 3;
            }
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if ("URLBase".equals(currentElement)) {
            device.setURLBase(new String(ch, start, length));
        } else 
        if (state <= 1) {
            if (state == 0) {
                switch (currentElement) {
                case "friendlyName":
                    device.setFriendlyName(new String(ch, start, length));
                    break;
                case "manufacturer":
                    device.setManufacturer(new String(ch, start, length));
                    break;
                case "modelDescription":
                    device.setModelDescription(new String(ch, start, length));
                    break;
                case "presentationURL":
                    device.setPresentationURL(new String(ch, start, length));
                    break;
                case "modelNumber":
                    device.setModelNumber(new String(ch, start, length));
                    break;
                case "modelName":
                    device.setModelName(new String(ch, start, length));
                    break;
                default:
                }
            }
            switch (currentElement) {
            case "serviceType":
                device.setServiceTypeCIF(new String(ch, start, length));
                break;
            case "controlURL":
                device.setControlURLCIF(new String(ch, start, length));
                break;
            case "eventSubURL":
                device.setEventSubURLCIF(new String(ch, start, length));
                break;
            case "SCPDURL":
                device.setSCPDURLCIF(new String(ch, start, length));
                break;
            case "deviceType":
                device.setDeviceTypeCIF(new String(ch, start, length));
                break;
            default:
            }
        } else 
        if (state == 2) {
            switch (currentElement) {
            case "serviceType":
                device.setServiceType(new String(ch, start, length));
                break;
            case "controlURL":
                device.setControlURL(new String(ch, start, length));
                break;
            case "eventSubURL":
                device.setEventSubURL(new String(ch, start, length));
                break;
            case "SCPDURL":
                device.setSCPDURL(new String(ch, start, length));
                break;
            case "deviceType":
                device.setDeviceType(new String(ch, start, length));
                break;
            default:
            }
        }
    }
    
}
