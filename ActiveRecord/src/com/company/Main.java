package com.company;

import com.company.active.ConsAO;
import com.company.active.ProdAO;
import com.company.active.Proxy;
import com.company.asynchr.ConsumentAs;
import com.company.asynchr.MonitorAs;
import com.company.asynchr.ProducentAs;

import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

class TestData
{
    public LinkedList<ITestObj> threads;
    public long time;
}




public class Main {

    static public int TEST_TIME=60*1000;
    static public int BUFFOR_SIZE=1000;
    static public int NUMBER_OF_CLIENTS=10;
    static public int DEV_TIME=250*1000; // we will probably use 10%


    static Pair<Integer,Double> runAsynchronousTest(int iTimeMs,int iMonSize,int iNumberOfClients,long iWorkTimeNs,long iSomethingElseTimeNs, long iDeviationTimeNs, boolean verbose)
    {
        MonitorAs mon=new MonitorAs(iMonSize);
        List<ITestObj> clients = new LinkedList<ITestObj>();
        for (int i=0;i<iNumberOfClients;i++)
        {
            clients.add(new ProducentAs(mon,i,iWorkTimeNs,iSomethingElseTimeNs,iDeviationTimeNs,verbose));
            clients.add(new ConsumentAs(mon,i,iWorkTimeNs,iSomethingElseTimeNs,iDeviationTimeNs,verbose));
        }
        for( ITestObj e: clients)
        {
            e.start();
        }
        long startTime=System.currentTimeMillis();
        while(System.currentTimeMillis() - startTime < iTimeMs)
        {
            System.out.print("");
        }
        for( ITestObj e: clients)
        {
            e.end();
        }

        startTime=System.currentTimeMillis();
        while(System.currentTimeMillis() - startTime < 1) // wait a milisecond
        {
            System.out.print("");
        }
        long arythemiticMean=0;
        long valTab[]= new long[iNumberOfClients*2];
        double standardDeviation=0;
        long diffSum=0;

        int i=0;
        for( ITestObj e: clients)
        {
            arythemiticMean=arythemiticMean + e.timesDone();
            valTab[i]=e.timesDone();
            i++;
            try{e.stop();}catch(Exception ex){;}
        }
        
        
        arythemiticMean= arythemiticMean / (iNumberOfClients*2);
        
        for(i=0;i<iNumberOfClients*2;i++)
        {
            diffSum+= (valTab[i] - arythemiticMean)*(valTab[i] - arythemiticMean);
        }
        standardDeviation= Math.sqrt(((double)diffSum)/(iNumberOfClients*2));
        Pair<Integer,Double> out=new Pair<>();
        out.fst= (int )arythemiticMean;
        out.snd= standardDeviation;


        return out;
    }




    public static Pair<Integer,Double> runActiveObjectTest(int iTimeMs,int iProxySize,int iNumberOfClients,long iWorkTimeNs,long iSomethingElseTimeNs, long iDeviationTimeNs, boolean verbose)
    {
        Proxy proxy= new Proxy(iProxySize,iWorkTimeNs,iDeviationTimeNs);
        LinkedList<ProdAO> prodList=new LinkedList<>();
        LinkedList<ConsAO> consList=new LinkedList<>();
        proxy.start();
        for(int i=0;i<iNumberOfClients;i++)
        {
            ProdAO tmp1=new ProdAO(proxy,i,iSomethingElseTimeNs,iDeviationTimeNs,verbose);
            ConsAO tmp2=new ConsAO(proxy,i,iSomethingElseTimeNs,iDeviationTimeNs,verbose);
            prodList.add(tmp1);
            consList.add(tmp2);
        }
        for(ProdAO e: prodList)
        {
            e.start();
        }

        for(ConsAO e: consList)
        {
            e.start();
        }


        long startTime=System.currentTimeMillis();
        while(System.currentTimeMillis() - startTime < iTimeMs)
        {
            System.out.print("");
        }
        for( ConsAO e: consList)
        {
            e.end();
        }
        for( ProdAO e: prodList)
        {
            e.end();
        }
        startTime=System.currentTimeMillis();
        while(System.currentTimeMillis() - startTime < 1) // wait a milisecond
        {
            System.out.print("");
        }

        long arythemiticMean=0;
        long valTab[]= new long[iNumberOfClients*2];
        double standardDeviation=0;
        long diffSum=0;

        int i=0;
        for( ProdAO e: prodList)
        {
            arythemiticMean=arythemiticMean + e.timesDone();
            valTab[i]=e.timesDone();
            i++;
            try{e.stop();}catch(Exception ex){;}
        }
        for( ConsAO e: consList)
        {
            arythemiticMean=arythemiticMean + e.timesDone();
            valTab[i]=e.timesDone();
            i++;
            try{e.stop();}catch(Exception ex){;}
        }
        proxy.end();

        arythemiticMean= arythemiticMean / (iNumberOfClients*2);

        for(i=0;i<iNumberOfClients*2;i++)
        {
            diffSum+= (valTab[i] - arythemiticMean)*(valTab[i] - arythemiticMean);
            //System.out.printf("(valTab[%d] <%d> -  arythemiticMean <%d>\n",i,valTab[i],arythemiticMean);
        }
        standardDeviation= Math.sqrt(((double)diffSum)/(iNumberOfClients*2));
        Pair<Integer,Double> out=new Pair<>();
        out.fst= (int )arythemiticMean;
        out.snd= standardDeviation;
        return out;

    }





