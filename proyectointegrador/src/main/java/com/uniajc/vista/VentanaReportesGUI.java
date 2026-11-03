// Autor: Jahel Gutiérrez | Commit 6: Ventana Swing de Reportes Ejecutivos y Auditoria usando Controladores MVC
package com.uniajc.vista;

import com.uniajc.controlador.ControladorCliente;
import com.uniajc.controlador.ControladorEntrega;
import com.uniajc.controlador.ControladorInventario;
import com.uniajc.controlador.ControladorVenta;
import com.uniajc.modelo.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class VentanaReportesGUI extends JFrame {

    private final Usuario usuario;
    private final ControladorVenta ctrlVenta;
    private final ControladorInventario ctrlInventario;
    private final ControladorCliente ctrlCliente;
    private final ControladorEntrega ctrlEntrega;

    private JLabel lblTotalVentas;
    private JLabel lblSaldoPorCobrar;
    private JLabel lblClientes;
    private JLabel lblProductos;
    private JLabel lblPendientes;

    private JTable tablaStockBajo;
    private DefaultTableModel modeloStockBajo;
    private JTable tablaVentas;
    private DefaultTableModel modeloVentas;

    public VentanaReportesGUI(Usuario usuario, ControladorVenta ctrlVenta,
                              ControladorInventario ctrlInventario, ControladorCliente ctrlCliente,
                              ControladorEntrega ctrlEntrega) {
        this.usuario = usuario;
        this.ctrlVenta = ctrlVenta;
        this.ctrlInventario = ctrlInventario;
        this.ctrlCliente = ctrlCliente;
        this.ctrlEntrega = ctrlEntrega;
        initComponents();
        actualizarReportes();
    }

    private void actualizarReportes() {
        try {
            List<Venta> ventas = ctrlVenta.listarVentas();
            List<Producto> productos = ctrlInventario.listarProductos();
            List<Producto> stockBajo = ctrlInventario.listarProductosAlertaStockBajo();
            List<Cliente> clientes = ctrlCliente.listarClientes();
            List<MovimientoInventario> movPendientes = ctrlInventario.listarPendientesKardex();
            List<Entrega> entregas = ctrlEntrega.listarEntregasPorUsuario(usuario);

            BigDecimal sumaVentas = BigDecimal.ZERO;
            BigDecimal sumaSaldos = BigDecimal.ZERO;
            for (Venta v : ventas) {
                if (v.getTotalVenta() != null) sumaVentas = sumaVentas.add(v.getTotalVenta());
                if (v.getSaldoPendiente() != null) sumaSaldos = sumaSaldos.add(v.getSaldoPendiente());
            }

            int entregasActivas = 0;
            for (Entrega e : entregas) {
                if (e.getEstado() != EstadoEntrega.ENTREGADO) entregasActivas++;
            }

            lblTotalVentas.setText("<html><center><b>Total Vendido</b><br>$" + sumaVentas + " (" + ventas.size() + " ventas)</center></html>");
            lblSaldoPorCobrar.setText("<html><center><b>Cartera / Saldo Pendiente</b><br>$" + sumaSaldos + "</center></html>");
            lblClientes.setText("<html><center><b>Clientes Registrados</b><br>" + clientes.size() + "</center></html>");
            lblProductos.setText("<html><center><b>Productos Catálogo</b><br>" + productos.size() + "</center></html>");
            lblPendientes.setText("<html><center><b>Kardex Pend. / Entregas</b><br>" + movPendientes.size() + " / " + entregasActivas + "</center></html>");

            modeloStockBajo.setRowCount(0);
            for (Producto p : stockBajo) {
                modeloStockBajo.addRow(new Object[]{
                    p.getIdProducto(),
                    p.getNombreProducto(),
                    p.getStockActual(),
                    p.getStockMinimo(),
                    p.getEstado()
                });
            }

            modeloVentas.setRowCount(0);
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            for (Venta v : ventas) {
                modeloVentas.addRow(new Object[]{
                    v.getIdVenta(),
                    v.getFechaVenta() != null ? v.getFechaVenta().format(fmt) : "",
                    v.getCliente() != null ? v.getCliente().getNombreCompleto() : "",
                    v.getTotalVenta(),
                    v.getSaldoPendiente(),
                    v.getEstado()
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al generar reportes: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void initComponents() {
        setTitle("Mobilia Haus - Reportes Generales y Auditoría");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setSize(1120, 660);
        setLocationRelativeTo(null);

        JPanel panelFondo = new JPanel(new BorderLayout(0, 12));
        panelFondo.setBackground(EstiloGUI.FONDO);

        JPanel panelHeader = new JPanel(new BorderLayout());
        panelHeader.setBackground(EstiloGUI.CAFE_OSCURO);
        panelHeader.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));

        JPanel panelMarca = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        panelMarca.setOpaque(false);
        JLabel lblLogo = new JLabel();
        EstiloGUI.ponerLogo(lblLogo, 48, 48);
        JLabel lblMarca = new JLabel("Mobilia Haus");
        lblMarca.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblMarca.setForeground(Color.WHITE);
        panelMarca.add(lblLogo);
        panelMarca.add(lblMarca);

        String rolStr = usuario.getRol() != null ? usuario.getRol().getNombreRol() : "Sin rol";
        JLabel lblUser = new JLabel("Usuario: " + usuario.getNombreCompleto() + " | Rol: " + rolStr);
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblUser.setForeground(Color.WHITE);

        panelHeader.add(panelMarca, BorderLayout.WEST);
        panelHeader.add(lblUser, BorderLayout.EAST);
        panelFondo.add(panelHeader, BorderLayout.NORTH);

        JPanel panelCentro = new JPanel(new BorderLayout(14, 14));
        panelCentro.setBackground(EstiloGUI.FONDO);
        panelCentro.setBorder(BorderFactory.createEmptyBorder(6, 18, 18, 18));

        JPanel panelTop = new JPanel(new BorderLayout());
        panelTop.setOpaque(false);
        JLabel lblTitulo = new JLabel("Tablero Ejecutivo y Auditoría de Indicadores");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitulo.setForeground(EstiloGUI.CAFE_OSCURO);

        JButton btnActualizar = new JButton("Actualizar Reportes");
        EstiloGUI.prepararBoton(btnActualizar, true);
        btnActualizar.addActionListener(e -> actualizarReportes());

        panelTop.add(lblTitulo, BorderLayout.WEST);
        panelTop.add(btnActualizar, BorderLayout.EAST);

        JPanel panelTarjetas = new JPanel(new GridLayout(1, 5, 12, 12));
        panelTarjetas.setBackground(EstiloGUI.FONDO);
        panelTarjetas.setPreferredSize(new Dimension(1000, 82));

        lblTotalVentas = crearTarjeta();
        lblSaldoPorCobrar = crearTarjeta();
        lblClientes = crearTarjeta();
        lblProductos = crearTarjeta();
        lblPendientes = crearTarjeta();

        panelTarjetas.add(lblTotalVentas);
        panelTarjetas.add(lblSaldoPorCobrar);
        panelTarjetas.add(lblClientes);
        panelTarjetas.add(lblProductos);
        panelTarjetas.add(lblPendientes);

        JPanel panelNorteCentro = new JPanel(new BorderLayout(0, 12));
        panelNorteCentro.setOpaque(false);
        panelNorteCentro.add(panelTop, BorderLayout.NORTH);
        panelNorteCentro.add(panelTarjetas, BorderLayout.CENTER);
        panelCentro.add(panelNorteCentro, BorderLayout.NORTH);

        String[] colsSB = {"ID Prod", "Producto", "Stock Actual", "Stock Mínimo", "Estado"};
        modeloStockBajo = new DefaultTableModel(colsSB, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tablaStockBajo = new JTable(modeloStockBajo);
        EstiloGUI.configurarTabla(tablaStockBajo);
        JScrollPane sp1 = new JScrollPane(tablaStockBajo);
        sp1.setBorder(BorderFactory.createTitledBorder("Productos con Alerta de Reposición (Stock <= Mínimo RF-06)"));

        String[] colsV = {"ID Venta", "Fecha", "Cliente", "Total Venta", "Saldo Pendiente", "Estado"};
        modeloVentas = new DefaultTableModel(colsV, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tablaVentas = new JTable(modeloVentas);
        EstiloGUI.configurarTabla(tablaVentas);
        JScrollPane sp2 = new JScrollPane(tablaVentas);
        sp2.setBorder(BorderFactory.createTitledBorder("Resumen de Ventas y Saldos por Cobrar (RF-10, RF-18)"));

        JPanel panelTablas = new JPanel(new GridLayout(1, 2, 14, 0));
        panelTablas.setOpaque(false);
        panelTablas.add(sp1);
        panelTablas.add(sp2);

        panelCentro.add(panelTablas, BorderLayout.CENTER);
        panelFondo.add(panelCentro, BorderLayout.CENTER);
        setContentPane(panelFondo);
    }

    private JLabel crearTarjeta() {
        JLabel l = new JLabel("<html><center><b>Indicador</b><br>0</center></html>", SwingConstants.CENTER);
        l.setOpaque(true);
        l.setBackground(EstiloGUI.BLANCO);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        l.setBorder(BorderFactory.createLineBorder(EstiloGUI.GRIS_BORDE));
        return l;
    }
}
