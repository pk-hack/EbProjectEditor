package ebhack;

import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JComponent;

/**
 * Abstract class for a drawing area of any kind. If you want to draw images you
 * want {@link ImageDrawingArea} which extends this. This could also be used for
 * something like tile arrangement "drawing". Some methods may be moved from
 * ImageDrawingArea to this in the future when I make TileArrangementDrawingArea
 * for the tile editor (don't expect this too soon :). NOTE: Arrangement editing
 * is done with a simpler method using private internal classes.
 * 
 * @author AnyoneEB
 */
public abstract class DrawingArea extends JComponent implements MouseListener,
    MouseMotionListener, Undoable, CopyAndPaster
{
    /** The {@link DrawingArea.Toolset} used by this. */
    protected Toolset tools;
    /** The {@link DrawingArea.Palette} used by this. */
    protected Palette pal;

    /**
     * Interface to identify a palette of anything, not only colors.
     * 
     * @author AnyoneEB
     */
    public interface Palette
    {
        /**
         * Gets the selected item in this Palette. Named colorIndex to avoid
         * confusion with other methods.
         * 
         * @return The selected index.
         */
        public int getSelectedColorIndex();

        /**
         * Sets the selected item in this Palette. Named colorIndex to avoid
         * confusion with other methods.
         * 
         * @param c Item number to set
         */
        public void setSelectedColorIndex(int c);
    }

    /**
     * Interface to identify a toolset for a drawing area. Includes final ints
     * for identifying basic tools.
     * 
     * @author AnyoneEB
     */
    public interface Toolset
    {
        /** Basic tool constants. */
        public static final int TOOL_PENCIL = 1, TOOL_LINE = 2,
                TOOL_RECTANGLE = 3, TOOL_ROUND_RECTANGLE = 4, TOOL_OVAL = 5,
                TOOL_PAINT_BUCKET = 6, TOOL_EYEDROPER = 7, TOOL_SELECTION = 8;

        /** Fill types for (round) retangle and oval. Only draw outline. */
        public static final int FILL_NONE = 0; // only draw outline
        /**
         * Fill types for (round) retangle and oval. Fill with the background
         * color.
         */
        public static final int FILL_BACKGROUND = 1;
        /**
         * Fill types for (round) retangle and oval. Fill with the same color as
         * the outline.
         */
        public static final int FILL_OPQUE = 2;

        /** Fill in all directions. */
        public static final int FILL_METHOD_ALL = 0;
        /** Fill only in horizontal and vertical directions. */
        public static final int FILL_METHOD_NO_DIAGONALS = 1;

        /**
         * Returns an int identifying the selected drawing tool.
         * 
         * @return Selected drawing tool
         */
        public int getSelectedDrawingTool();

        /**
         * Returns an int identifying the selected fill type.
         * 
         * @return Selected fill type.
         */
        public int getFillType(); // get fill type for tools that apply

        /**
         * Returns an int identifying the selected fill method.
         * 
         * @return Selected fill method.
         */
        public int getFillMethod(); // get fill type for tools that apply

        /**
         * Returns true if transparent pixels should be treated as transparent
         * when dragging selections. This may be an user-selected option.
         * 
         * @return True if transparent pixels should be treated as transparent
         *         when dragging selections.
         */
        public boolean isTransparentSelection(); // for moving selections

        /**
         * Returns the radius to be used for rounded rectangles.
         * 
         * @return Radius to use for rounded rectangles
         */
        public int getRoundedRectRadius();
    }
    /** Zoom factor to display this at. One is actual size. */
    public float zoom = 1; // 1 = actual size
    /** Size of drawing. */
    public int drawingWidth, drawingHeight;

    // draw point at x, y using information from image
    /**
     * Draws point x, y to the screen using stored information.
     * 
     * @param x Coordinate
     * @param y Coordinate
     */
    public abstract void drawPoint(int x, int y);

    // set x, y on image to c and then call drawPoint x,y
    /**
     * Draws point x, y to the screen as "color" c and write that into stored
     * information.
     * 
     * @param x Coordinate
     * @param y Coordinate
     * @param c "color" to put at x, y.
     */
    public abstract void drawPoint(int x, int y, int c);

    // returns the int of the point x,y
    /**
     * Returns the "color" of point x, y.
     * 
     * @param x Coordinate
     * @param y Coordinate
     * @return "color" at x, y
     */
    public abstract int getPoint(int x, int y);

    /**
     * Returns the zoom.
     * 
     * @return float
     */
    public float getZoom()
    {
        return zoom;
    }

    /**
     * Sets the zoom.
     * 
     * @param zoom The zoom to set
     */
    public void setZoom(float zoom)
    {
        this.zoom = zoom;
    }

    /**
     * Returns the drawingHeight.
     * 
     * @return int
     */
    public int getDrawingHeight()
    {
        return drawingHeight;
    }

    /**
     * Returns the drawingWidth.
     * 
     * @return int
     */
    public int getDrawingWidth()
    {
        return drawingWidth;
    }

    /**
     * @see java.awt.Component#paint(Graphics)
     */
    public void paint(Graphics g)
    {
        super.paint(g);
    }

    /**
     * @see java.awt.event.MouseListener#mouseClicked(MouseEvent)
     */
    public void mouseClicked(MouseEvent me)
    {}

    /**
     * @see java.awt.event.MouseListener#mouseEntered(MouseEvent)
     */
    public void mouseEntered(MouseEvent me)
    {}

    /**
     * @see java.awt.event.MouseListener#mouseExited(MouseEvent)
     */
    public void mouseExited(MouseEvent me)
    {}

    /**
     * @see java.awt.event.MouseListener#mousePressed(MouseEvent)
     */
    public void mousePressed(MouseEvent me)
    {
    // start tool
    }

    /**
     * @see java.awt.event.MouseListener#mouseReleased(MouseEvent)
     */
    public void mouseReleased(MouseEvent me)
    {
    // actually apply changes
    }

    /**
     * @see java.awt.event.MouseMotionListener#mouseDragged(MouseEvent)
     */
    public void mouseDragged(MouseEvent me)
    {}

    /**
     * @see java.awt.event.MouseMotionListener#mouseMoved(MouseEvent)
     */
    public void mouseMoved(MouseEvent me)
    {}

    /**
     * Undo the last action.
     */
    public abstract void undo(); // undo last action

    /**
     * Add an undo point now.
     */
    public abstract void addUndo(); // add current point to undo

    protected ArrayList listeners = new ArrayList();

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.AbstractButton#addActionListener(java.awt.event.ActionListener)
     */
    public void addActionListener(ActionListener l)
    {
        listeners.add(l);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.AbstractButton#removeActionListener(java.awt.event.ActionListener)
     */
    public void removeActionListener(ActionListener l)
    {
        listeners.remove(l);
    }

    protected void fireActionPerformed(ActionEvent ae)
    {
        for (Iterator i = listeners.iterator(); i.hasNext();)
        {
            ((ActionListener) i.next()).actionPerformed(ae);
        }
    }

    /**
     * Consturctor for DrawingArea. Adds a MouseListener and a
     * MouseMotionListener to this.
     */
    public DrawingArea()
    {
        this.addMouseListener(this);
        this.addMouseMotionListener(this);
    }
}