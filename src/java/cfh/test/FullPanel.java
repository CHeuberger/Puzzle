package cfh.test;

import static java.awt.event.InputEvent.ALT_DOWN_MASK;
import static java.awt.event.InputEvent.CTRL_DOWN_MASK;
import static java.awt.event.InputEvent.SHIFT_DOWN_MASK;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;


public class FullPanel extends JPanel {

    static final int MENU_HEIGHT = 22;
    
    private final FullMenu menu;
    
    private BufferedImage testImage;


    FullPanel() {
        setBackground(Color.BLACK);
        setBorder(new CompoundBorder(
                new BevelBorder(BevelBorder.RAISED, Color.GRAY.brighter(), Color.GRAY.darker()),
                new BevelBorder(BevelBorder.LOWERED, Color.GRAY.brighter(), Color.GRAY.darker())
                ));
//        setBorder(new javax.swing.border.LineBorder(Color.BLUE, 10));
        setLayout(null);
        
        menu = new FullMenu(this);
        
        try {
            testImage = ImageIO.read(new File("pics/bus.png"));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    @Override
    protected void paintChildren(Graphics g) {
        super.paintChildren(g);
        Graphics2D gg = (Graphics2D) g.create();
        try {
            Insets border = getBorder().getBorderInsets(this);
            gg.translate(border.left, border.top);
            int w = getWidth()-border.left-border.right;
            gg.setClip(0, 0, w, getHeight()-border.top-border.bottom);
            if (testImage != null) {
                gg.drawImage(testImage, 50, -5, 120, 120, this);
            }

            if (menu.showMenu) {
                gg.setClip(0, 0, w, MENU_HEIGHT);
                menu.paint(gg);
            }
        } finally {
            gg.dispose();
        }
    }
    
    //==============================================================================================
    
    private static class FullMenu {
        
        private final JComponent parent;
        private final List<MenuItem> items;
        
        private boolean showMenu = false;

        FullMenu(JComponent parent) {
            this.parent = parent;
            
            int w = 0;
            items = Collections.unmodifiableList(Arrays.asList(
                    new MenuItem("Test1",   0, 100),
                    new MenuItem("Test2", 100, 100)
                    ));
            
            parent.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent ev) {
                    doMousePressed(ev);
                }
                @Override
                public void mouseReleased(MouseEvent ev) {
                    doMouseReleased(ev);
                }
            });
            parent.addMouseMotionListener(new MouseAdapter() {
                @Override
                public void mouseMoved(MouseEvent ev) {
                    doMouseMoved(ev);
                }
            });
        }
        
        
        private void doMousePressed(MouseEvent ev) {
            if (showMenu) {
            }
        }
        
        private void doMouseReleased(MouseEvent ev) {
            if (showMenu) {
            }
        }
        
        private void doMouseMoved(MouseEvent ev) {
            if ((ev.getModifiersEx() & (ALT_DOWN_MASK | CTRL_DOWN_MASK | SHIFT_DOWN_MASK)) == 0) {
                boolean show = ev.getY() < MENU_HEIGHT;
                if (show != showMenu) {
                    showMenu = show;
                    parent.repaint();
                }
            }
        }

        void paint(Graphics2D gg) {
            gg.setColor(new Color(80, 80, 80, 210));
            gg.fillRect(0, 0, parent.getWidth(), MENU_HEIGHT);
            gg.setColor(Color.GREEN.brighter());
            FontMetrics fm = gg.getFontMetrics();
            int y = fm.getHeight();
            for (MenuItem item : items) {
                gg.drawLine(item.x, 0, item.x, MENU_HEIGHT);
                gg.drawLine(item.x+item.w, 0, item.x+item.w, MENU_HEIGHT);
                int x = item.x + (item.w - fm.stringWidth(item.text)) / 2;
                gg.drawString(item.text, x, y);
            }
        }
        
        //==========================================================================================
        
        private static class MenuItem {
            
            final String text;
            final int x;
            final int w;
            
            private MenuItem(String text, int x, int w) {
                this.text = text;
                this.x = x;
                this.w = w;
            }
        }
    }
}
