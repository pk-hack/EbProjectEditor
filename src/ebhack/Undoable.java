/*
 * Created on Jul 13, 2003
 */
package ebhack;

/**
 * Marks a class as being able to undo actions.
 * @author AnyoneEB
 */
public interface Undoable
{
	/**
	 * Adds an undo point now.
	 */
	public void addUndo();
	/**
	 * Undo to most recent undo point.
	 */
	public void undo();
}
