import 'dart:async';
import 'dart:convert';
import 'dart:io';
import 'package:client_flutter/layout_connected.dart';
import 'package:flutter/material.dart';

import 'package:web_socket_channel/io.dart';

// Access appData globaly with:
// AppData appData = Provider.of<AppData>(context);
// AppData appData = Provider.of<AppData>(context, listen: false)

enum ConnectionStatus {
  disconnected,
  connected,
}

class AppData with ChangeNotifier {
  String plat = "";
  String ip = "localhost";
  String port = "8888";
  String usu = "";
  String enemigo = "no hay nadie";
  bool tuTurno = false;
  int puntuacionRival = 0;
  int miPuntuacion = 0;
  List<dynamic> board = [
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-"
  ];

  List<dynamic> boardColors = [];

  IOWebSocketChannel? _channel;
  ConnectionStatus connectionStatus = ConnectionStatus.disconnected;

  String? mySocketId;
  List<String> clients = [];
  String selectedClient = "";
  int? selectedClientIndex;
  String messages = "";
  int tiradas = 0;

  AppData() {
    _getLocalIpAddress();
  }

  void _getLocalIpAddress() async {
    try {
      final List<NetworkInterface> interfaces = await NetworkInterface.list(
          type: InternetAddressType.IPv4, includeLoopback: false);
      if (interfaces.isNotEmpty) {
        final NetworkInterface interface = interfaces.first;
        final InternetAddress address = interface.addresses.first;
        ip = address.address;
        notifyListeners();
      }
    } catch (e) {
      // ignore: avoid_print
      print("Can't get local IP address : $e");
    }
  }

  void connectToServer() async {
    notifyListeners();
    await Future.delayed(const Duration(seconds: 1));

    _channel = IOWebSocketChannel.connect("ws://$ip:$port");

    final message = {
      'type': 'playerName',
      'name': usu,
    };
    _channel!.sink.add(jsonEncode(message));
    _channel!.stream.listen(
      (message) {
        final data = jsonDecode(message);

        if (connectionStatus != ConnectionStatus.connected) {
          connectionStatus = ConnectionStatus.connected;
        }

        switch (data['type']) {
          case "turno":
            tuTurno = true;
            print("lo recibi");

            break;
          case "tetoca":
            plat = data['value'];
            if (data['value'] == "android") {
              tuTurno = true;
            }
          case "board":
            notifyListeners();
            tuTurno =
                data["turno"].toString().toLowerCase() == usu.toLowerCase();

            board.clear();
            board = data["list"];

            print(board);
            break;
          case 'list':
            boardColors = data["list"];
            break;
          case 'lista':
            notifyListeners();
            print(data["lista"]);
            Map<String, dynamic> mapa =
                data["lista"]; // No necesitas jsonDecode
            List<String> claves = mapa.keys.toList();
            for (String clave in claves) {
              if (clave != usu) {
                enemigo = clave;
                break; // Romper el bucle cuando se encuentra la primera clave diferente de "usu"
              }
            }
            break;

          case 'disconnected':
            String removeId = data['id'];
            if (selectedClient == removeId) {
              selectedClient = "";
            }
            clients.remove(data['id']);
            messages += "Disconnected client: ${data['id']}\n";
            break;
          default:
            break;
        }

        notifyListeners();
      },
      onError: (error) {
        connectionStatus = ConnectionStatus.disconnected;
        mySocketId = "";
        selectedClient = "";
        clients = [];
        messages = "";
        notifyListeners();
      },
      onDone: () {
        connectionStatus = ConnectionStatus.disconnected;
        mySocketId = "";
        selectedClient = "";
        clients = [];
        messages = "";
        notifyListeners();
      },
    );
  }

  disconnectFromServer() async {
    notifyListeners();

    // Simulate connection delay
    await Future.delayed(const Duration(seconds: 1));

    _channel!.sink.close();
  }

  int contarRepeticionesTotales(List<dynamic> lista) {
    int contador = 0;

    for (int i = 0; i < lista.length; i++) {
      String elementoActual = lista[i];

      if (elementoActual != "-") {
        for (int j = i + 1; j < lista.length; j++) {
          String otroElemento = lista[j];

          if (otroElemento != "-" && elementoActual == otroElemento) {
            contador++;
          }
        }
      }
    }

    print("Se repite $contador veces.");
    return contador;
  }

  messageBoard(int imageindex) {
    final message = {
      'type': 'board',
      'from': usu,
      'value': imageindex,
    };
    _channel!.sink.add(jsonEncode(message));
  }

  finTurno() {
    final message = {
      'type': 'finturno',
      'value': 'flutter',
    };
    _channel!.sink.add(jsonEncode(message));
  }

  List modificarSinRepeticiones(List<dynamic> lista) {
    Set<String> nombresRepetidos = Set();
    Set<String> nombresNoRepetidos = Set();

    for (String nombre in lista) {
      if (!nombresNoRepetidos.add(nombre)) {
        // Si el nombre ya está en nombresNoRepetidos, entonces es repetido
        nombresRepetidos.add(nombre);
      }
    }

    for (int i = 0; i < lista.length; i++) {
      String elementoActual = lista[i];

      if (!nombresRepetidos.contains(elementoActual)) {
        // Si el elemento no está en nombresRepetidos, se reemplaza con '-'
        lista[i] = '-';
      }
    }
    return lista;
  }
}
