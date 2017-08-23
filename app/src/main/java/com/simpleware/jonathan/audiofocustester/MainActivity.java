package com.simpleware.jonathan.audiofocustester;

import android.media.AudioManager;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatCheckBox;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    AppCompatCheckBox checkBox;
    TextView textView;
    AudioManager manager;
    Spinner spinner;
    Handler handler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        handler = new Handler();
        checkBox = (AppCompatCheckBox) findViewById(R.id.chk_box);
        textView = (TextView) findViewById(R.id.txtVw);
        spinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.focus_states, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        manager = (AudioManager) getSystemService(AUDIO_SERVICE);
    }

    public void register(View view) {
        if(checkBox.isChecked()) {
            new Thread(registerRunnable).start();
        }
        else {
            int result = manager.requestAudioFocus(mFocusChangeListener, AudioManager.STREAM_MUSIC, getFocusState());
            textView.setText("Request using UI thread, result = " + (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED ? "GRANTED": "DENIED"));
        }
    }

    public void unregister(View view) {
        if(checkBox.isChecked()) {
            new Thread(unregisterRunnable).start();
        }
        else {
            int result = manager.abandonAudioFocus(mFocusChangeListener);
            textView.setText("Abandon audio focus using UI thread, result = " + (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED ? "GRANTED": "DENIED"));
        }
    }

    private int getFocusState() {
        switch ((String) spinner.getSelectedItem()) {
            case "AUDIOFOCUS_GAIN": return AudioManager.AUDIOFOCUS_GAIN;
            case "AUDIOFOCUS_GAIN_TRANSIENT": return AudioManager.AUDIOFOCUS_GAIN_TRANSIENT;
            case "AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK": return AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK;
            case "AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE": return AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE;
        }
        return 0;
    }

    private String getFocusState(int state) {
        switch (state) {
            case AudioManager.AUDIOFOCUS_GAIN: return "AUDIOFOCUS_GAIN";
            case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT: return "AUDIOFOCUS_GAIN_TRANSIENT";
            case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK: return "AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK";
            case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE: return "AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE";
        }
        return "AUDIOFOCUS_NONE";
    }

    AudioManager.OnAudioFocusChangeListener mFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            handler.post(focusChangeRunnable.setMessage("OnAudioFocusChangeListener fired, focusChange = " + getFocusState(focusChange)));
        }
    };

    Runnable registerRunnable = new Runnable() {
        @Override
        public void run() {
            final int result = manager.requestAudioFocus(mFocusChangeListener, AudioManager.STREAM_MUSIC, getFocusState());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    textView.setText("Request using Worker thread, result = " + (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED ? "GRANTED": "DENIED"));
                }
            });
        }
    };

    Runnable unregisterRunnable = new Runnable() {
        @Override
        public void run() {
            final int result = manager.abandonAudioFocus(mFocusChangeListener);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    textView.setText("Abandon audio focus using Wortker thread, result = " + (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED ? "GRANTED": "DENIED"));
                }
            });
        }
    };

    MessageRunnable focusChangeRunnable = new MessageRunnable() {
        @Override
        public void run() {
            Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
        }
    };

    public static abstract class MessageRunnable implements Runnable {

        String message;

        Runnable setMessage(String message) {
            this.message = message;
            return this;
        }
    }
}
