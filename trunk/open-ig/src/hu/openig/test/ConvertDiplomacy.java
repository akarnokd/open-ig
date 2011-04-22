/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.test;

import hu.openig.utils.PACFile;
import hu.openig.utils.PACFile.PACEntry;
import hu.openig.utils.XElement;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Convert diplomatic definition.
 * @author akarnokd, Apr 22, 2011
 */
public final class ConvertDiplomacy {

	/**
	 * Utility class. 
	 */
	private ConvertDiplomacy() {
	}

	/**
	 * @param args no arguments
	 * @throws Exception on error
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		
		Map<String, String> map = new LinkedHashMap<String, String>();

		XElement dipl = new XElement("diplomacy");
		Map<String, PACEntry> mapByName = PACFile.mapByName(PACFile.parseFully(new File("g:\\Games\\IGHU\\DATA\\TEXT.PAC ")));
		for (String s : mapByName.keySet()) {
			String st = fixChars(mapByName.get(s).data);
			if (st.contains("This is a template text fot Jason")) {
				dipl.add(parse(s, st, map));
			}
		}
		PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream("diplomacy_hu.xml"), "UTF-8"));
		out.println("<?xml version='1.0' encoding='UTF-8'?>");
		out.println(dipl.toString());
		out.close();
		
		out = new PrintWriter(new OutputStreamWriter(new FileOutputStream("labels_hu.xml"), "UTF-8"));
		out.println("<?xml version='1.0' encoding='UTF-8'?>");
		out.println("<labels>");
		for (Map.Entry<String, String> me : map.entrySet()) {
			out.printf("\t<entry key='%s'>%s</entry>%n", XElement.sanitize(me.getKey()), XElement.sanitize(me.getValue()));
		}
		out.println("</labels>");
		out.close();
		
		
		map.clear();
		dipl = new XElement("diplomacy");
		mapByName = PACFile.mapByName(PACFile.parseFully(new File("g:\\Games\\IG\\DATA\\TEXT.PAC ")));
		for (String s : mapByName.keySet()) {
			String st = fixChars(mapByName.get(s).data);
			if (st.contains("This is a template text fot Jason")) {
				dipl.add(parse(s, st, map));
			}
		}
		out = new PrintWriter(new OutputStreamWriter(new FileOutputStream("diplomacy_en.xml"), "UTF-8"));
		out.println("<?xml version='1.0' encoding='UTF-8'?>");
		out.println(dipl.toString());
		out.close();
		
		out = new PrintWriter(new OutputStreamWriter(new FileOutputStream("labels_en.xml"), "UTF-8"));
		out.println("<?xml version='1.0' encoding='UTF-8'?>");
		out.println("<labels>");
		for (Map.Entry<String, String> me : map.entrySet()) {
			out.printf("\t<entry key='%s'>%s</entry>%n", XElement.sanitize(me.getKey()), XElement.sanitize(me.getValue()));
		}
		out.println("</labels>");
		out.close();

	}
	/**
	 * Parse the plain diplomacy content.
	 * @param name the race name
	 * @param content the content
	 * @param map the label mapping
	 * @return the element
	 */
	static XElement parse(String name, String content, Map<String, String> map) {
		XElement result = new XElement("player");
		if (name.startsWith("ALIEN3")) {
			result.set("id", "Morgath");
		} else
		if (name.startsWith("ALIEN4")) {
			result.set("id", "Ychom");
		} else
		if (name.startsWith("ALIEN5")) {
			result.set("id", "Dribs");
		} else
		if (name.startsWith("ALIEN6")) {
			result.set("id", "Sullep");
		} else
		if (name.startsWith("ALIEN7")) {
			result.set("id", "Dargslan");
		} else
		if (name.startsWith("ALIEN8")) {
			result.set("id", "Ecalep");
		} else
		if (name.startsWith("ALIEN9")) {
			result.set("id", "FreeTraders");
		} else
		if (name.startsWith("ALIEN10")) {
			result.set("id", "FreeNations");
		}
		
		List<String> ts = split(content);
		XElement neg = result.add("negotiate");
		neg.set("type", "DIPLOMATIC_RELATIONS");
		XElement appr = null;
		
		for (int i = 1; i <= 3; i++) {
			appr = neg.add("approach");
			appr.set("type", "AGGRESSIVE");
			appr.content = label(result, neg, appr, i, ts.get(i - 1), map);
		}			
		for (int i = 4; i <= 6; i++) {
			appr = neg.add("approach");
			appr.set("type", "NEUTRAL");
			appr.content = label(result, neg, appr, i, ts.get(i - 1), map);
		}			
		for (int i = 7; i <= 9; i++) {
			appr = neg.add("approach");
			appr.set("type", "HUMBLE");
			appr.content = label(result, neg, appr, i, ts.get(i - 1), map);
		}			
		
		for (int i = 10; i <= 18; i++) {
			String e = ts.get(i - 1);
			XElement resp = neg.add("response");
			resp.set("type", "AGGRESSIVE");
			if (i < 13) {
				resp.set("mode", "YES");
			} else
			if (i < 16) {
				resp.set("mode", "MAYBE");
			} else {
				resp.set("mode", "NO");
			}
			if (e.startsWith("*")) {
				resp.set("notalk", "true");
				e = e.substring(1);
			}
			int idx = nonDigit(e);
			if (idx >= 0 && (e.startsWith("+") || e.startsWith("-"))) {
				resp.set("change", e.substring(0, idx));
				resp.content = label(result, neg, resp, i, e.substring(idx + 1), map);
			} else {
				resp.set("change", "0");
				resp.content = label(result, neg, resp, i, e, map);
			}
		}
		
		for (int i = 19; i <= 27; i++) {
			String e = ts.get(i - 1);
			XElement resp = neg.add("response");
			resp.set("type", "NEUTRAL");
			if (i < 22) {
				resp.set("mode", "YES");
			} else
			if (i < 25) {
				resp.set("mode", "MAYBE");
			} else {
				resp.set("mode", "NO");
			}
			if (e.startsWith("*")) {
				resp.set("notalk", "true");
				e = e.substring(1);
			}
			int idx = nonDigit(e);
			if (idx >= 0 && (e.startsWith("+") || e.startsWith("-"))) {
				resp.set("change", e.substring(0, idx));
				resp.content = label(result, neg, resp, i, e.substring(idx + 1), map);
			} else {
				resp.set("change", "0");
				resp.content = label(result, neg, resp, i, e, map);
			}
		}
		
		for (int i = 28; i <= 36; i++) {
			String e = ts.get(i - 1);
			XElement resp = neg.add("response");
			resp.set("type", "AGGRESSIVE");
			if (i < 31) {
				resp.set("mode", "YES");
			} else
			if (i < 34) {
				resp.set("mode", "MAYBE");
			} else {
				resp.set("mode", "NO");
			}
			if (e.startsWith("*")) {
				resp.set("notalk", "true");
				e = e.substring(1);
			}
			int idx = nonDigit(e);
			if (idx >= 0 && (e.startsWith("+") || e.startsWith("-"))) {
				resp.set("change", e.substring(0, idx));
				resp.content = label(result, neg, resp, i, e.substring(idx + 1), map);
			} else {
				resp.set("change", "0");
				resp.content = label(result, neg, resp, i, e, map);
			}
		}
		
		neg = result.add("negotiate");
		neg.set("type", "MONEY");
		appr = neg.add("approach");
		appr.content = label(result, neg, 37, ts.get(36), map);
		
		for (int i = 38; i <= 43; i++) {
			String e = ts.get(i - 1);
			XElement resp = neg.add("response");
			if (i < 41) {
				resp.set("mode", "YES");
			} else {
				resp.set("mode", "NO");
			}
			if (e.startsWith("*")) {
				resp.set("notalk", "true");
				e = e.substring(1);
			}
			int idx = nonDigit(e);
			if (idx >= 0 && (e.startsWith("+") || e.startsWith("-"))) {
				resp.set("change", e.substring(0, idx));
				resp.content = label(result, neg, resp, i, e.substring(idx + 1), map);
			} else {
				resp.set("change", "0");
				resp.content = label(result, neg, resp, i, e, map);
			}
		}
		
		neg = result.add("negotiate");
		neg.set("type", "TRADE");
		appr = neg.add("approach");
		appr.content = label(result, neg, 44, ts.get(43), map);
		
		for (int i = 45; i <= 50; i++) {
			String e = ts.get(i - 1);
			XElement resp = neg.add("response");
			if (i < 48) {
				resp.set("mode", "YES");
			} else {
				resp.set("mode", "NO");
			}
			if (e.startsWith("*")) {
				resp.set("notalk", "true");
				e = e.substring(1);
			}
			int idx = nonDigit(e);
			if (idx >= 0 && (e.startsWith("+") || e.startsWith("-"))) {
				resp.set("change", e.substring(0, idx));
				resp.content = label(result, neg, resp, i, e.substring(idx + 1), map);
			} else {
				resp.set("change", "0");
				resp.content = label(result, neg, resp, i, e, map);
			}
		}
		
		neg = result.add("negotiate");
		neg.set("type", "ALLY");
		appr = neg.add("approach");
		appr.content = label(result, neg, 51, ts.get(50), map);
		
		for (int i = 52; i <= 57; i++) {
			String e = ts.get(i - 1);
			XElement resp = neg.add("response");
			if (i < 55) {
				resp.set("mode", "YES");
			} else {
				resp.set("mode", "NO");
			}
			if (e.startsWith("*")) {
				resp.set("notalk", "true");
				e = e.substring(1);
			}
			int idx = nonDigit(e);
			if (idx >= 0 && (e.startsWith("+") || e.startsWith("-"))) {
				resp.set("change", e.substring(0, idx));
				resp.content = label(result, neg, resp, i, e.substring(idx + 1), map);
			} else {
				resp.set("change", "0");
				resp.content = label(result, neg, resp, i, e, map);
			}
		}
		
		neg = result.add("negotiate");
		neg.set("type", "DARGSLAN");
		appr = neg.add("approach");
		appr.content = label(result, neg, 58, ts.get(57), map);
		
		for (int i = 59; i <= 64; i++) {
			String e = ts.get(i - 1);
			XElement resp = neg.add("response");
			if (i < 62) {
				resp.set("mode", "YES");
			} else {
				resp.set("mode", "NO");
			}
			if (e.startsWith("*")) {
				resp.set("notalk", "true");
				e = e.substring(1);
			}
			int idx = nonDigit(e);
			if (idx >= 0 && (e.startsWith("+") || e.startsWith("-"))) {
				resp.set("change", e.substring(0, idx));
				resp.content = label(result, neg, resp, i, e.substring(idx + 1), map);
			} else {
				resp.set("change", "0");
				resp.content = label(result, neg, resp, i, e, map);
			}
		}
		
		neg = result.add("negotiate");
		neg.set("type", "SURRENDER");
		appr = neg.add("approach");
		appr.content = label(result, neg, 65, ts.get(64), map);
		
		for (int i = 66; i <= 71; i++) {
			String e = ts.get(i - 1);
			XElement resp = neg.add("response");
			if (i < 69) {
				resp.set("mode", "YES");
			} else {
				resp.set("mode", "NO");
			}
			if (e.startsWith("*")) {
				resp.set("notalk", "true");
				e = e.substring(1);
			}
			int idx = nonDigit(e);
			if (idx >= 0 && (e.startsWith("+") || e.startsWith("-"))) {
				resp.set("change", e.substring(0, idx));
				resp.content = label(result, neg, resp, i, e.substring(idx + 1), map);
			} else {
				resp.set("change", "0");
				resp.content = label(result, neg, resp, i, e, map);
			}
		}
		
		neg = result.add("terminate");
		neg.content = label(result, neg, 72, ts.get(71), map);

		neg = result.add("call");
		neg.set("type", "SURRENDER");
		for (int i = 73; i <= 75; i++) {
			appr = neg.add("approach");
			appr.content = label(result, neg, i, ts.get(i - 1), map);
		}
		
		neg = result.add("call");
		neg.set("type", "ALLIANCE");
		for (int i = 76; i <= 78; i++) {
			appr = neg.add("approach");
			appr.content = label(result, neg, i, ts.get(i - 1), map);
		}
		
		neg = result.add("call");
		neg.set("type", "PEACE");
		for (int i = 79; i <= 81; i++) {
			appr = neg.add("approach");
			appr.content = label(result, neg, i, ts.get(i - 1), map);
		}
		
		neg = result.add("call");
		neg.set("type", "MONEY");
		for (int i = 82; i <= 84; i++) {
			appr = neg.add("approach");
			appr.content = label(result, neg, i, ts.get(i - 1), map);
		}
		
		neg = result.add("call");
		neg.set("type", "WAR");
		for (int i = 85; i <= 87; i++) {
			appr = neg.add("approach");
			appr.content = label(result, neg, i, ts.get(i - 1), map);
		}
		
		neg = result.add("call");
		neg.set("type", "RESIGN");
		for (int i = 88; i <= 90; i++) {
			appr = neg.add("approach");
			appr.content = label(result, neg, i, ts.get(i - 1), map);
		}
		
		return result;
	}
	/** 
	 * Select the last non-letter character. 
	 * @param s the string
	 * @return the index
	 */
	static int nonDigit(String s) {
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c != '-' && c != '+' && c != ' ' && !Character.isDigit(c)) {
				return i - 1;
			}
		}
		return -1;
	}
	/**
	 * Create a label entry for the given raw content.
	 * @param race the race element
	 * @param neg the negotiation/call element
	 * @param appr the approach/response element
	 * @param i the index
	 * @param content the content
	 * @param map the map for the labels
	 * @return the key
	 */
	public static String label(XElement race, XElement neg, XElement appr, int i, String content, Map<String, String> map) {
		String key = "diplomacy."
			+ race.get("id")
			+ "."
			+ neg.get("type")
		;
		if (appr.has("type")) {
			key += "." + appr.get("type");
		}
		if (appr.has("mode")) {
			key += "." + appr.get("mode");
		}
		key += "." + i;
		content = content
		.replaceAll("\r\n", " ")
		.replaceAll("\\s+", " ")
		.replaceAll("zzzzzzz", "%s")
		.replaceAll("xxxxx", "%s")
		.replaceAll("aaaaa", "%s")
		.trim();
		
		if (map.put(key, content) != null) {
			System.err.printf("duplicate: %s = %s%n", key, content);
		}
		
		return key;
	}
	/**
	 * Create a label entry for the given raw content.
	 * @param race the race element
	 * @param neg the negotiation/call element
	 * @param i the index
	 * @param content the content
	 * @param map the map for the labels
	 * @return the key
	 */
	public static String label(XElement race, XElement neg, int i, String content, Map<String, String> map) {
		String key = "diplomacy."
			+ race.get("id")
		;
		if (neg.has("type")) {
			key += "." + neg.get("type");
		} else {
			key += "." + neg.name;
		}

		key += "." + i;
		content = content
		.replaceAll("\r\n", " ")
		.replaceAll("\\s+", " ")
		.replaceAll("zzzzzzz", "%s")
		.replaceAll("xxxxx", "%s")
		.replaceAll("aaaaa", "%s")
		.trim();
		
		if (content.startsWith("*")) {
			content = content.substring(1);
		}
		
		if (map.put(key, content) != null) {
			System.err.printf("duplicate: %s = %s%n", key, content);
		}
		
		return key;
	}
	/**
	 * Split the content by the @ symbols.
	 * @param content the content
	 * @return the talks
	 */
	static List<String> split(String content) {
		List<String> result = new ArrayList<String>();
		
		content = content.replaceAll("#.*?\r\n", "");
		
		int start = 0;
		do {
			int next = content.indexOf('@', start);
			if (next > 0) {
				result.add(content.substring(start, next));
			} else 
			if (next < 0) {
				result.add(content.substring(start));
				break;
			}
			start = next + 1;
		} while (true);
		
		return result;
	}
	/**
	 * Fix old dos characters to proper unicode characters.
	 * @param data the data
	 * @return the changed text
	 */
	static String fixChars(byte[] data) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < data.length; i++) {
			int c = data[i] & 0xFF;
			switch (c) {
			case 0x82: c = 'é';	break;
			case 0xA2: c = 'ó';	break;
			case 0xA0: c = 'á';	break;
			case 0x93: c = 'õ';	break;
			case 0x81: c = 'ü';	break;
			case 0x94: c = 'ö';	break;
			case 0xA1: c = 'í';	break;
			case 0xA3: c = 'ú'; break;
			case 0x97: c = 'Ú'; break;
			case 0x96: c = 'û'; break;
			case 0x99: c = 'Ö'; break;
			case 0x8F: c = 'Á'; break;
			case 0x90: c = 'É'; break;
			case 0x9A: c = 'Ü'; break;
			case 0x8D: c = 'Í'; break;
			default:
			}
			sb.append((char)c);
		}
		return sb.toString();
	}
}
