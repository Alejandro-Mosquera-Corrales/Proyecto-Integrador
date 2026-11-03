// Autor: Jahel Gutiérrez | Commit 6: Vista interactiva por Consola (Scanner) conectada a los Controladores MVC
package com.uniajc.vista;

import com.uniajc.controlador.*;
import com.uniajc.modelo.*;

import javax.swing.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;

public class VistaConsola {

    private final ControladorAuth ctrlAuth;
    private final ControladorInventario ctrlInventario;
    private final ControladorCliente ctrlCliente;
    private final ControladorProveedor ctrlProveedor;
    private final ControladorVenta ctrlVenta;
    private final ControladorEntrega ctrlEntrega;
    private final Scanner scanner;

    public VistaConsola(ControladorAuth ctrlAuth) {
        this(ctrlAuth, new ControladorInventario(), new ControladorCliente(),
             new ControladorProveedor(), new ControladorVenta(), new ControladorEntrega());
    }

    public VistaConsola(ControladorAuth ctrlAuth, ControladorInventario ctrlInventario,
                        ControladorCliente ctrlCliente, ControladorProveedor ctrlProveedor,
                        ControladorVenta ctrlVenta, ControladorEntrega ctrlEntrega) {
        this.ctrlAuth = ctrlAuth;
        this.ctrlInventario = ctrlInventario;
        this.ctrlCliente = ctrlCliente;
        this.ctrlProveedor = ctrlProveedor;
        this.ctrlVenta = ctrlVenta;
        this.ctrlEntrega = ctrlEntrega;
        this.scanner = new Scanner(System.in);
    }

    public void iniciar() {
        System.out.println("\n============================================================");
        System.out.println("     MOBILIA HAUS - SISTEMA DE INVENTARIO (KARDEX) Y VENTAS ");
        System.out.println("============================================================");

        while (ctrlAuth.getUsuarioSesion() == null) {
            System.out.println("\n--- INICIO DE SESIÓN (RF-01) ---");
            System.out.print("Usuario (o escriba 'gui' para abrir ventana gráfica, 'salir' para terminar): ");
            String user = scanner.nextLine().trim();
            if (user.equalsIgnoreCase("salir")) {
                System.out.println("Hasta pronto.");
                return;
            }
            if (user.equalsIgnoreCase("gui")) {
                SwingUtilities.invokeLater(() -> new LoginGUI(ctrlAuth).setVisible(true));
                return;
            }
            System.out.print("Contraseña: ");
            String pass = scanner.nextLine().trim();

            try {
                Usuario u = ctrlAuth.iniciarSesion(user, pass);
                System.out.println("[OK] Bienvenido(a) " + u.getNombreCompleto() + " | Rol: " + u.getRol());
            } catch (Exception ex) {
                System.out.println("[ERROR] " + ex.getMessage());
            }
        }

        menuPrincipal();
    }

    private void menuPrincipal() {
        boolean activo = true;
        while (activo) {
            Usuario u = ctrlAuth.getUsuarioSesion();
            System.out.println("\n------------------------------------------------------------");
            System.out.println("MENÚ PRINCIPAL MOBILIA HAUS | Sesión: " + u.getNombreCompleto() + " [" + u.getRol() + "]");
            System.out.println("------------------------------------------------------------");
            System.out.println("1. Catálogo de Productos (RF-05, RF-06)");
            System.out.println("2. Kardex: Solicitar o Autorizar Movimientos (RF-07 a RF-17)");
            System.out.println("3. Gestión de Clientes (RF-03)");
            System.out.println("4. Gestión de Proveedores (RF-04)");
            System.out.println("5. Ventas, Pagos y Comprobantes (RF-10, RF-11, RF-18)");
            System.out.println("6. Entregas a Domicilio (RF-19, RF-20, RF-21)");
            System.out.println("7. Abrir Interfaz Gráfica Swing con sesión actual");
            System.out.println("0. Cerrar sesión y salir");
            System.out.print("Seleccione una opción: ");

            String op = scanner.nextLine().trim();
            try {
                switch (op) {
                    case "1": menuProductos(); break;
                    case "2": menuKardex(); break;
                    case "3": menuClientes(); break;
                    case "4": menuProveedores(); break;
                    case "5": menuVentas(); break;
                    case "6": menuEntregas(); break;
                    case "7":
                        SwingUtilities.invokeLater(() -> new MenuPrincipalGUI(u, ctrlAuth).setVisible(true));
                        return;
                    case "0":
                        ctrlAuth.cerrarSesion();
                        activo = false;
                        break;
                    default:
                        System.out.println("Opción no válida.");
                }
            } catch (Exception ex) {
                System.out.println("[ERROR] " + ex.getMessage());
            }
        }
    }

