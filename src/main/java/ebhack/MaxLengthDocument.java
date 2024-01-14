package ebhack;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

/**
 * A <code>PlainDocument</code> that limits the number of characters that can be entered.
 * Use in: <code>new JTextField().setDocument(new MaxLengthDocument(4))</code>
 * 
 * @author AnyoneEB
 */
public class MaxLengthDocument extends PlainDocument
{
	private int maxLength;
	
	/**
	 * Creates a new <code>MaxLengthDocument</code> with the given maximum length.
	 * 
	 * @param maxLength The maximum number of characters that can be entered.
	 */
	public MaxLengthDocument(int maxLength)
	{
		this.maxLength = maxLength;
	}
	public void insertString(int offs, String str, AttributeSet a)
		throws BadLocationException
	{
		if (str == null)
		{
			return;
		}
		if (this.getLength() + str.length() > maxLength)
		{
			if (str.length() > 1)
			{
				str = str.substring(0, maxLength - this.getLength());
			}
			else
			{
				return;
			}
		}
		super.insertString(offs, str, a);
	}
	public int getMaxLength()
	{
		return this.maxLength;
	}
	public void setMaxLength(int ml)
	{
	    this.maxLength = ml;
	    if(this.getLength() >ml)
	    {
	        try
            {
                this.remove(ml, this.getLength() - ml);
            }
            catch (BadLocationException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
	    }
	}
}