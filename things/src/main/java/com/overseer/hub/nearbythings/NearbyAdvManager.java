package com.overseer.hub.nearbythings;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AdvertisingOptions;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

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

public class NearbyAdvManager {

    private static final String TAG = "NearByManagerAdv";

    protected static final String SERVICE_ID = "com.overseer.hub.nearbythings";

    private Context ctx;
    private ConnectionsClient client;
    private EventListener listener;

    private String currentEndpoint;


    public NearbyAdvManager(Context ctx, EventListener listener) {
        Log.d(TAG, "Constructor..");
        this.ctx = ctx;
        this.listener = listener;
        client = Nearby.getConnectionsClient(ctx);
        client.startAdvertising("AndroidThings",
                SERVICE_ID,
                connectionLifeCycleCB,
                new AdvertisingOptions.Builder().setStrategy(Strategy.P2P_STAR).build())
                .addOnSuccessListener(
                        new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.i(TAG, "OnSuccess...");
                            }
                        }
                )
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "OnFailure 1");
                        e.printStackTrace();
                    }
                });
    }


    private ConnectionLifecycleCallback connectionLifeCycleCB = new ConnectionLifecycleCallback() {
        @Override
        public void onConnectionInitiated(String s, ConnectionInfo connectionInfo) {
            Log.i(TAG, "Connection initiated. Endpoint ["+s+"]");
            NearbyAdvManager.this.currentEndpoint = s;
            Log.i(TAG, "Incoming endpoint ["+connectionInfo.getEndpointName()+"]");
            Log.i(TAG, "Is incoming ["+connectionInfo.isIncomingConnection()+"]");
            // Let us accept the connection

            Nearby.getConnectionsClient(ctx)
                    .acceptConnection(s, payloadCallback );

        }

        @Override
        public void onConnectionResult(String s, ConnectionResolution connectionResolution) {
            Log.i(TAG, "Connection result. Endpoint ["+s+"]");
            setConnected(currentEndpoint);
            setStatus("Connected");
            Nearby.getConnectionsClient(ctx).stopAdvertising();

        }

        @Override
        public void onDisconnected(String s) {
            Log.i(TAG, "Disconnected. Endpoint ["+s+"]");

        };
    };


    private PayloadCallback payloadCallback = new PayloadCallback() {
        @Override
        public void onPayloadReceived(String s, Payload payload) {
            Log.i(TAG, "Payload received");
            byte[] b = payload.asBytes();
            String content = new String(b);
            Log.i(TAG, "Content ["+content+"]");
            listener.onMessage(content);
            setMessage(content);
        }

        @Override
        public void onPayloadTransferUpdate(String s, PayloadTransferUpdate payloadTransferUpdate) {
            Log.d(TAG, "Payload Transfer update ["+s+"]");
            if (payloadTransferUpdate.getStatus() == PayloadTransferUpdate.Status.SUCCESS) {

                Log.i(TAG, "Payload received from Endpoint ["+s+"]");
            }
        }
    };

    public interface EventListener {
        public void onMessage(String message);
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
}
