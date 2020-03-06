package cfh.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.border.Border;


public class MainPanel extends JPanel {
    
    private final List<SubPanel> panels = new ArrayList<>();

    public MainPanel() {
        setBackground(Color.BLACK);
        setBorder(new javax.swing.border.LineBorder(Color.BLUE, 20));
        setLayout(null);

        PanelDragged adapter = new PanelDragged();
        addMouseListener(adapter);
        addMouseMotionListener(adapter);
    }
    
    public void add(SubPanel panel) {
        super.add(panel);
        panels.add(panel);
    }
    
    @Override
    protected void paintChildren(Graphics g) {
        Graphics2D gg = (Graphics2D) g.create();
        try {
            Border border = getBorder();
            int w = getWidth();
            int h = getHeight();
            if (border != null) {
                Insets insets = border.getBorderInsets(this);
                w -= insets.left + insets.right;
                h -= insets.top + insets.bottom;
                gg.setClip(insets.left, insets.top, w, h);
            }
            super.paintChildren(gg);
        } finally {
            gg.dispose();
        }
    }
    
    //==================================================================================================================
    
    private class PanelDragged extends MouseAdapter {
        
        private final int RESIZE = 20;
        private final int MODIFIER_KEYS = MouseEvent.SHIFT_DOWN_MASK | MouseEvent.CTRL_DOWN_MASK | MouseEvent.ALT_DOWN_MASK | MouseEvent.META_DOWN_MASK;
        
        private SubPanel dragging = null;
        private Point pressed = null;
        
        @Override
        public void mousePressed(MouseEvent ev) {
            System.out.println(ev);
            pressed = ev.getPoint();
            if (isDrag(ev) && ev.getButton() == ev.BUTTON1) {
                dragging = panels
                .stream()
                .sorted(Comparator.comparingInt(p -> getComponentZOrder(p)))
                .filter(p -> p.getBounds().contains(pressed))
                .findFirst()
                .orElse(null);
                System.out.println(dragging);
            }
        }
        
        @Override
        public void mouseReleased(MouseEvent ev) {
            System.out.println(ev);
            if (isDrag(ev) && ev.getButton() == ev.BUTTON1) {
                dragging = null;
            }
        }
        
        @Override
        public void mouseDragged(MouseEvent ev) {
            System.out.println(ev);
            if (dragging != null && isDrag(ev)) {
                int dx = ev.getX() - pressed.x;
                int dy = ev.getY() - pressed.y;
                Rectangle bounds = dragging.getBounds();
                int px = pressed.x - bounds.x;
                int py = pressed.y - bounds.y;
                if (px >= bounds.width-RESIZE && py >= bounds.height-RESIZE) {
                    bounds.width += dx;
                    bounds.height += dy;
                } else if (px >= bounds.width-RESIZE) {
                    bounds.width += dx;
                } else if (py >= bounds.height-RESIZE) {
                    bounds.height += dy;
//                } else if (px <= RESIZE && py <= RESIZE) {
//                    bounds.x += dx;
//                    bounds.width -= dx;
//                    bounds.y += dy;
//                    bounds.height -= dy;
//                } else if (px <= RESIZE) {
//                    bounds.x += dx;
//                    bounds.width -= dx;
//                } else if (py <= RESIZE) {
//                    bounds.y += dy;
//                    bounds.height -= dy;
                } else {
                    bounds.x += dx;
                    bounds.y += dy;
                }
                if (bounds.width < 2*RESIZE) {
                    bounds.width = 2*RESIZE;
                }
                if (bounds.height < 2*RESIZE) {
                    bounds.height = 2*RESIZE;
                }
                dragging.setBounds(bounds);
                pressed = ev.getPoint();
                repaint();
            }
        }
        
        private boolean isDrag(MouseEvent ev) {
            return (ev.getModifiersEx() & MODIFIER_KEYS) == ev.CTRL_DOWN_MASK; 
        }
    }
}
