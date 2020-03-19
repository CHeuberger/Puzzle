package cfh.puzzle;

import java.io.Serializable;

public abstract class Size implements Serializable {

    private transient int width;
    private transient int height;
    
    protected Size() {
    }
    
    
    void width(int width) {
        this.width= width;
    }
    
    public int width() {
        return width;
    }
    
    void height(int height) {
        this.height = height;
    }
    
    public int height() {
        return height;
    }
    
    public abstract int getCount();
    public abstract int getSizeX();
    public abstract int getSizeY();

    public abstract int getOverlap();
    public abstract int getBaseVariation();

    public abstract int getBorderWidth();

    public abstract int getPegWidth();
    public abstract int getPegLength();
    public abstract int getPegRadius();

    public abstract int getPegPositionDelta();
    public abstract int getPegRadiusDelta();
    public abstract int getPegHeightDelta();

    public abstract int getEdgeColorChange();
}