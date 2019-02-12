package sample;

import com.jfoenix.controls.JFXProgressBar;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    private Thread th;
    private String search_term;
    private String Videotype = "mp4|wma|aac|avi|flv|mov|mkv";
    private String Doctype = "pdf|doc|docx|webm";
    private String Audiotype = "mp3|wav";
    private String Pictype = "png|jpg|gif";
    private String VMediatype[] = {"mp4", "wma", "avi", "flv", "mov", "mkv","webm"};
    private String DMediatype[] = {"pdf", "doc", "docx", "epub"};
    private String AMediatype[] = {"mp3", "wav","aac"};
    private String PMediatype[] = {"png", "jpg","gif"};
    private boolean exist;
    @FXML
    TextField Search;
    @FXML
    Pane bStart;
    @FXML
    GridPane data;
    @FXML
    JFXProgressBar progress;
    @FXML
    AnchorPane main;
    @FXML
    Label rescount, searching;
    @FXML
    ChoiceBox<String> type;
    @FXML
    Button BStop;

    private int i;
    private Clipboard clipboard;
    private ClipboardContent content;
    private Document htmlDocument;
    private Document links;

    // Initialise the interface

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        type.getItems().add("Audio");
        type.getItems().add("Video");
        type.getItems().add("Document");
        type.getItems().add("Picture");
        clipboard = Clipboard.getSystemClipboard();
        content = new ClipboardContent();
    }

    // -------------------------------------------------------------

    // Starting the research

    @FXML
    void StartSearch() {
        if (!Search.getText().isEmpty() && !type.getSelectionModel().isEmpty()) {
            th = new Thread(() -> {
                try {
                    Platform.runLater(() -> {
                        rescount.setText("");
                        Search.setEditable(false);
                        bStart.setOpacity(0);
                        BStop.setOpacity(1);
                        BStop.setDisable(false);
                        progress.setOpacity(1);
                        type.setDisable(true);
                        searching.setOpacity(1);
                        data.getChildren().clear();
                    });

                    i = 0;
                    if (type.getSelectionModel().getSelectedItem().equals("Audio")) {
                        search_term = Audiotype;
                    }

                    if (type.getSelectionModel().getSelectedItem().equals("Video")) {
                        search_term = Videotype;
                    }

                    if (type.getSelectionModel().getSelectedItem().equals("Document")) {
                        search_term = Doctype;
                    }

                    if (type.getSelectionModel().getSelectedItem().equals("Picture")) {
                        search_term = Pictype;
                    }


                    htmlDocument = Jsoup.connect("https://www.google.fr/search?q=" +
                            Search.getText() + " -inurl:(htm|html|php|pls|txt) intitle:index.of “last modified” (" + search_term + ")" + "&num=10000").get();

                    for (Element e : htmlDocument.getElementById("rso").getElementsByTag("a")) {
                        try {
                            links = Jsoup.connect(e.attr("href")).get();
                            for (Element ee : links.getElementsByTag("a")) {
                                if (!ee.text().contains("translate.google")
                                        && !ee.text().contains("webcache.googleusercontent")
                                        && !e.attr("href").contains("translate.google")
                                        && !e.attr("href").contains("webcache.googleusercontent")) {
                                    if (e.text().contains("Index of")) {
                                        if (requestedFormat(e.attr("href") + ee.attr("href")) && ContainRequested(ee.attr("href"), Search.getText())) {
                                            AddElt(ee.text(), e.attr("href") + ee.attr("href"));
                                        } else {
                                            if (ee.attr("href").endsWith("/"))
                                                gettingInside(e.attr("href") + ee.attr("href"));
                                        }
                                    } else {
                                        if (requestedFormat(e.attr("href") + ee.attr("href")) && ContainRequested(ee.attr("href"), Search.getText()))
                                            AddElt(ee.text(), e.attr("href") + ee.attr("href"));
                                    }
                                }
                            }
                        } catch (Exception exp) {
                           System.out.println(exp.getMessage());
                        }
                    }
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            });
            th.setDaemon(true);
            th.start();
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Dont forget to write what you are looking for !");
            alert.show();
        }
    }
    // -------------------------------------------------------------

    // Helpful functions -------------------------------------------------------------

    private synchronized boolean ContainRequested(String str, String requested) {
        exist = false;
        for (String s : requested.split(" ")) {
            if (str.toLowerCase().contains(s.toLowerCase().trim())) {
                exist = true;
            }
        }
        if (exist) {
            exist = requestedFormat(str);
        }
        return exist;
    }

    private synchronized boolean requestedFormat(String str) {
        exist = false;
        if (type.getSelectionModel().getSelectedItem().equals("Audio")) {
            for (String s : AMediatype) {
                if (str.contains("." + s)) {
                    exist = true;
                }
            }
        }

        if (type.getSelectionModel().getSelectedItem().equals("Video")) {
            for (String s : VMediatype) {
                if (str.contains("." + s)) {
                    exist = true;
                }
            }
        }

        if (type.getSelectionModel().getSelectedItem().equals("Document")) {
            for (String s : DMediatype) {
                if (str.contains("." + s)) {
                    exist = true;
                }
            }
        }

        if (type.getSelectionModel().getSelectedItem().equals("Picture")) {
            for (String s : PMediatype) {
                if (str.contains("." + s)) {
                    exist = true;
                }
            }
        }


        return exist;
    }

    private synchronized void gettingInside(String url) {
        try {
            Document insidelinks = Jsoup.connect(url).get();
            for (Element ee : insidelinks.getElementsByTag("a")) {
                if (requestedFormat(ee.attr("href")) && ContainRequested(ee.attr("href"), Search.getText())) {
                    //      System.out.println(ee.text()+"  url :"+url+ee.attr("href"));
                    AddElt(ee.text(), url + ee.attr("href"));
                }
            }
        } catch (Exception e) {
            // e.printStackTrace();
        }
    }

    private synchronized void AddElt(String t, String url) {
        try {
            Label title = new Label(t);
            Button getLink = new Button("Get The Link");
            getLink.setStyle("-fx-text-fill: #ffffff;\n" +
                    "-fx-background-color:  #411d1f;");
            getLink.setOnAction(event -> {
                System.out.println("Load " + url);
                content.putString(url);
                clipboard.setContent(content);
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setContentText("Link copied !");
                alert.setHeaderText("");
                alert.setTitle("");
                alert.show();
            });
            HBox.setHgrow(title, Priority.ALWAYS);
            HBox.setHgrow(getLink, Priority.ALWAYS);
            title.setTextFill(Color.WHITE);
            Platform.runLater(() -> {
                data.add(title, 0, i);
                data.add(getLink, 1, i);
                i++;
                rescount.setText(String.valueOf(i));
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // -------------------------------------------------------------

    // Stop research -------------------------------------------------------------

    @FXML
    public void Stop() {
        try {
            th.stop();
            BStop.setOpacity(0);
            BStop.setDisable(true);
            Search.setEditable(true);
            bStart.setOpacity(1);
            searching.setOpacity(0);
            progress.setOpacity(0);
            type.setDisable(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // -------------------------------------------------------------
}
