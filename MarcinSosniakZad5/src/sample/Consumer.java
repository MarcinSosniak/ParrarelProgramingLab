package sample;

import java.util.Random;

public class Consumer extends Thread
{
    private int iMaxDeviation=50;
    private int iAverage=50;
    private boolean work=true;
    private boolean special=false;
    private long startTime;
    private long cosnumedTimes=0;
    private Random rand= new Random();
    private MonitorPorcje mon;

    public Consumer(MonitorPorcje mon)
    {
        this.mon=mon;
    }

    public Consumer(MonitorPorcje mon, int iAverage, int iMaxDeviation)
    {
        special=true;
        this.mon=mon;
        this.iAverage=iAverage;
        this.iMaxDeviation=iMaxDeviation;
    }


    private int getRandomInt()
    {
        int randInt=rand.nextInt()%(iMaxDeviation*2);
        if (randInt < 0) randInt=-randInt;
        return iAverage + randInt-iMaxDeviation+1;
    }



    public void sayGoodbye()
    {
        long totalTime=startTime-System.nanoTime();
        if(special)
        {
//            System.out.printf("Special Consument finished: average=%d, maxDeviation=%d, timesProduced=%d, totaltime=%d, timeperProduct=%f\n",
//                    iAverage,iMaxDeviation,cosnumedTimes,totalTime,((double)totalTime) / cosnumedTimes  );
            System.out.printf("Special Consument finished: average=%d, maxDeviation=%d, timesProduced=%d\n",iAverage,iMaxDeviation,cosnumedTimes);
        }
        else
        {
//            System.out.printf("Standard Consument finished: , timesProduced=%d, totaltime=%d, timeperProduct=%f\n",
//                    cosnumedTimes,totalTime,((double)totalTime) / cosnumedTimes  );
            System.out.printf("Standard Consument finished: consumedTimes %d\n",cosnumedTimes);
        }
    }

    public void end()
    {
        work=false;
    }


    public void run()
    {
        System.out.println("consumer Ready");
        startTime=System.nanoTime();
        while(work) consume();
    }
    private void consume()
    {
        int iAmount=getRandomInt();
        int[] iData;
        iData=mon.consume(iAmount);
        //System.out.println("consumend " + iAmount + " of  data");
        cosnumedTimes= cosnumedTimes+1;
    }
}
