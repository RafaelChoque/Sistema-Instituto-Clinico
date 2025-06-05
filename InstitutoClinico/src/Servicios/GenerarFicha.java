/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package Servicios;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import javax.swing.*;
import java.awt.Desktop;
import java.io.*;
import ConexionLogin.Conexion;
import ConexionLogin.Login;
import ConexionLogin.Session;
import com.formdev.flatlaf.FlatLightLaf;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.sql.Connection;
import javax.swing.JOptionPane;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.*;
import java.util.Date;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JSpinner;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SpinnerDateModel;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableRowSorter;
import javax.swing.table.TableModel;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.text.Normalizer;


/**
 *
 * @author Erlan
 */
public class GenerarFicha extends javax.swing.JFrame {

    /**
     * Creates new form GenerarFicha
     *
     * @param idusuario
     */
    private int idusuario;

    private DefaultListModel<String> modeloLista = new DefaultListModel<>();

    public GenerarFicha(int idusuario) {
        this.idusuario = idusuario;
        initComponents();

        if (BuscarItemsNombre.getText().isEmpty()) {
            lblBuscarItems.setVisible(true);
        } else {
            lblBuscarItems.setVisible(false);
        }
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        cargarServicios();

        ListaItems.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        cargarTablaAfiches();
        ID.setVisible(false);
        ID.setEnabled(false);
        ID.setFocusable(false);
        ID.setRequestFocusEnabled(false);
        aplicarColorFilasAlternadas(TablaAfiches);

    }
    private TableRowSorter<DefaultTableModel> sorter;

    private GenerarFicha() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    private void aplicarColorFilasAlternadas(JTable tabla) {
        TableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {

                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (isSelected) {
                    setBackground(table.getSelectionBackground());
                    setForeground(table.getSelectionForeground());
                } else {
                    if (row % 2 == 0) {
                        setBackground(Color.WHITE);
                        setForeground(Color.BLACK);
                    } else {
                        setBackground(new Color(240, 240, 240));
                        setForeground(Color.BLACK);
                    }
                }

                return this;
            }
        };

