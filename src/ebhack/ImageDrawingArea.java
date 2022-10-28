package ebhack;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
import java.util.ArrayList;

/**
 * {@link DrawingArea}for editing images.
 * 
 * @author AnyoneEB
 */
public class ImageDrawingArea extends DrawingArea
{
    private BufferedImage img = null, selectImg; // img being edited
    /** {@link ColorPalette}used by this. */
    protected ColorPalette pal;

    /** Selection stuff. */
    protected int sx, sy, selx, sely; // x,y of start of mouse drag
    /** Current selection. */
    protected Rectangle selection = new Rectangle();
    private Clipboard cb;
    /** Internally used boolean values. */
    protected boolean wasDragged = false, isMakingSelect, drawGridlines = true;

    private ArrayList undoArr = new ArrayList();

    private String actionCommand = "ImageDrawingArea";

    /**
     * Interface to identify a {@link DrawingArea.Palette}of colors.
     * 
     * @author AnyoneEB
     */
    public interface ColorPalette extends Palette
    {
        /**
         * Returns the selected Color.
         * 
         * @return The currently selected Color.
         */
        public Color getSelectedColor();

        /**
         * Sets the selected Color to the specified Color. If the specified
         * Color is not a valid selection, unpredictable results will follow.
         * 
         * @param c Color
         */
        public void setSelectedColor(Color c);

        /**
         * Returns the Color the specified Color number represents.
         * 
         * @param c A Color number
         * @return A Color
         */
        public Color getColorOf(int c);

        /**
         * Returns the Color number which represents the specified Color.
         * 
         * @param c A Color
         * @return A Color number
         */
        public int getIndexOf(Color c);
    }

    /**
     * Clipboard for an ImageDrawingArea. You can use the get and setClipboard()
     * methods to make two ImageDrawingArea's share a clipboard.
     * 
     * @see ImageDrawingArea#getClipboard()
     * @see ImageDrawingArea#setClipboard(ImageDrawingArea.Clipboard)
     * @author AnyoneEB
     */
    public static class Clipboard
    {
        /** Image on clipboard. */
        protected BufferedImage img;
        /** Area copied from. */
        protected Rectangle size = new Rectangle();

        public Clipboard()
        {}

        /**
         * Returns true if this <code>Clipboard</code> is empty.
         * 
         * @return true if nothing has been copied onto this
         *         <code>Clipboard</code>
         */
        public boolean isClipboardEmpty()
        {
            return img == null;
        }

        /**
         * Returns the Image on the clipboard.
         * 
         * @return Image on clipboard.
         */
        public BufferedImage getImg()
        {
            return img;
        }

        /**
         * Returns the area copied from.
         * 
         * @return Area copied from.
         */
        public Rectangle getSize()
        {
            return size;
        }

        /**
         * Sets the image on the clipboard.
         * 
         * @param image New image to put on clipboard.
         */
        public void setImg(BufferedImage image)
        {
            img = image;
        }

        /**
         * Sets the area copied from.
         * 
         * @param rectangle New area copied from.
         */
        public void setSize(Rectangle rectangle)
        {
            size = rectangle;
        }

        /**
         * Puts the given values onto the clipboard.
         * 
         * @param image New image to put on clipboard.
         * @param rectangle New area copied from.
         */
        public void copy(BufferedImage image, Rectangle rectangle)
        {
            this.setImg(image);
            this.setSize(rectangle);
        }
    }

    /**
     * Returns this ImageDrawingArea's clipboard.
     * 
     * @see ImageDrawingArea.Clipboard
     * @return This ImageDrawingArea's clipboard.
     */
    public Clipboard getClipboard()
    {
        return this.cb;
    }

    /**
     * Sets this ImageDrawingArea's clipboard to the specified clipboard. Use
     * <code>imgDrawAreaA.setClipboard(imgDrawAreaB.getClipboard())</code> to
     * make imgDrawAreaA and imgDrawAreaB use the same clipboard.
     * 
     * @see ImageDrawingArea.Clipboard
     * @param newCb Clipboard to use as this ImageDrawArea's clipboard.
     */
    public void setClipboard(Clipboard newCb)
    {
        this.cb = newCb;
    }

