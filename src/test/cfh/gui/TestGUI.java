package cfh.gui;

import java.awt.Color;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;


public class TestGUI {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(TestGUI::new);
    }
    
    private TestGUI() {
        SubPanel test1 = new SubPanel("test1");
        test1.setBounds(50, 50, 250, 150);
        JPanel panel = new JPanel();
        panel.setBackground(Color.BLACK);
        test1.add(panel);
        
        SubPanel test2 = new SubPanel("test2");
        test2.setBounds(150, 150, 250, 150);
        
        MainPanel main = new MainPanel();
        main.add(test1);
        main.add(test2);
        
        JFrame frame = new JFrame();
        // XXX
        frame.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent ev) {
                if (ev.getKeyCode() == ev.VK_ESCAPE) {
                    frame.dispose();
                }
            }
        });
        frame.add(main);
        frame.setDefaultCloseOperation(frame.DISPOSE_ON_CLOSE);
        frame.setUndecorated(true);
        frame.setSize(Toolkit.getDefaultToolkit().getScreenSize());
        frame.validate();
        frame.setVisible(true);
    }
}
