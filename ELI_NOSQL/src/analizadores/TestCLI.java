package analizadores;
public class TestCLI {
    public static void main(String[] args) throws Exception {
        String path = args.length > 0 ? args[0] : "test.code";
        String codigo = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(path)), java.nio.charset.StandardCharsets.UTF_8);
        Analizar motor = new Analizar();
        motor.analizar(codigo);
        System.out.println("=== TOKENS (" + motor.getTokens().size() + ") ===");
        System.out.println("=== ERRORES LEXICOS: " + motor.getErroresLexicos().size() + " ===");
        for (Lexico.ErrorLexico e : motor.getErroresLexicos()) {
            System.out.println("  LEX ERROR: " + e.descripcion + " L" + e.linea + ":C" + e.columna);
        }
        System.out.println("=== ERRORES SINTACTICOS: " + motor.getErroresSintacticos().size() + " ===");
        for (analizadores.errores.ErrorSintactico e : motor.getErroresSintacticos()) {
            System.out.println("  SYN ERROR: " + e.descripcion + " L" + e.linea + ":C" + e.columna);
        }
        System.out.println("=== ERRORES EJECUCION: " + motor.getErroresEjecucion().size() + " ===");
        for (String[] e : motor.getErroresEjecucion()) {
            System.out.println("  EXEC ERROR: " + e[0] + ": " + e[1] + " L" + e[2]);
        }
        System.out.println("=== CONSOLA ===");
        System.out.println(motor.getSalidaConsola());
    }
}
