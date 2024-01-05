/*
 * Created on Mar 30, 2003
 */
package ebhack;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;

/**
 * A wrapper for a <code>JComboBox</code> that has a text field and a search
 * button.
 * 
 * @author AnyoneEB
 * @see #comboBox
 */
public class JSearchableComboBox extends JComponent implements ActionListener,
    KeyListener
{
    /** Make combo box not searchable. */
    public static final int SEARCH_NONE = 0;
    /** Show a search text field to the left of the combo box. */
    public static final int SEARCH_LEFT = 1;
    /** Show a search text field to the right of the combo box. */
    public static final int SEARCH_RIGHT = 2;
    /** Search by typing in combo box and hitting [enter]. */
    public static final int SEARCH_EDIT = 3;

    /**
     * The JComboBox this is a wrapper for.
     */
    public JComboBox comboBox;
    private JTextField tf;
    private JButton findb;
    /** <code>JLabel</code> put in front of the combo box. */
    protected JLabel label;

    /**
     * Creates a new JSearchableComboBox wrapper for the specified JComboBox.
     * 
     * @param jcb The JComboBox this is a wrapper for.
     * @param text Text to label the JComboBox with.
     */
    public JSearchableComboBox(JComboBox jcb, String text)
    {
        this(jcb, text, SEARCH_LEFT);
    }

    /**
     * Creates a new JSearchableComboBox wrapper for the specified JComboBox.
     * 
     * @param jcb The JComboBox this is a wrapper for.
     * @param text Text to label the JComboBox with.
     * @param smode Search mode, one of {@link #SEARCH_NONE},
     *            {@link #SEARCH_LEFT},{@link #SEARCH_RIGHT},
     *            {@link #SEARCH_EDIT}.
     */
    public JSearchableComboBox(JComboBox jcb, String text, int smode)
    {
        this(jcb, text, 10, smode);
    }

    /**
     * Creates a new JSearchableComboBox wrapper for the specified JComboBox.
     * 
     * @param jcb The JComboBox this is a wrapper for.
     * @param text Text to label the JComboBox with.
     * @param searchSize the size of the search text field if <code>smode</code>
     *            is <code>SEARCH_LEFT</code> or <code>SEARCH_RIGHT</code>
     * @param smode Search mode, one of {@link #SEARCH_NONE},
     *            {@link #SEARCH_LEFT},{@link #SEARCH_RIGHT},
     *            {@link #SEARCH_EDIT}.
     */
    public JSearchableComboBox(final JComboBox jcb, String text,
        int searchSize, int smode)
    {
        super();
        this.comboBox = jcb;
        if (smode == SEARCH_EDIT)
        {
            jcb.setEditable(true);
            jcb.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent ae)
                {
                    if (jcb.getSelectedIndex() == -1
                        && jcb.getSelectedItem() != null)
                        ToolModule
                            .search(jcb.getSelectedItem().toString(), jcb);
                }
            });
        }
        else if (smode != SEARCH_NONE)
        {
            tf = new JTextField(searchSize);
            findb = new JButton("Find");
        }
        this.label = new JLabel(text);
        initGraphics(smode);
    }

    private void initGraphics(int smode)
    {
        setLayout(new BorderLayout());

        if (smode == SEARCH_EDIT || smode == SEARCH_NONE)
        {
            add(ToolModule.pairComponents(label, comboBox, true));
        }
        else
        {
            findb.addActionListener(this);
            tf.addKeyListener(this);

            add(ToolModule.pairComponents(tf, findb, true),
                (smode == SEARCH_LEFT ? BorderLayout.WEST : BorderLayout.EAST));
            add(ToolModule.pairComponents(label, comboBox, true),
                (smode == SEARCH_LEFT ? BorderLayout.EAST : BorderLayout.WEST));
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent ae)
    {
        ToolModule.search(tf.getText().toLowerCase(), comboBox);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
     */
    public void keyTyped(KeyEvent ke)
    {}

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
     */
    public void keyPressed(KeyEvent ke)
    {}

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
     */
    public void keyReleased(KeyEvent ke)
    {
        if (ke.getKeyCode() == KeyEvent.VK_ENTER)
        {
            ToolModule.search(tf.getText(), comboBox);
        }
    }

    public void setEnabled(boolean enable)
    {
        super.setEnabled(enable);
        if (enable)
        {
            if (tf != null)
            {
                tf.setEnabled(true);
                findb.setEnabled(true);
            }
            comboBox.setEnabled(true);
            label.setEnabled(true);
        }
        else
        {
            if (tf != null)
            {
                tf.setEnabled(false);
                findb.setEnabled(false);
            }
            comboBox.setEnabled(false);
            label.setEnabled(false);
        }
    }

    /**
     * Returns the <code>JLabel</code> identifying this <code>JComboBox</code>.
     * 
     * @return the <code>JLabel</code> labeling this
     * @see #label
     */
    public JLabel getLabel()
    {
        return label;
    }

    public int getSelectedIndex()
    {
        return comboBox.getSelectedIndex();
    }

    public Object getSelectedItem()
    {
        return comboBox.getSelectedItem();
    }

    public void setSelectedIndex(int index)
    {
        comboBox.setSelectedIndex(index);
        if (comboBox.isEditable())
            comboBox.getEditor().setItem(comboBox.getSelectedItem());
        comboBox.repaint();
    }

    public void setActionCommand(String s)
    {
        comboBox.setActionCommand(s);
    }

    public void addActionListener(ActionListener al)
    {
        comboBox.addActionListener(al);
    }

    public String getActionCommand()
    {
        return comboBox.getActionCommand();
    }

    public JComboBox getJComboBox()
    {
        return comboBox;
    }

    public void setToolTipText(String text)
    {
        super.setToolTipText(text);
        comboBox.setToolTipText(text);
        label.setToolTipText(text);
        if (tf != null)
        {
            tf.setToolTipText(text);
            findb.setToolTipText(text);
        }
    }
}