/* ------------- ELI NOSQL - Lexer (JFlex) -------------
   Basado en el enunciado del proyecto (comentarios, tipos, operadores, instrucciones).
   Genera: analizadores.Lexico
------------------------------------------------------- */

package analizadores;

import java_cup.runtime.Symbol;
import java.util.ArrayList;

%%

%public
%class Lexico
%unicode
%cup
%line
%column
%char
%ignorecase

%state COMMENT_MULTI

%{

    // ====== ESTRUCTURAS PARA REPORTES (Tokens / Errores) ======
    public static class TokenInfo {
        public final String lexema;
        public final String tipo;
        public final int linea;
        public final int columna;

        public TokenInfo(String lexema, String tipo, int linea, int columna) {
            this.lexema = lexema;
            this.tipo = tipo;
            this.linea = linea;
            this.columna = columna;
        }
    }

    public static class ErrorLexico {
        public final String tipo;        // "Léxico"
        public final String descripcion; // detalle
        public final int linea;
        public final int columna;

        public ErrorLexico(String tipo, String descripcion, int linea, int columna) {
            this.tipo = tipo;
            this.descripcion = descripcion;
            this.linea = linea;
            this.columna = columna;
        }
    }

    public static final ArrayList<TokenInfo> TOKENS = new ArrayList<>();
    public static final ArrayList<ErrorLexico> ERRORES = new ArrayList<>();

    private Symbol token(int sym, String tipo) {
        TOKENS.add(new TokenInfo(yytext(), tipo, yyline + 1, yycolumn + 1));
        return new Symbol(sym, yyline + 1, yycolumn + 1, yytext());
    }

    private Symbol token(int sym, String tipo, Object value) {
        TOKENS.add(new TokenInfo(yytext(), tipo, yyline + 1, yycolumn + 1));
        return new Symbol(sym, yyline + 1, yycolumn + 1, value);
    }

    private void errorLexico(String descripcion) {
        ERRORES.add(new ErrorLexico("Léxico", descripcion, yyline + 1, yycolumn + 1));
    }

%}

/* ================== MACROS ================== */
WS          = [ \t\r\f]+
NL          = \n
DIG         = [0-9]
INT         = -?{DIG}+
DEC         = -?{DIG}+ "." {DIG}+
ID          = [a-zA-Z_][a-zA-Z0-9_]*
ESC         = \\([\"\\nrt])
STRCHAR     = ([^\"\\\n]|{ESC})
STRING      = \"{STRCHAR}*\"

/* ================== REGLAS ================== */
%%

/* --------- Comentarios --------- */
/* Comentario de una línea: ## ... fin de línea */
"##".*                              { /* ignorar */ }

/* Comentario multilínea: #* ... #* */
"#*"                                { yybegin(COMMENT_MULTI); }
<COMMENT_MULTI> "#*"                { yybegin(YYINITIAL); }
<COMMENT_MULTI> {NL}                { /* mantener conteo de líneas */ }
<COMMENT_MULTI> .                   { /* ignorar */ }

/* --------- Espacios --------- */
{WS}                                { /* ignorar */ }
{NL}                                { /* ignorar */ }

/* --------- Palabras reservadas (instrucciones) --------- */
"database"                          { return token(sym.DATABASE, "database"); }
"use"                               { return token(sym.USE, "use"); }
"table"                             { return token(sym.TABLE, "table"); }
"read"                              { return token(sym.READ, "read"); }
"fields"                            { return token(sym.FIELDS, "fields"); }
"filter"                            { return token(sym.FILTER, "filter"); }
"store"                             { return token(sym.STORE, "store"); }
"at"                                { return token(sym.AT, "at"); }
"export"                            { return token(sym.EXPORT, "export"); }
"add"                               { return token(sym.ADD, "add"); }
"update"                            { return token(sym.UPDATE, "update"); }
"set"                               { return token(sym.SET, "set"); }
"clear"                             { return token(sym.CLEAR, "clear"); }
"save"                              { return token(sym.SAVE, "save"); }

/* --------- Tipos de dato --------- */
"int"                               { return token(sym.T_INT, "tipo_int"); }
"float"                             { return token(sym.T_FLOAT, "tipo_float"); }
"bool"                              { return token(sym.T_BOOL, "tipo_bool"); }
"string"                            { return token(sym.T_STRING, "tipo_string"); }
"array"                             { return token(sym.T_ARRAY, "tipo_array"); }
"object"                            { return token(sym.T_OBJECT, "tipo_object"); }
"null"                              { return token(sym.NULL, "null", null); }

/* --------- Literales booleanos --------- */
"true"                              { return token(sym.BOOL, "bool", true); }
"false"                             { return token(sym.BOOL, "bool", false); }

/* --------- Operadores relacionales --------- */
"=="                                { return token(sym.IGUAL_IGUAL, "=="); }
"!="                                { return token(sym.DIFERENTE, "!="); }
">="                                { return token(sym.MAYOR_IGUAL, ">="); }
"<="                                { return token(sym.MENOR_IGUAL, "<="); }
">"                                 { return token(sym.MAYOR, ">"); }
"<"                                 { return token(sym.MENOR, "<"); }

/* --------- Operadores lógicos --------- */
"&&"                                { return token(sym.AND, "&&"); }
"||"                                { return token(sym.OR, "||"); }
"!"                                 { return token(sym.NOT, "!"); }

/* --------- Símbolos --------- */
"{"                                 { return token(sym.LLAVEIZQ, "{"); }
"}"                                 { return token(sym.LLAVEDER, "}"); }
"("                                 { return token(sym.PARIZQ, "("); }
")"                                 { return token(sym.PARDER, ")"); }
"["                                 { return token(sym.CORIZQ, "["); }
"]"                                 { return token(sym.CORDER, "]"); }

":"                                 { return token(sym.DOSP, ":"); }
";"                                 { return token(sym.PTCOMA, ";"); }
","                                 { return token(sym.COMA, ","); }
"="                                 { return token(sym.IGUAL, "="); }
"*"                                 { return token(sym.ASTERISCO, "*"); }

/* --------- Literales numéricas --------- */
{DEC}                               { return token(sym.DECIMAL, "decimal", Double.valueOf(yytext())); }
{INT}                               { return token(sym.ENTERO, "entero", Integer.valueOf(yytext())); }

/* --------- Strings --------- */
{STRING}                            {
                                        String raw = yytext();
                                        // quitar comillas
                                        String s = raw.substring(1, raw.length()-1);
                                        // escapes básicos
                                        s = s.replace("\\n", "\n")
                                             .replace("\\r", "\r")
                                             .replace("\\t", "\t")
                                             .replace("\\\"", "\"")
                                             .replace("\\\\", "\\");
                                        return token(sym.CADENA, "cadena", s);
                                    }

/* --------- Identificadores --------- */
{ID}                                { return token(sym.ID, "id", yytext()); }

/* --------- Cualquier otro carácter: ERROR LÉXICO --------- */
.                                   {
                                        String d = "El carácter \"" + yytext() + "\" no pertenece al lenguaje";
                                        errorLexico(d);
                                        // no retornamos token; seguimos escaneando
                                    }

<<EOF>>                              { return new Symbol(sym.EOF); }