package gui;


import elementi.*;
import elementi.TableRow;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTreeCell;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import util.WindowsManager;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static java.lang.Math.atan;
import static java.lang.Math.floor;
import static java.lang.Math.round;
import static java.lang.String.format;
import static javafx.application.Platform.runLater;

public class Main extends Application {
    private static Stage stage;
    private static final int BUTTON_WIDTH = 55;
    private static final int BUTTON_HEIGHT = 55;
    private ObservableList<TableRow> listaBrani = FXCollections.observableArrayList();
    private MediaPlayer mediaPlayer;
    private static TreeItem<String> playlistTreeItem;
    private static Map<TreeItem<String>, Playlist> mappaPlaylist = new HashMap<>();
    private TreeView<String> playList;
    private static TreeItem<String> playlistCorrente = null;
    private TableView tableSongs;
    private Duration duration;
    private Slider progressMusic;
    private Slider volumeSlider;
    private Media mediaCorrente;
    private Label testoDurata = new Label("00:00/00:00");
    private static int i = -1;
    private TableRow tableRow;
    private String playPause = "pause";
    private static Image playPauseImg;
    private static Button playPauseButton;
    private static ToggleButton shuffleBtn;
    private String durationMedia = "0:00";
    private Label songLbl;
    private List<Integer> listaIndiciVisitati = new ArrayList<>();
    private static int codice = 1;
    private String maximizedMinimized = "minimized";
    private Boolean resizebottom = false;
    private double dx;
    private double dy;
    private double xOffset;
    private double yOffset;
    private String angolo = "";
    private static SplitPane sp;

