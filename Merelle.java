import java.util.Random;
import java.util.Scanner;
import boardifier.model.Model;
import boardifier.view.View;
import boardifier.control.StageFactory;
import boardifier.model.GameException;
import javafx.application.Application;
import javafx.stage.Stage;
import boardifier.view.RootPane;

public class Merelle extends Application {
    static final Scanner input = new Scanner(System.in);
    private static int gamemode;

    public static void main(String[] args) {
        gamemode = ask();
        Application.launch(Merelle.class, args);
    }

    public static int ask() {
        int gm = 0;
        boolean valid = false;

        System.out.println("Choose your gamemode:");
        System.out.println("Player Versus Player (1)");
        System.out.println("Player Versus Computer (2)");
        System.out.println("Computer Versus Computer (3)");

        while (!valid) {
            if (!input.hasNextLine()) {
                System.out.println("End of input stream. Exiting.");
                System.exit(0);
            }

            String line = input.nextLine().trim();

            // Vérification de la commande d'arrêt
            if (line.equalsIgnoreCase("stop")) {
                System.out.println("Stopping program...");
                System.exit(0);
            }

            try {
                gm = Integer.parseInt(line);
                if (verif(gm)) {
                    valid = true;
                } else {
                    System.out.println("Choose a possible gamemode: 1, 2 or 3\n");
                }
            } catch (NumberFormatException e) {
                System.out.println("Error: Enter one of the following numbers (1, 2 or 3), or type 'stop'.\n");
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

    @Override
    public void start(Stage primaryStage) throws Exception {
        Model model = new Model();
        RootPane rootPane = new RootPane();
        View view = new View(model, primaryStage, rootPane);
        Game game = new Game(model, view, input);

        StageFactory.registerModelAndView("main", MerelleStageModel.class.getName(), MerelleStageView.class.getName());
        MerelleStageModel stageModel = new MerelleStageModel("main", model);
        PlayerC player1 = null, player2 = null;
        int aiDiff = 1;
        int ai1Diff = 1;
        int ai2Diff = 1;

        switch (gamemode) {
            case 1:
                String np1 = askName(1);
                String np2 = askName(2);
                System.out.println("Player 1 = " + np1);
                System.out.println("Player 2 = " + np2);

                player1 = new PlayerC(np1, Color.WHITE, 9, stageModel);
                player2 = new PlayerC(np2, Color.BLACK, 9, stageModel);
                break;

            case 2:
                String np = askName(1);
                player1 = new PlayerC(np, Color.WHITE, 9, stageModel);
                player2 = new PlayerC(boardifier.model.Player.COMPUTER, "Robert Hue", Color.BLACK, 9, stageModel);
                aiDiff = askDifficulty("AI");
                break;

            case 3:
                player1 = new PlayerC(boardifier.model.Player.COMPUTER, randomName(1, 1), Color.WHITE, 9, stageModel);
                player2 = new PlayerC(boardifier.model.Player.COMPUTER, randomName(2, 1), Color.BLACK, 9, stageModel);
                ai1Diff = askDifficulty("AI 1");
                ai2Diff = askDifficulty("AI 2");
                break;

            default:
                return;
        }

        model.getPlayers().add(player1);
        model.getPlayers().add(player2);
        model.setGameStage(stageModel);

        if (gamemode == 1) {
            game.setup();
        } else if (gamemode == 2) {
            game.setup(aiDiff);
        } else if (gamemode == 3) {
            game.setup(ai1Diff, ai2Diff);
        }

        for (Pawn p : player1.getPawns()) stageModel.addElement(p);
        for (Pawn p : player2.getPawns()) stageModel.addElement(p);

        game.setFirstStageName("main");

        try {
            MerelleStageView stageView = new MerelleStageView("main", stageModel);
            stageView.createLooks();
            view.setView(stageView);
            game.initLook();
        } catch (GameException e) {
            System.err.println("Error starting game: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }

        new Thread(() -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {}
            game.stageLoop();
        }).start();
    }

    public static String askName(int nb) {
        System.out.print("Player " + nb + " name: ");
        String name = input.nextLine().trim();

        if (name.equalsIgnoreCase("stop")) {
            System.out.println("Stopping program...");
            System.exit(0);
        }

        return name;
    }

    public static int askDifficulty(String aiName) {
        System.out.println("Choose " + aiName + " difficulty:");
        System.out.println("Level 1 (1)");
        System.out.println("Level 2 (2)");

        while (true) {
            if (!input.hasNextLine()) System.exit(0);
            String line = input.nextLine().trim();

            if (line.equalsIgnoreCase("stop")) {
                System.out.println("Stopping program...");
                System.exit(0);
            }

            try {
                int diff = Integer.parseInt(line);
                if (diff == 1 || diff == 2) {
                    return diff;
                } else {
                    System.out.println("Please choose 1 or 2.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Error: Enter 1 or 2, or type 'stop'.");
            }
        }
    }

    public static String randomName(int slot, int theme) {
        Random randomNumbers = new Random();
        int value = (randomNumbers.nextInt(8) + 1);
        if (slot == 1 && value % 2 == 0) {
            while (value % 2 == 0) {
                value = (randomNumbers.nextInt(8) + 1);
            }
        }
        if (slot == 2 && value % 2 != 0) {
            while (value % 2 != 0) {
                value = (randomNumbers.nextInt(8) + 1);
            }
        }
        String name = "computer";
        if (theme == 1) {
            if (value == 1) return "Robert Hue";
            if (value == 2) return "Jean Lassalle";
            if (value == 3) return "Ségolène Royal";
            if (value == 4) return "Nicolas Dupont-Aignan";
            if (value == 5) return "Sandrine Rousseau";
            if (value == 6) return "Rachida Dati";
            if (value == 7) return "Philippe Poutou";
            if (value == 8) return "Patrick Balkany";
        }
        return name;
    }
}
