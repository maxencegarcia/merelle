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
        Jeu jeu = new Jeu(model, view);

        StageFactory.registerModelAndView("main", MerelleStageModel.class.getName(), MerelleStageView.class.getName());

        switch (gm) {
            case 1:
                String np1 = askname(1);
                String np2 = askname(2);
                System.out.println("Player 1 = " + np1);
                System.out.println("Player 2 = " + np2);

                MerelleStageModel stageModel = new MerelleStageModel("main", model);
                Joueur joueur1 = new Joueur(np1, Couleur.BLANC, 9, stageModel);
                Joueur joueur2 = new Joueur(np2, Couleur.NOIR, 9, stageModel);

                model.getPlayers().add(joueur1);
                model.getPlayers().add(joueur2);
                model.setGameStage(stageModel);
                jeu.setup();

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
                break;

            case 2:
                System.out.println("Player Vs Computer");
                break;

            case 3:
                System.out.println("Computer Vs Computer");
                break;
        }
    }

    public static String askname(int nb) {
        System.out.print("Nom du joueur " + nb + " : ");
        return input.nextLine();
    }
}