    /** Resets graphics after image or zoom change. */
    protected void initGraphics()
    {
        this.drawingWidth = img.getWidth(this);
        this.drawingHeight = img.getHeight(this);

        int w = (int) (drawingWidth * zoom) + 1, h = (int) (drawingHeight * zoom) + 1;

        this.setPreferredSize(new Dimension(w, h));

        repaint();
        // Graphics g = this.getGraphics();
        // g.drawImage(img, 0, 0, w, h, Color.BLACK, this);
    }

    /**
     * Creates a new ImageDrawingArea showing the specified image.
     * 
     * @param img Image
     * @param tools Toolset
     * @param pal Palette
     * @see DrawingArea.Toolset
     * @see ImageDrawingArea.ColorPalette
     */
    public ImageDrawingArea(Image img, Toolset tools, ColorPalette pal)
    {
        super();

        this.img = (BufferedImage) img;
        this.zoom = 1;
        this.tools = tools;
        this.pal = pal;
        this.cb = new Clipboard();

        this.initGraphics();
    }

    /**
     * Creates a new ImageDrawingArea showing the specified image and zoom.
     * 
     * @param img Image
     * @param tools Toolset
     * @param pal Palette
     * @param zoom float of the zoom factor. 1.0 = 1x zoom
     * @see DrawingArea.Toolset
     * @see ImageDrawingArea.ColorPalette
     */
    public ImageDrawingArea(Image img, Toolset tools, ColorPalette pal,
        float zoom)
    {
        super();

        this.img = (BufferedImage) img;
        this.zoom = zoom;
        this.tools = tools;
        this.pal = pal;
        this.cb = new Clipboard();

        this.initGraphics();
    }

    /**
     * Creates a new ImageDrawingArea with no image.
     * 
     * @param tools Toolset
     * @param pal Palette
     * @see DrawingArea.Toolset
     * @see ImageDrawingArea.ColorPalette
     */
    public ImageDrawingArea(Toolset tools, ColorPalette pal)
    {
        super();

        this.tools = tools;
        this.pal = pal;
        this.cb = new Clipboard();
    }

    /**
     * Creates a new ImageDrawingArea with no image. Fires an ActionEvent when a
     * change is made.
     * 
     * @param tools Toolset
     * @param pal Palette
     * @param al ActionListener to tell about changes.
     * @see DrawingArea.Toolset
     * @see ImageDrawingArea.ColorPalette
     */
    public ImageDrawingArea(Toolset tools, ColorPalette pal, ActionListener al)
    {
        super();

        this.tools = tools;
        this.pal = pal;
        this.addActionListener(al);
        this.cb = new Clipboard();
    }

    /**
     * Changes the image and zoom to the specified image and zoom. Resets undo
     * and selection, but leaves clipboard.
     * 
     * @param img new image
     * @param zoom new zoom
     */
    public void setImage(Image img, float zoom)
    {
        this.img = (BufferedImage) img;
        this.zoom = zoom;

        this.undoArr.clear();
        this.selection = new Rectangle();
        // anything else to reset?

        this.initGraphics();
    }

    /**
     * Changes the image to the specified image.
     * 
     * @param img new image
     */
    public void setImage(Image img)
    {
        setImage(img, this.zoom);
    }

    /**
     * Returns the image this is editing.
     * 
     * @return The current image.
     */
    public Image getImage()
    {
        return this.img;
    }

    /**
     * @see net.starmen.pkhack.DrawingArea#drawPoint(int, int)
     */
    public void drawPoint(int x, int y)
    {
        this.drawPoint(x, y, this.getGraphics());
    }

    /**
     * Draws point x, y of the image using the provided <code>Graphics</code>
     * zoomed. Important note: this draws the point zoomed. Meaning it will draw
     * a filled rectangle of the size <code>zoom</code> x <code>zoom</code>
     * with the top-left corner at x*zoom, y*zoom.
     * 
     * @param x Coordinate on the image.
     * @param y Coordinate on the image.
     * @param g <code>Graphics</code> to draw to.
     */
    public void drawPoint(int x, int y, Graphics g)
    {
        g.setColor(getPointColor(x, y));
        g.fillRect((int) (x * zoom), (int) (y * zoom), (int) zoom, (int) zoom);
    }

