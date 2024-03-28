package com.project;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.json.JSONArray;
import org.json.JSONObject;

public class ChatServer extends WebSocketServer {

    static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
    static List<String> board_colors = new ArrayList<>(
            (Arrays.asList("rojo", "negro", "amarillo", "azul", "gris", "naranja",
                    "rosa", "verde", "rojo", "negro", "amarillo", "azul", "gris", "naranja", "rosa", "verde")));

    static List<String> board = new ArrayList<>(
            Arrays.asList("-", "-", "-", "-", "-", "-", "-", "-", "-", "-", "-", "-", "-", "-", "-", "-"));

    private static final Map<String, Integer> connectedClients = new HashMap<>();
    private Map<WebSocket, String> connectionPlayerMap = new HashMap<>();
    private int posi1;
    private int posi2;
    private static int tiradas = 1;
    private static String color1, color2;
    private static boolean iguales;

    public ChatServer(int port) {
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

        // Enviem la direcció URI del nou client a tothom
        JSONObject objCln = new JSONObject("{}");
        objCln.put("type", "connected");
        objCln.put("from", "server");
        objCln.put("id", clientId);
        broadcast(objCln.toString());

        // ENVIAR LA LISTA A TODOS LOS CLIENTES
        JSONObject objResponse = new JSONObject("{}");
        objResponse.put("type", "list");
        objResponse.put("from", "server");
        objResponse.put("list", board_colors);
        conn.send(objResponse.toString());

        // Mostrem per pantalla (servidor) la nova connexió
        String host = conn.getRemoteSocketAddress().getAddress().getHostAddress();
        System.out.println("New client (" + clientId + "): " + host);

    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        String clientId = getConnectionId(conn);
        connectedClients.remove(connectionPlayerMap.get(conn));
        connectionPlayerMap.remove(conn);
        System.out.println("Client disconnected '" + clientId + "'");
        if (connectedClients.size() == 0) {
            System.out.println("entroooooo");
            for (int i = 0; i < board.size(); i++) {
                board.set(i, "-");
            }
        }
        System.out.println(board);
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        try {
            JSONObject objRequest = new JSONObject(message);
            String type = objRequest.getString("type");
            if (type.equalsIgnoreCase("board")) {
                String nombre = objRequest.getString("from");
                if (tiradas == 1) {
                    posi1 = objRequest.getInt("value");
                    color1 = cogerColor(posi1);
                    JSONObject objResponse = new JSONObject("{}");
                    objResponse.put("type", "board");
                    objResponse.put("lista", connectedClients);
                    objResponse.put("list", board);
                    objResponse.put("turno", nombre);
                    broadcast(objResponse.toString());
                } else if (tiradas == 2) {
                    posi2 = objRequest.getInt("value");
                    color2 = cogerColor(posi2);
                    JSONObject objResponse1 = new JSONObject("{}");
                    objResponse1.put("type", "board");
                    objResponse1.put("lista", connectedClients);
                    objResponse1.put("list", board);
                    objResponse1.put("turno", nombre);
                    broadcast(objResponse1.toString());
                    iguales = coloresIguales(color1, color2);
                    if (iguales) {
                        tiradas = 1;
                        connectedClients.put(nombre, connectedClients.get(nombre) + 1);
                        JSONObject objResponse2 = new JSONObject("{}");
                        objResponse2.put("type", "board");
                        objResponse2.put("lista", connectedClients);
                        objResponse2.put("list", board);
                        objResponse2.put("turno", nombre);
                        broadcast(objResponse2.toString());
                    } else {
                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                tiradas = 1;
                                String otroNombre = "";
                                for (Map.Entry<String, Integer> entry : connectedClients.entrySet()) {
                                    if (!entry.getKey().equalsIgnoreCase(nombre)) {
                                        otroNombre = entry.getKey();
                                        break;
                                    }
                                }
                                JSONObject objResponse3 = new JSONObject("{}");
                                objResponse3.put("type", "board");
                                objResponse3.put("turno", otroNombre);
                                objResponse3.put("lista", connectedClients);
                                objResponse3.put("list", board);
                                broadcast(objResponse3.toString());
                            }
                        }, 1500);
                    }

                }

            } else if (type.equalsIgnoreCase("playerName")) {
                connectionPlayerMap.put(conn, objRequest.getString("name"));
                System.out.println( objRequest.getString("name"));
                connectedClients.put(objRequest.getString("name"), 0);
                if (connectedClients.size() == 1) {
                    JSONObject objTurno = new JSONObject("{}");
                    objTurno.put("type", "turno");
                    objTurno.put("clienteTurno", objRequest.getString("name"));
                    broadcast(objTurno.toString());
                } else {
                    JSONObject objTurno = new JSONObject("{}");
                    objTurno.put("type", "turno");
                    objTurno.put("clienteTurno", "");
                    broadcast(objTurno.toString());
                }
                JSONObject objResponse = new JSONObject("{}");
                objResponse.put("type", "lista");
                objResponse.put("lista", connectedClients);
                broadcast(objResponse.toString());

            }

        } catch (

        Exception e) {
            e.printStackTrace();
        }
    }

    public static String cogerColor(int posicion) {
        String color = board_colors.get(posicion);
        board.set(posicion, color);
        tiradas = tiradas + 1;
        return color;
    }

    public static boolean coloresIguales(String col1, String col2) {
        if (col1.equalsIgnoreCase(col2)) {
            return true;
        }
        modificarSinRepeticiones(board);
        return false;
    }

    public static List<String> modificarSinRepeticiones(List<String> lista) {
        Set<String> nombresRepetidos = new HashSet<>();
        Set<String> nombresNoRepetidos = new HashSet<>();

        for (String nombre : lista) {
            if (!nombresNoRepetidos.add(nombre)) {
                // Si el nombre ya está en nombresNoRepetidos, entonces es repetido
                nombresRepetidos.add(nombre);
            }
        }

        for (int i = 0; i < lista.size(); i++) {
            String elementoActual = lista.get(i);

            if (!nombresRepetidos.contains(elementoActual)) {
                // Si el elemento no está en nombresRepetidos, se reemplaza con '-'
                lista.set(i, "-");
            }
        }
        return lista;
    }

    public static List<String> convertirJSONArrayALista(JSONArray jsonArray) {
        List<String> lista = new ArrayList<>();

        // Iterar sobre el JSONArray y agregar elementos a la lista
        for (int i = 0; i < jsonArray.length(); i++) {
            lista.add(jsonArray.getString(i));
        }

        return lista;
    }

    /*
     * JSONObject objResponse = new JSONObject("{}");
     * objResponse.put("type", "broadcast");
     * objResponse.put("list", getBoard());
     * broadcast(objResponse.toString());
     */

    @Override
    public void onError(WebSocket conn, Exception ex) {
        // Quan hi ha un error
        ex.printStackTrace();
    }

    public void runServerBucle() {
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

    public String getConnectionId(WebSocket connection) {
        String name = connection.toString();
        return name.replaceAll("org.java_websocket.WebSocketImpl@", "").substring(0, 3);
    }

}