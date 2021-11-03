package com.raylib.java.rlgl.data;

import com.raylib.java.rlgl.RenderBatch;
import com.raylib.java.rlgl.vr.VR;

public class rlglData {

    RenderBatch currentBatch;              // Current render batch
    RenderBatch defaultBatch;               // Default internal render batch
    ExtSupported extSupported;
    State state;
    VR vr;

    public rlglData(){
        currentBatch = new RenderBatch();
        defaultBatch = new RenderBatch();
        extSupported = new ExtSupported();
        state = new State();
        vr = new VR();
    }

    public void setCurrentBatch(RenderBatch currentBatch){
        this.currentBatch = currentBatch;
    }

    public void setDefaultBatch(RenderBatch defaultBatch){
        this.defaultBatch = defaultBatch;
    }

    public RenderBatch getCurrentBatch(){
        return currentBatch;
    }

    public RenderBatch getDefaultBatch(){
        return defaultBatch;
    }

    public ExtSupported getExtSupported(){
        return extSupported;
    }

    public void setExtSupported(ExtSupported extSupported){
        this.extSupported = extSupported;
    }

    public State getState(){
        return state;
    }

    public void setState(State state){
        this.state = state;
    }

    public VR getVr(){
        return vr;
    }

    public void setVr(VR vr){
        this.vr = vr;
    }
}
