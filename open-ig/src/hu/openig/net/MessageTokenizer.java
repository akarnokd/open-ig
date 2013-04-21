/*
 * Copyright 2008-2013, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.net;

import hu.openig.utils.U;

import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author akarnokd, 2013.04.21.
 *
 */
public class MessageTokenizer {
	/** The token types. */
	public enum TokenType {
		/** End of data. */
		EOF,
		/** Identifier. */
		IDENTIFIER,
		/** String. */
		STRING,
		/** Long number. */
		INTEGER,
		/** Double number. */
		DOUBLE,
		/** Symbol. */
		SYMBOL
	}
	/**
	 * The output token record.
	 * @author akarnokd, 2013.04.21.
	 */
	public static class Token {
		/** The type. */
		public final TokenType type;
		/** The value. */
		public final Object value;
		/**
		 * Constructor.
		 * @param type the type
		 * @param value the value
		 */
		public Token(TokenType type, Object value) {
			this.type = type;
			this.value = value;
		}
		@Override
		public String toString() {
			return type + ": \"" + value + "\"";
		}
	}
	/** The reader object. */
	protected Reader r;
	/**
	 * Constructor.
	 * @param r the reader to use
	 */
	public MessageTokenizer(Reader r) {
		this.r = r;
	}
	/**
	 * Process the entire stream and add the values to the list.
	 * @param out the output
	 * @throws IOException on error
	 */
	public void process(List<? super Token> out) throws IOException {
		TokenType tok = null;
		StringBuilder b = new StringBuilder();
		int v = r.read();
		
		while (v >= 0) {
			char c = (char)v;
			if (tok == null) {
				if (c == '"') {
					tok = TokenType.STRING;
				} else
				if (c == '-') {
					tok = TokenType.INTEGER;
					b.append(c);
				} else
				if (c == '.') {
					tok = TokenType.DOUBLE;
					b.append(c);
				} else
				if (Character.isDigit(c)) {
					tok = TokenType.INTEGER;
					b.append(c);
				} else
				if (Character.isJavaIdentifierStart(c)) {
					tok = TokenType.IDENTIFIER;
					b.append(c);
				} else 
				if (!Character.isWhitespace(c)) {
					out.add(new Token(TokenType.SYMBOL, String.valueOf(c)));
				}
			} else
			if (tok == TokenType.IDENTIFIER) {
				if (Character.isJavaIdentifierPart(c)) {
					b.append(c);
				} else {
					out.add(new Token(TokenType.IDENTIFIER, b.toString()));
					b.setLength(0);
					tok = null;
					continue;
				}
			} else
			if (tok == TokenType.DOUBLE) {
				if (Character.isDigit(c)) {
					b.append(c);
				} else {
					out.add(new Token(TokenType.DOUBLE, Double.valueOf(b.toString())));
					b.setLength(0);
					tok = null;
					continue;
				}
			} else
			if (tok == TokenType.INTEGER) {
				if (c == '.') {
					b.append(c);
					tok = TokenType.DOUBLE;
				} else
				if (Character.isDigit(c)) {
					b.append(c);
				} else {
					out.add(new Token(TokenType.INTEGER, Long.valueOf(b.toString())));
					b.setLength(0);
					tok = null;
					continue;
				}
			} else
			if (tok == TokenType.STRING) {
				if (c == '\\') {
					v = r.read();
					if (v < 0) {
						throw new EOFException();
					}
					c = (char)v;
					if (c == 'n') {
						b.append("\n");
					} else
					if (c == 'r') {
						b.append("\r");
					} else
					if (c == 't') {
						b.append("\t");
					} else {
						b.append(c);
					}
				} else
				if (c == '"') {
					out.add(new Token(TokenType.STRING, b.toString()));
					b.setLength(0);
					tok = null;
				} else {
					b.append(c);
				}
			}
			v = r.read();
		}
		
		if (tok != null) {
			switch (tok) {
			case DOUBLE:
				out.add(new Token(tok, Double.valueOf(b.toString())));
				break;
			case INTEGER:
				out.add(new Token(tok, Long.valueOf(b.toString())));
				break;
			case IDENTIFIER:
			case STRING:
				out.add(new Token(tok, b.toString()));
				break;
			default:
			}
		}
		out.add(new Token(TokenType.EOF, null));
	}
	/**
	 * Parses the entire source.
	 * @return the list of tokens
	 * @throws IOException on error
	 */
	public List<Token> parse() throws IOException {
		List<Token> result = new ArrayList<Token>();
		process(result);
		return result;
	}
	/**
	 * Test program.
	 * @param args no arguments
	 * @throws Exception on error
	 */
	public static void main(String[] args) throws Exception {
		StringReader r = new StringReader(
				"OBJECT { value=1, v=1.1, i=true, a=-1, b=.1, c=-.1, d=-1.1, array=[ \"str\\\"\" ] }");
		MessageTokenizer mt = new MessageTokenizer(r);
		List<Token> list = U.newArrayList();
		mt.process(list);
		
		for (Token e : list) {
			System.out.println(e);
		}
	}
}