    private void menuProductos() throws Exception {
        System.out.println("\n--- CATÁLOGO DE PRODUCTOS ---");
        System.out.println("1. Listar catálogo y existencias");
        System.out.println("2. Crear ficha de producto (Stock inicial = 0 RN-01)");
        System.out.println("3. Ver alertas de stock bajo (RF-06)");
        System.out.print("Opción: ");
        String op = scanner.nextLine().trim();
        if ("1".equals(op)) {
            List<Producto> lista = ctrlInventario.listarProductos();
            for (Producto p : lista) {
                System.out.printf("ID #%d | %-26s | Cat: %-12s | $%10s | Stock: %d (Disp: %d, Mín: %d) | %s%n",
                        p.getIdProducto(), p.getNombreProducto(), p.getCategoria(),
                        p.getPrecioUnitario(), p.getStockActual(), p.getStockDisponible(),
                        p.getStockMinimo(), p.getEstado());
            }
        } else if ("2".equals(op)) {
            System.out.print("Nombre del producto: "); String nom = scanner.nextLine();
            System.out.print("Descripción: "); String desc = scanner.nextLine();
            System.out.print("Categoría: "); String cat = scanner.nextLine();
            System.out.print("Material: "); String mat = scanner.nextLine();
            System.out.print("Color: "); String col = scanner.nextLine();
            System.out.print("Precio unitario ($): "); BigDecimal precio = new BigDecimal(scanner.nextLine().trim());
            System.out.print("Stock mínimo: "); int min = Integer.parseInt(scanner.nextLine().trim());
            ctrlInventario.crearProductoCatalogo(nom, desc, cat, mat, col, precio, min, ctrlAuth.getUsuarioSesion());
            System.out.println("[OK] Producto creado en catálogo con Stock = 0 (RN-01).");
        } else if ("3".equals(op)) {
            List<Producto> alertas = ctrlInventario.listarProductosAlertaStockBajo();
            System.out.println("Productos que requieren reposición (" + alertas.size() + "):");
            for (Producto p : alertas) {
                System.out.println(" -> ID #" + p.getIdProducto() + " " + p.getNombreProducto()
                        + " | Stock actual: " + p.getStockActual() + " <= Mínimo: " + p.getStockMinimo());
            }
        }
    }

