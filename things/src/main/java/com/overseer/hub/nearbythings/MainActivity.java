package com.overseer.hub.nearbythings;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.google.android.things.iotcore.ConnectionParams;
import com.google.android.things.iotcore.IotCoreClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

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

    private PrivateKey privateKey;
    private PublicKey publicKey;
    private IotCoreClient coreClient;

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

        String fileName = "/sdcard/keys/rsa_private_pkcs8";
        String pubFileName = "/sdcard/keys/public_key.der";
        try {
            byte[] keyBytes = Files.readAllBytes(Paths.get(fileName));
            PKCS8EncodedKeySpec priSpec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            privateKey = keyFactory.generatePrivate(priSpec);
            byte[] pubKeyBytes = Files.readAllBytes(Paths.get(pubFileName));
            X509EncodedKeySpec pubSpec = new X509EncodedKeySpec(pubKeyBytes);
            publicKey = keyFactory.generatePublic(pubSpec);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }

        KeyPair keys = new KeyPair(publicKey, privateKey);
        ConnectionParams connectionParams = new ConnectionParams.Builder()
                .setProjectId("wsn-musicmonitor")
                .setRegistry("instrumentoverseer", "us-central1")
                .setDeviceId("instrumenthub")
                .build();

        coreClient = new IotCoreClient.Builder()
                .setConnectionParams(connectionParams)
                .setKeyPair(keys)
                .build();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        coreClient.disconnect();
    }

    private NearbyAdvManager.EventListener listener = new NearbyAdvManager.EventListener() {
        @Override
        public void onMessage(String message) {
            Log.d(TAG, "Received message " + message);
        }
    };

        /** Finds an hub using Nearby Connections. */
    public void startNearbyAdvManager(View view) {
        nearbyAdvManager = new NearbyAdvManager(this,listener, coreClient);
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
