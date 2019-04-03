package com.company;

public class BufforClass {

    private int size;
    private int iWritePoint=0;
    private int iReadPoint=0;
    private int iCount=0;
    private BufferedObject buff[];
    public BufforClass(int n)
    {
        size=n;
        buff=new BufferedObject[n];
        for (int i=0;i<n;i++)
            buff[i]=new BufferedObject();
    }

    public int getSize()
    {
        return size;
    }
    public BufferedObject getAccess(int id)
    {
        return buff[id];
    }

    public int read() {
        //System.out.println("in buffor read, count= "+ iCount+ " canRead() = " + canRead());
        if(!canRead())
        {
            return -1;
        }
        int out= buff[iReadPoint].get();
        iReadPoint=(iReadPoint+1)%size;
        iCount--;

        return out;
    }
    public int write(int val)
    {
        if(!canWrite())
        {
            return -1;
        }
        int out= buff[iWritePoint].set(val).get();
//        System.out.println("set buff[" +iWritePoint+ "] as " +val + " and got " + out+"");
        iWritePoint=(iWritePoint+1)%size;
        iCount++;
        return  out;
    }

    public boolean canRead()
    {
        return iCount>0;
    }
    public boolean canWrite()
    {
        return iCount<=size;
    }


}
