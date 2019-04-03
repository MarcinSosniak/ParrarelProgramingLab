package com.company.asynchr;


import com.company.BufferedObject;
import com.company.ITestObj;
import com.company.Pair;

import java.util.Random;

public class ProducentAs extends Thread implements ITestObj {
    private static int MIN_TIME=1000*250;
    private static int  MAX_TIME=1000*1000;

    private long workTime=(MAX_TIME+MIN_TIME)/2;
    private long somethingElseTime=(MAX_TIME+MIN_TIME)/2;
    private long deviationTime=(MAX_TIME-MIN_TIME)/2;
    private MonitorAs mon;
    private int numerProd;
    private int iCounter=0;
    private long totalCounter=0;
    private boolean fWork=true;
    private boolean verbose=false;
    private Random rand=new Random(System.nanoTime());

    public ProducentAs(MonitorAs mon, int numerProd, long workTime, long somethingElseTime, long deviationTime ){
        this.workTime=workTime;
        this.somethingElseTime=somethingElseTime;
        this.deviationTime=deviationTime;
        this.mon=mon;
        this.numerProd=numerProd;
    }

    public ProducentAs(MonitorAs mon, int numerProd, long workTime, long somethingElseTime, long deviationTime,boolean verbose ){
        this.workTime=workTime;
        this.somethingElseTime=somethingElseTime;
        this.deviationTime=deviationTime;
        this.mon=mon;
        this.numerProd=numerProd;
        this.verbose=verbose;
    }

    public void setVerbose(boolean verbose)
    {
        this.verbose=verbose;
    }

    public ProducentAs(MonitorAs mon, int numerProd){
        this.mon=mon;
        this.numerProd=numerProd;
    }

    public void run(){
        while(fWork)
        {
            produce();
            totalCounter++;
        }
    }

    public void  end()
    {
        fWork=false;
    }

    public void sayGoodbye()
    {
        System.out.println("Producent ended produced " + totalCounter + " times s" );
    }

    public long timesDone()
    {
        return totalCounter;
    }


    public void produce()
    {
        Pair<Integer,BufferedObject> p;
        BufferedObject target;
        Integer ticket;



        p=mon.tryProduce();
        somethingElse();
        work();
        ticket=p.fst;
        target=p.snd;
        think();
        //System.out.println("producent got out as: " + target);
        target.set(numerProd*1000+iCounter);
        if(verbose)
            System.out.println("produced: " + (numerProd*1000+iCounter));

        iCounter=(iCounter+1)%1000;
        mon.FinishedProduce(ticket);
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




    private int think()
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
