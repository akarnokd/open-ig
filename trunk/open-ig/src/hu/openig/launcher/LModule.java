/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.launcher;

import java.util.ArrayList;
import java.util.List;

/**
 * The launcher module definition.
 * @author akarnokd, 2010.10.31.
 */
public class LModule {
	/** The module id. */
	public String id;
	/** The version string. The format is 0.0[0].000 */
	public String version;
	/** The general information. */
	public final LInformation general = new LInformation();
	/** The release notes. */
	public final LInformation releaseNotes = new LInformation();
	/** The files required. */
	public final List<LFile> files = new ArrayList<>();
	/** The files to remove. */
	public final List<LRemoveFile> removeFiles = new ArrayList<>();
	/** The startup memory in megabytes. */
	public int memory;
	/** The optional class name to execute. */
	public String clazz;
	/**
	 * Compare the versions with another.
	 * For example:
	 * 0.8 &lt; 0.81 &lt 0.81.125 &lt; 0.9
	 * @param otherVersion the other version number
	 * @return -1 if this is smaller, 1 if this is larger, 0 if they are equal
	 */
	public int compareVersion(String otherVersion) {
		return compareVersion(version, otherVersion);
	}
	/**
	 * Compare the versions with another.
	 * For example:
	 * 0.8 &lt; 0.81 &lt 0.81.125 &lt; 0.9
	 * @param version current version
	 * @param otherVersion the other version number
	 * @return -1 if this is smaller, 1 if this is larger, 0 if they are equal
	 */
	public static int compareVersion(String version, String otherVersion) {
		String[] versionParts = version.split("\\.");
		String[] thatversionParts = otherVersion.split("\\.");
		int max = Math.max(versionParts.length, thatversionParts.length);
		int[] v1 = new int[max];
		int[] v2 = new int[max];
		
		versionToInt(versionParts, v1);
		versionToInt(thatversionParts, v2);
		
		for (int i = 0; i < v1.length; i++) {
			if (v1[i] < v2[i]) {
				return -1;
			} else
			if (v1[i] > v2[i]) {
				return 1;
			}
		}
		return 0;
	}
	/**
	 * @param versionParts the version number part strings
	 * @param v1 the version number integers
	 */
	private static void versionToInt(String[] versionParts, int[] v1) {
		if (versionParts.length > 0) {
			v1[0] = Integer.parseInt(versionParts[0]);
		}
		if (versionParts.length > 1) {
			v1[1] = Integer.parseInt(versionParts[1]);
			if (v1[1] < 10) {
				v1[1] *= 10;
			}
		}
		if (versionParts.length > 2) {
			v1[2] = Integer.parseInt(versionParts[2]);
			if (versionParts[2].length() < 3) {
				if (v1[2] < 10) {
					v1[2] *= 100;
				} else
				if (v1[2] < 100) {
					v1[2] *= 10;
				}
			}
		}
	}
}
