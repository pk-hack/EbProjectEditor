package ebhack;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.util.ArrayList;

/**
 * A <code>LineNumberReader</code> that doesn't return text after comment symbols.
 * By default the only comment symbol is "#". Comment symbols are <code>String</code>s
 * of any length.
 * 
 * @author AnyoneEB
 * @see LineNumberReader
 */
public class CommentedLineNumberReader extends LineNumberReader
{
	private String[] comments = { "#" };

	/** 
	 * Reads a line with comments removed.
	 * 
	 * @return The next line in the file with the comments removed
	 * @see java.io.BufferedReader#readLine()
	 * @throws IOException
	 */
	public String readLine() throws IOException
	{
		String out = super.readLine();
		if (out != null)
		{
			for (int i = 0; i < comments.length; i++)
			{
				int place = out.indexOf(comments[i]); //place to cut off
				if (place != -1) //if -1 then comments[i] isn't in the String
					out = out.substring(0, place);
			}
		}
		return out;
	}

	/**
	 * Reads a line with comments removed until a used line is found.
	 * A used line is one where <code>line.trim().length() > 0</code>.
	 * 
	 * @return The next line with text on it <code>trim()</code>'d
	 * @throws IOException
	 */
	public String readUsedLine() throws IOException
	{
		String out = this.readLine();
		if (out == null || (out = out.trim()).length() > 0)
		{
			return out;
		}
		else
		{
			return this.readUsedLine();
		}
	}

	/**
	 * Reads the rest of the file into a <code>String[]</code>.
	 * If you want the whole file, make sure you haven't called
	 * <code>readLine()</code> (or <code>readUsedLine()</code>) on this.
	 * 
	 * @param readUnused If true {@link #readLine()} is used to read lines, else {@link #readUsedLine} is used
	 * @return A <code>String[]</code> with each element being a line in the file 
	 * @throws IOException
	 */
	public String[] readLines(boolean readUnused) throws IOException
	{
		ArrayList al = new ArrayList();
		String tempStr;
		while ((tempStr = (readUnused ? this.readLine() : this.readUsedLine()))
			!= null)
		{
			al.add(tempStr);
		}
		return (String[]) al.toArray(new String[al.size()]);
	}
	
	/**
	 * Reads the rest of the file into a <code>String[]</code>.
	 * If you want the whole file, make sure you haven't called
	 * <code>readLine()</code> (or <code>readUsedLine()</code>) on this.
	 * Same as calling <code>readLines(true)</code>.
	 * 
	 * @return A <code>String[]</code> with each element being a line in the file 
	 * @throws IOException
	 * @see #readLines(boolean)
	 */
	public String[] readLines() throws IOException
	{
		return readLines(true);
	}
	
	/**
	 * Reads the rest of the file into a <code>String[]</code>.
	 * If you want the whole file, make sure you haven't called
	 * <code>readLine()</code> (or <code>readUsedLine()</code>) on this.
	 * Same as calling <code>readLines(false)</code>.
	 * 
	 * @return A <code>String[]</code> with each element being a used line in the file 
	 * @throws IOException
	 * @see #readLines(boolean)
	 */
	public String[] readUsedLines() throws IOException
	{
		return readLines(false);
	}

	/**
	 * Creates a new CommentedLineNumberReader from the specified file with the specified comment markers.
	 * 
	 * @param f <code>File</code> to read from.
	 * @param c <code>String[]</code> of comment indicators
	 * @throws FileNotFoundException
	 */
	public CommentedLineNumberReader(File f, String[] c)
		throws FileNotFoundException
	{
		super(new FileReader(f));
		this.comments = c;
	}

	/**
	 * Creates a new CommentedLineNumberReader from the specified file with "#" as the comment marker.
	 * 
	 * @param f <code>File</code> to read from.
	 * @throws FileNotFoundException
	 */
	public CommentedLineNumberReader(File f) throws FileNotFoundException
	{
		super(new FileReader(f));
	}

	/**
	 * Creates a new CommentedLineNumberReader from the specified file with the specified comment markers.
	 * 
	 * @param f Path of file to read from
	 * @param c <code>String[]</code> of comment indicators
	 * @throws FileNotFoundException
	 */
	public CommentedLineNumberReader(String f, String[] c)
		throws FileNotFoundException
	{
		super(new FileReader(f));
		this.comments = c;
	}

	/**
	 * Creates a new CommentedLineNumberReader from the specified file with "#" as the comment marker.
	 * 
	 * @param f Path of file to read from
	 * @throws FileNotFoundException
	 */
	public CommentedLineNumberReader(String f) throws FileNotFoundException
	{
		super(new FileReader(f));
	}

	/**
	 * Creates a new CommentedLineNumberReader from the specified Reader with "#" as the comment marker.
	 * 
	 * @param r <code>Reader</code> to read from
	 */
	public CommentedLineNumberReader(Reader r)
	{
		super(r);
	}
}
