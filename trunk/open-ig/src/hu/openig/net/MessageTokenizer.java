/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.net;

import hu.openig.net.MessageTokenizer.Token;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * The message tokenizer.
 * @author akarnokd, 2013.04.21.
 */
public class MessageTokenizer implements Iterable<Token> {
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
	public static final class Token {
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
	@Override
	public Iterator<Token> iterator() {
		return new TokenIterator();
	}
	/**
	 * A token iterator implementation.
	 * @author akarnokd, 2013.04.21.
	 */
	protected class TokenIterator implements Iterator<Token> {
		/** Has the cursor moved? */
		boolean moved;
		/** Is the source finished? */
		boolean done;
		/** The current token type. */
		TokenType tok;
		/** The builder for values. */
		StringBuilder b = new StringBuilder();
		/** The current token. */
		Token current;
		/** The last read character. */
		int v;
		/** Should the next move entry fetch a character? */
		boolean fetchNext;
		/** Constructor. Reads the first character. */
		protected TokenIterator() {
			fetchNext = true;
		}
		@Override
		public boolean hasNext() {
			if (!done) {
				if (!moved) {
					done = !move();
					moved = true;
				}
			}
			return !done;
		}
		@Override
		public Token next() {
			if (hasNext()) {
				moved = false;
				return current;
			}
			throw new NoSuchElementException();
		}
		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
		/** Fetches the next character. */
		void fetch() {
			fetchNext = false;
			try {
				v = r.read();
			} catch (IOException ex) {
				v = -1;
			}
		}
		/**
		 * Parse the next token.
		 * @return true if a token was extracted
		 */
		boolean move() {
			if (fetchNext) {
				fetch();
			}
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
						current = create(TokenType.SYMBOL, String.valueOf(c));
						fetchNext = true;
						return true;
					}
				} else
				if (tok == TokenType.IDENTIFIER) {
					if (Character.isJavaIdentifierPart(c)) {
						b.append(c);
					} else {
						current = create(TokenType.IDENTIFIER, b);
						clear();
						return true;
					}
				} else
				if (tok == TokenType.DOUBLE) {
					if (Character.isDigit(c)) {
						b.append(c);
					} else {
						current = create(TokenType.DOUBLE, b);
						clear();
						return true;
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
						current = create(TokenType.INTEGER, b);
						clear();
						return true;
					}
				} else
				if (tok == TokenType.STRING) {
					if (c == '\\') {
						fetch();
						if (v < 0) {
							break;
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
						current = create(TokenType.STRING, b);
						clear();
						fetchNext = true;
						return true;
					} else {
						b.append(c);
					}
				}
				fetch();
			}
			
			if (tok != null) {
				current = create(tok, b);
				clear();
				return true;
			}
			if (current == null || current.type != TokenType.EOF) {
				current = create(TokenType.EOF, null);
				return true;
			}
			return false;
		}
		/** Clear the current token and buffer. */
		private void clear() {
			tok = null;
			b.setLength(0);
		}
	}
	/**
	 * Process the entire stream and add the values to the list.
	 * @param out the output
	 * @throws IOException on error
	 */
	public void process(List<? super Token> out) throws IOException {
		for (Token t : this) {
			out.add(t);
		}
	}
	/**
	 * Creates a token with the given type and value.
	 * @param tok the token
	 * @param value the value as string
	 * @return the token
	 */
	static Token create(TokenType tok, CharSequence value) {
		switch (tok) {
		case DOUBLE:
			return (new Token(tok, Double.valueOf(value.toString())));
		case INTEGER:
			return (new Token(tok, Long.valueOf(value.toString())));
		case IDENTIFIER:
		case STRING:
		case SYMBOL:
			return (new Token(tok, value.toString()));
		case EOF:
			return new Token(tok, null);
		default:
			throw new IllegalArgumentException(tok + " " + value);
		}
	}
	/**
	 * Parses the entire source.
	 * @return the list of tokens
	 * @throws IOException on error
	 */
	public List<Token> parse() throws IOException {
		List<Token> result = new ArrayList<>();
		process(result);
		return result;
	}
	/**
	 * Test program.
	 * @param args no arguments
	 * @throws Exception on error
	 */
	public static void main(String[] args) throws Exception {
		try (StringReader r = new StringReader(
				"OBJECT { value=1, v=1.1, i=true, a=-1, b=.1, c=-.1, d=-1.1, array=[ \"str\\\"\" ] }")) {

			/*
			MessageTokenizer mt = new MessageTokenizer(r);
			List<Token> list = new ArrayList<>();
			mt.process(list);
			
			for (Token e : list) {
				System.out.println(e);
			}
			*/
			System.out.println("-----");
	        for (Object o : new MessageTokenizer(r)) {
	            System.out.println(o);
	        }
		}
	}
}
