import boardifier.model.Player;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

class Joueur extends Player{
    private static final Map<Player, Joueur> registry = new HashMap<>();

    // private Player player;
    private Couleur couleur;
    private Pion[] pions;
    private int pionsRestants;

    public Joueur(String nom, Couleur couleur, int nombrePions, boardifier.model.GameStageModel stageModel) {
        this(Player.HUMAN, nom, couleur, nombrePions, stageModel);
    }

    public Joueur(int type,String nom, Couleur couleur, int nombrePions, boardifier.model.GameStageModel stageModel) {
        // this.player = Player.createHumanPlayer(nom);
        super(type, nom);
        this.couleur = couleur;
        this.pions = new Pion[nombrePions];
        this.pionsRestants = nombrePions;

        for (int i = 0; i < nombrePions; i++) {
            pions[i] = new Pion(couleur, i + 1, stageModel);
        }

        // registry.put(this.player, this);
    }

    // public Player getPlayer() {
    //     return this.player;
    // }

    public static Joueur fromPlayer(Player p) {
        return registry.get(p);
    }

    public String getNom() {
        return name;
    }

    public Couleur getCouleur() {
        return couleur;
    }

    public int getPionsRestants() {
        return pionsRestants;
    }
    public Pion[] getPions() {return pions;}

    public Pion getPionNonPlace() {
        for (Pion pion : pions) {
            if (pion != null && !pion.estPlace()) {
                pionsRestants--;
                return pion;
            }
        }
        return null;
    }

    public int compterPions() {
        int count = 0;
        for (Pion pion : pions) {
            if (pion != null && pion.estPlace()) {
                count++;
            }
        }
        return count;
    }

    public void retirerPion(Pion victime) {
        for (int i = 0; i < pions.length; i++) {
            if (pions[i] == victime) {
                pions[i] = null;
                return;
            }
        }
    }
}