/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.v1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author karnok, 2009.09.26.
 * @version $Revision 1.0$
 */
public class SubtitleManager {
	/** The subtitle entry. */
	public class SubEntry {
		/** Time start. */
		public long start;
		/** Time end. */
		public long end;
		/** Text. */
		public String text;
		/**
		 * Is the time within this entry.
		 * @param time the time
		 * @return true
		 */
		public boolean isIn(long time) {
			return start <= time && time <= end;
		}
	}
	/** The entry list. */
	final List<SubEntry> entries = new ArrayList<SubEntry>();
	/** The last time. */
	long lastTime;
	/** The last index. */
	int lastIndex;
	/** 
	 * Constructor.
	 * @param in the subtitle data to load
	 */
	public SubtitleManager(InputStream in) {
		try {
			BufferedReader bin = new BufferedReader(new InputStreamReader(in, "UTF-8"));
			try {
				String line = null;
				while ((line = bin.readLine()) != null) {
					if (line.length() > 19) {
						SubEntry e = new SubEntry(); // 00:00.000-00:00.000
						e.start = Integer.parseInt(line.substring(0, 2)) * 60 * 1000
						+ Integer.parseInt(line.substring(3, 5)) * 1000
						+ Integer.parseInt(line.substring(6, 9));
						e.end = Integer.parseInt(line.substring(10, 12)) * 60 * 1000
						+ Integer.parseInt(line.substring(13, 15)) * 1000
						+ Integer.parseInt(line.substring(16, 19));
						e.text = line.substring(19);
						entries.add(e);
					}
				}
			} finally {
				bin.close();
			}
		} catch (IOException ex) {
			
		}
	}
	/**
	 * Get a subtitle entry for the given time.
	 * @param time the time
	 * @return the text or null
	 */
	public String get(long time) {
		if (time < lastTime) {
			for (int i = lastIndex; i >= 0; i--) {
				SubEntry e = entries.get(i);
				if (e.isIn(time)) {
					lastIndex = i;
					lastTime = time;
					return e.text;
				}
			}
		} else {
			for (int i = lastIndex; i < entries.size(); i++) {
				SubEntry e = entries.get(i);
				if (e.isIn(time)) {
					lastIndex = i;
					lastTime = time;
					return e.text;
				}
			}
		}
		return null;
	}
}
