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
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * Alessandro Bahgat Shehata - ale dot bahgat at gmail dot com
 * Daniele Castagna - daniele dot castagna at gmail dot com
 *
 */

package hu.openig.net;

import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * A simple SAX handler that is used to parse XML name value pairs in the form
 * &lt;name&gt;value&lt;/name&gt;.
 *
 * @see org.xml.sax.helpers.DefaultHandler
 */
public class UPnPNameValueHandler extends DefaultHandler {

    /**
     * A reference to the name-value map to populate with the data being read.
     */
    private Map<String, String> nameValue;

    /**
     * The last read element.
     */
    private String currentElement;

    /**
     * Creates a new instance of a <tt>NameValueHandler</tt>, storing values in
     * the supplied map.
     *
     * @param nameValue the map to store name-value pairs in
     */
    public UPnPNameValueHandler(Map<String, String> nameValue) {
        this.nameValue = nameValue;
    }

    @Override
    public void startElement(String uri, String localName, String qName,
            Attributes attributes) throws SAXException {
        currentElement = localName;
    }

    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        currentElement = null;
    }

    @Override
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        if (currentElement != null) {
            String value = new String(ch, start, length);
            String old = nameValue.put(currentElement, value);
            if (old != null) {
                nameValue.put(currentElement, old + value);
            }
        }
    }

}
