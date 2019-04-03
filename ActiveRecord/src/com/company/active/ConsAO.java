package com.company.active;

import java.util.Random;

public class ConsAO extends Thread {
    private static int MIN_TIME=1000*250;
    private static int  MAX_TIME=1000*1000;


    private long somethingElseTime=(MAX_TIME+MIN_TIME)/2;
    private long deviationTime=(MAX_TIME-MIN_TIME)/2;

    private Proxy proxy;
    private int numerCons;
    private int iCounter;
    private long totalCounter=0;
    private boolean fWork=true;
    private int javastuuuupid;
    private boolean verbose=false;
    private Random rand=new Random(System.nanoTime());

    public ConsAO(Proxy proxy, int numerCons){
        this.proxy=proxy;
        this.numerCons=numerCons;
    }
    public ConsAO(Proxy proxy, int numerCons,  long somethingElseTime, long deviationTime ){

        this.somethingElseTime=somethingElseTime;
        this.deviationTime=deviationTime;
        this.proxy=proxy;
        this.numerCons=numerCons;
    }

    public ConsAO(Proxy proxy, int numerCons, long somethingElseTime, long deviationTime,boolean verbose ){

        this.somethingElseTime=somethingElseTime;
        this.deviationTime=deviationTime;
        this.proxy=proxy;
        this.numerCons=numerCons;
        this.verbose=verbose;
    }

    public void  end()
    {
        fWork=false;
    }

    public void sayGoodbye()
    {
        System.out.println("Consumer ended consumed " + totalCounter + " times s" );
    }

    public long timesDone()
    {
        return totalCounter;
    }

    public void run(){
        while(fWork)
        {
            consume();
        }
    }

    void consume()
    {
        Future fut=proxy.consume();
        javastuuuupid=somethingElse();
        int data=fut.getVal();
        if(verbose)
            System.out.println("consumed: " + data);
        iCounter=(iCounter+1)%1000;
        totalCounter++;


    }




    private int somethingElse()
    {
        long time=rand.nextInt();
        if(time<0) time=-time;
        time = time% (deviationTime*2);
        time = time -deviationTime;
        time = time + somethingElseTime;
        long start=System.nanoTime();
        int hue=0;
        while(System.nanoTime()- start < time)
        {
            hue+=1;
        }
        return hue;
    }

    public void setVerbose(boolean verbose)
    {
        this.verbose=verbose;
    }


    int think()
    {
        int time=rand.nextInt();
        if (time<0) time=-time;
        time=time%(MAX_TIME-MIN_TIME);
        time = time + MIN_TIME;
        long start=System.nanoTime();
        int hue=0;
        while(System.nanoTime()- start < time)
        {
            hue+=1;
        }
        return hue;
    }
}
