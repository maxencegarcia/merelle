package boardifier.view;

import boardifier.control.Logger;
import boardifier.model.Coord2D;
import boardifier.model.GameElement;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;

import java.util.ArrayList;
import java.util.List;

public abstract class ElementLook {

    // ==========================================
    // ATTRIBUTS COMMUNS & JAVAFX
    // ==========================================
    protected GameElement element;
    protected int depth;
    protected ElementLook parent;
    protected int anchorType;
    private RootPane rootPane;

    private final Group group;
    private final List<Shape> shapes;

    public static final int ANCHOR_CENTER = 0;
    public static final int ANCHOR_TOPLEFT = 1;

    // ==========================================
    // ATTRIBUTS MODE TEXTE
    // ==========================================
    protected String[][] shape; // buffer de chaînes pour l'aspect visuel en mode texte
    protected int width;        // largeur du viewport (mode texte)
    protected int height;       // hauteur du viewport (mode texte)

    // ==========================================
    // CONSTRUCTEURS
    // ==========================================
    public ElementLook(GameElement element, int width, int height, int depth) {
        this.element = element;

        // Initialisation JavaFX
        this.group = new Group();
        this.group.setTranslateX(element.getX());
        this.group.setTranslateY(element.getY());
        this.shapes = new ArrayList<>();

        // Initialisation Commune
        this.depth = depth;
        this.parent = null;
        this.anchorType = ANCHOR_CENTER;
        this.rootPane = null;

        // Initialisation Mode Texte
        if (width < 0) width = 0;
        if (height < 0) height = 0;
        this.width = width;
        this.height = height;
        this.shape = new String[height][width];
        clearShape();
    }

    public ElementLook(GameElement element, int depth) {
        this(element, 0, 0, depth);
    }

    public ElementLook(GameElement element) {
        this(element, 0, 0, 0);
    }

    // ==========================================
    // GETTERS & SETTERS (COMMUNS)
    // ==========================================
    public GameElement getElement() {
        return element;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public ElementLook getParent() {
        return parent;
    }

    public void setParent(ElementLook parent) {
        this.parent = parent;
    }

    public boolean hasParent() {
        return parent != null;
    }

    public int getAnchorType() {
        return anchorType;
    }

    public void setAnchorType(int anchorType) {
        this.anchorType = anchorType;
    }

    public RootPane getRootPane() {
        return rootPane;
    }

    public void setRootPane(RootPane rootPane) {
        this.rootPane = rootPane;
    }

    // ==========================================
    // GESTION DES DIMENSIONS (HYBRIDE)
    // ==========================================
    public int getWidth() {
        // Si JavaFX contient des éléments, on utilise les bornes graphiques, sinon la valeur texte
        if (!group.getChildren().isEmpty()) {
            Bounds b = group.getBoundsInLocal();
            return (int) b.getWidth();
        }
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        if (!group.getChildren().isEmpty()) {
            Bounds b = group.getBoundsInLocal();
            return (int) b.getHeight();
        }
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setSize(int width, int height) {
        if (width < 0) width = 0;
        if (height < 0) height = 0;
        if ((this.width != width) || (this.height != height)) {
            this.width = width;
            this.height = height;
            shape = new String[height][width];
            clearShape();
        }
    }

    // ==========================================
    // LOGIQUE JAVAFX (GROUP & SHAPES)
    // ==========================================
    public Group getGroup() {
        return group;
    }

    public void clearGroup() {
        group.getChildren().clear();
    }

    public List<Shape> getShapes() {
        return shapes;
    }

    public void clearShapes() {
        shapes.clear();
    }

    public void addShape(Shape shape) {
        group.getChildren().add(shape);
        shapes.add(shape);
    }

    public void addNode(Node node) {
        Bounds b = node.getBoundsInParent();
        Rectangle r = new Rectangle(b.getMinX(), b.getMinY(), b.getWidth(), b.getHeight());
        r.setFill(Color.TRANSPARENT);
        group.getChildren().add(node);
        group.getChildren().add(r);
        shapes.add(r);
    }

    public boolean isPointWithin(Coord2D point) {
        for (Node node : group.getChildren()) {
            Bounds b = node.localToScene(node.getBoundsInParent());
            if ((point.getX() >= b.getMinX()) && (point.getX() <= b.getMaxX()) &&
                    (point.getY() >= b.getMinY()) && (point.getY() <= b.getMaxY())) {
                return true;
            }
        }
        return false;
    }

    // ==========================================
    // LOGIQUE MODE TEXTE (SHAPE BUFFER)
    // ==========================================
    protected void clearShape() {
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                shape[i][j] = " ";
            }
        }
    }

    protected void printShape() {
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                System.out.print(shape[i][j]);
            }
            System.out.println();
        }
    }

    public String getShapePoint(int x, int y) {
        if (shape == null) return null;
        if ((x >= 0) && (x < width) && (y >= 0) && (y < height)) return shape[y][x];
        return null;
    }

    // ==========================================
    // GESTION DES ÉVÉNEMENTS & CALLBACKS
    // ==========================================
    public void onLocationChange() {
        Logger.trace("look location of [" + this + "] changed to " + element.getX() + "," + element.getY());
        group.setTranslateX(element.getX());
        group.setTranslateY(element.getY());
    }

    public void onVisibilityChange() {
        boolean visible = element.isVisible();

        // Logique JavaFX
        for (Node node : group.getChildren()) {
            node.setVisible(visible);
        }

        // Logique Texte
        if (!visible) {
            clearShape();
        } else {
            onFaceChange();
        }
    }

    public void onSelectionChange() {}

    public void onFaceChange() {
        render();
    }

    protected abstract void render();

    public void moveTo(double x, double y) {
        // Met à jour la position de l'élément sans déclencher d'événement immédiat
        element.setLocation(x, y, false);

        // Met à jour le rendu visuel (JavaFX & Logs)
        Logger.trace("look location of [" + this + "] changed to " + x + "," + y);
        group.setTranslateX(x);
        group.setTranslateY(y);
    }
}