package gui;

public class Controller {

    public void doStuff2(){
        new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        // your code here
                    }
                },
                5000
        );
    }

    public void doStuff(){
        System.out.println("hej");
    }
}
