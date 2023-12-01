package MergeTheNumbers.AI;

import it.unical.mat.embasp.languages.Id;
import it.unical.mat.embasp.languages.Param;
import it.unical.mat.embasp.languages.asp.SymbolicConstant;

@Id("unisci")
public class Unisci {
    @Param(0)
    private SymbolicConstant row;

    @Param(1)
    private SymbolicConstant col;

    @Param(2)
    private SymbolicConstant row1;

    @Param(3)
    private SymbolicConstant col1;

    public Unisci() {}

    public Unisci(SymbolicConstant row, SymbolicConstant col, SymbolicConstant row1, SymbolicConstant col1) {
        this.row = row;
        this.col = col;
        this.row = row1;
        this.col = col1;
    }

    public SymbolicConstant getRow() {
        return row;
    }

    public void setRow(SymbolicConstant row) {
        this.row = row;
    }

    public SymbolicConstant getCol() {
        return col;
    }

    public void setCol(SymbolicConstant col) {
        this.col = col;
    }

    public SymbolicConstant getRow1() {
        return row1;
    }

    public void setRow1(SymbolicConstant row1) {
        this.row1 = row1;
    }

    public SymbolicConstant getCol1() {
        return col1;
    }

    public void setCol1(SymbolicConstant col1) {
        this.col1 = col1;
    }

    //Mi restituisce il valore delle unioni come interi
    public int getIntRow() {
        return Integer.valueOf(String.valueOf(row));
    }

    public int getIntCol() {
        return Integer.valueOf(String.valueOf(col));
    }

    public int getIntRow1() {
        return Integer.valueOf(String.valueOf(row1));
    }

    public int getIntCol1() {
        return Integer.valueOf(String.valueOf(col1));
    }

    public String toString() {
        return "unisci(" + row + "," + col + "," + row1 + "," + col1 + ")";
    }
}