package cfh;

import java.awt.Component;
import java.awt.HeadlessException;
import java.io.File;
import java.util.prefs.Preferences;

import javax.swing.JFileChooser;

/**
 * @version 1.1, 06.03.2020
 */
public class FileChooser extends JFileChooser {

    private static final long serialVersionUID = 2779986381458039130L;

    private final String PREF_OPEN = "open";
    private final String PREF_SAVE = "save";
    
    private final Preferences prefs;
    
    public FileChooser() {
        String classname = Thread.currentThread().getStackTrace()[2].getClassName();
        prefs = Preferences.userRoot().node("/" + classname.replace('.', '/'));
    }

    @Override
    public int showOpenDialog(Component parent) throws HeadlessException {
        String file = prefs.get(PREF_OPEN, "");
        setSelectedFile(new File(file));
        int option = super.showOpenDialog(parent);
        if (option == APPROVE_OPTION) {
            file = getSelectedFile().getAbsolutePath();
            prefs.put(PREF_OPEN, file);
        }
        return option;
    }

    @Override
    public int showSaveDialog(Component parent) throws HeadlessException {
        String file = prefs.get(PREF_SAVE, null);
        if (file == null) {
            file = prefs.get(PREF_OPEN, "");
            System.out.println(file);
            if (!file.isEmpty()) {
                int i = file.lastIndexOf('.');
                if (i != -1) {
                    file = file.substring(0, i);
                }
            }
        }
        setSelectedFile(new File(file));
        int option = super.showSaveDialog(parent);
        if (option == APPROVE_OPTION) {
            file = getSelectedFile().getAbsolutePath();
            prefs.put(PREF_SAVE, file);
        }
        return option;
    }
}
