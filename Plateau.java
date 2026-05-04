class Plateau {
    private Pion[][] grille;
    private int[] place;

    public Plateau() {
        this.grille = new Pion[8][3];
        this.place = new int[24];
    }

    public boolean estValide(Position pos) {
        return pos.getX() >= 0 && pos.getX() <= 7 && pos.getY() >= 0 && pos.getY() <= 2;
    }

    public boolean estVide(Position pos) {
        if (!estValide(pos)) return false;
        int index = pos.getX() + (pos.getY() * 8);
        return this.place[index] == 0;
    }

    public void placerPion(Pion pion, Position pos) {
        if (estValide(pos)) {
            this.grille[pos.getX()][pos.getY()] = pion;
            pion.placer(pos);
            int index = pos.getX() + (pos.getY() * 8);
            this.place[index] = 1;
        }
        else
    }

    public void retirerPion(Position pos) {
        if (estValide(pos)) {
            this.grille[pos.getX()][pos.getY()] = null;
            int index = pos.getX() + (pos.getY() * 8);
            this.place[index] = 0;
        }
    }

    public Pion getPion(Position pos) {
        if (!estValide(pos)) return null;
        return this.grille[pos.getX()][pos.getY()];
    }

    public void deplacerPion(Pion pion, Position oldPosition, Position newPosition) {
        this.retirerPion(oldPosition);
        this.placerPion(pion, newPosition);
    }
}
