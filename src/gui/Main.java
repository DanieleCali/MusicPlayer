package gui;


import elementi.*;
import elementi.TableRow;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTreeCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import util.WindowsManager;
import java.io.File;
import java.io.IOException;
import java.util.*;

import static java.lang.Math.floor;
import static java.lang.Math.round;
import static java.lang.String.format;
import static javafx.application.Platform.runLater;

public class Main extends Application {
    private Stage stage;
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
    private Text testoDurata = new Text("00:00/00:00");
    private static int i = -1;
    private TableRow tableRow;
    private String playPause = "pause";
    private static Image playPauseImg;
    private static Button playPauseButton;
    private static ToggleButton shuffleBtn;
    private String durationMedia = "0:00";

    @Override
    public void start(Stage primaryStage) throws Exception{
        stage = primaryStage;
        primaryStage.setTitle("Cali' Player");
        primaryStage.setScene(new Scene(window(), 900, 600));
        primaryStage.show();
        stage.setOnCloseRequest(we -> {
            try {
                Map<Playlist, List<Brano>> listPlaylist = new HashMap<>();
                for(TreeItem<String> treeItem: mappaPlaylist.keySet())
                    listPlaylist.put(mappaPlaylist.get(treeItem), mappaPlaylist.get(treeItem).getListaBrani());
                WindowsManager.salvaArchivio(listPlaylist);
            } catch (IOException e) {}
        });
        fillaCampi();
    }


    public static void main(String[] args) {
        launch(args);
    }

    //crea tutta la finestra
    private Parent window(){
        BorderPane mainBP = new BorderPane();
        mainBP.setTop(controlMusicBox());
        mainBP.setCenter(treeTable());
        mainBP.setBottom(barraRicerca());
        return mainBP;
    }


    // crea il box per il controllo della musica
    private HBox controlMusicBox(){
        //bottoni gui con listener
        playPauseImg = new Image(getClass().getResourceAsStream("/play.png"));
        playPauseButton = WindowsManager.createButton(null, BUTTON_WIDTH, BUTTON_HEIGHT, playPauseImg, false);
        playPauseButton.setOnAction(e -> {
            if(playPause.equals("pause")) {
                playMedia();
                if (mediaPlayer != null) {
                    playPauseImg = new Image(getClass().getResourceAsStream("/pause.png"));
                    playPause = "play";
                }
            }
            else if(mediaPlayer != null) {
                    mediaPlayer.pause();
                    playPauseImg = new Image(getClass().getResourceAsStream("/play.png"));
                    playPause = "pause";
                }
            playPauseButton.setGraphic(new ImageView(playPauseImg));
        });

        Image backwardImg = new Image(getClass().getResourceAsStream("/backward.png"));
        Button backwardButton = WindowsManager.createButton(null, BUTTON_WIDTH, BUTTON_HEIGHT, backwardImg, false);
        backwardButton.setOnAction(e -> {
            if(mediaPlayer != null) {
                i--;
                if (i < 0) i = listaBrani.size() - 1;
                setMedia();
                playMedia();
                tableSongs.getSelectionModel().select(i);
            }
        });

        Image forwardImg = new Image(getClass().getResourceAsStream("/forward.png"));
        Button forwardButton = WindowsManager.createButton(null, BUTTON_WIDTH, BUTTON_HEIGHT, forwardImg, false);
        forwardButton.setOnAction(e ->  {if (mediaPlayer != null) mediaPlayer.seek(mediaPlayer.getTotalDuration());});

        Image shuffleImg = new Image(getClass().getResourceAsStream("/shuffle.png"));
        shuffleBtn = new ToggleButton(null, new ImageView(shuffleImg));
        shuffleBtn.setPrefSize(BUTTON_WIDTH, BUTTON_HEIGHT);

        //box del controllo del volume
        Text testoVolume = new Text("Volume:");
        volumeSlider = new Slider(0, 1, 0.5);
        volumeSlider.setOrientation(Orientation.VERTICAL);
        volumeSlider.setPrefHeight(50);
        VBox sliderVolumeBox = WindowsManager.createVBox(0, 2, Pos.CENTER, testoVolume, volumeSlider);

        //box della durata e lo slider della canzone
        progressMusic = new Slider();
        progressMusic.valueProperty().addListener(ov -> {
            if (progressMusic.isValueChanging()) {
                if(duration!=null) {
                    mediaPlayer.seek(duration.multiply(progressMusic.getValue() / 100.0));
                }
                updateValues(testoDurata);
            }
        });
        HBox boxSliderSong = WindowsManager.createHBox(2,0,Pos.CENTER,testoDurata, progressMusic);

        //box del nome della canzone e del boxSliderSong
        Text songLbl = new Text("Canzone che sto ascoltando");
        VBox sliderSong = WindowsManager.createVBox(3, 1, Pos.CENTER, songLbl, boxSliderSong);

        //tutto il box sopra della gui
        HBox musicBox = WindowsManager.createHBox(5, 3, Pos.CENTER, null, playPauseButton, backwardButton, sliderSong, forwardButton, shuffleBtn, sliderVolumeBox);
        HBox.setHgrow(progressMusic, Priority.ALWAYS);
        HBox.setHgrow(sliderSong , Priority.ALWAYS);
        HBox.setHgrow(boxSliderSong, Priority.ALWAYS);

        return musicBox;
    }

