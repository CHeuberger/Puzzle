package cfh.test;

import static java.awt.event.InputEvent.ALT_DOWN_MASK;
import static java.awt.event.InputEvent.CTRL_DOWN_MASK;
import static java.awt.event.InputEvent.SHIFT_DOWN_MASK;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;

public class FullPanel extends JPanel {

    static final int MENU_HEIGHT = 20;
    
    private boolean showMenu = false;

    FullPanel() {
        setBackground(Color.BLACK);
        setBorder(new CompoundBorder(
                new BevelBorder(BevelBorder.RAISED, Color.GRAY.brighter(), Color.GRAY.darker()),
                new BevelBorder(BevelBorder.LOWERED, Color.GRAY.brighter(), Color.GRAY.darker())
                ));
        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent ev) {
                System.out.println(ev);
                doMouseMoved(ev);
            }
        });
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (showMenu) {
            Graphics2D gg = (Graphics2D) g.create();
            try  {
                paintMenu(gg);
            } finally {
                gg.dispose();
            }
        }
    }
    
    private void paintMenu(Graphics2D gg) {
        gg.setBackground(Color.GRAY);
        gg.clearRect(0, 0, getWidth(), MENU_HEIGHT);
        gg.setColor(Color.BLACK);
        gg.drawString("Quit", 10, 10);
    }
    
    private void doMouseMoved(MouseEvent ev) {
        if ((ev.getModifiersEx() & (ALT_DOWN_MASK | CTRL_DOWN_MASK | SHIFT_DOWN_MASK)) == 0) {
            boolean show = ev.getY() < MENU_HEIGHT;
            if (show != showMenu) {
                showMenu = show;
                System.out.println(showMenu);
                repaint();
            }
        }
    }
}
