package analizadores.errores;

public class ErrorSintactico {
    public final String tipo;
    public final String descripcion;
    public final int    linea;
    public final int    columna;

    public ErrorSintactico(String tipo, String descripcion,
                           int linea, int columna) {
        this.tipo        = tipo;
        this.descripcion = descripcion;
        this.linea       = linea;
        this.columna     = columna;
    }
}