    @Override
    public void start(Stage primaryStage) throws Exception {
        stage = primaryStage;
        stage.getIcons().add(new Image("iconaProg.png"));
        Scene scene = new Scene(window(), 1520, 900);
        scene.setFill(Color.TRANSPARENT);
        stage.initStyle(StageStyle.TRANSPARENT);
        scene.getStylesheets().add(getClass().getResource("treeView.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.show();
        fillaCampi();
    }


    public static void main(String[] args) {
        launch(args);
    }

    //crea tutta la finestra
    private Parent window() {
        BorderPane mainBP = new BorderPane();
        mainBP.setTop(controlMusicBox());
        mainBP.setCenter(treeTable());
        mainBP.setBottom(barraRicerca());
        

        mainBP.setOnMousePressed(event -> {

            if (event.getX() > stage.getWidth() - 10
                    && event.getX() < stage.getWidth() + 10
                    && event.getY() > stage.getHeight() - 10
                    && event.getY() < stage.getHeight() + 10) {
                resizebottom = true;
                angolo = "bassoDestra";
                dx = stage.getWidth() - event.getX();
                dy = stage.getHeight() - event.getY();

            }
//            else if(event.getX() < stage.getX() - 10
//                    && event.getX() > stage.getX() + 10
//                    && event.getY() < stage.getY() - 10
//                    && event.getY() > stage.getY() + 10) {
//                resizebottom = true;
//                angolo = "altoSinistra";
//
//                dx = stage.getWidth() - event.getX();
//                dy = stage.getHeight() + event.getY();
//            }

        });

        mainBP.setOnMouseDragged(event -> {
            if (resizebottom == true){
                switch (angolo){
                    case "bassoDestra":
                        stage.setWidth(event.getX() + dx);
                        stage.setHeight(event.getY() + dy);
                        break;
                    case "altoSinistra":
                        stage.setWidth(event.getX() + dx);
                        stage.setHeight(event.getY() + dy);
                        break;
                }
            }

        });
        return mainBP;
    }


    // crea il box per il controllo della musica
    private VBox controlMusicBox() {
        //bottoni gui con listener
        Image lyricsImg = new Image(getClass().getResourceAsStream("/lyrics.png"));
        Button lyricsBtn = WindowsManager.createButton(null, BUTTON_WIDTH, BUTTON_HEIGHT, lyricsImg, false);
        lyricsBtn.setOnAction(e -> {
            try{
                String[] percorso = mediaCorrente.getSource().replaceAll("%20", "").split("/");
                String nomeFileIntero = percorso[percorso.length-1];
                String nomeAnime = nomeFileIntero.split("-")[0];
                String nomeSong = nomeFileIntero.split("-")[1];
                if (nomeSong.substring(nomeSong.length()-4, nomeSong.length()).equals(".mp3")) nomeSong = nomeSong.substring(0, nomeSong.length()-4);
                String nomeLink = "http://www.animelyrics.com/anime/" + nomeAnime + "/" + nomeSong + ".htm";
                java.awt.Desktop.getDesktop().browse(java.net.URI.create(nomeLink.toLowerCase()));
            }catch (Exception e2){}
        });

        playPauseImg = new Image(getClass().getResourceAsStream("/play.png"));
        playPauseButton = WindowsManager.createButton(null, BUTTON_WIDTH, BUTTON_HEIGHT, playPauseImg, false);
        playPauseButton.setOnAction(e -> {
            if (playPause.equals("pause")) {
                playMedia();
                if (mediaPlayer != null) {
                    playPauseImg = new Image(getClass().getResourceAsStream("/pause.png"));
                    playPause = "play";
                } else {
                    if (tableSongs.getSelectionModel().getSelectedItem() != null) {
                        i = tableSongs.getSelectionModel().getFocusedIndex();
                        setMedia();
                        playMedia();
                        if (mediaPlayer != null) {
                            playPauseImg = new Image(getClass().getResourceAsStream("/pause.png"));
                            playPause = "play";
                        }
                    }
                }
            } else if (mediaPlayer != null) {
                mediaPlayer.pause();
                playPauseImg = new Image(getClass().getResourceAsStream("/play.png"));
                playPause = "pause";
            }
            playPauseButton.setGraphic(new ImageView(playPauseImg));
        });

        Image backwardImg = new Image(getClass().getResourceAsStream("/backward.png"));
        Button backwardButton = WindowsManager.createButton(null, BUTTON_WIDTH, BUTTON_HEIGHT, backwardImg, false);
        backwardButton.setOnAction(e -> {
            if (mediaPlayer != null) {
                i--;
                if (i < 0) i = listaBrani.size() - 1;
                setMedia();
                playMedia();
                tableSongs.getSelectionModel().select(i);
            }
        });

        Image forwardImg = new Image(getClass().getResourceAsStream("/forward.png"));
        Button forwardButton = WindowsManager.createButton(null, BUTTON_WIDTH, BUTTON_HEIGHT, forwardImg, false);
        forwardButton.setOnAction(e -> {
            if (mediaPlayer != null) mediaPlayer.seek(mediaPlayer.getTotalDuration());
        });

        Image shuffleImg = new Image(getClass().getResourceAsStream("/shuffle.png"));
        shuffleBtn = new ToggleButton(null, new ImageView(shuffleImg));
        shuffleBtn.setPrefSize(BUTTON_WIDTH, BUTTON_HEIGHT);

        //box del controllo del volume
        Label testoVolume = new Label("Volume");
        volumeSlider = new Slider(0, 1, 0.5);
        volumeSlider.setOrientation(Orientation.VERTICAL);
        volumeSlider.setPrefHeight(50);
        VBox sliderVolumeBox = WindowsManager.createVBox(0, 2, Pos.CENTER, testoVolume, volumeSlider);

        //box della durata e lo slider della canzone
        progressMusic = new Slider();
        progressMusic.valueProperty().addListener(ov -> {
            if (progressMusic.isValueChanging()) {
                if (duration != null) {
                    mediaPlayer.seek(duration.multiply(progressMusic.getValue() / 100.0));
                }
                updateValues(testoDurata);
            }
        });


        HBox boxSliderSong = WindowsManager.createHBox(2, 0, Pos.CENTER, testoDurata, progressMusic);

        //box del nome della canzone e del boxSliderSong
        songLbl = new Label("");
        VBox sliderSong = WindowsManager.createVBox(3, 1, Pos.CENTER, songLbl, boxSliderSong);

        //tutto il box sopra della gui
        HBox musicBox = WindowsManager.createHBox(5, 3, Pos.CENTER, null, lyricsBtn, playPauseButton, backwardButton, sliderSong, forwardButton, shuffleBtn, sliderVolumeBox);
        musicBox.setPadding(new Insets(3, 3, 3, 10));
        HBox.setHgrow(progressMusic, Priority.ALWAYS);
        HBox.setHgrow(sliderSong, Priority.ALWAYS);
        HBox.setHgrow(boxSliderSong, Priority.ALWAYS);

        Image reduceImg = new Image(getClass().getResourceAsStream("/reduce.png"));
        Button reduceBtn = WindowsManager.createButton(null, 40, 20, reduceImg, false);
        reduceBtn.setId("bottoni");
        reduceBtn.setOnAction(e -> stage.setIconified(true));

        Image maximizeImg = new Image(getClass().getResourceAsStream("/maximize.png"));
        Image minimizeImg = new Image(getClass().getResourceAsStream("/minimize.png"));

        Button maximizeBtn = WindowsManager.createButton(null, 40, 20, maximizeImg, false);
        maximizeBtn.setId("bottoni");
        maximizeBtn.setOnAction(e -> {
            if (maximizedMinimized.equals("minimized")) {
                stage.setMaximized(true);
                maximizeBtn.setGraphic(new ImageView(minimizeImg));
                maximizedMinimized = "maximized";
            } else {
                stage.setMaximized(false);
                maximizeBtn.setGraphic(new ImageView(maximizeImg));
                maximizedMinimized = "minimized";
            }

        });

        Image closeImg = new Image(getClass().getResourceAsStream("/close.png"));
        Button closeBtn = WindowsManager.createButton(null, 40, 20, closeImg, false);
        closeBtn.setId("closeBtn");
        closeBtn.setOnAction(e -> {
            try {
                Map<Playlist, List<Brano>> listPlaylist = new HashMap<>();
                for (TreeItem<String> treeItem : mappaPlaylist.keySet())
                    listPlaylist.put(mappaPlaylist.get(treeItem), mappaPlaylist.get(treeItem).getListaBrani());
                WindowsManager.salvaArchivio(listPlaylist);
            } catch (IOException e2) {
            }
            Platform.exit();
        });

        Pane pane1 = new Pane();
        HBox.setHgrow(pane1, Priority.ALWAYS);
        Image spotifyImg = new Image(getClass().getResourceAsStream("/icona.png"));
        Pane pane2 = new Pane();
        HBox.setHgrow(pane2, Priority.ALWAYS);
        HBox closeMusicBox = WindowsManager.createHBox(5, 3, Pos.CENTER, null, new ImageView(spotifyImg), pane1, new Label("Windows Cali' Player"), pane2, reduceBtn, maximizeBtn, closeBtn);
        closeMusicBox.setPadding(new Insets(3, 5, 0, 10));
        class Delta {
            double x, y;
        }
        final Delta dragDelta = new Delta();
        closeMusicBox.setOnMousePressed(mouseEvent -> {
            dragDelta.x = stage.getX() - mouseEvent.getScreenX();
            dragDelta.y = stage.getY() - mouseEvent.getScreenY();
        });
        closeMusicBox.setOnMouseDragged(mouseEvent -> {
            stage.setX(mouseEvent.getScreenX() + dragDelta.x);
            stage.setY(mouseEvent.getScreenY() + dragDelta.y);
        });

        return new VBox(closeMusicBox, musicBox);
    }

    //riproduce la canzone giusta
    private void playMedia() {
        if (mediaCorrente != null) {
            if (mediaPlayer != null) {
                //se ho messo pausa e poi play su canzoni diverse allora setto il media
                if (!mediaPlayer.getMedia().getSource().equals(mediaCorrente.getSource())) {
                    mediaPlayer.stop();
                    mediaPlayer = new MediaPlayer(mediaCorrente);
                    mediaPlayer.play();
                    setCanzone();

                }
                //se invece ho messo pausa e play sulla stessa canzone riprendo la canzone da dove l'ho stoppata
                else {
                    mediaPlayer.play();
                    setCanzone();
                }
            }
            //se il mediaplayer è null è la prima volta che spingo play quindi setto il media
            else {
                mediaPlayer = new MediaPlayer(mediaCorrente);
                mediaPlayer.play();
                setCanzone();
            }

            //quando sto riproducendo la musica aggiorno lo slider
            mediaPlayer.setOnPlaying(() ->
                    mediaPlayer.currentTimeProperty().addListener(ov -> {
                        duration = mediaPlayer.getMedia().getDuration();
                        updateValues(testoDurata);
                    })
            );

            //modificando lo slider del volume, cambia effettivament eil volume
            mediaPlayer.volumeProperty().bindBidirectional(volumeSlider.valueProperty());

            //alla fine di ogni canzone riproduco quella successiva sia nel caso in cui la voglio in ordine, che nel caso in cui la voglio casuale
            mediaPlayer.setOnEndOfMedia(() -> {
                if (shuffleBtn.isSelected()) {
                    if (listaIndiciVisitati.size() != listaBrani.size()) {
                        randomIndex();
                        listaIndiciVisitati.add(i);
                    } else {
                        listaIndiciVisitati.clear();
                        randomIndex();
                        listaIndiciVisitati.add(i);
                    }
                } else i++;

                testoDurata.setText("00:00/00:00");
                progressMusic.setValue(0);
                tableSongs.getSelectionModel().select(i);

                try {
                    setMedia();
                    playMedia();
                } catch (IndexOutOfBoundsException ee) {
                    i = 0;
                    tableSongs.getSelectionModel().select(i);
                    setMedia();
                    playMedia();
                }
            });
        }
    }

    private void randomIndex() {
        Random r = new Random();
        int random = i;
        while (random == i) {
            random = r.nextInt(listaBrani.size());
            while (listaIndiciVisitati.contains(random))
                random = r.nextInt(listaBrani.size());
        }
        i = random;
    }

    //setta la label della canzone che si sta ascoltando
    private void setCanzone() {
        String[] arraySplitted = mediaCorrente.getSource().split("/");
        String titolo = arraySplitted[arraySplitted.length - 1].replace("%20", " ");
        String titoloFinale = titolo.substring(0, titolo.length() - 4);
        songLbl.setText(titoloFinale);
    }

    //aggiorna lo slider del progressMusic
    private void updateValues(Label time) {
        if (time != null) {
            runLater(() -> {
                Duration currentTime = mediaPlayer.getCurrentTime();
                time.setText(formatTime(currentTime, duration));

                if (!progressMusic.isDisabled()
                        && duration.greaterThan(Duration.ZERO)
                        && !progressMusic.isValueChanging()) {
                    progressMusic.setValue(currentTime.divide(duration).toMillis()
                            * 100.0);
                }
            });
        }
    }

    //formatta la stringa del tempo, in modo "00:00/00:00"
    private static String formatTime(Duration elapsed, Duration duration) {
        int intElapsed = (int) floor(elapsed.toSeconds());
        int elapsedHours = intElapsed / (60 * 60);
        if (elapsedHours > 0) {
            intElapsed -= elapsedHours * 60 * 60;
        }
        int elapsedMinutes = intElapsed / 60;
        int elapsedSeconds = intElapsed - elapsedHours * 60 * 60
                - elapsedMinutes * 60;

        if (duration.greaterThan(Duration.ZERO)) {
            int intDuration = (int) floor(duration.toSeconds());
            int durationHours = intDuration / (60 * 60);
            if (durationHours > 0) {
                intDuration -= durationHours * 60 * 60;
            }
            int durationMinutes = intDuration / 60;
            int durationSeconds = intDuration - durationHours * 60 * 60
                    - durationMinutes * 60;
            if (durationHours > 0) {
                return format("%d:%02d:%02d/%d:%02d:%02d",
                        elapsedHours, elapsedMinutes, elapsedSeconds,
                        durationHours, durationMinutes, durationSeconds);
            } else {
                return format("%02d:%02d/%02d:%02d",
                        elapsedMinutes, elapsedSeconds, durationMinutes,
                        durationSeconds);
            }
        } else {
            if (elapsedHours > 0) {
                return format("%d:%02d:%02d", elapsedHours,
                        elapsedMinutes, elapsedSeconds);
            } else {
                return format("%02d:%02d", elapsedMinutes,
                        elapsedSeconds);
            }
        }
    }

    //setta il media in indice i
    private void setMedia() {
        tableRow = (TableRow) tableSongs.getItems().get(i);
        mediaCorrente = new Media(new File(tableRow.getPercorso()).toURI().toString());
    }


    private HBox treeTable() {
        tableSongs = new TableView();
        sp = new SplitPane(tableSongs);
        sp.setOrientation(Orientation.VERTICAL);

        //alla pressione su una riga della tabella setta il media selezionato
        tableSongs.setRowFactory(tv -> {
            javafx.scene.control.TableRow row = new javafx.scene.control.TableRow();
            row.setOnMouseClicked(event -> {
                try {
                    i = tableSongs.getSelectionModel().getSelectedIndex();
                    setMedia();
                } catch (NullPointerException | IndexOutOfBoundsException ex) {
                }

                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    playMedia();
                    if (mediaPlayer != null) {
                        playPauseImg = new Image(getClass().getResourceAsStream("/pause.png"));
                        playPause = "play";
                    }

                    playPauseButton.setGraphic(new ImageView(playPauseImg));
                }
            });

            return row;
        });

        playlistTreeItem = new TreeItem<>("Playlist");
        playlistTreeItem.setExpanded(true);
        playList = new TreeView<>(playlistTreeItem);
        playList.setEditable(true);
        playList.setCellFactory(TextFieldTreeCell.forTreeView());
        playList.setOnEditCommit(event -> {
            if (playlistCorrente != null) {
                mappaPlaylist.get(playlistCorrente).setNome(event.getNewValue());
            }
        });

        playList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {

            @Override
            public void changed(ObservableValue observable, Object oldValue,
                                Object newValue) {
                playlistCorrente = (TreeItem<String>) newValue;
                Playlist playlist = mappaPlaylist.get(playlistCorrente);
                if (playlist != null) {
                    listaBrani.clear();
                    codice = 1;
                    for (Brano b : playlist.getListaBrani())
                        addRow(b.getNome(), b.getDurata(), b.getPercorso(), b);
                }
            }
        });


        tableSongs.prefWidthProperty().bind(stage.widthProperty());

        TableColumn delete = new TableColumn("  ");
        delete.setCellValueFactory(
                new PropertyValueFactory<TableRow, String>("delete"));
        delete.prefWidthProperty().bind(tableSongs.widthProperty().multiply(0.05));

        TableColumn codice = new TableColumn("N.");
        codice.setCellValueFactory(
                new PropertyValueFactory<TableRow, String>("codice"));
        codice.prefWidthProperty().bind(tableSongs.widthProperty().multiply(0.05));

        TableColumn titolo = new TableColumn("Titolo");
        titolo.setCellValueFactory(
                new PropertyValueFactory<TableRow, String>("titolo"));
        titolo.prefWidthProperty().bind(tableSongs.widthProperty().multiply(0.3));

        TableColumn durata = new TableColumn("Durata");
        durata.setCellValueFactory(
                new PropertyValueFactory<TableRow, String>("durata"));
        durata.prefWidthProperty().bind(tableSongs.widthProperty().multiply(0.1));

        TableColumn percorso = new TableColumn("Percorso");
        percorso.setCellValueFactory(
                new PropertyValueFactory<TableRow, String>("percorso"));
        percorso.prefWidthProperty().bind(tableSongs.widthProperty().multiply(0.50));



        tableSongs.getColumns().addAll(delete, codice, titolo, durata, percorso);

        //drag and drop dei file
        tableSongs.setOnDragOver(event -> {
            Dragboard db = event.getDragboard();
            if (db.hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
            } else {
                event.consume();
            }
        });

        //quando viene rilasciato un file lo aggiunge nella tabella con i rispettivi dati
        tableSongs.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles()) {
                success = true;
                String estenzione = null;
                for (File file : db.getFiles()) {
                    final String name = file.getName();
                    final String filePath = file.getAbsolutePath();
                    estenzione = name.substring(name.length() - 3);
                    Media media = new Media(new File(filePath).toURI().toString());
                    MediaPlayer mp = new MediaPlayer(media);
                    mp.setOnReady(() -> {
                        durationMedia = "" + Math.round(media.getDuration().toMinutes() * 100.0) / 100.0;
                        Brano brano = new Brano(name, filePath, durationMedia);
                        Playlist playlist = mappaPlaylist.get(playlistCorrente);
                        playlist.aggiungiBrano(brano);
                        addRow(name, durationMedia, filePath, brano);
                    });




                }
            }
            event.setDropCompleted(success);
            event.consume();
        });
        tableSongs.setItems(listaBrani);
        if (!listaBrani.isEmpty()) tableSongs.getSelectionModel().select(0);

        //crea la parte centrale
        VBox toolTree = WindowsManager.createVBox(0, 0, null, null, playList);
        VBox.setVgrow(playList, Priority.ALWAYS);
        toolTree.prefWidthProperty().bind(stage.widthProperty().multiply(0.4));
        HBox treeTableBox = WindowsManager.createHBox(0, 0, null, null, toolTree, sp);
        return treeTableBox;
    }

    //quando si apre il programma vengono fillati i campi delle playlist salvate e dei brani salvati
    Path songPath = Paths.get("E:\\01 - Daniele\\Musica\\");
    private void fillaCampi() {

        try {
            Map<Playlist, List<Brano>> playlistMap = WindowsManager.apriArchivio();
            Iterator iterator = playlistMap.keySet().iterator();
            while (iterator.hasNext()) {
                Playlist playlist = (Playlist) iterator.next();
                createPlaylist(playlist.getNome(), playlistMap.get(playlist));
            }

            playList.getSelectionModel().select(1);
            tableSongs.getSelectionModel().select(0);


        } catch (IOException | ClassNotFoundException e) {
        }
    }

    //aggiunge una riga alla tabella
    private void addRow(String titolo, String duration, String percorso, Brano b) {
        boolean presente = false;
        for (TableRow tr : listaBrani) {
            if (tr.getTitolo().equals(titolo))
                presente = true;
        }
        if (!presente) {
            Button delete = new Button("X");
            delete.setId("delete");


            TableRow t = new TableRow(delete, codice, titolo, duration, percorso);
            delete.setOnAction(e -> {
                mappaPlaylist.get(playlistCorrente).getListaBrani().remove(b);
                listaBrani.remove(t);
            });
            listaBrani.add(t);
            codice++;
        }
    }

    //barra di ricerca con aggiungi e rimuovi playlist
    private HBox barraRicerca() {
        //bottoni
        Image addImg = new Image(getClass().getResourceAsStream("/add.png"));
        Button addPlaylistBtn = WindowsManager.createButton(null, 50, 20, addImg, false);
        addPlaylistBtn.setOnAction(e -> createPlaylist("Senza Nome", null));

        Image removeImg = new Image(getClass().getResourceAsStream("/remove.png"));
        Button removePlaylistBtn = WindowsManager.createButton(null, 50, 20, removeImg, false);
        removePlaylistBtn.setOnAction(e -> removePlaylist());

        TextField ricercaTxt = WindowsManager.createTextField(18, "Cerca", true);
        ricercaTxt.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.equals("")) tableSongs.setItems(listaBrani);
            else {
                ObservableList<TableRow> obs = FXCollections.observableArrayList();
                new Thread(() -> {
                    for (TableRow s : listaBrani) {
                        if (s.getTitolo().toLowerCase().contains(newValue.toLowerCase()))
                            obs.add(s);
                    }
                }).start();
                tableSongs.setItems(obs);
            }
        });

        Pane pane = new Pane();
        HBox.setHgrow(pane, Priority.ALWAYS);

        HBox barraRicercaBox = WindowsManager.createHBox(10, 5, Pos.CENTER, null, addPlaylistBtn, removePlaylistBtn, pane, ricercaTxt);
        return barraRicercaBox;
    }

    //crea una playlist
    private void createPlaylist(String nome, List<Brano> listaBrani) {
        Playlist itemPlayList = new Playlist(nome, listaBrani);
        TreeItem<String> treeItem = new TreeItem<>(nome);
        mappaPlaylist.put(treeItem, itemPlayList);
        playlistTreeItem.getChildren().add(treeItem);
        playList.getSelectionModel().select(treeItem);
        playlistCorrente = treeItem;
    }

    //rimuove una playlist con tutti i suoi dati
    private void removePlaylist() {
        TreeItem<String> itemSelected = playList.getSelectionModel().getSelectedItem();
        if (itemSelected != null) {
            mappaPlaylist.remove(itemSelected);
            playlistTreeItem.getChildren().remove(itemSelected);
            TreeItem<String> newSelected = playList.getSelectionModel().getSelectedItem();
            try {
                List<Brano> playlist = mappaPlaylist.get(newSelected).getListaBrani();
                for (Brano brano : playlist)
                    addRow(brano.getNome(), brano.getDurata(), brano.getPercorso(), brano);
            } catch (NullPointerException e) {
                listaBrani.clear();
            }
        }
    }

}