    private void menuKardex() throws Exception {
        System.out.println("\n--- KARDEX DE INVENTARIO ---");
        System.out.println("1. Ver historial de Kardex (RF-17)");
        System.out.println("2. Ver movimientos PENDIENTES");
        System.out.println("3. Solicitar movimiento de entrada/salida (RN-03)");
        System.out.println("4. Autorizar o Rechazar movimiento pendiente (RF-16)");
        System.out.print("Opción: ");
        String op = scanner.nextLine().trim();
        if ("1".equals(op) || "2".equals(op)) {
            List<MovimientoInventario> lista = "2".equals(op)
                    ? ctrlInventario.listarPendientesKardex()
                    : ctrlInventario.listarHistorialKardex(0);
            for (MovimientoInventario m : lista) {
                System.out.printf("Mov #%d | Prod: %-22s | %-24s | Cant: %d | Ant: %s -> Nvo: %s | Est: %s | Motivo: %s%n",
                        m.getIdMovimiento(),
                        m.getProductoAfectado() != null ? m.getProductoAfectado().getNombreProducto() : "",
                        m.getTipo() != null ? m.getTipo().getDescripcion() : "",
                        m.getCantidad(),
                        m.getStockAnterior(), m.getStockNuevo(),
                        m.getEstadoMovimiento(), m.getMotivo());
            }
        } else if ("3".equals(op)) {
            System.out.print("ID del producto: "); int idProd = Integer.parseInt(scanner.nextLine().trim());
            Producto p = ctrlInventario.buscarProductoPorId(idProd);
            System.out.println("Tipos: 1=Compra recibida, 2=Dev.Cliente, 3=Ajuste(+), 5=Merc.Dañada, 6=Ajuste(-), 7=Dev.Proveedor");
            System.out.print("ID Tipo Movimiento: "); int idTipo = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("Cantidad: "); int cant = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("Motivo / Soporte documental: "); String motivo = scanner.nextLine();
            ctrlInventario.solicitarMovimientoKardex(p, TipoMovimiento.desdeId(idTipo), cant, motivo, null, ctrlAuth.getUsuarioSesion());
            System.out.println("[OK] Solicitud registrada como PENDIENTE.");
        } else if ("4".equals(op)) {
            System.out.print("ID del movimiento pendiente: "); int idMov = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("¿Aprobar? (s/n): "); boolean aprobar = scanner.nextLine().trim().equalsIgnoreCase("s");
            System.out.print("Observación: "); String obs = scanner.nextLine();
            ctrlInventario.decidirMovimientoKardex(idMov, ctrlAuth.getUsuarioSesion(), aprobar, obs);
            System.out.println("[OK] Decisión aplicada transaccionalmente en el Kardex.");
        }
    }

    private void menuClientes() throws Exception {
        System.out.println("\n--- CLIENTES (RF-03) ---");
        System.out.println("1. Listar clientes");
        System.out.println("2. Registrar nuevo cliente");
        System.out.print("Opción: ");
        String op = scanner.nextLine().trim();
        if ("1".equals(op)) {
            for (Cliente c : ctrlCliente.listarClientes()) {
                System.out.println("ID #" + c.getIdCliente() + " | " + c);
            }
        } else if ("2".equals(op)) {
            System.out.print("Documento: "); String doc = scanner.nextLine();
            System.out.print("Nombre: "); String nom = scanner.nextLine();
            System.out.print("Apellido: "); String ape = scanner.nextLine();
            System.out.print("Teléfono: "); String tel = scanner.nextLine();
            System.out.print("Correo: "); String cor = scanner.nextLine();
            System.out.print("Dirección: "); String dir = scanner.nextLine();
            ctrlCliente.registrarCliente(nom, ape, doc, tel, cor, dir);
            System.out.println("[OK] Cliente registrado.");
        }
    }

    private void menuProveedores() throws Exception {
        System.out.println("\n--- PROVEEDORES (RF-04) ---");
        System.out.println("1. Listar proveedores");
        System.out.println("2. Registrar proveedor");
        System.out.print("Opción: ");
        String op = scanner.nextLine().trim();
        if ("1".equals(op)) {
            for (Proveedor p : ctrlProveedor.listarProveedores()) {
                System.out.println("ID #" + p.getIdProveedor() + " | " + p);
            }
        } else if ("2".equals(op)) {
            System.out.print("NIT: "); String nit = scanner.nextLine();
            System.out.print("Razón Social: "); String rs = scanner.nextLine();
            System.out.print("Teléfono: "); String tel = scanner.nextLine();
            System.out.print("Correo: "); String cor = scanner.nextLine();
            ctrlProveedor.registrarProveedor(nit, rs, tel, cor);
            System.out.println("[OK] Proveedor registrado.");
        }
    }