    /**
     * @see net.starmen.pkhack.DrawingArea#drawPoint(int, int, int)
     */
    public void drawPoint(int x, int y, int c)
    {
        Graphics g = img.getGraphics();
        g.setColor(pal.getColorOf(c));
        g.drawLine(x, y, x, y);
        // this.drawPoint(x, y);
    }

    /**
     * Draws the specified Color at point x, y on the image. Note: this does not
     * draw anything to the screen. <code>repaint()</code> for that.
     * 
     * @param x Coordinate on the image.
     * @param y Coordinate on the image.
     * @param c Color
     */
    public void drawPoint(int x, int y, Color c)
    {
        Graphics g = img.getGraphics();
        g.setColor(c);
        g.drawLine(x, y, x, y);
        // this.drawPoint(x, y);
    }

    /**
     * @see net.starmen.pkhack.DrawingArea#getPoint(int, int)
     */
    public int getPoint(int x, int y)
    {
        return pal.getIndexOf(getPointColor(x, y));
    }

    /**
     * Returns the Color of point x, y on the image. Uses
     * <code>PixelGrabber</code> to get the exact color.
     * 
     * @param x Coordinate on the image.
     * @param y Coordinate on the image.
     * @return The Color of the specified point.
     */
    public Color getPointColor(int x, int y)
    {
        // return new Color(((BufferedImage) img).getRGB(x, y));

        int w = 1, h = 1;
        int[] pixels = new int[w * h];
        PixelGrabber pg = new PixelGrabber(img, x, y, w, h, pixels, 0, w);
        try
        {
            pg.grabPixels();
        }
        catch (InterruptedException e)
        {
            System.err.println("interrupted waiting for pixels!");
            return null;
        }
        int alpha, blue, red, green;

        alpha = (pixels[0] >> 24) & 0xff;
        red = (pixels[0] >> 16) & 0xff;
        green = (pixels[0] >> 8) & 0xff;
        blue = (pixels[0]) & 0xff;

        return new Color(red, green, blue, alpha);
    }

    public void undo()
    {
        undo(true);
    }

    public void undo(boolean notify)
    {
        if (!undoArr.isEmpty())
        {
            this.img = (BufferedImage) undoArr.get(undoArr.size() - 1);
            undoArr.remove(undoArr.size() - 1);
        }
        if (notify)
            fireChanged();
    }

    public void addUndo()
    {
        BufferedImage newImg = this.getNewImage(img); // copy img into newImg
        undoArr.add(img); // add old img to undo
        // add a copy of img to the array
        img = newImg; // set img to newImg
    }

    public void paint(Graphics g)
    {
        if (this.isEnabled())
        {
            if (img == null)
            {
                return;
            }
            if (this.drawingWidth * this.drawingHeight < 500)
            {
                for (int x = 0; x < this.drawingWidth; x++)
                {
                    for (int y = 0; y < this.drawingHeight; y++)
                    {
                        drawPoint(x, y, g);
                    }
                }
            }
            else
            {
                g.drawImage(img, 0, 0, getZoomedXY(drawingWidth),
                    getZoomedXY(drawingHeight),
                    // Color.BLACK,
                    this);
            }
            // selection image
            if (selection.width > 0 && selection.height > 0)
            {
                flattenSelection(g, true);
                // g.drawImage(
                // selectImg,
                // getZoomedXY(selection.x),
                // getZoomedXY(selection.y),
                // getZoomedXY(selection.width),
                // getZoomedXY(selection.height),
                // this);
                // g.setColor(Color.WHITE);
                // g.drawRect(
                // getZoomedXY(selection.x),
                // getZoomedXY(selection.y),
                // getZoomedXY(selection.width),
                // getZoomedXY(selection.height));
            }
            // gridlines
            if (this.isDrawGridlines())
            {
                g.setColor(Color.BLACK);
                for (int x = 0; x < this.drawingWidth; x++)
                {
                    g.drawLine(getZoomedXY(x), 0, getZoomedXY(x),
                        getZoomedXY(this.drawingHeight) - 1);
                }
                for (int y = 0; y < this.drawingHeight; y++)
                {
                    g.drawLine(0, getZoomedXY(y),
                        getZoomedXY(this.drawingWidth) - 1, getZoomedXY(y));
                }
                g.drawRect(0, 0, getZoomedXY(drawingWidth),
                    getZoomedXY(drawingHeight));
            }
            // selection border
            if (selection.width > 0 && selection.height > 0)
            {
                // flattenSelection(g, true);
                // g.drawImage(
                // selectImg,
                // getZoomedXY(selection.x),
                // getZoomedXY(selection.y),
                // getZoomedXY(selection.width),
                // getZoomedXY(selection.height),
                // this);
                g.setColor(Color.WHITE);
                g
                    .drawRect(getZoomedXY(selection.x),
                        getZoomedXY(selection.y), getZoomedXY(selection.width),
                        getZoomedXY(selection.height));
            }
        }
        else
        {
            g.setColor(Color.GRAY);
            g.fillRect(0, 0, this.getWidth(), this.getHeight());
            g.setColor(Color.RED);
            g.drawLine(0, 0, this.getWidth(), this.getHeight());
            g.drawLine(this.getWidth(), 0, 0, this.getHeight());
        }
    }

