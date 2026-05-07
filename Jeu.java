import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import boardifier.control.Controller;
import boardifier.model.Model;
import boardifier.view.View;
import boardifier.model.GameException;
import boardifier.model.Player;

class Jeu extends Controller {
    private Plateau plateau;
    private Phase phaseActuelle;
    private Phase phasePrecedente;
    static final Scanner input = new Scanner(System.in);


    public Jeu(Model model, View view) {
        super(model, view);
        phaseActuelle = Phase.PLACE;
        this.mapElementLook = new java.util.HashMap<>();
    }

    public void setup(){
        if (model.getGameStage() != null) {
            plateau = new Plateau(model.getGameStage());
            model.getGameStage().addContainer(plateau);
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
                    System.out.println("Welcome to the placement phase, the goal is to place your pieces and form mills before the moving phase, which will happen as soon as all the pieces have been placed.");
                }
                playpose();
                // update();
                // try {
                //     TimeUnit.MINUTES.sleep(1);
                // } catch (InterruptedException e) {
                //     // Restaurer le statut d'interruption (bonne pratique)
                //     Thread.currentThread().interrupt();
                // }
            } 
            if (phaseActuelle == Phase.MOVE) {
                playmove();
            }
            if (phaseActuelle == Phase.STEAL) {
                playsteal();
                phaseActuelle = phasePrecedente;
            }
            else {

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
    public void playpose(){
        Joueur joueur = (Joueur) model.getCurrentPlayer();
        System.out.println("its time for " + joueur.getNom() + " to play");
        Position pos = askPosition();
        
        if (plateau.estVide(pos)) {
            Pion pion = joueur.getPionNonPlace();
            if (pion != null) {
                plateau.placerPion(pion, pos);
            }
            if (estUnMoulin(pos, joueur.getCouleur())) {
                phasePrecedente = Phase.PLACE;
                phaseActuelle = Phase.STEAL;
            } else{
                model.setNextPlayer();
                // Joueur j1 = (Joueur) model.getPlayers().get(0);
                Joueur j2 = (Joueur) model.getPlayers().get(1);
                if (j2.getPionsRestants() == 0) {
                    phaseActuelle = Phase.MOVE;
                }
            }
        }
        else{
            System.out.println("pos invalide");
        }
    }
    public void playmove(){

    }
    public void playsteal(){

    }

    private boolean estUnMoulin(Position pos, Couleur couleur) {
        int x = pos.getX();
        int y = pos.getY();
        // for ?
        if (x%2 != 0) {
            
        }
        return false;
    }

    private Position askPosition() {
        System.out.print("Ligne (0-7) : ");
        int x = input.nextInt();
        System.out.print("Colonne (0-2) : ");
        int y = input.nextInt();
        return new Position(x, y);
    }
    public void initlook(){
        if (view.getGameStageView() == null) {
            return;
        }
        mapElementLook.clear();
        for(boardifier.model.GameElement element : model.getGameStage().getElements()){
            mapElementLook.put(element, view.getElementLook(element));
        }
    }

}