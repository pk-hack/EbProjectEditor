package ebhack;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;

import ebhack.DrawingArea.Toolset;

/**
 * A drawing toolset component which implements {@link DrawingArea.Toolset}.
 * The appearance is based on the toolset in Mr. Accident's EBSE. This allows
 * the user to control all of the toolset options.
 * 
 * @author AnyoneEB
 */
public class DrawingToolset extends JComponent implements Toolset
{
    private ActionListener al;
    private ButtonGroup bg;
    private JComboBox fillSelector, fillMethodSelector;
    private JCheckBox transparentSelection;
    private JTextField roundedRectRad;

    private JToggleButton pencil, pBucket, eyedropper, selection, line, rect,
            oval, roundedRect;
    protected final static int[] TOOL_ORDER = new int[]{TOOL_PENCIL,
        TOOL_PAINT_BUCKET, TOOL_EYEDROPER, TOOL_SELECTION, TOOL_LINE,
        TOOL_RECTANGLE, TOOL_OVAL, TOOL_ROUND_RECTANGLE};

    private void initGraphics(boolean flip)
    {
        this.setLayout(new BorderLayout());

        JButton hFlip, vFlip;
        bg = new ButtonGroup();

        JPanel buttons = new JPanel(new GridLayout(5, 2));

        pencil = new JToggleButton();
        pencil.setIcon(getPencilIcon());
        pencil.setActionCommand(Integer.toString(TOOL_PENCIL));
        pencil.setSelected(true); // start with pencil tool
        bg.add(pencil);
        buttons.add(pencil);

        pBucket = new JToggleButton();
        pBucket.setIcon(getPaintBucketIcon());
        pBucket.setActionCommand(Integer.toString(TOOL_PAINT_BUCKET));
        bg.add(pBucket);
        buttons.add(pBucket);

        eyedropper = new JToggleButton();
        eyedropper.setIcon(getEyedropperIcon());
        eyedropper.setActionCommand(Integer.toString(TOOL_EYEDROPER));
        bg.add(eyedropper);
        buttons.add(eyedropper);

        selection = new JToggleButton();
        selection.setIcon(getSelectionIcon());
        selection.setActionCommand(Integer.toString(TOOL_SELECTION));
        bg.add(selection);
        buttons.add(selection);

        line = new JToggleButton();
        line.setIcon(getLineIcon());
        line.setActionCommand(Integer.toString(TOOL_LINE));
        bg.add(line);
        buttons.add(line);

        rect = new JToggleButton();
        // rect.setIcon(new ImageIcon("net/starmen/pkhack/rect.gif"));
        rect.setIcon(getRectangleIcon());
        rect.setActionCommand(Integer.toString(TOOL_RECTANGLE));
        rect.setToolTipText("Rectangle");
        bg.add(rect);
        buttons.add(rect);

        oval = new JToggleButton();
        // oval.setIcon(new ImageIcon("net/starmen/pkhack/oval.gif"));
        oval.setIcon(getOvalIcon());
        oval.setToolTipText("Oval");
        oval.setActionCommand(Integer.toString(TOOL_OVAL));
        bg.add(oval);
        buttons.add(oval);

        roundedRect = new JToggleButton();
        // roundedRect.setIcon(
        // new ImageIcon("net/starmen/pkhack/roundedRect.gif"));
        roundedRect.setIcon(getRoundedRectangleIcon());
        roundedRect.setToolTipText("Rounded Rectangle");
        roundedRect.setActionCommand(Integer.toString(TOOL_ROUND_RECTANGLE));
        bg.add(roundedRect);
        buttons.add(roundedRect);

        if (flip)
        {
            hFlip = new JButton();
            hFlip.setIcon(getHFlipIcon());
            hFlip.setActionCommand("hFlip");
            hFlip.addActionListener(this.al);
            buttons.add(hFlip);

            vFlip = new JButton();
            vFlip.setIcon(getVFlipIcon());
            vFlip.setActionCommand("vFlip");
            vFlip.addActionListener(this.al);
            buttons.add(vFlip);
        }

        JPanel buttonsWrapper = new JPanel(new FlowLayout());
        buttonsWrapper.add(buttons);
        this.add(buttonsWrapper, BorderLayout.NORTH);

        // JPanel settings = new JPanel(new BorderLayout());
        Box settings = new Box(BoxLayout.Y_AXIS);

        this.fillSelector = new JComboBox();
        fillSelector.addItem("None");
        fillSelector.addItem("Background");
        fillSelector.addItem("Opque");
        fillSelector.setSelectedIndex(0);
        settings.add(ToolModule.getLabeledComponent("Fill: ", fillSelector));

        this.fillMethodSelector = new JComboBox();
        fillMethodSelector.addItem("All");
        fillMethodSelector.addItem("No diagonals");
        fillMethodSelector.setSelectedIndex(0);
        settings.add(ToolModule.getLabeledComponent("Fill Dirs: ",
            fillMethodSelector, "Selects regular fill (\"All\") or "
                + "horizonal/vertical only (\"No diagonals\")"));

        this.transparentSelection = new JCheckBox();
        this.transparentSelection.setSelected(false);
        settings.add(ToolModule.getLabeledComponent("Transparent Selection: ",
            transparentSelection));

        this.roundedRectRad = ToolModule.createSizedJTextField(2, true);
        this.roundedRectRad.setText("1");
        settings.add(ToolModule.getLabeledComponent("Curve Size: ",
            roundedRectRad, "Curve width & height for rounded rectangles"));

        this.add(settings, BorderLayout.SOUTH);

        for (int i = 1; i <= 8; i++)
        {
            final int j = i;
            this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke("ctrl " + j), "tool" + j);
            this.getActionMap().put("tool" + i, new AbstractAction()
            {
                public void actionPerformed(ActionEvent e)
                {
                    setSelectedDrawingTool(TOOL_ORDER[j - 1]);
                }
            });
        }
    }

    /**
     * Constructor for DrawingToolset.
     * 
     * @param al ActionListener to tell about flip events.
     */
    public DrawingToolset(ActionListener al) // to listen to flip events
    {
        super();
        this.al = al;
        initGraphics(true);
    }

    public DrawingToolset()
    {
        initGraphics(false);
    }

    /**
     * @see net.starmen.pkhack.DrawingArea.Toolset#getSelectedDrawingTool()
     */
    public int getSelectedDrawingTool()
    {
        return Integer.parseInt(bg.getSelection().getActionCommand());
    }

    private JToggleButton getButton(int tool)
    {
        switch (tool)
        {
            case TOOL_EYEDROPER:
                return eyedropper;
            case TOOL_LINE:
                return line;
            case TOOL_OVAL:
                return oval;
            case TOOL_PAINT_BUCKET:
                return pBucket;
            case TOOL_PENCIL:
                return pencil;
            case TOOL_RECTANGLE:
                return rect;
            case TOOL_ROUND_RECTANGLE:
                return roundedRect;
            case TOOL_SELECTION:
                return selection;
            default:
                return null;
        }
    }

    protected void setSelectedDrawingTool(int tool)
    {
        bg.setSelected(getButton(tool).getModel(), true);
    }

