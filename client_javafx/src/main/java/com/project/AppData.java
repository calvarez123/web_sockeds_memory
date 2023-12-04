package com.project;

import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;

import com.project.AppData.ConnectionStatus;
import com.project.AppSocketsClient.OnCloseObject;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.util.Duration;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class AppData {

    private static final AppData INSTANCE = new AppData();
    private AppSocketsClient socketClient;
    private String ip = "localhost";
    private String port = "8888";
    private ConnectionStatus connectionStatus = ConnectionStatus.DISCONNECTED;
    private String mySocketId;
    private List<String> clients = new ArrayList<>();
    private String selectedClient = "";
    private Integer selectedClientIndex;
    private StringBuilder messages = new StringBuilder();

    private int puntuacionMia = 0;
    private int puntuacionRival = 0;

    boolean tuTurno;

    int aciertos = 0;

    private List<String> board_colors = new ArrayList<>();

    private List<String> board = new ArrayList<>();


    public enum ConnectionStatus {
        DISCONNECTED, DISCONNECTING, CONNECTING, CONNECTED
    }
    public int getPuntuacionMia() {
        return puntuacionMia;
    }

    public int getAciertos() {
        return aciertos;
    }

    public void setAciertos(int aciertos) {
        this.aciertos = aciertos;
    }

    public  void setPuntuacionMia(int nuevaPuntuacionMia) {
        puntuacionMia = nuevaPuntuacionMia;
    }

    public  int getPuntuacionRival() {
        return puntuacionRival;
    }

    public  void setPuntuacionRival(int nuevaPuntuacionRival) {
        puntuacionRival = nuevaPuntuacionRival;
    }


    public static int contarRepeticionesTotales(List<String> lista) {
        int contador = 0;
        for (int i = 0; i < lista.size(); i++) {
            String elementoActual = lista.get(i);

            if (!elementoActual.equals("-")) {
                for (int j = i + 1; j < lista.size(); j++) {
                    String otroElemento = lista.get(j);

                    if (!otroElemento.equals("-") && elementoActual.equals(otroElemento)) {
                        contador++;
                    }
                }
            }
        }
        System.out.println("Se repite " + contador + " veces.");
        return contador;
    }

    public static void modificarSinRepeticiones(List<String> lista) {
        for (int i = 0; i < lista.size(); i++) {
            String elementoActual = lista.get(i);

            // Añadimos una condición para asegurarnos de que elementoActual no sea "-"
            if (!elementoActual.equals("-")) {
                boolean seRepite = false;

                for (int j = i + 1; j < lista.size(); j++) {
                    String otroElemento = lista.get(j);

                    // Añadimos una condición para asegurarnos de que otroElemento no sea "-"
                    if (!otroElemento.equals("-") && elementoActual.equals(otroElemento)) {
                        seRepite = true;
                        break;
                    }
                }

                if (!seRepite) {
                    // Si no se repite, modificamos la lista original para hacer que sea "-"
                    lista.set(i, "-");
                }
            }
        }
    }

    private AppData() {
    }

    public static AppData getInstance() {
        return INSTANCE;
    }

    public String getLocalIPAddress() throws SocketException, UnknownHostException {
        
        String localIp = "";
        Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
        while (networkInterfaces.hasMoreElements()) {
            NetworkInterface ni = networkInterfaces.nextElement();
            Enumeration<InetAddress> inetAddresses = ni.getInetAddresses();
            while (inetAddresses.hasMoreElements()) {
                InetAddress ia = inetAddresses.nextElement();
                if (!ia.isLinkLocalAddress() && !ia.isLoopbackAddress() && ia.isSiteLocalAddress()) {
                    System.out.println(ni.getDisplayName() + ": " + ia.getHostAddress());
                    localIp = ia.getHostAddress();
                    // Si hi ha múltiples direccions IP, es queda amb la última
                }
            }
        }

        // Si no troba cap direcció IP torna la loopback
        if (localIp.compareToIgnoreCase("") == 0) {
            localIp = InetAddress.getLocalHost().getHostAddress();
        }
        return localIp;
    }

    public void connectToServer() {
        try {
            URI location = new URI("ws://" + ip + ":" + port);
            socketClient = new AppSocketsClient(
                    location,
                    (ServerHandshake handshake) ->  { this.onOpen(handshake);},
                    (String message) ->             { this.onMessage(message); },
                    (OnCloseObject closeInfo) ->    { this.onClose(closeInfo); },
                    (Exception ex) ->               { this.onError(ex); }
            );
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        connectionStatus = ConnectionStatus.CONNECTING;
        socketClient.connect();
        UtilsViews.setViewAnimating("Connecting");
        PauseTransition pause = new PauseTransition(Duration.seconds(1));
        pause.setOnFinished(event -> {
            if (connectionStatus == ConnectionStatus.CONNECTED) {
                CtrlLayoutConnected ctrlConnected = (CtrlLayoutConnected) UtilsViews.getController("Connected");
                ctrlConnected.updateInfo();
                UtilsViews.setViewAnimating("Connected");
            } else {
                UtilsViews.setViewAnimating("Disconnected");
            }
        });
        pause.play();
    }

    public void disconnectFromServer() {
        connectionStatus = ConnectionStatus.DISCONNECTING;
        UtilsViews.setViewAnimating("Disconnecting");
        PauseTransition pause = new PauseTransition(Duration.seconds(1));
        pause.setOnFinished(event -> {
            socketClient.close();
        });
        pause.play();
    }

    private void onOpen (ServerHandshake handshake) {
        System.out.println("Handshake: " + handshake.getHttpStatusMessage());
        connectionStatus = ConnectionStatus.CONNECTED; 
    }

    private void onMessage(String message) {
        JSONObject data = new JSONObject(message);

        if (connectionStatus != ConnectionStatus.CONNECTED) {
            connectionStatus = ConnectionStatus.CONNECTED;
        }

        String type = data.getString("type");
        switch (type) {
            case "turno":
                tuTurno = true;
                System.out.println("lo recibi");
                
                break;
            case "board":
                CtrlLayoutConnected layautcoenConnected = new CtrlLayoutConnected();
                board.clear();
                data.getJSONArray("list").forEach(item -> board.add(item.toString()));
                setPuntuacionRival(data.getInt("puntuacion"));
                boolean finTurnoRival = data.getBoolean("finturno");

                if (finTurnoRival==true){
                    tuTurno = true;
                }else{
                    tuTurno= false;
                }

                

                String miPuntuacion = String.valueOf(puntuacionMia);

                board.remove(mySocketId);
              

                layautcoenConnected.actualizarBoard(board);
                
            
                
                break;
            
            case "list":
                board_colors.clear();
                System.out.println("mueve este");
                data.getJSONArray("list").forEach(item -> board_colors.add(item.toString()));
                board_colors.remove(mySocketId);
                
                break;
            case "id":
                mySocketId = data.getString("value");
                messages.append("Id received: ").append(data.getString("value")).append("\n");
                break;
            case "connected":
                clients.add(data.getString("id"));
                clients.remove(mySocketId);
                messages.append("Connected client: ").append(data.getString("id")).append("\n");
                updateClientList();
                break;
            case "disconnected":
                String removeId = data.getString("id");
                if (selectedClient.equals(removeId)) {
                    selectedClient = "";
                }
                clients.remove(data.getString("id"));
                messages.append("Disconnected client: ").append(data.getString("id")).append("\n");
                updateClientList();
                break;
            case "private":
                messages.append("Private message from '")
                        .append(data.getString("from"))
                        .append("': ")
                        .append(data.getString("value"))
                        .append("\n");
                break;
            default:
                messages.append("Message from '")
                        .append(data.getString("from"))
                        .append("': ")
                        .append(data.getString("value"))
                        .append("\n");
                break;
        }
        if (connectionStatus == ConnectionStatus.CONNECTED) {
            CtrlLayoutConnected ctrlConnected = (CtrlLayoutConnected) UtilsViews.getController("Connected");
            ctrlConnected.updateMessages(messages.toString());        
        }
    }

    public void onClose(OnCloseObject closeInfo) {
        connectionStatus = ConnectionStatus.DISCONNECTED;
        UtilsViews.setViewAnimating("Disconnected");
    }

    public void onError(Exception ex) {
        System.out.println("Error: " + ex.getMessage());
    }

    public void refreshClientsList() {
        JSONObject message = new JSONObject();
        message.put("type", "list");
        socketClient.send(message.toString());
    }

    public void updateClientList() {
        if (connectionStatus == ConnectionStatus.CONNECTED) {
            CtrlLayoutConnected ctrlConnected = (CtrlLayoutConnected) UtilsViews.getController("Connected");
            ctrlConnected.updateClientList(clients);
        }
    }
    

    public void selectClient(int index) {
        if (selectedClientIndex == null || selectedClientIndex != index) {
            selectedClientIndex = index;
            selectedClient = clients.get(index);
        } else {
            selectedClientIndex = null;
            selectedClient = "";
        }
    }

    public Integer getSelectedClientIndex() {
        return selectedClientIndex;
    }

    public void send(String msg) {
        if (selectedClientIndex == null) {
            broadcastMessage(msg);
        } else {
            privateMessage(msg);
        }
    }



    public List<String> getBoard_colors() {
        return this.board_colors;
    }

    public void setBoard_colors(List<String> board_colors) {
        this.board_colors = board_colors;
    }

    public List<String> getBoard() {
        return this.board_colors;
    }

    public void setBoard(List<String> board_colors) {
        this.board_colors = board_colors;
    }
    

    public void broadcastMessage(String msg) {
        JSONObject message = new JSONObject();
        message.put("type", "broadcast");
        message.put("value", msg);
        socketClient.send(message.toString());
    }
    public void MessegeBoard(List<String> msg,int puntuacion,boolean finTurno) {
        JSONObject message = new JSONObject();
        message.put("type", "board");
        message.put("from", "cliente");
        message.put("puntuacion", puntuacion);
        message.put("finturno", finTurno);
        message.put("value", msg);
        socketClient.send(message.toString());
    }

    public void privateMessage(String msg) {
        if (selectedClient.isEmpty()) return;
        JSONObject message = new JSONObject();
        message.put("type", "private");
        message.put("value", msg);
        message.put("destination", selectedClient);
        socketClient.send(message.toString());
    }

    public String getIp() {
        return ip;
    }

    public String setIp (String ip) {
        return this.ip = ip;
    }

    public String getPort() {
        return port;
    }

    public String setPort (String port) {
        return this.port = port;
    }

    public String getMySocketId () {
        return mySocketId;
    }
}
