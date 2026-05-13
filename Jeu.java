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
    private Scanner scanner;
    static final Scanner input = new Scanner(System.in);


    public Jeu(Model model, View view, Scanner scanner) {
        super(model, view);
        phaseActuelle = Phase.PLACE;
        this.scanner = scanner;
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
        Joueur joueur = (Joueur) model.getCurrentPlayer();
        System.out.println("its time for " + joueur.getNom() + " to play");
        System.out.println("Source : ");
        Position source = askPosition();
        System.out.println("destination");
        Position dest = askPosition();

        Pion pion =plateau.getPion(source);
        if (pion != null && pion.getCouleur() == joueur.getCouleur() && plateau.estVide(dest)) {
            Boolean canmove = false;
            if (joueur.compterPions() == 3) {
                canmove = true;
            }else if (sontcollé(source, dest)) {
                canmove = true;
            }
            if (canmove) {
                plateau.deplacerPion(pion, source, dest);
                if (estUnMoulin(dest, joueur.getCouleur())) {
                    phasePrecedente = Phase.MOVE;
                    phaseActuelle = Phase.STEAL;
                } else {
                    model.setNextPlayer();
                }
            
            }else {
                System.out.println("mouve non collé");
            }
        } else {
            System.out.println("move invalide");
        }
    }
    public void playsteal(){

    }

    private boolean estUnMoulin(Position pos, Couleur couleur) {
        int x = pos.getX();
        int y = pos.getY();
        // for ?
        if (x%2 != 0) {
            if (checklinecross(x, couleur)) {
                
            }
        }
        return false;
    }

    private Position askPosition() {
        // System.out.print("Ligne (0-7) : ");
        // int x = input.nextInt();
        // System.out.print("Colonne (0-2) : ");
        // int y = input.nextInt();
        // return new Position(x, y);
        int x = -1;
        int y = -1;
        boolean valide =false;
        System.out.print("Entrez la position (ligne 0-7, colonne 0-2) : ");
        String enter = input.next();
        while (!valide) {
            if (enter.length() == 2) {
                char c1 = enter.charAt(0);
                char c2 = enter.charAt(1);

                if (Character.isDigit(c1) && Character.isDigit(c2)) {
                    x = Character.getNumericValue(c1);
                    y = Character.getNumericValue(c2);
                    if (x >= 0 && x <= 7 && y >= 0 && y <= 2) {
                        valide = true;
                    }
                }

            }

            if (!valide) {
                System.out.println("Entrée invalide. Format attendu: LC (ex: 12 pour ligne 1, col 2) avec L:0-7 et C:0-2.");
            }
        }
        System.out.println("ligne = " + x);
        System.out.println("colonne = " + y);
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
    public boolean sontcollé(Position p1, Position p2){
        int x = p1.getX(), y = p1.getY(), x2 = p2.getX(), y2=p2.getY();

        if (y == y2) {
            int diff = Math.abs(x-x2);
            return diff ==1 || diff ==7;
        }else if (x==x2) {
            return Math.abs(y - y2) == 1 && x % 2 != 0;
        }
        return false;
    }

    private boolean checklinecross(int x, Couleur c){
        Pion p1 = plateau.getPion(new Position(x, 0));
        Pion p2 = plateau.getPion(new Position(x, 1));
        Pion p3 = plateau.getPion(new Position(x, 2));
        return p1 != null && p1.getCouleur() == c && p2 != null && p2.getCouleur() == c &&p3 != null && p3.getCouleur() == c;
    }

}