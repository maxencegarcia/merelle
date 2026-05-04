import boardifier.model.Player;

class Joueur extends Player {
    // private String nom;
    private Couleur couleur;
    private Pion[] pions;
    private int pionsRestants;

    public Joueur(String nom, Couleur couleur, int nombrePions, boardifier.model.GameStageModel stageModel) {
        super(Player.HUMAN, nom);
        this.couleur = couleur;
        this.pions = new Pion[nombrePions];
        this.pionsRestants = nombrePions;

        for (int i = 0; i < nombrePions; i++) {
            pions[i] = new Pion(couleur, i + 1, stageModel);
        }
    }

    public String getNom() { return name; }
    public Couleur getCouleur() { return couleur; }
    public int getPionsRestants() { return pionsRestants; }

    public Pion getPionNonPlace() {
        for (Pion pion : pions) {
            if (!pion.estPlace()) {
                pionsRestants--;
                return pion;
            }
        }
        return null;
    }

    public int compterPions() {
        int count = 0;
        for (Pion pion : pions) {
            if (pion.estPlace()) count++;
        }
        return count;
    }
}
