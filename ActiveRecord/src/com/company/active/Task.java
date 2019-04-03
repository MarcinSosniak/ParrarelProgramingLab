package com.company.active;

import com.company.BufforClass;

public class Task {
    private int taskType;
    private Future fut;
    private int writeVal=-1;
    private long timeStamp;
    private BufforClass buff;

    public Task(int taskType,Future fut,int writeVal,BufforClass buff)
    {
        timeStamp=System.nanoTime();
        this.buff=buff;
        this.taskType=taskType;
        this.fut=fut;
        this.writeVal=writeVal;
    }
    public Task(int taskType,Future fut,BufforClass buff)
    {
        timeStamp=System.nanoTime();
        this.buff=buff;
        this.taskType=taskType;
        this.fut=fut;
    }

    public long getTimeStamp()
    {
        return timeStamp;
    }


    public boolean guard()
    {
        if (taskType==CONST.TASK_READ)
        {
            return buff.canRead();
        }
        if (taskType==CONST.TASK_WRITE)
        {
            return buff.canWrite();
        }
        return false;
    }

    public void execute()
    {
        if(taskType==CONST.TASK_READ)
        {

            int out=buff.read();
            fut.insert(out);// insert copy off given value into future, and poke it's
        }
        if(taskType==CONST.TASK_WRITE)
        {
            int out=buff.write(writeVal);
            fut.insert(out);// insert copy off given value into future, and poke it's
        //System.out.println("In executing Writer Task END");
        }
    }





    public int val()
    {
        return writeVal;
    }

    public Future getFut() {
        return fut;
    }

    public boolean fRead()
    {
        return CONST.TASK_READ==taskType;
    }
    public boolean fWrite()
    {
        return CONST.TASK_WRITE==taskType;
    }
}
