package com.softwaresupermacy.mqtttest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.os.Bundle;

import com.softwaresupermacy.mqtttest.databinding.ActivityMainBinding;



import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;

import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;


import timber.log.Timber;

public class MainActivity extends AppCompatActivity implements MqttCallback {

    private static final String TAG = "MainActivity";
    private ActivityMainBinding mBinding;
    private MqttClient mClient;
    private static final String DRAMA_TOPIC = "topic/drama";
    private static final String FOOD_TOPIC = "topic/food";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.plant(new Timber.DebugTree());
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);


        String clientid = MqttClient.generateClientId();


        mBinding.connectButton.setOnClickListener(x->{
            mBinding.connectButton.setClickable(false);
            String serverUri = "tcp://" +
                    mBinding.ipAddress.getText().toString() +
                    ":"+ mBinding.port.getText().toString();

            try {
                mClient = new MqttClient(
                        serverUri
                        , clientid, new MemoryPersistence());

                mClient.setCallback(this);
                mClient.connect();
                mClient.subscribe(FOOD_TOPIC);

            } catch (MqttException e) {
                e.printStackTrace();
                Timber.e(e);
            }
        });

        mBinding.sendMessagesButton.setOnClickListener(x->{
            if (mClient!= null && mClient.isConnected()){
                MqttMessage msg = new MqttMessage(mBinding.text.getText().toString().getBytes());
                try {
                    mClient.publish(DRAMA_TOPIC, msg);
                } catch (MqttException e) {
                    e.printStackTrace();
                    Timber.e(e);
                    Timber.e("Errore sending a message");
                }
            }
        });
    }

    @Override
    public void connectionLost(Throwable cause) {
        Timber.e("Connection LOST");
        Timber.e(cause);
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        Timber.d("Message arrived Topic : " + topic + "\n" +  "Message : " + message.toString());
        runOnUiThread(() ->
                mBinding.log.append(message.toString() + "\n"));
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        Timber.d("Deliver complete");
    }
}
