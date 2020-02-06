package cfh.test;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class InfinitePanel extends JPanel implements MouseListener, MouseMotionListener, MouseWheelListener {
    
    private final List<Piece> pieces = new ArrayList<>();
    
    private int x = 0;
    private int y = 0;
    private double scale = 1;
    

    private InfinitePanel() {
        addMouseListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(this);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D gg = (Graphics2D) g.create();
        try {
            gg.translate(x, y);
            paintGrid(gg);
            gg.scale(scale, scale);
            paintPieces(gg);
        } finally {
            gg.dispose();
        }
    }
    
    private void paintGrid(Graphics2D gg) {
        gg.setColor(Color.BLUE);
        int x1 = 5-x;
        int x2 = getWidth()-x-5;
        int xm = (x <= 0) ? 2-x : (x < getWidth()) ? 0 : getWidth()-x-3;
        int y1 = 5-y;
        int y2 = getHeight()-y-5;
        int ym = (y <= 0) ? 2-y : (y < getHeight()) ? 0 : getHeight()-y-3;
        gg.drawLine(x1, ym, x2, ym);
        gg.drawLine(xm, y1, xm, y2);
    }
    
    private void paintPieces(Graphics2D gg) {
        ListIterator<Piece> iter = pieces.listIterator(pieces.size());
        while (iter.hasPrevious()) {
            iter.previous().paint(gg);
        }
    }
    
    
    Piece selected = null;
    int pressedX = 0;
    int pressedY = 0;
    
    @Override
    public void mousePressed(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            int px = (int) ((e.getX() - x)/scale);
            int py = (int) ((e.getY() - y)/scale);
            selected = pieces.stream().filter(p -> p.contains(px, py)).findFirst().orElse(null);
            if (selected != null) {
                pressedX = (int) (e.getX() - selected.x*scale);
                pressedY = (int) (e.getY() - selected.y*scale);
                pieces.remove(selected);
                pieces.add(0, selected);
                repaint();
            }
        } else if (SwingUtilities.isRightMouseButton(e)) {
            pressedX = e.getX() - x;
            pressedY = e.getY() - y;
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            if (selected != null) {
                selected.x = (int) ((e.getX() - pressedX)/scale);
                selected.y = (int) ((e.getY() - pressedY)/scale);
                repaint();
            }
        } else if (SwingUtilities.isRightMouseButton(e)) {
            x = e.getX() - pressedX;
            y = e.getY() - pressedY;
            repaint();
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        scale *= (1 - e.getWheelRotation()/5.0);
        repaint();
    }

    //----------------------------------------------------------------------------------------------
    
    public static void main(String[] args) {
        InfinitePanel panel = new InfinitePanel();
        panel.pieces.add(new Piece(20, 20));
        panel.pieces.add(new Piece(42, 20));
        
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(frame.DISPOSE_ON_CLOSE);
        frame.add(panel);
        frame.setSize(800, 600);
        frame.validate();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
