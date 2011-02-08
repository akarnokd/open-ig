/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */
package hu.openig.xold.res;

import hu.openig.xold.core.Errors;
import hu.openig.xold.core.Messages;
import hu.openig.utils.PACFile;
import hu.openig.utils.ResourceMapper;
import hu.openig.utils.PACFile.PACEntry;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages the contents of the TEXT.PAC file.
 * @author karnokd
 */
public class Texts {
	/** Maps building integers to building names. */
	public final Map<Integer, String> buildingName = new HashMap<Integer, String>();
	/** Maps building integers to building information lines. */
	public final Map<Integer, String[]> buildingInfo = new HashMap<Integer, String[]>();
	/** Maps equipment indexes to equipment names. */ 
	public final Map<Integer, String> equipmentName = new HashMap<Integer, String>();
	/** Maps equipment indexes to equipment information lines. */
	public final Map<Integer, String[]> equipmentInfo = new HashMap<Integer, String[]>();
	/** The map of error enumerations to error descriptions. */
	public final Map<Errors, String> errorTexts = new HashMap<Errors, String>();
	/** The map of messages to messate descriptions. */
	public final Map<Messages, String> messageTexts = new HashMap<Messages, String>();
	/**
	 * Constructor. Loads the text entries.
	 * @param resMap the resource mapper object
	 */
	public Texts(ResourceMapper resMap) {
		Map<String, PACEntry> entries = PACFile.mapByName(PACFile.parseFully(resMap.get("DATA/TEXT.PAC")));
		try {
			int idx = 0;
			// load building names
			for (String name : fixHungarianChars(new String(entries.get("EPUL_NEV.TXT").data, "ISO-8859-1")).split("\r\n")) {
				buildingName.put(idx, name);
				idx++;
			}
			// load building information lines
			String[] sa = fixHungarianChars(new String(entries.get("EPUL_INF.TXT").data, "ISO-8859-1")).split("\r\n");
			for (idx = 0; idx < sa.length; idx++) {
				String[] lines = new String[] { "", "", "" };
				for (int j = 0; j < 3; j++) {
					if (idx * 3 + j < sa.length) {
						lines[j] = sa[idx * 3 + j];
					}
				}
				buildingInfo.put(idx, lines);
			}

			sa = fixHungarianChars(new String(entries.get("EQTXT.TXT").data, "ISO-8859-1")).split("\r\n");
			for (idx = 0; idx < sa.length; idx++) {
				String[] lines = new String[] { "", "", "" };
				for (int j = 0; j < 3; j++) {
					if (idx * 3 + j < sa.length) {
						lines[j] = sa[idx * 3 + j];
					}
				}
				equipmentInfo.put(idx, lines);
			}
			// load error texts
			idx = 0;
			for (String name : fixHungarianChars(new String(entries.get("HIBAK.TXT").data, "ISO-8859-1")).split("\r\n")) {
				int i = name.indexOf(' ', 1);
				errorTexts.put(Errors.MAP.get(Integer.parseInt(name.substring(0, i).trim())), name.substring(i + 1));
				idx++;
			}
			idx = 0;
			// load building names
			for (String name : fixHungarianChars(new String(entries.get("TAL_NEV.TXT").data, "ISO-8859-1")).split("\r\n")) {
				equipmentName.put(idx, name);
				idx++;
			}
			// load message texts
			idx = 0;
			for (String name : fixHungarianChars(new String(entries.get("UZENET.TXT").data, "ISO-8859-1")).split("\r\n")) {
				int i = name.indexOf(' ', 1);
				messageTexts.put(Messages.MAP.get(Integer.parseInt(name.substring(0, i).trim())), name.substring(i + 1));
				idx++;
			}
		} catch (UnsupportedEncodingException ex) {
			ex.printStackTrace();
		}
	}
	/**
	 * Corrects the classical IBM-437 representation of hungarian words with the correct unicode characters.
	 * @param s the string to fix
	 * @return the fixed string
	 */
	public static String fixHungarianChars(String s) {
		StringBuilder b = new StringBuilder(s.length());
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			switch (c) {
			case '\u00A0': c = '\u00E1'; break; // a with accute
			case '\u0082': c = '\u00E9'; break; // e with accute
			case '\u00A1': c = '\u00ED'; break; // i with accute
			case '\u00A2': c = '\u00F3'; break; // o with accute
			case '\u0094': c = '\u00F6'; break; // o with umlaut
			case '\u0093': c = '\u0151'; break; // o with double accute
			case '\u00A3': c = '\u00FA'; break; // u with accute
			case '\u0081': c = '\u00FC'; break; // u with umlaut
			case '\u0096': c = '\u0171'; break; // u with double accute
			
			case '\u008F': c = '\u00C1'; break; // A with accute
			case '\u0090': c = '\u00C9'; break; // E with accute
			case '\u00D6': c = '\u00CD'; break; // I with accute ???????????
			case '\u0095': c = '\u00D3'; break; // O with accute
			case '\u0099': c = '\u00D6'; break; // O with umlaut
			case '\u00A7': c = '\u0150'; break; // O with double accute ???????????
			case '\u0097': c = '\u00DA'; break; // U with accute ???????????
			case '\u009A': c = '\u00DC'; break; // U with umlaut
			case '\u0098': c = '\u0170'; break; // U with double accute
			default:
			}
			b.append(c);
		}
		return b.toString();
	}
}
