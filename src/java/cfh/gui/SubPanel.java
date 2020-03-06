package cfh.gui;

import javax.swing.BorderFactory;
import javax.swing.JPanel;


public class SubPanel extends JPanel {

    public SubPanel(String title) {
        setBorder(BorderFactory.createTitledBorder(title));
    }
}
