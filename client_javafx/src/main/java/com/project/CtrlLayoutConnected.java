package com.project;

import java.util.ArrayList;
import java.util.List;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;

public class CtrlLayoutConnected {

    @FXML
    private Label serverAddressLabel;

    @FXML
    private Label clientIdLabel;

    @FXML
    private TextArea messagesArea;

    @FXML
    private Label puntuacion2, puntuacion1, labelMiTurno, labelRivalTurno;

    @FXML
    private Text jugador1;

    @FXML
    private Text jugador2;

    @FXML
    private ListView<String> clientsList;

    @FXML
    private TextField messageField;

    @FXML
    private Button sendButton, start;

    @FXML
    private ImageView imagen1, imagen2, imagen3, imagen4, imagen5, imagen6, imagen7, imagen8, imagen9, imagen10,
            imagen11, imagen12,
            imagen13, imagen14, imagen15, imagen16;

    @FXML
    private static List<ImageView> imageViews = new ArrayList<>();

    AppData infoData = AppData.getInstance();

    static Image imagenInicial = new Image("/assets/imagen_inicial.jpg");

    static Image rojo = new Image("/assets/rojo.png");
    static Image negro = new Image("/assets/negro.png");
    static Image amarillo = new Image("/assets/amarillo.png");
    static Image gris = new Image("/assets/gris.png");
    static Image naranja = new Image("/assets/naranja.png");
    static Image rosa = new Image("/assets/rosa.png");
    static Image verde = new Image("/assets/verde.png");
    static Image azul = new Image("/assets/azul.png");

    // rojo,negro,amarillo,blanco,gris,naranja,rosa,verde

    // List<String> board_colors = new ArrayList<>((Arrays.asList("rojo", "negro",
    // "amarillo", "azul", "gris", "naranja", "rosa", "verde","rojo", "negro",
    // "amarillo", "azul", "gris", "naranja", "rosa", "verde")));

    public void initialize() {

        puntuacion2.setText("0");

        puntuacion1.setText("0");

        imageViews.add(imagen1);
        imageViews.add(imagen2);
        imageViews.add(imagen3);
        imageViews.add(imagen4);
        imageViews.add(imagen5);
        imageViews.add(imagen6);
        imageViews.add(imagen7);
        imageViews.add(imagen8);
        imageViews.add(imagen9);
        imageViews.add(imagen10);
        imageViews.add(imagen11);
        imageViews.add(imagen12);
        imageViews.add(imagen13);
        imageViews.add(imagen14);
        imageViews.add(imagen15);
        imageViews.add(imagen16);
        setAppDataReference(AppData.getInstance());

    }

    public void setAppDataReference(AppData appData) {
        this.infoData = appData;
        appData.setLayoutConnected(this);
    }

    public void actualizarLabelTurno(String punt1, String punt2) {
        puntuacion1.setText(punt1);
        puntuacion2.setText(punt2);
        if (infoData.tuTurno) {
            labelMiTurno.setVisible(true);
            labelRivalTurno.setVisible(false);
        } else {
            labelRivalTurno.setVisible(true);
            labelMiTurno.setVisible(false);
        }
    }

    public void actualizarTurno() {
        if (infoData.tuTurno) {
            labelMiTurno.setVisible(true);
            labelRivalTurno.setVisible(false);
        } else {
            labelRivalTurno.setVisible(true);
            labelMiTurno.setVisible(false);
        }
    }

    public void actualizarNombresyPuntuacion(String jug1, String punt1, String jug2, String punt2) {
        jugador1.setText(jug1);
        jugador2.setText(jug2);
        puntuacion1.setText(punt1);
        puntuacion2.setText(punt2);
    }

    @FXML
    public void imgpressed(MouseEvent event) {
        if (infoData.tuTurno) {
            ImageView sourceimagen = (ImageView) event.getSource();
            int imageIndex = imageViews.indexOf(sourceimagen);
            if (sourceimagen.getImage().getUrl().equals(imagenInicial.getUrl())) {
                infoData.MessegeBoard(imageIndex);
            }
        }
    }

    @FXML
    private void handleDisconnect(ActionEvent event) {
        AppData appData = AppData.getInstance();
        appData.disconnectFromServer();
    }

    public void actualizarBoard(List<String> nuevoBoard) {
        for (int i = 0; i < imageViews.size() && i < nuevoBoard.size(); i++) {
            ImageView imageView = imageViews.get(i);
            String color = nuevoBoard.get(i);
            Image nuevaImagen;
            if (color.equals("-")) {
                nuevaImagen = imagenInicial;
            } else {
                switch (color) {
                    case "rojo":
                        nuevaImagen = rojo;
                        break;
                    case "negro":
                        nuevaImagen = negro;
                        break;
                    case "amarillo":
                        nuevaImagen = amarillo;
                        break;
                    case "azul":
                        nuevaImagen = azul;
                        break;
                    case "gris":
                        nuevaImagen = gris;
                        break;
                    case "naranja":
                        nuevaImagen = naranja;
                        break;
                    case "rosa":
                        nuevaImagen = rosa;
                        break;
                    case "verde":
                        nuevaImagen = verde;
                        break;
                    default:
                        nuevaImagen = imagenInicial;
                }
            }

            imageView.setImage(nuevaImagen);
        }

    }
}
