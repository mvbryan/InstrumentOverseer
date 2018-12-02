package com.overseer.nearbythings;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.ConnectionsClient;

import org.w3c.dom.Text;

/*
 * Copyright (C) 2018 Francesco Azzola
 *  Surviving with Android (https://www.survivingwithandroid.com)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private ConnectionsClient client;
    private String currentEndpoint;
    private NearbyDsvManager dsvManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "Starting Android Things app...");
        setContentView(R.layout.activity_main);
        TextView statusText = findViewById(R.id.textView_status);
        statusText.setText(getString(R.string.status, "Waiting to Search"));
        //NearbyDsvManager dsvManager = new NearbyDsvManager(this,listener);

        //Log.d(TAG,"Sending message...");
        //dsvManager.sendData("It WORKED!!!!");


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private NearbyDsvManager.EventListener listener = new NearbyDsvManager.EventListener() {
        @Override
        public void onDiscovered() {
            //Toast.makeText(MainActivity.this, "Endpoint discovered", Toast.LENGTH_LONG).show();
            Log.d(TAG, "Endpoint discovered");
        }

        @Override
        public void startDiscovering() {
           // Toast.makeText(MainActivity.this, "Start discovering...", Toast.LENGTH_LONG).show();
            Log.d(TAG, "Start discovering...");
        }

        @Override
        public void onConnected() {
          //  Toast.makeText(MainActivity.this, "Connected", Toast.LENGTH_LONG).show();
            Log.d(TAG,"Connected");

        }
    };

    /** Finds an hub using Nearby Connections. */
    public void startNearbyDsvManager(View view) {
        dsvManager = new NearbyDsvManager(this,listener);
        currentEndpoint = dsvManager.getCurrentEndpoint();
        findViewById(R.id.button_discover).setEnabled(false);
        findViewById(R.id.button_disconnect).setEnabled(true);

        //startDiscovering();
        //setStatusText(getString(R.string.status_searching));
        //findOpponentButton.setEnabled(false);
        Log.d(TAG,"Button pressed discovery started!");
    }

    public void disconnect(View view){
        //Nearby.getConnectionsClient(this).disconnectFromEndpoint(currentEndpoint);
    //    connectionsClient.disconnectFromEndpoint(opponentEndpointId);
        dsvManager.disconnect();
        findViewById(R.id.button_disconnect).setEnabled(false);
        findViewById(R.id.button_discover).setEnabled(true);
        Log.d(TAG,"Clicked the disconnected button and disconnected");
        resetStrings();
    }

    private void resetStrings(){
        TextView message = findViewById(R.id.textView_message);
        String m = "disconnected";
        message.setText(getString(R.string.message, m));
        TextView connected = findViewById(R.id.textView_connected);
        connected.setText(getString(R.string.connected_to,m));
        TextView status = findViewById(R.id.textView_status);
        status.setText(getString(R.string.status,m));
    }
}
