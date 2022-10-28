/*
 * Created on Aug 3, 2003
 */
package ebhack;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

/**
 * A DrawingArea that holds images as int[][]'s. Rounded rectangle non-filled
 * tool is not implemented, instead a regular rectangle gets drawn. If anyone
 * knows how to it please tell me!
 * 
 * @author AnyoneEB
 */
public class IntArrDrawingArea extends ImageDrawingArea
{
    private int[][] img = null, selectImg;
    private IntArrClipboard cb;
    private ArrayList undoArr = new ArrayList();

    /**
     * Clipboard for an IntArrDrawingArea. You can use the get and
     * setIntArrClipboard() methods to make two IntArrDrawingArea's share a
     * clipboard. This will not work for an ImageDrawingArea.
     * 
     * @see IntArrDrawingArea#getIntArrClipboard()
     * @see IntArrDrawingArea#setIntArrClipboard(IntArrDrawingArea.IntArrClipboard)
     * @author AnyoneEB
     */
    public static class IntArrClipboard extends ImageDrawingArea.Clipboard
    {
        /** Image on Clipboard as an int[]. */
        protected int[][] intArrImg;

        public IntArrClipboard()
        {}

        /**
         * Returns the Image on the clipboard as an int[].
         * 
         * @return The Image on the clipboard as an int[].
         */
        public int[][] getImgAsIntArr()
        {
            return intArrImg;
        }

        /**
         * Sets the image on the clipboard from an int[].
         * 
         * @param image New int[] to put on clipboard.
         */
        public void setImgAsIntArr(int[][] image)
        {
            intArrImg = image;
        }

        /**
         * Puts the given values onto the clipboard.
         * 
         * @param image New int[] to put on clipboard.
         * @param rectangle New area copied from.
         */
        public void copy(int[][] image, Rectangle rectangle)
        {
            this.setImgAsIntArr(image);
            this.setSize(rectangle);
        }

        /*
         * (non-Javadoc)
         * 
         * @see net.starmen.pkhack.ImageDrawingArea.Clipboard#clipboardEmpty()
         */
        public boolean isClipboardEmpty()
        {
            return intArrImg == null;
        }

    }

    /**
     * Creates a new IntArrDrawingArea with the given int[][], Toolset, and
     * palette.
     * 
     * @param img An int[][] of color indexes in <code>pal</code>.
     * @param tools Toolset
     * @param pal Palette
     */
    public IntArrDrawingArea(int[][] img, Toolset tools, ColorPalette pal)
    {
        this(tools, pal);
        this.setImage(img);
    }

    /**
     * Creates a new IntArrDrawingArea with the given int[][], Toolset, palette,
     * and zoom.
     * 
     * @param img An int[][] of color indexes in <code>pal</code>.
     * @param tools Toolset
     * @param pal Palette
     * @param zoom float of the zoom factor. 1.0 = 1x zoom
     */
    public IntArrDrawingArea(int[][] img, Toolset tools, ColorPalette pal,
        float zoom)
    {
        this(img, tools, pal);
        this.setZoom(zoom);
    }

    /**
     * Creates a new IntArrDrawingArea with the given Toolset and palette.
     * 
     * @param tools Toolset
     * @param pal Palette
     */
    public IntArrDrawingArea(Toolset tools, ColorPalette pal)
    {
        super(tools, pal);
        this.cb = new IntArrClipboard();
        this.zoom = 1;
    }

