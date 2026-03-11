package analizadores.instrucciones;

import analizadores.ast.Arbol;
import analizadores.ast.Instruccion;
import analizadores.data.DatabaseMemory;
import analizadores.tablas.TablaSimbolos;

public class UseDatabase extends Instruccion {
    private final String nombre;

    public UseDatabase(String nombre, int linea, int col) {
        super(linea, col);
        this.nombre = nombre;
    }

    @Override
    public void interpretar(Arbol arbol, TablaSimbolos ts) {
        DatabaseMemory db = ts.getDB(nombre);
        if (db == null) {
            arbol.error("Semántico",
                "La base de datos '" + nombre + "' no existe.",
                linea, columna);
            return;
        }
        ts.setDbActiva(db);
        arbol.print("Usando base de datos: " + nombre);
    }
}