    public void setZoom(float zoom)
    {
        super.setZoom(zoom);
        if (img != null)
            initGraphics();
    }

    /**
     * Horizonally flips the image.
     */
    public void doHFlip()
    {
        addUndo();

        int w = img.getWidth(), h = img.getHeight();
        int[] pixels = new int[w * h];
        BufferedImage out = (BufferedImage) this.createImage(w, h);
        PixelGrabber pg = new PixelGrabber(img.getScaledInstance(w, h, 0), 0,
            0, w, h, pixels, 0, w);
        try
        {
            pg.grabPixels();
        }
        catch (InterruptedException e)
        {
            System.err.println("interrupted waiting for pixels!");
            return;
        }
        Graphics g = out.getGraphics();
        int alpha, blue, red, green;
        for (int j = 0; j < h; j++)
        {
            for (int i = 0; i < w; i++)
            {
                alpha = (pixels[j * w + i] >> 24) & 0xff;
                red = (pixels[j * w + i] >> 16) & 0xff;
                green = (pixels[j * w + i] >> 8) & 0xff;
                blue = (pixels[j * w + i]) & 0xff;

                g.setColor(new Color(red, green, blue, alpha));
                g.drawLine((w - 1) - i, j, (w - 1) - i, j);
            }
        }
        this.img = out;

        repaint();

        fireChanged();
    }

    /**
     * Vertically flips the image.
     */
    public void doVFlip()
    {
        addUndo();

        int w = img.getWidth(), h = img.getHeight();
        int[] pixels = new int[w * h];
        BufferedImage out = (BufferedImage) this.createImage(w, h);
        PixelGrabber pg = new PixelGrabber(img.getScaledInstance(w, h, 0), 0,
            0, w, h, pixels, 0, w);
        try
        {
            pg.grabPixels();
        }
        catch (InterruptedException e)
        {
            System.err.println("interrupted waiting for pixels!");
            return;
        }
        Graphics g = out.getGraphics();
        int alpha, blue, red, green;
        for (int j = 0; j < h; j++)
        {
            for (int i = 0; i < w; i++)
            {
                alpha = (pixels[j * w + i] >> 24) & 0xff;
                red = (pixels[j * w + i] >> 16) & 0xff;
                green = (pixels[j * w + i] >> 8) & 0xff;
                blue = (pixels[j * w + i]) & 0xff;

                g.setColor(new Color(red, green, blue, alpha));
                g.drawLine(i, (h - 1) - j, i, (h - 1) - j);
            }
        }
        this.img = out;

        repaint();

        fireChanged();
    }

    private void doPaintBucketReg(int x, int y, Color newCol, Color currentCol)
    {
        // make sure it's on the image
        if (x > -1 && y > -1 && x < this.getDrawingWidth()
            && y < this.getDrawingHeight()
            && this.getPointColor(x, y).equals(currentCol))
        {
            this.drawPoint(x, y, newCol);
            for (int ax = -1; ax < 2; ax++)
                for (int ay = -1; ay < 2; ay++)
                    doPaintBucketReg(x + ax, y + ay, newCol, currentCol);
        }
    }

