package org.ohdsi.sql;
import java.util.ArrayList;
import java.util.List;

public class StringUtils {
	public static String replaceCharAt(String string, int pos, char ch) {
		return string.substring(0, pos) + ch + string.substring(pos + 1);
	}

	public static String replace(String string, int start, int end, String replacement) {
		if (end > string.length())
			return string.substring(0, start) + replacement;
		else
			return string.substring(0, start) + replacement + string.substring(end);
	}

	public static String replaceAll(String result, String search, String replace) {
		int pos = 0;
		while ((pos = result.indexOf(search, pos)) != -1) {
			result = replace(result, pos, pos + search.length(), replace);
			pos += replace.length();
		}
		return result;
	}

	public static class Token {
		public int		start;
		public int		end;
		public String	text;

		public Token(Token other) {
			start = other.start;
			end = other.end;
			text = other.text;
		}

		public Token() {
		};
	};

	/**
	 * Splits the SQL into tokens. Any alphanumeric (including underscore) sequence is considered a token. All other individual special characters are
	 * considered their own tokens. White space and SQL comments are not considered for tokens.
	 */
	public static List<Token> tokenizeSql(String sql) {
		List<Token> tokens = new ArrayList<Token>();
		int start = 0;
		int cursor = 0;
		boolean commentType1 = false; // Type 1: -- ... end of line
		boolean commentType2 = false; // Type 2: /* .. */
		for (; cursor < sql.length(); cursor++) {
			char ch = sql.charAt(cursor);
			if (commentType1) {
				if (ch == '\n') {
					commentType1 = false;
					start = cursor + 1;
				}
			} else if (commentType2) {
				if (ch == '/' && cursor > 0 && sql.charAt(cursor - 1) == '*') {
					commentType2 = false;
					start = cursor + 1;
				}
			} else if (!Character.isLetterOrDigit(ch) && ch != '_' && ch != '@') {
				if (cursor > start) {
					Token token = new Token();
					token.start = start;
					token.end = cursor;
					token.text = sql.substring(start, cursor);
					tokens.add(token);
				}
				if (ch == '-' && cursor < sql.length() && sql.charAt(cursor + 1) == '-') {
					commentType1 = true;
				} else if (ch == '/' && cursor < sql.length() && sql.charAt(cursor + 1) == '*') {
					commentType2 = true;
				} else if (!Character.isWhitespace(ch)) {
					Token token = new Token();
					token.start = cursor;
					token.end = cursor + 1;
					token.text = sql.substring(cursor, cursor + 1);
					tokens.add(token);
				}
				start = cursor + 1;
			}
		}

		if (cursor > start) {
			Token token = new Token();
			token.start = start;
			token.end = cursor;
			token.text = sql.substring(start, cursor);
			tokens.add(token);

		}
		return tokens;
	}

	// Safesplit works the same as default split, but takes escapes into account
	// Author: Martijn
	public static List<String> safeSplit(String string, char delimiter) {
		List<String> result = new ArrayList<String>();
		if (string.length() == 0) {
			result.add("");
			return result;
		}
		boolean literal = false;
		boolean escape = false;
		int startpos = 0;
		int i = 0;
		char currentchar;
		while (i < string.length()) {
			currentchar = string.charAt(i);
			if (currentchar == '"' && !escape) {
				literal = !literal;
			}
			if (!literal && (currentchar == delimiter && !escape)) {
				result.add(string.substring(startpos, i));
				startpos = i + 1;
			}
			if (currentchar == '\\') {
				escape = !escape;
			} else {
				escape = false;
			}
			i++;
		}
		result.add(string.substring(startpos, i));
		return result;
	}
}