    //riproduce la canzone giusta
    private void playMedia(){
        if(mediaCorrente != null) {
            if (mediaPlayer != null) {
                //se ho messo pausa e poi play su canzoni diverse allora setto il media
                if (!mediaPlayer.getMedia().getSource().equals(mediaCorrente.getSource())) {
                    mediaPlayer.stop();
                    mediaPlayer = new MediaPlayer(mediaCorrente);
                    mediaPlayer.play();
                }
                //se invece ho messo pausa e play sulla stessa canzone riprendo la canzone da dove l'ho stoppata
                else mediaPlayer.play();
            }
            //se il mediaplayer è null è la prima volta che spingo play quindi setto il media
            else{
                mediaPlayer = new MediaPlayer(mediaCorrente);
                mediaPlayer.play();
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
                //TODO:il casuale non è fatto benissimo, bisogna ritoccarlo
                if(shuffleBtn.isSelected()){
                    Random r = new Random();
                    int random = i;
                    while(random == i)
                        random = r.nextInt(listaBrani.size());
                    i = random;
                }
                else i++;

                testoDurata.setText("00:00/00:00");
                progressMusic.setValue(0);
                tableSongs.getSelectionModel().select(i);

                try {
                    setMedia();
                    playMedia();
                }
                catch (IndexOutOfBoundsException ee){
                    i = 0;
                    tableSongs.getSelectionModel().select(i);
                    setMedia();
                    playMedia();
                }
            });
        }
    }

    //aggiorna lo slider del progressMusic
    private void updateValues(Text time) {
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
    private void setMedia(){
        tableRow = (TableRow) tableSongs.getItems().get(i);
        mediaCorrente = new Media(new File(tableRow.getPercorso()).toURI().toString());
    }


    private HBox treeTable(){
        tableSongs = new TableView();
        //alla pressione su una riga della tabella setta il media selezionato
        tableSongs.setRowFactory( tv -> {
            javafx.scene.control.TableRow row = new javafx.scene.control.TableRow();
            row.setOnMouseClicked(event -> {
                try {
                    i = tableSongs.getSelectionModel().getSelectedIndex();
                    setMedia();
                }catch (NullPointerException | IndexOutOfBoundsException ex){}

                if (event.getClickCount() == 2 && (! row.isEmpty()) ) {
                    playMedia();
                    if (mediaPlayer != null) {
                        playPauseImg = new Image(getClass().getResourceAsStream("/pause.png"));
                        playPause = "play";
                    }

                    playPauseButton.setGraphic(new ImageView(playPauseImg));
                }
            });
            return row ;
        });

        playlistTreeItem = new TreeItem<>("Playlist");
        playlistTreeItem.setExpanded(true);
        playList = new TreeView<>(playlistTreeItem);
        playList.setEditable(true);
        playList.setCellFactory(TextFieldTreeCell.forTreeView());
        playList.setOnEditCommit(event -> {
            if (playlistCorrente != null){
                mappaPlaylist.get(playlistCorrente).setNome(event.getNewValue());
            }
        });

        playList.getSelectionModel().selectedItemProperty().addListener( new ChangeListener() {

            @Override
            public void changed(ObservableValue observable, Object oldValue,
                                Object newValue) {
                playlistCorrente = (TreeItem<String>) newValue;
                Playlist playlist = mappaPlaylist.get(playlistCorrente);
                if (playlist != null) {
                    listaBrani.clear();
                    for (Brano b : playlist.getListaBrani())
                        addRow(b.getNome(), b.getDurata(), b.getPercorso());
                }
            }
        });

        tableSongs.prefWidthProperty().bind(stage.widthProperty());

        TableColumn titolo = new TableColumn("Titolo");
        titolo.setCellValueFactory(
                new PropertyValueFactory<TableRow, String>("titolo"));
        titolo.prefWidthProperty().bind(tableSongs.widthProperty().multiply(0.4));

        TableColumn durata = new TableColumn("Durata");
        durata.setCellValueFactory(
                new PropertyValueFactory<TableRow, String>("durata"));
        durata.prefWidthProperty().bind(tableSongs.widthProperty().multiply(0.1));

        TableColumn percorso = new TableColumn("Percorso");
        percorso.setCellValueFactory(
                new PropertyValueFactory<TableRow, String>("percorso"));
        percorso.prefWidthProperty().bind(tableSongs.widthProperty().multiply(0.49));
        tableSongs.getColumns().addAll(titolo, durata, percorso);

        Text playlistLbl = new Text("Playlist");
        ToolBar toolBar = new ToolBar(playlistLbl);

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
                for (File file:db.getFiles()) {
                    final String name = file.getName();
                    final String filePath = file.getAbsolutePath();
                    estenzione = name.substring(name.length() - 3);
                    if(estenzione.equals("mp3") || estenzione.equals("m4a") || estenzione.equals("mp4") || estenzione.equals("wav") || estenzione.equals("flv")){
                        Media media = new Media(new File(filePath).toURI().toString());
                        MediaPlayer mp = new MediaPlayer(media);
                        mp.setOnReady(() ->  {
                            durationMedia = ""+ Math.round(media.getDuration().toMinutes()*100.0) / 100.0;
                            Brano brano = new Brano(name, filePath, durationMedia);
                            Playlist playlist = mappaPlaylist.get(playlistCorrente);
                            playlist.aggiungiBrano(brano);
                            addRow(name,durationMedia, filePath);
                        });

                    }
                }
            }
            event.setDropCompleted(success);
            event.consume();
        });
        tableSongs.setItems(listaBrani);


        //crea la parte centrale
        VBox toolTree = WindowsManager.createVBox(0, 0, null, null, toolBar, playList);
        VBox.setVgrow(playList, Priority.ALWAYS);
        toolTree.prefWidthProperty().bind(stage.widthProperty().multiply(0.4));
        HBox treeTableBox = WindowsManager.createHBox(0, 0, null, null, toolTree, tableSongs);
        return treeTableBox;
    }

    //quando si apre il programma vengono fillati i campi delle playlist salvate e dei brani salvati
    private void fillaCampi(){
        try {
            Map<Playlist, List<Brano>> playlistMap = WindowsManager.apriArchivio();
            Iterator iterator = playlistMap.keySet().iterator();
            while(iterator.hasNext()) {
                Playlist playlist = (Playlist) iterator.next();
                createPlaylist(playlist.getNome(), playlistMap.get(playlist));
            }

            playList.getSelectionModel().select(1);

        } catch (IOException | ClassNotFoundException e) {}
    }

    //aggiunge una riga alla tabella
    private void addRow(String titolo, String duration, String percorso){
        TableRow t = new TableRow(titolo, duration, percorso);
        listaBrani.add(t);
    }

    //barra di ricerca con aggiungi e rimuovi playlist
    private HBox barraRicerca(){
        //bottoni
        Image addImg = new Image(getClass().getResourceAsStream("/add.png"));
        Button addPlaylistBtn = WindowsManager.createButton(null, 50, 20, addImg, false);
        addPlaylistBtn.setOnAction(e -> createPlaylist("Senza Nome", null));

        Image removeImg = new Image(getClass().getResourceAsStream("/remove.png"));
        Button removePlaylistBtn = WindowsManager.createButton(null, 50, 20, removeImg, false);
        removePlaylistBtn.setOnAction(e -> removePlaylist());

        TextField ricercaTxt = WindowsManager.createTextField(18, "inserisci il titolo di un brano da ricercare", true);

        Pane pane = new Pane();
        HBox.setHgrow(pane, Priority.ALWAYS);

        HBox barraRicercaBox = WindowsManager.createHBox(10, 5, Pos.CENTER, null, addPlaylistBtn, removePlaylistBtn, pane, ricercaTxt);
        return barraRicercaBox;
    }

    //crea una playlist
    private void createPlaylist(String nome, List<Brano> listaBrani){
        Playlist itemPlayList = new Playlist(nome, listaBrani);
        TreeItem<String> treeItem = new TreeItem<>(nome);
        mappaPlaylist.put(treeItem, itemPlayList);
        playlistTreeItem.getChildren().add(treeItem);
        playList.getSelectionModel().select(treeItem);
        playlistCorrente = treeItem;
    }

    //rimuove una playlist con tutti i suoi dati
    private void removePlaylist(){
        TreeItem<String> itemSelected =  playList.getSelectionModel().getSelectedItem();
        if(itemSelected != null) {
            mappaPlaylist.remove(itemSelected);
            playlistTreeItem.getChildren().remove(itemSelected);
            TreeItem<String> newSelected = playList.getSelectionModel().getSelectedItem();
            try {
                List<Brano> playlist = mappaPlaylist.get(newSelected).getListaBrani();
                for (Brano brano : playlist)
                    addRow(brano.getNome(), brano.getDurata(), brano.getPercorso());
            }catch (NullPointerException e){
                listaBrani.clear();
            }
        }
    }
}
