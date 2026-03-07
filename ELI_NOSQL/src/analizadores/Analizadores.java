package analizadores;

import analizadores.errores.ErrorSintactico;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class Analizadores extends JFrame {

    // ===== Componentes UI =====
    private final JTextArea        editorCodigo;
    private final JTextArea        consola;
    private final JTable           tablaTokens;
    private final JTable           tablaErrores;
    private final DefaultTableModel modeloTokens;
    private final DefaultTableModel modeloErrores;
    private final JTabbedPane      pestanas;

    // ===== Estado =====
    private final Analizar motor = new Analizar();
    private File archivoActual;

    private static final String[] COL_TOKENS  =
        {"Lexema", "Tipo", "Línea", "Columna"};
    private static final String[] COL_ERRORES =
        {"Tipo", "Descripción", "Línea", "Columna"};

    public Analizadores() {
        super("ELI NOSQL Compiler");

        // ===== Editor de código =====
        editorCodigo = new JTextArea();
        editorCodigo.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        editorCodigo.setTabSize(4);
        JScrollPane scrollEditor = new JScrollPane(editorCodigo);
        scrollEditor.setRowHeaderView(crearPanelLineas());

        // ===== Consola =====
        consola = new JTextArea();
        consola.setEditable(false);
        consola.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        consola.setBackground(new Color(30, 30, 30));
        consola.setForeground(Color.WHITE);
        consola.setCaretColor(Color.WHITE);

        // ===== Tabla de tokens =====
        modeloTokens = new DefaultTableModel(COL_TOKENS, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaTokens = new JTable(modeloTokens);
        estilizarTabla(tablaTokens);

        // ===== Tabla de errores =====
        modeloErrores = new DefaultTableModel(COL_ERRORES, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaErrores = new JTable(modeloErrores);
        estilizarTabla(tablaErrores);

        // ===== Pestañas =====
        pestanas = new JTabbedPane();
        pestanas.addTab("Consola", new JScrollPane(consola));
        pestanas.addTab("Tokens",  new JScrollPane(tablaTokens));
        pestanas.addTab("Errores", new JScrollPane(tablaErrores));

        // ===== Split pane =====
        JSplitPane split = new JSplitPane(
            JSplitPane.VERTICAL_SPLIT, scrollEditor, pestanas);
        split.setResizeWeight(0.60);

        // ===== Toolbar =====
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        JButton btnEjecutar = new JButton("Ejecutar \u25B6");
        btnEjecutar.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        btnEjecutar.setFocusPainted(false);
        btnEjecutar.addActionListener(e -> ejecutar());
        toolbar.add(btnEjecutar);
        toolbar.addSeparator();
        JLabel lblInfo = new JLabel("  F5 para ejecutar  |  Ctrl+S guardar");
        lblInfo.setForeground(Color.GRAY);
        toolbar.add(lblInfo);

        // ===== Menú =====
        setJMenuBar(construirMenu());

        // ===== Layout =====
        add(toolbar, BorderLayout.NORTH);
        add(split,   BorderLayout.CENTER);

        // Atajo F5
        getRootPane().registerKeyboardAction(
            e -> ejecutar(),
            KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
    }

    // ====================================================================
    // Ejecución
    // ====================================================================
    private void ejecutar() {
        String codigo = editorCodigo.getText();
        motor.analizar(codigo);

        consola.setText("");
        modeloTokens.setRowCount(0);
        modeloErrores.setRowCount(0);

        // Tokens
        for (Lexico.TokenInfo t : motor.getTokens()) {
            modeloTokens.addRow(new Object[]{
                t.lexema, t.tipo, t.linea, t.columna
            });
        }

        // Errores
        boolean hayErrores = false;

        for (Lexico.ErrorLexico e : motor.getErroresLexicos()) {
            modeloErrores.addRow(new Object[]{
                e.tipo, e.descripcion, e.linea, e.columna
            });
            hayErrores = true;
        }
        for (ErrorSintactico e : motor.getErroresSintacticos()) {
            modeloErrores.addRow(new Object[]{
                e.tipo, e.descripcion, e.linea, e.columna
            });
            hayErrores = true;
        }
        for (String[] e : motor.getErroresEjecucion()) {
            modeloErrores.addRow(e);
            hayErrores = true;
        }

        // Consola
        String salida = motor.getSalidaConsola();
        if (!salida.isBlank()) {
            consola.setText(salida);
        } else if (motor.tuvoErroresPrevios()) {
            consola.setText(
                "Ejecución detenida: hay errores léxicos o sintácticos.\n");
        }

        // Actualizar títulos de pestañas con conteo
        int totalErrores = motor.getErroresLexicos().size()
                         + motor.getErroresSintacticos().size()
                         + motor.getErroresEjecucion().size();
        pestanas.setTitleAt(1, "Tokens (" + motor.getTokens().size() + ")");
        pestanas.setTitleAt(2, "Errores (" + totalErrores + ")");

        pestanas.setSelectedIndex(hayErrores ? 2 : 0);

        // tras ejecutar cualquier script, si hay una BD activa con ruta, guardar automáticamente
        try {
            var ts = motor.getTablaSimbolos();
            if (ts != null) {
                var db = ts.getDbActiva();
                if (db != null && db.getRutaArchivo() != null) {
                    analizadores.persistencia.JsonManager.guardar(db, db.getRutaArchivo());
                    consola.append("\n(auto) base de datos guardada en: " + db.getRutaArchivo());
                }
            }
        } catch (Exception ignored) {
            // ignorar errores de guardado para no bloquear la UI
        }
    }

    // ====================================================================
    // Operaciones de archivo
    // ====================================================================
    private void nuevoArchivo() {
        if (!editorCodigo.getText().isEmpty()) {
            int opt = JOptionPane.showConfirmDialog(this,
                "¿Descartar los cambios actuales?",
                "Nuevo archivo", JOptionPane.YES_NO_OPTION);
            if (opt != JOptionPane.YES_OPTION) return;
        }
        editorCodigo.setText("");
        archivoActual = null;
        setTitle("ELI NOSQL Compiler - Nuevo");
    }

    private void abrirArchivo() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter(
            "Archivos ELI (*.code)", "code"));
        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        archivoActual = fc.getSelectedFile();
        try {
            String contenido = Files.readString(
                archivoActual.toPath(), StandardCharsets.UTF_8);
            editorCodigo.setText(contenido);
            editorCodigo.setCaretPosition(0);
            setTitle("ELI NOSQL Compiler - " + archivoActual.getName());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                "Error al abrir: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void guardarArchivo() {
        if (archivoActual == null) {
            JFileChooser fc = new JFileChooser();
            fc.setFileFilter(new FileNameExtensionFilter(
                "Archivos ELI (*.code)", "code"));
            if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
            archivoActual = fc.getSelectedFile();
            if (!archivoActual.getName().endsWith(".code")) {
                archivoActual = new File(
                    archivoActual.getAbsolutePath() + ".code");
            }
        }
        try {
            Files.writeString(archivoActual.toPath(),
                editorCodigo.getText(), StandardCharsets.UTF_8);
            setTitle("ELI NOSQL Compiler - " + archivoActual.getName());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                "Error al guardar: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void guardarBaseDatosActiva() {
        // save current database via JsonManager
        analizadores.tablas.TablaSimbolos ts = motor.getTablaSimbolos();
        if (ts == null) {
            JOptionPane.showMessageDialog(this,
                "No hay tabla de símbolos disponible.",
                "Guardar BD", JOptionPane.WARNING_MESSAGE);
            return;
        }
        analizadores.data.DatabaseMemory db = ts.getDbActiva();
        if (db == null) {
            JOptionPane.showMessageDialog(this,
                "No hay base de datos activa.",
                "Guardar BD", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String ruta = db.getRutaArchivo();
        if (ruta == null || ruta.isBlank()) {
            JFileChooser fc = new JFileChooser();
            fc.setDialogTitle("Guardar base de datos como...");
            if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
            File sel = fc.getSelectedFile();
            ruta = sel.getAbsolutePath();
            if (!ruta.toLowerCase().endsWith(".json")) ruta += ".json";
            db.setRutaArchivo(ruta);
        }
        try {
            analizadores.persistencia.JsonManager.guardar(db, ruta);
            JOptionPane.showMessageDialog(this,
                "Base de datos guardada en:\n" + ruta,
                "Guardar BD", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Error al guardar base de datos:\n" + ex.getMessage(),
                "Guardar BD", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ====================================================================
    // Helpers UI
    // ====================================================================
    private JMenuBar construirMenu() {
        JMenuBar mb = new JMenuBar();
        JMenu menuArchivo = new JMenu("Archivo");
        menuArchivo.setMnemonic(KeyEvent.VK_A);

        JMenuItem miNuevo   = new JMenuItem("Nuevo");
        miNuevo.setAccelerator(KeyStroke.getKeyStroke(
            KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
        miNuevo.addActionListener(e -> nuevoArchivo());

        JMenuItem miAbrir   = new JMenuItem("Abrir...");
        miAbrir.setAccelerator(KeyStroke.getKeyStroke(
            KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
        miAbrir.addActionListener(e -> abrirArchivo());

        JMenuItem miGuardar = new JMenuItem("Guardar");
        miGuardar.setAccelerator(KeyStroke.getKeyStroke(
            KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
        miGuardar.addActionListener(e -> guardarArchivo());

        JMenuItem miGuardarBD = new JMenuItem("Guardar BD activa");
        miGuardarBD.setAccelerator(KeyStroke.getKeyStroke(
            KeyEvent.VK_B, InputEvent.CTRL_DOWN_MASK));
        miGuardarBD.addActionListener(e -> guardarBaseDatosActiva());

        JMenuItem miSalir   = new JMenuItem("Salir");
        miSalir.addActionListener(e -> System.exit(0));

        menuArchivo.add(miNuevo);
        menuArchivo.add(miAbrir);
        menuArchivo.add(miGuardar);
        menuArchivo.add(miGuardarBD);
        menuArchivo.addSeparator();
        menuArchivo.add(miSalir);
        mb.add(menuArchivo);
        return mb;
    }

    private JTextArea crearPanelLineas() {
        JTextArea nums = new JTextArea("1\n");
        nums.setEditable(false);
        nums.setBackground(new Color(230, 230, 230));
        nums.setForeground(new Color(100, 100, 100));
        nums.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        nums.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));

        editorCodigo.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e)  { actualizarLineas(nums); }
            public void removeUpdate(DocumentEvent e)  { actualizarLineas(nums); }
            public void changedUpdate(DocumentEvent e) { actualizarLineas(nums); }
        });
        return nums;
    }

    private void actualizarLineas(JTextArea nums) {
        int lines = editorCodigo.getLineCount();
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= lines; i++) sb.append(i).append("\n");
        nums.setText(sb.toString());
    }

    private void estilizarTabla(JTable tabla) {
        tabla.setRowHeight(22);
        tabla.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        tabla.getTableHeader().setFont(
            new Font(Font.SANS_SERIF, Font.BOLD, 12));
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        tabla.setFillsViewportHeight(true);
        tabla.setGridColor(new Color(210, 210, 210));
    }

    // ====================================================================
    // Main
    // ====================================================================
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
            new Analizadores().setVisible(true);
        });
    }
}