    private void doPaintBucketNoDia(int x, int y, Color newCol, Color currentCol)
    {
        // make sure it's on the image
        if (x > -1 && y > -1 && x < this.getDrawingWidth()
            && y < this.getDrawingHeight()
            && this.getPointColor(x, y).equals(currentCol))
        {
            this.drawPoint(x, y, newCol);
            doPaintBucketNoDia(x - 1, y, newCol, currentCol);
            doPaintBucketNoDia(x + 1, y, newCol, currentCol);
            doPaintBucketNoDia(x, y - 1, newCol, currentCol);
            doPaintBucketNoDia(x, y + 1, newCol, currentCol);
        }
    }

    /**
     * Recursively fills an area of the same color with the current color.
     * 
     * @param x X-coordinate to start from.
     * @param y Y-coordinate to start from.
     */
    protected void doPaintBucket(int x, int y)
    {
        Color currentCol = this.getPointColor(x, y), newCol = pal
            .getSelectedColor();
        if (currentCol.equals(newCol))
        {
            undo();
            return;
        }
        if (tools.getFillMethod() == Toolset.FILL_METHOD_NO_DIAGONALS)
        {
            doPaintBucketNoDia(x, y, newCol, currentCol);
        }
        else
        {
            doPaintBucketReg(x, y, newCol, currentCol);
        }
    }

    /**
     * Takes the start and end of drag and responds to them based on the
     * selected tool.
     * 
     * @param x1 X-coordinate of drag start.
     * @param y1 Y-coordinate of drag start.
     * @param x2 X-coordinate of drag end.
     * @param y2 Y-coordinate of drag end.
     */
    protected void doTool(int x1, int y1, int x2, int y2)
    {
        // x1, y1 is start of drag
        // x2, y2 is end/current place of drag
        // addUndo();
        Graphics g = img.getGraphics();
        g.setColor(pal.getSelectedColor());

        int tool = tools.getSelectedDrawingTool(), fill = tools.getFillType();
        int rx1 = x1, ry1 = y1, rx2 = x2, ry2 = y2;
        if (x1 > x2)
        {
            rx1 = x2;
            rx2 = x1;
        }
        if (y1 > y2)
        {
            ry1 = y2;
            ry2 = y1;
        }
        switch (tool)
        {
            case Toolset.TOOL_EYEDROPER:
                pal.setSelectedColor(getPointColor(x2, y2));
                // undo();
                break;
            case Toolset.TOOL_PENCIL:
                // undo();
                g.drawLine(x2, y2, x2, y2);
                // this.drawLine(rx1, ry1, rx2, ry2,
                // pal.getSelectedColorIndex());
                break;
            case Toolset.TOOL_LINE:
                g.drawLine(x1, y1, x2, y2);
                break;
            case Toolset.TOOL_RECTANGLE:
                switch (fill)
                {
                    case Toolset.FILL_NONE:
                        g.drawRect(rx1, ry1, rx2 - rx1, ry2 - ry1);
                        break;
                    case Toolset.FILL_OPQUE:
                        g.fillRect(rx1, ry1, rx2 - rx1, ry2 - ry1);
                        break;
                    case Toolset.FILL_BACKGROUND:
                        g.setColor(pal.getColorOf(0));
                        g.fillRect(rx1, ry1, rx2 - rx1, ry2 - ry1);
                        g.setColor(pal.getSelectedColor());
                        g.drawRect(rx1, ry1, rx2 - rx1, ry2 - ry1);
                        break;
                }
                break;
            case Toolset.TOOL_OVAL:
                switch (fill)
                {
                    /*
                     * case Toolset.FILL_NONE : drawOval(rx1, ry1, rx2 - rx1,
                     * ry2 - ry1, c, false); break; case Toolset.FILL_OPQUE :
                     * drawOval(rx1, ry1, rx2 - rx1, ry2 - ry1, c, true); break;
                     * case Toolset.FILL_BACKGROUND : drawOval(rx1, ry1, rx2 -
                     * rx1, ry2 - ry1, 0, true); drawOval(rx1, ry1, rx2 - rx1,
                     * ry2 - ry1, c, false);
                     */
                    case Toolset.FILL_NONE:
                        g.drawOval(rx1, ry1, rx2 - rx1, ry2 - ry1);
                        break;
                    case Toolset.FILL_OPQUE:
                        g.fillOval(rx1, ry1, rx2 - rx1, ry2 - ry1);
                        break;
                    case Toolset.FILL_BACKGROUND:
                        g.setColor(pal.getColorOf(0));
                        g.fillOval(rx1, ry1, rx2 - rx1, ry2 - ry1);
                        g.setColor(pal.getSelectedColor());
                        g.drawOval(rx1, ry1, rx2 - rx1, ry2 - ry1);
                        break;
                }
                break;
            case Toolset.TOOL_ROUND_RECTANGLE:
                int curve = tools.getRoundedRectRadius();
                switch (fill)
                {
                    case Toolset.FILL_NONE:
                        g.drawRoundRect(rx1, ry1, rx2 - rx1, ry2 - ry1, curve,
                            curve);
                        break;
                    case Toolset.FILL_OPQUE:
                        g.fillRoundRect(rx1, ry1, rx2 - rx1, ry2 - ry1, curve,
                            curve);
                        break;
                    case Toolset.FILL_BACKGROUND:
                        g.setColor(pal.getColorOf(0));
                        g.fillRoundRect(rx1, ry1, rx2 - rx1, ry2 - ry1, curve,
                            curve);
                        g.setColor(pal.getSelectedColor());
                        g.drawRoundRect(rx1, ry1, rx2 - rx1, ry2 - ry1, curve,
                            curve);
                        break;
                }
                break;
            case Toolset.TOOL_PAINT_BUCKET:
                // undo();
                // do on mouse release
                break;
            case Toolset.TOOL_SELECTION:
                if (!isMakingSelect)
                {
                    // move selection
                    selection.setLocation(selx + (x2 - x1), sely + (y2 - y1));
                }
                else
                {
                    // create new selection
                    // don't do anything if selection out of range
                    if (rx1 >= 0 && ry1 >= 0 && rx2 < getWidth()
                        && ry2 < getHeight())
                    {
                        selection = new Rectangle(rx1, ry1, rx2 - rx1, ry2
                            - ry1);
                        if (selection.width > 0 && selection.height > 0)
                        {
                            selectImg = this.getNewImage(img, selection);
                        }
                    }
                }
                break;
        }

        repaint();
    }

