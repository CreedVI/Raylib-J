package com.raylib.java.rlgl.data;

import com.raylib.java.rlgl.rlRenderBatch;
import com.raylib.java.rlgl.vr.VR;

public class rlglData {

    rlRenderBatch currentBatch;              // Current render batch
    rlRenderBatch defaultBatch;               // Default internal render batch
    ExtSupported extSupported;
    State state;
    VR vr;

    public rlglData() {
        currentBatch = new rlRenderBatch();
        defaultBatch = new rlRenderBatch();
        extSupported = new ExtSupported();
        state = new State();
        vr = new VR();
    }

    public void setCurrentBatch(rlRenderBatch currentBatch) {
        this.currentBatch = currentBatch;
    }

    public void setDefaultBatch(rlRenderBatch defaultBatch) {
        this.defaultBatch = defaultBatch;
    }

    public rlRenderBatch getCurrentBatch() {
        return currentBatch;
    }

    public rlRenderBatch getDefaultBatch() {
        return defaultBatch;
    }

    public ExtSupported getExtSupported() {
        return extSupported;
    }

    public void setExtSupported(ExtSupported extSupported) {
        this.extSupported = extSupported;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public VR getVr() {
        return vr;
    }

    public void setVr(VR vr) {
        this.vr = vr;
    }
}
