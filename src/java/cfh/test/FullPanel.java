package cfh.test;

import static java.awt.event.InputEvent.*;

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
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
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
        private MenuItem highlight = null;

        FullMenu(JComponent parent) {
            this.parent = parent;
            
            items = Collections.unmodifiableList(Arrays.asList(
                    new MenuItem("Test1",   0, 100, this::test),
                    new MenuItem("Test2", 100, 100, this::test),
                    new MenuItem("QUIT", -100, 100, this::doQuit)
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
                @Override
                public void mouseDragged(MouseEvent ev) {
                    doMouseDragged(ev);
                }
            });
        }
        
        private void test() {
            JOptionPane.showMessageDialog(null, "test");
        }
        
        private void doQuit() {
            // TODO confirm
            SwingUtilities.windowForComponent(parent).dispose();
        }
        
        private void doMousePressed(MouseEvent ev) {
            if (showMenu && SwingUtilities.isLeftMouseButton(ev)) {
                highlight = items.stream().filter(i -> i.contains(ev, parent.getWidth())).findFirst().orElse(null);
                parent.repaint();
            }
        }
        
        private void doMouseReleased(MouseEvent ev) {
            if (showMenu && SwingUtilities.isLeftMouseButton(ev)) {
                if (highlight != null) {
                    highlight.action.run();
                }
                // TODO
                highlight = null;
                parent.repaint();
            }
        }
        
        private void doMouseMoved(MouseEvent ev) {
            if (!showMenu && (ev.getModifiersEx() & (BUTTON1_DOWN_MASK | BUTTON2_DOWN_MASK | BUTTON3_DOWN_MASK)) != 0)
                return;
            boolean show = ev.getY() < MENU_HEIGHT;
            if (show != showMenu) {
                showMenu = show;
                highlight = null;
                parent.repaint();
            }
        }

        private void doMouseDragged(MouseEvent ev) {
            if (showMenu && highlight != null) {
                if (!highlight.contains(ev, parent.getWidth())) {
                    highlight = null;
                    parent.repaint();
                }
            }
            doMouseMoved(ev);
        }
        
        void paint(Graphics2D gg) {
            gg.setColor(new Color(80, 80, 80, 210));
            gg.fillRect(0, 0, parent.getWidth(), MENU_HEIGHT);
            FontMetrics fm = gg.getFontMetrics();
            int y = fm.getHeight();
            for (MenuItem item : items) {
                int x = item.x<0 ? item.x+parent.getWidth() : item.x;
                if (item == highlight) {
                    gg.setColor(Color.GREEN.darker());
                    gg.fillRect(x, 0, item.w, MENU_HEIGHT);
                }
                gg.setColor(Color.GREEN.brighter());
                gg.drawLine(x, 0, x, MENU_HEIGHT);
                gg.drawLine(x+item.w, 0, x+item.w, MENU_HEIGHT);
                x += (item.w - fm.stringWidth(item.text)) / 2;
                gg.drawString(item.text, x, y);
            }
        }
        
        //==========================================================================================
        
        private static class MenuItem {
            
            final String text;
            final int x;
            final int w;
            final Runnable action;
            
            private MenuItem(String text, int x, int w, Runnable action) {
                this.text = text;
                this.x = x;
                this.w = w;
                this.action = action;
            }
            
            private boolean contains(MouseEvent ev, int width) {
                int left = x < 0 ? x + width : x;
                int right = left + w;
                return left <= ev.getX() && ev.getX() <= right;
            }
            
            @Override
            public String toString() {
                return text;
            }
        }
    }
}
