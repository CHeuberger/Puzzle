package cfh.test;

import java.awt.Color;
import java.awt.Graphics2D;

public class Piece {
    
    int x;
    int y;
    
    Piece(int x, int y) {
        this.x = x;
        this.y = y;
    }

    boolean contains(int x, int y) {
        return this.x <= x && x <= this.x+20 && this.y <= y && y <= this.y+20;
    }
    
    void paint(Graphics2D gg) {
        gg.setColor(Color.GRAY);
        gg.fillRect(x, y, 20, 20);
        gg.setColor(Color.BLACK);
        gg.draw3DRect(x, y, 20, 20, true);
        gg.draw3DRect(x+1, y+1, 18, 18, true);
    }
}
