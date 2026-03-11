package analizadores.ast;

import analizadores.tablas.TablaSimbolos;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Arbol {
    private final LinkedList<Instruccion>       instrucciones;
    private final StringBuilder                 consola;
    private final List<String[]>                erroresEjecucion;
    private List<Map<String, Object>>           ultimoResultado;
    private List<String>                        ultimosCampos;

    public Arbol(LinkedList<Instruccion> instrucciones) {
        this.instrucciones    = instrucciones;
        this.consola          = new StringBuilder();
        this.erroresEjecucion = new ArrayList<>();
    }

    public void ejecutar(TablaSimbolos ts) {
        for (Instruccion inst : instrucciones) {
            inst.interpretar(this, ts);
        }
    }

    public void print(String texto) {
        consola.append(texto).append("\n");
    }

    public void error(String tipo, String desc, int linea, int col) {
        erroresEjecucion.add(new String[]{
            tipo, desc, String.valueOf(linea), String.valueOf(col)
        });
    }

    public void setUltimoResultado(List<Map<String, Object>> resultado,
                                   List<String> campos) {
        this.ultimoResultado = resultado;
        this.ultimosCampos   = campos;
    }

    public String               getConsola()          { return consola.toString(); }
    public List<String[]>       getErrores()          { return erroresEjecucion; }
    public LinkedList<Instruccion> getInstrucciones() { return instrucciones; }
    public List<Map<String, Object>> getUltimoResultado() { return ultimoResultado; }
    public List<String>         getUltimosCampos()    { return ultimosCampos; }
}
