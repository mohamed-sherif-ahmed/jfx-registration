package sample;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import net.glxn.qrgen.core.image.ImageType;
import net.glxn.qrgen.javase.QRCode;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

import javax.print.*;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;
import java.io.*;
import java.security.spec.ECField;
import java.sql.Timestamp;
import java.util.UUID;

public class Controller {

    @FXML
    private TextField txtName;

    @FXML
    private TextField txtEmail;

    @FXML
    private TextField txtPhone;

    @FXML
    private Button btnSubmit;

    @FXML
    private BorderPane borderPane;

    PrintRequestAttributeSet pras;
    PrintService pss[];
    String payload = "";
    boolean append = true;


    public void initialize(){
        VirtualKeyboard vkb = new VirtualKeyboard();

        // just add a border to easily visualize the boundary of the keyboard:
        vkb.view().setStyle("-fx-border-color: darkblue; -fx-border-radius: 5;");

        borderPane.setBottom(vkb.view());

    }

    public void sendRequest(){
        System.out.println("IN SENENENE");

        UUID rGen = UUID.randomUUID();
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        File qrCodeFile = QRCode.from(rGen.toString()).to(ImageType.GIF).withSize(250, 250).file();
        try{
            this.payload = "data={" +
                    "\"name\":" + "\"" + this.txtName.getText() + "\"" +"," +
                    "\"email\":" + "\"" + this.txtEmail.getText() + "\"" + "," +
                    "\"phone\":" + "\"" + this.txtPhone.getText() + "\"" + "," +
                    "\"timestamp\":" + "\"" + timestamp.getTime() + "\"" + "," +
                    "\"qr_code\":" + "\"" + rGen.toString() + "\"" + "," +
                    "\"user_type\":\"client\"" +
                    "}";
            System.out.println(payload);
            StringEntity entity = new StringEntity(payload,
                    ContentType.APPLICATION_FORM_URLENCODED);

            HttpClient httpClient = HttpClientBuilder.create().build();
            HttpPost request = new HttpPost(Main.SERVER_URL + "/broker/user");
            request.setEntity(entity);

            HttpResponse response = httpClient.execute(request);
            System.out.println(response.getStatusLine().getStatusCode());

        }catch (Exception io){
            System.out.println(io.toString());
            saveOffline();
        }


        try{
            pras = new HashPrintRequestAttributeSet();
            pras.add(new Copies(1));
            pss = PrintServiceLookup.lookupPrintServices(DocFlavor.INPUT_STREAM.GIF, pras);

            if (pss.length == 0)
                throw new RuntimeException("No printer services available.");

            PrintService ps = pss[0];
            System.out.println("Printing to " + ps);
            DocPrintJob job = ps.createPrintJob();
            FileInputStream fin = new FileInputStream(qrCodeFile);
            Doc doc = new SimpleDoc(fin, DocFlavor.INPUT_STREAM.GIF, null);
            job.print(doc, pras);
            fin.close();
        }catch (Exception io){
            System.out.println(io.toString());
        }
    }

    public void saveOffline() {
        FileWriter out = null;

        try {
            out = new FileWriter("saved_file.sfr", append);
            out.write(this.payload);
            out.write("\n");
            out.flush();
            out.close();
            this.append = true;
        } catch (Exception fnfe) {
            System.out.println(fnfe.toString());
        }

    }

    public void syncOfflineFiles(){
        BufferedReader in = null;
        String s;

        try{
            in = new BufferedReader(new FileReader("saved_file.sfr"));
            while ((s = in.readLine()) != null){
                StringEntity entity = new StringEntity(s,
                        ContentType.APPLICATION_FORM_URLENCODED);

                HttpClient httpClient = HttpClientBuilder.create().build();
                HttpPost request = new HttpPost(Main.SERVER_URL + "/broker/user");
                request.setEntity(entity);

                HttpResponse response = httpClient.execute(request);
                System.out.println(response.getStatusLine().getStatusCode());
            }
            this.append = false;
            in.close();
        }catch (Exception e){
            System.out.println(e.toString());
        }


    }
}
