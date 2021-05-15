package com.quidvis.moneydrop.fragment.custom;

import android.util.Log;

import androidx.fragment.app.Fragment;

import com.quidvis.moneydrop.interfaces.OnDestroyListener;
import com.quidvis.moneydrop.interfaces.OnPauseListener;
import com.quidvis.moneydrop.interfaces.OnResumeListener;
import com.quidvis.moneydrop.interfaces.OnStartListener;
import com.quidvis.moneydrop.interfaces.OnStopListener;

import java.util.HashMap;

public abstract class CustomCompatFragment extends Fragment {

    private final HashMap<Integer, OnStartListener> onStartListeners = new HashMap<>();
    private final HashMap<Integer, OnStopListener> onStopListeners = new HashMap<>();
    private final HashMap<Integer, OnPauseListener> onPauseListeners = new HashMap<>();
    private final HashMap<Integer, OnResumeListener> onResumeListeners = new HashMap<>();
    private final HashMap<Integer, OnDestroyListener> onDestroyListeners = new HashMap<>();

    public void setOnStartFragmentListener(int index, OnStartListener onStartListener) {
        this.onStartListeners.put(index, onStartListener);
    }

    public void setOnStopFragmentListener(int index, OnStopListener onStopListener) {
        this.onStopListeners.put(index, onStopListener);
    }

    public void setOnPauseFragmentListener(int index, OnPauseListener onPauseListener) {
        this.onPauseListeners.put(index, onPauseListener);
    }

    public void setOnResumeFragmentListener(int index, OnResumeListener onResumeListener) {
        this.onResumeListeners.put(index, onResumeListener);
    }

    public void setOnDestroyFragmentListener(int index, OnDestroyListener onDestroyListener) {
        this.onDestroyListeners.put(index, onDestroyListener);
    }

    public void removeOnStartFragmentListener(int index) {
        this.onStartListeners.remove(index);
    }

    public void removeOnStopFragmentListener(int index) {
        this.onStopListeners.remove(index);
    }

    public void removeOnPauseFragmentListener(int index) {
        this.onPauseListeners.remove(index);
    }

    public void removeOnResumeFragmentListener(int index) {
        this.onResumeListeners.remove(index);
    }

    public void removeOnDestroyFragmentListener(int index) {
        this.onDestroyListeners.remove(index);
    }

    @Override
    public void onStart() {
        super.onStart();
        for (java.util.Map.Entry<Integer, OnStartListener> entry : onStartListeners.entrySet())
            entry.getValue().onStart(entry.getKey());
    }

    @Override
    public void onStop() {
        super.onStop();
        for (java.util.Map.Entry<Integer, OnStopListener> entry : onStopListeners.entrySet())
            entry.getValue().onStop(entry.getKey());
    }

    @Override
    public void onPause() {
        super.onPause();
        for (java.util.Map.Entry<Integer, OnPauseListener> entry : onPauseListeners.entrySet())
            entry.getValue().onPause(entry.getKey());
    }

    @Override
    public void onResume() {
        super.onResume();
        for (java.util.Map.Entry<Integer, OnResumeListener> entry : onResumeListeners.entrySet())
            entry.getValue().onResume(entry.getKey());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        for (java.util.Map.Entry<Integer, OnDestroyListener> entry : onDestroyListeners.entrySet())
            entry.getValue().onDestroy(entry.getKey());
    }
}
