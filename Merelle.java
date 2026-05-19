import java.util.Random;
import java.util.Scanner;
import boardifier.model.Model;
import boardifier.view.View;
import boardifier.control.StageFactory;
import boardifier.model.GameException;

public class Merelle {
    static final Scanner input = new Scanner(System.in);

    public static void main(String[] args) {
        int gm = ask();
        lancer(gm);
    }

    public static int ask() {
        int gm = 0;
        boolean valide = false;

        System.out.println("Choose your gamemode:");
        System.out.println("Player Versus Player (1)");
        System.out.println("Player Versus Computer (2)");
        System.out.println("Computer Versus Computer (3)");

        while (!valide) {
            if (!input.hasNext()) {
                System.out.println("End of input stream. Exiting.");
                System.exit(0);
            }
            if (input.hasNextInt()) {
                gm = input.nextInt();
                input.nextLine();

                if (verif(gm)) {
                    valide = true;
                } else {
                    System.out.println("Choose a possible gamemode: 1, 2 or 3\n");
                }
            } else {
                System.out.println("Error : Enter one of the following number(1, 2 or 3).\n");
                input.nextLine();
            }
        }

        if (gm == 1) System.out.println("\nYou chose Player Versus Player");
        if (gm == 2) System.out.println("\nYou chose Player Versus Computer");
        if (gm == 3) System.out.println("\nYou chose Computer Versus Computer");

        return gm;
    }

    public static boolean verif(int gm) {
        return gm == 1 || gm == 2 || gm == 3;
    }

    public static void lancer(int gm) {
        Model model = new Model();
        View view = new View(model);
        Jeu jeu = new Jeu(model, view, input);

        StageFactory.registerModelAndView("main", MerelleStageModel.class.getName(), MerelleStageView.class.getName());
        MerelleStageModel stageModel = new MerelleStageModel("main", model);
        Joueur joueur1, joueur2;
        int iadiff =1;
        int ia1diff=1;
        int ia2diff=1;

        switch (gm) {
            case 1:
                String np1 = askname(1);
                String np2 = askname(2);
                System.out.println("Player 1 = " + np1);
                System.out.println("Player 2 = " + np2);

                joueur1 = new Joueur(np1, Couleur.BLANC, 9, stageModel);
                joueur2 = new Joueur(np2, Couleur.NOIR, 9, stageModel);

                
                break;

            case 2:
                String np = askname(1);
                joueur1 = new Joueur(np, Couleur.BLANC, 9, stageModel);
                joueur2 = new Joueur(boardifier.model.Player.COMPUTER, "Robert Hue", Couleur.NOIR, 9, stageModel);
                System.out.println("choisissez la difficulté de l'ia 1");
                System.out.println("niveau 1 (1)");
                System.out.println("niveau 2 (2)");
                iadiff = input.nextInt();
                break;
                
            case 3:
                joueur1 = new Joueur(boardifier.model.Player.COMPUTER, randomnom(1, 1), Couleur.BLANC, 9, stageModel);
                joueur2 = new Joueur(boardifier.model.Player.COMPUTER, randomnom(2, 1), Couleur.NOIR, 9, stageModel);
                System.out.println("choisissez la difficulté de l'ia 1");
                System.out.println("niveau 1 (1)");
                System.out.println("niveau 2 (2)");
                ia1diff = input.nextInt();
                System.out.println("choisissez la difficulté de l'ia 2");
                System.out.println("niveau 1 (1)");
                System.out.println("niveau 2 (2)");
                ia2diff = input.nextInt();
                break;
            default : 
                return;
        }

        model.getPlayers().add(joueur1);
        model.getPlayers().add(joueur2);
        model.setGameStage(stageModel);
        if (gm == 1) {
            jeu.setup();
        }
        if (gm == 2) {
            jeu.setup(iadiff);
        }
        if (gm == 3) {
            jeu.setup(ia1diff, ia2diff);
        }
        // jeu.setup();

        for(Pion p : joueur1.getPions()) stageModel.addElement(p);
        for(Pion p : joueur2.getPions()) stageModel.addElement(p);


        jeu.setFirstStageName("main");

        try {
            MerelleStageView stageView = new MerelleStageView("main", stageModel);
            stageView.createLooks();
            view.setView(stageView);
            // initialisation de la vue a faire
            // jeu.startGame();
            jeu.initlook();
        } catch (GameException e) {
            System.err.println("Erreur lors du démarrage du jeu : " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }

        jeu.stageLoop();
    }

    public static String askname(int nb) {
        System.out.print("Nom du joueur " + nb + " : ");
        return input.nextLine();
    }

    public static String randomnom(int partie, int theme){
        Random randomNumbers = new Random();
        int value = (randomNumbers.nextInt(8)+1);
        if( partie == 1 && value%2==0) {
            while (value%2==0) {
                value = (randomNumbers.nextInt(8)+1);
            }
        }
        if( partie == 2 && value%2!=0) {
            while (value%2!=0) {
                value = (randomNumbers.nextInt(8)+1);
            }
        }
        String nom ="computer";
        if (theme == 1) {
            if (value == 1) {
            return nom = "Robert Hue";
            }
            if (value == 2) {
                return nom = "Jean Lassalle";
            }
            if (value == 3) {
                return nom = "Ségolène Royal";
            }
            if (value == 4) {
                return nom = "Nicolas Dupont-Aignan";
            }
            if (value == 5) {
                return nom = "Sandrine Rousseau";
            }
            if (value == 6) {
                return nom = "Rachida Dati";
            }
            if (value == 7) {
                return nom = "Philippe Poutou";
            }
            if (value == 8) {
                return nom = "Patrick Balkany";
            }
        }
        return nom;
    }
}