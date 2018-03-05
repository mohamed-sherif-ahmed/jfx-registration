package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.BufferedReader;
import java.io.FileReader;

public class Main extends Application {
    public static String SERVER_URL;
    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("/sample.fxml"));
        primaryStage.setTitle("Register");
        primaryStage.setScene(new Scene(root, 560, 460));
        primaryStage.show();
    }


    public static void main(String[] args) {
        BufferedReader in = null;

        try{
            in = new BufferedReader(new FileReader("config.cnf"));
            Main.SERVER_URL = in.readLine();
            in.close();
        }catch (Exception io){
            System.out.println(io.toString());
        }

        launch(args);

    }
}
