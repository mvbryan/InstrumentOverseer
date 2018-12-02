package com.overseer.hub.nearbythings;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.nearby.connection.ConnectionsClient;

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
    private NearbyAdvManager nearbyAdvManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "Starting Android Things app...");
        final LCDManager lcdManager = new LCDManager();
        setContentView(R.layout.activity_main);
        TextView statusText = findViewById(R.id.textView_status);
        statusText.setText(getString(R.string.status, "Waiting to Advertise"));

//      NearbyAdvManager advManager = new NearbyAdvManager(this, new NearbyAdvManager.EventListener() {
//          @Override
//          public void onMessage(String message) {
////            lcdManager.displayString(message);
//              Log.i(TAG, "Message was: " + message);
//          }
//      });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private NearbyAdvManager.EventListener listener = new NearbyAdvManager.EventListener() {
        @Override
        public void onMessage(String message) {
            Log.d(TAG, "Received message " + message);
        }
    };

        /** Finds an hub using Nearby Connections. */
    public void startNearbyAdvManager(View view) {
        nearbyAdvManager = new NearbyAdvManager(this,listener);
        findViewById(R.id.button_advertise).setEnabled(false);
        findViewById(R.id.button_disconnect).setEnabled(true);

        //startDiscovering();
        //setStatusText(getString(R.string.status_searching));
        //findOpponentButton.setEnabled(false);
        Log.d(TAG,"Button pressed advertising started!");
    }

    public void disconnect(View view){
        //Nearby.getConnectionsClient(this).disconnectFromEndpoint(currentEndpoint);
        //    connectionsClient.disconnectFromEndpoint(opponentEndpointId);
        nearbyAdvManager.disconnect();
        findViewById(R.id.button_disconnect).setEnabled(false);
        findViewById(R.id.button_advertise).setEnabled(true);
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
