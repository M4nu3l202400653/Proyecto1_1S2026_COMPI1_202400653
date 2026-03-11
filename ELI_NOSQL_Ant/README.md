# Entorno de Prueba - ELI NOSQL Compiler

## 📋 Descripción

Este proyecto es un **compilador/intérprete educativo** para el lenguaje **ELI-NOSQL**, capaz de:
- Analizar código con palabras clave, tipos, operadores y expresiones
- Crear bases de datos en memoria y persistirlas en JSON
- Ejecutar código incluso con errores léxicos/sintácticos
- Generar reportes de tokens y errores

## 🚀 Instalación Rápida

### 1. Compilar el proyecto
```powershell
PS> .\setup_test_env.ps1
```

Este script automáticamente:
- Crea directorios necesarios (`test_examples`, `test_output`, `databases`)
- Compila lexer, parser y código Java
- Verifica que todo esté listo

### 2. Ejecutar ejemplos
```powershell
PS> .\run_examples.ps1
```

Esto procesa automáticamente:
- `dificil.code` - E-commerce con tablas complejas
- `medio.code` - Sistema de colegio
- `prueba.code` - Universidad (con errores intencionales)

### 3. Abrir interfaz gráfica
```powershell
PS> .\run_app.ps1
```

O directamente:
```powershell
PS> ant run
```

## 📂 Estructura del Proyecto

```
ELI_NOSQL_Ant/
├── src/analizadores/
│   ├── Lexico.jflex              # Análisis léxico
│   ├── Sintactico.cup            # Análisis sintáctico
│   ├── Analizar.java             # Motor de análisis
│   ├── Analizadores.java         # GUI Swing
│   ├── instrucciones/            # Implementación de comandos
│   ├── ast/                       # Árbol de sintaxis
│   ├── data/                      # Estructuras de datos
│   └── persistencia/JsonManager.java  # Serialización JSON
├── build/classes/                # Código compilado
├── lib/                           # Dependencias (JFlex, CUP, etc.)
├── build.xml                      # Build Ant
├── compilar.bat                   # Script compilación
├── dificil.code                   # Ejemplo complejo
├── medio.code                     # Ejemplo intermedio
└── prueba.code                    # Ejemplo con errores
```

## 🔧 Comandos Útiles

### Compilación
```powershell
# Compilación completa
ant clean lexer parser compile

# Solo regenerar lexer
ant lexer

# Solo regenerar parser
ant parser

# Compilar sin limpiar
ant compile
```

### Ejecución
```powershell
# GUI completa
ant run

# Ejecutar archivo específico (desde código)
# En Analizadores.java: File → Abrir

# Tests headless
java -cp build/classes;lib/* analizadores.TestCLI
```

## 📝 Formato del Lenguaje ELI-NOSQL

### Ejemplo básico:
```eli
## Comentario de línea
#* comentario
   multilínea *#

database tienda {
  store at "tienda.json";
}

use tienda;

table productos {
  id: int;
  nombre: string;
  precio: float;
  disponible: bool;
  tags: array;
  meta: object;
}

add productos {
  id: 1,
  nombre: "Laptop",
  precio: 1500.50,
  disponible: true,
  tags: ["tech", "work"],
  meta: { marca: "Dell", ram: 16 }
};

read productos {
  fields: nombre, precio;
  filter: precio > 1000.0 && disponible == true;
};

export "resultado.json";
```

### Instrucciones soportadas:
- **database** - Crear BD
- **use** - Activar BD
- **table** - Crear tabla
- **add** - Insertar registro
- **update** - Actualizar registros
- **read** - Consultar
- **clear** - Vaciar tabla
- **export** - Exportar resultado a JSON
- **save** - Guardar BD a JSON

### Tipos de datos:
- `int` - Enteros
- `float` - Decimales
- `bool` - Booleanos
- `string` - Texto
- `array` - Arreglos `[1, 2, 3]`
- `object` - Objetos `{ campo: valor }`

### Operadores:
- Relacionales: `==`, `!=`, `>`, `<`, `>=`, `<=`
- Lógicos: `&&` (AND), `||` (OR), `!` (NOT)

## 🖥️ Interfaz Gráfica

