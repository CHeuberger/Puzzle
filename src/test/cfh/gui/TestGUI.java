package cfh.gui;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;


public class TestGUI {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(TestGUI::new);
    }
    
    private TestGUI() {
        SubPanel test = new SubPanel("test");
        test.setBounds(50, 50, 250, 150);
        
        MainPanel main = new MainPanel();
        main.add(test);
        
        JFrame frame = new JFrame();
        frame.add(main);
        frame.setDefaultCloseOperation(frame.DISPOSE_ON_CLOSE);
        frame.setUndecorated(false);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        frame.setExtendedState(frame.MAXIMIZED_BOTH);
        frame.setVisible(true);
    }
}
