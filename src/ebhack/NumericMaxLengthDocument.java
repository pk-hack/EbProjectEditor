/*
 * Created on Aug 26, 2004
 */
package ebhack;

import java.util.regex.Pattern;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;

/**
 * Limited length <code>Document</code> that can only contain numbers.
 * 
 * @author AnyoneEB
 * @see net.starmen.pkhack.MaxLengthDocument
 */
public class NumericMaxLengthDocument extends MaxLengthDocument
{
    private Pattern p;

    /**
     * Creates a new <code>Document</code> that can never be more than
     * <code>maxLength</code> characters long and may only hold decimal
     * digits.
     * 
     * @param maxLength maximum character length of this document
     */
    public NumericMaxLengthDocument(int maxLength)
    {
        this(maxLength, null);
    }

    /**
     * Creates a new <code>Document</code> that can never be more than
     * <code>maxLength</code> characters long and may not hold characters
     * matched by <code>pattern</code>.
     * 
     * @param maxLength maximum character length of this document
     * @param pattern a regular expression pattern which should match any
     *            <em>single</em> character that may not be in this document.
     *            The default is "[^\\d]".
     */
    public NumericMaxLengthDocument(int maxLength, String pattern)
    {
        super(maxLength);
        if (pattern == null)
            pattern = "[^\\d]";
        p = Pattern.compile(pattern + "+");
        try
        {
            insertString(0, "0", new SimpleAttributeSet());
        }
        catch (BadLocationException e)
        {}
    }

    public void insertString(int offs, String str, AttributeSet a)
        throws BadLocationException
    {
        while (this.getLength() > 0 && this.getText(0, 1).equals("0"))
        {
            super.remove(0, 1);
            if (offs > 0)
                offs--;
        }
        super.insertString(offs, p.matcher(str).replaceAll(""), a);
        //remove leading zeros
        if (this.getLength() > 1 && this.getText(0, 1).equals("0"))
            remove(0, 1);
        else if (this.getLength() == 0)
            super.insertString(0, "0", new SimpleAttributeSet());
    }

    public void remove(int offs, int len) throws BadLocationException
    {
        super.remove(offs, len);
        if (this.getLength() == 0)
            super.insertString(0, "0", new SimpleAttributeSet());
        else if (this.getLength() > 1 && this.getText(0, 1).equals("0"))
            remove(0, 1);
    }
}