    /**
     * Converts the specified zoomed x or y value to a non-zoomed value. Same as
     * dividing by the zoom.
     * 
     * @param xy A zoomed x or y value.
     * @return A non-zoomed x or y value.
     */
    public int getImgXY(int xy)
    {
        return (int) (xy / zoom);
    }

    /**
     * Converts the specified non-zoomed x or y value to a zoomed value. Same as
     * multipling by the zoom.
     * 
     * @param xy A non-zoomed x or y value.
     * @return A zoomed x or y value.
     */
    public int getZoomedXY(int xy)
    {
        return (int) (xy * zoom);
    }

    /**
     * @see java.awt.event.MouseMotionListener#mouseDragged(MouseEvent)
     */
    public void mouseDragged(MouseEvent me)
    {
        if (this.isEnabled())
        {
            this.wasDragged = true;
            int tool = tools.getSelectedDrawingTool();
            if (!(tool == Toolset.TOOL_EYEDROPER
                || tool == Toolset.TOOL_PAINT_BUCKET || tool == Toolset.TOOL_PENCIL)
                || tool == Toolset.TOOL_SELECTION)
            {
                // other tools need to be undo'd before drawing again
                undo(false);
                addUndo();
            }
            doTool(sx, sy, getImgXY(me.getX()), getImgXY(me.getY()));

            getNewImage();
        }
    }

    /**
     * @see java.awt.event.MouseListener#mousePressed(MouseEvent)
     */
    public void mousePressed(MouseEvent me)
    {
        if (this.isEnabled())
        {
            this.wasDragged = false;
            sx = getImgXY(me.getX());
            sy = getImgXY(me.getY());
            selx = selection.x;
            sely = selection.y;
            if (tools.getSelectedDrawingTool() == Toolset.TOOL_SELECTION)
            {
                isMakingSelect = ((!(selection.width > 0 && selection.height > 0)) || !(selection
                    .contains(sx, sy)));
            }
            if (!(tools.getSelectedDrawingTool() == Toolset.TOOL_EYEDROPER))
            {
                addUndo();
            }

            doTool(sx, sy, sx, sy);
        }
    }

    /**
     * @see java.awt.event.MouseListener#mouseClicked(MouseEvent)
     */
    public void mouseClicked(MouseEvent me)
    {
        if (this.isEnabled())
        {
            if (tools.getSelectedDrawingTool() == Toolset.TOOL_PAINT_BUCKET)
            {
                addUndo();
                doPaintBucket(getImgXY(me.getX()), getImgXY(me.getY()));
                repaint();
                fireChanged();
            }
        }
    }

