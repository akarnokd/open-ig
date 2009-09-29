/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.v1;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author karnokd, 2009.09.23.
 * @version $Revision 1.0$
 */
public class ResourceLocator {
	/** The directories and ZIP files that contain resources, order is inportant. */
	private final List<String> containers = new ArrayList<String>();
	/** The resource map. */
	public final Map<ResourceType, Map<String, Map<String, ResourcePlace>>> resourceMap = new HashMap<ResourceType, Map<String, Map<String, ResourcePlace>>>();
	/** The pre-opened ZIP containers. */
	private final Map<String, ZipFile> zipContainers = new HashMap<String, ZipFile>();
	/**
	 * A concrete resource place.
	 * @author karnokd, 2009.09.23.
	 * @version $Revision 1.0$
	 */
	class ResourcePlace {
		/** The container of this resource: A directory or a ZIP file. */
		String container;
		/** The resource language or <code>generic</code> if common. */
		String language;
		/** The resource path and name within the container. */
		String name;
		/** The file name as it is within the container. */
		String fileName;
		/** The type of the resource. */
		ResourceType type;
		/** Is the container a ZIP file? */
		boolean zipContainer;
		/** The resource size. */
		int size;
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof ResourcePlace) {
				ResourcePlace rp = (ResourcePlace)obj;
				return this.name.equals(rp.name);
			}
			return false;
		}
		@Override
		public int hashCode() {
			return name.hashCode();
		}
		@Override
		public String toString() {
			return fileName + " [" + type + "]";
		}
		/** 
		 * Open an input stream to the file. 
		 * @return the input stream to the file
		 */
		public InputStream open() {
			try {
				if (zipContainer) {
					ZipFile zf = zipContainers.get(container);
					try {
						return zf.getInputStream(zf.getEntry(fileName));
					} catch (IllegalStateException ex) {
						zf = new ZipFile(container);
						zipContainers.put(container, zf);
						return zf.getInputStream(zf.getEntry(fileName));
					}
				} else {
					return new FileInputStream(container + "/" + fileName);
				}
			} catch (IOException ex) {
				throw new RuntimeException(ex);
			} 
		}
	}
	/**
	 * Set the list of resource containers.
	 * @param containers the iterable of containers
	 */
	public void setContainers(Iterable<String> containers) {
		this.containers.clear();
		for (String s : containers) {
			this.containers.add(s);
		}
	}
	/**
	 * Scan the given resource containers for concrete files.
	 */
	public void scanResources() {
		// scan backwards and let the newer overrule the existing resource
		for (int i = containers.size() - 1; i >= 0; i--) {
			String c = containers.get(i);
			if (c.toLowerCase().endsWith(".zip")) {
				analyzeZip(c);
			} else {
				c = c.replaceAll("\\\\", "/");
				if (!c.endsWith("/")) {
					c += "/";
				}
				analyzeDir(c, c);
			}
		}
	}
	/**
	 * Analyze the ZIP file.
	 * @param zipFile the zip file to analyze
	 */
	private void analyzeZip(String zipFile) {
		try {
			ZipFile zf = new ZipFile(zipFile);
			zipContainers.put(zipFile, zf);
			Enumeration<? extends ZipEntry> en = zf.entries();
			while (en.hasMoreElements()) {
				ZipEntry ze = en.nextElement();
				if (ze.isDirectory()) {
					continue;
				}
				String name = ze.getName();
				ResourcePlace rp = new ResourcePlace();
				rp.container = zipFile;
				rp.zipContainer = true;
				rp.size = (int)ze.getSize();
				
				setNameParts(name, rp);
			}
			zf.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	/**
	 * Set name parts into the resource place.
	 * @param name the file name
	 * @param rp the resource place
	 */
	private void setNameParts(String name, ResourcePlace rp) {
		// skip type selector
		int idx = name.indexOf('/');
		if (idx < 0) {
			return;
		}
		rp.language = name.substring(0, idx);
		int idx2 = name.indexOf('.');
		
		String type = "";
		if (idx2 > 0) {
			type = name.substring(idx2 + 1).toLowerCase();
			rp.name = name.substring(idx + 1, idx2);
		} else {
			rp.name = name.substring(idx + 1);
		}
		
		rp.fileName = name;
		
		if ("png".equals(type) || "img".equals(type)) {
			rp.type = ResourceType.IMAGE;
		} else
		if ("wav.raw".equals(type) || "wav".equals(type) || "ogg".equals(type)) {
			rp.type = ResourceType.AUDIO;
		} else
		if ("sub".equals(type)) {
			rp.type = ResourceType.SUBTITLE;
		} else
		if ("ani.gz".equals(type)) {
			rp.type = ResourceType.VIDEO;
		} else {
			rp.type = ResourceType.DATA;
		}
		
		addResourcePlace(rp);
	}
	/**
	 * @param rp add a resource place.
	 */
	private void addResourcePlace(ResourcePlace rp) {
		Map<String, Map<String, ResourcePlace>> res = resourceMap.get(rp.type);
		if (res == null) {
			res = new HashMap<String, Map<String, ResourcePlace>>();
			resourceMap.put(rp.type, res);
		}
		Map<String, ResourcePlace> rps = res.get(rp.language);
		if (rps == null) {
			rps = new HashMap<String, ResourcePlace>();
			res.put(rp.language, rps);
		}
		rps.put(rp.name, rp);
	}
	/**
	 * Analyze the contents of the directory.
	 * @param directory the base directory
	 * @param rel the current directory
	 */
	private void analyzeDir(String directory, String rel) {
		File dir = new File(rel);
		File[] files = dir.listFiles();
		if (files == null) {
			return;
		}
		for (File f : files) {
			 if (!f.isHidden()) {
				if (f.isDirectory()) {
					analyzeDir(directory, rel + f.getName() + "/");
				} else {
					 ResourcePlace rp = new ResourcePlace();
					 rp.container = directory;
					 rp.size = (int)f.length();
					 setNameParts(rel.substring(directory.length()) + f.getName(), rp);
				}
			 }
		}
	}
	/**
	 * Close all zip containers.
	 */
	public void close() {
		for (ZipFile zf : zipContainers.values()) {
			try {
				zf.close();
			} catch (IOException ex) {
				// ignored
			}
		}
	}
	/**
	 * Get resource for a language, type and resource name.
	 * @param language the language
	 * @param resourceName the resource name with dash
	 * @param type the resource type
	 * @return the resource place
	 */
	public ResourcePlace get(String language, String resourceName, ResourceType type) {
		Map<String, Map<String, ResourcePlace>> res = resourceMap.get(type);
		if (res != null) {
			Map<String, ResourcePlace> rps = res.get(language);
			if (rps != null) {
				ResourcePlace rp = rps.get(resourceName);
				if (rp != null) {
					return rp;
				}
			}
			rps = res.get("generic");
			if (rps != null) {
				return rps.get(resourceName);
			}
		}
		return null;
	}
	/**
	 * Get resource for a language, type and resource name, but does not look for a generic version.
	 * @param language the language
	 * @param resourceName the resource name with dash
	 * @param type the resource type
	 * @return the resource place
	 */
	public ResourcePlace getExactly(String language, String resourceName, ResourceType type) {
		Map<String, Map<String, ResourcePlace>> res = resourceMap.get(type);
		if (res != null) {
			Map<String, ResourcePlace> rps = res.get(language);
			if (rps != null) {
				ResourcePlace rp = rps.get(resourceName);
				if (rp != null) {
					return rp;
				}
			}
		}
		return null;
	}
	
	/**
	 * Clear the resource map.
	 */
	public void clear() {
		resourceMap.clear();
	}
}
