package cfh.puzzle;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JColorChooser;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import cfh.FileChooser;

public class GamePanel extends JPanel implements GameListener {

    static final int DELTA_SNAP = 8;

    private final String CMD_HOME = "Home";
    private final String CMD_ARRANGE = "Arrange";
    private final String CMD_BACKGROUND = "Background";
    private final String CMD_SHOW = "Show";
    private final String CMD_DEBUG = "Debug";

    private Image image = null;
    private Image background = null;
    private final int sizeX;
    private final int sizeY;

    private JMenuItem showMenuItem;
    
    private final List<Piece> pieces = new ArrayList<>();
    

    public GamePanel(int sx, int sy) {
        if (sx < 1) throw new IllegalArgumentException("negative sx: " + sx);
        if (sy < 1) throw new IllegalArgumentException("negative sy: " + sy);
        sizeX = sx;
        sizeY = sy;
        
        setComponentPopupMenu(createPopup());

        DragListener dragListener = new DragListener();
        addMouseListener(dragListener);
        addMouseMotionListener(dragListener);
    }
    
    protected void setImage(Image img) {
        image = img;
        showMenuItem.setEnabled(image != null);
    }

    private JPopupMenu createPopup() {
        MenuListener menuListener = new MenuListener();
        
        JMenuItem home = new JMenuItem("Home");
        home.setActionCommand(CMD_HOME);
        home.addActionListener(menuListener);
        
        JMenuItem arrange = new JMenuItem("Arrange");
        arrange.setActionCommand(CMD_ARRANGE);
        arrange.addActionListener(menuListener);
        
        showMenuItem = new JMenuItem("Show");
        showMenuItem.setActionCommand(CMD_SHOW);
        showMenuItem.addActionListener(menuListener);
        showMenuItem.setEnabled(image != null);
        
        JMenuItem bg = new JMenuItem("Background");
        bg.setActionCommand(CMD_BACKGROUND);
        bg.addActionListener(menuListener);
        
        JMenuItem debug = new JMenuItem("Debug");
        debug.setActionCommand(CMD_DEBUG);
        debug.addActionListener(menuListener);
        
        JPopupMenu menu = new JPopupMenu();
        menu.add(home);
        menu.add(showMenuItem);
        menu.addSeparator();
        menu.add(bg);
        menu.addSeparator();
        menu.add(arrange);
        menu.addSeparator();
        menu.add(debug);
        
        return menu;
    }

    public Component add(Piece piece) {
        piece.addGameListener(this);
        pieces.add(piece);
        return super.add(piece);
    }

    @Override
    public void pieceSelected(Piece piece) {
        Container parent = piece.getParent();
        parent.setComponentZOrder(piece, 0);
        piece.repaint();
        for (Piece p  : piece.getGroup()) {
            parent.setComponentZOrder(p, 0);
            p.repaint();
        }
    }

    @Override
    public void pieceMoved(Piece piece, int x, int y) {
        Piece next = null;
        double min = DELTA_SNAP + 1;
        Point myOffset = piece.getTileOffset();
        Set<Piece> group = piece.getGroup();
        for (Piece p : group) {
            for (Piece neighbour : p.getNeighbours()) {
                if (!group.contains(neighbour) && piece.getDir() == neighbour.getDir()) {
                    double dst = myOffset.distance(neighbour.getTileOffset());
                    if (dst < min) {
                        min = dst;
                        next = neighbour;
                    }
                }
            }
        }

        if (next != null) {
            Point offset = next.getTileOffset();
            int dx = offset.x - myOffset.x;
            int dy = offset.y - myOffset.y;
            for (Piece p : group) {
                p.setLocation(p.getX()+dx, p.getY()+dy);
            }
            piece.connect(next);
        }
    }

