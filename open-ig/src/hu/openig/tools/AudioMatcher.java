/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.tools;

import hu.openig.utils.IOUtils;
import hu.openig.utils.U;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author akarnokd, 2012.10.20.
 *
 */
public final class AudioMatcher {
	/** Utility class. */
	private AudioMatcher() { }

	/**
	 * @param args no arguments
	 * @throws Exception on error
	 */
	public static void main(String[] args) throws Exception {
		Map<String, byte[]> srcMap = new LinkedHashMap<>();
		Map<String, byte[]> dstMap = new LinkedHashMap<>();

		scanMessages(srcMap, dstMap);
		
		compare(srcMap, dstMap, "ani-sound", "ATVEZETO", "interlude");
		
	}

	/**
	 * @param srcMap the source map
	 * @param dstMap the destination map
	 */
	static void scanMessages(Map<String, byte[]> srcMap,
			Map<String, byte[]> dstMap) {
		File srcDir = new File("c:/games/IGHU/ATVEZETO");
		File dstDir = new File("audio/hu/interlude");
		
		File[] smps = srcDir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith("ani");
			}
		});
		
		File[] wavs = dstDir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith("wav");
			}
		});
		
		for (File smp : smps) {
			byte[] raw = OriginalConverter.extractWav(smp);
			// signify
			for (int i = 0; i < raw.length; i++) {
				raw[i] += 128;
			}
			srcMap.put(smp.getName(), raw);
		}

		for (File wav : wavs) {
			byte[] raw = IOUtils.load(wav);
			int idx = U.arrayIndexOf(raw, "data".getBytes(), 0);
			dstMap.put(wav.getName(), Arrays.copyOfRange(raw, idx + 8, raw.length));
		}
	}

	/**
	 * @param srcMap the source map
	 * @param dstMap the destination map
	 */
	static void scanSound(Map<String, byte[]> srcMap,
			Map<String, byte[]> dstMap) {
		File srcDir = new File("c:/games/IGHU/Sound");
		File dstDir = new File("audio/hu/ui");
		
		File[] smps = srcDir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith("smp.wav");
			}
		});
		
		File[] wavs = dstDir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith("wav");
			}
		});
		
		for (File smp : smps) {
			srcMap.put(smp.getName(), IOUtils.load(smp));
		}

		for (File wav : wavs) {
			dstMap.put(wav.getName(), IOUtils.load(wav));
		}
	}

	/**
	 * @param srcMap the source map
	 * @param dstMap the destination map
	 * @param tagName the tag name
	 * @param srcDir the source directory name
	 * @param dstDir the destination directory name
	 */
	static void compare(Map<String, byte[]> srcMap,
			Map<String, byte[]> dstMap, String tagName, String srcDir, String dstDir) {
		for (Map.Entry<String, byte[]> e1 : srcMap.entrySet()) {
			boolean found = false;
			for (Map.Entry<String, byte[]> e2 : dstMap.entrySet()) {
				byte[] d1 = e1.getValue();
				byte[] d2 = e2.getValue();
				int len = Math.min(d1.length, d2.length);
				d1 = Arrays.copyOf(d1, len);
				d2 = Arrays.copyOf(d2, len);
				
				if (Arrays.equals(d1, d2)) {
					System.out.printf("<%s src='%s/%s' dst='%s/%s'/>%n", tagName, srcDir, e1.getKey(), dstDir, e2.getKey());
					found = true;
				}
			}
			if (!found) {
				System.err.println(e1.getKey());
			}
		}
	}

}
