package com.project;

import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;
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
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppData {

    private static final AppData INSTANCE = new AppData();
    private AppSocketsClient socketClient;
    private String ip = "localhost";
    private String port = "8888";
    private ConnectionStatus connectionStatus = ConnectionStatus.DISCONNECTED;
    private String mySocketId;
    private Map<String, Integer> localConnectedClients = new HashMap<>();

    CtrlLayoutConnected layautcoenConnected = new CtrlLayoutConnected();
    String playerName = "";
    String otroJugador = "No hay nadie";

    boolean tuTurno = false;

    static List<String> board = new ArrayList<>(
            Arrays.asList("-", "-", "-", "-", "-", "-", "-", "-", "-", "-", "-", "-", "-", "-", "-", "-"));

    public enum ConnectionStatus {
        DISCONNECTED, DISCONNECTING, CONNECTING, CONNECTED
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
                    (ServerHandshake handshake) -> {
                        this.onOpen(handshake);
                    },
                    (String message) -> {
                        this.onMessage(message);
                    },
                    (OnCloseObject closeInfo) -> {
                        this.onClose(closeInfo);
                    },
                    (Exception ex) -> {
                        this.onError(ex);
                    });
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        connectionStatus = ConnectionStatus.CONNECTING;
        socketClient.connect();
        UtilsViews.setViewAnimating("Connecting");
        PauseTransition pause = new PauseTransition(Duration.seconds(1));
        pause.setOnFinished(event -> {
            if (connectionStatus == ConnectionStatus.CONNECTED) {
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

    public void setLayoutConnected(CtrlLayoutConnected layoutConnected) {
        this.layautcoenConnected = layoutConnected;
    }

    private void onOpen(ServerHandshake handshake) {
        System.out.println("Handshake: " + handshake.getHttpStatusMessage());
        connectionStatus = ConnectionStatus.CONNECTED;
        JSONObject message = new JSONObject();
        try {
            message.put("type", "playerName");
            message.put("name", playerName);
            socketClient.send(message.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void onMessage(String message) {
        JSONObject data = new JSONObject(message);

        if (connectionStatus != ConnectionStatus.CONNECTED) {
            connectionStatus = ConnectionStatus.CONNECTED;
        }

        String type = data.getString("type");
        switch (type) {
            case "turno":
                if (data.getString("clienteTurno").equalsIgnoreCase(playerName)) {
                    tuTurno = true;
                    System.out.println("lo recibi");
                }
                Platform.runLater(() -> layautcoenConnected.actualizarTurno());
                break;
            case "board":
                tuTurno = data.getString("turno").equalsIgnoreCase(playerName);
                board.clear();
                data.getJSONArray("list").forEach(item -> board.add(item.toString()));
                JSONObject listaa = data.getJSONObject("lista");
                Map<String, Integer> updatedClientss = new HashMap<>();
                for (String key : listaa.keySet()) {
                    if (!key.equalsIgnoreCase(playerName)) {
                        otroJugador = key;
                    }
                    updatedClientss.put(key, listaa.getInt(key));

                }
                if (layautcoenConnected != null) {
                    Platform.runLater(() -> layautcoenConnected.actualizarLabelTurno(
                            String.valueOf(updatedClientss.get(playerName)),
                            String.valueOf(updatedClientss.get(otroJugador))));
                    Platform.runLater(() -> layautcoenConnected.actualizarBoard(board));
                }

                break;
            case "id":
                mySocketId = data.getString("value");
                break;
            case "lista":
                JSONObject lista = data.getJSONObject("lista");
                Map<String, Integer> updatedClients = new HashMap<>();
                for (String key : lista.keySet()) {
                    updatedClients.put(key, lista.getInt(key));
                }
                localConnectedClients.clear();
                localConnectedClients.putAll(updatedClients);

                for (String key : localConnectedClients.keySet()) {
                    if (!key.equalsIgnoreCase(playerName)) {
                        otroJugador = key;
                    }
                }
                Platform.runLater(() -> layautcoenConnected.actualizarNombresyPuntuacion(playerName,
                        "0", otroJugador,
                        "0"));

                break;
            default:
                break;
        }
    }

    public void onClose(OnCloseObject closeInfo) {
        connectionStatus = ConnectionStatus.DISCONNECTED;
        UtilsViews.setViewAnimating("Disconnected");
    }

    public void onError(Exception ex) {
        System.out.println("Error: " + ex.getMessage());
    }

    public void on(String ex) {
        System.out.println("Error: " + ex);
    }

    public void MessegeBoard(int msg) {
        JSONObject message = new JSONObject();
        message.put("type", "board");
        message.put("from", playerName);
        message.put("value", msg);
        socketClient.send(message.toString());
    }

    public String getIp() {
        return ip;
    }

    public String setIp(String ip) {
        return this.ip = ip;
    }

    public String setPlayerName(String name) {
        return this.playerName = name;
    }

    public String getPort() {
        return port;
    }

    public String setPort(String port) {
        return this.port = port;
    }

    public String getMySocketId() {
        return mySocketId;
    }
}
