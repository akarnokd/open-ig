/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.launcher;

import hu.openig.utils.Exceptions;
import hu.openig.utils.XElement;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamException;

/**
 * The container for various module updates.
 * @author akarnokd, 2010.10.31.
 */
public class LUpdate {
	/** The list of modules. */
	public final List<LModule> modules = new ArrayList<>();
	/**
	 * Parse a module definition located at the specified URL.
	 * @param data the XML data in byte array
	 * @throws IOException if there is a problem with the file or network
	 */
	public void parse(byte[] data) throws IOException {
		try {
			process(XElement.parseXML(new ByteArrayInputStream(data)));
		} catch (XMLStreamException ex) {
			Exceptions.add(ex);
		}
	}
	/**
	 * Process the contents of the XML.
	 * @param root the root node
	 */
	public void process(XElement root) {
		for (XElement module : root.childrenWithName("module")) {
			LModule mdl = new LModule();
			modules.add(mdl);
			
			mdl.id = module.get("id");
			mdl.version = module.get("version");
			
			XElement gen = module.childElement("general");
			mdl.general.url = gen.get("url");
			mdl.general.parse(gen);
			
			XElement not = module.childElement("notes");
			mdl.releaseNotes.url = not.get("url");
			mdl.releaseNotes.parse(not);
			
			XElement exec = module.childElement("execute");
			if (exec.has("memory")) {
				mdl.memory = Integer.parseInt(exec.get("memory"));
			}
			if (exec.has("class")) {
				mdl.clazz = exec.get("class");
			}
			
			for (XElement eFile : module.childrenWithName("file")) {
				LFile f = new LFile();
				f.url = eFile.get("url");
				f.sha1 = eFile.get("sha1");
				f.size = eFile.getLong("size", -1);
				f.language = eFile.get("language", "generic");
				f.parse(eFile);
				mdl.files.add(f);
			}
			for (XElement eDelete : module.childrenWithName("remove")) {
				LRemoveFile f = new LRemoveFile();
				f.file = eDelete.get("file");
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
	public LModule getModule(String id) {
		for (LModule m : modules) {
			if (m.id.equals(id)) {
				return m;
			}
		}
		return null;
	}
}
