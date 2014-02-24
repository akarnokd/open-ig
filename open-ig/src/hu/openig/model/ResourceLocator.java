/*
 * Copyright 2008-2013, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.core.ResourceType;
import hu.openig.utils.Exceptions;
import hu.openig.utils.IOUtils;
import hu.openig.utils.ImageUtils;
import hu.openig.utils.XElement;

import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
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
import java.util.Locale;
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
	private final List<String> containers = new ArrayList<>();
	/** The resource map from type to language to path. */
	public final Map<ResourceType, Map<String, Map<String, ResourcePlace>>> resourceMap = new HashMap<>();
	/** The default language to use. */
	public String language;
	/** The pre-opened ZIP containers. */
	private final ThreadLocal<Map<String, ZipFile>> zipContainers = 
		new ThreadLocal<Map<String, ZipFile>>() {
		@Override
		protected Map<String, ZipFile> initialValue() {
			return new HashMap<>();
		}
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
					}
					zf = new ZipFile(container);
					zipContainers.get().put(container, zf);
					return zf.getInputStream(zf.getEntry(fileName));
				}
				return new FileInputStream(container + "/" + fileName);
			} catch (IOException ex) {
				throw new RuntimeException(ex);
			} 
		}
		/** @return open a completely new input stream to the given resource (to avoid sharing a single zip file.) */
		public InputStream openNew() {
			try {
				if (zipContainer) {
					final ZipFile zf = new ZipFile(container);
					final InputStream in = zf.getInputStream(zf.getEntry(fileName));
					BufferedInputStream bin = new BufferedInputStream(in) {
						@Override
						public void close() throws IOException {
							super.close();
							zf.close();
						}
					};
					return bin;
				}
				return new FileInputStream(container + "/" + fileName);
			} catch (IOException ex) {
				throw new RuntimeException(ex);
			} 
			
		}
		/** @return get the data as byte array */
		public byte[] get() {
			try (InputStream in = open()) {
				return IOUtils.load(in);
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
		/** @return the original filename within the container. */
		public String getFileName() {
			return fileName;
		}
		/** @return the resource type. */
		public ResourceType type() {
			return this.type;
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
				if (c.toLowerCase(Locale.ENGLISH).endsWith(".zip") && !new File(c).isDirectory()) {
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
		try (ZipFile zf = new ZipFile(zipFile)) {
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
				
				setNameParts(name, rp);
			}
		} catch (IOException ex) {
			Exceptions.add(ex);
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
		
		rp.fileName = name;
		
		name = name.substring(idx + 1);
		
		if (!setNameType(name, "png", ResourceType.IMAGE, rp)) {
			if (!setNameType(name, "wav", ResourceType.AUDIO, rp)) {
				if (!setNameType(name, "ogg", ResourceType.AUDIO, rp)) {
					if (!setNameType(name, "sub", ResourceType.SUBTITLE, rp)) {
						if (!setNameType(name, "ani.gz", ResourceType.VIDEO, rp)) {
							if (!setNameType(name, "xml", ResourceType.DATA, rp)) {
								rp.name = name;
								rp.type = ResourceType.OTHER;
							}
						}
					}
				}
			}
		}
		addResourcePlace(rp);
	}
	/**
	 * Set the name and type if the filename matches the given extension.
	 * @param name the filename
	 * @param ext the extension
	 * @param type the type
	 * @param out the output
	 * @return true if matches
	 */
	static boolean setNameType(String name, String ext, ResourceType type, ResourcePlace out) {
		if (name.toLowerCase(Locale.ENGLISH).endsWith(ext.toLowerCase(Locale.ENGLISH))) {
			name = name.substring(0, name.length() - ext.length());
			if (name.endsWith(".")) {
				name = name.substring(0, name.length() - 1);
			}
			out.type = type;
			out.name = name;
			return true;
		}
		return false;
	}
	/**
	 * @param rp add a resource place.
	 */
	private void addResourcePlace(ResourcePlace rp) {
		Map<String, Map<String, ResourcePlace>> res = resourceMap.get(rp.type);
		if (res == null) {
			res = new HashMap<>();
			resourceMap.put(rp.type, res);
		}
		Map<String, ResourcePlace> rps = res.get(rp.language);
		if (rps == null) {
			rps = new HashMap<>();
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
		try (InputStream in = rp.open();
			BufferedInputStream bin = new BufferedInputStream(in, Math.max(8192, in.available()))) {
			return optimizeImage(ImageIO.read(bin));
		} catch (IOException ex) {
			Exceptions.add(ex);
			throw new AssertionError("Resource error" + language + " " + resourceName);
		}
	}
	/**
	 * Convert the image into a compatible format for local rendering.
	 * @param img the bufferedImage
	 * @return the converted image
	 */
	BufferedImage optimizeImage(BufferedImage img) {
		BufferedImage img2 = GraphicsEnvironment.getLocalGraphicsEnvironment()
				.getDefaultScreenDevice().getDefaultConfiguration()
				.createCompatibleImage(
					img.getWidth(),
					img.getHeight(),
					img.getColorModel().getTransparency()
//					img.getColorModel().hasAlpha() ? Transparency.BITMASK
//						: Transparency.OPAQUE
						);
			Graphics2D g = img2.createGraphics();
			g.drawImage(img, 0, 0, null);
			g.dispose();
		 
			return img2;
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
		try (InputStream in = rp.open()) {
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			byte[] buffer = new byte[Math.max(8192, in.available())];
			int read;
			do {
				read = in.read(buffer);
				if (read > 0) {
					bout.write(buffer, 0, read);
				}
			} while (read >= 0);
			return bout.toByteArray();
		} catch (IOException ex) {
			Exceptions.add(ex);
			throw new AssertionError("Resource error" + language + " " + resourceName);
		}
				
	}
	/**
	 * Get a list of resources of a given path.
	 * @param language the target language
	 * @param path the path
	 * @return the list of resources
	 */
	public List<ResourcePlace> list(String language, String path) {
		List<ResourcePlace> result = new ArrayList<>();
		Set<String> rs = new HashSet<>();
		for (Map<String, Map<String, ResourcePlace>> e : resourceMap.values()) {
			for (String s : new String[] { language, "generic" }) {
				Map<String, ResourcePlace> e1 = e.get(s);
				if (e1 != null) {
					for (Map.Entry<String, ResourcePlace> e2 : e1.entrySet()) {
						String resid = e2.getKey();
						if (resid.startsWith(path)) {
							if (rs.add(resid)) {
								result.add(e2.getValue());
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
		Set<String> result = new HashSet<>();
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
		return new ArrayList<>(result);
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
			try (InputStream in = rp.open();
					BufferedInputStream bin = new BufferedInputStream(in, Math.max(8192, in.available()))) {
				return XElement.parseXML(bin);
			}
		} catch (IOException | XMLStreamException ex) {
			Exceptions.add(ex);
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
