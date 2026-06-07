import boardifier.control.StageFactory;
import boardifier.model.Model;
import boardifier.model.GameException;
import boardifier.view.RootPane;
import boardifier.view.View;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.event.ActionEvent;
import javafx.stage.Stage;
import java.util.ResourceBundle;
import java.util.Locale;
import java.util.Scanner;

public class StarterController {

    @FXML private Button pvpButton;
    @FXML private Button pveButton;
    @FXML private Button eveButton;
    @FXML private MenuItem newButton;
    @FXML private MenuItem openButton;
    @FXML private MenuItem quitButton;
    @FXML private TextField player1Field;
    @FXML private TextField player2Field;
    @FXML private Button launchButton;
    @FXML private ComboBox<String> difficultyBox;
    @FXML private ComboBox<String> difficultyBox2;

    @FXML
    void onpvpclicked(ActionEvent event) throws Exception {
        loadScene("pvpstarter.fxml", pvpButton);
    }

    @FXML
    void onpveclicked(ActionEvent event) throws Exception {
        loadScene("pvestarter.fxml", pveButton);
    }

    @FXML
    void oneveclicked(ActionEvent event) throws Exception {
        loadScene("evestarter.fxml", eveButton);
    }

    @FXML
    void onquitclicked(ActionEvent event) {
        System.exit(0);
    }

    @FXML
    void onnewclicked(ActionEvent event) throws Exception {
        loadScene("starter.fxml", newButton);
    }

    @FXML
    void onopenclicked(ActionEvent event) {}


    @FXML
    void onlaunchpvpclicked(ActionEvent event) throws Exception {
        String name1 = player1Field.getText().trim();
        String name2 = player2Field.getText().trim();
        if (name1.isEmpty()) name1 = "Player 1";
        if (name2.isEmpty()) name2 = "Player 2";
        Stage stage = (Stage) launchButton.getScene().getWindow();
        launchGameScene(stage, setupGame(stage, name1, name2, Color.WHITE, Color.BLACK, -1, -1), "");
    }

    @FXML
    void onlaunchpveclicked(ActionEvent event) throws Exception {
        String name1 = player1Field.getText().trim();
        if (name1.isEmpty()) name1 = "Player 1";
        int diff = difficultyBox.getValue().equals("Double") ? 1 : 2;
        Stage stage = (Stage) launchButton.getScene().getWindow();
        launchGameScene(stage, setupGame(stage, name1, Merelle.randomName(1,2), Color.WHITE, Color.BLACK, -1, diff), difficultyBox.getValue());
    }

    @FXML
    void onlauncheeveclicked(ActionEvent event) throws Exception {
        int diff = difficultyBox.getValue().equals("Double") ? 1 : 2;
        int diff2 = difficultyBox2.getValue().equals("Double") ? 1 : 2;
        Stage stage = (Stage) launchButton.getScene().getWindow();
        launchGameScene(stage, setupGame(stage, Merelle.randomName(1,1), Merelle.randomName(2,1), Color.WHITE, Color.BLACK, 1, 1), difficultyBox.getValue() + " vs " + difficultyBox2.getValue());
    }

    @FXML
    public void initialize() {
        if (difficultyBox != null) {
            difficultyBox.getItems().addAll("Defense", "Double");
            difficultyBox.setValue("Defense");
        }
        if (difficultyBox2 != null) {
            difficultyBox2.getItems().addAll("Defense", "Double");
            difficultyBox2.setValue("Defense");
        }
    }

    private Game setupGame(Stage stage, String name1, String name2,Color color1, Color color2, int diff1, int diff2) throws GameException {
        Model model = new Model();
        RootPane boardifierRootPane = new RootPane();
        View view = new View(model, stage, boardifierRootPane);

        StageFactory.registerModelAndView("main",
                MerelleStageModel.class.getName(),
                MerelleStageView.class.getName());

        MerelleStageModel stageModel = new MerelleStageModel("main", model);

        PlayerC p1;
        if (diff1 >= 0) {
            p1 = new PlayerC(boardifier.model.Player.COMPUTER, name1, color1, 9, stageModel);
        } else {
            p1 = new PlayerC(name1, color1, 9, stageModel);
        }

        PlayerC p2;
        if (diff2 >= 0) {
            p2 = new PlayerC(boardifier.model.Player.COMPUTER, name2, color2, 9, stageModel);
        } else {
            p2 = new PlayerC(name2, color2, 9, stageModel);
        }

        model.getPlayers().add(p1);
        model.getPlayers().add(p2);
        model.setGameStage(stageModel);
        for (Pawn p : p1.getPawns()) stageModel.addElement(p);
        for (Pawn p : p2.getPawns()) stageModel.addElement(p);

        Game game = new Game(model, view, new Scanner(System.in));
        if (diff1 >= 0 && diff2 >= 0) {
            game.setup(diff1, diff2);
        } else if (diff2 >= 0) {
            game.setup(diff2);
        } else {
            game.setup();
        }

        MerelleStageView stageView = new MerelleStageView("main", stageModel);
        stageView.createLooks();
        view.setView(stageView);

        game.setFirstStageName("main");
        game.initLook();
        return game;
    }

    private void launchGameScene(Stage stage, Game game, String modeIA) throws Exception {
        ResourceBundle bundle = ResourceBundle.getBundle("nom", Locale.getDefault());
        FXMLLoader loader = new FXMLLoader(getClass().getResource("affi.fxml"), bundle);
        Parent gameRoot = loader.load();

        GameController gameController = loader.getController();
        gameController.init(game, game.getBoard(), game.getModel());
        gameController.setModeIA(modeIA);

        stage.setScene(new Scene(gameRoot));
        stage.show();

        new Thread(() -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            game.stageLoop();
        }).start();
    }

    private void loadScene(String fxmlFile, Button source) throws Exception {
        ResourceBundle bundle = ResourceBundle.getBundle("nom", Locale.getDefault());
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile), bundle);
        Parent root = loader.load();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.setScene(new Scene(root));
    }

    private void loadScene(String fxmlFile, MenuItem source) throws Exception {
        ResourceBundle bundle = ResourceBundle.getBundle("nom", Locale.getDefault());
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile), bundle);
        Parent root = loader.load();
        Stage stage = (Stage) source.getParentPopup().getOwnerWindow();
        stage.setScene(new Scene(root));
    }
}