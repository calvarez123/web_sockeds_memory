package com.project;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.json.JSONArray;
import org.json.JSONObject;

public class ChatServer extends WebSocketServer {

    static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
    List<String> board_colors = new ArrayList<>((Arrays.asList("rojo", "negro", "amarillo", "azul", "gris", "naranja", "rosa", "verde","rojo", "negro", "amarillo", "azul", "gris", "naranja", "rosa", "verde")));
    List<String> board = new ArrayList<>(Arrays.asList("-", "-", "-", "-", "-", "-", "-", "-", "-", "-", "-", "-", "-", "-", "-", "-"));
    private static final List<String> connectedClients = new ArrayList<>();
    private static int turnoActual = 0;


    public ChatServer (int port) {
        super(new InetSocketAddress(port));
    }

    @Override
    public void onStart() {
        // Quan el servidor s'inicia
        String host = getAddress().getAddress().getHostAddress();
        int port = getAddress().getPort();
        System.out.println("WebSockets server running at: ws://" + host + ":" + port);
        System.out.println("Type 'exit' to stop and exit server.");
        setConnectionLostTimeout(0);
        setConnectionLostTimeout(100);
        Collections.shuffle(board_colors);
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        // Quan un client es connecta
        String clientId = getConnectionId(conn);

        // Saludem personalment al nou client
        JSONObject objWlc = new JSONObject("{}");
        objWlc.put("type", "private");
        objWlc.put("from", "server");
        objWlc.put("value", "Welcome to the chat server");
        conn.send(objWlc.toString()); 
        

        // Li enviem el seu identificador
        JSONObject objId = new JSONObject("{}");
        objId.put("type", "id");
        objId.put("from", "server");
        objId.put("value", clientId);
        conn.send(objId.toString()); 

        // Enviem al client la llista amb tots els clients connectats
        sendList(conn);

        // Enviem la direcció URI del nou client a tothom 
        JSONObject objCln = new JSONObject("{}");
        objCln.put("type", "connected");
        objCln.put("from", "server");
        objCln.put("id", clientId);
        broadcast(objCln.toString());

        //ENVIAR LA LISTA A TODOS LOS CLIENTES
        JSONObject objResponse = new JSONObject("{}");
        objResponse.put("type", "list");
        objResponse.put("from", "server");
        objResponse.put("list", board_colors);
        conn.send(objResponse.toString()); 

        // Mostrem per pantalla (servidor) la nova connexió
        String host = conn.getRemoteSocketAddress().getAddress().getHostAddress();
        System.out.println("New client (" + clientId + "): " + host);

        connectedClients.add(getConnectionId(conn));
        if (connectedClients.size() == 1) {
            System.err.println("ennviado");
            // Es el primer cliente, comienza el turno
            enviarTurno(conn);
        }
    }

    private void enviarTurno(WebSocket conn) {
        // Obtener el cliente actual para el turno
        String clienteTurno = connectedClients.get(turnoActual);
    
        // Enviar mensaje al cliente actual indicando que es su turno
        JSONObject objTurno = new JSONObject("{}");
        objTurno.put("type", "turno");
        objTurno.put("from", "server");
        objTurno.put("clienteTurno", clienteTurno);
        conn.send(objTurno.toString());
    
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        // Quan un client es desconnecta
        String clientId = getConnectionId(conn);

        // Informem a tothom que el client s'ha desconnectat
        JSONObject objCln = new JSONObject("{}");
        objCln.put("type", "disconnected");
        objCln.put("from", "server");
        objCln.put("id", clientId);
        broadcast(objCln.toString());

        // Mostrem per pantalla (servidor) la desconnexió
        System.out.println("Client disconnected '" + clientId + "'");

        connectedClients.clear();
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        // Quan arriba un missatge
        String clientId = getConnectionId(conn);
        try {
            JSONObject objRequest = new JSONObject(message);
            String type = objRequest.getString("type");

            if (type.equalsIgnoreCase("board")) {
                JSONArray jsonArray = objRequest.getJSONArray("value");
                int puntuacionRival = objRequest.getInt("puntuacion");
                System.out.println(puntuacionRival);
                List<String> Actualizadoboard = convertirJSONArrayALista(jsonArray);
                setBoard(Actualizadoboard);

                JSONObject objResponse = new JSONObject("{}");
                objResponse.put("type", "board");
                objResponse.put("puntuacion", puntuacionRival);
                objResponse.put("list", getBoard());
                broadcast(objResponse.toString()); 
                
            
            }
            if (type.equalsIgnoreCase("list")) {
                // El client demana la llista de tots els clients
                System.out.println("Client '" + clientId + "'' requests list of clients");
                sendList(conn);

            } else if (type.equalsIgnoreCase("private")) {
                // El client envia un missatge privat a un altre client
                System.out.println("Client '" + clientId + "'' sends a private message");

                JSONObject objResponse = new JSONObject("{}");
                objResponse.put("type", "private");
                objResponse.put("from", clientId);
                objResponse.put("value", objRequest.getString("value"));

                String destination = objRequest.getString("destination");
                WebSocket desti = getClientById(destination);

                if (desti != null) {
                    desti.send(objResponse.toString()); 
                }
                
            } else if (type.equalsIgnoreCase("broadcast")) {
                // El client envia un missatge a tots els clients
                System.out.println("Client '" + clientId + "'' sends a broadcast message to everyone");
             
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*JSONObject objResponse = new JSONObject("{}");
                objResponse.put("type", "broadcast");
                objResponse.put("list", getBoard());
                broadcast(objResponse.toString()); */

    @Override
    public void onError(WebSocket conn, Exception ex) {
        // Quan hi ha un error
        ex.printStackTrace();
    }

    public void runServerBucle () {
        boolean running = true;
        try {
            System.out.println("Starting server");
            start();
            while (running) {
                String line;
                line = in.readLine();
                if (line.equals("exit")) {
                    running = false;
                }
            } 
            System.out.println("Stopping server");
            stop(1000);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }  
    }

    public void sendList (WebSocket conn) {
        JSONObject objResponse = new JSONObject("{}");
        objResponse.put("type", "list");
        objResponse.put("from", "server");
        objResponse.put("list", getClients());
        conn.send(objResponse.toString()); 
    }

  

    public String getConnectionId (WebSocket connection) {
        String name = connection.toString();
        return name.replaceAll("org.java_websocket.WebSocketImpl@", "").substring(0, 3);
    }

    public String[] getClients () {
        int length = getConnections().size();
        String[] clients = new String[length];
        int cnt = 0;

        for (WebSocket ws : getConnections()) {
            clients[cnt] = getConnectionId(ws);               
            cnt++;
        }
        return clients;
    }


    public WebSocket getClientById (String clientId) {
        for (WebSocket ws : getConnections()) {
            String wsId = getConnectionId(ws);
            if (clientId.compareTo(wsId) == 0) {
                return ws;
            }               
        }
        
        return null;
    }

    public List<String> getBoard() {
        return board;
    }

    // Setter para establecer la lista completa
    public void setBoard(List<String> board) {
        this.board = board;
    }

    public static List<String> convertirJSONArrayALista(JSONArray jsonArray) {
        List<String> lista = new ArrayList<>();

        // Iterar sobre el JSONArray y agregar elementos a la lista
        for (int i = 0; i < jsonArray.length(); i++) {
            lista.add(jsonArray.getString(i));
        }

        return lista;
    }
}