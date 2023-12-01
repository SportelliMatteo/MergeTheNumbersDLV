package MergeTheNumbers;

public class GameRunner implements Runnable{
    private volatile boolean playing = true;

    public void end() {
        playing = false;
    }

    @Override
    public void run() {
        while (playing) {
            try {
                Thread.sleep((long) 1000);
                System.out.println("Sto giocando");
                MergeTheNumbersGame.getInstance().play();
            } catch (InterruptedException e){
                playing = false;
            }
        }
        System.out.println("Non ci sono più mosse...");
    }
}