package MergeTheNumbers.AI;

import it.unical.mat.embasp.languages.Id;
import it.unical.mat.embasp.languages.Param;
import it.unical.mat.embasp.languages.asp.SymbolicConstant;

@Id("cella")
public class Cella {
    @Param(0)
    private SymbolicConstant row;
    @Param(1)
    private SymbolicConstant col;
    @Param(2)
    private SymbolicConstant value;
    public Cella() {
    }
    public Cella(SymbolicConstant row, SymbolicConstant col, SymbolicConstant value) {
        this.row = row;
        this.col = col;
        this.value = value;
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

    public SymbolicConstant getValue() {
        return value;
    }

    public void setValue(SymbolicConstant value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "cella(" + row + "," + col + "," + value + ")";
    }
}
