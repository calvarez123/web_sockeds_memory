package com.project;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import java.util.Random;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
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
    private Label puntuacion2,puntuacion1,labelMiTurno,labelRivalTurno;

  

    @FXML
    private Text jugador1;

    @FXML
    private Text jugador2;

    @FXML
    private ListView<String> clientsList;

    @FXML
    private TextField messageField;

    @FXML
    private Button sendButton,start;

    @FXML
    private ImageView imagen1,imagen2,imagen3,imagen4,imagen5,imagen6,imagen7,imagen8,imagen9,imagen10,imagen11,imagen12,
    imagen13,imagen14,imagen15,imagen16;

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
    
    
    //List<String> board_colors = new ArrayList<>((Arrays.asList("rojo", "negro", "amarillo", "azul", "gris", "naranja", "rosa", "verde","rojo", "negro", "amarillo", "azul", "gris", "naranja", "rosa", "verde")));
    
    List<String> board_colors = infoData.getBoard_colors();


    Random random = new Random();

 


    public void initialize() {
        Collections.shuffle(board_colors);

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
        
        
        
        System.out.println(board_colors);
        clientsList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        clientsList.setOnMouseClicked(event -> {

            // Set new selection (or deselect)
            AppData appData = AppData.getInstance();
            int clickedIndex = clientsList.getSelectionModel().getSelectedIndex();
            appData.selectClient(clickedIndex); 

            // Get real selection (can be unset)
            Integer selectedIndex = appData.getSelectedClientIndex();
            if (selectedIndex != null) {
                sendButton.setText("Send");
            } else {
                sendButton.setText("Broadcast");
            }
            sendButton.requestFocus();

            // De-select all
            for (int i = 0; i < clientsList.getItems().size(); i++) {
                clientsList.getSelectionModel().clearSelection(i);
            }
            

            appData.updateClientList();
        });

        
    
        clientsList.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(item);
                    AppData appData = AppData.getInstance();
                    Integer selectedIndex = appData.getSelectedClientIndex();
                    if (selectedIndex != null && selectedIndex.intValue() == getIndex()) {
                        setStyle("-fx-background-color: #14b5ff; -fx-text-fill: white;");
                    } else {
                        setStyle(null);
                    }
                }
            }
        });

        
    }

    @FXML
    public void startBoton(ActionEvent event) {
        System.out.println(infoData.tuTurno);

         if(infoData.tuTurno==true){
            labelMiTurno.setVisible(true);
            labelRivalTurno.setVisible(false);
        }else if (infoData.tuTurno!=true) {
            labelRivalTurno.setVisible(true);
            labelMiTurno.setVisible(false);
        }
        start.setVisible(false);
    }

    int tiradas = 0;
    
    @FXML
    public void imgpressed(MouseEvent event) {
        
        
        if(infoData.tuTurno==true){
            labelMiTurno.setVisible(true);
            labelRivalTurno.setVisible(false);
        }else if (infoData.tuTurno!=true) {
            labelRivalTurno.setVisible(true);
            labelMiTurno.setVisible(false);
        }

        boolean finalturno = false;
        
        if (infoData.tuTurno){        
            puntuacion2.setText(String.valueOf(infoData.getPuntuacionRival()));
        
            ImageView sourceimagen = (ImageView) event.getSource();

            // Obtiene el índice de la imagen en la lista
            int imageIndex = imageViews.indexOf(sourceimagen);

            Image color= null;

            // Verifica el contenido del tablero en esa posición

            // rojo,negro,amarillo,blanco,gris,naranja,rosa,verde
            if (infoData.board.get(imageIndex).equals("-")) {
                // Si es "-", cambia a rojo
                if (board_colors.get(imageIndex).equals("rojo")){
                    color = rojo;
                }else if (board_colors.get(imageIndex).equals("negro")){
                    color = negro;
                }else if (board_colors.get(imageIndex).equals("amarillo")){
                    color = amarillo;
                }else if (board_colors.get(imageIndex).equals("azul")){
                    color = azul;
                }else if (board_colors.get(imageIndex).equals("gris")){
                    color = gris;
                }else if (board_colors.get(imageIndex).equals("naranja")){
                    color = naranja;
                }else if (board_colors.get(imageIndex).equals("rosa")){
                    color = rosa;
                }else if (board_colors.get(imageIndex).equals("verde")){
                    color = verde;
                };

                sourceimagen.setImage(color);

            
                System.out.println("board antes del set"+infoData.board);
                infoData.board.set(imageIndex, board_colors.get(imageIndex));
                infoData.setBoard(infoData.board);

                System.out.println("despues del click "+infoData.board);
                infoData.MessegeBoard(infoData.board,infoData.getPuntuacionMia());
             
                
          

                int cantidad = infoData.contarRepeticionesTotales(infoData.board);

                System.out.println(cantidad);
                if (cantidad == infoData.getPuntuacionMia()+1){
                    System.out.println("---has acertado una ams----");
                    infoData.setPuntuacionMia(infoData.getPuntuacionMia()+1);
                    puntuacion1.setText(String.valueOf(infoData.getPuntuacionMia()));
                    tiradas=0;
                    finalturno = false;
                    System.out.println("acertado tiradas-->"+tiradas);
                }else{
                    tiradas++;
                    finalturno =false;
                    System.out.println("fallo tiradas-->"+tiradas);
                    if (tiradas==2){
                    
                    /*
                    
                     */
                    
                    System.out.println("fallo 2 tiradas-->"+tiradas);
                    
                    
                    
                    finalturno = true;
                    
                    
                    infoData.board=infoData.modificarSinRepeticiones(infoData.board);
                    System.out.println(infoData.board);

                    infoData.setBoard(infoData.board);
                    infoData.tuTurno=false;
                    tiradas=0;
                   

                    actualizarBoard(infoData.board,false);
                    
                    
                    System.out.println("board que envio--->"+infoData.board);
                    infoData.broadcastMessage("android");
                    infoData.MessegeBoard(infoData.board,infoData.getPuntuacionMia());
                    

                };
                
            }
                
                
            
            } else {
                // Si no es "-", cambia a la imagen inicial
                sourceimagen.setImage(imagenInicial);
                infoData.board.set(imageIndex, "-");

                System.out.println("entre qui");

                infoData.setBoard(infoData.board);          
            };
          
    }else{
      System.out.println("entre en este else");
  
    }
        
    }
    
    
    @FXML
    private void handleDisconnect(ActionEvent event) {
        AppData appData = AppData.getInstance();
        appData.disconnectFromServer();
    }

    @FXML
    private void handleSend(ActionEvent event) {
        AppData appData = AppData.getInstance();
        String message = messageField.getText();
        //appData.send(message);
        messageField.clear();
    }

    public void updateInfo() {
        AppData appData = AppData.getInstance();
        serverAddressLabel.setText("ws://" + appData.getIp() + ":" + appData.getPort());
        clientIdLabel.setText(appData.getMySocketId());
    }

    public void updateMessages(String messages) {
        messagesArea.setText(messages);
    }

    public void updateClientList(List<String> clients) {
        Platform.runLater(() -> {
            clientsList.setItems(FXCollections.observableArrayList(clients));
        });
    }


    public void actualizarBoard(List<String> nuevoBoard,boolean mio) {
            if (mio!=true){
                
            }
            // Actualiza las imágenes en las ImageView según el contenido del nuevo board
            for (int i = 0; i < imageViews.size() && i < nuevoBoard.size(); i++) {
                ImageView imageView = imageViews.get(i);
                String color = nuevoBoard.get(i);
    
                // Lógica para asignar la imagen según el color o la imagen inicial si es "-"
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
