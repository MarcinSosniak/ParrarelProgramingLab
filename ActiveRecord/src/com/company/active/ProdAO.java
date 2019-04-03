package com.company.active;

import java.util.Random;

public class ProdAO extends Thread{
    private static int MIN_TIME=1000*250;
    private static int  MAX_TIME=1000*1000;

    private long somethingElseTime=(MAX_TIME+MIN_TIME)/2;
    private long deviationTime=(MAX_TIME-MIN_TIME)/2;
    private Proxy proxy;
    private int numerProd;
    private int iCounter=0;
    private long totalCounter=0;
    private boolean fWork=true;
    private boolean verbose=false;
    private Random rand=new Random(System.nanoTime());

    public ProdAO(Proxy proxy, int numerProd, long somethingElseTime, long deviationTime ){
        this.somethingElseTime=somethingElseTime;
        this.deviationTime=deviationTime;
        this.proxy=proxy;
        this.numerProd=numerProd;
    }

    public ProdAO(Proxy proxy, int numerProd,  long somethingElseTime, long deviationTime,boolean verbose ){
        this.somethingElseTime=somethingElseTime;
        this.deviationTime=deviationTime;
        this.proxy=proxy;
        this.numerProd=numerProd;
        this.verbose=verbose;
    }

    public void setVerbose(boolean verbose)
    {
        this.verbose=verbose;
    }

    public ProdAO(Proxy proxy, int numerProd){
        this.proxy=proxy;
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
        //System.out.println("Before getin Fut Prod#"+numerProd);
        Future fut=proxy.produce(numerProd*1000+iCounter);
        //System.out.println("After getin Fut Prod#"+numerProd);
        somethingElse();
        int val=fut.getVal();
        if(verbose)
            System.out.println("produced: " + (numerProd*1000+iCounter) + " and got return of : " + val);
        iCounter=(iCounter+1)%1000;

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



}