    public static void testAO()
    {
        Proxy proxy= new Proxy(1000,250*1000*1000,25*1000*1000);
        LinkedList<ProdAO> prodList=new LinkedList<>();
        LinkedList<ConsAO> consList=new LinkedList<>();
        proxy.start();
        for(int i=0;i<2;i++)
        {
            ProdAO tmp1=new ProdAO(proxy,i,250*1000*1000,25*1000*1000,true);
            ConsAO tmp2=new ConsAO(proxy,i,250*1000*1000,25*1000*1000,true);
            prodList.add(tmp1);
            consList.add(tmp2);
            tmp1.start();
            tmp2.start();
        }

        long startTime=System.currentTimeMillis();


        Scanner s= new Scanner(System.in);
        s.nextLine();

        for(ProdAO e: prodList)
        {
            e.end();
        }
        for(ConsAO e: consList)
        {
            e.end();
        }

        for(ProdAO e: prodList)
        {
            e.sayGoodbye();
        }
        for(ConsAO e: consList)
        {
            e.sayGoodbye();
        }

        proxy.end();




    }














    public static void main(String[] args)
    {
//        testAO();
//        System.exit(7);
        double standardDeviation;
        File fout = new File("c:/tmp/CoresTest_alpha.txt");
        BufferedWriter bw;
        try{bw=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fout)));}
        catch(IOException ioe)
        {
            System.out.print(ioe.getMessage());
            ioe.printStackTrace();
            return;
        }

        Pair<Integer,Double> testOutcome;
        String outcome="";
        for(int i=0;i<=25;i++)
        {


                int iWorktime= i * 100 * 1000;
                int iSomethingElse= 2500 * 1000 - iWorktime;
                int iDevTime=i*1000 + 500;
                for(int k=1;k<=4;k++) {
                    String tmp = String.format("%d %d %d ", iWorktime / 1000, iSomethingElse / 1000, k);
                    testOutcome = runAsynchronousTest(TEST_TIME, BUFFOR_SIZE, k, iWorktime, iSomethingElse, iDevTime, false);
                    outcome = outcome + String.format("Asynchoronous test #%d#%d ,with mean %d and standardDeviation %f\n", i, k, testOutcome.fst, testOutcome.snd);
                    tmp = tmp + String.format("%d ", testOutcome.fst);
                    testOutcome = runActiveObjectTest(TEST_TIME, BUFFOR_SIZE, k, iWorktime, iSomethingElse, iDevTime, false);
                    outcome = outcome + String.format("ActiveObject test #%d#%d ,with mean %d and standardDeviation %f\n", i, k, testOutcome.fst, testOutcome.snd);
                    tmp = tmp + String.format("%d", testOutcome.fst);
                    try {
                        bw.write(tmp);
                        bw.newLine();
                    } catch (IOException ieo) {
                        try {
                            bw.write(tmp);
                            bw.newLine();
                        } catch (IOException ieo2) {
                            System.out.print("write to file failed exiting");
                            return;
                        }
                    }
                }

        }
        try{bw.close();} catch(IOException ieo) {try{bw.close();} catch(IOException ieo2) {System.out.print("file close failed  message\n"+ ieo2.getMessage()+"\n"); ieo2.printStackTrace(); }}
        System.out.print(outcome);




//        long timesAccesed=-1;
//
//        String outcomeroni="";
//        for (int i=0;i<2;i++)
//        {

//            Pair testOutcome=runAsynchronousTest(10*1000,1000,NUMBER_OF_CLIENTS,250*1000, 125*1000,20*1000,false);
//            outcomeroni = outcomeroni + Long.toString(testOutcome.fst) + "\n";
//        }
//        System.out.println(outcomeroni);
    }




//    public static void main(String[] args) {
//	// write your code here
//        LinkedList<ProducentAs> prod=new LinkedList<>();
//        LinkedList<ConsumentAs> cons=new LinkedList<>();
//        MonitorAs mon=new MonitorAs(10000);
//        for (int i=0;i<29;i++)
//        {
//            prod.add(new ProducentAs(mon,i));
//            cons.add(new ConsumentAs(mon,i));
//        }
//
//        for (ProducentAs p  : prod)
//        {
//            p.setVerbose(true);
//            p.start();
//        }
//        for(ConsumentAs c : cons)
//        {
//            c.setVerbose(true);
//            c.start();
//        }
//
//        long startTime=System.currentTimeMillis();
//
//
//        Scanner s= new Scanner(System.in);
//        s.nextLine();
//
//
//        for (ProducentAs p  : prod)
//        {
//            p.end();
//        }
//
//        for(ConsumentAs c : cons)
//        {
//            c.end();
//        }
//
//        for (ProducentAs p  : prod)
//        {
//            p.sayGoodbye();
//        }
//
//        for(ConsumentAs c : cons)
//        {
//            c.sayGoodbye();
//        }
//
//
//
//        System.out.printf("test Concluded, taken %f seconds", (System.currentTimeMillis()-startTime)/1000.0);
//    }
}
