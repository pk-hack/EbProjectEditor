/*
 * Created on Jan 21, 2004
 */
package ebhack;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.KeyStroke;

/**
 * TODO Write javadoc for this class
 * 
 * @author AnyoneEB
 */
public class PrefsCheckBox extends JCheckBoxMenuItem
{
    private YMLPreferences prefs;
    private String pref;
    private boolean defVal;

    private void init()
    {
        if (prefs.hasValue(pref))
            this.setSelected(prefs.getValueAsBoolean(pref));
        else
            prefs.setValueAsBoolean(pref, defVal);
        super.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                prefs.setValueAsBoolean(pref, isSelected());

                for (Iterator i = listeners.iterator(); i.hasNext();)
                {
                    ((ActionListener) i.next())
                        .actionPerformed(new ActionEvent(this, 0,
                            getActionCommand()));
                }
            }
        });
    }

    private ArrayList listeners = new ArrayList();

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

    /**
     * Contructor a <code>JCheckBox</code> with a given title and preference
     * to edit and a default value of false.
     * 
     * @param label text to label this with
     * @param prefs class hold preferences
     * @param pref preference name to be editing
     */
    public PrefsCheckBox(String label, YMLPreferences prefs, String pref) {
        this(label, prefs, pref, false);
    }

    /**
     * Contructor a <code>JCheckBox</code> with a given title and preference
     * to edit.
     * 
     * @param label text to label this with
     * @param prefs class hold preferences
     * @param pref preference name to be editing
     * @param def default value
     */
    public PrefsCheckBox(String label, YMLPreferences prefs, String pref,
        boolean def) {
        super(label, def);
        this.prefs = prefs;
        this.pref = pref;
        this.defVal = def;

        init();
    }

    public PrefsCheckBox(String label, YMLPreferences prefs, String pref,
        boolean def, char m) {
        this(label, prefs, pref, def);
        this.setMnemonic(m);
    }

    public PrefsCheckBox(String label, YMLPreferences prefs, String pref,
        boolean def, char m, String key) {
        this(label, prefs, pref, def, m);
        if (key != null) this.setAccelerator(KeyStroke.getKeyStroke(key));
    }

    public PrefsCheckBox(String label, YMLPreferences prefs, String pref,
        boolean def, char m, String key, String ac, ActionListener al) {
        this(label, prefs, pref, def, m, key);
        if (ac != null) this.setActionCommand(ac);
        if (al != null) this.addActionListener(al);
    }

    private PrefsCheckBox() {
        super();
    }
}
