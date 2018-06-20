/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.are.liquidador;

import java.sql.SQLException;
import java.util.Date;

/**
 *
 * @author Aimer
 */
public class LiquidadorReparto {

    static java.util.Date fecha = new Date();
    static String periodo = "";
    static int anio = -1;
    static int mes = -1;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here

        if (args.length == 0) {
            System.out.println("Falta parametro");
            anio = fecha.getYear();
            mes = fecha.getMonth();
        } else {
            periodo = args[0];

            if (periodo.length() != 6) {
                System.out.println("Longitud parametro periodo debe ser 6. " + periodo);
                return;
            }

            try {

                anio = Integer.parseInt(periodo.substring(0, 4));
                mes = Integer.parseInt(periodo.substring(4, 6));

                if (anio <= 0) {
                    System.out.println("Parametro a�o no valido");
                    return;
                }

                if (mes < 0 || mes > 12) {
                    System.out.println("Parametro mes no valido");
                    return;
                }
            } catch (Exception e) {
                System.out.println("Error parametro perioro");
                return;
            }

        }

        db conexion = null;
        System.out.println("Inicio: " + fecha.toString());
        System.out.println("Periodo: Anio: " + anio + " Mes: " + mes);

        try {

            System.out.println("Inicio: " + fecha.toString());
            System.out.println("Clasificar Suministros");

            Inicializar();
            Clasificar();
            Devoluciones();


        } catch (Exception e) {
            // TODO Auto-generated catch block
            System.out.println("Error. " + e.getMessage());
            Utilidades.AgregarLog("Error: " + e.getMessage());
        } finally {
            
        }

    }

    public static void Inicializar() {
        System.out.println("Inicializando registros del periodo :" + periodo);
        Utilidades.AgregarLog("Inicializando registros del periodo :" + periodo);
        db conexion = null;
        try {
            conexion = new db();
            String sql = "UPDATE tb_reparto SET CONTRATO = '', "
                    + "CLASIFICACION ='', "
                    + "TIPOLOGIA  = '', "
                    + "MIXTO = 0,"
                    + "FECHA_CLASIFICACION = null ,"
                    + "USUARIO_CLASIFICACION = '' "
                    + " WHERE YEAR(FLECT) = ? AND MONTH(FLECT) = ? ";
            java.sql.PreparedStatement pst = conexion.getConnection().prepareStatement(sql);
            pst.setInt(1, anio);
            pst.setInt(2, mes);
            long total = conexion.Update(pst);
            if (total > 0) {
                conexion.Commit();
            }
            System.out.println("Total registros inicializados: " + total);
            Utilidades.AgregarLog("Total registros inicializados: " + total);

        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
            Utilidades.AgregarLog("Error: " + e.getMessage());
        } finally {
            if (conexion != null) {
                try {
                    conexion.Close();
                } catch (SQLException ex) {
                    System.out.println("Error: " + ex.getMessage());
                    Utilidades.AgregarLog("Error: " + ex.getMessage());
                }
            }
        }

    }

    public static void Clasificar() {
        System.out.println("Iniciando proceso de clasificacion de Facturas");
        Utilidades.AgregarLog("Iniciando proceso de clasificacion de Facturas");
        db conexion = null;
        long contador = 0;
        try {
            conexion = new db();
            String sql = "SELECT distinct unicom, itinerario,ruta FROM tb_reparto WHERE YEAR(FLECT) = ? AND MONTH(FLECT) = ? ORDER BY unicom,ruta,itinerario";
            java.sql.PreparedStatement pst = conexion.getConnection().prepareStatement(sql);
            pst.setInt(1, anio);
            pst.setInt(2, mes);
            java.sql.ResultSet rs = conexion.Query(pst);
            while (rs.next()) {
                sql = "select clasificacion,tipologia,mixto from itinerario where unicom=? and ruta=? and itinerario=? and estado=1";
                java.sql.PreparedStatement pst2 = conexion.getConnection().prepareStatement(sql);
                pst2.setString(1, rs.getString("unicom"));
                pst2.setString(2, rs.getString("ruta"));
                pst2.setString(3, rs.getString("itinerario"));

                java.sql.ResultSet rs2 = conexion.Query(pst2);
                if (rs2.next()) {  // Se encontro el itinerario
                    System.out.println("Clasificando Facturas: Unicom: " + rs.getString("unicom") + " Ruta: " + rs.getString("ruta") + " Itinerario: " + rs.getString("itinerario"));
                    Utilidades.AgregarLog("Clasificando Facturas: Unicom: " + rs.getString("unicom") + " Ruta: " + rs.getString("ruta") + " Itinerario: " + rs.getString("itinerario"));
                    sql = "UPDATE tb_reparto SET clasificacion=?, tipologia=?, mixto=?, "
                            + "usuario_clasificacion='robot', fecha_clasificacion =SYSDATETIME() "
                            + " WHERE unicom=? AND ruta=? AND itinerario=? "
                            + " AND YEAR(FLECT) = ? AND MONTH(FLECT) = ? ";

                    java.sql.PreparedStatement pst3 = conexion.getConnection().prepareStatement(sql);
                    pst3.setString(1, rs2.getString("clasificacion"));
                    pst3.setString(2, rs2.getString("tipologia"));
                    pst3.setString(3, rs2.getString("mixto"));
                    pst3.setString(4, rs.getString("unicom"));
                    pst3.setString(5, rs.getString("ruta"));
                    pst3.setString(6, rs.getString("itinerario"));
                    pst3.setInt(7, anio);
                    pst3.setInt(8, mes);
                    int filas = conexion.Update(pst3);
                    if (filas > 0) {
                        contador += filas;
                        System.out.println("Facturas clasificadas: " + filas);
                        Utilidades.AgregarLog("Facturas clasificadas: " + filas);
                        conexion.Commit();
                    }

                }

            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
            Utilidades.AgregarLog("Error: " + e.getMessage());
        } finally {
            if (conexion != null) {
                try {
                    conexion.Close();
                } catch (SQLException e) {
                    // TODO Auto-generated catch block
                    System.out.println("Error: " + e.getMessage());
                }
            }
        }

        System.out.println("Proceso finalizado");
        Utilidades.AgregarLog("Proceso finalizado");
        System.out.println("Total Facturas clasificadas: " + contador);
        Utilidades.AgregarLog("Total Facturas clasificadas: " + contador);

    }

    private static void Devoluciones() {
        System.out.println("Iniciando proceso Devueltos");
        Utilidades.AgregarLog("Iniciando proceso Devueltos");
        
        db conexion = null;
        
        try {
            
            conexion = new db();
            String sql = "UPDATE tb_reparto SET DEVOLUCION=1 WHERE nis_rad IN (SELECT DISTINCT suministro FROM ex_suministros WHERE estado=1 AND tipo='R' AND periodo = ?) AND YEAR(FLECT)=? AND MONTH(FLECT) = ? ";
            java.sql.PreparedStatement pst = conexion.getConnection().prepareStatement(sql);
            pst.setString(1,Integer.toString(anio) + String.format("%02d", mes));
            pst.setInt(2, anio);
            pst.setInt(3, mes);
            int contador = conexion.Update(pst);
            if (contador > 0) {
                conexion.Commit();
            }
        
            System.out.println("Registros actualizados como devueltos:  " + contador);
            Utilidades.AgregarLog("Registros actualizados como devueltos:  " + contador);
        
        } catch (SQLException ex) {
            System.out.println("Error: " + ex.getMessage());
            Utilidades.AgregarLog("Error: " + ex.getMessage());
        } finally {
            if (conexion != null) {
                try {
                    conexion.Close();
                } catch (SQLException e) {
                    // TODO Auto-generated catch block
                    System.out.println("Error: " + e.getMessage());
                    Utilidades.AgregarLog("Error: " + e.getMessage());
                }
            }
        }
        
        
    }
}
