package sample;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.property.HorizontalAlignment;
import com.itextpdf.layout.property.TextAlignment;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import net.glxn.qrgen.core.image.ImageType;
import net.glxn.qrgen.javase.QRCode;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.*;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import javax.print.*;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;
import java.awt.print.PrinterJob;
import java.io.*;
import java.sql.Timestamp;
import java.util.Scanner;
import java.util.UUID;

import com.google.gson.*;
import org.apache.http.util.EntityUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.printing.PDFPageable;

public class Controller {

    @FXML
    private BorderPane borderPane;

    @FXML
    private TextField txtName;

    @FXML
    private TextField txtEmail;

    @FXML
    private TextField txtPhone;

    @FXML
    private Button btnSubmit;

    @FXML
    private Label statusLabel;

    PrintRequestAttributeSet pras;
    PrintService pss[];
    String payload = "";
    boolean append = true;
    Scanner sc = new Scanner(System.in);


    public void initialize(){
        VirtualKeyboard vkb = new VirtualKeyboard();

        // just add a border to easily visualize the boundary of the keyboard:
        vkb.view().setStyle("-fx-border-color: darkblue; -fx-border-radius: 5;");

        //borderPane.setBottom(vkb.view());

    }

    public void sendRequest(){
        System.out.println("IN SENENENE");

        UUID rGen = UUID.randomUUID();
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        if (this.txtPhone.getText().matches("01[0125][0-9]{8}")) {
            System.out.println("True");
        } else {
            System.out.println("false");
        }




        try{

            Request rq = new Request(this.txtName.getText(), this.txtPhone.getText(), this.txtEmail.getText(), this.txtEmail.getText(), this.txtPhone.getText(), "" + timestamp.getTime());
            Gson rqJSON = new Gson();
            this.payload = rqJSON.toJson(rq);
//            this.payload = "data={" +
//                    "\"name\":" + "\"" + this.txtName.getText() + "\"" +"," +
//                    "\"email\":" + "\"" + this.txtEmail.getText() + "\"" + "," +
//                    "\"username\":" + "\"" + this.txtEmail.getText() + "\"" + "," +
//                    "\"phone\":" + "\"" + this.txtPhone.getText() + "\"" + "," +
//                    "\"password\":" + "\"" + this.txtPhone.getText() + "\"" + "," +
//                    "\"date\":" + "\"" + timestamp.getTime() + "\"" + "," +
//                    "\"qr_code\":" + "\"" + rGen.toString() + "\"" + "," +
//                    "\"user_type\":\"client\"" +
//                    "}";
            System.out.println(payload);
            StringEntity entity = new StringEntity(payload,
                    ContentType.APPLICATION_FORM_URLENCODED);

            // HttpClient httpClient = HttpClientBuilder.create().build();
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpParams params = httpClient.getParams();
            HttpConnectionParams.setConnectionTimeout(params, 50000);
            HttpConnectionParams.setSoTimeout(params, 50000);
            HttpPost request = new HttpPost(Main.SERVER_URL);
            request.setEntity(entity);
            request.setHeader("Content-Type", "application/json");
            HttpResponse response = httpClient.execute(request);
            HttpEntity entity1 = response.getEntity();
            System.out.println(entity1);
            String entnt = EntityUtils.toString(entity1);
            Response rs = rqJSON.fromJson(entnt, Response.class);
            this.statusLabel.setText(rs.getMsg());
            System.out.println(rs.getBody().getUser().getQr_code());
            System.out.println(response.getStatusLine().getStatusCode());




            File qrCodeFile = QRCode.from(rs.getBody().getUser().getQr_code()).to(ImageType.JPG).withSize(300, 300).file();

            PdfWriter wrtr = new PdfWriter("test1.pdf");
            System.out.println(qrCodeFile.getAbsolutePath());
            //Initialize PDF document
            PdfDocument pdf = new PdfDocument(wrtr);

            // Initialize document
            Document document = new Document(pdf);

            document.setHorizontalAlignment(HorizontalAlignment.CENTER);

            Paragraph p = new Paragraph();
            p.add(rs.getBody().getUser().getName());
            p.setHorizontalAlignment(HorizontalAlignment.CENTER);
//            Text name = new Text(rs.getBody().getUser().getName());
//            Text index = new Text(rs.getBody().getUser().getIndex());
            Paragraph p2 = new Paragraph();
            p2.add("INDEX : " + rs.getBody().getUser().getIndex());
            p2.setHorizontalAlignment(HorizontalAlignment.CENTER);

            document.add(p);
            document.add(new Image(ImageDataFactory.create(qrCodeFile.getAbsolutePath())).setHorizontalAlignment(HorizontalAlignment.CENTER));
            document.add(p2);

            document.close();
            printFile();

        }catch (Exception io){
            //io.printStackTrace();
            System.out.println(io.getMessage());
            statusLabel.setText(io.getMessage());
            saveOffline();
        }


        try{
            
//            System.out.println("Printing to " + ps);
//            DocPrintJob job = ps.createPrintJob();
//            //FileInputStream fin = new FileInputStream("test1.pdf");
//            PDDocument document = PDDocument.load(new File("test1.pdf"));
//            Doc doc = new SimpleDoc(document, DocFlavor.INPUT_STREAM.AUTOSENSE, null);
//            job.print(doc, new HashPrintRequestAttributeSet());
////            fin.close();
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

    public void printFile() throws Exception{
        pras = new HashPrintRequestAttributeSet();
        pras.add(new Copies(1));
        pss = PrintServiceLookup.lookupPrintServices(DocFlavor.SERVICE_FORMATTED.PAGEABLE, pras);

        if (pss.length == 0)
            throw new RuntimeException("No printer services available.");

        for (PrintService p : pss){
            System.out.println(p.getName());
        }


        PrintService ps = pss[Main.PRINTER_NUMBER];
        PDDocument document = PDDocument.load(new File("test1.pdf"));
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPageable(new PDFPageable(document));
        job.setPrintService(ps);
        job.print();
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
