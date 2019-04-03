package sample;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MonitorPorcje {
    private static int BUFFOR_DEAULT_SIZE=202;
    private int[] iBuff;
    private int iCount;
    private int iReaderPoint;
    private int iWriterPoint;
    private boolean hasAnyProdWaiters=false;
    private boolean hasAnyConsWaiters=false;
    private int iSize;
    private ReentrantLock lock;
    private Condition firstProducer;
    private Condition firstConsumer;
    private Condition producentCond;
    private Condition consumerCond;

    public MonitorPorcje()
    {
        iSize=BUFFOR_DEAULT_SIZE;
        iBuff=new int[iSize];
        iCount=0;
        iReaderPoint=0;
        iWriterPoint=0;
        lock=new ReentrantLock();
        producentCond=lock.newCondition();
        consumerCond=lock.newCondition();
        firstProducer=lock.newCondition();
        firstConsumer=lock.newCondition();
    }


    private void addOne(int data)
    {
        iBuff[iWriterPoint]=data;
        iWriterPoint=(iWriterPoint+1)%iSize;
        iCount=iCount+1;
    }

    private int readOne()
    {
        int out;
        out=iBuff[iReaderPoint];
        iReaderPoint=(iReaderPoint+1)%iSize;
        iCount=iCount-1;
        return out;
    }

    public boolean produce(int iArray[])
    {
        if(iArray.length * 2 > iSize ) return false; // checking conditions of buffors
        if(iArray.length <= 0) return true;
        try
        {
            lock.lock();
            // Sprawidzic jak procesy
            //

            //!!!!!!!!!!!! \/ tutaj jestem zmuszony przyznac racje....
            if(lock.hasWaiters(producentCond)) // inne rozwiazanie niz ze ze mienna
                try{producentCond.await();}catch(Exception e) {e.printStackTrace();}

            while(hasAnyProdWaiters)
            {
                try{producentCond.await();}catch(Exception e) {e.printStackTrace();}
            }

            hasAnyProdWaiters=true;
            while (iCount + iArray.length > iSize)
            {
                try{firstProducer.await();}catch(Exception e) {e.printStackTrace();}
            }

            for (int e : iArray)
            {
                addOne(e);
            }

            producentCond.signal();
            firstConsumer.signal();
            hasAnyProdWaiters=false;
        }
        finally {
            lock.unlock();
        }
        return true;
    }

    public int[] consume(int amount)
    {

        if(amount * 2 > iSize ) return null;
        if(amount <= 0) return null;
        int[] outArray= new int[amount];
        try
        {
            lock.lock();
            //!!!!!!!!!!!! \/ tutaj jestem zmuszony przyznac racje....
            if (lock.hasWaiters(consumerCond))
                try{consumerCond.await();}catch(Exception e) {e.printStackTrace();}

            while (hasAnyConsWaiters)
            {
                try{consumerCond.await();}catch(Exception e) {e.printStackTrace();}
            }
            hasAnyConsWaiters=true;
            //System.out.println("trying to eat "+ amount + " of data, but there is only "+ iCount);
            while(iCount < amount)
            {
                try{firstConsumer.await();}catch(Exception e) {e.printStackTrace();}
            }

            for (int i=0; i<amount;i++)
            {
                outArray[i]=readOne();
            }
            hasAnyConsWaiters=false;
            consumerCond.signal();
            firstProducer.signal();
        }
        finally {
            lock.unlock();
        }
        return outArray;
    }





}
