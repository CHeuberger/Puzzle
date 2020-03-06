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

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;


public class MainPanel extends JPanel {
    
    private static final int RESIZE_BORDER = 16;
    private static final int MODIFIER_KEYS = MouseEvent.SHIFT_DOWN_MASK 
                                           | MouseEvent.CTRL_DOWN_MASK 
                                           | MouseEvent.ALT_DOWN_MASK 
                                           | MouseEvent.META_DOWN_MASK;

    private final List<SubPanel> panels = new ArrayList<>();

    public MainPanel() {
        setBackground(Color.BLACK);
//        XXX
//        setBorder(new CompoundBorder(
//            new BevelBorder(BevelBorder.RAISED, Color.GRAY.brighter(), Color.GRAY.darker()),
//            new BevelBorder(BevelBorder.LOWERED, Color.GRAY.brighter(), Color.GRAY.darker())
//      ));
//        setBorder(new javax.swing.border.LineBorder(Color.BLUE, 20));
        setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.GREEN),
            "test",
            TitledBorder.DEFAULT_JUSTIFICATION,
            TitledBorder.DEFAULT_POSITION,
            null,
            Color.GREEN
            ));
        setLayout(null);

        SubPanelDrag subDrag = new SubPanelDrag();
        addMouseListener(subDrag);
        addMouseMotionListener(subDrag);
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
    
    private boolean isDrag(MouseEvent ev) {
        return (ev.getModifiersEx() & MODIFIER_KEYS) == ev.CTRL_DOWN_MASK; 
    }
    
//    XXX
    public static void debug(String format, Object... args) {
        System.out.printf(format, args);
        System.out.flush();
    }
    
    //==================================================================================================================
    
    private class SubPanelDrag extends MouseAdapter {
        
        private SubPanel dragging = null;
        private Point pressed = null;
        
        @Override
        public void mousePressed(MouseEvent ev) {
            pressed = ev.getPoint();
            if (isDrag(ev) && ev.getButton() == ev.BUTTON1) {
                dragging = panels
                .stream()
                .sorted(Comparator.comparingInt(p -> getComponentZOrder(p)))
                .filter(p -> p.getBounds().contains(pressed))
                .findFirst()
                .orElse(null);
                if (dragging != null) {
                    setComponentZOrder(dragging, 0);
                    repaint();
                }
            }
        }
        
        @Override
        public void mouseReleased(MouseEvent ev) {
            dragging = null;
        }
        
        @Override
        public void mouseDragged(MouseEvent ev) {
            if (dragging != null && isDrag(ev)) {
                int dx = ev.getX() - pressed.x;
                int dy = ev.getY() - pressed.y;
                Rectangle bounds = dragging.getBounds();
                int px = pressed.x - bounds.x;
                int py = pressed.y - bounds.y;
                if (px >= bounds.width-RESIZE_BORDER && py >= bounds.height-RESIZE_BORDER) {
                    bounds.width += dx;
                    bounds.height += dy;
                } else if (px >= bounds.width-RESIZE_BORDER) {
                    bounds.width += dx;
                } else if (py >= bounds.height-RESIZE_BORDER) {
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
                if (bounds.width < RESIZE_BORDER) {
                    bounds.width = RESIZE_BORDER;
                }
                if (bounds.height < RESIZE_BORDER) {
                    bounds.height = RESIZE_BORDER;
                }
                dragging.setBounds(bounds);
                pressed = ev.getPoint();
                repaint();
            }
        }
    }
}
