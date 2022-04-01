package cs1302.gallery;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.Scene;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonElement;
import com.google.gson.JsonArray;
import java.nio.charset.StandardCharsets;
import javafx.scene.layout.BorderPane;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

/**
 * This class represents an iTunes GalleryApp!.
 */
public class GalleryApp extends Application {
    // The root container for the application scene graph
    VBox root;
    // The container for menu bar
    MenuBar menuBar;
    // The container for play/pause button, search field, and update button
    ToolBar toolBar;
    // The container for progress bar
    HBox bottom;
    // User's input into search field
    String input;
    Scene scene;
    Stage stage;
    TilePane tilePane = new TilePane();
    ProgressBar progressBar = new ProgressBar();
    double progress = 0.0;
    JsonArray undisplayed;
    JsonArray used;
    ImageView[] imgs = new ImageView[20];
    Button pauseButton;
    Button update;
    boolean play = true;
    double playCount = -1.0;
    String[] results;
    Timeline timeline;
    TextField query;
    JsonArray jResults;

    /**
     * Start method to create the layout of the application.
     * @inheritdoc
     *
     */
    @Override
    public void start(Stage stage) {
        root = new VBox();
        // Adding components to container
        HBox pane = new HBox();
        BorderPane borderPane = new BorderPane();
        VBox topMenu = new VBox();
        topMenu.getChildren().addAll(addMenuBar(), addToolBar());
        //topMenu.getChildren().addAll(addToolBar());

        bottom = new HBox();
        progressBar.setLayoutX(25.0);
        progressBar.setLayoutY(550.0);
        Label bottomLabel = new Label("Images provided courtesy of iTunes");
        bottom.getChildren().addAll(progressBar, bottomLabel);

        // Creates menu option of file and adds to menu bar
        //Creates tool bar and adds buttons and text field
        VBox top = new VBox();
        top.getChildren().addAll(topMenu);
        pane.getChildren().addAll(root);

        borderPane.setTop(top);
        borderPane.setCenter(pane);
        borderPane.setBottom(bottom);

        Thread task = new Thread(() -> {
            getImages(input);
            Platform.runLater(() -> {
                root.getChildren().add(updateTilepane());
            });
        });


        task.setDaemon(true);
        task.start();
        scene = new Scene(borderPane, 500, 490);
        stage.setMaxWidth(1230);
        stage.setMaxHeight(720);
        stage.setTitle("Gallery!");
        stage.setScene(scene);
        stage.sizeToScene();
        stage.show();
    } // start

    /**
     * This method creates the top MenuBar object and its components to add to the
     * stage along with the event habdlers when the buttons are clicked.
     *
     * @return menuBar to be displayed
     */
    public MenuBar addMenuBar() {
        // Creates menu option of file and adds to menu bar
        Menu file = new Menu("File");
        //Menu theme = new Menu("Theme");
        Menu help = new Menu("Help");
        menuBar = new MenuBar();
        menuBar.getMenus().addAll(file, help);
        // Creates exit option for file and adds to File option
        MenuItem exitItem = new MenuItem("Exit");
        file.getItems().add(exitItem);
        // Creates about option for help and adds to Help option
        MenuItem helpItem = new MenuItem("About");
        help.getItems().add(helpItem);
        // Event Handler for Exit menu item
        exitItem.setOnAction(event -> System.exit(0));
        //Event Handler for Aboutme menu item
        helpItem.setOnAction(e -> {
            aboutMe();
        });
        return menuBar;
    }


    /**
     * Method creates the different components of the top bar and
     * adds them.
     */
    public void createToolBar() {
        // Creates tool bar and adds buttons and text field
        toolBar = new ToolBar();
        pauseButton = new Button("Play");
        Label searchQuery = new Label("Search Query:");
        query = new TextField("sza");
        input = parseInput(query);
        update = new Button("Update Images");
        toolBar.getItems().addAll(pauseButton, searchQuery, query,
                update);
    }

    /**
     * This method is a helper of the event handler for the pause button.
     */
    public void randomImgChange() {
        playCount++;
        // if playCount is even
        if (playCount % 2 == 0.0) {
            play = true;
        } //if playCount is odd
        if (playCount % 2 != 0.0) {
            play = false;
        }
        Platform.runLater(() -> {
            //if on play, change button and play timeline
            if (play) {
                pauseButton.setText("Pause");
                timeline.play();
                //play = true;
            } //if on pause, change button and pause timeline
            if (!play) {
                pauseButton.setText("Play");
                timeline.pause();
                //play = false;
                return;
            }
        });
        if (play) {
            ifPlay();
        }
    } //rndImg


