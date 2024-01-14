/*
 * Created on Jul 14, 2003
 */
package ebhack;

/**
 * Marks that the implementing class can copy and paste.
 * @author AnyoneEB
 */
public interface CopyAndPaster
{
	/** Copies the current selection to the clipboard. */
	public void copy();
	/** Pastes what's in the clipboard. No action if nothing copied. */
	public void paste();
	/** Deletes current selection. */
	public void delete();
	/** <code>copy()</code>'s, then <code>delete()</code>'s. */
	public void cut();
}
