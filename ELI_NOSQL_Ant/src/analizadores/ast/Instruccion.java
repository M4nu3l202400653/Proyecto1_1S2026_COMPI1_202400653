package analizadores.ast;

import analizadores.tablas.TablaSimbolos;

public abstract class Instruccion {
    protected final int linea;
    protected final int columna;

    public Instruccion(int linea, int columna) {
        this.linea   = linea;
        this.columna = columna;
    }

    public abstract void interpretar(Arbol arbol, TablaSimbolos ts);
}
