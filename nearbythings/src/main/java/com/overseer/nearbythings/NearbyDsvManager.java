package com.overseer.nearbythings;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.nearby.Nearby;

import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes;
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

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

public class NearbyDsvManager {

    private static final String TAG = "NearbyApp";

    protected static final String SERVICE_ID = "com.overseer.hub.nearbythings";
    protected static final Strategy STRATEGY = Strategy.P2P_STAR;

    private Context ctx;
    private EventListener listener;

    private String currentEndpoint;

    //private String status;
    //private String message;
    //private String connected;

    public NearbyDsvManager(Context ctx, final EventListener listener) {
        this.listener = listener;
        this.ctx = ctx;

        ((Activity)ctx).setContentView(R.layout.activity_main);

        Log.i(TAG, "NearbyDsvManager");
        Nearby.getConnectionsClient(ctx)
                .startDiscovery(SERVICE_ID,
                        endpointDiscoveryCB,
                        new DiscoveryOptions.Builder().setStrategy(Strategy.P2P_STAR).build())
                .addOnSuccessListener(
                        new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.i(TAG, "OnSuccess...");
                                listener.startDiscovering();
                            }
                        }
                )
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "OnFailure", e);
                        e.printStackTrace();
                    }
                });
    }


    private EndpointDiscoveryCallback endpointDiscoveryCB = new EndpointDiscoveryCallback() {
        @Override
        public void onEndpointFound(String s, DiscoveredEndpointInfo discoveredEndpointInfo) {
            Log.i(TAG, "Endpoint found ["+s+"]. Connecting....");
            listener.onDiscovered();
            getConnection(s);

        }

        @Override
        public void onEndpointLost(String s) {
            Log.e(TAG, "Endpoint lost ["+s+"]");
        }
    };


    private void getConnection(String endpointId) {
        Nearby.getConnectionsClient(ctx)
                .requestConnection(endpointId, endpointId,connectionLifecycleCallback)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Requesting connection..");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error requesting connection", e);
                    }
                });

    }

    private ConnectionLifecycleCallback connectionLifecycleCallback = new ConnectionLifecycleCallback() {
        @Override
        public void onConnectionInitiated(String s, ConnectionInfo connectionInfo) {
            Log.i(TAG, "Connected to endpoint ["+s+"]");
            NearbyDsvManager.this.currentEndpoint = s;
            Nearby.getConnectionsClient(ctx).acceptConnection(s, payloadCallback);


        }

        @Override
        public void onConnectionResult(String s, ConnectionResolution connectionResolution) {
            switch (connectionResolution.getStatus().getStatusCode()) {
                case ConnectionsStatusCodes.STATUS_OK:
                    listener.onConnected();
                    setConnected(currentEndpoint);
                    setStatus("Connected");
                    Nearby.getConnectionsClient(ctx).stopDiscovery();
                    sendData("REAL MESSAGE HERE!!!");
                    break;
                case ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED:
                    Log.i(TAG, "Connection rejected");
                    break;
                case ConnectionsStatusCodes.STATUS_ERROR:
                    Log.i(TAG, "Connection error");
                    break;
            }
        }

        @Override
        public void onDisconnected(String s) {

        }
    };

    private PayloadCallback payloadCallback = new PayloadCallback() {
        @Override
        public void onPayloadReceived(String s, Payload payload) {
            Log.i(TAG, "Payload received");
            byte[] b = payload.asBytes();
            String content = new String(b);
            Log.i(TAG, "Content ["+content+"]");
        }

        @Override
        public void onPayloadTransferUpdate(String s, PayloadTransferUpdate payloadTransferUpdate) {
            Log.d(TAG, "Payload Transfer update ["+s+"]");
            if (payloadTransferUpdate.getStatus() == PayloadTransferUpdate.Status.SUCCESS) {

                Log.i(TAG, "Payload received from Endpoint ["+s+"]");
                setMessage("Message Sent");
            }
        }
    };


    public void sendData(String data) {
        Log.i(TAG, "Sending data ["+data+"]");
        Log.i(TAG, "Current endpoint ["+currentEndpoint+"]");
        if (currentEndpoint != null) {
            Log.d(TAG, "Sending data to ["+currentEndpoint+"]");
            Payload payload = Payload.fromBytes(data.getBytes());
            Nearby.getConnectionsClient(ctx).sendPayload(currentEndpoint, payload);
        }
    }

    public interface EventListener {
         void onDiscovered();
         void startDiscovering();
         void onConnected();
    }

    /** Updates the opponent name on the UI. */
    private void setConnected(String currentEndpoint) {
        TextView connected = ((Activity)ctx).findViewById(R.id.textView_connected);
        connected.setText(ctx.getString(R.string.connected_to, currentEndpoint));

    }

    /**Update status on the UI. */
    private void setStatus(String currentStatus) {
        TextView status = ((Activity)ctx).findViewById(R.id.textView_status);
        status.setText(ctx.getString(R.string.status, currentStatus));
    }

    /**Update message on the UI. */
    private void setMessage(String currentMessage) {
        TextView message = ((Activity)ctx).findViewById(R.id.textView_message);
        message.setText(ctx.getString(R.string.message, currentMessage));
    }

    public void disconnect(){
        Nearby.getConnectionsClient(ctx).disconnectFromEndpoint(currentEndpoint);
    }

    public String getCurrentEndpoint(){
        return this.currentEndpoint;
    }

}
