package com.company.active;

import com.company.BufforClass;

import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Proxy extends Thread{
    private BufforClass buff;
    private long workTime;
    private long deviationTime;
    private boolean fLive=true;
    private boolean fWork=true;
    private Random rand=new Random(System.nanoTime());
    private ReentrantLock lock= new ReentrantLock();
    private Condition schedulerCond = lock.newCondition();
    LinkedList<Task> readTaskQue=new LinkedList<Task>();
    LinkedList<Task> writeTaskQue=new LinkedList<Task>();

    public Proxy(int n,long workTime,long deviationTime)
    {
        this.workTime=workTime;
        buff= new BufforClass(n);
        this.deviationTime=deviationTime;
    }

    public void run()
    {
        schedule();
    }

//    private void schedule()
//    {
//        boolean fAnyPossibilities=false;
//        while (fWork)
//        {
////            if(!readTaskQue.isEmpty())
////                System.out.print("readTaskQue not empty and Guard is= " +readTaskQue.peekFirst().guard() +" ; ");
////            else
////                System.out.print("writeTaskQue empty ; ");
////            if(!writeTaskQue.isEmpty())
////                System.out.print("writeTaskQue not empty and Guard is= " +writeTaskQue.peekFirst().guard() +"\n");
////            else
////                System.out.print("readTaskQue empty\n");
//            if(!readTaskQue.isEmpty())
//            {
//                if(readTaskQue.peekFirst()==null)
//                {
//                    readTaskQue.removeFirst();
//                }
//                else if(readTaskQue.peekFirst().guard())
//                {
//                    //System.out.println("Scheduled read");
//                    fAnyPossibilities=true;
//                    readTaskQue.removeFirst().execute();
//                    work();
//                }
//            }
//            if(!writeTaskQue.isEmpty() )
//            {
//                if(writeTaskQue.peekFirst()==null)
//                {
//                    writeTaskQue.removeFirst();
//                }
//                else if(writeTaskQue.peekFirst().guard())
//                {
//                    //System.out.println("Scheduled write");
//                    fAnyPossibilities = true;
//                    writeTaskQue.removeFirst().execute();
//                    work();
//                }
//            }
//            //work();
//            if(!fAnyPossibilities)
//            {
//                try
//                {
//                    lock.lock();
//                    try{schedulerCond.await();} catch(Exception e) {;}
//                }
//                finally {
//                    lock.unlock();
//                }
//            }
//            fAnyPossibilities=false;
//        }
//    }
//
//    public Future produce(int val)
//    {
//        try
//        {
//            lock.lock();
//            Future out = new Future();
//            Task task = new Task(CONST.TASK_WRITE, out, val,buff);
//            writeTaskQue.addLast(task);
//            schedulerCond.signal();
//            return out;
//        }
//        finally {
//            lock.unlock();
//        }
//    }
//
//    public Future consume()
//    {
//        try
//        {
//            lock.lock();
//            Future out = new Future();
//            Task task = new Task(CONST.TASK_READ, out,buff);
//            readTaskQue.addLast(task);
//            schedulerCond.signal();
//            return out;
//        }
//        finally {
//            lock.unlock();
//        }
//    }

    public Future produce(int val)
    {
        try
        {
            lock.lock();
            Future out = new Future();
            Task task = new Task(CONST.TASK_WRITE, out, val,buff);
            writeTaskQue.addLast(task);
            schedulerCond.signal();
            return out;
        }
        finally {
            lock.unlock();
        }
    }

    public Future consume()
    {
        try
        {
            lock.lock();
            Future out = new Future();
            Task task = new Task(CONST.TASK_READ, out,buff);
            readTaskQue.addLast(task);
            schedulerCond.signal();
            return out;
        }
        finally {
            lock.unlock();
        }
    }


        private void schedule()
    {
        //boolean fAnyPossibilities=false;
        boolean fReadPoss=false;
        boolean fWritePoss=false;
        while (fWork)
        {

            if(!readTaskQue.isEmpty())
            {
                if(readTaskQue.peekFirst()==null)
                {
                    try{readTaskQue.removeFirst();} catch (Exception e) {;}
                }
                else if(readTaskQue.peekFirst().guard())
                {
                    fReadPoss=true;
                }
            }
            if(!writeTaskQue.isEmpty() )
            {
                if(writeTaskQue.peekFirst()==null)
                {
                    try{writeTaskQue.removeFirst();} catch (Exception e) {;}
                }
                else if(writeTaskQue.peekFirst().guard())
                {
                    fWritePoss=true;
                }
            }
            //work();
            if(!fReadPoss && !fWritePoss)
            {
                try
                {
                    lock.lock();
                    try{schedulerCond.await();} catch(Exception e) {;}
                }
                finally {
                    lock.unlock();
                }
            }
            else if(fReadPoss && !fWritePoss)
            {
                readTaskQue.removeFirst().execute();
                work();
            }
            else if(!fReadPoss && fWritePoss)
            {
                writeTaskQue.removeFirst().execute();
                work();
            }
            else
            {
                if(readTaskQue.peekFirst().getTimeStamp() > writeTaskQue.peekFirst().getTimeStamp())
                {
                    writeTaskQue.removeFirst().execute();
                }
                else
                {
                    readTaskQue.removeFirst().execute();
                }
                work();
            }

            fReadPoss=false;
            fWritePoss=false;
        }
    }





    public void end()
    {
        try
        {
            lock.lock();
            fWork=false;
            schedulerCond.signal();
        }
        finally {
            lock.unlock();
        }
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



}
