package com.company;

public class BufferedObject {
    private int myInt=0;
    public BufferedObject set(int in)
    {
        myInt=in;
        return  this;
    }
    public int get()
    {
        return myInt;
    }

    public BufferedObject copy()
    {
        BufferedObject out = new BufferedObject();
        out.myInt=this.myInt;
        return out;
    }
}