        for (int i = 0; i < tabla.getColumnCount(); i++) {
            tabla.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }
    }
        private DefaultListModel<String> modeloOriginal;

    public class TipoPrecioItem {

        private String valor;
        private String descripcion;

        public TipoPrecioItem(String valor, String descripcion) {
            this.valor = valor;
            this.descripcion = descripcion;
        }

        public String getValor() {
            return valor;
        }

        @Override
        public String toString() {
            return descripcion;
        }
    }


    public void generarFichaFormatoRecibo(
            String nombrePaciente, String apellidoPaciente, String nombreDoctor,
            Date fecha, Date hora, String formaPago, String medioPago,
            double total, ListModel<Servicio> servicios,
            int numeroFicha, int anioFicha, Date fechaGuardado, String nombreUsuario,
            int id_afiche, String notas) {

        try {
            String[] partesNombreDoctor = nombreDoctor.trim().split("\\s+");
            String apellidoDoc = partesNombreDoctor.length > 1 ? partesNombreDoctor[partesNombreDoctor.length - 1] : "";
            String nombreDoc = partesNombreDoctor.length > 1 ? partesNombreDoctor[0] : partesNombreDoctor[0];

            apellidoDoc = apellidoDoc.replaceAll("\\s+", "");
            nombreDoc = nombreDoc.replaceAll("\\s+", "");

            SimpleDateFormat sdfNombreArchivo = new SimpleDateFormat("yyyyMMdd");
            String fechaFormato = sdfNombreArchivo.format(fechaGuardado);

            String nombreArchivo = String.format("ficha_%s%s_%d_%s.pdf", apellidoDoc, nombreDoc, id_afiche, fechaFormato);
            File pdfFile = new File(nombreArchivo);

            int alturaMinima = 260;
            int altura = alturaMinima + servicios.getSize() * 8;

            if (notas != null && !notas.trim().isEmpty()) {
                int lineasNotas = notas.split("\n").length;
                altura += (lineasNotas * 20); 
            }
            Rectangle pageSize = new Rectangle(226, altura);
            Document document = new Document(pageSize, 10f, 10f, 5f, 5f);
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(pdfFile));
            document.open();

            generarPaginaRecibo(document, nombrePaciente, apellidoPaciente, nombreDoctor, fecha, hora,
                    formaPago, medioPago, total, servicios, numeroFicha, anioFicha, fechaGuardado, nombreUsuario, id_afiche, notas);

            document.newPage();

            generarPaginaRecibo(document, nombrePaciente, apellidoPaciente, nombreDoctor, fecha, hora,
                    formaPago, medioPago, total, servicios, numeroFicha, anioFicha, fechaGuardado, nombreUsuario, id_afiche, notas);

            document.close();
            writer.flush();
            writer.close();

            if (Desktop.isDesktopSupported()) {
                try {
                    Desktop.getDesktop().open(pdfFile);
                } catch (Exception ex) {
                }
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error al generar PDF: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private void generarPaginaRecibo(Document document,
            String nombrePaciente, String apellidoPaciente, String nombreDoctor,
            Date fecha, Date hora, String formaPago, String medioPago,
            double total, ListModel<Servicio> servicios,
            int numeroFicha, int anioFicha, Date fechaGuardado, String nombreUsuario,
            int idAfiche, String notas) throws Exception {

        Font fontNormal = new Font(Font.FontFamily.HELVETICA, 8, Font.NORMAL);
        Font fontBold = new Font(Font.FontFamily.HELVETICA, 8, Font.BOLD);
        Font fontTitulo = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD);
        Font fontLinea = new Font(Font.FontFamily.COURIER, 5, Font.NORMAL);
        Font fontIdFicha = new Font(Font.FontFamily.HELVETICA, 6, Font.ITALIC);

        InputStream is = getClass().getClassLoader().getResourceAsStream("Imagenes/LogoSantaFe.jpg");
        if (is == null) {
            throw new IOException("No se encontró la imagen LogoSantaFe.jpg en el classpath.");
        }

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[1024];
        while ((nRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        buffer.flush();

        Image logo = Image.getInstance(buffer.toByteArray());

        logo.scaleToFit(90, 90);
        PdfPTable tablaCabeceraLogoID = new PdfPTable(2);
        tablaCabeceraLogoID.setWidthPercentage(100);
        tablaCabeceraLogoID.setWidths(new float[]{1f, 1f});
        tablaCabeceraLogoID.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        PdfPCell cellLogo = new PdfPCell(logo);
        cellLogo.setBorder(Rectangle.NO_BORDER);
        cellLogo.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellLogo.setVerticalAlignment(Element.ALIGN_TOP);
        tablaCabeceraLogoID.addCell(cellLogo);

        PdfPCell cellIdFicha = new PdfPCell(new Phrase("" + idAfiche, fontIdFicha));
        cellIdFicha.setBorder(Rectangle.NO_BORDER);
        cellIdFicha.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cellIdFicha.setVerticalAlignment(Element.ALIGN_TOP);
        tablaCabeceraLogoID.addCell(cellIdFicha);

        document.add(tablaCabeceraLogoID);

        PdfPTable tablaCabecera = new PdfPTable(1);
        tablaCabecera.setWidthPercentage(100);
        tablaCabecera.setSpacingBefore(2f);
        tablaCabecera.setSpacingAfter(2f);
        tablaCabecera.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        PdfPCell cellCentro = new PdfPCell(new Phrase(new Chunk("Centro Médico Santa Fe", fontTitulo)));
        cellCentro.setBorder(Rectangle.NO_BORDER);
        cellCentro.setPaddingTop(2f);
        cellCentro.setPaddingBottom(2f);
        cellCentro.setHorizontalAlignment(Element.ALIGN_CENTER);
        tablaCabecera.addCell(cellCentro);

        PdfPCell cellDireccion = new PdfPCell(new Phrase(new Chunk("Zona Villa Bolívar Forno\nAv. Tiahuanacu N°17a\nLa Paz - Bolivia", fontNormal)));
        cellDireccion.setBorder(Rectangle.NO_BORDER);
        cellDireccion.setPaddingTop(2f);
        cellDireccion.setPaddingBottom(2f);
        cellDireccion.setHorizontalAlignment(Element.ALIGN_CENTER);
        tablaCabecera.addCell(cellDireccion);

        PdfPCell cellSeparador1 = new PdfPCell(new Phrase(new Chunk("--------------------------", fontLinea)));
        cellSeparador1.setBorder(Rectangle.NO_BORDER);
        cellSeparador1.setPaddingTop(0f);
        cellSeparador1.setPaddingBottom(0f);
        cellSeparador1.setHorizontalAlignment(Element.ALIGN_CENTER);
        tablaCabecera.addCell(cellSeparador1);

        PdfPCell cellRecibo = new PdfPCell(new Phrase(new Chunk(String.format("Recibo Nº %d/%02d", numeroFicha, anioFicha), fontNormal)));
        cellRecibo.setBorder(Rectangle.NO_BORDER);
        cellRecibo.setPaddingTop(0f);
        cellRecibo.setPaddingBottom(0f);
        cellRecibo.setHorizontalAlignment(Element.ALIGN_CENTER);
        tablaCabecera.addCell(cellRecibo);

        PdfPCell cellSeparador2 = new PdfPCell(new Phrase(new Chunk("--------------------------", fontLinea)));
        cellSeparador2.setBorder(Rectangle.NO_BORDER);
        cellSeparador2.setPaddingTop(0f);
        cellSeparador2.setPaddingBottom(0f);
        cellSeparador2.setHorizontalAlignment(Element.ALIGN_CENTER);
        tablaCabecera.addCell(cellSeparador2);

        document.add(tablaCabecera);

        SimpleDateFormat sdfFecha = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat sdfHora = new SimpleDateFormat("HH:mm");
        String fechaStr = sdfFecha.format(fecha);
        String horaStr = sdfHora.format(hora);

        PdfPTable tablaDatos = new PdfPTable(1);
        tablaDatos.setWidthPercentage(100);
        tablaDatos.setSpacingBefore(0f);
        tablaDatos.setSpacingAfter(0f);
        tablaDatos.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        PdfPCell cellPaciente = new PdfPCell(new Phrase(new Chunk("Paciente: " + nombrePaciente + " " + apellidoPaciente, fontNormal)));
        cellPaciente.setBorder(Rectangle.NO_BORDER);
        cellPaciente.setPaddingTop(2f);
        cellPaciente.setPaddingBottom(2f);
        tablaDatos.addCell(cellPaciente);

        PdfPCell cellDoctor = new PdfPCell(new Phrase(new Chunk("Médico Responsable: " + nombreDoctor, fontNormal)));
        cellDoctor.setBorder(Rectangle.NO_BORDER);
        cellDoctor.setPaddingTop(2f);
        cellDoctor.setPaddingBottom(2f);
        tablaDatos.addCell(cellDoctor);

        PdfPCell cellFechaHora = new PdfPCell(new Phrase(new Chunk("Fecha atención: " + fechaStr + "    Hora: " + horaStr, fontNormal)));
        cellFechaHora.setBorder(Rectangle.NO_BORDER);
        cellFechaHora.setPaddingTop(2f);
        cellFechaHora.setPaddingBottom(2f);
        tablaDatos.addCell(cellFechaHora);

        PdfPCell cellSeparadorDatos = new PdfPCell(new Phrase(new Chunk("-------------------------------------------------------------------", fontLinea)));
        cellSeparadorDatos.setBorder(Rectangle.NO_BORDER);
        cellSeparadorDatos.setPaddingTop(0f);
        cellSeparadorDatos.setPaddingBottom(0f);
        tablaDatos.addCell(cellSeparadorDatos);

        document.add(tablaDatos);

        PdfPTable tablaServicios = new PdfPTable(2);
        tablaServicios.setWidthPercentage(100);
        tablaServicios.setSpacingBefore(0f);
        tablaServicios.setSpacingAfter(0f);
        tablaServicios.setWidths(new float[]{2f, 1f});
        tablaServicios.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        PdfPCell cabeceraDetalle = new PdfPCell(new Phrase(new Chunk("Detalle", fontBold)));
        cabeceraDetalle.setBorder(Rectangle.NO_BORDER);
        cabeceraDetalle.setPaddingTop(0f);
        cabeceraDetalle.setPaddingBottom(0f);
        cabeceraDetalle.setHorizontalAlignment(Element.ALIGN_LEFT);
        tablaServicios.addCell(cabeceraDetalle);

        PdfPCell cabeceraPrecio = new PdfPCell(new Phrase(new Chunk("Precio (Bs)", fontBold)));
        cabeceraPrecio.setBorder(Rectangle.NO_BORDER);
        cabeceraPrecio.setPaddingTop(0f);
        cabeceraPrecio.setPaddingBottom(0f);
        cabeceraPrecio.setHorizontalAlignment(Element.ALIGN_RIGHT);
        tablaServicios.addCell(cabeceraPrecio);

        PdfPCell lineaSeparadora = new PdfPCell(new Phrase(new Chunk("-------------------------------------------------------------------", fontLinea)));
        lineaSeparadora.setColspan(2);
        lineaSeparadora.setBorder(Rectangle.NO_BORDER);
        lineaSeparadora.setPaddingTop(0f);
        lineaSeparadora.setPaddingBottom(0f);
        tablaServicios.addCell(lineaSeparadora);

        for (int i = 0; i < servicios.getSize(); i++) {
            Servicio servicio = servicios.getElementAt(i);

            PdfPCell celdaNombre = new PdfPCell(new Phrase(new Chunk(servicio.getNombre(), fontNormal)));
            celdaNombre.setBorder(Rectangle.NO_BORDER);
            celdaNombre.setHorizontalAlignment(Element.ALIGN_LEFT);
            celdaNombre.setPaddingTop(0f);
            celdaNombre.setPaddingBottom(0f);
            celdaNombre.setMinimumHeight(0f);

            PdfPCell celdaPrecio = new PdfPCell(new Phrase(new Chunk(String.format("%.2f", servicio.getPrecio()), fontNormal)));
            celdaPrecio.setBorder(Rectangle.NO_BORDER);
            celdaPrecio.setHorizontalAlignment(Element.ALIGN_RIGHT);
            celdaPrecio.setPaddingTop(0f);
            celdaPrecio.setPaddingBottom(0f);
            celdaPrecio.setMinimumHeight(0f);

            tablaServicios.addCell(celdaNombre);
            tablaServicios.addCell(celdaPrecio);
        }

        document.add(tablaServicios);

        PdfPTable tablaSeparador1 = new PdfPTable(1);
        tablaSeparador1.setWidthPercentage(100);
        tablaSeparador1.setSpacingBefore(0f);
        tablaSeparador1.setSpacingAfter(0f);
        tablaSeparador1.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        PdfPCell separador1 = new PdfPCell(new Phrase(new Chunk("-------------------------------------------------------------------", fontLinea)));
        separador1.setBorder(Rectangle.NO_BORDER);
        separador1.setPaddingTop(0f);
        separador1.setPaddingBottom(0f);
        separador1.setHorizontalAlignment(Element.ALIGN_CENTER);

        tablaSeparador1.addCell(separador1);

        document.add(tablaSeparador1);

        PdfPTable tablaTotal = new PdfPTable(2);
        tablaTotal.setWidthPercentage(100);
        tablaTotal.setWidths(new float[]{2f, 1f});
        tablaTotal.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        PdfPCell celdaTextoTotal = new PdfPCell(new Phrase(new Chunk("Total:", fontBold)));
        celdaTextoTotal.setBorder(Rectangle.NO_BORDER);
        celdaTextoTotal.setHorizontalAlignment(Element.ALIGN_LEFT);
        celdaTextoTotal.setPaddingTop(0f);
        celdaTextoTotal.setPaddingBottom(0f);
        celdaTextoTotal.setMinimumHeight(0f);

        PdfPCell celdaMontoTotal = new PdfPCell(new Phrase(new Chunk("Bs " + String.format("%.2f", total), fontBold)));
        celdaMontoTotal.setBorder(Rectangle.NO_BORDER);
        celdaMontoTotal.setHorizontalAlignment(Element.ALIGN_RIGHT);
        celdaMontoTotal.setPaddingTop(0f);
        celdaMontoTotal.setPaddingBottom(0f);
        celdaMontoTotal.setMinimumHeight(0f);

        tablaTotal.addCell(celdaTextoTotal);
        tablaTotal.addCell(celdaMontoTotal);
        document.add(tablaTotal);

        PdfPTable tablaSeparador2 = new PdfPTable(1);
        tablaSeparador2.setWidthPercentage(100);
        tablaSeparador2.setSpacingBefore(0f);
        tablaSeparador2.setSpacingAfter(0f);
        tablaSeparador2.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        PdfPCell separador2 = new PdfPCell(new Phrase(new Chunk("-------------------------------------------------------------------", fontLinea)));
        separador2.setBorder(Rectangle.NO_BORDER);
        separador2.setPaddingTop(0f);
        separador2.setPaddingBottom(0f);
        tablaSeparador2.addCell(separador2);
        document.add(tablaSeparador2);

        PdfPTable tablaInfo = new PdfPTable(1);
        tablaInfo.setWidthPercentage(100);
        tablaInfo.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        
                if (notas != null && !notas.trim().isEmpty()) {
            PdfPTable tablaNotas = new PdfPTable(1);
            tablaNotas.setWidthPercentage(100);
            tablaNotas.setSpacingBefore(2f);
            tablaNotas.setSpacingAfter(2f);
            tablaNotas.getDefaultCell().setBorder(Rectangle.NO_BORDER);

            PdfPCell tituloNotas = new PdfPCell(new Phrase("Nota:", fontBold));
            tituloNotas.setBorder(Rectangle.NO_BORDER);
            tituloNotas.setHorizontalAlignment(Element.ALIGN_LEFT);
            tituloNotas.setPaddingBottom(1f);
            tablaNotas.addCell(tituloNotas);

            PdfPCell contenidoNotas = new PdfPCell(new Phrase(notas, fontIdFicha));
            contenidoNotas.setBorder(Rectangle.NO_BORDER);
            contenidoNotas.setHorizontalAlignment(Element.ALIGN_LEFT);
            contenidoNotas.setPaddingBottom(2f);
            tablaNotas.addCell(contenidoNotas);

            document.add(tablaNotas);
        }

        tablaInfo.addCell(new Phrase("Forma de pago: " + formaPago, fontNormal));
        tablaInfo.addCell(new Phrase("Medio de pago: " + medioPago, fontNormal));
        tablaInfo.addCell(new Phrase("Usuario responsable: " + nombreUsuario, fontNormal));

        PdfPCell espacio = new PdfPCell(new Phrase(" "));
        espacio.setBorder(Rectangle.NO_BORDER);
        espacio.setFixedHeight(10f);
        tablaInfo.addCell(espacio);

        PdfPCell cellGracias = new PdfPCell(new Phrase("¡Gracias por su confianza!", fontNormal));
        cellGracias.setBorder(Rectangle.NO_BORDER);
        cellGracias.setHorizontalAlignment(Element.ALIGN_CENTER);
        tablaInfo.addCell(cellGracias);
        document.add(tablaInfo);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        Superior = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        btnCerrarSesion = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        ListaPersonal = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        TablaAfiches = new javax.swing.JTable();
        jPanel4 = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        Nombre = new javax.swing.JTextField();
        Apellido = new javax.swing.JTextField();
        ID = new javax.swing.JTextField();
        AgregarTecnico = new javax.swing.JLabel();
        SeleccionarDoc = new javax.swing.JButton();
        NombreDoctor = new javax.swing.JTextField();
        FechaAtencion = new com.toedter.calendar.JDateChooser();
        Hora = new javax.swing.JSpinner();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        AgregarTecnico1 = new javax.swing.JLabel();
        lblBuscarItems = new javax.swing.JLabel();
        BuscarItemsNombre = new javax.swing.JTextField();
        jScrollPane2 = new javax.swing.JScrollPane();
        ListaItems = new javax.swing.JList<>();
        jLabel11 = new javax.swing.JLabel();
        jPanel7 = new javax.swing.JPanel();
        AgregarTecnico3 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        ComboTipoPrecio = new javax.swing.JComboBox<>();
        btnVistaPrevia = new javax.swing.JButton();
        jScrollPane4 = new javax.swing.JScrollPane();
        ListaItemsSeleccionados = new javax.swing.JList<>();
        TotalSumaServicios = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        formadepago = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        TipoPago = new javax.swing.JComboBox<>();
        FormaPago = new javax.swing.JComboBox<>();
        guardar = new javax.swing.JButton();
        limpiar = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        AgregarTecnico4 = new javax.swing.JLabel();
        AgregarTecnico2 = new javax.swing.JLabel();
        jPanel8 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        NotasTextArea = new javax.swing.JTextArea();
        FondoGris = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setUndecorated(true);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        Superior.setBackground(new java.awt.Color(80, 35, 100));
        Superior.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Imagenes/cerrarsesion.png"))); // NOI18N
        Superior.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(1890, 0, 20, 60));

        btnCerrarSesion.setBackground(new java.awt.Color(33, 14, 68));
        btnCerrarSesion.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnCerrarSesion.setForeground(new java.awt.Color(255, 255, 255));
        btnCerrarSesion.setText("Cerrar Sesión");
        btnCerrarSesion.setBorder(null);
        btnCerrarSesion.setHorizontalAlignment(SwingConstants.LEFT);
        btnCerrarSesion.setBorder(BorderFactory.createEmptyBorder(0, 35, 0, 0));
        btnCerrarSesion.setIconTextGap(10);
        btnCerrarSesion.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnCerrarSesionMouseExited(evt);
            }
        });
        btnCerrarSesion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCerrarSesionActionPerformed(evt);
            }
        });
        Superior.add(btnCerrarSesion, new org.netbeans.lib.awtextra.AbsoluteConstraints(1740, 0, 180, 60));

        getContentPane().add(Superior, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1920, 60));

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        ListaPersonal.setFont(new java.awt.Font("Candara", 1, 24)); // NOI18N
        ListaPersonal.setText("Lista de Fichas ");
        jPanel2.add(ListaPersonal, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 20, 160, -1));

        TablaAfiches.setAutoCreateRowSorter(true);
        TablaAfiches.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "Paciente", "Fecha de Atención", "Hora de Atención", "Forma de Pago", "Medio de Pago", "Total de Precio"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        TablaAfiches.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                TablaAfichesMouseClicked(evt);
            }
        });
        TablaAfiches.getTableHeader().setReorderingAllowed(false);
        TablaAfiches.getTableHeader().setResizingAllowed(false);
        TablaAfiches.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        TablaAfiches.setFocusable(false);
        jScrollPane1.setViewportView(TablaAfiches);
        if (TablaAfiches.getColumnModel().getColumnCount() > 0) {
            TablaAfiches.getColumnModel().getColumn(0).setResizable(false);
        }

        jPanel2.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 100, 1190, 860));

        jPanel4.setBackground(new java.awt.Color(255, 255, 255));
        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
        jPanel4.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel9.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Imagenes/Buscar.png"))); // NOI18N
        jPanel4.add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(15, 15, -1, -1));

        jTextField1.setBackground(new java.awt.Color(233, 236, 239));
        jTextField1.setText("Buscar Fichas");
        jTextField1.setToolTipText("");
        jTextField1.setBorder(null);
        jTextField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField1ActionPerformed(evt);
            }
        });
        jPanel4.add(jTextField1, new org.netbeans.lib.awtextra.AbsoluteConstraints(45, 15, 130, 20));
        String placeholder = "Buscar Fichas";
        jTextField1.setText(placeholder);
        jTextField1.setForeground(Color.GRAY);

        jTextField1.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (jTextField1.getText().equals(placeholder)) {
                    jTextField1.setText("");
                    jTextField1.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (jTextField1.getText().trim().isEmpty()) {
                    jTextField1.setText(placeholder);
                    jTextField1.setForeground(Color.GRAY);
                }
            }
        });

        jTextField1.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filterTable();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filterTable();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filterTable();
            }

            private void filterTable() {
                String query = normalize(jTextField1.getText().trim());

                if (query.equals(normalize(placeholder)) || query.isEmpty()) {
                    sorter.setRowFilter(null);
                } else {
                    sorter.setRowFilter(new RowFilter<TableModel, Integer>() {
                        @Override
                        public boolean include(Entry<? extends TableModel, ? extends Integer> entry) {
                            String texto = normalize(entry.getStringValue(1).trim());
                            return texto.contains(query);
                        }
                    });
                }
            }

            private String normalize(String text) {
                if (text == null) return "";
                String normalized = Normalizer.normalize(text, Normalizer.Form.NFD);
                normalized = normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
                return normalized.toLowerCase();
            }
        });

        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Imagenes/Fondo_1.png"))); // NOI18N
        jPanel4.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(5, 5, 190, 40));

        jPanel2.add(jPanel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 50, 1190, 910));

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(194, 194, 194)));
        jPanel1.setToolTipText("");
        jPanel1.setOpaque(false);
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel4.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel4.setText("Nombre:");
        jPanel1.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 40, -1, 20));

        jLabel5.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel5.setText("Apellidos:");
        jPanel1.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 70, -1, 20));

        Nombre.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                NombreActionPerformed(evt);
            }
        });
        jPanel1.add(Nombre, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 40, 470, -1));

        Apellido.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ApellidoActionPerformed(evt);
            }
        });
        jPanel1.add(Apellido, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 70, 470, -1));

        ID.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                IDActionPerformed(evt);
            }
        });
        jPanel1.add(ID, new org.netbeans.lib.awtextra.AbsoluteConstraints(630, 10, 10, -1));

        AgregarTecnico.setFont(new java.awt.Font("Candara", 1, 24)); // NOI18N
        AgregarTecnico.setText("Datos del Paciente");
        jPanel1.add(AgregarTecnico, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 10, 210, -1));

        SeleccionarDoc.setBackground(new java.awt.Color(80, 35, 100));
        SeleccionarDoc.setForeground(new java.awt.Color(255, 255, 255));
        SeleccionarDoc.setText("Seleccionar Doctor");
        SeleccionarDoc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SeleccionarDocActionPerformed(evt);
            }
        });
        jPanel1.add(SeleccionarDoc, new org.netbeans.lib.awtextra.AbsoluteConstraints(480, 140, 140, -1));

        NombreDoctor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                NombreDoctorActionPerformed(evt);
            }
        });
        jPanel1.add(NombreDoctor, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 140, 310, -1));
        jPanel1.add(FechaAtencion, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 170, 310, -1));

        Hora.setModel(new SpinnerDateModel());
        JSpinner.DateEditor horaficha = new JSpinner.DateEditor(Hora, "HH:mm");
        Hora.setEditor(horaficha);
        jPanel1.add(Hora, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 200, 70, -1));

        jLabel17.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel17.setText("Hora de Atencion:");
        jPanel1.add(jLabel17, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 200, -1, -1));

        jLabel18.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel18.setText("Fecha de Atencion:");
        jPanel1.add(jLabel18, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 170, -1, -1));

        jLabel6.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel6.setText("Doctor:");
        jPanel1.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 140, -1, 20));

        AgregarTecnico1.setFont(new java.awt.Font("Candara", 1, 24)); // NOI18N
        AgregarTecnico1.setText("Detalles");
        jPanel1.add(AgregarTecnico1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 110, 100, -1));

        lblBuscarItems.setForeground(new java.awt.Color(136, 134, 133));
        lblBuscarItems.setText("Buscar Servicios");
        lblBuscarItems.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));
        jPanel1.add(lblBuscarItems, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 270, 150, 30));

        BuscarItemsNombre.setBackground(new java.awt.Color(233, 236, 239));
        BuscarItemsNombre.setBorder(null);
        BuscarItemsNombre.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BuscarItemsNombreActionPerformed(evt);
            }
        });
        jPanel1.add(BuscarItemsNombre, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 270, 190, 30));
        BuscarItemsNombre.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                filtrarLista();
            }

            public void removeUpdate(DocumentEvent e) {
                filtrarLista();
            }

            public void changedUpdate(DocumentEvent e) {
                filtrarLista();
            }
        });
        BuscarItemsNombre.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                lblBuscarItems.setVisible(false);
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (BuscarItemsNombre.getText().isEmpty()) {
                    lblBuscarItems.setVisible(true);
                }
            }
        });
        BuscarItemsNombre.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                lblBuscarItems.setVisible(false);
                filtrarLista();
            }

            public void removeUpdate(DocumentEvent e) {
                if (BuscarItemsNombre.getText().isEmpty()) {
                    lblBuscarItems.setVisible(true);
                } else {
                    lblBuscarItems.setVisible(false);
                }
                filtrarLista();
            }

            public void changedUpdate(DocumentEvent e) {
                if (BuscarItemsNombre.getText().isEmpty()) {
                    lblBuscarItems.setVisible(true);
                } else {
                    lblBuscarItems.setVisible(false);
                }
                filtrarLista();
            }
        });

        ListaItems.setModel(new javax.swing.AbstractListModel<String>(){
            String[] strings = {};
            public int getSize() { return strings.length; }
            public String getElementAt(int i) { return strings[i]; }
        });
        jScrollPane2.setViewportView(ListaItems);

        jPanel1.add(jScrollPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 310, 220, 270));

        jLabel11.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Imagenes/Buscar.png"))); // NOI18N
        jPanel1.add(jLabel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(35, 275, -1, -1));

        jPanel7.setBackground(new java.awt.Color(233, 236, 239));
        jPanel1.add(jPanel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 270, 220, 30));

        AgregarTecnico3.setFont(new java.awt.Font("Candara", 1, 24)); // NOI18N
        AgregarTecnico3.setText("Servicios");
        jPanel1.add(AgregarTecnico3, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 240, 110, -1));

        jLabel8.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel8.setText("Tipo de precio:");
        jPanel1.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(280, 330, -1, 20));

        ComboTipoPrecio.setModel(new javax.swing.DefaultComboBoxModel<TipoPrecioItem>(
            new TipoPrecioItem[] {
                new TipoPrecioItem("normal", "Normal"),
                new TipoPrecioItem("emergencia", "Emergencia (+30%)")
            }
        )
    );
    ComboTipoPrecio.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            ComboTipoPrecioActionPerformed(evt);
        }
    });
    jPanel1.add(ComboTipoPrecio, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 360, 140, -1));

    btnVistaPrevia.setBackground(new java.awt.Color(80, 35, 100));
    btnVistaPrevia.setForeground(new java.awt.Color(255, 255, 255));
    btnVistaPrevia.setText("Agregar y calcular");
    btnVistaPrevia.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            btnVistaPreviaActionPerformed(evt);
        }
    });
    jPanel1.add(btnVistaPrevia, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 400, 140, 30));

    jScrollPane4.setBorder(javax.swing.BorderFactory.createTitledBorder("Servicios Agregados:"));

    ListaItemsSeleccionados.setModel(new DefaultListModel<Servicio>());
    jScrollPane4.setViewportView(ListaItemsSeleccionados);

    jPanel1.add(jScrollPane4, new org.netbeans.lib.awtextra.AbsoluteConstraints(410, 300, 230, 280));

    TotalSumaServicios.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            TotalSumaServiciosActionPerformed(evt);
        }
    });
    jPanel1.add(TotalSumaServicios, new org.netbeans.lib.awtextra.AbsoluteConstraints(410, 590, 230, -1));

    jLabel3.setText("Total:");
    jPanel1.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(370, 590, -1, 20));

    formadepago.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
    formadepago.setText("Forma de Pago:");
    jPanel1.add(formadepago, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 670, -1, -1));

    jLabel7.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
    jLabel7.setText("Medio de pago:");
    jPanel1.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 700, -1, 20));

    TipoPago.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Efectivo", "QR", "Tarjeta"}));
    jPanel1.add(TipoPago, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 700, 460, -1));

    FormaPago.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Contado"}));
    jPanel1.add(FormaPago, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 670, 460, -1));

    guardar.setBackground(new java.awt.Color(80, 35, 100));
    guardar.setForeground(new java.awt.Color(255, 255, 255));
    guardar.setText("Guardar e Imprimir");
    guardar.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            guardarActionPerformed(evt);
        }
    });
    jPanel1.add(guardar, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 870, 140, 30));

    limpiar.setText("Limpiar");
    limpiar.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            limpiarActionPerformed(evt);
        }
    });
    jPanel1.add(limpiar, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 870, 100, 30));

    jPanel3.setBackground(new java.awt.Color(204, 204, 204));
    jPanel1.add(jPanel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(19, 100, 630, 1));

    jPanel5.setBackground(new java.awt.Color(204, 204, 204));
    jPanel1.add(jPanel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(19, 625, 630, 1));

    jPanel6.setBackground(new java.awt.Color(204, 204, 204));
    jPanel1.add(jPanel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(19, 230, 630, 1));

    AgregarTecnico4.setFont(new java.awt.Font("Candara", 1, 24)); // NOI18N
    AgregarTecnico4.setText("Pago");
    jPanel1.add(AgregarTecnico4, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 640, 160, -1));

    AgregarTecnico2.setFont(new java.awt.Font("Candara", 1, 24)); // NOI18N
    AgregarTecnico2.setText("Nota (Opcional)");
    jPanel1.add(AgregarTecnico2, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 750, 200, -1));

    jPanel8.setBackground(new java.awt.Color(204, 204, 204));
    jPanel1.add(jPanel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(19, 735, 630, 1));

    NotasTextArea.setColumns(20);
    NotasTextArea.setRows(5);
    jScrollPane3.setViewportView(NotasTextArea);
    NotasTextArea.setLineWrap(true);
    NotasTextArea.setWrapStyleWord(true);

    jPanel1.add(jScrollPane3, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 780, 610, 80));

    jPanel2.add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(1200, 50, 660, 910));

    getContentPane().add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 80, 1880, 980));

    FondoGris.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Imagenes/Background.jpg"))); // NOI18N
    getContentPane().add(FondoGris, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1920, 1080));

    pack();
    }// </editor-fold>//GEN-END:initComponents
    private void cargarServicios() {
        modeloLista.clear();
        try {
            Connection con = Conexion.obtenerConexion();
            String sql = "SELECT nombre_servicio FROM servicios WHERE estado = 1";
            PreparedStatement stmt = con.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                modeloLista.addElement(rs.getString("nombre_servicio"));
            }

            ListaItems.setModel(modeloLista);

            rs.close();
            stmt.close();
            con.close();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error cargando servicios: " + e.getMessage());
        }
    }

    private void cargarTablaAfiches() {
        DefaultTableModel modelo = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                switch (columnIndex) {
                    case 0:
                        return Integer.class;
                    case 1:
                        return String.class;
                    case 2:
                        return LocalDate.class;
                    case 3:
                        return String.class;
                    case 4:
                        return String.class;
                    case 5:
                        return String.class;
                    case 6:
                        return Double.class;
                    default:
                        return Object.class;
                }
            }
        };

        modelo.setColumnIdentifiers(new Object[]{
            "ID", "Paciente", "Fecha Atención", "Hora Atención", "Forma de Pago", "Medio de Pago", "Total de Precio"
        });

        String sql = """
    SELECT 
        a.id_afiche, CONCAT(a.nombre_paciente, ' ', a.apellido_paciente) AS paciente,
        a.fecha_atencion,
        a.hora_atencion,
        a.forma_pago,
        a.medio_pago,
        a.precio_total
    FROM afiches a
    """;

        try (Connection con = Conexion.obtenerConexion(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                modelo.addRow(new Object[]{
                    rs.getInt("id_afiche"),
                    rs.getString("paciente"),
                    rs.getTimestamp("fecha_atencion").toLocalDateTime().toLocalDate(),
                    rs.getString("hora_atencion"),
                    rs.getString("forma_pago"),
                    rs.getString("medio_pago"),
                    rs.getDouble("precio_total")
                });
            }

            TablaAfiches.setModel(modelo);

            sorter = new TableRowSorter<>(modelo);
            TablaAfiches.setRowSorter(sorter);

            List<RowSorter.SortKey> sortKeys = new ArrayList<>();
            sortKeys.add(new RowSorter.SortKey(0, SortOrder.DESCENDING));
            sorter.setSortKeys(sortKeys);
            sorter.sort();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Error al cargar afiches: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public String quitarTildes(String texto) {
        if (texto == null) {
            return "";
        }
        texto = Normalizer.normalize(texto, Normalizer.Form.NFD);
        return texto.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }

    private void filtrarLista() {
        String texto = BuscarItemsNombre.getText().toLowerCase().trim();

        if (texto.isEmpty()) {
            ListaItems.setModel(modeloLista);
            return;
        }

        String filtro = quitarTildes(texto);
        DefaultListModel<String> modeloFiltrado = new DefaultListModel<>();

        for (int i = 0; i < modeloLista.size(); i++) {
            String original = modeloLista.getElementAt(i);
            String normalizado = quitarTildes(original.toLowerCase());

            if (normalizado.contains(filtro)) {
                modeloFiltrado.addElement(original);
            }
        }

        ListaItems.setModel(modeloFiltrado);
    }

    private void actualizarPreciosYTotal() {
        DefaultListModel<Servicio> modelo = (DefaultListModel<Servicio>) ListaItemsSeleccionados.getModel();
        TipoPrecioItem tipoPrecioItem = (TipoPrecioItem) ComboTipoPrecio.getSelectedItem();
        String tipoPrecio = tipoPrecioItem.getValor();

        try {
            Connection con = Conexion.obtenerConexion();

            for (int i = 0; i < modelo.size(); i++) {
                Servicio servicio = modelo.getElementAt(i);

                String sql = "SELECT precio_normal, precio_emergencia FROM servicios WHERE nombre_servicio = ?";
                PreparedStatement stmt = con.prepareStatement(sql);
                stmt.setString(1, servicio.getNombre());
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    double nuevoPrecio = tipoPrecio.equals("emergencia") ? rs.getDouble("precio_emergencia") : rs.getDouble("precio_normal");
                    servicio.setPrecio(nuevoPrecio);
                }

                rs.close();
                stmt.close();
            }

            con.close();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al actualizar precios: " + ex.getMessage());
        }

        ListaItemsSeleccionados.repaint();
        actualizarTotal();
    }

    private void actualizarTotal() {
        DefaultListModel<Servicio> modelo = (DefaultListModel<Servicio>) ListaItemsSeleccionados.getModel();
        double total = 0.0;

        for (int i = 0; i < modelo.size(); i++) {
            total += modelo.getElementAt(i).getPrecio();
        }

        TotalSumaServicios.setText(String.format("%.2f", total));
    }

    private void TablaAfichesMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_TablaAfichesMouseClicked

    }//GEN-LAST:event_TablaAfichesMouseClicked

    private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField1ActionPerformed

    private void NombreActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_NombreActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_NombreActionPerformed

    private void ApellidoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ApellidoActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_ApellidoActionPerformed

    private void IDActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_IDActionPerformed
        ID.setVisible(false);
        ID.setEnabled(false);
        ID.setFocusable(false);
        ID.setRequestFocusEnabled(false);
    }//GEN-LAST:event_IDActionPerformed

    private void guardarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_guardarActionPerformed
        String nombrePaciente = Nombre.getText().trim();
        String apellidoPaciente = Apellido.getText().trim();
        String nombreDoctor = NombreDoctor.getText().trim();
        Date fechaAtencionDate = FechaAtencion.getDate();
        Date horaAtencionDate = (Date) Hora.getValue();
        String formaPago = FormaPago.getSelectedItem().toString();
        String medioPago = TipoPago.getSelectedItem().toString();
        String TotalServicioTexto = TotalSumaServicios.getText().trim().replace(",", ".");
        String notas = NotasTextArea.getText().trim();
        Date fechaGuardado = new Date();

        if (nombrePaciente.isEmpty() || apellidoPaciente.isEmpty() || nombreDoctor.isEmpty() || fechaAtencionDate == null) {
            JOptionPane.showMessageDialog(this, "Complete todos los campos obligatorios.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (TotalServicioTexto.isEmpty()) {
            JOptionPane.showMessageDialog(this, "El total del servicio está vacío.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        double TotalServicio;
        try {
            TotalServicio = Double.parseDouble(TotalServicioTexto);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "El total del servicio no es un número válido.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            Connection con = Conexion.obtenerConexion();

            String sqlMedico = "SELECT id_medico FROM medicos WHERE CONCAT(nombre, ' ', apellido) = ?";
            PreparedStatement psMedico = con.prepareStatement(sqlMedico);
            psMedico.setString(1, nombreDoctor);
            ResultSet rsMedico = psMedico.executeQuery();

            int idMedico = -1;
            if (rsMedico.next()) {
                idMedico = rsMedico.getInt("id_medico");
            } else {
                JOptionPane.showMessageDialog(this, "Médico no encontrado.");
                rsMedico.close();
                psMedico.close();
                con.close();
                return;
            }
            rsMedico.close();
            psMedico.close();

            int idCajero = ObtenerIdCajero();
            java.sql.Date sqlFecha = new java.sql.Date(fechaAtencionDate.getTime());
            java.sql.Time sqlHora = new java.sql.Time(horaAtencionDate.getTime());

            Calendar cal = Calendar.getInstance();
            cal.setTime(fechaAtencionDate);
            int anioCompleto = cal.get(Calendar.YEAR);
            int anioCorto = anioCompleto % 100;

            String sqlMaxNumero = "SELECT MAX(numero_ficha) AS max_num FROM afiches WHERE anio_ficha = ?";
            PreparedStatement psMax = con.prepareStatement(sqlMaxNumero);
            psMax.setInt(1, anioCorto);
            ResultSet rsMax = psMax.executeQuery();

            int siguienteNumero = 1;
            if (rsMax.next()) {
                int maxNum = rsMax.getInt("max_num");
                if (!rsMax.wasNull()) {
                    siguienteNumero = maxNum + 1;
                }
            }
            rsMax.close();
            psMax.close();

            String sqlInsert = "INSERT INTO afiches (id_cajero, id_medico, nombre_paciente, apellido_paciente, fecha_atencion, hora_atencion, forma_pago, medio_pago, precio_total, numero_ficha, anio_ficha, notas) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement psInsert = con.prepareStatement(sqlInsert, Statement.RETURN_GENERATED_KEYS);
            psInsert.setInt(1, idCajero);
            psInsert.setInt(2, idMedico);
            psInsert.setString(3, nombrePaciente);
            psInsert.setString(4, apellidoPaciente);
            psInsert.setDate(5, sqlFecha);
            psInsert.setTime(6, sqlHora);
            psInsert.setString(7, formaPago);
            psInsert.setString(8, medioPago);
            psInsert.setDouble(9, TotalServicio);
            psInsert.setInt(10, siguienteNumero);
            psInsert.setInt(11, anioCorto);
            psInsert.setString(12, notas.isEmpty() ? null : notas);

            int filas = psInsert.executeUpdate();
            int idAfiche = -1;

            if (filas > 0) {
                ResultSet rsKeys = psInsert.getGeneratedKeys();
                if (rsKeys.next()) {
                    idAfiche = rsKeys.getInt(1);
                }
                rsKeys.close();
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo guardar la ficha.");
                psInsert.close();
                con.close();
                return;
            }
            psInsert.close();

            ListModel<Servicio> itemsSeleccionados = ListaItemsSeleccionados.getModel();
            TipoPrecioItem tipoPrecioItem = (TipoPrecioItem) ComboTipoPrecio.getSelectedItem();
            String tipoPrecio = tipoPrecioItem.getValor();

            String sqlServicio = "SELECT id_servicio FROM servicios WHERE nombre_servicio = ?";
            PreparedStatement psServicio = con.prepareStatement(sqlServicio);

            String sqlDetalle = "INSERT INTO detalle_afiche (id_afiche, id_servicio, tipo_precio) VALUES (?, ?, ?)";
            PreparedStatement psDetalle = con.prepareStatement(sqlDetalle);

            for (int i = 0; i < itemsSeleccionados.getSize(); i++) {
                Servicio servicio = itemsSeleccionados.getElementAt(i);
                String nombreServicio = servicio.getNombre();

                psServicio.setString(1, nombreServicio);
                ResultSet rsServ = psServicio.executeQuery();

                if (rsServ.next()) {
                    int idServicio = rsServ.getInt("id_servicio");

                    psDetalle.setInt(1, idAfiche);
                    psDetalle.setInt(2, idServicio);
                    psDetalle.setString(3, tipoPrecio);

                    psDetalle.executeUpdate();
                }

                rsServ.close();
            }

            psServicio.close();
            psDetalle.close();
            con.close();

            JOptionPane.showMessageDialog(this, "Ficha y detalles guardados con éxito.");
            cargarTablaAfiches();
            generarFichaFormatoRecibo(
                    nombrePaciente,
                    apellidoPaciente,
                    nombreDoctor,
                    fechaAtencionDate,
                    horaAtencionDate,
                    formaPago,
                    medioPago,
                    TotalServicio,
                    ListaItemsSeleccionados.getModel(),
                    siguienteNumero,
                    anioCorto,
                    fechaGuardado,
                    Session.getNombreCompleto(),
                    idAfiche,
                    notas
            );
            limpiarCampos();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al guardar ficha: " + e.getMessage());
            e.printStackTrace();
        }
    }//GEN-LAST:event_guardarActionPerformed
    private int ObtenerIdCajero() {
        int id = -1;
        try {
            Connection con = Conexion.obtenerConexion();
            PreparedStatement ps = con.prepareStatement("SELECT id_cajero FROM cajeros WHERE id_usuario = ?");
            ps.setInt(1, idusuario);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                id = rs.getInt("id_cajero");
            }
            rs.close();
            ps.close();
            con.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al obtener el ID del Cajero: " + e.getMessage());
        }
        return id;
    }

    private void limpiarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_limpiarActionPerformed
        limpiarCampos();
    }//GEN-LAST:event_limpiarActionPerformed
    private void limpiarCampos() {
        Nombre.setText("");
        Apellido.setText("");
        NombreDoctor.setText("");
        FechaAtencion.setDate(null);
        Hora.setValue(new Date());
        FormaPago.setSelectedIndex(0);
        TipoPago.setSelectedIndex(0);
        TotalSumaServicios.setText("");
        DefaultListModel<Servicio> modeloLista = (DefaultListModel<Servicio>) ListaItemsSeleccionados.getModel();
        modeloLista.clear();
        ComboTipoPrecio.setSelectedIndex(0);
        NotasTextArea.setText("");
    }
    private void NombreDoctorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_NombreDoctorActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_NombreDoctorActionPerformed

    private void SeleccionarDocActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SeleccionarDocActionPerformed
        SeleccionarDoc SelecDoc = new SeleccionarDoc(NombreDoctor);
        SelecDoc.setLocationRelativeTo(null);
        SelecDoc.setVisible(true);
    }//GEN-LAST:event_SeleccionarDocActionPerformed

    private void btnVistaPreviaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnVistaPreviaActionPerformed
        List<String> seleccionados = ListaItems.getSelectedValuesList();

        DefaultListModel<Servicio> modeloSeleccionados;
        if (ListaItemsSeleccionados.getModel() instanceof DefaultListModel) {
            modeloSeleccionados = (DefaultListModel<Servicio>) ListaItemsSeleccionados.getModel();
        } else {
            modeloSeleccionados = new DefaultListModel<>();
        }

        try {
            Connection con = Conexion.obtenerConexion();
            TipoPrecioItem tipoPrecioItem = (TipoPrecioItem) ComboTipoPrecio.getSelectedItem();
            String tipoPrecio = tipoPrecioItem.getValor();

            for (String item : seleccionados) {
                boolean yaExiste = false;

                for (int i = 0; i < modeloSeleccionados.size(); i++) {
                    if (modeloSeleccionados.getElementAt(i).getNombre().equals(item)) {
                        yaExiste = true;
                        break;
                    }
                }

                if (!yaExiste) {
                    String sql = "SELECT precio_normal, precio_emergencia FROM servicios WHERE nombre_servicio = ?";
                    PreparedStatement stmt = con.prepareStatement(sql);
                    stmt.setString(1, item);
                    ResultSet rs = stmt.executeQuery();

                    if (rs.next()) {
                        double precio = tipoPrecio.equals("emergencia") ? rs.getDouble("precio_emergencia") : rs.getDouble("precio_normal");
                        Servicio servicio = new Servicio(item, precio);
                        modeloSeleccionados.addElement(servicio);
                    }

                    rs.close();
                    stmt.close();
                }
            }

            con.close();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al agregar servicio: " + e.getMessage());
        }

        ListaItemsSeleccionados.setModel(modeloSeleccionados);
        actualizarTotal();
    }//GEN-LAST:event_btnVistaPreviaActionPerformed

    private void TotalSumaServiciosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TotalSumaServiciosActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_TotalSumaServiciosActionPerformed

    private void ComboTipoPrecioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ComboTipoPrecioActionPerformed
        actualizarPreciosYTotal();
    }//GEN-LAST:event_ComboTipoPrecioActionPerformed

    private void btnCerrarSesionMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnCerrarSesionMouseExited

    }//GEN-LAST:event_btnCerrarSesionMouseExited

    private void btnCerrarSesionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCerrarSesionActionPerformed
        Login cerrar = new Login();
        cerrar.setLocationRelativeTo(null);
        cerrar.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_btnCerrarSesionActionPerformed

    private void BuscarItemsNombreActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BuscarItemsNombreActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_BuscarItemsNombreActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            e.printStackTrace();
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new GenerarFicha().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel AgregarTecnico;
    private javax.swing.JLabel AgregarTecnico1;
    private javax.swing.JLabel AgregarTecnico2;
    private javax.swing.JLabel AgregarTecnico3;
    private javax.swing.JLabel AgregarTecnico4;
    private javax.swing.JTextField Apellido;
    private javax.swing.JTextField BuscarItemsNombre;
    private javax.swing.JComboBox<TipoPrecioItem> ComboTipoPrecio;
    private com.toedter.calendar.JDateChooser FechaAtencion;
    private javax.swing.JLabel FondoGris;
    private javax.swing.JComboBox<String> FormaPago;
    private javax.swing.JSpinner Hora;
    private javax.swing.JTextField ID;
    private javax.swing.JList<String> ListaItems;
    private javax.swing.JList<Servicio> ListaItemsSeleccionados;
    private javax.swing.JLabel ListaPersonal;
    private javax.swing.JTextField Nombre;
    private javax.swing.JTextField NombreDoctor;
    private javax.swing.JTextArea NotasTextArea;
    private javax.swing.JButton SeleccionarDoc;
    private javax.swing.JPanel Superior;
    private javax.swing.JTable TablaAfiches;
    private javax.swing.JComboBox<String> TipoPago;
    private javax.swing.JTextField TotalSumaServicios;
    private javax.swing.JButton btnCerrarSesion;
    private javax.swing.JButton btnVistaPrevia;
    private javax.swing.JLabel formadepago;
    private javax.swing.JButton guardar;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JLabel lblBuscarItems;
    private javax.swing.JButton limpiar;
    // End of variables declaration//GEN-END:variables
}