/*    private void setSelectedDrawingTool(JToggleButton tool)
    {
        bg.setSelected(tool.getModel(), true);
    }*/

    /**
     * @see net.starmen.pkhack.DrawingArea.Toolset#getFillType()
     */
    public int getFillType()
    {
        return this.fillSelector.getSelectedIndex();
    }

    /**
     * @see net.starmen.pkhack.DrawingArea.Toolset#getFillMethod()
     */
    public int getFillMethod()
    {
        return this.fillMethodSelector.getSelectedIndex();
    }

    /**
     * @see net.starmen.pkhack.DrawingArea.Toolset#isTransparentSelection()
     */
    public boolean isTransparentSelection()
    {
        return this.transparentSelection.isSelected();
    }

    /**
     * @see net.starmen.pkhack.DrawingArea.Toolset#getRoundedRectRadius()
     */
    public int getRoundedRectRadius()
    {
        try
        {
            return Integer.parseInt(this.roundedRectRad.getText());
        }
        catch (NumberFormatException e)
        {
            return 1;
        }
    }

    // Icons for the buttons
    private static Icon rectIco, rrectIco, ovalIco, lineIco, hFlipIco,
            vFlipIco, pencilIco, selIco, paintIco, eyeIco;

    public static Icon getRectangleIcon()
    {
        if (rectIco != null)
            return rectIco;
        BufferedImage rectImg = new BufferedImage(16, 16,
            BufferedImage.TYPE_4BYTE_ABGR);
        Graphics g = rectImg.getGraphics();

        // Created by ImageFileToCode from net/starmen/pkhack/rect.gif
        g.setColor(new Color(0, 0, 0));
        g.drawLine(0, 1, 15, 1);
        g.drawLine(0, 2, 0, 13);
        g.drawLine(15, 2, 15, 13);
        g.drawLine(0, 14, 15, 14);

        return rectIco = new ImageIcon(rectImg);
    }

    public static Icon getOvalIcon()
    {
        if (ovalIco != null)
            return ovalIco;
        BufferedImage out = new BufferedImage(16, 16,
            BufferedImage.TYPE_4BYTE_ABGR);
        Graphics g = out.getGraphics();

        // Created by ImageFileToCode from net/starmen/pkhack/oval.gif
        g.setColor(new Color(255, 255, 255));
        g.setColor(new Color(0, 0, 0));
        g.drawLine(5, 3, 5, 3);
        g.drawLine(6, 3, 6, 3);
        g.drawLine(7, 3, 7, 3);
        g.drawLine(8, 3, 8, 3);
        g.drawLine(9, 3, 9, 3);
        g.drawLine(10, 3, 10, 3);
        g.drawLine(2, 4, 2, 4);
        g.drawLine(3, 4, 3, 4);
        g.drawLine(4, 4, 4, 4);
        g.drawLine(5, 4, 5, 4);
        g.drawLine(10, 4, 10, 4);
        g.drawLine(11, 4, 11, 4);
        g.drawLine(12, 4, 12, 4);
        g.drawLine(13, 4, 13, 4);
        g.drawLine(1, 5, 1, 5);
        g.drawLine(2, 5, 2, 5);
        g.drawLine(13, 5, 13, 5);
        g.drawLine(14, 5, 14, 5);
        g.drawLine(0, 6, 0, 6);
        g.drawLine(1, 6, 1, 6);
        g.drawLine(14, 6, 14, 6);
        g.drawLine(15, 6, 15, 6);
        g.drawLine(0, 7, 0, 7);
        g.drawLine(15, 7, 15, 7);
        g.drawLine(0, 8, 0, 8);
        g.drawLine(15, 8, 15, 8);
        g.drawLine(0, 9, 0, 9);
        g.drawLine(1, 9, 1, 9);
        g.drawLine(14, 9, 14, 9);
        g.drawLine(15, 9, 15, 9);
        g.drawLine(1, 10, 1, 10);
        g.drawLine(2, 10, 2, 10);
        g.drawLine(13, 10, 13, 10);
        g.drawLine(14, 10, 14, 10);
        g.drawLine(2, 11, 2, 11);
        g.drawLine(3, 11, 3, 11);
        g.drawLine(4, 11, 4, 11);
        g.drawLine(5, 11, 5, 11);
        g.drawLine(10, 11, 10, 11);
        g.drawLine(11, 11, 11, 11);
        g.drawLine(12, 11, 12, 11);
        g.drawLine(13, 11, 13, 11);
        g.drawLine(5, 12, 5, 12);
        g.drawLine(6, 12, 6, 12);
        g.drawLine(7, 12, 7, 12);
        g.drawLine(8, 12, 8, 12);
        g.drawLine(9, 12, 9, 12);
        g.drawLine(10, 12, 10, 12);

        return ovalIco = new ImageIcon(out);
    }

    public static Icon getRoundedRectangleIcon()
    {
        if (rrectIco != null)
            return rrectIco;
        BufferedImage out = new BufferedImage(16, 16,
            BufferedImage.TYPE_4BYTE_ABGR);
        Graphics g = out.getGraphics();

        // Created by ImageFileToCode from net/starmen/pkhack/roundedRect.gif
        g.setColor(new Color(255, 255, 255));
        g.setColor(new Color(0, 0, 0));
        g.drawLine(2, 1, 2, 1);
        g.drawLine(3, 1, 3, 1);
        g.drawLine(4, 1, 4, 1);
        g.drawLine(5, 1, 5, 1);
        g.drawLine(6, 1, 6, 1);
        g.drawLine(7, 1, 7, 1);
        g.drawLine(8, 1, 8, 1);
        g.drawLine(9, 1, 9, 1);
        g.drawLine(10, 1, 10, 1);
        g.drawLine(11, 1, 11, 1);
        g.drawLine(12, 1, 12, 1);
        g.drawLine(13, 1, 13, 1);
        g.drawLine(1, 2, 1, 2);
        g.drawLine(2, 2, 2, 2);
        g.drawLine(13, 2, 13, 2);
        g.drawLine(14, 2, 14, 2);
        g.drawLine(0, 3, 0, 3);
        g.drawLine(1, 3, 1, 3);
        g.drawLine(14, 3, 14, 3);
        g.drawLine(15, 3, 15, 3);
        g.drawLine(0, 4, 0, 4);
        g.drawLine(15, 4, 15, 4);
        g.drawLine(0, 5, 0, 5);
        g.drawLine(15, 5, 15, 5);
        g.drawLine(0, 6, 0, 6);
        g.drawLine(15, 6, 15, 6);
        g.drawLine(0, 7, 0, 7);
        g.drawLine(15, 7, 15, 7);
        g.drawLine(0, 8, 0, 8);
        g.drawLine(15, 8, 15, 8);
        g.drawLine(0, 9, 0, 9);
        g.drawLine(15, 9, 15, 9);
        g.drawLine(0, 10, 0, 10);
        g.drawLine(15, 10, 15, 10);
        g.drawLine(0, 11, 0, 11);
        g.drawLine(15, 11, 15, 11);
        g.drawLine(0, 12, 0, 12);
        g.drawLine(1, 12, 1, 12);
        g.drawLine(14, 12, 14, 12);
        g.drawLine(15, 12, 15, 12);
        g.drawLine(1, 13, 1, 13);
        g.drawLine(2, 13, 2, 13);
        g.drawLine(13, 13, 13, 13);
        g.drawLine(14, 13, 14, 13);
        g.drawLine(2, 14, 2, 14);
        g.drawLine(3, 14, 3, 14);
        g.drawLine(4, 14, 4, 14);
        g.drawLine(5, 14, 5, 14);
        g.drawLine(6, 14, 6, 14);
        g.drawLine(7, 14, 7, 14);
        g.drawLine(8, 14, 8, 14);
        g.drawLine(9, 14, 9, 14);
        g.drawLine(10, 14, 10, 14);
        g.drawLine(11, 14, 11, 14);
        g.drawLine(12, 14, 12, 14);
        g.drawLine(13, 14, 13, 14);

        return rrectIco = new ImageIcon(out);
    }

    public static Icon getSelectionIcon()
    {
        if (selIco != null)
            return selIco;
        BufferedImage out = new BufferedImage(16, 16,
            BufferedImage.TYPE_4BYTE_ABGR);
        Graphics g = out.getGraphics();

        // Created by ImageFileToCode from net/starmen/pkhack/selection.gif
        g.setColor(new Color(0, 0, 0));
        g.drawLine(0, 0, 0, 0);
        g.drawLine(1, 0, 1, 0);
        g.drawLine(5, 0, 5, 0);
        g.drawLine(6, 0, 6, 0);
        g.drawLine(7, 0, 7, 0);
        g.drawLine(11, 0, 11, 0);
        g.drawLine(12, 0, 12, 0);
        g.drawLine(13, 0, 13, 0);
        g.drawLine(0, 1, 0, 1);
        g.drawLine(15, 2, 15, 2);
        g.drawLine(15, 3, 15, 3);
        g.drawLine(15, 4, 15, 4);
        g.drawLine(0, 5, 0, 5);
        g.drawLine(0, 6, 0, 6);
        g.drawLine(0, 7, 0, 7);
        g.drawLine(15, 8, 15, 8);
        g.drawLine(15, 9, 15, 9);
        g.drawLine(15, 10, 15, 10);
        g.drawLine(0, 11, 0, 11);
        g.drawLine(0, 12, 0, 12);
        g.drawLine(0, 13, 0, 13);
        g.drawLine(15, 14, 15, 14);
        g.drawLine(2, 15, 2, 15);
        g.drawLine(3, 15, 3, 15);
        g.drawLine(4, 15, 4, 15);
        g.drawLine(8, 15, 8, 15);
        g.drawLine(9, 15, 9, 15);
        g.drawLine(10, 15, 10, 15);
        g.drawLine(14, 15, 14, 15);
        g.drawLine(15, 15, 15, 15);

        return selIco = new ImageIcon(out);
    }

    public static Icon getPencilIcon()
    {
        if (pencilIco != null)
            return pencilIco;
        BufferedImage out = new BufferedImage(16, 16,
            BufferedImage.TYPE_4BYTE_ABGR);
        Graphics g = out.getGraphics();

        // Created by ImageFileToCode from net/starmen/pkhack/pencil.gif
        g.setColor(new Color(255, 255, 255));
        g.setColor(new Color(0, 0, 0));
        g.drawLine(10, 0, 10, 0);
        g.drawLine(11, 0, 11, 0);
        g.setColor(new Color(182, 134, 152));
        g.drawLine(12, 0, 12, 0);
        g.setColor(new Color(0, 0, 0));
        g.drawLine(13, 0, 13, 0);
        g.drawLine(14, 0, 14, 0);
        g.drawLine(10, 1, 10, 1);
        g.setColor(new Color(182, 134, 152));
        g.drawLine(11, 1, 11, 1);
        g.drawLine(12, 1, 12, 1);
        g.drawLine(13, 1, 13, 1);
        g.setColor(new Color(0, 0, 0));
        g.drawLine(14, 1, 14, 1);
        g.drawLine(15, 1, 15, 1);
        g.drawLine(9, 2, 9, 2);
        g.drawLine(10, 2, 10, 2);
        g.drawLine(11, 2, 11, 2);
        g.setColor(new Color(182, 134, 152));
        g.drawLine(12, 2, 12, 2);
        g.drawLine(13, 2, 13, 2);
        g.drawLine(14, 2, 14, 2);
        g.setColor(new Color(0, 0, 0));
        g.drawLine(15, 2, 15, 2);
        g.drawLine(8, 3, 8, 3);
        g.drawLine(9, 3, 9, 3);
        g.setColor(new Color(255, 212, 0));
        g.drawLine(10, 3, 10, 3);
        g.setColor(new Color(0, 0, 0));
        g.drawLine(11, 3, 11, 3);
        g.drawLine(12, 3, 12, 3);
        g.setColor(new Color(182, 134, 152));
        g.drawLine(13, 3, 13, 3);
        g.setColor(new Color(0, 0, 0));
        g.drawLine(14, 3, 14, 3);
        g.drawLine(15, 3, 15, 3);
        g.drawLine(7, 4, 7, 4);
        g.drawLine(8, 4, 8, 4);
        g.setColor(new Color(255, 212, 0));
        g.drawLine(9, 4, 9, 4);
        g.drawLine(10, 4, 10, 4);
        g.drawLine(11, 4, 11, 4);
        g.setColor(new Color(0, 0, 0));
        g.drawLine(12, 4, 12, 4);
        g.drawLine(13, 4, 13, 4);
        g.drawLine(14, 4, 14, 4);
        g.drawLine(6, 5, 6, 5);
        g.drawLine(7, 5, 7, 5);
        g.setColor(new Color(255, 212, 0));
        g.drawLine(8, 5, 8, 5);
        g.drawLine(9, 5, 9, 5);
        g.drawLine(10, 5, 10, 5);
        g.drawLine(11, 5, 11, 5);
        g.setColor(new Color(0, 0, 0));
        g.drawLine(12, 5, 12, 5);
        g.drawLine(13, 5, 13, 5);
        g.drawLine(5, 6, 5, 6);
        g.drawLine(6, 6, 6, 6);
        g.setColor(new Color(255, 212, 0));
        g.drawLine(7, 6, 7, 6);
        g.drawLine(8, 6, 8, 6);
        g.drawLine(9, 6, 9, 6);
        g.drawLine(10, 6, 10, 6);
        g.setColor(new Color(0, 0, 0));
        g.drawLine(11, 6, 11, 6);
        g.drawLine(12, 6, 12, 6);
        g.drawLine(4, 7, 4, 7);
        g.drawLine(5, 7, 5, 7);
        g.setColor(new Color(255, 212, 0));
        g.drawLine(6, 7, 6, 7);
        g.drawLine(7, 7, 7, 7);
        g.drawLine(8, 7, 8, 7);
        g.drawLine(9, 7, 9, 7);
        g.setColor(new Color(0, 0, 0));
        g.drawLine(10, 7, 10, 7);
        g.drawLine(11, 7, 11, 7);
        g.drawLine(4, 8, 4, 8);
        g.setColor(new Color(255, 212, 0));
        g.drawLine(5, 8, 5, 8);
        g.drawLine(6, 8, 6, 8);
        g.drawLine(7, 8, 7, 8);
        g.drawLine(8, 8, 8, 8);
        g.setColor(new Color(0, 0, 0));
        g.drawLine(9, 8, 9, 8);
        g.drawLine(10, 8, 10, 8);
        g.drawLine(4, 9, 4, 9);
        g.drawLine(5, 9, 5, 9);
        g.setColor(new Color(255, 212, 0));
        g.drawLine(6, 9, 6, 9);
        g.drawLine(7, 9, 7, 9);
        g.setColor(new Color(0, 0, 0));
        g.drawLine(8, 9, 8, 9);
        g.drawLine(9, 9, 9, 9);
        g.drawLine(3, 10, 3, 10);
        g.drawLine(4, 10, 4, 10);
        g.drawLine(5, 10, 5, 10);
        g.drawLine(6, 10, 6, 10);
        g.drawLine(7, 10, 7, 10);
        g.drawLine(8, 10, 8, 10);
        g.drawLine(3, 11, 3, 11);
        g.drawLine(4, 11, 4, 11);
        g.drawLine(5, 11, 5, 11);
        g.drawLine(6, 11, 6, 11);
        g.drawLine(2, 12, 2, 12);
        g.drawLine(3, 12, 3, 12);
        g.drawLine(4, 12, 4, 12);
        g.drawLine(5, 12, 5, 12);
        g.drawLine(2, 13, 2, 13);
        g.drawLine(3, 13, 3, 13);
        g.drawLine(4, 13, 4, 13);
        g.drawLine(1, 14, 1, 14);
        g.drawLine(2, 14, 2, 14);

        return pencilIco = new ImageIcon(out);
    }

    public static Icon getPaintBucketIcon()
    {
        if (paintIco != null)
            return paintIco;
        BufferedImage out = new BufferedImage(16, 16,
            BufferedImage.TYPE_4BYTE_ABGR);
        Graphics g = out.getGraphics();

        // Created by ImageFileToCode from net/starmen/pkhack/pBucket.gif
        g.setColor(new Color(255, 255, 255));
        g.setColor(new Color(0, 0, 4));
        g.drawLine(8, 1, 8, 1);
        g.setColor(new Color(0, 0, 2));
        g.drawLine(9, 1, 9, 1);
        g.setColor(new Color(0, 0, 0));
        g.drawLine(7, 2, 7, 2);
        g.setColor(new Color(255, 255, 255));
        g.drawLine(8, 2, 8, 2);
        g.setColor(new Color(1, 0, 0));
        g.drawLine(9, 2, 9, 2);
        g.setColor(new Color(0, 0, 4));
        g.drawLine(2, 3, 2, 3);
        g.setColor(new Color(0, 0, 2));
        g.drawLine(3, 3, 3, 3);
        g.drawLine(4, 3, 4, 3);
        g.drawLine(6, 3, 6, 3);
        g.setColor(new Color(255, 255, 255));
        g.drawLine(7, 3, 7, 3);
        g.drawLine(8, 3, 8, 3);
        g.setColor(new Color(255, 254, 255));
        g.drawLine(9, 3, 9, 3);
        g.setColor(new Color(1, 0, 0));
        g.drawLine(10, 3, 10, 3);
        g.setColor(new Color(0, 0, 4));
        g.drawLine(11, 3, 11, 3);
        g.setColor(new Color(0, 0, 0));
        g.drawLine(1, 4, 1, 4);
        g.drawLine(2, 4, 2, 4);
        g.drawLine(3, 4, 3, 4);
        g.setColor(new Color(0, 28, 30));
        g.drawLine(5, 4, 5, 4);
        g.setColor(new Color(189, 255, 255));
        g.drawLine(6, 4, 6, 4);
        g.setColor(new Color(188, 255, 255));
        g.drawLine(7, 4, 7, 4);
        g.drawLine(8, 4, 8, 4);
        g.setColor(new Color(0, 29, 28));
        g.drawLine(9, 4, 9, 4);
        g.setColor(new Color(188, 255, 255));
        g.drawLine(10, 4, 10, 4);
        g.setColor(new Color(255, 255, 255));
        g.drawLine(11, 4, 11, 4);
        g.setColor(new Color(0, 0, 0));
        g.drawLine(12, 4, 12, 4);
        g.setColor(new Color(0, 0, 4));
        g.drawLine(0, 5, 0, 5);
        g.setColor(new Color(0, 0, 0));
        g.drawLine(1, 5, 1, 5);
        g.drawLine(2, 5, 2, 5);
        g.setColor(new Color(0, 0, 4));
        g.drawLine(4, 5, 4, 5);
        g.setColor(new Color(68, 162, 164));
        g.drawLine(5, 5, 5, 5);
        g.setColor(new Color(69, 163, 165));
        g.drawLine(6, 5, 6, 5);
        g.setColor(new Color(67, 163, 161));
        g.drawLine(7, 5, 7, 5);
        g.drawLine(8, 5, 8, 5);
        g.setColor(new Color(67, 163, 162));
        g.drawLine(9, 5, 9, 5);
        g.drawLine(10, 5, 10, 5);
        g.setColor(new Color(0, 0, 0));
        g.drawLine(11, 5, 11, 5);
        g.drawLine(12, 5, 12, 5);
        g.drawLine(13, 5, 13, 5);
        g.setColor(new Color(0, 0, 2));
        g.drawLine(0, 6, 0, 6);
        g.drawLine(1, 6, 1, 6);
        g.setColor(new Color(0, 8, 9));
        g.drawLine(3, 6, 3, 6);
        g.setColor(new Color(82, 156, 157));
        g.drawLine(4, 6, 4, 6);
        g.setColor(new Color(47, 171, 169));
        g.drawLine(5, 6, 5, 6);
        g.setColor(new Color(16, 184, 184));
        g.drawLine(6, 6, 6, 6);
        g.setColor(new Color(0, 192, 191));
        g.drawLine(7, 6, 7, 6);
        g.drawLine(8, 6, 8, 6);
        g.setColor(new Color(84, 156, 155));
        g.drawLine(9, 6, 9, 6);
        g.setColor(new Color(0, 7, 7));
        g.drawLine(10, 6, 10, 6);
        g.setColor(new Color(0, 0, 0));
        g.drawLine(11, 6, 11, 6);
        g.drawLine(12, 6, 12, 6);
        g.setColor(new Color(0, 14, 14));
        g.drawLine(13, 6, 13, 6);
        g.drawLine(14, 6, 14, 6);
        g.setColor(new Color(0, 0, 2));
        g.drawLine(0, 7, 0, 7);
        g.drawLine(1, 7, 1, 7);
        g.setColor(new Color(0, 8, 9));
        g.drawLine(3, 7, 3, 7);
        g.setColor(new Color(0, 22, 23));
        g.drawLine(4, 7, 4, 7);
        g.setColor(new Color(47, 171, 169));
        g.drawLine(5, 7, 5, 7);
        g.setColor(new Color(0, 50, 50));
        g.drawLine(6, 7, 6, 7);
        g.setColor(new Color(0, 192, 191));
        g.drawLine(7, 7, 7, 7);
        g.drawLine(8, 7, 8, 7);
        g.setColor(new Color(0, 22, 21));
        g.drawLine(9, 7, 9, 7);
        g.setColor(new Color(0, 7, 7));
        g.drawLine(10, 7, 10, 7);
        g.setColor(new Color(0, 0, 0));
        g.drawLine(11, 7, 11, 7);
        g.drawLine(12, 7, 12, 7);
        g.setColor(new Color(0, 14, 14));
        g.drawLine(13, 7, 13, 7);
        g.setColor(new Color(100, 148, 148));
        g.drawLine(14, 7, 14, 7);
        g.setColor(new Color(0, 0, 4));
        g.drawLine(15, 7, 15, 7);
        g.setColor(new Color(0, 0, 2));
        g.drawLine(0, 8, 0, 8);
        g.setColor(new Color(0, 0, 0));
        g.drawLine(1, 8, 1, 8);
        g.drawLine(2, 8, 2, 8);
        g.setColor(new Color(0, 29, 28));
        g.drawLine(5, 8, 5, 8);
        g.setColor(new Color(65, 163, 162));
        g.drawLine(6, 8, 6, 8);
        g.setColor(new Color(92, 152, 152));
        g.drawLine(7, 8, 7, 8);
        g.setColor(new Color(0, 11, 11));
        g.drawLine(8, 8, 8, 8);
        g.setColor(new Color(0, 0, 0));
        g.drawLine(9, 8, 9, 8);
        g.drawLine(10, 8, 10, 8);
        g.setColor(new Color(0, 7, 7));
        g.drawLine(11, 8, 11, 8);
        g.setColor(new Color(0, 20, 21));
        g.drawLine(12, 8, 12, 8);
        g.setColor(new Color(33, 177, 177));
        g.drawLine(13, 8, 13, 8);
        g.drawLine(14, 8, 14, 8);
        g.setColor(new Color(0, 0, 4));
        g.drawLine(15, 8, 15, 8);
        g.setColor(new Color(0, 0, 2));
        g.drawLine(0, 9, 0, 9);
        g.setColor(new Color(0, 0, 0));
        g.drawLine(1, 9, 1, 9);
        g.drawLine(2, 9, 2, 9);
        g.setColor(new Color(0, 29, 28));
        g.drawLine(5, 9, 5, 9);
        g.setColor(new Color(65, 163, 162));
        g.drawLine(6, 9, 6, 9);
        g.setColor(new Color(0, 18, 18));
        g.drawLine(7, 9, 7, 9);
        g.setColor(new Color(0, 11, 11));
        g.drawLine(8, 9, 8, 9);
        g.setColor(new Color(0, 0, 0));
        g.drawLine(9, 9, 9, 9);
        g.drawLine(10, 9, 10, 9);
        g.setColor(new Color(0, 7, 7));
        g.drawLine(11, 9, 11, 9);
        g.setColor(new Color(86, 154, 155));
        g.drawLine(12, 9, 12, 9);
        g.setColor(new Color(33, 177, 177));
        g.drawLine(13, 9, 13, 9);
        g.setColor(new Color(0, 43, 43));
        g.drawLine(14, 9, 14, 9);
        g.setColor(new Color(0, 0, 2));
        g.drawLine(0, 10, 0, 10);
        g.setColor(new Color(0, 0, 0));
        g.drawLine(1, 10, 1, 10);
        g.drawLine(2, 10, 2, 10);
        g.setColor(new Color(0, 0, 2));
        g.drawLine(6, 10, 6, 10);
        g.setColor(new Color(0, 0, 0));
        g.drawLine(7, 10, 7, 10);
        g.drawLine(8, 10, 8, 10);
        g.setColor(new Color(0, 7, 7));
        g.drawLine(9, 10, 9, 10);
        g.setColor(new Color(0, 22, 21));
        g.drawLine(10, 10, 10, 10);
        g.setColor(new Color(33, 177, 177));
        g.drawLine(11, 10, 11, 10);
        g.drawLine(12, 10, 12, 10);
        g.setColor(new Color(0, 0, 2));
        g.drawLine(13, 10, 13, 10);
        g.drawLine(0, 11, 0, 11);
        g.setColor(new Color(0, 0, 0));
        g.drawLine(1, 11, 1, 11);
        g.drawLine(2, 11, 2, 11);
        g.drawLine(7, 11, 7, 11);
        g.drawLine(8, 11, 8, 11);
        g.setColor(new Color(0, 7, 7));
        g.drawLine(9, 11, 9, 11);
        g.setColor(new Color(84, 156, 155));
        g.drawLine(10, 11, 10, 11);
        g.setColor(new Color(33, 177, 177));
        g.drawLine(11, 11, 11, 11);
        g.setColor(new Color(0, 43, 43));
        g.drawLine(12, 11, 12, 11);
        g.setColor(new Color(0, 0, 0));
        g.drawLine(1, 12, 1, 12);
        g.drawLine(2, 12, 2, 12);
        g.setColor(new Color(0, 0, 4));
        g.drawLine(8, 12, 8, 12);
        g.setColor(new Color(65, 163, 162));
        g.drawLine(9, 12, 9, 12);
        g.drawLine(10, 12, 10, 12);
        g.setColor(new Color(0, 0, 4));
        g.drawLine(11, 12, 11, 12);
        g.setColor(new Color(0, 0, 0));
        g.drawLine(1, 13, 1, 13);
        g.drawLine(2, 13, 2, 13);
        g.setColor(new Color(0, 29, 28));
        g.drawLine(9, 13, 9, 13);
        g.drawLine(10, 13, 10, 13);
        g.setColor(new Color(0, 0, 4));
        g.drawLine(2, 14, 2, 14);

        return paintIco = new ImageIcon(out);
    }

    public static Icon getEyedropperIcon()
    {
        if (eyeIco != null)
            return eyeIco;
        BufferedImage out = new BufferedImage(16, 16,
            BufferedImage.TYPE_4BYTE_ABGR);
        Graphics g = out.getGraphics();

        // Created by ImageFileToCode from net/starmen/pkhack/eyedropper.gif
        g.setColor(new Color(255, 255, 255));
        g.setColor(new Color(0, 0, 0));
        g.drawLine(9, 1, 9, 1);
        g.drawLine(10, 1, 10, 1);
        g.drawLine(11, 1, 11, 1);
        g.drawLine(6, 2, 6, 2);
        g.drawLine(8, 2, 8, 2);
        g.drawLine(9, 2, 9, 2);
        g.drawLine(10, 2, 10, 2);
        g.drawLine(11, 2, 11, 2);
        g.drawLine(12, 2, 12, 2);
        g.drawLine(7, 3, 7, 3);
        g.drawLine(8, 3, 8, 3);
        g.drawLine(9, 3, 9, 3);
        g.drawLine(10, 3, 10, 3);
        g.drawLine(11, 3, 11, 3);
        g.drawLine(12, 3, 12, 3);
        g.drawLine(13, 3, 13, 3);
        g.drawLine(8, 4, 8, 4);
        g.drawLine(9, 4, 9, 4);
        g.drawLine(10, 4, 10, 4);
        g.drawLine(11, 4, 11, 4);
        g.drawLine(12, 4, 12, 4);
        g.drawLine(13, 4, 13, 4);
        g.drawLine(14, 4, 14, 4);
        g.drawLine(7, 5, 7, 5);
        g.drawLine(9, 5, 9, 5);
        g.drawLine(10, 5, 10, 5);
        g.drawLine(11, 5, 11, 5);
        g.drawLine(12, 5, 12, 5);
        g.drawLine(13, 5, 13, 5);
        g.drawLine(14, 5, 14, 5);
        g.drawLine(6, 6, 6, 6);
        g.drawLine(10, 6, 10, 6);
        g.drawLine(11, 6, 11, 6);
        g.drawLine(12, 6, 12, 6);
        g.drawLine(13, 6, 13, 6);
        g.drawLine(14, 6, 14, 6);
        g.drawLine(5, 7, 5, 7);
        g.drawLine(11, 7, 11, 7);
        g.drawLine(12, 7, 12, 7);
        g.drawLine(13, 7, 13, 7);
        g.drawLine(4, 8, 4, 8);
        g.drawLine(10, 8, 10, 8);
        g.drawLine(12, 8, 12, 8);
        g.drawLine(3, 9, 3, 9);
        g.drawLine(9, 9, 9, 9);
        g.drawLine(13, 9, 13, 9);
        g.drawLine(2, 10, 2, 10);
        g.drawLine(8, 10, 8, 10);
        g.drawLine(2, 11, 2, 11);
        g.drawLine(7, 11, 7, 11);
        g.setColor(new Color(94, 192, 209));
        g.drawLine(1, 12, 1, 12);
        g.drawLine(2, 12, 2, 12);
        g.setColor(new Color(0, 0, 0));
        g.drawLine(3, 12, 3, 12);
        g.drawLine(6, 12, 6, 12);
        g.setColor(new Color(94, 192, 209));
        g.drawLine(1, 13, 1, 13);
        g.drawLine(2, 13, 2, 13);
        g.drawLine(3, 13, 3, 13);
        g.setColor(new Color(0, 0, 0));
        g.drawLine(4, 13, 4, 13);
        g.drawLine(5, 13, 5, 13);
        g.setColor(new Color(94, 192, 209));
        g.drawLine(6, 13, 6, 13);
        g.drawLine(7, 13, 7, 13);
        g.drawLine(2, 14, 2, 14);
        g.drawLine(3, 14, 3, 14);
        g.drawLine(4, 14, 4, 14);
        g.drawLine(5, 14, 5, 14);
        g.drawLine(6, 14, 6, 14);
        g.drawLine(7, 14, 7, 14);
        g.drawLine(4, 15, 4, 15);
        g.drawLine(5, 15, 5, 15);

        return eyeIco = new ImageIcon(out);
    }

    public static Icon getLineIcon()
    {
        if (lineIco != null)
            return lineIco;
        BufferedImage out = new BufferedImage(16, 16,
            BufferedImage.TYPE_4BYTE_ABGR);
        Graphics g = out.getGraphics();

        // Created by ImageFileToCode from net/starmen/pkhack/line.gif
        g.setColor(new Color(255, 255, 255));
        g.setColor(new Color(0, 0, 0));
        g.drawLine(2, 1, 2, 1);
        g.drawLine(1, 2, 1, 2);
        g.drawLine(2, 2, 2, 2);
        g.drawLine(3, 2, 3, 2);
        g.drawLine(1, 3, 1, 3);
        g.drawLine(2, 3, 2, 3);
        g.drawLine(3, 3, 3, 3);
        g.drawLine(4, 3, 4, 3);
        g.drawLine(2, 4, 2, 4);
        g.drawLine(3, 4, 3, 4);
        g.drawLine(4, 4, 4, 4);
        g.drawLine(5, 4, 5, 4);
        g.drawLine(3, 5, 3, 5);
        g.drawLine(4, 5, 4, 5);
        g.drawLine(5, 5, 5, 5);
        g.drawLine(6, 5, 6, 5);
        g.drawLine(4, 6, 4, 6);
        g.drawLine(5, 6, 5, 6);
        g.drawLine(6, 6, 6, 6);
        g.drawLine(7, 6, 7, 6);
        g.drawLine(5, 7, 5, 7);
        g.drawLine(6, 7, 6, 7);
        g.drawLine(7, 7, 7, 7);
        g.drawLine(8, 7, 8, 7);
        g.drawLine(6, 8, 6, 8);
        g.drawLine(7, 8, 7, 8);
        g.drawLine(8, 8, 8, 8);
        g.drawLine(9, 8, 9, 8);
        g.drawLine(7, 9, 7, 9);
        g.drawLine(8, 9, 8, 9);
        g.drawLine(9, 9, 9, 9);
        g.drawLine(10, 9, 10, 9);
        g.drawLine(8, 10, 8, 10);
        g.drawLine(9, 10, 9, 10);
        g.drawLine(10, 10, 10, 10);
        g.drawLine(11, 10, 11, 10);
        g.drawLine(9, 11, 9, 11);
        g.drawLine(10, 11, 10, 11);
        g.drawLine(11, 11, 11, 11);
        g.drawLine(12, 11, 12, 11);
        g.drawLine(10, 12, 10, 12);
        g.drawLine(11, 12, 11, 12);
        g.drawLine(12, 12, 12, 12);
        g.drawLine(13, 12, 13, 12);
        g.drawLine(11, 13, 11, 13);
        g.drawLine(12, 13, 12, 13);
        g.drawLine(13, 13, 13, 13);
        g.drawLine(14, 13, 14, 13);
        g.drawLine(12, 14, 12, 14);
        g.drawLine(13, 14, 13, 14);

        return lineIco = new ImageIcon(out);
    }

    public static Icon getHFlipIcon()
    {
        if (hFlipIco != null)
            return hFlipIco;
        BufferedImage out = new BufferedImage(16, 16,
            BufferedImage.TYPE_4BYTE_ABGR);
        Graphics g = out.getGraphics();

        // Created by ImageFileToCode from net/starmen/pkhack/hFlip.gif
        g.setColor(new Color(255, 255, 255));
        g.setColor(new Color(0, 0, 0));
        g.drawLine(4, 3, 4, 3);
        g.drawLine(11, 3, 11, 3);
        g.drawLine(3, 4, 3, 4);
        g.drawLine(4, 4, 4, 4);
        g.drawLine(11, 4, 11, 4);
        g.drawLine(12, 4, 12, 4);
        g.drawLine(2, 5, 2, 5);
        g.drawLine(3, 5, 3, 5);
        g.drawLine(4, 5, 4, 5);
        g.drawLine(11, 5, 11, 5);
        g.drawLine(12, 5, 12, 5);
        g.drawLine(13, 5, 13, 5);
        g.drawLine(1, 6, 1, 6);
        g.drawLine(2, 6, 2, 6);
        g.drawLine(3, 6, 3, 6);
        g.drawLine(4, 6, 4, 6);
        g.drawLine(11, 6, 11, 6);
        g.drawLine(12, 6, 12, 6);
        g.drawLine(13, 6, 13, 6);
        g.drawLine(14, 6, 14, 6);
        g.drawLine(0, 7, 0, 7);
        g.drawLine(1, 7, 1, 7);
        g.drawLine(2, 7, 2, 7);
        g.drawLine(3, 7, 3, 7);
        g.drawLine(4, 7, 4, 7);
        g.drawLine(5, 7, 5, 7);
        g.drawLine(6, 7, 6, 7);
        g.drawLine(7, 7, 7, 7);
        g.drawLine(8, 7, 8, 7);
        g.drawLine(9, 7, 9, 7);
        g.drawLine(10, 7, 10, 7);
        g.drawLine(11, 7, 11, 7);
        g.drawLine(12, 7, 12, 7);
        g.drawLine(13, 7, 13, 7);
        g.drawLine(14, 7, 14, 7);
        g.drawLine(15, 7, 15, 7);
        g.drawLine(0, 8, 0, 8);
        g.drawLine(1, 8, 1, 8);
        g.drawLine(2, 8, 2, 8);
        g.drawLine(3, 8, 3, 8);
        g.drawLine(4, 8, 4, 8);
        g.drawLine(5, 8, 5, 8);
        g.drawLine(6, 8, 6, 8);
        g.drawLine(7, 8, 7, 8);
        g.drawLine(8, 8, 8, 8);
        g.drawLine(9, 8, 9, 8);
        g.drawLine(10, 8, 10, 8);
        g.drawLine(11, 8, 11, 8);
        g.drawLine(12, 8, 12, 8);
        g.drawLine(13, 8, 13, 8);
        g.drawLine(14, 8, 14, 8);
        g.drawLine(15, 8, 15, 8);
        g.drawLine(1, 9, 1, 9);
        g.drawLine(2, 9, 2, 9);
        g.drawLine(3, 9, 3, 9);
        g.drawLine(4, 9, 4, 9);
        g.drawLine(11, 9, 11, 9);
        g.drawLine(12, 9, 12, 9);
        g.drawLine(13, 9, 13, 9);
        g.drawLine(14, 9, 14, 9);
        g.drawLine(2, 10, 2, 10);
        g.drawLine(3, 10, 3, 10);
        g.drawLine(4, 10, 4, 10);
        g.drawLine(11, 10, 11, 10);
        g.drawLine(12, 10, 12, 10);
        g.drawLine(13, 10, 13, 10);
        g.drawLine(3, 11, 3, 11);
        g.drawLine(4, 11, 4, 11);
        g.drawLine(11, 11, 11, 11);
        g.drawLine(12, 11, 12, 11);
        g.drawLine(4, 12, 4, 12);
        g.drawLine(11, 12, 11, 12);

        return hFlipIco = new ImageIcon(out);
    }

    public static Icon getVFlipIcon()
    {
        if (vFlipIco != null)
            return vFlipIco;
        BufferedImage out = new BufferedImage(16, 16,
            BufferedImage.TYPE_4BYTE_ABGR);
        Graphics g = out.getGraphics();

        // Created by ImageFileToCode from net/starmen/pkhack/vFlip.gif
        g.setColor(new Color(255, 255, 255));
        g.setColor(new Color(0, 0, 0));
        g.drawLine(7, 0, 7, 0);
        g.drawLine(8, 0, 8, 0);
        g.drawLine(6, 1, 6, 1);
        g.drawLine(7, 1, 7, 1);
        g.drawLine(8, 1, 8, 1);
        g.drawLine(9, 1, 9, 1);
        g.drawLine(5, 2, 5, 2);
        g.drawLine(6, 2, 6, 2);
        g.drawLine(7, 2, 7, 2);
        g.drawLine(8, 2, 8, 2);
        g.drawLine(9, 2, 9, 2);
        g.drawLine(10, 2, 10, 2);
        g.drawLine(4, 3, 4, 3);
        g.drawLine(5, 3, 5, 3);
        g.drawLine(6, 3, 6, 3);
        g.drawLine(7, 3, 7, 3);
        g.drawLine(8, 3, 8, 3);
        g.drawLine(9, 3, 9, 3);
        g.drawLine(10, 3, 10, 3);
        g.drawLine(11, 3, 11, 3);
        g.drawLine(3, 4, 3, 4);
        g.drawLine(4, 4, 4, 4);
        g.drawLine(5, 4, 5, 4);
        g.drawLine(6, 4, 6, 4);
        g.drawLine(7, 4, 7, 4);
        g.drawLine(8, 4, 8, 4);
        g.drawLine(9, 4, 9, 4);
        g.drawLine(10, 4, 10, 4);
        g.drawLine(11, 4, 11, 4);
        g.drawLine(12, 4, 12, 4);
        g.drawLine(7, 5, 7, 5);
        g.drawLine(8, 5, 8, 5);
        g.drawLine(7, 6, 7, 6);
        g.drawLine(8, 6, 8, 6);
        g.drawLine(7, 7, 7, 7);
        g.drawLine(8, 7, 8, 7);
        g.drawLine(7, 8, 7, 8);
        g.drawLine(8, 8, 8, 8);
        g.drawLine(7, 9, 7, 9);
        g.drawLine(8, 9, 8, 9);
        g.drawLine(7, 10, 7, 10);
        g.drawLine(8, 10, 8, 10);
        g.drawLine(3, 11, 3, 11);
        g.drawLine(4, 11, 4, 11);
        g.drawLine(5, 11, 5, 11);
        g.drawLine(6, 11, 6, 11);
        g.drawLine(7, 11, 7, 11);
        g.drawLine(8, 11, 8, 11);
        g.drawLine(9, 11, 9, 11);
        g.drawLine(10, 11, 10, 11);
        g.drawLine(11, 11, 11, 11);
        g.drawLine(12, 11, 12, 11);
        g.drawLine(4, 12, 4, 12);
        g.drawLine(5, 12, 5, 12);
        g.drawLine(6, 12, 6, 12);
        g.drawLine(7, 12, 7, 12);
        g.drawLine(8, 12, 8, 12);
        g.drawLine(9, 12, 9, 12);
        g.drawLine(10, 12, 10, 12);
        g.drawLine(11, 12, 11, 12);
        g.drawLine(5, 13, 5, 13);
        g.drawLine(6, 13, 6, 13);
        g.drawLine(7, 13, 7, 13);
        g.drawLine(8, 13, 8, 13);
        g.drawLine(9, 13, 9, 13);
        g.drawLine(10, 13, 10, 13);
        g.drawLine(6, 14, 6, 14);
        g.drawLine(7, 14, 7, 14);
        g.drawLine(8, 14, 8, 14);
        g.drawLine(9, 14, 9, 14);
        g.drawLine(7, 15, 7, 15);
        g.drawLine(8, 15, 8, 15);

        return vFlipIco = new ImageIcon(out);
    }
}