import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.print.*;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class SistemaAparcamientoPro extends JFrame implements Printable {
    private static final String VERSION = "1.2.0";
    private static final String DATA_FILE = "aparcamiento_data.txt";
    private static final String CONFIG_FILE = "config.properties";
    private static final String TICKETS_FILE = "tickets_activos.txt";
    
    // Colores del tema
    private static final Color PRIMARY_COLOR = new Color(41, 128, 185);
    private static final Color SECONDARY_COLOR = new Color(52, 152, 219);
    private static final Color SUCCESS_COLOR = new Color(39, 174, 96);
    private static final Color WARNING_COLOR = new Color(243, 156, 18);
    private static final Color DANGER_COLOR = new Color(231, 76, 60);
    private static final Color DARK_COLOR = new Color(44, 62, 80);
    private static final Color LIGHT_COLOR = new Color(236, 240, 241);
    private static final Color WHITE_COLOR = Color.WHITE;
    
    private Map<String, VehicleInfo> espacios;
    private Map<String, TicketInfo> ticketsActivos;
    private JTextArea areaEstado;
    private JTextField campoPlaca;
    private JTextField campoEspacio;
    private JTextField campoTicket;
    private JLabel lblEstadisticas;
    private JLabel lblTitulo;
    private JLabel lblReloj;
    private JLabel lblOcupacion;
    private JProgressBar barraOcupacion;
    private JPanel panelVistaGrafica;
    private JPanel panelMetricas;
    private JTextArea areaHistorial;
    private Properties config;
    private int totalEspacios;
    private String ticketParaImprimir = "";
    private Timer timerActualizacion;
    private boolean modoOscuro = false;
    
    static class VehicleInfo {
        String placa;
        LocalDateTime horaEntrada;
        String codigoTicket;
        
        VehicleInfo(String placa, LocalDateTime horaEntrada, String codigoTicket) {
            this.placa = placa;
            this.horaEntrada = horaEntrada;
            this.codigoTicket = codigoTicket;
        }
        
        @Override
        public String toString() {
            return placa + "|" + horaEntrada.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "|" + codigoTicket;
        }
        
        static VehicleInfo fromString(String str) {
            try {
                String[] parts = str.split("\\|");
                if (parts.length >= 2) {
                    String codigo = parts.length > 2 ? parts[2] : generateTicketCode();
                    return new VehicleInfo(parts[0], LocalDateTime.parse(parts[1]), codigo);
                }
            } catch (Exception e) {
                System.err.println("Error parsing vehicle info: " + e.getMessage());
            }
            return null;
        }
    }
    
    static class TicketInfo {
        String codigoTicket;
        String placa;
        String espacio;
        LocalDateTime horaEntrada;
        double tarifaHora;
        
        TicketInfo(String codigoTicket, String placa, String espacio, LocalDateTime horaEntrada, double tarifaHora) {
            this.codigoTicket = codigoTicket;
            this.placa = placa;
            this.espacio = espacio;
            this.horaEntrada = horaEntrada;
            this.tarifaHora = tarifaHora;
        }
        
        @Override
        public String toString() {
            return codigoTicket + "|" + placa + "|" + espacio + "|" + 
                   horaEntrada.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "|" + tarifaHora;
        }
        
        static TicketInfo fromString(String str) {
            try {
                String[] parts = str.split("\\|");
                if (parts.length == 5) {
                    return new TicketInfo(parts[0], parts[1], parts[2], 
                                        LocalDateTime.parse(parts[3]), Double.parseDouble(parts[4]));
                }
            } catch (Exception e) {
                System.err.println("Error parsing ticket info: " + e.getMessage());
            }
            return null;
        }
    }
    
    public SistemaAparcamientoPro() {
        cargarConfiguracion();
        espacios = new HashMap<>();
        ticketsActivos = new HashMap<>();
        for (int i = 1; i <= totalEspacios; i++) {
            espacios.put("E" + i, null);
        }
        cargarDatos();
        cargarTickets();
        initUI();
        configurarCierreSeguro();
        iniciarTimerActualizacion();
    }
    
    private void cargarConfiguracion() {
        config = new Properties();
        try (InputStream input = new FileInputStream(CONFIG_FILE)) {
            config.load(input);
            totalEspacios = Integer.parseInt(config.getProperty("total.espacios", "20"));
        } catch (IOException e) {
            totalEspacios = 20;
            crearConfiguracionPorDefecto();
        }
    }
    
    private void crearConfiguracionPorDefecto() {
        config.setProperty("total.espacios", "20");
        config.setProperty("tarifa.hora", "2.50");
        config.setProperty("empresa.nombre", "ParkingPro System");
        config.setProperty("empresa.direccion", "Av. Principal 123");
        config.setProperty("empresa.telefono", "(555) 123-4567");
        
        // Configuración de tarifas
        config.setProperty("minutos.gracia", "15"); // 15 minutos gratis
        config.setProperty("minutos.minimos", "30"); // Mínimo 30 minutos
        config.setProperty("tipo.calculo", "fraccion"); // fraccion, hora_completa, minuto_exacto
        
        try (OutputStream output = new FileOutputStream(CONFIG_FILE)) {
            config.store(output, "Configuración del Sistema de Aparcamiento");
        } catch (IOException e) {
            System.err.println("Error creando configuración: " + e.getMessage());
        }
    }
    
    private void initUI() {
        setTitle(config.getProperty("empresa.nombre", "Sistema de Aparcamiento") + " v" + VERSION);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Configurar Look and Feel del sistema
        
        // Panel superior con dashboard avanzado
        JPanel panelSuperior = crearPanelGradiente(new Color(34, 45, 65), new Color(52, 73, 94));
        panelSuperior.setLayout(new BorderLayout());
        panelSuperior.setBorder(new EmptyBorder(15, 25, 15, 25));
        
        // Panel de título con reloj
        JPanel panelTitulo = new JPanel(new BorderLayout());
        panelTitulo.setOpaque(false);
        
        lblTitulo = new JLabel("[P] " + config.getProperty("empresa.nombre", "PARKING PRO SYSTEM").toUpperCase());
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 24));
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Reloj en tiempo real
        lblReloj = new JLabel();
        lblReloj.setFont(new Font("Courier New", Font.BOLD, 16));
        lblReloj.setForeground(new Color(255, 215, 0));
        lblReloj.setHorizontalAlignment(SwingConstants.RIGHT);
        
        panelTitulo.add(lblTitulo, BorderLayout.CENTER);
        panelTitulo.add(lblReloj, BorderLayout.EAST);
        
        // Panel de métricas con indicadores visuales
        panelMetricas = new JPanel(new GridLayout(1, 4, 15, 0));
        panelMetricas.setOpaque(false);
        
        // Barra de ocupación
        barraOcupacion = new JProgressBar(0, 100);
        barraOcupacion.setStringPainted(true);
        barraOcupacion.setFont(new Font("Arial", Font.BOLD, 11));
        barraOcupacion.setForeground(new Color(46, 204, 113));
        barraOcupacion.setBackground(new Color(52, 73, 94));
        
        // Etiquetas de métricas
        lblOcupacion = new JLabel("[OCUPACION: 0%]");
        lblOcupacion.setFont(new Font("Arial", Font.BOLD, 12));
        lblOcupacion.setForeground(Color.WHITE);
        lblOcupacion.setHorizontalAlignment(SwingConstants.CENTER);
        
        lblEstadisticas = new JLabel();
        lblEstadisticas.setFont(new Font("Arial", Font.BOLD, 12));
        lblEstadisticas.setForeground(Color.WHITE);
        lblEstadisticas.setHorizontalAlignment(SwingConstants.CENTER);
        
        JLabel lblIndicador = crearIndicadorLED("SISTEMA ACTIVO", Color.GREEN);
        
        panelMetricas.add(lblOcupacion);
        panelMetricas.add(barraOcupacion);
        panelMetricas.add(lblEstadisticas);
        panelMetricas.add(lblIndicador);
        
        panelSuperior.add(panelTitulo, BorderLayout.NORTH);
        panelSuperior.add(panelMetricas, BorderLayout.CENTER);
        
        // Panel de controles con diseño moderno
        JPanel panelControles = new JPanel();
        panelControles.setLayout(new BoxLayout(panelControles, BoxLayout.Y_AXIS));
        panelControles.setBackground(WHITE_COLOR);
        panelControles.setBorder(new CompoundBorder(
            new EmptyBorder(25, 25, 25, 25),
            new TitledBorder(null, "[VEHICULOS] Gestión y Control", 
                TitledBorder.LEFT, TitledBorder.TOP, 
                new Font("Arial", Font.BOLD, 16), new Color(34, 45, 65))
        ));
        
        // Campos de entrada con estilo
        JPanel panelCampos = new JPanel(new GridBagLayout());
        panelCampos.setBackground(WHITE_COLOR);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 5, 10, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Campo Placa
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel lblPlaca = new JLabel("[PLACA]");
        lblPlaca.setFont(new Font("Arial", Font.BOLD, 12));
        lblPlaca.setForeground(new Color(34, 45, 65));
        panelCampos.add(lblPlaca, gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        campoPlaca = crearCampoTexto();
        panelCampos.add(campoPlaca, gbc);
        
        // Campo Espacio
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE;
        JLabel lblEspacio = new JLabel("[ESPACIO]");
        lblEspacio.setFont(new Font("Arial", Font.BOLD, 12));
        lblEspacio.setForeground(new Color(34, 45, 65));
        panelCampos.add(lblEspacio, gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        campoEspacio = crearCampoTexto();
        panelCampos.add(campoEspacio, gbc);
        
        // Campo Ticket
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE;
        JLabel lblTicket = new JLabel("[TICKET]");
        lblTicket.setFont(new Font("Arial", Font.BOLD, 12));
        lblTicket.setForeground(new Color(34, 45, 65));
        panelCampos.add(lblTicket, gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        campoTicket = crearCampoTexto();
        panelCampos.add(campoTicket, gbc);
        
        panelControles.add(panelCampos);
        
        // Panel de botones principales con diseño moderno
        JPanel panelBotonesPrincipales = new JPanel(new GridLayout(2, 3, 10, 10));
        panelBotonesPrincipales.setBackground(WHITE_COLOR);
        panelBotonesPrincipales.setBorder(new EmptyBorder(20, 0, 10, 0));
        
        JButton btnEntrada = crearBoton("[+] ENTRADA", new Color(46, 204, 113));
        JButton btnSalida = crearBoton("[-] SALIDA", new Color(231, 76, 60));
        JButton btnBuscar = crearBoton("[?] BUSCAR", new Color(52, 152, 219));
        JButton btnLeerTicket = crearBoton("[T] LEER TICKET", new Color(230, 126, 34));
        JButton btnReporte = crearBoton("[R] REPORTE", new Color(34, 45, 65));
        JButton btnImprimirTicket = crearBoton("[P] REIMPRIMIR", new Color(142, 68, 173));
        
        panelBotonesPrincipales.add(btnEntrada);
        panelBotonesPrincipales.add(btnSalida);
        panelBotonesPrincipales.add(btnBuscar);
        panelBotonesPrincipales.add(btnLeerTicket);
        panelBotonesPrincipales.add(btnReporte);
        panelBotonesPrincipales.add(btnImprimirTicket);
        
        panelControles.add(panelBotonesPrincipales);
        
        // Área de estado con diseño mejorado
        areaEstado = new JTextArea(20, 50);
        areaEstado.setEditable(false);
        areaEstado.setFont(new Font("Courier New", Font.PLAIN, 12));
        areaEstado.setBackground(new Color(248, 249, 250));
        areaEstado.setForeground(DARK_COLOR);
        areaEstado.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        JScrollPane scrollPane = new JScrollPane(areaEstado);
        scrollPane.setBorder(new CompoundBorder(
            new EmptyBorder(15, 15, 15, 15),
            new TitledBorder(null, "[MONITOR] Estado del Aparcamiento", 
                TitledBorder.LEFT, TitledBorder.TOP, 
                new Font("Arial", Font.BOLD, 16), new Color(34, 45, 65))
        ));
        scrollPane.setBackground(WHITE_COLOR);
        
        // Panel inferior con botones de administración
        JPanel panelAdmin = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        panelAdmin.setBackground(new Color(248, 249, 250));
        panelAdmin.setBorder(new EmptyBorder(10, 20, 10, 20));
        
        JButton btnActualizar = crearBotonSecundario("[↻] ACTUALIZAR");
        JButton btnExportar = crearBotonSecundario("[↗] EXPORTAR");
        JButton btnConfig = crearBotonSecundario("[⚙] CONFIGURACION");
        JButton btnTema = crearBotonSecundario("[☼] TEMA");
        
        panelAdmin.add(btnTema);
        
        panelAdmin.add(btnActualizar);
        panelAdmin.add(btnExportar);
        panelAdmin.add(btnConfig);
        
        // Eventos
        btnEntrada.addActionListener(e -> registrarEntrada());
        btnSalida.addActionListener(e -> registrarSalida());
        btnBuscar.addActionListener(e -> buscarVehiculo());
        btnLeerTicket.addActionListener(e -> leerTicket());
        btnReporte.addActionListener(e -> generarReporte());
        btnImprimirTicket.addActionListener(e -> reimprimirTicket());
        btnActualizar.addActionListener(e -> actualizarEstado());
        btnExportar.addActionListener(e -> exportarDatos());
        btnConfig.addActionListener(e -> mostrarConfiguracion());
        btnTema.addActionListener(e -> cambiarTema());
        
        // Panel central con vista gráfica y estado
        JPanel panelCentral = new JPanel(new BorderLayout());
        
        // Vista gráfica del parking
        panelVistaGrafica = crearVistaGraficaParking();
        JScrollPane scrollVistaGrafica = new JScrollPane(panelVistaGrafica);
        scrollVistaGrafica.setBorder(new TitledBorder("[VISTA] Plano del Aparcamiento"));
        scrollVistaGrafica.setPreferredSize(new Dimension(400, 300));
        
        // Panel con pestañas
        JTabbedPane pestanas = new JTabbedPane();
        pestanas.setFont(new Font("Arial", Font.BOLD, 11));
        pestanas.addTab("[MONITOR] Estado", scrollPane);
        pestanas.addTab("[VISTA] Gráfica", scrollVistaGrafica);
        pestanas.addTab("[LOG] Historial", crearPanelHistorial());
        
        panelCentral.add(pestanas, BorderLayout.CENTER);
        
        // Layout principal
        add(panelSuperior, BorderLayout.NORTH);
        add(panelControles, BorderLayout.WEST);
        add(panelCentral, BorderLayout.CENTER);
        add(panelAdmin, BorderLayout.SOUTH);
        
        // Configuración final de la ventana
        getContentPane().setBackground(WHITE_COLOR);
        actualizarEstado();
        pack();
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(1400, 900));
        
        // Configurar tooltips
        configurarTooltips();
        
        // Icono de la aplicación
        try {
            setIconImage(Toolkit.getDefaultToolkit().createImage("parking-icon.png"));
        } catch (Exception e) {
            // Usar icono por defecto
        }
    }
    
    // Métodos auxiliares para crear componentes con diseño moderno
    private JTextField crearCampoTexto() {
        JTextField campo = new JTextField(20);
        campo.setFont(new Font("Consolas", Font.PLAIN, 14));
        campo.setBorder(new CompoundBorder(
            new LineBorder(new Color(52, 152, 219), 2, false),
            new EmptyBorder(10, 15, 10, 15)
        ));
        campo.setBackground(new Color(248, 249, 250));
        
        // Validación visual en tiempo real
        campo.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                validarCampoVisualmente(campo);
            }
        });
        
        return campo;
    }
    
    private void validarCampoVisualmente(JTextField campo) {
        String texto = campo.getText().trim();
        
        if (campo == campoPlaca) {
            if (texto.isEmpty()) {
                campo.setBorder(new CompoundBorder(
                    new LineBorder(new Color(149, 165, 166), 2),
                    new EmptyBorder(10, 15, 10, 15)
                ));
            } else if (texto.length() >= 3) {
                campo.setBorder(new CompoundBorder(
                    new LineBorder(new Color(46, 204, 113), 2),
                    new EmptyBorder(10, 15, 10, 15)
                ));
            } else {
                campo.setBorder(new CompoundBorder(
                    new LineBorder(new Color(231, 76, 60), 2),
                    new EmptyBorder(10, 15, 10, 15)
                ));
            }
        } else if (campo == campoEspacio) {
            if (texto.isEmpty() || espacios.containsKey(texto.toUpperCase())) {
                campo.setBorder(new CompoundBorder(
                    new LineBorder(new Color(46, 204, 113), 2),
                    new EmptyBorder(10, 15, 10, 15)
                ));
            } else {
                campo.setBorder(new CompoundBorder(
                    new LineBorder(new Color(231, 76, 60), 2),
                    new EmptyBorder(10, 15, 10, 15)
                ));
            }
        }
    }
    
    private void cambiarTema() {
        modoOscuro = !modoOscuro;
        
        Color fondoPrincipal = modoOscuro ? new Color(44, 62, 80) : Color.WHITE;
        Color fondoSecundario = modoOscuro ? new Color(52, 73, 94) : new Color(248, 249, 250);
        Color textoColor = modoOscuro ? Color.WHITE : new Color(34, 45, 65);
        
        // Aplicar tema a componentes principales
        getContentPane().setBackground(fondoPrincipal);
        
        if (areaEstado != null) {
            areaEstado.setBackground(fondoSecundario);
            areaEstado.setForeground(textoColor);
        }
        
        if (areaHistorial != null) {
            areaHistorial.setBackground(fondoSecundario);
            areaHistorial.setForeground(textoColor);
        }
        
        repaint();
        agregarAlHistorial("Tema cambiado a " + (modoOscuro ? "oscuro" : "claro"));
    }
    
    private JButton crearBoton(String texto, Color color) {
        JButton boton = new JButton(texto);
        boton.setFont(new Font("Arial", Font.BOLD, 11));
        boton.setBackground(color);
        boton.setForeground(Color.WHITE);
        boton.setBorder(new CompoundBorder(
            new LineBorder(color.darker(), 1),
            new EmptyBorder(15, 25, 15, 25)
        ));
        boton.setFocusPainted(false);
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Efectos hover mejorados
        boton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                boton.setBackground(color.brighter());
                boton.setBorder(new CompoundBorder(
                    new LineBorder(color, 2),
                    new EmptyBorder(14, 24, 14, 24)
                ));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                boton.setBackground(color);
                boton.setBorder(new CompoundBorder(
                    new LineBorder(color.darker(), 1),
                    new EmptyBorder(15, 25, 15, 25)
                ));
            }
        });
        
        return boton;
    }
    
    private JButton crearBotonSecundario(String texto) {
        JButton boton = new JButton(texto);
        boton.setFont(new Font("Arial", Font.BOLD, 10));
        boton.setBackground(new Color(236, 240, 241));
        boton.setForeground(new Color(34, 45, 65));
        boton.setBorder(new CompoundBorder(
            new LineBorder(new Color(149, 165, 166), 1),
            new EmptyBorder(10, 20, 10, 20)
        ));
        boton.setFocusPainted(false);
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        boton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                boton.setBackground(new Color(52, 152, 219));
                boton.setForeground(Color.WHITE);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                boton.setBackground(new Color(236, 240, 241));
                boton.setForeground(new Color(34, 45, 65));
            }
        });
        
        return boton;
    }
    
    // Métodos para componentes visuales avanzados
    private JPanel crearPanelGradiente(Color color1, Color color2) {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                GradientPaint gp = new GradientPaint(0, 0, color1, 0, getHeight(), color2);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
    }
    
    private JLabel crearIndicadorLED(String texto, Color color) {
        JLabel indicador = new JLabel("● " + texto);
        indicador.setFont(new Font("Arial", Font.BOLD, 12));
        indicador.setForeground(color);
        indicador.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Efecto de parpadeo para indicador activo
        Timer parpadeo = new Timer(1000, e -> {
            indicador.setForeground(indicador.getForeground().equals(color) ? 
                color.darker() : color);
        });
        parpadeo.start();
        
        return indicador;
    }
    
    private JPanel crearVistaGraficaParking() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                dibujarPlanoParkingGrafico(g);
            }
        };
        panel.setBackground(new Color(248, 249, 250));
        panel.setPreferredSize(new Dimension(600, 400));
        return panel;
    }
    
    private void dibujarPlanoParkingGrafico(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int espacioWidth = 80;
        int espacioHeight = 40;
        int margen = 20;
        int espaciosPorFila = 5;
        
        for (int i = 1; i <= totalEspacios; i++) {
            String espacio = "E" + i;
            VehicleInfo vehiculo = espacios.get(espacio);
            
            int fila = (i - 1) / espaciosPorFila;
            int columna = (i - 1) % espaciosPorFila;
            
            int x = margen + columna * (espacioWidth + 10);
            int y = margen + fila * (espacioHeight + 15);
            
            // Color según estado
            if (vehiculo == null) {
                g2d.setColor(new Color(46, 204, 113)); // Verde - libre
            } else {
                g2d.setColor(new Color(231, 76, 60)); // Rojo - ocupado
            }
            
            // Dibujar espacio con sombra
            g2d.setColor(Color.GRAY);
            g2d.fillRoundRect(x + 2, y + 2, espacioWidth, espacioHeight, 10, 10);
            
            if (vehiculo == null) {
                g2d.setColor(new Color(46, 204, 113));
            } else {
                g2d.setColor(new Color(231, 76, 60));
            }
            g2d.fillRoundRect(x, y, espacioWidth, espacioHeight, 10, 10);
            
            // Borde
            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRoundRect(x, y, espacioWidth, espacioHeight, 10, 10);
            
            // Texto del espacio
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 12));
            FontMetrics fm = g2d.getFontMetrics();
            String texto = espacio;
            if (vehiculo != null) {
                texto += "\n" + vehiculo.placa;
            }
            
            int textX = x + (espacioWidth - fm.stringWidth(espacio)) / 2;
            int textY = y + (espacioHeight + fm.getHeight()) / 2;
            g2d.drawString(espacio, textX, textY - 5);
            
            if (vehiculo != null) {
                g2d.setFont(new Font("Arial", Font.PLAIN, 10));
                fm = g2d.getFontMetrics();
                textX = x + (espacioWidth - fm.stringWidth(vehiculo.placa)) / 2;
                g2d.drawString(vehiculo.placa, textX, textY + 10);
            }
        }
    }
    
    private JScrollPane crearPanelHistorial() {
        areaHistorial = new JTextArea(15, 40);
        areaHistorial.setEditable(false);
        areaHistorial.setFont(new Font("Courier New", Font.PLAIN, 11));
        areaHistorial.setBackground(new Color(248, 249, 250));
        areaHistorial.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JScrollPane scroll = new JScrollPane(areaHistorial);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        
        return scroll;
    }
    
    private void configurarTooltips() {
        campoPlaca.setToolTipText("Ingrese la placa del vehículo (ej: ABC123)");
        campoEspacio.setToolTipText("Espacio de aparcamiento (ej: E1, E2...) - Opcional para entrada");
        campoTicket.setToolTipText("Código del ticket para operaciones de salida");
        barraOcupacion.setToolTipText("Porcentaje de ocupación actual del aparcamiento");
    }
    
    private void iniciarTimerActualizacion() {
        timerActualizacion = new Timer(1000, e -> {
            // Actualizar reloj
            lblReloj.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
            
            // Actualizar vista gráfica cada 5 segundos
            if (panelVistaGrafica != null) {
                panelVistaGrafica.repaint();
            }
        });
        timerActualizacion.start();
    }
    
    private void agregarAlHistorial(String evento) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        String entrada = String.format("[%s] %s\n", timestamp, evento);
        
        if (areaHistorial != null) {
            areaHistorial.append(entrada);
            areaHistorial.setCaretPosition(areaHistorial.getDocument().getLength());
        }
    }
    
    // Métodos para cálculo optimizado de tarifas
    private double calcularTarifaOptimizada(long minutos, double tarifaHora) {
        // Configuración de tarifas (se puede hacer configurable)
        int minutosGracia = Integer.parseInt(config.getProperty("minutos.gracia", "15")); // 15 min gratis
        int minutosMinimos = Integer.parseInt(config.getProperty("minutos.minimos", "30")); // Mínimo 30 min
        String tipoCalculo = config.getProperty("tipo.calculo", "fraccion"); // fraccion, hora_completa, minuto_exacto
        
        // Aplicar minutos de gracia
        if (minutos <= minutosGracia) {
            return 0.0;
        }
        
        // Aplicar mínimo
        long minutosACobrar = Math.max(minutos, minutosMinimos);
        
        switch (tipoCalculo) {
            case "minuto_exacto":
                // Cobro por minuto exacto
                return (minutosACobrar / 60.0) * tarifaHora;
                
            case "fraccion":
                // Cobro por fracciones de 15 minutos
                long fracciones = (minutosACobrar + 14) / 15; // Redondear hacia arriba cada 15 min
                return (fracciones * 15 / 60.0) * tarifaHora;
                
            case "hora_completa":
            default:
                // Cobro por hora completa (sistema original)
                long horas = (minutosACobrar + 59) / 60; // Redondear hacia arriba
                return horas * tarifaHora;
        }
    }
    
    private String obtenerDetallesTiempo(long minutos) {
        long horas = minutos / 60;
        long minutosRestantes = minutos % 60;
        
        if (horas == 0) {
            return String.format("%d minutos", minutos);
        } else if (minutosRestantes == 0) {
            return String.format("%d hora%s", horas, horas > 1 ? "s" : "");
        } else {
            return String.format("%d hora%s y %d minuto%s", 
                horas, horas > 1 ? "s" : "", 
                minutosRestantes, minutosRestantes > 1 ? "s" : "");
        }
    }
    
    private String obtenerDetalleCalculo(long minutos, double tarifaHora) {
        int minutosGracia = Integer.parseInt(config.getProperty("minutos.gracia", "15"));
        int minutosMinimos = Integer.parseInt(config.getProperty("minutos.minimos", "30"));
        String tipoCalculo = config.getProperty("tipo.calculo", "fraccion");
        
        if (minutos <= minutosGracia) {
            return String.format("Gratis (≤%d min)", minutosGracia);
        }
        
        long minutosACobrar = Math.max(minutos, minutosMinimos);
        
        switch (tipoCalculo) {
            case "minuto_exacto":
                return String.format("%.1f min × $%.2f/hora", minutosACobrar / 60.0 * 60, tarifaHora);
                
            case "fraccion":
                long fracciones = (minutosACobrar + 14) / 15;
                return String.format("%d fracciones de 15min × $%.2f/hora", fracciones, tarifaHora);
                
            case "hora_completa":
            default:
                long horas = (minutosACobrar + 59) / 60;
                return String.format("%d hora%s × $%.2f", horas, horas > 1 ? "s" : "", tarifaHora);
        }
    }
    
    private void registrarEntrada() {
        String placa = campoPlaca.getText().trim().toUpperCase();
        String espacio = campoEspacio.getText().trim().toUpperCase();
        
        if (placa.isEmpty()) {
            mostrarError("Ingrese la placa del vehículo");
            return;
        }
        
        if (espacio.isEmpty()) {
            espacio = buscarEspacioLibre();
            if (espacio == null) {
                mostrarError("No hay espacios disponibles");
                return;
            }
            campoEspacio.setText(espacio);
        }
        
        if (!espacios.containsKey(espacio)) {
            mostrarError("Espacio no válido");
            return;
        }
        
        if (espacios.get(espacio) != null) {
            mostrarError("Espacio ocupado");
            return;
        }
        
        String codigoTicket = generateTicketCode();
        LocalDateTime horaEntrada = LocalDateTime.now();
        double tarifaHora = Double.parseDouble(config.getProperty("tarifa.hora", "2.50"));
        
        VehicleInfo vehiculo = new VehicleInfo(placa, horaEntrada, codigoTicket);
        TicketInfo ticket = new TicketInfo(codigoTicket, placa, espacio, horaEntrada, tarifaHora);
        
        espacios.put(espacio, vehiculo);
        ticketsActivos.put(codigoTicket, ticket);
        
        guardarDatos();
        guardarTickets();
        
        // Agregar al historial
        agregarAlHistorial(String.format("ENTRADA: %s en %s [%s]", placa, espacio, codigoTicket));
        
        // Preparar ticket para impresión
        prepararTicketImpresion(ticket);
        
        // Mostrar diálogo de confirmación con diseño mejorado
        String mensajeConfirmacion = String.format(
            "<html><div style='text-align: center; font-family: Arial; font-size: 14px;'>" +
            "<b>[EXITO] Vehículo registrado correctamente</b><br><br>" +
            "<b>PLACA:</b> %s<br>" +
            "<b>ESPACIO:</b> %s<br>" +
            "<b>CODIGO:</b> %s<br><br>" +
            "<b>¿Desea imprimir el ticket?</b>" +
            "</div></html>",
            placa, espacio, codigoTicket
        );
        
        int respuesta = JOptionPane.showConfirmDialog(
            this,
            mensajeConfirmacion,
            "[TICKET] Entrada Registrada",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (respuesta == JOptionPane.YES_OPTION) {
            imprimirTicket();
        }
        
        limpiarCampos();
        actualizarEstado();
    }
    
    private void registrarSalida() {
        String codigoTicket = campoTicket.getText().trim().toUpperCase();
        String espacio = campoEspacio.getText().trim().toUpperCase();
        
        // Buscar por código de ticket primero
        if (!codigoTicket.isEmpty()) {
            TicketInfo ticket = ticketsActivos.get(codigoTicket);
            if (ticket != null) {
                procesarSalida(ticket);
                return;
            } else {
                mostrarError("Código de ticket no válido o ya procesado");
                return;
            }
        }
        
        // Buscar por espacio
        if (!espacio.isEmpty()) {
            if (!espacios.containsKey(espacio)) {
                mostrarError("Espacio no válido");
                return;
            }
            
            VehicleInfo vehiculo = espacios.get(espacio);
            if (vehiculo == null) {
                mostrarError("Espacio libre");
                return;
            }
            
            TicketInfo ticket = ticketsActivos.get(vehiculo.codigoTicket);
            if (ticket != null) {
                procesarSalida(ticket);
                return;
            }
        }
        
        mostrarError("Ingrese el código del ticket o el espacio del vehículo");
    }
    
    private void procesarSalida(TicketInfo ticket) {
        long minutos = java.time.Duration.between(ticket.horaEntrada, LocalDateTime.now()).toMinutes();
        
        // Cálculo mejorado de tarifas
        double tarifaCalculada = calcularTarifaOptimizada(minutos, ticket.tarifaHora);
        String detallesTiempo = obtenerDetallesTiempo(minutos);
        
        double tarifa = tarifaCalculada;
        
        // Mostrar información de cobro con diseño mejorado
        String mensaje = String.format(
            "<html><div style='font-family: Arial; font-size: 14px;'>" +
            "<b>[COBRO] INFORMACION DE PAGO</b><br>" +
            "================================================<br><br>" +
            "<b>PLACA:</b> %s<br>" +
            "<b>ESPACIO:</b> %s<br>" +
            "<b>CODIGO:</b> %s<br>" +
            "<b>ENTRADA:</b> %s<br>" +
            "<b>SALIDA:</b> %s<br>" +
            "<b>TIEMPO:</b> %s<br>" +
            "<b>TARIFA:</b> $%.2f por hora<br>" +
            "<b>CALCULO:</b> %s<br><br>" +
            "<div style='background-color: #d4edda; padding: 10px; border: 2px solid #28a745;'>" +
            "<b>[TOTAL A COBRAR: $%.2f]</b>" +
            "</div>" +
            "</div></html>",
            ticket.placa,
            ticket.espacio,
            ticket.codigoTicket,
            ticket.horaEntrada.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
            detallesTiempo,
            ticket.tarifaHora,
            obtenerDetalleCalculo(minutos, ticket.tarifaHora),
            tarifa
        );
        
        int respuesta = JOptionPane.showConfirmDialog(
            this,
            mensaje,
            "[PAGO] Confirmar Cobro",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (respuesta == JOptionPane.YES_OPTION) {
            // Liberar espacio y eliminar ticket
            espacios.put(ticket.espacio, null);
            ticketsActivos.remove(ticket.codigoTicket);
            
            guardarDatos();
            guardarTickets();
            
            // Agregar al historial
            agregarAlHistorial(String.format("SALIDA: %s de %s - Cobrado: $%.2f", 
                ticket.placa, ticket.espacio, tarifa));
            
            String mensajeExito = String.format(
                "<html><div style='text-align: center; font-family: Arial; font-size: 14px;'>" +
                "<b>[EXITO] Salida procesada correctamente</b><br><br>" +
                "<b>[TOTAL COBRADO: $%.2f]</b>" +
                "</div></html>",
                tarifa
            );
            mostrarExito(mensajeExito);
            limpiarCampos();
            actualizarEstado();
        }
    }
    
    private void buscarVehiculo() {
        String placa = campoPlaca.getText().trim().toUpperCase();
        if (placa.isEmpty()) {
            mostrarError("Ingrese la placa a buscar");
            return;
        }
        
        String espacio = buscarEspacioPorPlaca(placa);
        if (espacio != null) {
            VehicleInfo vehiculo = espacios.get(espacio);
            campoEspacio.setText(espacio);
            campoTicket.setText(vehiculo.codigoTicket);
            String infoVehiculo = String.format(
                "<html><div style='font-family: Arial; font-size: 14px;'>" +
                "<b>[ENCONTRADO] Vehiculo localizado</b><br><br>" +
                "<b>ESPACIO:</b> %s<br>" +
                "<b>CODIGO TICKET:</b> %s<br>" +
                "<b>HORA ENTRADA:</b> %s" +
                "</div></html>",
                espacio,
                vehiculo.codigoTicket,
                vehiculo.horaEntrada.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
            );
            mostrarInfo(infoVehiculo);
        } else {
            mostrarError("Vehículo no encontrado");
        }
    }
    
    private void generarReporte() {
        StringBuilder reporte = new StringBuilder();
        reporte.append("REPORTE DEL SISTEMA\n");
        reporte.append("==================\n");
        reporte.append("Fecha: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).append("\n\n");
        
        int ocupados = 0;
        double ingresoTotal = 0;
        
        for (Map.Entry<String, VehicleInfo> entry : espacios.entrySet()) {
            if (entry.getValue() != null) {
                ocupados++;
                long minutos = java.time.Duration.between(entry.getValue().horaEntrada, LocalDateTime.now()).toMinutes();
                double tarifaHora = Double.parseDouble(config.getProperty("tarifa.hora", "2.50"));
                ingresoTotal += calcularTarifaOptimizada(minutos, tarifaHora);
            }
        }
        
        reporte.append("Espacios totales: ").append(totalEspacios).append("\n");
        reporte.append("Espacios ocupados: ").append(ocupados).append("\n");
        reporte.append("Espacios libres: ").append(totalEspacios - ocupados).append("\n");
        reporte.append("Ocupación: ").append(String.format("%.1f%%", (ocupados * 100.0 / totalEspacios))).append("\n");
        reporte.append("Ingreso estimado: $").append(String.format("%.2f", ingresoTotal)).append("\n");
        
        JTextArea areaReporte = new JTextArea(reporte.toString());
        areaReporte.setEditable(false);
        JScrollPane scroll = new JScrollPane(areaReporte);
        scroll.setPreferredSize(new Dimension(400, 300));
        JOptionPane.showMessageDialog(this, scroll, "Reporte del Sistema", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private String buscarEspacioLibre() {
        for (Map.Entry<String, VehicleInfo> entry : espacios.entrySet()) {
            if (entry.getValue() == null) {
                return entry.getKey();
            }
        }
        return null;
    }
    
    private String buscarEspacioPorPlaca(String placa) {
        for (Map.Entry<String, VehicleInfo> entry : espacios.entrySet()) {
            if (entry.getValue() != null && entry.getValue().placa.equals(placa)) {
                return entry.getKey();
            }
        }
        return null;
    }
    
    private void actualizarEstado() {
        StringBuilder estado = new StringBuilder();
        int ocupados = 0;
        
        // Encabezado con estilo
        estado.append("\n");
        estado.append("  ╔══════════════════════════════════════════════╗\n");
        estado.append("  ║           ESTADO DEL APARCAMIENTO            ║\n");
        estado.append("  ╚══════════════════════════════════════════════╝\n\n");
        
        // Mostrar espacios en formato de cuadrícula visual
        for (int fila = 0; fila < (totalEspacios + 4) / 5; fila++) {
            StringBuilder lineaEspacios = new StringBuilder("  ");
            StringBuilder lineaEstado = new StringBuilder("  ");
            
            for (int col = 0; col < 5 && (fila * 5 + col + 1) <= totalEspacios; col++) {
                int numEspacio = fila * 5 + col + 1;
                String espacio = "E" + numEspacio;
                VehicleInfo vehiculo = espacios.get(espacio);
                
                // Formato visual mejorado
                lineaEspacios.append(String.format("╔═══════════╗  "));
                
                if (vehiculo == null) {
                    lineaEstado.append(String.format("║ %s [LIBRE] ║  ", String.format("%3s", espacio)));
                } else {
                    lineaEstado.append(String.format("║%s [%s]║  ", 
                        String.format("%3s", espacio), 
                        String.format("%6s", vehiculo.placa.length() > 6 ? 
                            vehiculo.placa.substring(0, 6) : vehiculo.placa)));
                    ocupados++;
                }
            }
            
            estado.append(lineaEspacios).append("\n");
            estado.append(lineaEstado).append("\n");
            
            // Línea inferior de los cuadros
            StringBuilder lineaInferior = new StringBuilder("  ");
            for (int col = 0; col < 5 && (fila * 5 + col + 1) <= totalEspacios; col++) {
                lineaInferior.append("╚═══════════╝  ");
            }
            estado.append(lineaInferior).append("\n\n");
        }
        
        // Leyenda
        estado.append("  ┌─────────────────────────────────────────────┐\n");
        estado.append("  │  [LIBRE] = Disponible  │  [OCUPADO] = En uso │\n");
        estado.append("  └─────────────────────────────────────────────┘\n");
        
        // Detalles de vehículos ocupados
        if (ocupados > 0) {
            estado.append("\n  ╔═══════════════════════════════════════════════╗\n");
            estado.append("  ║              VEHICULOS ACTIVOS                ║\n");
            estado.append("  ╠═══════════════════════════════════════════════╣\n");
            
            for (Map.Entry<String, VehicleInfo> entry : espacios.entrySet()) {
                if (entry.getValue() != null) {
                    VehicleInfo vehiculo = entry.getValue();
                    long minutos = java.time.Duration.between(vehiculo.horaEntrada, LocalDateTime.now()).toMinutes();
                    estado.append(String.format("  ║ %s │ %-8s │ %s │ %3d min ║\n", 
                        entry.getKey(),
                        vehiculo.placa,
                        vehiculo.horaEntrada.format(DateTimeFormatter.ofPattern("HH:mm")),
                        minutos));
                }
            }
            estado.append("  ╚═══════════════════════════════════════════════╝\n");
        }
        
        areaEstado.setText(estado.toString());
        areaEstado.setCaretPosition(0); // Scroll al inicio
        
        // Actualizar métricas visuales
        double porcentajeOcupacion = (ocupados * 100.0 / totalEspacios);
        
        // Actualizar barra de ocupación
        if (barraOcupacion != null) {
            barraOcupacion.setValue((int) porcentajeOcupacion);
            barraOcupacion.setString(String.format("%.1f%%", porcentajeOcupacion));
            
            // Cambiar color según ocupación
            if (porcentajeOcupacion < 50) {
                barraOcupacion.setForeground(new Color(46, 204, 113)); // Verde
            } else if (porcentajeOcupacion < 80) {
                barraOcupacion.setForeground(new Color(243, 156, 18)); // Amarillo
            } else {
                barraOcupacion.setForeground(new Color(231, 76, 60)); // Rojo
            }
        }
        
        // Actualizar etiquetas
        if (lblOcupacion != null) {
            lblOcupacion.setText(String.format("[OCUPACION: %.1f%%]", porcentajeOcupacion));
        }
        
        String statsText = String.format(
            "[LIBRES: %d] | [TICKETS: %d]", 
            totalEspacios - ocupados, ticketsActivos.size());
        
        lblEstadisticas.setText(statsText);
    }
    
    private void exportarDatos() {
        try (PrintWriter writer = new PrintWriter("reporte_" + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")) + ".txt")) {
            writer.println("REPORTE DE APARCAMIENTO");
            writer.println("Fecha: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            for (int j = 0; j < 50; j++) writer.print("=");
            writer.println();
            
            for (Map.Entry<String, VehicleInfo> entry : espacios.entrySet()) {
                if (entry.getValue() != null) {
                    writer.printf("%s: %s (desde %s)\n", entry.getKey(), entry.getValue().placa,
                        entry.getValue().horaEntrada.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                }
            }
            mostrarExito("[EXPORTAR] Datos exportados correctamente");
        } catch (IOException e) {
            mostrarError("[EXPORTAR] Error al exportar: " + e.getMessage());
        }
    }
    
    private void mostrarConfiguracion() {
        JDialog dialog = new JDialog(this, "Configuración Avanzada", true);
        
        JPanel panelConfig = new JPanel(new GridLayout(8, 2, 5, 5));
        panelConfig.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        JTextField txtEspacios = new JTextField(config.getProperty("total.espacios"));
        JTextField txtTarifa = new JTextField(config.getProperty("tarifa.hora"));
        JTextField txtEmpresa = new JTextField(config.getProperty("empresa.nombre"));
        JTextField txtGracia = new JTextField(config.getProperty("minutos.gracia", "15"));
        JTextField txtMinimos = new JTextField(config.getProperty("minutos.minimos", "30"));
        
        JComboBox<String> comboCalculo = new JComboBox<>(new String[]{
            "fraccion", "hora_completa", "minuto_exacto"
        });
        comboCalculo.setSelectedItem(config.getProperty("tipo.calculo", "fraccion"));
        
        panelConfig.add(new JLabel("Total espacios:"));
        panelConfig.add(txtEspacios);
        panelConfig.add(new JLabel("Tarifa por hora ($):"));
        panelConfig.add(txtTarifa);
        panelConfig.add(new JLabel("Nombre empresa:"));
        panelConfig.add(txtEmpresa);
        panelConfig.add(new JLabel("Minutos gracia:"));
        panelConfig.add(txtGracia);
        panelConfig.add(new JLabel("Minutos mínimos:"));
        panelConfig.add(txtMinimos);
        panelConfig.add(new JLabel("Tipo de cálculo:"));
        panelConfig.add(comboCalculo);
        
        JButton btnGuardar = new JButton("Guardar");
        JButton btnCancelar = new JButton("Cancelar");
        
        btnGuardar.addActionListener(e -> {
            try {
                int espacios = Integer.parseInt(txtEspacios.getText());
                double tarifa = Double.parseDouble(txtTarifa.getText());
                if (espacios < 1 || espacios > 100) {
                    mostrarError("El número de espacios debe estar entre 1 y 100");
                    return;
                }
                if (tarifa < 0) {
                    mostrarError("La tarifa debe ser un valor positivo");
                    return;
                }
                config.setProperty("total.espacios", txtEspacios.getText());
                config.setProperty("tarifa.hora", txtTarifa.getText());
                config.setProperty("empresa.nombre", txtEmpresa.getText());
                config.setProperty("minutos.gracia", txtGracia.getText());
                config.setProperty("minutos.minimos", txtMinimos.getText());
                config.setProperty("tipo.calculo", (String) comboCalculo.getSelectedItem());
                try (OutputStream output = new FileOutputStream(CONFIG_FILE)) {
                    config.store(output, null);
                    mostrarExito("[CONFIG] Configuracion guardada. Reinicie la aplicacion.");
                    dialog.dispose();
                } catch (IOException ex) {
                    mostrarError("[CONFIG] Error guardando configuracion");
                }
            } catch (NumberFormatException ex) {
                mostrarError("[CONFIG] Ingrese valores numericos validos");
            }
        });
        
        btnCancelar.addActionListener(e -> dialog.dispose());
        
        panelConfig.add(btnGuardar);
        panelConfig.add(btnCancelar);
        
        dialog.add(panelConfig);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
    
    private void cargarDatos() {
        try (BufferedReader reader = new BufferedReader(new FileReader(DATA_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    VehicleInfo vehiculo = VehicleInfo.fromString(parts[1]);
                    if (vehiculo != null) {
                        espacios.put(parts[0], vehiculo);
                    }
                }
            }
        } catch (IOException e) {
            // Archivo no existe, usar datos por defecto
        }
    }
    
    private void guardarDatos() {
        try (PrintWriter writer = new PrintWriter(DATA_FILE)) {
            for (Map.Entry<String, VehicleInfo> entry : espacios.entrySet()) {
                if (entry.getValue() != null) {
                    writer.println(entry.getKey() + ":" + entry.getValue().toString());
                }
            }
        } catch (IOException e) {
            System.err.println("Error guardando datos: " + e.getMessage());
        }
    }
    
    private void configurarCierreSeguro() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int respuesta = JOptionPane.showConfirmDialog(
                    SistemaAparcamientoPro.this,
                    "¿Desea cerrar el sistema?",
                    "Confirmar salida",
                    JOptionPane.YES_NO_OPTION
                );
                if (respuesta == JOptionPane.YES_OPTION) {
                    if (timerActualizacion != null) {
                        timerActualizacion.stop();
                    }
                    guardarDatos();
                    guardarTickets();
                    System.exit(0);
                }
            }
        });
    }
    
    private static String generateTicketCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        
        // Formato: TK-YYYYMMDD-NNNN
        code.append("TK-");
        code.append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        code.append("-");
        code.append(String.format("%04d", random.nextInt(10000)));
        
        return code.toString();
    }
    
    private void limpiarCampos() {
        campoPlaca.setText("");
        campoEspacio.setText("");
        campoTicket.setText("");
    }
    
    private void mostrarError(String mensaje) {
        mostrarMensajePersonalizado(mensaje, "[ERROR] Sistema", JOptionPane.ERROR_MESSAGE, DANGER_COLOR);
    }
    
    private void mostrarExito(String mensaje) {
        mostrarMensajePersonalizado(mensaje, "[EXITO] Operacion", JOptionPane.INFORMATION_MESSAGE, SUCCESS_COLOR);
    }
    
    private void mostrarInfo(String mensaje) {
        mostrarMensajePersonalizado(mensaje, "[INFO] Informacion", JOptionPane.INFORMATION_MESSAGE, SECONDARY_COLOR);
    }
    
    private void mostrarMensajePersonalizado(String mensaje, String titulo, int tipo, Color color) {
        // Crear panel personalizado para el mensaje
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(WHITE_COLOR);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        JLabel lblMensaje = new JLabel("<html><div style='text-align: center; font-family: Arial; font-size: 14px;'>" + 
                                      mensaje.replace("\n", "<br>") + "</div></html>");
        lblMensaje.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(lblMensaje, BorderLayout.CENTER);
        
        JOptionPane.showMessageDialog(this, panel, titulo, tipo);
    }
    
    // Métodos para el sistema de tickets
    private void leerTicket() {
        String codigoTicket = campoTicket.getText().trim().toUpperCase();
        
        if (codigoTicket.isEmpty()) {
            mostrarError("Ingrese el código del ticket");
            return;
        }
        
        TicketInfo ticket = ticketsActivos.get(codigoTicket);
        if (ticket == null) {
            mostrarError("Ticket no encontrado o ya procesado");
            return;
        }
        
        long minutos = java.time.Duration.between(ticket.horaEntrada, LocalDateTime.now()).toMinutes();
        double tarifa = calcularTarifaOptimizada(minutos, ticket.tarifaHora);
        String detallesTiempo = obtenerDetallesTiempo(minutos);
        
        campoPlaca.setText(ticket.placa);
        campoEspacio.setText(ticket.espacio);
        
        String info = String.format(
            "<html><div style='font-family: Arial; font-size: 14px;'>" +
            "<b>[TICKET] INFORMACION COMPLETA</b><br>" +
            "==========================================<br><br>" +
            "<b>CODIGO:</b> %s<br>" +
            "<b>PLACA:</b> %s<br>" +
            "<b>ESPACIO:</b> %s<br>" +
            "<b>ENTRADA:</b> %s<br>" +
            "<b>TIEMPO:</b> %s<br>" +
            "<b>CALCULO:</b> %s<br><br>" +
            "<div style='background-color: #fff3cd; padding: 8px; border: 2px solid #ffc107;'>" +
            "<b>[TARIFA ACTUAL: $%.2f]</b>" +
            "</div>" +
            "</div></html>",
            ticket.codigoTicket,
            ticket.placa,
            ticket.espacio,
            ticket.horaEntrada.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
            detallesTiempo,
            obtenerDetalleCalculo(minutos, ticket.tarifaHora),
            tarifa
        );
        
        mostrarInfo(info);
    }
    
    private void reimprimirTicket() {
        String codigoTicket = campoTicket.getText().trim().toUpperCase();
        
        if (codigoTicket.isEmpty()) {
            mostrarError("Ingrese el código del ticket a reimprimir");
            return;
        }
        
        TicketInfo ticket = ticketsActivos.get(codigoTicket);
        if (ticket == null) {
            mostrarError("Ticket no encontrado");
            return;
        }
        
        prepararTicketImpresion(ticket);
        imprimirTicket();
    }
    
    private void prepararTicketImpresion(TicketInfo ticket) {
        StringBuilder ticketTexto = new StringBuilder();
        ticketTexto.append("========================================\n");
        ticketTexto.append("       ").append(config.getProperty("empresa.nombre", "PARKING SYSTEM")).append("\n");
        ticketTexto.append("========================================\n");
        ticketTexto.append("Dirección: ").append(config.getProperty("empresa.direccion", "")).append("\n");
        ticketTexto.append("Teléfono: ").append(config.getProperty("empresa.telefono", "")).append("\n");
        ticketTexto.append("----------------------------------------\n");
        ticketTexto.append("TICKET DE ENTRADA\n");
        ticketTexto.append("----------------------------------------\n");
        ticketTexto.append("Código: ").append(ticket.codigoTicket).append("\n");
        ticketTexto.append("Placa: ").append(ticket.placa).append("\n");
        ticketTexto.append("Espacio: ").append(ticket.espacio).append("\n");
        ticketTexto.append("Fecha: ").append(ticket.horaEntrada.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))).append("\n");
        ticketTexto.append("Hora: ").append(ticket.horaEntrada.format(DateTimeFormatter.ofPattern("HH:mm:ss"))).append("\n");
        ticketTexto.append("Tarifa: $").append(String.format("%.2f", ticket.tarifaHora)).append(" por hora\n");
        ticketTexto.append("----------------------------------------\n");
        ticketTexto.append("CONSERVE ESTE TICKET\n");
        ticketTexto.append("Necesario para la salida\n");
        ticketTexto.append("----------------------------------------\n");
        ticketTexto.append("Código QR: [").append(ticket.codigoTicket).append("]\n");
        ticketTexto.append("========================================\n");
        
        ticketParaImprimir = ticketTexto.toString();
    }
    
    private void imprimirTicket() {
        try {
            PrinterJob job = PrinterJob.getPrinterJob();
            job.setPrintable(this);
            
            if (job.printDialog()) {
                job.print();
                mostrarExito("[IMPRESION] Ticket enviado a impresora correctamente");
            }
        } catch (PrinterException e) {
            mostrarError("[IMPRESION] Error al imprimir: " + e.getMessage());
        }
    }
    
    @Override
    public int print(Graphics g, PageFormat pf, int page) throws PrinterException {
        if (page > 0) return NO_SUCH_PAGE;
        
        Graphics2D g2d = (Graphics2D) g;
        g2d.translate(pf.getImageableX(), pf.getImageableY());
        
        Font font = new Font("Monospaced", Font.PLAIN, 10);
        g2d.setFont(font);
        
        String[] lines = ticketParaImprimir.split("\n");
        int y = 20;
        
        for (String line : lines) {
            g2d.drawString(line, 10, y);
            y += 12;
        }
        
        return PAGE_EXISTS;
    }
    
    private void cargarTickets() {
        try (BufferedReader reader = new BufferedReader(new FileReader(TICKETS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                TicketInfo ticket = TicketInfo.fromString(line);
                if (ticket != null) {
                    ticketsActivos.put(ticket.codigoTicket, ticket);
                }
            }
        } catch (IOException e) {
            // Archivo no existe, usar datos por defecto
        }
    }
    
    private void guardarTickets() {
        try (PrintWriter writer = new PrintWriter(TICKETS_FILE)) {
            for (TicketInfo ticket : ticketsActivos.values()) {
                writer.println(ticket.toString());
            }
        } catch (IOException e) {
            System.err.println("Error guardando tickets: " + e.getMessage());
        }
    }
    
    public static void main(String[] args) {
        // Configurar propiedades del sistema para mejor renderizado
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        
        SwingUtilities.invokeLater(() -> {
            // Personalizar algunos colores globales
            UIManager.put("OptionPane.background", Color.WHITE);
            UIManager.put("Panel.background", Color.WHITE);
            
            new SistemaAparcamientoPro().setVisible(true);
        });
    }
}