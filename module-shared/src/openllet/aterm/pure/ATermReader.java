package openllet.aterm.pure;

import java.io.IOException;
import java.io.Reader;

import openllet.aterm.ATerm;
import openllet.aterm.ParseError;
import openllet.atom.OpenError;

public class ATermReader
{
	private static final int INITIAL_TABLE_SIZE = 2048;
	private static final int TABLE_INCREMENT = 4096;

	private static final int INITIAL_BUFFER_SIZE = 1024;

	private final Reader reader;

	public int last_char;
	private int pos;

	private int nr_terms;
	private ATerm[] table;

	private char[] buffer;
	private int limit;
	private int bufferPos;

	public ATermReader(final Reader reader_)
	{
		this(reader_, INITIAL_BUFFER_SIZE);
	}

	public ATermReader(final Reader reader_, final int bufferSize)
	{
		reader = reader_;
		last_char = -1;
		pos = 0;

		if (bufferSize < INITIAL_BUFFER_SIZE)
			buffer = new char[bufferSize];
		else
			buffer = new char[INITIAL_BUFFER_SIZE];
		limit = -1;
		bufferPos = -1;
	}

	public void initializeSharing()
	{
		table = new ATerm[INITIAL_TABLE_SIZE];
		nr_terms = 0;
	}

	public void storeNextTerm(final ATerm t, final int size)
	{
		if (table == null)
			return;

		if (size <= PureFactory.abbrevSize(nr_terms))
			return;

		if (nr_terms == table.length)
		{
			final ATerm[] new_table = new ATerm[table.length + TABLE_INCREMENT];
			System.arraycopy(table, 0, new_table, 0, table.length);
			table = new_table;
		}

		table[nr_terms++] = t;
	}

	public ATerm getTerm(final int index)
	{
		if (index < 0 || index >= nr_terms)
			throw new OpenError("illegal index");
		return table[index];
	}

	public int read() throws IOException
	{
		if (bufferPos == limit)
		{
			limit = reader.read(buffer);
			bufferPos = 0;
		}

		if (limit == -1)
			last_char = -1;
		else
		{
			last_char = buffer[bufferPos++];
			pos++;
		}

		return last_char;
	}

	public int readSkippingWS() throws IOException
	{
		do
			last_char = read();
		while (Character.isWhitespace(last_char));

		return last_char;

	}

	public int skipWS() throws IOException
	{
		while (Character.isWhitespace(last_char))
			last_char = read();

		return last_char;
	}

	public int readOct() throws IOException
	{
		int val = Character.digit(last_char, 8);
		val += Character.digit(read(), 8);

		if (val < 0)
			throw new ParseError("octal must have 3 octdigits.");

		val += Character.digit(read(), 8);

		if (val < 0)
			throw new ParseError("octal must have 3 octdigits");

		return val;
	}

	public int getLastChar()
	{
		return last_char;
	}

	public int getPosition()
	{
		return pos;
	}
}