### Menús:
- **Archivo**
  - Nuevo (Ctrl+N)
  - Abrir (Ctrl+O)
  - Guardar código (Ctrl+S)
  - Guardar BD activa (Ctrl+B)  ← **Para generar JSON**
  - Salir

### Pestaña Consola:
- Muestra salida de ejecución
- Errores semánticos reportados

### Pestaña Tokens:
- Lista de tokens reconocidos
- Lexema, tipo, línea, columna

### Pestaña Errores:
- Errores léxicos, sintácticos, semánticos y de ejecución
- Permite identificar problemas en el código

## ✅ Características Principales

✓ **Análisis tolerante** - Continúa ejecutando aunque haya errores
✓ **Persistencia automática** - Genera JSON automáticamente
✓ **Interfaz visual** - GUI Swing intuitiva
✓ **Recuperación de errores** - Parser resiliente
✓ **Tipos complejos** - Soporta arrays y objetos anidados
✓ **Filtros complejos** - Expresiones booleanas
✓ **Exportación** - Consultas a JSON

## ⚠️ Limitaciones y Notas

1. **Sin instrucción `use`** - Si creas BD pero no la activas, las operaciones fallarán
2. **Errores léxicos ignorados** - Caracteres inválidos se ignoran silenciosamente
3. **Sin índices** - Las búsquedas son lineales (para datos pequeños está bien)
4. **Sin transacciones** - Cada operación es independiente
5. **Guardado obligatorio** - Debes usar Ctrl+B o `save` para persistir

## 🧪 Archivos de Prueba Incluidos

### dificil.code
- **Propósito**: Prueba completa con tablas complejas
- **Contenido**: E-commerce con 3 tablas, múltiples operaciones
- **Esperado**: Genera `ecommerce.json` y `reporte_consultas.json`
- **Status**: ✅ Sin errores

### medio.code
- **Propósito**: Casos intermedios con algunas incoherencias
- **Contenido**: Sistema de colegio con errores sintácticos intencionales
- **Esperado**: Genera `colegio.json` y `estudiantes_activos.json`
- **Status**: ⚠️ Ejecuta a pesar de errores

### prueba.code
- **Propósito**: Prueba de tolerancia a errores
- **Contenido**: Caracteres inválidos (`@`, `$`, `#`, `%`, `&`, `^`, `~`)
- **Esperado**: **No genera JSON** (falta `use universidad;`)
- **Status**: ✗ Error semántico: Sin BD activa

## 📊 Flujo de Ejecución

```
Código fuente (.code)
         ↓
   [LEXER - JFlex]  → Tokens + Errores léxicos
         ↓
   [PARSER - CUP]   → AST + Errores sintácticos
         ↓
  [INTERPRETE]      → Ejecución + Errores semánticos
         ↓
  [JSON Manager]    → Archivo .json (si hay BD activa)
```

## 🛠️ Troubleshooting

### Problema: "No hay base de datos activa"
**Solución**: Asegúrate de:
1. Declarar la BD: `database miDB { store at "...json"; }`
2. Activarla: `use miDB;`
3. En ese orden

### Problema: No se genera JSON
**Solución**: 
- Presiona `Ctrl+B` o usa menú `Archivo → Guardar BD activa`
- O ejecuta `save "ruta.json";` en el código

### Problema: Errores compilando
**Solución**: 
```powershell
ant clean lexer parser compile
```

### Problema: GUI no abre
**Solución**: Asegúrate de que X11/display está disponible, o usa:
```powershell
java -cp "build\classes;lib\*" analizadores.TestCLI
```

## 📚 Referencias

- **JFlex**: Generador de lexers → `lib/jflex-1.9.1.jar`
- **CUP**: Generador de parsers → `lib/javacup.jar`
- **Ant**: Build tool → `build.xml`

## 🎯 Próximos Pasos

1. Experimenta con tus propios archivos `.code`
2. Modifica `Lexico.jflex` para agregar palabras clave
3. Extiende `Sintactico.cup` con nuevas instrucciones
4. Agrega métodos de agregación (SUM, COUNT, etc.)
5. Implementa persistencia en otras bases de datos

---

**Creado**: Marzo 2026
**Versión**: 1.0 (Tolerancia a errores + JSON)
