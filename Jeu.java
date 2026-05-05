import java.util.concurrent.TimeUnit;

import boardifier.control.Controller;
import boardifier.model.Model;
import boardifier.view.View;
import boardifier.model.GameException;
import boardifier.model.Player;

class Jeu extends Controller {
    private Plateau plateau;
    private Phase phaseActuelle;

    public Jeu(Model model, View view) {
        super(model, view);
        phaseActuelle = Phase.PLACE;
    }

    public void setup(){
        if (model.getGameStage() != null) {
            plateau = (Plateau) model.getGameStage().getContainers().get(0);
        }
    }

    public void demarrerPartie(String stageName) {
        try {
            // On entoure l'appel qui pose problème d'un bloc try-catch
            startStage(stageName);
        } catch (GameException e) {
            System.err.println("Erreur critique lors du démarrage du stage Boardifier : " + e.getMessage());
            e.printStackTrace();
            // Optionnel : arrêter le programme proprement en cas d'échec d'initialisation
            System.exit(1);
        }
    }

    int u = 0;
    @Override
    public void stageLoop(){
        while (!model.isEndGame()) {
            if (estTermine()) {
                stopGame();
                break;
            }
            if (phaseActuelle == Phase.PLACE) {
                if (u==0) {
                    System.out.println("Bienvenue dans la phase de placement le but est de placer ses pion et de former des moulin avant la phase de deplacement qui arriveras des que tout les pions auront été placer");
                }
                update();
                try {
                    TimeUnit.MINUTES.sleep(1);
                } catch (InterruptedException e) {
                    // Restaurer le statut d'interruption (bonne pratique)
                    Thread.currentThread().interrupt();
                }
            } else {

            }
            u++;
            update();
        }
        afficherGagnant();
    }

    public boolean estTermine() {
        if (model.getPlayers().size() < 2) {
            return false;
        }
        Joueur j1 = Joueur.fromPlayer(model.getPlayers().get(0));
        Joueur j2 = Joueur.fromPlayer(model.getPlayers().get(1));
        return phaseActuelle != Phase.PLACE && (j1.compterPions() < 3 || j2.compterPions() < 3);
    }

    public void stopGame(){
        model.stopGame();
    }

    public void afficherGagnant() {
        Joueur j1 = Joueur.fromPlayer(model.getPlayers().get(0));
        Joueur j2 = Joueur.fromPlayer(model.getPlayers().get(1));
        if (j1.compterPions() < 3) {
            System.out.println("Joueur 2 gagnant");
        } else if (j2.compterPions() < 3) {
            System.out.println("Joueur 1 gagnant");
        }
    }
}