    public void mouseReleased(MouseEvent me)
    {
        if (this.isEnabled())
        {
            if (tools.getSelectedDrawingTool() == Toolset.TOOL_SELECTION)
            {
                undo();
            }
            if (!this.wasDragged)
            {
                if (selection.height != 0 && selection.width != 0)
                {
                    addUndo();
                    flattenSelection();
                }
                this.selection = new Rectangle();
            }
            fireChanged();
        }
    }

    /**
     * Notifies ActionListener's that a change has been made. Run whenever a
     * change is made.
     */
    protected void fireChanged()
    {
        this.fireActionPerformed(new ActionEvent(this,
            ActionEvent.ACTION_PERFORMED, this.getActionCommand()));
    }

    /**
     * Copies a specified part of an Image into a new Image. The new Image will
     * be a completely separate object from the orginal.
     * 
     * @param img Image to copy
     * @param x Coordinate of top-left corner to start at.
     * @param y Coordinate of top-left corner to start at.
     * @param w Width of output image.
     * @param h Height of output image.
     * @return A new Image containing the same information.
     * @see #getNewImage(BufferedImage)
     * @see #getNewImage(BufferedImage, Rectangle)
     */
    public BufferedImage getNewImage(BufferedImage img, int x, int y, int w,
        int h)
    {
        int[] pixels = new int[w * h];
        BufferedImage out = (BufferedImage) this.createImage(w, h);
        PixelGrabber pg = new PixelGrabber(img.getSubimage(x, y, w, h), 0, 0,
            w, h, pixels, 0, w);
        try
        {
            pg.grabPixels();
        }
        catch (InterruptedException e)
        {
            System.err.println("interrupted waiting for pixels!");
            return null;
        }
        Graphics g = out.getGraphics();
        int alpha, blue, red, green;
        Color c;
        for (int j = 0; j < h; j++)
        {
            for (int i = 0; i < w; i++)
            {
                alpha = (pixels[j * w + i] >> 24) & 0xff;
                red = (pixels[j * w + i] >> 16) & 0xff;
                green = (pixels[j * w + i] >> 8) & 0xff;
                blue = (pixels[j * w + i]) & 0xff;

                c = new Color(red, green, blue, alpha);
                g.setColor(c);
                g.drawLine(i, j, i, j);
            }
        }
        return out;
    }

    /**
     * Copies a specified part of an Image into a new Image. The new Image will
     * be a completely separate object from the orginal.
     * 
     * @param img Image to copy
     * @param coords Rectangle representing the part of the image to copy.
     * @return A new Image containing the same information.
     * @see #getNewImage(BufferedImage)
     * @see #getNewImage(BufferedImage, int, int, int, int)
     */
    public BufferedImage getNewImage(BufferedImage img, Rectangle coords)
    {
        return this.getNewImage(img, coords.x, coords.y, coords.width,
            coords.height);
    }

    /** Replaces the current image with a copy of the current image. */
    protected void getNewImage()
    {
        img = this.getNewImage(img);
    }

    /**
     * Copies a Image into a new Image. The new Image will be a completely
     * separate object from the orginal.
     * 
     * @param img Image to copy
     * @return A new Image containing the same information.
     * @see #getNewImage(BufferedImage, int, int, int, int)
     * @see #getNewImage(BufferedImage, Rectangle)
     */
    public BufferedImage getNewImage(BufferedImage img)
    {
        return this.getNewImage(img, 0, 0, img.getWidth(), img.getHeight());
    }

