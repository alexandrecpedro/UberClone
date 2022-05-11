package com.app.uber.helper;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import java.text.DecimalFormat;

public class Local {
    /** Calcula as distâncias entre:
     * (a) motorista e passageiro
     * (b) passageiro e destino final, após motorista pegar o passageiro **/
    public static float calcularDistancia(LatLng latLngInicial, LatLng latLngFinal){

        Location localInicial = new Location("Local inicial");
        localInicial.setLatitude(latLngInicial.latitude);
        localInicial.setLongitude(latLngInicial.longitude);

        Location localFinal = new Location("Local final");
        localFinal.setLatitude(latLngFinal.latitude);
        localFinal.setLongitude(latLngFinal.longitude);

        //Calcula distancia - Resultado em Metros
        // dividir por 1000 para converter em KM
        float distancia = localInicial.distanceTo(localFinal) / 1000;

        return distancia;
    }

    /** Converter a distância para metros (m) e quilômetros (km) **/
    public static String formatarDistancia(float distancia) {

        String distanciaFormatada;
        // testando se a distância é menor que 1 KM, retornar em metros
        if (distancia < 1) {
            distancia = distancia * 1000;//em Metros
            distanciaFormatada = Math.round(distancia) + " M ";
        } else {
            DecimalFormat decimal = new DecimalFormat("0.0");
            distanciaFormatada = decimal.format(distancia) + " KM ";
        }

        return distanciaFormatada;
    }
}
