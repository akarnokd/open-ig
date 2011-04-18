/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.core;

import hu.openig.utils.IOUtils;
import hu.openig.utils.ImageUtils;
import hu.openig.utils.XElement;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
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
import javax.xml.stream.XMLStreamException;

/**
 * The resource manager and locator class to
 * access resources by type and virtual path.
 * @author akarnokd, 2009.09.23.
 */
public class ResourceLocator {
	/** The directories and ZIP files that contain resources, order is inportant. */
	private final List<String> containers = new ArrayList<String>();
	/** The resource map from type to language to path. */
	public final Map<ResourceType, Map<String, Map<String, ResourcePlace>>> resourceMap = new HashMap<ResourceType, Map<String, Map<String, ResourcePlace>>>();
	/** The default language to use. */
	public String language;
	/** The pre-opened ZIP containers. */
	private final ThreadLocal<Map<String, ZipFile>> zipContainers = 
		new ThreadLocal<Map<String, ZipFile>>() {
		@Override
		protected Map<String, ZipFile> initialValue() {
			return new HashMap<String, ZipFile>();
		};
	};
	/** 
	 * Package-private. Use Configuration.newResourceLocator() instead.
	 * @param language the default language to use 
	 */
	ResourceLocator(String language) {
		this.language = language;
	}
	/**
	 * A concrete resource place.
	 * @author akarnokd, 2009.09.23.
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
					ZipFile zf = zipContainers.get().get(container);
					if (zf != null) {
						try {
							return zf.getInputStream(zf.getEntry(fileName));
						} catch (IllegalStateException ex) {
							zf = new ZipFile(container);
							zipContainers.get().put(container, zf);
							return zf.getInputStream(zf.getEntry(fileName));
						}
					} else {
						zf = new ZipFile(container);
						zipContainers.get().put(container, zf);
						return zf.getInputStream(zf.getEntry(fileName));
					}
				} else {
					return new FileInputStream(container + "/" + fileName);
				}
			} catch (IOException ex) {
				throw new RuntimeException(ex);
			} 
		}
		/** @return get the data as byte array */
		public byte[] get() {
			try {
				InputStream in = open();
				try {
					return IOUtils.load(in);
				} finally {
					in.close();
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
	void setContainers(Iterable<String> containers) {
		this.containers.clear();
		for (String s : containers) {
			this.containers.add(s);
		}
	}
	/**
	 * Scan the given resource containers for concrete files.
	 */
	void scanResources() {
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
			zipContainers.get().put(zipFile, zf);
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
		for (ZipFile zf : zipContainers.get().values()) {
			try {
				zf.close();
			} catch (IOException ex) {
				// ignored
			}
		}
	}
	/**
	 * Get resource for a language, type and resource name.
	 * @param resourceName the resource name with dash
	 * @param type the resource type
	 * @return the resource place
	 */
	public ResourcePlace get(String resourceName, ResourceType type) {
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
	void clear() {
		resourceMap.clear();
	}
	/**
	 * Get the given resource as image.
	 * @param resourceName the resource name, don't start it with slash
	 * @return the buffered image
	 */
	public BufferedImage getImage(String resourceName) {
		return getImage(resourceName, false);
	}
	/**
	 * Get the given resource as image.
	 * @param resourceName the resource name, don't start it with slash
	 * @param optional do not throw an AssertionError if the resource is missing?
	 * @return the buffered image or null if non-existent
	 */
	public BufferedImage getImage(String resourceName, boolean optional) {
		ResourcePlace rp = get(resourceName, ResourceType.IMAGE);
		if (rp == null) {
			if (!optional) {
				throw new AssertionError("Missing resource: " + language + " " + resourceName);
			}
			return null;
		}
		InputStream in = rp.open();
		try {
			BufferedInputStream bin = new BufferedInputStream(in, Math.max(8192, in.available()));
			try {
				return ImageIO.read(bin);
			} finally {
				try { bin.close(); } catch (IOException ex) { ex.printStackTrace(); }
			}
		} catch (IOException ex) {
			ex.printStackTrace();
			throw new AssertionError("Resource error" + language + " " + resourceName);
		} finally {
			try { in.close(); } catch (IOException ex) { ex.printStackTrace(); }
		}
	}
	/**
	 * Returns the given resource as byte data.
	 * @param resourceName the resource name.
	 * @return the byte data of the resource
	 */
	public byte[] getData(String resourceName) {
		ResourcePlace rp = get(resourceName, ResourceType.DATA);
		if (rp == null) {
			rp = get(resourceName, ResourceType.OTHER);
		}
		if (rp == null) {
			throw new AssertionError("Missing resource: " + language + " " + resourceName);
		}
		InputStream in = rp.open();
		try {
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			byte[] buffer = new byte[Math.max(8192, in.available())];
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
	 * @param resourceName the resource name omitting any leading slash.
	 * @return the element
	 */
	public XElement getXML(String resourceName) {
		try {
			ResourcePlace rp = get(resourceName, ResourceType.DATA);
			if (rp == null) {
				throw new AssertionError("Missing resource: " + language + " " + resourceName);
			}
			InputStream in = rp.open();
			try {
				BufferedInputStream bin = new BufferedInputStream(in, Math.max(8192, in.available()));
				try {
					return XElement.parseXML(bin);
				} finally {
					bin.close();
				}
			} finally {
				in.close();
			}
		} catch (IOException ex) {
			ex.printStackTrace();
			throw new AssertionError("Resource error" + language + " " + resourceName);
		} catch (XMLStreamException ex) {
			ex.printStackTrace();
			throw new AssertionError("Resource error" + language + " " + resourceName);
		}
	}
	/**
	 * Get a multi-phase animation by splitting the target image.
	 * @param name the button name
	 * @param width the phase width or -1 if not applicable
	 * @param step the number of steps or -1 if not applicable
	 * @return the array.
	 */
	public BufferedImage[] getAnimation(String name, int width, int step) {
		BufferedImage img = getImage(name);
		int n = width >= 0 ? img.getWidth() / width : step;
		int w = width >= 0 ? width : img.getWidth() / step;
		BufferedImage[] result = new BufferedImage[n];
		for (int i = 0; i < result.length; i++) {
			result[i] = ImageUtils.newSubimage(img, i * w, 0, w, img.getHeight());
		}
		return result;
	}
	
}
