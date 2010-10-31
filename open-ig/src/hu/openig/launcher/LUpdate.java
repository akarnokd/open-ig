/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.launcher;

import hu.openig.utils.XML;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * The container for various module updates.
 * @author karnokd, 2010.10.31.
 * @version $Revision 1.0$
 */
public class LUpdate {
	/** The list of modules. */
	public final List<LModule> modules = new ArrayList<LModule>();
	/**
	 * Parse a module definition located at the specified URL.
	 * @param data the XML data in byte array
	 * @throws IOException if there is a problem with the file or network
	 */
	public void parse(byte[] data) throws IOException {
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(new ByteArrayInputStream(data));
			process(doc.getDocumentElement());
		} catch (SAXException ex) {
			throw new IOException(ex);
		} catch (ParserConfigurationException ex) {
			throw new IOException(ex);
		}
	}
	/**
	 * Process the contents of the XML.
	 * @param root the root node
	 */
	void process(Element root) {
		for (Element module : XML.childrenWithName(root, "module")) {
			LModule mdl = new LModule();
			modules.add(mdl);
			
			mdl.id = module.getAttribute("id");
			mdl.version = module.getAttribute("version");
			
			Element gen = XML.childElement(module, "general");
			mdl.general.url = gen.getAttribute("url");
			mdl.general.parse(gen);
			
			Element not = XML.childElement(module, "notes");
			mdl.releaseNotes.url = not.getAttribute("url");
			mdl.releaseNotes.parse(not);
			
			Element exec = XML.childElement(module, "execute");
			if (exec.hasAttribute("memory")) {
				mdl.memory = Integer.parseInt(exec.getAttribute("memory"));
			}
			mdl.executeFile = exec.getAttribute("file");
			
			for (Element eFile : XML.childrenWithName(module, "file")) {
				LFile f = new LFile();
				f.url = eFile.getAttribute("url");
				f.sha1 = eFile.getAttribute("sha1");
				f.parse(eFile);
				mdl.files.add(f);
			}
			for (Element eDelete : XML.childrenWithName(module, "remove")) {
				LRemoveFile f = new LRemoveFile();
				f.file = eDelete.getAttribute("file");
				f.parse(eDelete);
				mdl.removeFiles.add(f);
			}
		}
	}
	/**
	 * Retrieve a module by its ID.
	 * @param id the module id
	 * @return the module or null if not found
	 */
	LModule getModule(String id) {
		for (LModule m : modules) {
			if (m.id.equals(id)) {
				return m;
			}
		}
		return null;
	}
}
