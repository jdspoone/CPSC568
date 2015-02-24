package casa;

/*
 * <p>Copyright: Copyright 2003-2014, Knowledge Science Group, University of Calgary.
 * Permission to use, copy, modify, distribute and sell this software and its
 * documentation for any purpose is hereby granted without fee, provided that
 * the above copyright notice appear in all copies and that both that copyright
 * notice and this permission notice appear in supporting documentation.  The
 * Knowledge Science Group makes no representations about the suitability of
 * this software for any purpose.  It is provided "as is" without express or
 * implied warranty.</p>
 *
 * @author <a href="http://cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 * @version 0.9
 */

public class TokenParser  {
	private static final int EOI = -1;
	private static final char EOIchar = Character.MIN_VALUE;
	/** Corresponds to a double quote character. */
	public final static char QUOTE = '"';
	/** Corresponds to a forward slash character. */
	//public final static char SLASH = '|'; //changed 2003/06/20 rck
	public final static char SLASH = '\\';
	/** Corresponds to a blank character. */
	public final static char BLANK = ' ';
	public final static char NEWLINE = '\n';
	public final static char TAB = '\t';
	public final static char RETURN = '\r'; 
	public final static char OPENPAREN = '(';
	public final static char CLOSEPAREN = ')';

	public String source;
	private int length;
	private int current;
	private boolean quotesIn=false; //used to signal that quotes should be left around a quoted string
	private boolean ignoreParenGroups = true; //used to signal that "(" should be returned as a token rather than used to match ")" to form a sequence.
	private boolean putBack = false;
	private String last = null;

	public TokenParser() {
		reset( "" );
	}

	public TokenParser( String string ) {
		reset( string );
	}

	public void reset( String string ) {
		this.source = string;
		this.length = string.length();
		this.current = 0;
	}

	public static String makeFit( String s ) {
		if (s==null) return s;
		String trim = s.trim();

		if (trim.startsWith("(") && trim.endsWith(")")) { //potentially a Lisp-like expression
			String parsed = new TokenParser(s).getNextTokenAcceptingParenGroups();
			if (parsed.length() == trim.length()) // definitely a Lisp=like expression
				return s;
			//but it could be that we have form like '(... "\n" ...)' which would be shorter...
			try {
				int credit = 0;
				String[] qsplit = trim.split("\"");
				for (int i=1, e=qsplit.length; i<e; i+=2) {
					credit += (qsplit[i].split("\\").length-1);
				}
				if (parsed.length()+credit == trim.length())
					return s;
			}
			catch (Throwable e) {}
		}

		if( ( s.indexOf( BLANK ) > -1 ) || // we should quote if its not a Lisp expression and it contains blank, quote, or slash
				( s.indexOf( QUOTE ) > -1 ) ||
				( s.indexOf( SLASH ) > -1 ) 
				&& (trim.startsWith("\"") && trim.endsWith("\""))) //and it's not already quoted
			return TokenParser.quoteString( s );
		else
			return s;
	}

	public static String concatString( String[] array ) {
		StringBuffer buffer = new StringBuffer();

		if( array.length > 0 ) {
			for( int i = 0; i < array.length; i++ ) {
				buffer.append( BLANK );
				buffer.append( makeFit( array[ i ] ) );
			}
			buffer.delete( 0, 1 );
		}
		return buffer.toString();
	}

	public static String quoteString( String s ) {
		StringBuffer buffer = new StringBuffer( s );

		for( int i = buffer.length(); i-- > 0; )
			switch( buffer.charAt( i ) ) {
			case QUOTE:
			case SLASH:
				buffer.insert( i, SLASH );
			}
		buffer.insert( 0, QUOTE );
		buffer.append( QUOTE );
		return buffer.toString();
	}

	public String getNextTokenQuotesIn() {
		quotesIn = true;
		String ret = getNextToken();
		quotesIn = false;
		return ret;
	}
	
	public String getNextTokenAcceptingParenGroups() {
		ignoreParenGroups = false;
		String ret = getNextToken();
		ignoreParenGroups = true;
		return ret;		
	}

	// IMPORTANT
	//
	// any <"> <,> and </> within a quoted string
	// MUST be preceeded by a SLASH </>. For example, the
	// string:
	//    "a slash '/', a comma ',' and a quote '"'"
	//
	// is represented as
	//    "a slash '//'/, a comma '/,' and a quote '/"'"
	//
	// For quoted strings always use the <quote> method
	//
	public String getNextToken() {
		if (putBack) {
			putBack = false;
			return last;
		}
		current = nextNonSpacePositionFrom( current );
		if( current == EOI )
			return null;

		StringBuffer buffer = new StringBuffer();
		
		//states
		boolean quote = false,
				    slash = false;
	  int     paren = 0;
	  
		loop:
			while( true ) {
				char c = getCharAt( current++ );

				switch( c ) {
				case EOIchar:
					current = EOI;
					break loop;

				default      : //handle the 3 modes in order: slash, quote, paren
					if( slash ) {
						slash = false;
						switch (c) {
						case 'n': buffer.append('\n'); break;
						case 't': buffer.append('\t'); break;
						default:  buffer.append( c );
						}
						continue;
					}
					if( quote ) {
						switch( c ) {
						case SLASH:
							slash = true;
							continue;

						case QUOTE:
							quote = false;
							if (quotesIn) buffer.append( c );
							continue;

						default    :
							buffer.append( c );
							continue;
						}
					}
					if (paren>0) {
						buffer.append(c);
						switch(c) {
						case OPENPAREN:
							paren++;
							break;
						case CLOSEPAREN:
							paren--;
							break;
						}
						continue;
					}
				}
				switch( c ) {
				case SLASH:
					slash = true;
					continue;

				case QUOTE:
					quote = true;
					if (quotesIn) buffer.append( c );
					continue;
					
				case OPENPAREN:
					buffer.append(c);
					if (ignoreParenGroups)
						break loop;
					paren++;
					continue;

				case BLANK:
				case NEWLINE:
				case TAB:
				case RETURN:
					break loop;

				default    :
					buffer.append( c );
				}
			}
		last = buffer.toString().trim();
		return last;
	}

	/**
	 *
	 * @return Rest of the source string starting with <code>current</code>
	 */
	public String getRemaining() {
		current = nextNonSpacePositionFrom( current );
		if( current == EOI ) return null;

		return source.substring( current );
	}

	/**
	 * "Puts back" the last token into the buffer so that the next call to
	 * getNextToken() will return the same token again.  Note that this
	 * method only works one level deep: only one token can be put back
	 * at any time.
	 */
	public void putback() {
		putBack = true;
	}

	private char getCharAt( int position ) {
		if( ( position != EOI ) &&
				( position < length ) )
			return source.charAt( position );
		else
			return EOIchar;
	}

	private int nextNonSpacePositionFrom( int position ) {
		if( position != EOI )
			while( length > position ) {
				char c = source.charAt( position );
				if(!(c==BLANK /*|| c==NEWLINE || c==RETURN || c==TAB*/))
					return position;
				else
					position++;
			}

		return EOI;
	}
}