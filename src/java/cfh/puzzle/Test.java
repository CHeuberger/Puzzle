package cfh.puzzle;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.QuadCurve2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import cfh.FileChooser;

public class Test extends GamePanel {

    private static final int MAXX = 2900;
    private static final int MAXY = 1800;

    private static final Color[] COLORS = new Color[] {
        Color.RED,
        Color.GREEN,
        Color.BLUE,
        Color.YELLOW,
        Color.MAGENTA,
        Color.GRAY,
        Color.CYAN,
        Color.ORANGE};

    public static void main(String[] args) {
        int type = 10;
        if (args.length > 0) {
            try {
                type = Integer.parseInt(args[0]);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, ex);
            }
        }

        String useImage;
        BufferedImage image = null;
        if (args.length > 1 && !args[1].equals("-")) {
            useImage = args[1].equalsIgnoreCase("none") ? null : args[1];
            URL url = ClassLoader.getSystemClassLoader().getResource(useImage);
            if (url == null) {
                try {
                    image = ImageIO.read(new File(useImage));
                } catch (IOException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, new Object[] {ex, args[1], useImage});
                }
            } else {
                try {
                	image = ImageIO.read(url);
                } catch (IOException ex) {
                	ex.printStackTrace();
                	JOptionPane.showMessageDialog(null, new Object[] {ex, args[1], url});
                }
            }
        } else {
            FileChooser chooser = new FileChooser();
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            chooser.setMultiSelectionEnabled(false);
            if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                useImage = file.getAbsolutePath();
                try {
                	image = ImageIO.read(file);
                } catch (IOException ex) {
                	ex.printStackTrace();
                	JOptionPane.showMessageDialog(null, new Object[] {ex, args[1], file});
                }
            } else {
                return;
            }
        }
        
        long seed;
        if (args.length > 2) {
        	if (args[2].equals("random")) {
        		seed = randomSeed();
        	} else {
        		try {
        			seed = Long.parseLong(args[2]);
        		} catch (NumberFormatException ex) {
        			ex.printStackTrace();
                	JOptionPane.showMessageDialog(null, new Object[] {ex, args[2]});
        			return;
        		}
        	}
        } else {
            seed = randomSeed();
        }
        System.out.printf("Seed: %d%n", seed);
        System.out.printf("Image: %s%n", useImage);
        
        Size size;
        if (args.length > 3) {
        	int i = args[3].toLowerCase().indexOf('x');
        	if (i == -1) {
            	JOptionPane.showMessageDialog(null, new Object[] {"Wrong format, expected <count>x<template>", args[3]});
    			return;
        	} 
        	int count;
        	String templName;
        	try {
        		count = Integer.parseInt(args[3].substring(0, i));
        	} catch (NumberFormatException ex) {
        		ex.printStackTrace();
        		JOptionPane.showMessageDialog(null, new Object[] {ex, args[3]});
        		return;
        	}
        	templName = args[3].substring(i+1);
        	Template templ = Template.get(templName);
        	size = new TemplateSizeImpl(count, templ);
        } else {
        	size = null;
        }
        
        if (size == null) {
        	SizePanel sizePanel = new SizePanel();
			size = sizePanel.showAndGetSize();
        }
        
        if (size != null) {
            new Test(type, image, size, seed);
        }
    }

	private static long randomSeed() {
		long seed = 8006678197202707420L ^ System.nanoTime();
		seed %= 100000;
		return seed;
	}

    private final Size puzzleSize;
    private final int type;

    private final Random random;

    private JFrame frame;
    private List<Piece> pieces;

    private Test(int t, BufferedImage image, Size size, long seed) {
        super(size.getSizeX(), size.getSizeY());

        puzzleSize = size;
        type = t;

        random = new Random(seed);

        setLayout(null);
        setOpaque(false);
        setSize(MAXX, MAXY);

        pieces = createPieces(image);
        for (Piece piece : pieces) {
            add(piece);
        }


        frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(null);
        frame.add(this);
        frame.setSize(1024, 800);
        frame.validate();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        
        SwingUtilities.invokeLater(new Runnable() {
            
            @Override
            public void run() {
                doShow();
            }
        });
    }

    private List<Piece> createPieces(BufferedImage image) {
        List<Piece> result = new ArrayList<Piece>();
        BufferedImage mask1 = loadImage("resources/mask1.png");
        BufferedImage mask2 = loadImage("resources/mask75.png");

        final int COUNT = puzzleSize.getCount();
        final int SX = puzzleSize.getSizeX();
        final int SY = puzzleSize.getSizeY();
        final int BORDER = puzzleSize.getBorderWidth();
        final int OVER = puzzleSize.getOverlap();

        final int X;
        final int Y;
        if (image == null) {
            Y = (int) Math.sqrt(COUNT);
            X = Math.round((float)COUNT / Y);
        } else {
            int width = image.getWidth() + BORDER + BORDER;
            int height = image.getHeight() + BORDER + BORDER;
            double area = (double)(width*height)/(COUNT);
            double min = Math.sqrt(area);
            X = (int) Math.round(width/min);
            Y = (int) Math.round(height/min);
//            int sx = width / PREFX;
//            int sy = height / PREFY;
//            int min = Math.max(sx, sy);
//            X = (width + min/2) / min;
//            Y = (height + min/2) / min;
            System.out.printf("Size: %d x %d (%d)%n", X, Y, X*Y);
            AffineTransform scale = AffineTransform.getScaleInstance(
                (double) (X*SX-BORDER-BORDER) / image.getWidth(),
                (double) (Y*SY-BORDER-BORDER) / image.getHeight());
            AffineTransformOp op = new AffineTransformOp(scale, AffineTransformOp.TYPE_BICUBIC);
            image = op.filter(image, null);
            if (BORDER > 0) {
                width = BORDER + image.getWidth() + BORDER;
                height = BORDER + image.getHeight() + BORDER;
                BufferedImage border = new BufferedImage(width, height, image.getType());
                Graphics2D gg = border.createGraphics();
                try {
                    gg.setComposite(AlphaComposite.Src);
                    Color base = new Color(200, 150, 100);
                    gg.setColor(base);
                    gg.fill3DRect(0, 0, width, height, true);
                    if (BORDER >= 3) {
                        gg.fill3DRect(1, 1, width-2, height-2, true);
                        if (BORDER >= 6) {
                            gg.fill3DRect(2, 2, width-4, height-4, true);
                        }
                    }
                    gg.drawImage(image, BORDER, BORDER, null);
                    image = border;
                } finally {
                    gg.dispose();
                }
            }
        }
        setImage(image);

        switch (type) {
            case 0: {  // 5 ovals
                for (int i = 0; i < 5; i++) {
                    Piece piece = new ShapePiece(0, 0,
                        new Ellipse2D.Float(30f*i, 0f, 29f, 59f), 
                        image);
                    result.add(piece);
                }
            } break;

            case 1: {  // two pieces
                Path2D.Float div = new Path2D.Float();
                div.moveTo(100f, 000f);
                div.lineTo(100f, 040f);
                div.lineTo(110f, 040f);

                div.quadTo(120f, 040f, 120f, 030f);
                div.quadTo(140f, 050f, 120f, 070f);
                div.quadTo(120f, 060f, 110f, 060f);

                div.lineTo(100f, 060f);
                div.lineTo(100f, 100f);

                Path2D.Float path = new Path2D.Float();
                path.moveTo(000f, 000f);
                path.append(div, true);
                path.lineTo(000f, 100f);
                path.closePath();

                Piece piece1 = new ShapePiece(0, 0, path, image);
                piece1.setLocation(100, 100);
                result.add(piece1);

                path = new Path2D.Float(div);
                path.lineTo(200f, 100f);
                path.lineTo(200f, 000f);
                path.closePath();

                Piece piece2 = new ShapePiece(0, 0, path, image);
                piece2.setLocation(300, 100);
                result.add(piece2);

                piece1.addNeighbour(piece2);
            } break;

            case 2: {  // shapes
                AffineTransform translate = AffineTransform.getTranslateInstance(100f, 100f);
                Path2D.Float path = new Path2D.Float();
                path.append(new Arc2D.Float(000f, 000f, 
                    100f, 100f, 
                    0f, 135f, Arc2D.CHORD), false);
                path.closePath();
                path.transform(translate);
                result.add(new ShapePiece(0, 0, path, image));

                path = new Path2D.Float();
                path.append(new Arc2D.Float(000f, 000f, 
                    100f, 100f, 
                    0f, 135f, Arc2D.OPEN), false);
                path.closePath();
                translate.translate(100d, 000d);
                path.transform(translate);
                result.add(new ShapePiece(0, 0, path, image));

                path = new Path2D.Float();
                path.append(new Arc2D.Float(000f, 000f, 
                    100f, 100f, 
                    0f, 135f, Arc2D.PIE), false);
                path.closePath();
                translate.translate(100d, 000d);
                path.transform(translate);
                result.add(new ShapePiece(0, 0, path, image));

                path = new Path2D.Float(); 
                path.append(new CubicCurve2D.Float(000f, 050f, 
                    000f, 000f, 
                    100f, 000f, 
                    100f, 050f), false);
                path.closePath();
                translate.translate(100d, 000d);
                path.transform(translate);
                result.add(new ShapePiece(0, 0, path, image));

                path = new Path2D.Float();
                path.append(new QuadCurve2D.Float(000f, 000f,
                    000f, 100f,
                    100f, 000f), false);
                path.closePath();
                translate.translate(100d, 000d);
                path.transform(translate);
                result.add(new ShapePiece(0, 0, path, image));
            } break;

            case 3: {  // one area
                Area area = new Area(new Rectangle(100, 100));
                area.add(new Area(new Ellipse2D.Float(110f, 035f, 030f, 030f)));
                area.subtract(new Area(new Rectangle(90, 40, 30, 20)));
                result.add(new ShapePiece(0, 0, area, image));
            } break;

            case 4: {  // 4 mask 1
                Piece[] quad = new Piece[4];
                for (int i = 0; i/4 < 4; i++) {
                    int x =  50 + (i%2)*50 + (i%4/2)*50 + (i/8)*250;
                    int y = 100 - (i%2)*50 + (i%8/2)*50 + (i%8/4)*150;
                    int ix = 850 + (i%2)*50 + (i%4/2)*50;
                    int iy = 600 - (i%2)*50 + (i%4/2)*50;
                    MaskPiece piece = new MaskPiece(x, y, Direction.NORTH, mask1, image, ix, iy, i / 4);
                    piece.setLocation(x, y);
                    result.add(piece);
                    quad[i % 4] = piece;
                    if (i % 4 == 3) {
                        quad[0].addNeighbour(quad[1]);
                        quad[0].addNeighbour(quad[2]);
                        quad[1].addNeighbour(quad[3]);
                        quad[2].addNeighbour(quad[3]);
                    }
                }
            } break;

            case 5: {  // 4 mask 2
                Piece quad[] = new Piece[4];
                for (int i = 0; i/4 < 4; i++) {
                    int x = 50 + (i%2)*100 + (i/8)*250;
                    int y = 50 + (i%8/2)*100 + (i%8/4)*50;
                    int ix = 450 + (i%2)*100;
                    int iy = 300 + (i%4/2)*100;
                    MaskPiece piece = new MaskPiece(x, y, Direction.NORTH, mask2, image, ix, iy, i / 4);
                    piece.setLocation(x, y);
                    result.add(piece);
                    quad[i % 4] = piece;
                    if (i % 4 == 3) {
                        quad[0].addNeighbour(quad[1]);
                        quad[0].addNeighbour(quad[2]);
                        quad[1].addNeighbour(quad[3]);
                        quad[2].addNeighbour(quad[3]);
                    }
                }
            } break;

            case 60:
            case 61:
            case 62:
            case 63:
            {  // rectangular/overlapped - correct orientation
                final int rule = type - 60;
                if (image == null) 
                    break;
                Piece[][] quad = new Piece[X][Y];
                boolean[][] used = new boolean[X][Y];
                int free = X * Y;
                for (int i = 0; i < X; i++) {
                    for (int j = 0; j < Y; j++) {
                        int x = SX * i;
                        int y = SY * j;
                        MaskPiece piece = new MaskPiece(x, y, Direction.NORTH, mask2, image, x, y, rule);
                        result.add(piece);
                        quad[i][j] = piece;
                        if (i > 0)
                            piece.addNeighbour(quad[i-1][j]);
                        if (j > 0)
                            piece.addNeighbour(quad[i][j-1]);

                        int r = random.nextInt(free);
                        for (int i1 = 0; i1 < X && r >= 0; i1++) {
                            for (int j1 = 0; j1 < Y && r >= 0; j1++) {
                                if (! used[i1][j1]) {
                                    if (r == 0) {
                                        used[i1][j1] = true;
                                        free--;
                                        piece.setLocation(50 + i1*(SX+5), 50 + j1*(SY+5));
                                    }
                                    r--;
                                }
                            }
                        }
                        //                        piece.setLocation(50+x, 50+y);  //DEBUG
                    }
                }
            } break;
            
            case 70:
            case 71:
            case 72:
            case 73: 
            {    // rectangular overlapped rotated
                final int rule = type - 70;
                Piece[][] quad = new Piece[X][Y];
                boolean[][] used = new boolean[X][Y];
                int free = X * Y;
                for (int i = 0; i < X; i++) {
                    for (int j = 0; j < Y; j++) {
                        int x = SX * i;
                        int y = SY * j;
                        Direction dir;
                        switch (random.nextInt(5)) {
                            case 1: dir = Direction.EAST; break;
                            case 2: dir = Direction.SOUTH; break;
                            case 3: dir = Direction.WEST; break;
                            default: dir = Direction.NORTH; break;
                        }
                        MaskPiece piece = new MaskPiece(x, y, dir, mask2, image, x, y, rule);
                        result.add(piece);
                        quad[i][j] = piece;
                        if (i > 0)
                            piece.addNeighbour(quad[i-1][j]);
                        if (j > 0)
                            piece.addNeighbour(quad[i][j-1]);

                        int r = random.nextInt(free);
                        for (int i1 = 0; i1 < X && r >= 0; i1++) {
                            for (int j1 = 0; j1 < Y && r >= 0; j1++) {
                                if (! used[i1][j1]) {
                                    if (r == 0) {
                                        used[i1][j1] = true;
                                        free--;
                                        piece.setLocation(50 + i1*(SX+5), 50 + j1*(SY+5));
                                    }
                                    r--;
                                }
                            }
                        }
                    }
                }
            } break;

            case 10:
            case 11:
            {  // maskS
                Piece[][] quad = new Piece[X][Y];
                boolean[][] used = new boolean[X][Y];
                int free = X * Y;
                Piece[] list = new Piece[free];

                Polygon[][] shapes = createShapes(X, Y);
                BufferedImage[][] masks = createMasks(X, Y, shapes);

                Rectangle base = new Rectangle(OVER, OVER, SX, SY);
                for (int i = 0; i < X; i++) {
                    for (int j = 0; j < Y; j++) {
                        int x = SX * i - OVER;
                        int y = SY * j - OVER;
                        Direction dir;
                        switch (random.nextInt(4)) {
                            case 1: dir = Direction.EAST; break;
                            case 2: dir = Direction.SOUTH; break;
                            case 3: dir = Direction.WEST; break;
                            default: dir = Direction.NORTH; break;
                        }                        
                        MaskPiece piece = type == 10 ?
                        		new MaskPiece(x, y, dir, masks[i][j], image, x, y, 2) :
                        		new MaskPieceDebug(x, y, dir, masks[i][j], image, x, y, 2, base, shapes[i][j]);
                        piece.setName(String.format("%dx%d", j, i));
                        quad[i][j] = piece;
                        if (i > 0)
                            piece.addNeighbour(quad[i-1][j]);
                        if (j > 0)
                            piece.addNeighbour(quad[i][j-1]);

                        piece.setLocation(SX-OVER + i*(SX+15), SY-OVER + j*(SY+15));

                        int r = random.nextInt(free);
                        for (int i1 = 0; i1 < X && r >= 0; i1++) {
                            for (int j1 = 0; j1 < Y && r >= 0; j1++) {
                                if (! used[i1][j1]) {
                                    if (r == 0) {
                                        used[i1][j1] = true;
                                        list[j1 + i1*Y] = piece;
                                        free -= 1;
                                        int x1 = SX-OVER + i1*(SX+18) + 5*(j1%2);
                                        int y1 = SY-OVER + j1*(SY+18) + 5*(i1%2);
                                        piece.setLocation(x1, y1);
                                    }
                                    r -= 1;
                                }
                            }
                        }
                    }
                }
                result.addAll(Arrays.asList(list));
            } break;

            default:
                System.err.println("unknown type " + type);
                System.exit(-1);
        }

        for (int i = 0; i < result.size(); i++) {
            Piece piece = result.get(i);
            piece.setForeground(Color.BLACK);
            piece.setBackground(COLORS[i % COLORS.length]);
        }
        return result;
    }

    private Polygon[][] createShapes(int X, int Y) {
        final int BASE = puzzleSize.getBaseVariation();
        final int SX = puzzleSize.getSizeX();
        final int SY = puzzleSize.getSizeY();
        
        Polygon[][] shapes = new Polygon[X][Y];
        int[][] dx = new int[X+1][Y+1];
        int[][] dy = new int[X+1][Y+1];
        for (int j = 1; j < Y; j++) {
            dy[0][j] = rnd(0, BASE);
        }
        for (int i = 0; i < X; i++) {
            if (i+1 < X) {
                dx[i+1][0] = rnd(0, BASE);
            }
            for (int j = 0; j < Y; j++) {
                if (i+1 < X) {
                    dx[i+1][j+1] = rnd(0, BASE);
                }
                if (j+1 < Y) {
                    dy[i+1][j+1] = rnd(0, BASE);
                }
                Polygon shape = new Polygon();
                shape.addPoint(dx[i][j], dy[i][j]);
                shape.addPoint(SX+dx[i+1][j], dy[i+1][j]);
                shape.addPoint(SX+dx[i+1][j+1], SY+dy[i+1][j+1]);
                shape.addPoint(dx[i][j+1], SY+dy[i][j+1]);
                shape.addPoint(dx[i][j], dy[i][j]);
                shapes[i][j] = shape;
            }
        }
        return shapes;
    }
    
    private BufferedImage[][] createMasks(int X, int Y, Polygon[][] shapes) {
        final int BASE = puzzleSize.getBaseVariation();
        final int OVER = puzzleSize.getOverlap();
        final int SX = puzzleSize.getSizeX();
        final int SY = puzzleSize.getSizeY();
        final int PEGWIDTH = puzzleSize.getPegWidth();
        final int PEGLENGTH = puzzleSize.getPegLength();
        final int PEGRADIUS = puzzleSize.getPegRadius();
        final int PEGPOSDELTA = puzzleSize.getPegPositionDelta();
        final int PEGRADIUSDELTA = puzzleSize.getPegRadiusDelta();
        final int PEGHEIGHTDELTA = puzzleSize.getPegHeightDelta();

        BufferedImage[][] masks = new BufferedImage[X][Y];
        Color fillColor = new Color(128, 128, 128, 255);
        Color transpColor = new Color(128, 128, 128, 0);

        for (int i = 0; i < X; i++) {
            for (int j = 0; j < Y; j++) {
                BufferedImage m = new BufferedImage(OVER+SX+OVER, OVER+SY+OVER, BufferedImage.TYPE_INT_ARGB);
                Graphics2D gg = m.createGraphics();
                try {
                    gg.setComposite(AlphaComposite.Src);
                    gg.translate(OVER, OVER);
                    gg.setColor(fillColor);
                    gg.fill(shapes[i][j]);
                } finally {
                    gg.dispose();
                }
                masks[i][j] = m;
            }
        }
// if (true) return masks;        

        BufferedImage tmp;

        tmp = new BufferedImage(SX, BASE+OVER, BufferedImage.TYPE_INT_ARGB);
        for (int i = 0; i < X; i++) {
            for (int j = 1; j < Y; j++) {
                Graphics2D gg = tmp.createGraphics();
                try {
                    gg.setComposite(AlphaComposite.Src);
                    gg.setColor(transpColor);
                    gg.fillRect(0, 0, tmp.getWidth(), tmp.getHeight());
                    gg.setColor(fillColor);
                    int extra = random.nextInt(100);
                    if (extra < 3) {
                        int x = rnd(SX/2, 2*PEGPOSDELTA);
                        int w = rnd(PEGRADIUS+PEGRADIUS, PEGRADIUSDELTA);
                        int h = rnd(PEGRADIUS+PEGRADIUS, PEGHEIGHTDELTA);
                        gg.fillOval(x-w, BASE-h, w+w, h+h);
                    } else if (extra < 999) {
                        int x = rnd(SX/2, PEGPOSDELTA);
                        gg.fillRect(x-PEGWIDTH, 0, PEGWIDTH+PEGWIDTH, BASE+10);
                        int w = rnd(PEGRADIUS+2, PEGRADIUSDELTA);
                        int h = rnd(PEGRADIUS+PEGRADIUS, PEGHEIGHTDELTA);
                        gg.fillOval(x-w, BASE+rnd(PEGLENGTH+1,1), w+w, h);
                    }
                } finally {
                    gg.dispose();
                }
                Graphics2D g0 = masks[i][j-1].createGraphics();
                Graphics2D g1 = masks[i][j].createGraphics();
                try {
                    if (random.nextInt(100) < (20+60*((i+j)%2))) {
                        g0.setComposite(AlphaComposite.SrcOver);
                        g1.setComposite(AlphaComposite.DstOut);
                        g0.drawImage(tmp, OVER, OVER+SY-BASE, this);
                        g1.drawImage(tmp, OVER, OVER-BASE, this);
                    } else {
                        g0.setComposite(AlphaComposite.DstOut);
                        g0.scale(1.0, -1.0);
                        g1.setComposite(AlphaComposite.SrcOver);
                        g1.scale(1.0, -1.0);
                        g0.drawImage(tmp, OVER, -OVER-SY-BASE, this);
                        g1.drawImage(tmp, OVER, -OVER-BASE, this);
                    }
                } finally {
                    g1.dispose();
                    g0.dispose();
                }
            }
        }
        tmp = new BufferedImage(BASE+OVER, SY, BufferedImage.TYPE_INT_ARGB);
        for (int i = 1; i < X; i++) {
            for (int j = 0; j < Y; j++) {
                Graphics2D gg = tmp.createGraphics();
                try {
                    gg.setComposite(AlphaComposite.Src);
                    gg.setColor(transpColor);
                    gg.fillRect(0, 0, tmp.getWidth(), tmp.getHeight());
                    gg.setColor(fillColor);
                    int extra = random.nextInt(100);
                    if (extra < 3) {
                        int y = rnd(SY/2, 2*PEGPOSDELTA);
                        int w = rnd(PEGRADIUS+PEGRADIUS, PEGHEIGHTDELTA);
                        int h = rnd(PEGRADIUS+PEGRADIUS, PEGRADIUSDELTA);
                        gg.fillOval(BASE-w, y-h, w+w, h+h);
                    } else if (extra < 999) {
                        int y = rnd(SY/2, PEGPOSDELTA);
                        gg.fillRect(0, y-PEGWIDTH, BASE+10, PEGWIDTH+PEGWIDTH);
                        int w = rnd(PEGRADIUS+PEGRADIUS, PEGHEIGHTDELTA);
                        int h = rnd(PEGRADIUS+2, PEGRADIUSDELTA);
                        gg.fillOval(BASE+rnd(PEGLENGTH+1,1), y-h, w, h+h);
                    }
                } finally {
                    gg.dispose();
                }
                Graphics2D g0 = masks[i-1][j].createGraphics();
                Graphics2D g1 = masks[i][j].createGraphics();
                try {
                    if (random.nextInt(100) > (20+60*((i+j)%2))) {
                        g0.setComposite(AlphaComposite.SrcOver);
                        g1.setComposite(AlphaComposite.DstOut);
                        g0.drawImage(tmp, OVER+SX-BASE, OVER, this);
                        g1.drawImage(tmp, OVER-BASE, OVER, this);
                    } else {
                        g0.setComposite(AlphaComposite.DstOut);
                        g0.scale(-1.0, 1.0);
                        g1.setComposite(AlphaComposite.SrcOver);
                        g1.scale(-1.0, 1.0);
                        g0.drawImage(tmp, -OVER-SX-BASE, OVER, this);
                        g1.drawImage(tmp, -OVER-BASE, OVER, this);
                    }
                } finally {
                    g1.dispose();
                    g0.dispose();
                }
            }
        }

        final int EDGECOLOR = puzzleSize.getEdgeColorChange();
        long time = System.nanoTime();
        for (int i = 0; i < X; i++) {
            for (int j = 0; j < Y; j++) {
                BufferedImage mask = masks[i][j];
                for (int x = 1; x < mask.getWidth()-1; x++) {
                    for (int y = 1; y < mask.getHeight()-1; y++) {
                        int rgb = mask.getRGB(x, y);
                        if (rgb != 0) {
                            if (mask.getRGB(x-1, y) == 0 || mask.getRGB(x, y-1) == 0) {
                                mask.setRGB(x, y, rgb - EDGECOLOR);
                            } else if (mask.getRGB(x+1, y) == 0 || mask.getRGB(x, y+1) == 0) {
                                mask.setRGB(x, y, rgb + EDGECOLOR);
                            }
                        }
                    }
                }
            }
        }
        time -= System.nanoTime();
        System.out.printf("time: %.3f ms%n", time / -1e6);

        return masks;
    }

    private int rnd(int mean, int delta) {
        int value;
        if (delta < 0) {
            value = mean + random.nextInt(-delta-delta+1) - -delta;
        } else if (delta % 2 == 0) {
            value = mean + 2*(random.nextInt(delta/2+delta/2+1) - delta/2);
        } else {
            value = mean + random.nextInt(delta+delta+1) - delta;
        }
        return value;
    }

    private BufferedImage loadImage(String filename) {
        URL url = getClass().getResource(filename);
        if (url != null) {
            try {
                return ImageIO.read(url);
            } catch (IOException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, ex);
            }
        } else {
            JOptionPane.showMessageDialog(null, "could not load " + filename);
        }
        return null;
    }
}
