package cfh.test;

import java.awt.Color;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;


public class FullScreen {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(FullScreen::new);
    }

    //----------------------------------------------------------------------------------------------
    
    private final JFrame frame;
    
    private FullScreen() {
        FullPanel mainPanel = new FullPanel();
        
        frame = new JFrame();
        frame.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent ev) {
                if (ev.getKeyChar() == KeyEvent.VK_ESCAPE) {
                    frame.dispose();
                }
            }
        });
        frame.setContentPane(mainPanel);
        frame.setDefaultCloseOperation(frame.DO_NOTHING_ON_CLOSE);
        frame.setUndecorated(true);
        frame.getContentPane().setBackground(Color.BLACK);
        frame.setSize(600, 600);
        frame.setLocationRelativeTo(null);
        frame.setExtendedState(frame.MAXIMIZED_BOTH);
        frame.setVisible(true);
    }
}
