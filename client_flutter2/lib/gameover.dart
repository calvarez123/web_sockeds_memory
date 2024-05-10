import 'package:flutter/material.dart';

class GameOverScreen extends StatelessWidget {
  final String winner;
  final int score;

  GameOverScreen({Key? key, required this.winner, required this.score})
      : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('Game Over'),
        backgroundColor: Colors.blue,
        centerTitle: true,
      ),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Text(
              'El ganador es: $winner',
              style: TextStyle(
                fontSize: 24,
                fontWeight: FontWeight.bold,
                color: Colors.black,
              ),
            ),
            SizedBox(height: 20),
            Text(
              'Puntuación: $score',
              style: TextStyle(
                fontSize: 20,
                color: Colors.redAccent,
              ),
            ),
            SizedBox(height: 40),
            ElevatedButton(
              onPressed: () => Navigator.of(context)
                  .pop(), // Pops back to the previous screen or to the main menu
              child: Text('Volver al menú principal'),
              style: ElevatedButton.styleFrom(
                backgroundColor: Colors.blue, // Background color
              ),
            ),
          ],
        ),
      ),
    );
  }
}
