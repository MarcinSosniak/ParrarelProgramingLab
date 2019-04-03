package com.company.active;

public class Future {

    private int val=-1;
    private boolean ready=false;

    public synchronized boolean check()
    {
        return ready;
    }
    private boolean nonSynchCheck()
    {
        return ready;
    }

    public synchronized Future insert(int val)
    {
        this.val=val;
        ready=true;
        notifyAll();
        return this;
    }



    public synchronized int getVal()
    {
        while(!nonSynchCheck())
        {
            try{wait();} catch(Exception e) {;}
        }
        return val;
    }

}
