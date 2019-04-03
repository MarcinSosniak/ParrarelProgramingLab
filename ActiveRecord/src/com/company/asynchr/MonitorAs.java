package com.company.asynchr;



import com.company.BufferedObject;
import com.company.BufforClass;
import com.company.Pair;

import java.util.LinkedList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class MonitorAs {
    private ReentrantLock prodLock=new ReentrantLock();
    private ReentrantLock consLock=new ReentrantLock();
    private Condition prodCondition=prodLock.newCondition();
    private Condition consCondition=consLock.newCondition();
    private LinkedList<Integer> clientTickets= new LinkedList<>();
    private LinkedList<Integer> producentTickets= new LinkedList<>();
    private BufforClass buff;
    int size;

    public MonitorAs(int n)
    {
        buff=new BufforClass(n);
        size=n;
        for (int i=0;i<n;i++)
        {
            producentTickets.add(new Integer(i));
        }
    }
    public MonitorAs(BufforClass buffor)
    {
        this.buff=buffor;
        this.size=buff.getSize();
        for (int i=0;i<size;i++)
        {
            producentTickets.add(new Integer(i));
        }
    }

    public Pair<Integer,BufferedObject> tryProduce()
    {
        Pair outPair=new Pair<Integer,BufferedObject>();
        try {
            prodLock.lock();
            //System.out.println("Prod obtained lock");
            while (producentTickets.isEmpty())
            {
                prodCondition.await();
            }
            Integer ticket=producentTickets.removeFirst();
            BufferedObject out=buff.getAccess(ticket.intValue());
            //System.out.println("from buff.getAccess("+ ticket.intValue() + ") and got: "+ out  );
            //System.out.println("producentTickets got" + ticket  );
            outPair.fst=ticket;
            outPair.snd=out;
            //System.out.println("and the otucoming Pair is: " + outPair + " fst= "+outPair.fst + " snd= " + outPair.snd );


        }
        catch(Exception e){
            e.printStackTrace();
        }
        finally {
            try{prodLock.unlock();} catch(Exception e) {;}
        }
        return outPair;
    }

    public void FinishedProduce(Integer ticket)
    {
        try{
            consLock.lock();
            clientTickets.addLast(ticket);
            consCondition.signal();
        }
        finally
        {
            try{consLock.unlock();} catch(Exception e) {;}
        }
    }

    public Pair<Integer,BufferedObject> tryConsume()
    {
        Pair outPair=new Pair<Integer,BufferedObject>();
        BufferedObject out=null;
        Integer ticket=null;
        try
        {
            consLock.lock();
            while (clientTickets.isEmpty())
            {
                consCondition.await();
            }
            ticket=clientTickets.removeFirst();
            out=buff.getAccess(ticket.intValue());
            outPair.fst=ticket;
            outPair.snd=out;
        }
        catch(Exception e){
            e.printStackTrace();
        }
        finally
        {
            try{consLock.unlock();} catch(Exception e) {;}
        }
        return outPair;
    }

    public void FinishedConsume(Integer ticket)
    {
        try{
            prodLock.lock();
            producentTickets.addLast(ticket);
            prodCondition.signal();
        }
        finally
        {
            try{prodLock.unlock();} catch(Exception e) {;}
        }
    }







}
