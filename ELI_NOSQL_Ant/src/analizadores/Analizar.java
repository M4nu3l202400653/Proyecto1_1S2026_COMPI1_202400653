package analizadores;

import analizadores.ast.Arbol;
import analizadores.ast.Instruccion;
import analizadores.errores.ErrorSintactico;
import analizadores.tablas.TablaSimbolos;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Analizar {

    private List<Lexico.TokenInfo>  tokens;
    private List<Lexico.ErrorLexico> erroresLexicos;
    private List<ErrorSintactico>   erroresSintacticos;
    private List<String[]>          erroresEjecucion;
    private String                  salidaConsola;

    // tabla de símbolos correspondiente a la última ejecución
    private TablaSimbolos           ultimaTabla;

    public void analizar(String codigo) {
        // ===== Limpiar estado estático de ejecuciones anteriores =====
        Lexico.TOKENS.clear();
        Lexico.ERRORES.clear();
        Sintactico.ERRORES.clear();

        // ===== Lexer + Parser =====
        LinkedList<Instruccion> instrucciones = null;
        try {
            Lexico    lexer  = new Lexico(new StringReader(codigo));
            Sintactico parser = new Sintactico(lexer);
            java_cup.runtime.Symbol resultado = parser.parse();
            if (resultado != null && resultado.value instanceof LinkedList) {
                @SuppressWarnings("unchecked")
                LinkedList<Instruccion> tmp =
                    (LinkedList<Instruccion>) resultado.value;
                instrucciones = tmp;
            }
        } catch (Exception e) {
            // Los errores fatales ya fueron registrados en Sintactico.ERRORES
        }

        erroresLexicos      = new ArrayList<>(Lexico.ERRORES);
        erroresSintacticos  = new ArrayList<>(Sintactico.ERRORES);
        tokens              = new ArrayList<>(Lexico.TOKENS);

        // ===== Interpretación (ejecutar aunque haya errores léxicos/sintácticos) =====
        salidaConsola    = "";
        erroresEjecucion = new ArrayList<>();

        if (instrucciones != null) {
            // ejecutar incluso si hay errores previos, para permitir guardar JSON
            TablaSimbolos ts   = new TablaSimbolos();
            Arbol          arbol = new Arbol(instrucciones);
            arbol.ejecutar(ts);
            salidaConsola    = arbol.getConsola();
            erroresEjecucion = arbol.getErrores();
            // almacenar tabla para uso posterior (GUI / guardado manual)
            ultimaTabla = ts;
        } else {
            // en caso de que no haya instrucciones en absoluto
            ultimaTabla = null;
        }
    }

    public List<Lexico.TokenInfo>   getTokens()              { return tokens; }
    public List<Lexico.ErrorLexico> getErroresLexicos()      { return erroresLexicos; }
    public List<ErrorSintactico>    getErroresSintacticos()  { return erroresSintacticos; }
    public List<String[]>           getErroresEjecucion()    { return erroresEjecucion; }
    public String                   getSalidaConsola()       { return salidaConsola; }

    /**
     * Devuelve la última tabla de símbolos generada tras analizar código.
     * Puede ser null si no se ha ejecutado nada o si hubo errores antes.
     */
    public TablaSimbolos getTablaSimbolos() { return ultimaTabla; }

    public boolean tuvoErroresPrevios() {
        return !erroresLexicos.isEmpty() || !erroresSintacticos.isEmpty();
    }
}
