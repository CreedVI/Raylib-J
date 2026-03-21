package com.raylib.java.core;

public class Time{

    /**
     * Current time measure
     */
    double current;

    /**
     *  Previous time measure
     */
    double previous;

    /**
     * Time measure for frame update
     */
    double update;

    /**
     * Time measure for frame draw
     */
    double draw;

    /**
     * Time measure for one frame
     */
    double frame;

    /**
     * Desired time for one frame, if 0 not applied
     */
    double target;

    /**
     * Frame counter
     */
    int frameCounter;

    public Time(){

    }

    public double getCurrent(){
        return current;
    }

    public void setCurrent(double current){
        this.current = current;
    }

    public double getPrevious(){
        return previous;
    }

    public void setPrevious(double previous){
        this.previous = previous;
    }

    public double getUpdate(){
        return update;
    }

    public void setUpdate(double update){
        this.update = update;
    }

    public double getDraw(){
        return draw;
    }

    public void setDraw(double draw){
        this.draw = draw;
    }

    public double getFrame(){
        return frame;
    }

    public void setFrame(double frame){
        this.frame = frame;
    }

    public double getTarget(){
        return target;
    }

    public void setTarget(double target){
        this.target = target;
    }

}
