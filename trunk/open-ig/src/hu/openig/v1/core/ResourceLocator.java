/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.v1.core;

import hu.openig.utils.ImageUtils;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * @author karnokd, 2009.09.23.
 * @version $Revision 1.0$
 */
public class ResourceLocator {
	/** The directories and ZIP files that contain resources, order is inportant. */
	private final List<String> containers = new ArrayList<String>();
	/** The resource map from type to language to path. */
	public final Map<ResourceType, Map<String, Map<String, ResourcePlace>>> resourceMap = new HashMap<ResourceType, Map<String, Map<String, ResourcePlace>>>();
	/** The pre-opened ZIP containers. */
	private final Map<String, ZipFile> zipContainers = new HashMap<String, ZipFile>();
	/**
	 * A concrete resource place.
	 * @author karnokd, 2009.09.23.
	 * @version $Revision 1.0$
	 */
	public class ResourcePlace {
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
		long size;
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
					if (zf != null) {
						try {
							return zf.getInputStream(zf.getEntry(fileName));
						} catch (IllegalStateException ex) {
							zf = new ZipFile(container);
							zipContainers.put(container, zf);
							return zf.getInputStream(zf.getEntry(fileName));
						}
					} else {
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
		/**
		 * @return the resource path and name separated by slashes
		 */
		public String getName() {
			return name;
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
//		List<ResourcePlace> resources = loadResourceCache();
//		if (resources == null) {
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
//			storeResourceCache();
//		} else {
//			dispatchResourcePlaces(resources);
//		}
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
				rp.size = ze.getSize();
				
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
		} else 
		if ("xml".equals(type)) {
			rp.type = ResourceType.DATA;
		} else {
			rp.type = ResourceType.OTHER;
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
					 rp.size = f.length();
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
		resourceName = resourceName.replaceAll("/{2,}", "/");
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
	/**
	 * Get the given resource as image.
	 * @param language the language
	 * @param resourceName the resource name, don't start it with slash
	 * @return the buffered image
	 */
	public BufferedImage getImage(String language, String resourceName) {
		ResourcePlace rp = get(language, resourceName, ResourceType.IMAGE);
		if (rp == null) {
			throw new AssertionError("Missing resource: " + language + " " + resourceName);
		}
		InputStream in = rp.open();
		try {
			return ImageIO.read(in);
		} catch (IOException ex) {
			ex.printStackTrace();
			throw new AssertionError("Resource error" + language + " " + resourceName);
		} finally {
			try { in.close(); } catch (IOException ex) { ex.printStackTrace(); }
		}
	}
	/**
	 * Returns the given resource as byte data.
	 * @param language the language
	 * @param resourceName the resource name.
	 * @return the byte data of the resource
	 */
	public byte[] getData(String language, String resourceName) {
		ResourcePlace rp = get(language, resourceName, ResourceType.DATA);
		if (rp == null) {
			rp = get(language, resourceName, ResourceType.OTHER);
		}
		if (rp == null) {
			throw new AssertionError("Missing resource: " + language + " " + resourceName);
		}
		InputStream in = rp.open();
		try {
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			byte[] buffer = new byte[4096];
			int read = 0;
			do {
				read = in.read(buffer);
				if (read > 0) {
					bout.write(buffer, 0, read);
				}
			} while (read >= 0);
			return bout.toByteArray();
		} catch (IOException ex) {
			ex.printStackTrace();
			throw new AssertionError("Resource error" + language + " " + resourceName);
		} finally {
			try { in.close(); } catch (IOException ex) { ex.printStackTrace(); }
		}
				
	}
	/**
	 * Get a list of resources of a given path.
	 * @param language the target language
	 * @param path the path
	 * @return the list of resources
	 */
	public List<ResourcePlace> list(String language, String path) {
		List<ResourcePlace> result = new ArrayList<ResourcePlace>();
		Set<String> rs = new HashSet<String>();
		for (Map<String, Map<String, ResourcePlace>> e : resourceMap.values()) {
			for (String s : new String[] { language, "generic" }) {
				Map<String, ResourcePlace> e1 = e.get(s);
				if (e1 != null) {
					for (Map.Entry<String, ResourcePlace> e2 : e1.entrySet()) {
						if (e2.getKey().startsWith(path)) {
							int idx = e2.getKey().indexOf('/', path.length());
							if (idx < 0) {
								if (rs.add(e2.getKey())) {
									result.add(e2.getValue());
								}
							}
						}
					}
				}
			}
		}
		return result;
	}
	/**
	 * Lists the directories below a given path. Use an ending slash for non-root directories.
	 * @param language the language
	 * @param path the path
	 * @return the list of directory names
	 */
	public List<String> listDirectories(String language, String path) {
		Set<String> result = new HashSet<String>();
		for (Map<String, Map<String, ResourcePlace>> e : resourceMap.values()) {
			for (String s : new String[] { language, "generic" }) {
				Map<String, ResourcePlace> e1 = e.get(s);
				if (e1 != null) {
					for (Map.Entry<String, ResourcePlace> e2 : e1.entrySet()) {
						if (e2.getKey().startsWith(path)) {
							int idx = e2.getKey().indexOf('/', path.length());
							if (idx > 0) {
								result.add(e2.getKey().substring(path.length(), idx));
							}
						}
					}
				}
			}
		}
		return new ArrayList<String>(result);
	}
	/**
	 * Get the given XML resource.
	 * @param language the language.
	 * @param resourceName the resource name omitting any leading slash.
	 * @return the element
	 */
	public Element getXML(String language, String resourceName) {
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			ResourcePlace rp = get(language, resourceName, ResourceType.DATA);
			if (rp == null) {
				throw new AssertionError("Missing resource: " + language + " " + resourceName);
			}
			InputStream in = rp.open();
			try {
				return db.parse(in).getDocumentElement();
			} finally {
				in.close();
			}
		} catch (IOException ex) {
			ex.printStackTrace();
			throw new AssertionError("Resource error" + language + " " + resourceName);
		} catch (ParserConfigurationException ex) {
			ex.printStackTrace();
			throw new AssertionError("Resource error" + language + " " + resourceName);
		} catch (SAXException ex) {
			ex.printStackTrace();
			throw new AssertionError("Resource error" + language + " " + resourceName);
		}
	}
	/**
	 * Get a multi-phase animation by splitting the target image.
	 * @param language the target language
	 * @param name the button name
	 * @param width the phase width or -1 if not applicable
	 * @param step the number of steps or -1 if not applicable
	 * @return the array.
	 */
	public BufferedImage[] getAnimation(String language, String name, int width, int step) {
		BufferedImage img = getImage(language, name);
		int n = width >= 0 ? img.getWidth() / width : step;
		int w = width >= 0 ? width : img.getWidth() / step;
		BufferedImage[] result = new BufferedImage[n];
		for (int i = 0; i < result.length; i++) {
			result[i] = ImageUtils.newSubimage(img, i * w, 0, w, img.getHeight());
		}
		return result;
	}
//	/**
//	 * Save the resource cache.
//	 */
//	protected void storeResourceCache() {
//		try {
//			File f = new File("resource-cache.xml.gz");
//			PrintWriter pw = new PrintWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(f)), "UTF-8"));
//			try {
//				pw.println("<?xml version='1.0' encoding='UTF-8' standalone='yes'?>");
//				pw.println("<containers><!-- backwards -->");
//				for (int i = containers.size() - 1; i >= 0; i--) {
//					String name = containers.get(i);
//					f = new File(name);
//					if (f.exists()) {
//						boolean isZip = zipContainers.containsKey(name);
//						pw.printf("  <container name='%s' zip='%s'>%n"
//								, XML.toHTML(name), isZip);
//						for (Map<String, Map<String, ResourcePlace>> m1 : resourceMap.values()) {
//							for (Map<String, ResourcePlace> m2 : m1.values()) {
//								for (ResourcePlace rp : m2.values()) {
//									pw.printf("    <resource name='%s' language='%s' file='%s' size='%s' type='%s'/>%n"
//										, XML.toHTML(rp.name), XML.toHTML(rp.language), XML.toHTML(rp.fileName), 
//												rp.size, XML.toHTML(rp.type.toString())
//									);
//								}
//							}
//						}
//						pw.println("  </container>");
//					}
//				}
//				pw.println("</containers>");
//			} finally {
//				pw.close();
//			}
//		} catch (IOException ex) {
//			ex.printStackTrace();
//		}
//	}
//	/**
//	 * Try to load the resource cache.
//	 * @return the list of resource items or null if there is no cache or seems to be outdated.
//	 */
//	protected List<ResourcePlace> loadResourceCache() {
//		try {
//			File f = new File("resource-cache.xml.gz");
//			if (f.canRead()) {
//				GZIPInputStream gin = new GZIPInputStream(new FileInputStream(f));
//				try {
//					List<ResourcePlace> result = JavaUtils.newArrayList();
//					DocumentBuilderFactory db = DocumentBuilderFactory.newInstance();
//					DocumentBuilder doc = db.newDocumentBuilder();
//					Element root = doc.parse(gin).getDocumentElement();
//					int index = 0;
//					for (Element ec : XML.childrenWithName(root, "container")) {
//						if (index >= containers.size()) {
//							return null;
//						}
//						String container = ec.getAttribute("name");
//						if (!containers.get(containers.size() - 1 - index).equals(container)) {
//							return null;
//						}
//						File fc = new File(container);
//						if (!fc.exists()) {
//							return null;
//						}
//						boolean zip = "true".equals(ec.getAttribute("zip"));
//						for (Element erp : XML.childrenWithName(ec, "resource")) {
//							ResourcePlace rp = new ResourcePlace();
//							rp.name = erp.getAttribute("name");
//							rp.fileName = erp.getAttribute("file");
//							rp.language = erp.getAttribute("language");
//							rp.size = Long.parseLong(erp.getAttribute("size"));
//							rp.type = ResourceType.valueOf(erp.getAttribute("type"));
//							rp.zipContainer = zip;
//							rp.container = container;
//							
//							if (!rp.zipContainer) {
//								if (!(new File(container + "/" + rp.fileName).exists())) {
//									return null;
//								}
//							} else {
//								ZipFile zf = new ZipFile(container);
//								if (zf.getEntry(rp.fileName) == null) {
//									return null;
//								}
//							}
//							
//							result.add(rp);
//						}
//						index++;
//					}
//					return result;
//				} finally {
//					gin.close();
//				}
//			}
//		} catch (IOException ex) {
//			ex.printStackTrace();
//		} catch (SAXException ex) {
//			ex.printStackTrace();
//		} catch (ParserConfigurationException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return null;
//	}
//	/**
//	 * Dispatch the resource places into the resourceMap.
//	 * @param resources the list of resource places to dispatch
//	 */
//	private void dispatchResourcePlaces(List<ResourcePlace> resources) {
//		for (ResourcePlace rp : resources) {
//			addResourcePlace(rp);
//		}
//	}
	
}