    /**
     * Writes the selection with the specified Graphics. Does not actually
     * remove the selection.
     * 
     * @param g Graphics to paint to.
     * @param isZoomed Whether to draw zoomed.
     */
    public void flattenSelection(Graphics g, boolean isZoomed)
    {
        int w = selection.width, h = selection.height, x = selection.x, y = selection.y;
        int[] pixels = new int[w * h];
        PixelGrabber pg = new PixelGrabber(selectImg, 0, 0, w, h, pixels, 0, w);
        try
        {
            pg.grabPixels();
        }
        catch (InterruptedException e)
        {
            System.err.println("interrupted waiting for pixels!");
            return;
        }
        int alpha, blue, red, green;
        Color c;
        for (int j = 0; j < h; j++)
        {
            for (int i = 0; i < w; i++)
            {
                alpha = (pixels[j * w + i] >> 24) & 0xff;
                red = (pixels[j * w + i] >> 16) & 0xff;
                green = (pixels[j * w + i] >> 8) & 0xff;
                blue = (pixels[j * w + i]) & 0xff;

                c = new Color(red, green, blue, alpha);
                if (tools.isTransparentSelection())
                {
                    if (c.equals(pal.getColorOf(0)))
                    {
                        c = new Color(red, green, blue, 0);
                    }
                }
                g.setColor(c);
                if (isZoomed)
                {
                    g.fillRect(getZoomedXY(i + x), getZoomedXY(j + y),
                        getZoomedXY(1), getZoomedXY(1));
                }
                else
                {
                    g.drawLine(i + x, j + y, i + x, j + y);
                }
            }
        }
    }

    /**
     * Writes the selection to the image. Does not actually remove the
     * selection.
     */
    public void flattenSelection()
    {
        flattenSelection(img.getGraphics(), false);
    }

    /**
     * Cuts the selection. Deselects the selection afterwards. Same as
     * {@link #copy()}and then {@link #delete()}.
     * 
     * @see #paste()
     */
    public void cut()
    {
        copy();
        delete();
    }

    /**
     * Copies the selection.
     * 
     * @see #paste()
     */
    public void copy()
    {
        boolean selectionChanged = false;
        if (!(selection.width > 0 && selection.height > 0))
        {
            selection = new Rectangle(0, 0, img.getWidth(), img.getHeight());
            selectImg = getNewImage(img);
            selectionChanged = true;
        }
        this.cb.img = getNewImage(this.selectImg);
        this.cb.size = (Rectangle) this.selection.clone();
        if (selectionChanged)
        {
            selection = new Rectangle();
        }
    }

    /**
     * Pastes a {@link #copy()}'d or {@link #cut()}'d selection. It stays as a
     * selection until the user flattens it.
     */
    public void paste()
    {
        if (!cb.isClipboardEmpty())
            paste(cb.img, (Rectangle) cb.size.clone());
    }

    /**
     * Pastes a given selection. It stays as a selection until the user flattens
     * it.
     * 
     * @param img Image to paste, must fit exactly in sel
     * @param sel Selection rectangle to place image in
     */
    public void paste(BufferedImage img, Rectangle sel)
    {
        paste(img, sel, false);
    }

    /**
     * Pastes a given selection. If flatten is false, it stays as a selection
     * until the user flattens it.
     * 
     * @param img Image to paste, must fit exactly in sel
     * @param sel Selection rectangle to place image in
     * @param flatten Immediately flatten image
     */
    public void paste(BufferedImage img, Rectangle sel, boolean flatten)
    {
        this.selectImg = getNewImage(img);
        this.selection = sel;
        if (flatten)
        {
            flattenSelection();
            selection = new Rectangle();
        }
        fireChanged();
    }

    /**
     * Deletes the selection. Deselects the selection afterwards.
     */
    public void delete()
    {
        addUndo();
        if (!(selection.width > 0 && selection.height > 0))
        {
            selection = new Rectangle(0, 0, img.getWidth(), img.getHeight());
        }
        Graphics g = img.getGraphics();
        g.setColor(pal.getColorOf(0));
        g.fillRect(selection.x, selection.y, selection.width, selection.height);
        selection = new Rectangle();

        fireChanged();
    }

    /**
     * Returns the drawGridlines.
     * 
     * @return boolean
     */
    public boolean isDrawGridlines()
    {
        return drawGridlines;
    }

    /**
     * Sets the drawGridlines.
     * 
     * @param drawGridlines The drawGridlines to set
     */
    public void setDrawGridlines(boolean drawGridlines)
    {
        this.drawGridlines = drawGridlines;
    }

    /**
     * Returns the action command.
     * 
     * @return action command.
     */
    public String getActionCommand()
    {
        return actionCommand;
    }

    /**
     * Sets the action command.
     * 
     * @param string New action command.
     */
    public void setActionCommand(String string)
    {
        actionCommand = string;
    }
}