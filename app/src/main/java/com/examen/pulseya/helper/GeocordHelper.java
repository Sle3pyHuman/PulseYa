package com.examen.pulseya.helper;

import android.os.AsyncTask;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class GeocordHelper {

    public static void getCoordenadasAtravesDireccion(String address, String apiKey, GeocodeCallback callback) {
        new GetCoordenadas(callback).execute(address, apiKey);
    }

    private static class GetCoordenadas extends AsyncTask<String, Void, String[]> {

        private GeocodeCallback callback;

        public GetCoordenadas(GeocodeCallback callback) {
            this.callback = callback;
        }

        @Override
        protected String[] doInBackground(String... params) {
            String address = params[0];
            String apiKey = params[1];

            String latitude = "";
            String longitude = "";

            try {
                String urlString = "https://maps.googleapis.com/maps/api/geocode/json?address=" + address + "&key=" + apiKey;
                URL url = new URL(urlString);
                HttpURLConnection conneccion = (HttpURLConnection) url.openConnection();
                conneccion.setRequestMethod("GET");

                BufferedReader lector = new BufferedReader(new InputStreamReader(conneccion.getInputStream()));
                StringBuilder respuesta = new StringBuilder();
                String line;
                while ((line = lector.readLine()) != null) {
                    respuesta.append(line);
                }

                // Parse the JSON response
                JSONObject jsonResponse = new JSONObject(respuesta.toString());
                if (jsonResponse.getJSONArray("results").length() > 0) {
                    JSONObject location = jsonResponse.getJSONArray("results").getJSONObject(0)
                            .getJSONObject("geometry").getJSONObject("location");
                    latitude = location.getString("lat");
                    longitude = location.getString("lng");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return new String[]{latitude, longitude};
        }

        @Override
        protected void onPostExecute(String[] coordinates) {
            super.onPostExecute(coordinates);
            if (coordinates.length > 0) {
                callback.onCoordinatesFetched(coordinates[0], coordinates[1]);
            }
        }
    }

    public interface GeocodeCallback {
        void onCoordinatesFetched(String latitude, String longitude);
    }
}
