package MergeTheNumbers.AI;

import it.unical.mat.embasp.base.InputProgram;
import it.unical.mat.embasp.base.Output;
import it.unical.mat.embasp.languages.asp.*;
import it.unical.mat.embasp.platforms.desktop.DesktopHandler;
import it.unical.mat.embasp.specializations.dlv2.desktop.DLV2DesktopService;
import java.util.HashSet;
import java.util.Set;

public class GameAI {
    private static final String DLV_PATH = "lib/dlv-2.1.1-macos";
    private static String encodingResourceUnisci="regole/unisci";
    private static String encodingResourceMuovi="regole/muovi";
    private DesktopHandler desktopHandler;
    private InputProgram inputProgram;
    private String lastAnswerset=null; //in questa variabile salvo l'ultimo answerset generato ad ogni iterazione dalle fusioni

    public GameAI() {
        desktopHandler = new DesktopHandler(new DLV2DesktopService(DLV_PATH)); //Istanzio un oggetto desktopHandler con un DLV2DestopService che prende il path di DLV2.
        //Questo oggetto viene usato per comunicare con il programma DLV2 per l'esecuzione del programma
        inputProgram = new ASPInputProgram(); //Istanzio un oggetto ASPInputProgram. Questo oggetto conterrà le regole del programma logico per il gioco
        try {
            ASPMapper.getInstance().registerClass(Cella.class); //Uso ASPMApper per mappare gli oggetti Java alle entità ASP.
            // La classe Cella consente all'ASPMapper di gestire la corrispondenza tra oggetti Java e fatti o regole ASP
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //converto la mia matrice in una collezione di Celle
    private Set<Object> convertMatrixToCellSet(int[][] matrice){
        int sizeMatrice=matrice.length;
        int riga = sizeMatrice - 1, colonna = sizeMatrice - 1;

        Set<Object> cella = new HashSet<Object>(); //Creo un insieme di oggetti di tipo Cella. e uso HashSet per non avere duplicati fregandomene dell'ordine degli elementi

        for(int r = 0; r < matrice.length; r++) { //Scorro le righe della matrice
            colonna = sizeMatrice-1; //Ad ogni ciclo, reinizializzo questa variabile
            for(int c = 0; c < matrice.length; c++) { //Scorro le colonne della matrice
                //Creo un oggetto Cella e lo aggiungo all'insieme utilizzando le coordinate della cella e il valore corrispondente nella matrice
                cella.add(new Cella(new SymbolicConstant(String.valueOf(riga)),new SymbolicConstant(String.valueOf(colonna)),new SymbolicConstant(String.valueOf(matrice[r][c]))));
                colonna--; //Decremento ogni volta per passare alla colonna successiva
            }
            riga--;
        }

        return cella;

    }

    //utilizzo questa funzione per settare e correggere l'answerset generato da unisci
    private void setAnswerset(AnswerSet answerset) {
        lastAnswerset = new String(answerset.toString().replace("[", "").replace("]", ".").replace("),",")."));
    }

    public Unisci merge(int [][]matrix) {
        inputProgram.clearFilesPaths(); //Cancello eventuali percorsi dei file associati al programma
        inputProgram.clearPrograms(); //Cancello tutti i programmi associati all'oggetto program
        inputProgram.addFilesPath(encodingResourceUnisci); //Imposto il percorso del file per "unisci" ai percorsi dei file del programma

        try {
            ASPMapper.getInstance().unregisterClass(Muovi.class); //Cancello la mappatura "muovi" dell'ASPMapper
            ASPMapper.getInstance().registerClass(Unisci.class); //E registro la classe "unisci" per essere mappata
            inputProgram.addObjectsInput(convertMatrixToCellSet(matrix)); //Aggiungo un insieme di oggetti convertiti dalla matrice di input al programma. Il tutto è fatto dal metodo convertMatrixToCellSet
        } catch (Exception e) {
            e.printStackTrace();
        }

        desktopHandler.removeAll(); //Rimuovo tutto ciò che era stato generato precedentemente
        desktopHandler.addProgram(inputProgram); //E aggiungo il programma corrente al desktopHandler

        Output output =  desktopHandler.startSync(); //Invoco dlv in maniera sincrona ottenendo l'output

        AnswerSets answersSet = (AnswerSets) output; //Converto l'output in un oggetto AnswerSet
        for(AnswerSet a : answersSet.getOptimalAnswerSets()){ //Scorro tutti gli answer set
            try {
                Unisci unisci=null;
                //System.out.println(a); //per stampare l'answerset
                for(Object obj:a.getAtoms()){ //Itero su tutti gli atomi dell'answerset corrente
                    if(obj instanceof Unisci)  { //Verifico se l'oggetto è un'istanza della classe `Unisci`.
                        unisci = (Unisci) obj; //Assegno l'oggetto `Unisci` corrente a `unisci`.
                        setAnswerset(a); //Setto l'answerset corrente
                        return unisci; //E lo restituisco
                    }
                }
                setAnswerset(answersSet.getOptimalAnswerSets().get(0)); // Imposto l'AnswerSet corrente utilizzando il primo AnswerSet ottimale
                return unisci; //E lo restituisco
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    //Stessa cosa del metodo di sopra ma ora per "muovi"
    public Muovi move() {
        inputProgram.clearFilesPaths();
        inputProgram.clearPrograms();
        inputProgram.addFilesPath(encodingResourceMuovi);

        try {
            ASPMapper.getInstance().unregisterClass(Unisci.class);
            ASPMapper.getInstance().registerClass(Muovi.class);
            inputProgram.addProgram(lastAnswerset);
        } catch (Exception e) {
            e.printStackTrace();
        }

        desktopHandler.removeAll();
        desktopHandler.addProgram(inputProgram);

        Output output =  desktopHandler.startSync();

        AnswerSets answersSet = (AnswerSets) output;
        for(AnswerSet a : answersSet.getOptimalAnswerSets()){
            try {
                Muovi muovi=null;
                //System.out.println(a);
                for(Object obj:a.getAtoms()){
                    if(obj instanceof Muovi)  {
                        muovi = (Muovi) obj;
                    }
                }
                setAnswerset(answersSet.getOptimalAnswerSets().get(0));
                return muovi;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

}
