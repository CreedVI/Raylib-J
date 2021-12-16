package com.raylib.java.core;

public class Time{

    double current;                     // Current time measure
    double previous;                    // Previous time measure
    double update;                      // Time measure for frame update
    double draw;                        // Time measure for frame draw
    double frame;                       // Time measure for one frame
    double target;                      // Desired time for one frame, if 0 not applied

    int frameCounter;          // Frame counter

    public Time(){

    }

    public  double getCurrent(){
        return current;
    }

    public  void setCurrent(double current){
        this.current = current;
    }

    public  double getPrevious(){
        return previous;
    }

    public void setPrevious(double previous){
        this.previous = previous;
    }

    public  double getUpdate(){
        return update;
    }

    public  void setUpdate(double update){
        this.update = update;
    }

    public  double getDraw(){
        return draw;
    }

    public  void setDraw(double draw){
        this.draw = draw;
    }

    public  double getFrame(){
        return frame;
    }

    public  void setFrame(double frame){
        this.frame = frame;
    }

    public  double getTarget(){
        return target;
    }

    public  void setTarget(double target){
        this.target = target;
    }

}
