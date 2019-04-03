package sample;

import java.util.LinkedList;
import java.util.Scanner;

public class Main {

    private static final int PROD_COUNT=16;
    private static final int CONS_COUNT=16;
    private static final int testTime=5;
    public static void main(String[] args)
    {

        MonitorPorcje mon=new MonitorPorcje();
        LinkedList<Producent> prodList= new LinkedList<>();
        LinkedList<Consumer> consList= new LinkedList<>();


        prodList.add(new Producent(mon,95,5));
        prodList.add(new Producent(mon,95,5));
        prodList.add(new Producent(mon,5,5));
        prodList.add(new Producent(mon,5,5));
        prodList.add(new Producent(mon,25,25));
        prodList.add(new Producent(mon,25,25));
        prodList.add(new Producent(mon,75,25));
        prodList.add(new Producent(mon,75,25));

        consList.add(new Consumer(mon,95,5));
        consList.add(new Consumer(mon,95,5));
        consList.add(new Consumer(mon,5,5));
        consList.add(new Consumer(mon,5,5));
        consList.add(new Consumer(mon,25,25));
        consList.add(new Consumer(mon,25,25));
        consList.add(new Consumer(mon,75,25));
        consList.add(new Consumer(mon,75,25));




        int toFillProd=PROD_COUNT-prodList.size();
        int toFillCons=CONS_COUNT-consList.size();
        for(int i=0;i<toFillProd;i++)
        {
            prodList.add(new Producent(mon));
        }
        for(int i=0;i<toFillCons;i++)
        {
            consList.add(new Consumer(mon));
        }





        for(Producent e :  prodList)
        {
            e.start();
        }
        for(Consumer e: consList)
        {
            e.start();
        }
        long startTime=System.currentTimeMillis();

        Scanner keyboard = new Scanner(System.in);

        // dzieje sie tu straszan amagia javy. kiedy chce uzywac Thread.sleep albo innego narzedzia do mierzenia czasu
        // i wtedy z niewiadomych powod producentci i konsumenci maja zaden czas procesora
        // Zadenw  sensie, wykonuja do 100 powtorzen, zamiast 20 000 jak zywkle w ciaglu tego samego czasu
        // Czemu, nie jsetem wstanie powidziec
        System.out.printf("enter any char  integer\n");
        keyboard.nextLine();


        for(Producent e :  prodList)
        {
            e.end();
        }
        for(Consumer e: consList)
        {
            e.end();
        }




//        try{Thread.sleep(1000*testTime);}
//        catch (Exception e)
//        {
//           System.out.printf("test Interrupted, taken %f seconds", System.currentTimeMillis()/1000.0);
//            ;
//        }

        for(Producent e :  prodList)
        {
            e.sayGoodbye();
        }
        System.out.printf("\n\n");
        for(Consumer e: consList)
        {
            e.sayGoodbye();
        }


        System.out.printf("test Concluded, taken %f seconds", (System.currentTimeMillis()-startTime)/1000.0);
    }
}
