package cfh.gui;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;


public class SubPanel extends JPanel {

    public SubPanel(String title) {
        setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.GREEN),
            title,
            TitledBorder.DEFAULT_JUSTIFICATION,
            TitledBorder.DEFAULT_POSITION,
            null,
            Color.GREEN
            ));
        setBackground(Color.BLACK);
        setLayout(new BorderLayout());
    }
}