    /**
     * This method creates the ABoutMe and handles the event for when the About item is pressed
     * from the menu.
     */
    public void aboutMe() {
        Stage about = new Stage();
        String myPic = "https://pbs.twimg.com/profile_images/1371285222780764166"
            + "/ho1zHE5c_400x400.jpg";
        about.setTitle("About Jayla Scott");
        VBox info = new VBox();
        Scene infoScene = new Scene(info);
        Text basicInfo = new Text();
        basicInfo.setText(
            " Jayla Scott \n jayla.scott@uga.edu \n Application Version 7.0");
        ImageView myPicture = new ImageView(myPic);
        info.getChildren().addAll(basicInfo, myPicture);
        about.setScene(infoScene);
        about.sizeToScene();
        about.showAndWait();
    } //aboutMe

    /**
     * This method reads in the user's search input from the text box.
     *
     * @param query the textfield in menubar with user input
     * @return string of the parsed input to be read from the text field
     */
    public String parseInput(TextField query) {
        input = query.getText();
        //holds the parts of user input separately
        String[] words = input.split(" ");
        input = "";
        //adds individual words from the search to string
        for (int i = 0; i < words.length; i++) {
            if (i == 0) {
                input = input + words[i];
                continue;
            }
            input = input + "+" + words[i];
        }
        progress = 0.0;
        return input;
    } //parseInput

    /**
     * This method calls the method that adds the tool bar along
     * with handling events for pressing the different components.
     * @return the the tool bar to be added to scene and displayed along with functioning components
     */
    public ToolBar addToolBar() {
        createToolBar();
        // Event Handler for the play/pause button
        pauseButton.setOnAction(e -> {
            randomImgChange();
        });
        //action for update button when clicked
        update.setOnAction(e -> {
            boolean running = false;
            //if timeline has been activated and running, pause it
            if (timeline != null) {
                if (timeline.getStatus() == Animation.Status.RUNNING) {
                    running = true;
                    timeline.pause();
                }
            }
            String newInput = parseInput(query);
            progressBar.setProgress(0.0);
            Thread task = new Thread(() -> {
                getImages(newInput);
                Platform.runLater(() -> {
                    updateTilepane();
                });
            });
            task.setDaemon(true);
            task.start();

            if (running) {
                timeline.play();
            }
        });
        return toolBar;
    }


    /**
     * Method used to randomly replace images on the tilepane
     * on a timeline.
     */
    public void ifPlay() {
        EventHandler<ActionEvent> handler = (e -> {
            if (jResults.size() > 21) {
                //if the button reads pause, pause timeline
                if (pauseButton.getText() == "Play") {
                    timeline.pause();
                    return;
                }
                //get random image not being displayed
                int randomUnused = (int) (Math.random()
                        * undisplayed.size());
                //get random image being displayed
                int randomDisplaying = (int) (Math.random()
                        * imgs.length);
                // object random number in array
                JsonObject result = undisplayed.get(randomUnused)
                        .getAsJsonObject();
                JsonElement artworkUrl100 = result.get("artworkUrl100");
                if (artworkUrl100 != null) {
                    randomReplacement(artworkUrl100, randomDisplaying);
                }
                if (used.contains(undisplayed.get(randomUnused))) {
                    undisplayed.remove(undisplayed.get(randomUnused));
                }
            }
        });
        setTimeline(handler);
    }

    /**
     * Method selects  a random image being displayed to replace with an undisplayed one
     * and sets it into the ImageView object and readds all ImageView objects to TilePane.
     *
     * @param artworkUrl100 JsonElement of newly introduced member
     * @param randomDisplaying int of new member in the undisplayed array
     */
    public void randomReplacement(JsonElement artworkUrl100, int randomDisplaying) {
        //take new member and add to used
        used.add(artworkUrl100);
        String urlArt = artworkUrl100.getAsString();
        Image image = new Image(urlArt);
        //create new imageview object in place of old member
        imgs[randomDisplaying] = new ImageView();
        //store random member being displayed back in unused
        undisplayed.add(jResults.get(randomDisplaying));
        imgs[randomDisplaying].setImage(image);
        imgs[randomDisplaying].setFitHeight(100.0);
        imgs[randomDisplaying].setFitWidth(100.0);
        tilePane.getChildren().clear();
        //add ImageView object to tile pane object
        for (int i = 0; i < 20; i++) {
            tilePane.getChildren().add(imgs[i]);
        }
    }

