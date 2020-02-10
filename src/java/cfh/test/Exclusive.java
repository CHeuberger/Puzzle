package cfh.test;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JOptionPane;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;

public class Exclusive {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Exclusive::new);
    }
    
    
    private Exclusive() {
        GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        if (!device.isFullScreenSupported()) {
            JOptionPane.showMessageDialog(null, "Full Screen not supported");
            return;
        }
        
        JWindow window = new JWindow();
        window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent ev) {
                device.setFullScreenWindow(null);
                System.out.println("closed");
            }
        });
        window.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent ev) {
                System.out.println(ev);
                window.dispose();
            }
        });
        device.setFullScreenWindow(window);
        window.setVisible(true);
    }
}
