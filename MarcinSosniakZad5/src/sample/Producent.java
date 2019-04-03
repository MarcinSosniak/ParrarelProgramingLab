package sample;

import java.util.Random;

public class Producent extends Thread
{
    private int iAverage=50;
    private int iMaxDeviation=50;
    private boolean work=true;
    private boolean special=false;
    private long startTime;
    private long producesTimes=0;
    private Random rand= new Random();
    private MonitorPorcje mon;

    public Producent(MonitorPorcje mon)
    {
        this.mon=mon;
    }

    public Producent(MonitorPorcje mon, int iAverage, int iMaxDeviation)
    {
        special=true;
        this.mon=mon;
        this.iAverage=iAverage;
        this.iMaxDeviation=iMaxDeviation;
    }

    public void sayGoodbye()
    {
        long totalTime=startTime-System.nanoTime();
        if(special)
        {
//            System.out.printf("Special Producent finished: average=%d, maxDeviation=%d, timesProduced=%d, totaltime=%d, timeperProduct=%f\n",
//                                                           iAverage,iMaxDeviation,producesTimes,totalTime,((double)totalTime) / producesTimes  );
            System.out.printf("Special Producent finished: average=%d, maxDeviation=%d, timesProduced=%d\n",iAverage,iMaxDeviation,producesTimes);
        }
        else
        {
//            System.out.printf("Standard Producent finished: , timesProduced=%d, totaltime=%d, timeperProduct=%f\n",
//                   producesTimes,totalTime,((double)totalTime) / producesTimes  );
            System.out.printf("Standard Producent finished: timesProduced=%d\n",producesTimes);
        }
    }

    public void end()
    {
        work=false;
    }



    public void run()
    {
        System.out.println("producer Ready");
        startTime=System.nanoTime();
        while(work )produce();
    }

    private int getRandomInt()
    {
        int randInt=rand.nextInt()%(iMaxDeviation*2);
        if (randInt < 0) randInt=-randInt;
        return iAverage + randInt-iMaxDeviation+1;
    }


    private void produce()
    {

        int iAmount=getRandomInt();
        int[] iData= new int[iAmount];
        for (int i=0;i<iAmount;i++)
        {
            iData[i]=rand.nextInt();
        }
        mon.produce(iData);
        //System.out.println("produced " + iAmount + " of  data");
        producesTimes=producesTimes+1;
    }
}
