package com.company.asynchr;



import com.company.BufferedObject;
import com.company.ITestObj;
import com.company.Pair;

import java.util.Random;

public class ConsumentAs extends Thread implements ITestObj{
    private static int MIN_TIME=1000*250;
    private static int  MAX_TIME=1000*1000;

    private long workTime=(MAX_TIME+MIN_TIME)/2;
    private long somethingElseTime=(MAX_TIME+MIN_TIME)/2;
    private long deviationTime=(MAX_TIME-MIN_TIME)/2;

    private MonitorAs mon;
    private int numerCons;
    private int iCounter;
    private long totalCounter=0;
    private boolean fWork=true;
    private int javastuuuupid;
    private boolean verbose=false;
    private Random rand=new Random(System.nanoTime());

    public ConsumentAs(MonitorAs mon, int numerCons){
        this.mon=mon;
        this.numerCons=numerCons;
    }
    public ConsumentAs(MonitorAs mon, int numerCons, long workTime, long somethingElseTime, long deviationTime ){
        this.workTime=workTime;
        this.somethingElseTime=somethingElseTime;
        this.deviationTime=deviationTime;
        this.mon=mon;
        this.numerCons=numerCons;
    }

    public ConsumentAs(MonitorAs mon, int numerCons, long workTime, long somethingElseTime, long deviationTime,boolean verbose ){
        this.workTime=workTime;
        this.somethingElseTime=somethingElseTime;
        this.deviationTime=deviationTime;
        this.mon=mon;
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
        Pair<Integer,BufferedObject> p;
        BufferedObject target;
        Integer ticket;



        p=mon.tryConsume();
        javastuuuupid=somethingElse();
        ticket=p.fst;
        target=p.snd;
        javastuuuupid=work();

        int data=target.get();
        if(verbose)
            System.out.println("consumed: " + data);
        iCounter=(iCounter+1)%1000;
        totalCounter++;


        mon.FinishedConsume(ticket);
    }



    private int work()
    {
        long time=rand.nextInt();
        if(time<0) time=-time;
        time = time% (deviationTime*2);
        time = time -deviationTime;
        time = time + workTime;
        long start=System.nanoTime();
        int hue=0;
        while(System.nanoTime()- start < time)
        {
            hue+=1;
        }
        return hue;

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