    /**
     * Creates a new IntArrDrawingArea with the given Toolset, palette, and
     * ActionListener.
     * 
     * @param tools Toolset
     * @param pal Palette
     * @param al ActionListener to tell about changes.
     */
    public IntArrDrawingArea(Toolset tools, ColorPalette pal, ActionListener al)
    {
        this(tools, pal);
        this.addActionListener(al);
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.starmen.pkhack.Undoable#addUndo()
     */
    public void addUndo()
    {
        undoArr.add(getNewImage(img)); // add copy of img to undo
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.starmen.pkhack.CopyAndPaster#copy()
     */
    public void copy()
    {
        boolean selectionChanged = false;
        if (!(selection.width > 0 && selection.height > 0))
        {
            selection = new Rectangle(0, 0, img.length, img[0].length);
            selectImg = getNewImage(img);
            selectionChanged = true;
        }
        this.cb.copy(getNewImage(this.selectImg), (Rectangle) this.selection
            .clone());
        if (selectionChanged)
        {
            selection = new Rectangle();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.starmen.pkhack.CopyAndPaster#delete()
     */
    public void delete()
    {
        addUndo();
        if (!(selection.width > 0 && selection.height > 0))
        {
            selection = new Rectangle(0, 0, img.length, img[0].length);
            this.selectImg = new int[img.length][img[0].length];
        }
        else
        {
            this.selectImg = new int[selectImg.length][selectImg[0].length];
        }
        this.flattenSelection();
        selection = new Rectangle();

        fireChanged();
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.starmen.pkhack.ImageDrawingArea#doHFlip()
     */
    public void doHFlip()
    {
        addUndo();

        int[][] newImg = getNewImage(img);
        for (int x = 0; x < img.length; x++)
        {
            for (int y = 0; y < img[0].length; y++)
            {
                drawPoint(x, y, newImg[img.length - x - 1][y]);
            }
        }

        repaint();
        fireChanged();
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.starmen.pkhack.ImageDrawingArea#doTool(int, int, int, int)
     */
    protected void doTool(int x1, int y1, int x2, int y2)
    {
        // x1, y1 is start of drag
        // x2, y2 is end/current place of drag
        // addUndo();
        int c = pal.getSelectedColorIndex();

        int tool = tools.getSelectedDrawingTool(), fill = tools.getFillType();
        int rx1 = Math.min(x1, x2), ry1 = Math.min(y1, y2), rx2 = Math.max(x1,
            x2), ry2 = Math.max(y1, y2), w = Math
            .min(x2 - x1, img.length - rx1), h = Math.min(y2 - y1,
            img[0].length - ry1), rw = Math.min(rx2 - rx1, img.length - rx1), rh = Math
            .min(ry2 - ry1, img[0].length - ry1);
        switch (tool)
        {
            case Toolset.TOOL_EYEDROPER:
                pal.setSelectedColorIndex(getPoint(x2, y2));
                // undo();
                break;
            case Toolset.TOOL_PENCIL:
                // undo();
                drawPoint(x2, y2, c);
                break;
            case Toolset.TOOL_LINE:
                drawLine(x1, y1, x2, y2, c);
                break;
            case Toolset.TOOL_RECTANGLE:
                switch (fill)
                {
                    case Toolset.FILL_NONE:
                        drawRect(rx1, ry1, rx2 - rx1, ry2 - ry1, c, false);
                        break;
                    case Toolset.FILL_OPQUE:
                        drawRect(rx1, ry1, rx2 - rx1, ry2 - ry1, c, true);
                        break;
                    case Toolset.FILL_BACKGROUND:
                        drawRect(rx1, ry1, rx2 - rx1, ry2 - ry1, 0, true);
                        drawRect(rx1, ry1, rx2 - rx1, ry2 - ry1, c, false);
                        break;
                }
                break;
            case Toolset.TOOL_OVAL:
                switch (fill)
                {
                    case Toolset.FILL_NONE:
                        drawOval(rx1, ry1, rx2 - rx1, ry2 - ry1, c, false);
                        break;
                    case Toolset.FILL_OPQUE:
                        drawOval(rx1, ry1, rx2 - rx1, ry2 - ry1, c, true);
                        break;
                    case Toolset.FILL_BACKGROUND:
                        drawOval(rx1, ry1, rx2 - rx1, ry2 - ry1, 0, true);
                        drawOval(rx1, ry1, rx2 - rx1, ry2 - ry1, c, false);
                        break;
                }
                break;
            case Toolset.TOOL_ROUND_RECTANGLE:
                int curve = tools.getRoundedRectRadius();
                switch (fill)
                {
                    case Toolset.FILL_NONE:
                        drawRoundedRect(rx1, ry1, rx2 - rx1, ry2 - ry1, curve,
                            curve, c, false);
                        break;
                    case Toolset.FILL_OPQUE:
                        drawRoundedRect(rx1, ry1, rx2 - rx1, ry2 - ry1, curve,
                            curve, c, true);
                        break;
                    case Toolset.FILL_BACKGROUND:
                        drawRoundedRect(rx1, ry1, rx2 - rx1, ry2 - ry1, curve,
                            curve, 0, true);
                        drawRoundedRect(rx1, ry1, rx2 - rx1, ry2 - ry1, curve,
                            curve, c, false);
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
                    // selection.setLocation(selx + (x2 - x1), sely + (y2 -
                    // y1));
                    selection.setLocation(selx + w, sely + h);
                }
                else
                {
                    // create new selection
                    // selection = new Rectangle(rx1, ry1, rx2 - rx1, ry2 -
                    // ry1);
                    // don't do anything if selection out of range
                    if (rx1 >= 0 && ry1 >= 0 && rx2 < getWidth()
                        && ry2 < getHeight())
                    {
                        selection = new Rectangle(rx1, ry1, rw, rh);
                        if (selection.width > 0 && selection.height > 0)
                        {
                            selectImg = getNewImage(img, selection);
                        }
                    }
                }
                break;
        }

        repaint();
    }

    /**
     * Draws a rounded rectangle ( <strong>Warning: doesn't work right unfilled
     * </strong>). Unfilled draws a regular rectangle instead, ways to do it
     * better are welcome.
     * 
     * @param rx1 X-Coordinate of top-left corner of the rectangle containing
     *            this.
     * @param ry1 Y-Coordinate of top-left corner of the rectangle containing
     *            this.
     * @param w Width
     * @param h Height
     * @param curvex the width of the arc to use to round off the corners
     * @param curvey the height of the arc to use to round off the corners
     * @param c Color
     * @param fill If true, shape is filled in.
     */
    private void drawRoundedRect(int rx1, int ry1, int w, int h, int curvex,
        int curvey, int c, boolean fill)
    {
        if (fill)
        {
            RoundRectangle2D rr = new RoundRectangle2D.Double(rx1, ry1, w, h,
                curvex, curvey);
            for (int x = rx1; x <= rx1 + w; x++)
                for (int y = ry1; y <= ry1 + h; y++)
                    if (rr.contains(x, y))
                        drawPoint(x, y, c);
        }
        else
        {
            // TODO draw a real unfilled rounded rectangle
            this.drawRect(rx1, ry1, w, h, c, fill);
        }
    }

    /**
     * Draw an oval.
     * 
     * @param rx1 X-Coordinate of top-left corner.
     * @param ry1 Y-Coordinate of top-left corner.
     * @param w Width
     * @param h Height
     * @param c Color
     * @param fill If true, shape is filled in.
     */
    private void drawOval(int rx1, int ry1, int w, int h, int c, boolean fill)
    {
        if (fill)
        {
            Ellipse2D e = new Ellipse2D.Double(rx1, ry1, w, h);
            for (int x = rx1; x <= rx1 + w; x++)
                for (int y = ry1; y <= ry1 + h; y++)
                    if (e.contains(x, y))
                        drawPoint(x, y, c);
        }
        else
        {
            float yr = ((float) h) / 2, xr = ((float) w) / 2;
            double tp = 0.01; // t percison
            for (double t = 0; t < Math.PI * 2; t += tp)
            {
                drawPoint((int) Math.round(rx1 + xr + (xr * Math.cos(t))),
                    (int) Math.round(ry1 + yr + (yr * Math.sin(t))), c);
            }
        }
    }

    /**
     * Draw a rectangle.
     * 
     * @param rx1 X-Coordinate of top-left corner.
     * @param ry1 Y-Coordinate of top-left corner.
     * @param w Width
     * @param h Height
     * @param c Color
     * @param fill If true, shape is filled in.
     */
    private void drawRect(int rx1, int ry1, int w, int h, int c, boolean fill)
    {
        if (fill)
        {
            for (int x = rx1; x < rx1 + w; x++)
                for (int y = ry1; y < ry1 + h; y++)
                    drawPoint(x, y, c);
        }
        else
        {
            for (int x = rx1; x < rx1 + w; x++)
            {
                drawPoint(x, ry1, c);
                drawPoint(x, ry1 + h - 1, c);
            }
            for (int y = ry1; y < ry1 + h; y++)
            {
                drawPoint(rx1, y, c);
                drawPoint(rx1 + w - 1, y, c);
            }
        }
    }

    /**
     * Draw a line.
     * 
     * @param x1 X-Coordinate of first point.
     * @param y1 Y-Coordinate of first point.
     * @param x2 X-Coordinate of second point.
     * @param y2 Y-Coordinate of second point.
     * @param c Color
     */
    private void drawLine(int x1, int y1, int x2, int y2, int c)
    {
        if (x2 - x1 == 0)
        {
            for (int y = Math.min(y1, y2); y <= Math.max(y1, y2); y++)
            {
                drawPoint(x1, y, c);
            }
        }
        else
        {
            float m = (float) (y2 - y1) / (float) (x2 - x1);
            if (Math.abs(m) <= 1)
            {
                float y = y1;
                if (x1 < x2)
                {
                    for (int x = x1; x <= x2; x++)
                    {
                        drawPoint(x, Math.round(y), c);
                        y += m;
                    }
                }
                else
                {
                    for (int x = x1; x >= x2; x--)
                    {
                        drawPoint(x, Math.round(y), c);
                        y -= m;
                    }
                }
            }
            else
            {
                m = (float) (x2 - x1) / (float) (y2 - y1);
                float x = x1;
                if (y1 < y2)
                {
                    for (int y = y1; y <= y2; y++)
                    {
                        drawPoint(Math.round(x), y, c);
                        x += m;
                    }
                }
                else
                {
                    for (int y = y1; y >= y2; y--)
                    {
                        drawPoint(Math.round(x), y, c);
                        x -= m;
                    }
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.starmen.pkhack.ImageDrawingArea#doVFlip()
     */
    public void doVFlip()
    {
        addUndo();

        int[][] newImg = getNewImage(img);
        for (int x = 0; x < img.length; x++)
        {
            for (int y = 0; y < img[0].length; y++)
            {
                this.drawPoint(x, y, newImg[x][img[0].length - y - 1]);
            }
        }

        repaint();
        fireChanged();
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.starmen.pkhack.DrawingArea#drawPoint(int, int, int)
     */
    public void drawPoint(int x, int y, int c)
    {
        try
        {
            this.img[x][y] = c;
        }
        catch (ArrayIndexOutOfBoundsException e)
        {}
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.starmen.pkhack.ImageDrawingArea#flattenSelection()
     */
    public void flattenSelection()
    {
        if (selection.width == 0 || selection.height == 0)
            return;
        for (int x = selection.x; x < selection.width + selection.x; x++)
        {
            for (int y = selection.y; y < selection.height + selection.y; y++)
            {
                try
                {
                    drawPoint(x, y, this.selectImg[x - selection.x][y
                        - selection.y]);
                }
                catch (ArrayIndexOutOfBoundsException e)
                {}
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.starmen.pkhack.ImageDrawingArea#getClipboard()
     */
    public IntArrClipboard getIntArrClipboard()
    {
        return cb;
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.starmen.pkhack.ImageDrawingArea#getImage()
     */
    public Image getImage()
    {
        return this.getImage(img);
    }

    /**
     * Returns a copy of the int[][] being edited.
     * 
     * @return The image edited by this.
     */
    public int[][] getIntArrImage()
    {
        return getNewImage(img);
    }

    /**
     * Returns a copy of the int[][] being edited as a byte[].
     * 
     * @return The image edited by this.
     */
    public byte[][] getByteArrImage()
    {
        return getNewByteImage(img);
    }

    /**
     * Returns an Image based on an int[][] and the current palette. Use
     * {@link #getNewImage(int[][], int, int, int, int)}or
     * {@link #getNewImage(int[][], Rectangle)}if you only want part of the
     * int[][] in the image.
     * 
     * @param img int[][] to with image data.
     * @return Image containing the appearence of the image in <code>img</code>
     */
    public Image getImage(int[][] img)
    {
        BufferedImage out = new BufferedImage(img.length, img[0].length,
            BufferedImage.TYPE_4BYTE_ABGR_PRE);
        Graphics g = out.getGraphics();
        for (int x = 0; x < out.getWidth(); x++)
        {
            for (int y = 0; y < out.getHeight(); y++)
            {
                g.setColor(pal.getColorOf(img[x][y]));
                g.drawLine(x, y, x, y);
            }
        }
        return out;
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.starmen.pkhack.DrawingArea#getPoint(int, int)
     */
    public int getPoint(int x, int y)
    {
        try
        {
            return img[x][y];
        }
        catch (ArrayIndexOutOfBoundsException e)
        {
            return 0;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.starmen.pkhack.ImageDrawingArea#getPointColor(int, int)
     */
    public Color getPointColor(int x, int y)
    {
        return pal.getColorOf(this.getPoint(x, y));
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.Component#paint(java.awt.Graphics)
     */
    public void paint(Graphics g)
    {
        if (this.isEnabled())
        {
            if (img == null)
            {
                return;
            }
            // if (this.drawingWidth * this.drawingHeight < 500)
            // {
            for (int x = 0; x < this.drawingWidth; x++)
            {
                for (int y = 0; y < this.drawingHeight; y++)
                {
                    if (selection.width > 0 && selection.height > 0
                        && selection.contains(x, y))
                    {
                        g.setColor(pal.getColorOf(selectImg[x - selection.x][y
                            - selection.y]));
                        g.fillRect((int) (x * zoom), (int) (y * zoom),
                            (int) zoom, (int) zoom);
                    }
                    else
                    {
                        drawPoint(x, y, g);
                    }
                }
            }
            // }
            /*
             * else { g .drawImage( this.getImage(), 0, 0,
             * getZoomedXY(drawingWidth), getZoomedXY(drawingHeight),
             * //Color.BLACK, this);
             */
            // selection image
            // if (selection.width > 0 && selection.height > 0)
            // {
            // flattenSelection(g, true);
            // }
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

    /*
     * (non-Javadoc)
     * 
     * @see net.starmen.pkhack.ImageDrawingArea#setClipboard(net.starmen.pkhack.ImageDrawingArea.Clipboard)
     */
    public void setIntArrClipboard(IntArrClipboard newCb)
    {
        this.cb = newCb;
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.starmen.pkhack.Undoable#undo()
     */
    public void undo()
    {
        undo(true);
    }

    public void undo(boolean notify)
    {
        if (!undoArr.isEmpty())
        {
            this.img = (int[][]) undoArr.get(undoArr.size() - 1);
            undoArr.remove(undoArr.size() - 1);
            this.repaint();
        }
        if (notify)
            fireChanged();
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.starmen.pkhack.ImageDrawingArea#setImage(java.awt.Image, float)
     */
    public void setImage(int[][] img, float zoom)
    {
        this.img = getNewImage(img);
        this.setZoom(zoom);

        this.selection = new Rectangle();
        this.undoArr.clear();

        this.initGraphics();
    }

    public void setImage(int[][] img)
    {
        this.setImage(img, this.zoom);
    }

    public void setImage(byte[][] img)
    {
        this.setImage(getNewImage(img));
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.starmen.pkhack.ImageDrawingArea#getNewImage(java.awt.image.BufferedImage,
     *      int, int, int, int)
     */
    public static int[][] getNewImage(int[][] img, int x, int y, int w, int h)
    {
        // make sure height and width aren't out of range
        w = Math.min(w, img.length - x);
        h = Math.min(h, img[0].length - y);
        int[][] newImg = new int[w][h];

        for (int j = 0; j < h; j++)
        {
            for (int i = 0; i < w; i++)
            {
                newImg[i][j] = img[i + x][j + y];
            }
        }

        return newImg;
    }

    public static short[][] getNewShortImage(short[][] img, int x, int y,
        int w, int h)
    {
        // make sure height and width aren't out of range
        w = Math.min(w, img.length - x);
        h = Math.min(h, img[0].length - y);
        short[][] newImg = new short[w][h];

        for (int j = 0; j < h; j++)
        {
            for (int i = 0; i < w; i++)
            {
                newImg[i][j] = img[i + x][j + y];
            }
        }

        return newImg;
    }

    public static byte[][] getNewByteImage(int[][] img, int x, int y, int w,
        int h)
    {
        // make sure height and width aren't out of range
        w = Math.min(w, img.length - x);
        h = Math.min(h, img[0].length - y);
        byte[][] newImg = new byte[w][h];

        for (int j = 0; j < h; j++)
        {
            for (int i = 0; i < w; i++)
            {
                newImg[i][j] = (byte) img[i + x][j + y];
            }
        }

        return newImg;
    }

    public static int[][] getNewImage(byte[][] img, int x, int y, int w, int h)
    {
        // make sure height and width aren't out of range
        w = Math.min(w, img.length - x);
        h = Math.min(h, img[0].length - y);
        int[][] newImg = new int[w][h];

        for (int j = 0; j < h; j++)
        {
            for (int i = 0; i < w; i++)
            {
                newImg[i][j] = img[i + x][j + y] & 0xff;
            }
        }

        return newImg;
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.starmen.pkhack.ImageDrawingArea#getNewImage(java.awt.image.BufferedImage,
     *      java.awt.Rectangle)
     */
    public static int[][] getNewImage(int[][] img, Rectangle coords)
    {
        return getNewImage(img, coords.x, coords.y, coords.width, coords.height);
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.starmen.pkhack.ImageDrawingArea#getNewImage(java.awt.image.BufferedImage)
     */
    public static int[][] getNewImage(int[][] img)
    {
        return getNewImage(img, 0, 0, img.length, img[0].length);
    }

    public static short[][] getNewShortImage(short[][] img)
    {
        return getNewShortImage(img, 0, 0, img.length, img[0].length);
    }

    public static byte[][] getNewByteImage(int[][] img)
    {
        return getNewByteImage(img, 0, 0, img.length, img[0].length);
    }

    public static int[][] getNewImage(byte[][] img)
    {
        return getNewImage(img, 0, 0, img.length, img[0].length);
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.starmen.pkhack.CopyAndPaster#paste()
     */
    public void paste()
    {
        if (!cb.isClipboardEmpty())
            paste(cb.getImgAsIntArr(), (Rectangle) cb.size.clone());
    }

    /**
     * Pastes a given selection in the top-left corner. It stays as a selection
     * until the user flattens it.
     * 
     * @param img byte[][] to paste, must fit exactly in sel
     */
    public void paste(byte[][] img)
    {
        paste(img, false);
    }

    /**
     * Pastes a given selection in the top-left corner. If flatten is false, it
     * stays as a selection until the user flattens it.
     * 
     * @param img byte[][] to paste, must fit exactly in sel
     * @param flatten Immediately flatten image
     */
    public void paste(byte[][] img, boolean flatten)
    {
        paste(img, new Rectangle(img.length, img[0].length), flatten);
    }

    /**
     * Pastes a given selection. It stays as a selection until the user flattens
     * it.
     * 
     * @param img byte[][] to paste, must fit exactly in sel
     * @param sel Selection rectangle to place image in
     */
    public void paste(byte[][] img, Rectangle sel)
    {
        paste(img, sel, false);
    }

    /**
     * Pastes a given selection. If flatten is false, it stays as a selection
     * until the user flattens it.
     * 
     * @param img byte[][] to paste, must fit exactly in sel
     * @param sel Selection rectangle to place image in
     * @param flatten Immediately flatten image
     */
    public void paste(byte[][] img, Rectangle sel, boolean flatten)
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
     * Pastes a given selection in the top-left corner. It stays as a selection
     * until the user flattens it.
     * 
     * @param img int[][] to paste, must fit exactly in sel
     */
    public void paste(int[][] img)
    {
        paste(img, false);
    }

    /**
     * Pastes a given selection in the top-left corner. If flatten is false, it
     * stays as a selection until the user flattens it.
     * 
     * @param img int[][] to paste, must fit exactly in sel
     * @param flatten Immediately flatten image
     */
    public void paste(int[][] img, boolean flatten)
    {
        paste(img, new Rectangle(img.length, img[0].length), flatten);
    }

    /**
     * Pastes a given selection. It stays as a selection until the user flattens
     * it.
     * 
     * @param img int[][] to paste, must fit exactly in sel
     * @param sel Selection rectangle to place image in
     */
    public void paste(int[][] img, Rectangle sel)
    {
        paste(img, sel, false);
    }

    /**
     * Pastes a given selection. If flatten is false, it stays as a selection
     * until the user flattens it.
     * 
     * @param img int[][] to paste, must fit exactly in sel
     * @param sel Selection rectangle to place image in
     * @param flatten Immediately flatten image
     */
    public void paste(int[][] img, Rectangle sel, boolean flatten)
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

    /*
     * (non-Javadoc)
     * 
     * @see net.starmen.pkhack.DrawingArea#getDrawingHeight()
     */
    public int getDrawingHeight()
    {
        return super.getDrawingHeight();
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.starmen.pkhack.DrawingArea#getDrawingWidth()
     */
    public int getDrawingWidth()
    {
        return super.getDrawingWidth();
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.starmen.pkhack.ImageDrawingArea#initGraphics()
     */
    protected void initGraphics()
    {
        this.drawingWidth = img.length;
        this.drawingHeight = img[0].length;

        int w = (int) (drawingWidth * zoom) + 1, h = (int) (drawingHeight * zoom) + 1;

        this.setPreferredSize(new Dimension(w, h));

        repaint();
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.starmen.pkhack.ImageDrawingArea#getNewImage()
     */
    protected void getNewImage()
    {
        this.img = getNewImage(img);
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.starmen.pkhack.ImageDrawingArea#doPaintBucket(int, int)
     */
    /*
     * protected void doPaintBucket(int x, int y) { int currentCol =
     * this.getPoint(x, y), newCol = pal .getSelectedColorIndex(); if
     * (currentCol == newCol) { undo(); return; } this.drawPoint(x, y, newCol);
     * for (int ax = -1; ax < 2; ax++) for (int ay = -1; ay < 2; ay++) if (x +
     * ax > -1 && y + ay > -1 && x + ax < this.getDrawingWidth() && y + ay <
     * this.getDrawingHeight()) //make sure it's on the image if
     * (this.getPoint(x + ax, y + ay) == currentCol) doPaintBucket(x + ax, y +
     * ay); }
     */

    private void doPaintBucketReg(int x, int y, int newCol, int currentCol)
    {
        // make sure it's on the image
        if (x > -1 && y > -1 && x < this.getDrawingWidth()
            && y < this.getDrawingHeight() && this.getPoint(x, y) == currentCol)
        {
            this.drawPoint(x, y, newCol);
            for (int ax = -1; ax < 2; ax++)
                for (int ay = -1; ay < 2; ay++)
                    doPaintBucketReg(x + ax, y + ay, newCol, currentCol);
        }
    }

    private void doPaintBucketNoDia(int x, int y, int newCol, int currentCol)
    {
        // make sure it's on the image
        if (x > -1 && y > -1 && x < this.getDrawingWidth()
            && y < this.getDrawingHeight() && this.getPoint(x, y) == currentCol)
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
        int currentCol = this.getPoint(x, y), newCol = pal
            .getSelectedColorIndex();
        if (currentCol == newCol)
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
}