    @Override
    public void pieceDisconnect(Piece piece) {
        final Set<Piece> connected = piece.getConnected();
        if (!connected.isEmpty()) {
            for (Piece p : connected) {
                piece.disconnect(p);
                // disconnect non-neighbours
            }
            piece.setLocation(piece.getX()+2*DELTA_SNAP, piece.getY()+2*DELTA_SNAP);
            pieceSelected(piece);
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        int w = getWidth();
        int h = getHeight();
        
        Graphics2D gg = (Graphics2D) g;
        
        if (background != null) {
            for (int x = 0; x < w; x += background.getWidth(this)) {
                for (int y = 0; y < h; y += background.getHeight(this)) {
                    gg.drawImage(background, x, y, this);
                }
            }
        } else {
            gg.setColor(getBackground());
            gg.fillRect(0, 0, getWidth(), getHeight());
        }

        gg.setColor(Color.LIGHT_GRAY);
        for (int x = 0; x < w; x += sizeX) {
            gg.drawLine(x, 0, x, h);
        }
        for (int y = 0; y < h; y += sizeY) {
            gg.drawLine(0, y, w, y);
        }

        gg.setColor(Color.BLUE);
        for (int x = 0; x < w; x += 5*sizeX) {
            gg.drawLine(x, 0, x, h);
        }
        for (int y = 0; y < h; y += 5*sizeY) {
            gg.drawLine(0, y, w, y);
        }

        gg.setColor(Color.BLACK);
        for (int x = 0; x < w; x += 10*sizeX) {
            gg.drawLine(x, 0, x, h);
        }
        for (int y = 0; y < h; y += 10*sizeY) {
            gg.drawLine(0, y, w, y);
        }

        gg.fillRect(0, 0, 2, h);
        gg.fillRect(0, 0, w, 2);
        gg.fillRect(w-2, 0, 2, h);
        gg.fillRect(0, h-2, w, 2);
    }
    
    private void doHome() {
        setLocation(0, 0);
    }
    
    private void doArrange() {
        int w = getParent().getWidth();
        int h = getParent().getHeight();
        int i = 0;
        int j = 0;
        int x0 = 15 - getX();
        int y0 = 15 - getY();
        for (Piece piece : pieces) {
            if (!piece.getConnected().isEmpty())
                continue;

            int x = x0 + i*(sizeX+20) + 5*(j%2);
            int y = y0 + j*(sizeY+20) + 5*(i%2);
            piece.setLocation(x, y);

            j += 1;
            if (y + sizeY + piece.getHeight() > h - getY()) {
                j = 0;
                i += 1;
                if (x + sizeX + piece.getWidth() > w - getX()) {
                    x0 += 10;
                    y0 += 10;
                    i = 0;
                }
            }
        }
    }
    
    protected void doShow() {
        if (image != null) {
            ImageIcon icon = new ImageIcon(image);
            JLabel msg = new JLabel(icon);
            msg.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent ev) {
                    Container container = (Container)ev.getSource();
                    while (container != null) {
                        container = container.getParent();
                        if (container instanceof Dialog) {
                            ((Dialog) container).dispose();
                            break;
                        }
                    }
                }
            });
            JOptionPane.showMessageDialog(getParent(), msg, "Preview", JOptionPane.PLAIN_MESSAGE);
        }
    }
    
    private void doBackground() {
        FileChooser chooser = new FileChooser();
        if (chooser.showOpenDialog(getParent()) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (file.isFile()) {
                try {
                    Image img = ImageIO.read(file);
                    background = img;
                    repaint();
                } catch (IOException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(getParent(), ex, "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                switch (file.getName()) {
                    case "":
                    case "empty":
                    case "none":
                        background = null;
                        setBackground(null);
                        repaint();
                        break;
                    case "c":
                    case "col":
                    case "color":
                    case "solid":
                        Color color = JColorChooser.showDialog(getParent(), "Background", getBackground());
                        if (color != null) {
                            background = null;
                            setBackground(color);
                            repaint();
                        }
                        break;
                    default:
                        Object[] message = { "not a file", file };
                        JOptionPane.showMessageDialog(getParent(), message, "Error", JOptionPane.ERROR_MESSAGE);
                        break;
                }
            }
        }
    }
    
    private void doDebug() {
        System.out.println();
        for (Piece piece : pieces) {
            if (piece.getConnected().isEmpty())
                continue;
            System.out.printf("%s:", piece.getName());
            for (Piece connected : piece.getConnected()) {
                System.out.printf(" %s", connected.getName());
            }
            System.out.println();
        }
    }
    
    private void doArrange2() {
        int w = getParent().getWidth();
        int h = getParent().getHeight();
        int x = - getX();
        int y = - getY();
        int delta = 0;
        synchronized (getTreeLock()) {
            for (int i = 0; i < getComponentCount(); i++) {
                Component comp = getComponent(i);
                if (comp instanceof Piece) {
                    Piece piece = (Piece) comp;
                    if (!piece.getConnected().isEmpty())
                        continue;
                    
                    int pw = (piece.getWidth() + sizeX) / 2;
                    int ph = (piece.getHeight() + sizeY) / 2; 
                    if (x + pw > w - getX()) {
                        x = - getX() + delta;
                        y += ph;
                        if (y + ph > h - getY()) {
                            delta += 10;
                            x += 10;
                            y = - getY() + delta;
                        }
                    }
                    piece.setLocation(x, y);
                    x += pw;
                } else {
                    continue;
                }
            }
        }
    }
    
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private class DragListener extends MouseAdapter {

        private int gameX = 0;
        private int gameY = 0;

        private int pressedX;
        private int pressedY;

        @Override
        public void mousePressed(MouseEvent ev) {
            if (!SwingUtilities.isLeftMouseButton(ev)) {
                return;
            }
            pressedX = ev.getX();
            pressedY = ev.getY();
        }

        @Override
        public void mouseDragged(MouseEvent ev) {
            if (!SwingUtilities.isLeftMouseButton(ev)) {
                return;
            }
            gameX += ev.getX() - pressedX;
            gameY += ev.getY() - pressedY;
            if (gameX < getParent().getWidth() - getWidth()) {
                gameX = getParent().getWidth() - getWidth();
            }
            if (gameX > 0) {
                gameX = 0;
            }
            if (gameY < getParent().getHeight() - getHeight()) {
                gameY = getParent().getHeight() - getHeight();
            }
            if (gameY > 0) {
                gameY = 0;
            }
            setLocation(gameX, gameY);
        }
    }
    
    ////////////////////////////////////////////////////////////////////////////////////////////////
    
    private class MenuListener implements ActionListener {
        
        @Override
        public void actionPerformed(ActionEvent ev) {
            String cmd = ev.getActionCommand();
            switch (cmd) {
                case CMD_HOME:
                    doHome();
                    break;
                case CMD_ARRANGE:
                    if ((ev.getModifiers() & ev.CTRL_MASK) != 0) {
                        doArrange();
                    } else {
                        doArrange2();
                    }
                    break;
                case CMD_BACKGROUND:
                    doBackground();
                    break;
                case CMD_SHOW:
                    doShow();
                    break;
                case CMD_DEBUG:
                    doDebug();
                    break;
                default:
                	throw new AssertionError("invalic command \"" + cmd + "\"");
            }
        }
    }
}
