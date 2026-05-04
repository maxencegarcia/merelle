import boardifier.control.Controller;
import boardifier.model.Model;
import boardifier.view.View;
import boardifier.model.Player;

class Jeu extends Controller {
    private Plateau plateau;
    // private Joueur joueur1;
    // private Joueur joueur2;
    // private Joueur joueurActuel;
    private Phase phaseActuelle;
    public Jeu(Model model, View view) {
        // plateau = new Plateau();
        //  = new Joueur(j1, Couleur.BLANC, 9);
        // joueur2 = new Joueur(j2, Couleur.NOIR, 9);
        // joueurActuel = joueur1;
        // phaseActuelle = Phase.PLACE;
        super(model, view);
        phaseActuelle = Phase.PLACE;
    }
    public void setup(){
        if (model.getGameStage() != null) {
            plateau = new Plateau(model.getGameStage());
            model.getGameStage().addContainer(plateau);
        }
    }

    @Override
    public void stageLoop(){
        while (!model.isEndGame()) {
            if (estTermine()) {
                stopGame();
                break;
            }
            if (phaseActuelle == Phase.PLACE) {
                
            }else {

            }

            update();
        }
        afficherGagnant();
    }

    // public void jouer() {
    //     while (!estTermine()) {

    //         if (phaseActuelle == Phase.PLACE) {

    //         } else {

    //         }
    //     }
    //     afficherGagnant();
    // }

    public boolean estTermine() {
        if (model.getPlayers().size() < 2) {
            return false;
        }
        Joueur j1 = (Joueur) model.getPlayers().get(0);
        Joueur j2 = (Joueur) model.getPlayers().get(1);
        return phaseActuelle != Phase.PLACE && (j1.compterPions() < 3 || j2.compterPions() < 3);
    }

    public void stopGame(){
        model.stopGame();
    }

    public void afficherGagnant() {
        Joueur j1 = (Joueur) model.getPlayers().get(0);
        Joueur j2 = (Joueur) model.getPlayers().get(1);
        if (j1.compterPions() < 3) {
            System.out.println("Joueur 2 gagnant");
        } else if (j2.compterPions() < 3) {
            System.out.println("Joueur 1 gagnant");
        }
    }
}
