package view;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.util.ResourceBundle;
import java.util.Locale;


public class MerelleApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        ResourceBundle bundle = ResourceBundle.getBundle("nom", Locale.getDefault());

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/view/starter.fxml"),
                bundle
        );

        Parent root = loader.load();

        Scene scene = new Scene(root);
        primaryStage.setTitle("Merelle");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}