import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'app_data.dart';
import 'gameover.dart'; // Asegúrate de importar correctamente tu pantalla GameOver

class LayoutConnected extends StatefulWidget {
  const LayoutConnected({Key? key}) : super(key: key);

  @override
  State<LayoutConnected> createState() => _LayoutConnectedState();
}

class _LayoutConnectedState extends State<LayoutConnected> {
  @override
  Widget build(BuildContext context) {
    AppData appData = Provider.of<AppData>(context);

    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (appData.partidaTerminada) {
        String ganador = appData.ganador;
        int puntos = appData.puntosGanador;
        Navigator.push(
            context,
            MaterialPageRoute(
                builder: (context) => GameOverScreen(
                      winner: ganador,
                      score: puntos,
                    )));
        appData.partidaTerminada = false; // Reset the game end flag
      }
    });

    return Scaffold(
      appBar: AppBar(
        title: Text('Memory'),
        backgroundColor: Colors.blue,
        centerTitle: true,
      ),
      body: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          Padding(
            padding: EdgeInsets.only(top: 40.0),
            child: Text(
              '${appData.usu} : ${appData.miPuntuacion}              ${appData.enemigo} : ${appData.puntuacionRival}',
              textAlign: TextAlign.center,
              style: TextStyle(fontSize: 18.0, fontWeight: FontWeight.bold),
            ),
          ),
          const SizedBox(height: 20.0),
          Center(
            child: Text(
              appData.tuTurno ? "Tu Turno" : "Turno Rival",
              style: TextStyle(
                  fontSize: 24.0,
                  fontWeight: FontWeight.bold,
                  color: appData.tuTurno ? Colors.green : Colors.red),
            ),
          ),
          const SizedBox(height: 20.0),
          Center(
            child: Container(
              width: 400,
              height: 400,
              child: ImageGridView(),
            ),
          ),
        ],
      ),
    );
  }
}

class ImageGridView extends StatefulWidget {
  const ImageGridView({Key? key}) : super(key: key);

  @override
  _ImageGridViewState createState() => _ImageGridViewState();
}

class _ImageGridViewState extends State<ImageGridView> {
  late List<String> imagePaths;
  Set<int> clickedIndices = {};
  bool processingClick = false;

  @override
  void initState() {
    super.initState();
    imagePaths = List.generate(16, (index) => 'assets/imagen_inicial.jpg');
  }

  updateImagesAutomatically(AppData appData) {
    setState(() {
      for (int i = 0; i < appData.board.length; i++) {
        if (appData.board[i] == '-') {
          imagePaths[i] = 'assets/imagen_inicial.jpg';
        } else {
          imagePaths[i] = 'assets/${appData.board[i]}.png';
        }
      }
    });
  }

  void onTapLogic(AppData appData, int index) {
    if (processingClick) {
      return;
    }
    if (appData.tuTurno) {
      setState(() {
        processingClick = true;
        clickedIndices.add(index);

        String color = appData.boardColors[index];
        imagePaths[index] = 'assets/$color.png';
        appData.board[index] = color;

        appData.messageBoard(index);

        processingClick = false;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    AppData appData = Provider.of<AppData>(context);
    updateImagesAutomatically(appData);

    return GridView.builder(
      gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
        crossAxisCount: 4,
        crossAxisSpacing: 8.0,
        mainAxisSpacing: 8.0,
      ),
      itemCount: 16,
      itemBuilder: (context, index) {
        return GestureDetector(
          onTap: () => onTapLogic(appData, index),
          child: Padding(
            padding: const EdgeInsets.all(8.0),
            child: Image.asset(
              imagePaths[index],
              width: 100,
              height: 100,
              fit: BoxFit.cover,
            ),
          ),
        );
      },
    );
  }
}
