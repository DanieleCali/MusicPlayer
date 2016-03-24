package gui;

import com.sun.javafx.font.freetype.HBGlyphLayout;
import com.sun.javafx.scene.control.skin.VirtualFlow;
import elementi.*;
import elementi.TableRow;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTreeCell;
import javafx.scene.image.Image;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import util.WindowsManager;

import java.io.File;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class Main extends Application {
    private Stage stage;
    private static final int BUTTON_WIDTH = 50;
    private static final int BUTTON_HEIGHT = 50;
    private ObservableList<TableRow> listaBrani = FXCollections.observableArrayList();
    private MediaPlayer mediaPlayer;
    private static TreeItem<String> playlistTreeItem;
    private static Map<TreeItem<String>, Playlist> mappaPlaylist = new HashMap<>();
    private TreeView<String> playList;
    private static TreeItem<String> playlistCorrente = null;
    private TableView tableSongs;

    @Override
    public void start(Stage primaryStage) throws Exception{
        stage = primaryStage;
        primaryStage.setTitle("Hello World");
        primaryStage.setScene(new Scene(window(), 900, 600));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }

    private Parent window(){
        BorderPane mainBP = new BorderPane();
        mainBP.setTop(controlMusicBox());
        mainBP.setCenter(treeTable());
        mainBP.setBottom(barraRicerca());
        return mainBP;
    }

    // crea il box per il controllo della musica
    private HBox controlMusicBox(){
        //bottoni gui
        Image playImg = new Image(getClass().getResourceAsStream("/play.png"));
        Button playButton = WindowsManager.createButton(null, BUTTON_WIDTH, BUTTON_HEIGHT, playImg, false);
        playButton.setOnAction(e -> {
            //TableRow tableRow = (TableRow) tableSongs.getSelectionModel().getSelectedItem();
            //Media media = new Media(new File(tableRow.getPercorso()).toURI().toString());
            //mediaPlayer = new MediaPlayer(media);
            //mediaPlayer.setAutoPlay(true);
            //mediaPlayer.setVolume(1);
            //mediaPlayer.play();
            //System.out.println(media.getDuration());
        });

        Image pauseImg = new Image(getClass().getResourceAsStream("/pause.png"));
        Button pauseButton = WindowsManager.createButton(null, BUTTON_WIDTH, BUTTON_HEIGHT, pauseImg, false);
        pauseButton.setOnAction(e -> mediaPlayer.pause());

        Image shuffleImg = new Image(getClass().getResourceAsStream("/shuffle.png"));
        Button shuffleButton = WindowsManager.createButton(null, BUTTON_WIDTH, BUTTON_HEIGHT, shuffleImg, false);

        Image repeatImg = new Image(getClass().getResourceAsStream("/repeat.png"));
        Button repeatButton = WindowsManager.createButton(null, BUTTON_WIDTH, BUTTON_HEIGHT, repeatImg, false);
        repeatButton.setOnAction(e -> mediaPlayer.seek(mediaPlayer.getStartTime()));

        //box del controllo del volume
        Text testoVolume = new Text("Volume:");
        Slider volumeSlider = new Slider();
        volumeSlider.setOrientation(Orientation.VERTICAL);
        volumeSlider.setPrefHeight(50);
        VBox sliderVolumeBox = WindowsManager.createVBox(0, 2, Pos.CENTER, testoVolume, volumeSlider);

        //box della durata e lo slider della canzone
        Slider progressMusic = new Slider();
        Text testoDurata = new Text("0:00");
        HBox boxSliderSong = WindowsManager.createHBox(2,0,Pos.CENTER,testoDurata, progressMusic);

        //box del nome della canzone e del boxSliderSong
        Text songLbl = new Text("Canzone che sto ascoltando");
        VBox sliderSong = WindowsManager.createVBox(3, 1, Pos.CENTER, songLbl, boxSliderSong);

        //tutto il box sopra della gui
        HBox musicBox = WindowsManager.createHBox(5, 3, Pos.CENTER, null, playButton, pauseButton, sliderSong, shuffleButton, repeatButton, sliderVolumeBox);
        HBox.setHgrow(progressMusic, Priority.ALWAYS);
        HBox.setHgrow(sliderSong , Priority.ALWAYS);
        HBox.setHgrow(boxSliderSong, Priority.ALWAYS);

        return musicBox;
    }


    private HBox treeTable(){
        Text playlistLbl = new Text("Playlist");
        tableSongs = new TableView();
        tableSongs.setRowFactory( tv -> {
            javafx.scene.control.TableRow row = new javafx.scene.control.TableRow();
            row.setOnMouseClicked(event -> {
                TableRow rowData = (TableRow) row.getItem();
                String percorso = rowData.getPercorso();

                Media media = new Media(new File(percorso).toURI().toString());
                mediaPlayer = new MediaPlayer(media);

                mediaPlayer.play();
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
                        addRow(b.getNome(), b.getPercorso());
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
        ToolBar toolBar = new ToolBar(playlistLbl);

        tableSongs.setOnDragOver(event -> {
            Dragboard db = event.getDragboard();
            if (db.hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
            } else {
                event.consume();
            }
        });

        tableSongs.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles()) {
                success = true;
                String filePath = null;
                String name = null;
                String estenzione = null;
                for (File file:db.getFiles()) {
                    name = file.getName();
                    filePath = file.getAbsolutePath();
                    estenzione = name.substring(name.length() - 3);
                    if(estenzione.equals("mp3")){
                        Brano brano = new Brano(name, filePath, "2:00");
                        Playlist playlist = mappaPlaylist.get(playlistCorrente);
                        playlist.aggiungiBrano(brano);
                        addRow(name, filePath);
                    }
                }
            }

            event.setDropCompleted(success);
            event.consume();
        });

        tableSongs.setItems(listaBrani);
        VBox toolTree = WindowsManager.createVBox(0, 0, null, null, toolBar, playList);
        VBox.setVgrow(playList, Priority.ALWAYS);
        toolTree.prefWidthProperty().bind(stage.widthProperty().multiply(0.4));
        HBox treeTableBox = WindowsManager.createHBox(0, 0, null, null, toolTree, tableSongs);
        return treeTableBox;
    }

    private void addRow(String titolo, String percorso){
        TableRow t = new TableRow(titolo, "2:00", percorso);
        listaBrani.add(t);
        Media media = new Media(new File(percorso).toURI().toString());
        MediaPlayer mediaPlayer = new MediaPlayer(media);
        mediaPlayer.setAutoPlay(true);
    }

    private HBox barraRicerca(){
        //bottoni
        Image addImg = new Image(getClass().getResourceAsStream("/add.png"));
        Button addPlaylistBtn = WindowsManager.createButton(null, 50, 20, addImg, false);
        addPlaylistBtn.setOnAction(e -> createPlaylist());

        Image removeImg = new Image(getClass().getResourceAsStream("/remove.png"));
        Button removePlaylistBtn = WindowsManager.createButton(null, 50, 20, removeImg, false);
        removePlaylistBtn.setOnAction(e -> removePlaylist());

        TextField ricercaTxt = WindowsManager.createTextField(18, "inserisci il titolo di un brano da ricercare", true);

        Pane pane = new Pane();
        HBox.setHgrow(pane, Priority.ALWAYS);

        HBox barraRicercaBox = WindowsManager.createHBox(10, 5, Pos.CENTER, null, addPlaylistBtn, removePlaylistBtn, pane, ricercaTxt);
        return barraRicercaBox;
    }

    private void createPlaylist(){
        Playlist itemPlayList = new Playlist("Senza Nome");
        TreeItem<String> treeItem = new TreeItem<>("Senza Nome");
        mappaPlaylist.put(treeItem, itemPlayList);
        playlistTreeItem.getChildren().add(treeItem);
    }

    private void removePlaylist(){
        TreeItem<String> itemSelected =  playList.getSelectionModel().getSelectedItem();
        if(itemSelected != null) {
            mappaPlaylist.remove(itemSelected);
            playlistTreeItem.getChildren().remove(itemSelected);
        }
    }
}
