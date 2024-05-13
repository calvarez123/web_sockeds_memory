package com.project;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.text.Text;

public class CtrlLayoutGameOver {
    @FXML
    private Text ganador;

    @FXML
    private Text puntuacion;
    AppData infoData = AppData.getInstance();

    
    public void initialize() {
        setAppDataReference(AppData.getInstance());

    }
    public void actualizarDatos(String nombreGanador, String puntuacionGanador) {
        System.out.println(nombreGanador+puntuacionGanador);
            ganador.setText("El ganador es: "+nombreGanador);
            puntuacion.setText("La puntuacion es: "+puntuacionGanador);
    }

    public void setAppDataReference(AppData appData) {
        this.infoData = appData;
        appData.setLayoutGameOver(this);
    }
}
