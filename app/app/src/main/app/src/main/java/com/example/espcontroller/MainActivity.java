package com.example.espcontroller;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends Activity {

    private TextView status;
    private volatile boolean running = true;

    // ESP8266 controller address
    private static final String ESP_URL =
            "http://192.168.4.1/controller";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        status = new TextView(this);
        status.setText("Connecting to ESP8266...");
        status.setTextSize(38);
        status.setTextColor(Color.WHITE);
        status.setBackgroundColor(Color.rgb(20, 20, 20));
        status.setGravity(Gravity.CENTER);

        setContentView(status);

        startReceiver();
    }

    private void startReceiver() {

        new Thread(() -> {

            while (running) {

                HttpURLConnection connection = null;

                try {
                    URL url = new URL(ESP_URL);

                    connection =
                            (HttpURLConnection) url.openConnection();

                    connection.setConnectTimeout(1000);
                    connection.setReadTimeout(1000);
                    connection.setRequestMethod("GET");

                    BufferedReader reader =
                            new BufferedReader(
                                    new InputStreamReader(
                                            connection.getInputStream()
                                    )
                            );

                    String command = reader.readLine();

                    reader.close();

                    if (command == null) {
                        command = "NONE";
                    }

                    final String finalCommand =
                            command.trim();

                    runOnUiThread(() ->
                            showCommand(finalCommand)
                    );

                } catch (Exception e) {

                    runOnUiThread(() ->
                            status.setText(
                                    "ESP8266 NOT CONNECTED"
                            )
                    );

                } finally {

                    if (connection != null) {
                        connection.disconnect();
                    }
                }

                try {
                    Thread.sleep(60);
                } catch (InterruptedException ignored) {
                }
            }

        }).start();
    }

    private void showCommand(String command) {

        switch (command) {

            case "UP":
                status.setText("↑  UP");
                break;

            case "DOWN":
                status.setText("↓  DOWN");
                break;

            case "LEFT":
                status.setText("←  LEFT");
                break;

            case "RIGHT":
                status.setText("→  RIGHT");
                break;

            default:
                status.setText("READY");
                break;
        }
    }

    @Override
    protected void onDestroy() {
        running = false;
        super.onDestroy();
    }
}