    private void menuVentas() throws Exception {
        System.out.println("\n--- VENTAS Y PAGOS (RF-10, RF-11, RF-18) ---");
        System.out.println("1. Listar ventas");
        System.out.println("2. Registrar nueva venta (descuenta stock en Kardex)");
        System.out.println("3. Registrar abono / pago a una venta (RF-18)");
        System.out.println("4. Ver comprobante de venta (RF-11)");
        System.out.print("Opción: ");
        String op = scanner.nextLine().trim();
        if ("1".equals(op)) {
            for (Venta v : ctrlVenta.listarVentas()) {
                System.out.printf("Venta #%d | Cliente: %-22s | Total: $%10s | Saldo: $%10s | Estado: %s%n",
                        v.getIdVenta(),
                        v.getCliente() != null ? v.getCliente().getNombreCompleto() : "",
                        v.getTotalVenta(), v.getSaldoPendiente(), v.getEstado());
            }
        } else if ("2".equals(op)) {
            System.out.print("ID del Cliente: "); int idCli = Integer.parseInt(scanner.nextLine().trim());
            Cliente cli = ctrlCliente.buscarPorId(idCli);
            Venta v = new Venta();
            v.setCliente(cli);
            v.setVendedor(ctrlAuth.getUsuarioSesion());
            System.out.print("Tipo de venta (Contado/Crédito): "); v.setTipoVenta(scanner.nextLine().trim());
            boolean seguir = true;
            while (seguir) {
                System.out.print("ID Producto a vender: "); int idP = Integer.parseInt(scanner.nextLine().trim());
                Producto prod = ctrlInventario.buscarProductoPorId(idP);
                System.out.print("Cantidad: "); int cant = Integer.parseInt(scanner.nextLine().trim());
                v.agregarDetalle(prod, cant);
                System.out.print("¿Agregar otro producto? (s/n): ");
                seguir = scanner.nextLine().trim().equalsIgnoreCase("s");
            }
            ctrlVenta.registrarVenta(v);
            System.out.println(ctrlVenta.generarComprobanteTexto(v));
        } else if ("3".equals(op)) {
            System.out.print("ID de la Venta: "); int idV = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("Monto a abonar ($): "); BigDecimal monto = new BigDecimal(scanner.nextLine().trim());
            System.out.println("Métodos: 1=Efectivo, 2=Tarjeta Crédito, 3=Tarjeta Débito, 4=Transferencia");
            System.out.print("ID Método: "); int idM = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("Referencia: "); String ref = scanner.nextLine();
            ctrlVenta.registrarPago(idV, monto, MetodoPago.desdeId(idM), ref, "Abono", ctrlAuth.getUsuarioSesion());
            System.out.println("[OK] Pago registrado y saldo pendiente descontado.");
        } else if ("4".equals(op)) {
            System.out.print("ID de la Venta: "); int idV = Integer.parseInt(scanner.nextLine().trim());
            Venta v = ctrlVenta.buscarVentaPorId(idV);
            System.out.println(ctrlVenta.generarComprobanteTexto(v));
        }
    }

    private void menuEntregas() throws Exception {
        System.out.println("\n--- ENTREGAS A DOMICILIO (RF-19 a RF-21) ---");
        System.out.println("1. Listar entregas");
        System.out.println("2. Avanzar estado de entrega (En bodega -> En ruta -> Entregado)");
        System.out.print("Opción: ");
        String op = scanner.nextLine().trim();
        if ("1".equals(op)) {
            for (Entrega e : ctrlEntrega.listarEntregasPorUsuario(ctrlAuth.getUsuarioSesion())) {
                System.out.printf("Entrega #%d | Venta #%d | Dir: %-24s | Recibe: %-18s | Estado: %s%n",
                        e.getIdEntrega(), e.getIdVenta(), e.getDireccionEntrega(), e.getNombreRecibe(), e.getEstado());
            }
        } else if ("2".equals(op)) {
            System.out.print("ID de la Entrega: "); int idE = Integer.parseInt(scanner.nextLine().trim());
            System.out.println("Nuevo estado: 2 = En ruta, 3 = Entregado");
            int idEst = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("Observación: "); String obs = scanner.nextLine();
            ctrlEntrega.avanzarEstadoEntrega(idE, ctrlAuth.getUsuarioSesion(), EstadoEntrega.desdeId(idEst), obs);
            System.out.println("[OK] Estado logístico actualizado.");
        }
    }
}
