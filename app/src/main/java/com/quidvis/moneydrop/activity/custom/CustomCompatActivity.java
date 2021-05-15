package com.quidvis.moneydrop.activity.custom;

import androidx.appcompat.app.AppCompatActivity;

import com.quidvis.moneydrop.interfaces.OnDestroyListener;
import com.quidvis.moneydrop.interfaces.OnPauseListener;
import com.quidvis.moneydrop.interfaces.OnRestartListener;
import com.quidvis.moneydrop.interfaces.OnResumeListener;
import com.quidvis.moneydrop.interfaces.OnStartListener;
import com.quidvis.moneydrop.interfaces.OnStopListener;

import java.util.HashMap;

public abstract class CustomCompatActivity extends AppCompatActivity {

    private final HashMap<Integer, OnStartListener> onStartListeners = new HashMap<>();
    private final HashMap<Integer, OnStopListener> onStopListeners = new HashMap<>();
    private final HashMap<Integer, OnPauseListener> onPauseListeners = new HashMap<>();
    private final HashMap<Integer, OnResumeListener> onResumeListeners = new HashMap<>();
    private final HashMap<Integer, OnRestartListener> onRestartListeners = new HashMap<>();
    private final HashMap<Integer, OnDestroyListener> onDestroyListeners = new HashMap<>();

    public void setOnStartActivityListener(int index, OnStartListener onStartListener) {
        this.onStartListeners.put(index, onStartListener);
    }

    public void setOnStopActivityListener(int index, OnStopListener onStopListener) {
        this.onStopListeners.put(index, onStopListener);
    }

    public void setOnPauseActivityListener(int index, OnPauseListener onPauseListener) {
        this.onPauseListeners.put(index, onPauseListener);
    }

    public void setOnResumeActivityListener(int index, OnResumeListener onResumeListener) {
        this.onResumeListeners.put(index, onResumeListener);
    }

    public void setOnRestartActivityListener(int index, OnRestartListener onRestartListener) {
        this.onRestartListeners.put(index, onRestartListener);
    }

    public void setOnDestroyActivityListener(int index, OnDestroyListener onDestroyListener) {
        this.onDestroyListeners.put(index, onDestroyListener);
    }

    public void removeOnStartActivityListener(int index) {
        this.onStartListeners.remove(index);
    }

    public void removeOnStopActivityListener(int index) {
        this.onStopListeners.remove(index);
    }

    public void removeOnPauseActivityListener(int index) {
        this.onPauseListeners.remove(index);
    }

    public void removeOnResumeActivityListener(int index) {
        this.onResumeListeners.remove(index);
    }

    public void removeOnDestroyActivityListener(int index) {
        this.onDestroyListeners.remove(index);
    }

    @Override
    protected void onStart() {
        super.onStart();
        for (java.util.Map.Entry<Integer, OnStartListener> entry : onStartListeners.entrySet())
            entry.getValue().onStart(entry.getKey());
    }

    @Override
    protected void onStop() {
        super.onStop();
        for (java.util.Map.Entry<Integer, OnStopListener> entry : onStopListeners.entrySet())
            entry.getValue().onStop(entry.getKey());
    }

    @Override
    protected void onPause() {
        super.onPause();
        for (java.util.Map.Entry<Integer, OnPauseListener> entry : onPauseListeners.entrySet())
            entry.getValue().onPause(entry.getKey());
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        for (java.util.Map.Entry<Integer, OnRestartListener> entry : onRestartListeners.entrySet())
            entry.getValue().onRestart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        for (java.util.Map.Entry<Integer, OnResumeListener> entry : onResumeListeners.entrySet())
            entry.getValue().onResume(entry.getKey());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        for (java.util.Map.Entry<Integer, OnDestroyListener> entry : onDestroyListeners.entrySet())
            entry.getValue().onDestroy(entry.getKey());
    }
}
