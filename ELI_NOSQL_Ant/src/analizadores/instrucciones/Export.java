package analizadores.instrucciones;

import analizadores.ast.Arbol;
import analizadores.ast.Instruccion;
import analizadores.persistencia.JsonManager;
import analizadores.tablas.TablaSimbolos;

public class Export extends Instruccion {
    private final String rutaArchivo;

    public Export(String rutaArchivo, int linea, int col) {
        super(linea, col);
        this.rutaArchivo = rutaArchivo;
    }

    @Override
    public void interpretar(Arbol arbol, TablaSimbolos ts) {
        var resultado = arbol.getUltimoResultado();
        var campos    = arbol.getUltimosCampos();

        if (resultado == null) {
            arbol.error("Semántico",
                "No hay resultado previo para exportar. Ejecute 'read' primero.",
                linea, columna);
            return;
        }

        try {
            JsonManager.exportarResultado(resultado, campos, rutaArchivo);
            arbol.print("Resultado exportado a: " + rutaArchivo);
        } catch (Exception e) {
            arbol.error("Semántico",
                "Error al exportar: " + e.getMessage(), linea, columna);
        }
    }
}