    /**
     * Method sets the timeline for the random image swap every 2 secs..
     *
     * @param handler the lambda expression that the timeline uses
     */
    public void setTimeline(EventHandler<ActionEvent> handler) {
        KeyFrame keyFrame = new KeyFrame(Duration.seconds(2), handler);
        timeline = new Timeline();
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.getKeyFrames().add(keyFrame);
        timeline.play();
    }

    /**
     * This method reads in the JSON query results and parses them.
     *
     * @param input the users newly entered search
     */
    public void getImages(String input) {
        //set tile pane to always show 5 col, 4 rows
        tilePane.setPrefColumns(5);
        tilePane.setPrefRows(4);
        InputStreamReader reader = null;
        URL url = null;
        //put user input into default itunes search url
        String qString = "https://itunes.apple.com/search?term="
                + input;
        encodeValue(qString);
        readAndParse(url, qString, reader);
        //if less than 21 results are gathered from search
        if (results.length < 21) {
            Platform.runLater(() -> {
                //show dialog box/popup mentioning the error
                Alert alert = new Alert(AlertType.ERROR);
                alert.setResizable(true);
                alert.setContentText("Please enter another search");
                alert.showAndWait();
                pauseButton.setText("Play");
            });
            return;
        }
        for (int i = 0; i < 20; i++) {
            importResults(i);
        }
    }

    /**
     * This is a helper method to read in the JSON query results and parse them.
     *
     * @param url         the link of the image from iTunes
     * @param qString the string that contains the parsed url link
     * @param reader      a reader which passes through the query results
     */
    public void readAndParse(URL url, String qString, InputStreamReader reader) {
        try {
            url = new URL(qString);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e.getMessage());
        }
        try {
            reader = new InputStreamReader(url.openStream());
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
        JsonParser jp = new JsonParser();
        JsonElement je = jp.parse(reader);
        // root of response
        JsonObject root = je.getAsJsonObject();
        // "results" array
        jResults = root.getAsJsonArray("results");
        // second copy of all members
        undisplayed = root.getAsJsonArray("results");
        // will hold all members being displayed
        used = new JsonArray();
        results = new String[jResults.size()];
    }

    /**
     * Method fills the results array with the string urls
     * from the JSON query results.
     *
     * @param i the number of the image url being imported (from proj faq)
     */
    public void importResults(int i) {
        JsonObject result = jResults.get(i).getAsJsonObject();
        used.add(result);
        JsonElement artworkUrl100 = result.get("artworkUrl100");
        if (artworkUrl100 != null) { // member might not exist
            String urlArt = artworkUrl100.getAsString();
            Image image = new Image(urlArt);
            results[i] = urlArt;
            imgs[i] = new ImageView();
            imgs[i].setImage(new Image(results[i]));
            Platform.runLater(() -> incrementProgress());
        }
    }

    /**
     * Method to update the images with results from the new user input.
     *
     * @return TilePane of images deing displayed
     */
    public TilePane updateTilepane() {
        int numResults = results.length;
        if (numResults < 21) {
            return tilePane;
        }
        //resets tile pane before adding new images
        tilePane.getChildren().clear();
        progress = 0.0;
        for (int i = 0; i < 20; i++) {
            imgs[i].setImage(new Image(results[i]));
            imgs[i].setFitWidth(100.0);
            imgs[i].setFitHeight(100.0);
            tilePane.getChildren().add(imgs[i]);
        }
        for (int x = 0; x < undisplayed.size(); x++) {
            if (used.contains(undisplayed.get(x))) {
                undisplayed.remove(undisplayed.get(x));
            }
        }
        return tilePane;
    }

    /**
     * This method encodes values in the URL query string.
     *
     * @param value the string to be encoded
     * @return String of the encoded url string
     */
    public String encodeValue(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * Method increases the progress of the images being downloaded from the
     * query.
     */
    public void incrementProgress() {
        //every time method is called, increment progress by 0.05
        progress = progress + 0.05;
        progressBar.setProgress(progress);
    }

    /**
     * This method creates the progress bar and courtesy label to add to the stage.
     *
     * @return HBox bottom that holds the progress bars
     */
    public HBox addProgressBar() {
        bottom = new HBox();
        progressBar.setLayoutX(25.0);
        progressBar.setLayoutY(550.0);
        Label bottomLabel = new Label("Images provided courtesy of iTunes");
        bottom.getChildren().addAll(progressBar, bottomLabel);
        return bottom;
    }

} // GalleryApp
