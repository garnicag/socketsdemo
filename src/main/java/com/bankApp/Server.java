package com.bankApp;

import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.HashMap;

public class Server {
    // Definición del valor del puerto que ocupará el socket
    static int SOCKET_PORT = 4400;

    /**
     * Datos de la conexión a MySQL
     * @param DB_NAME Nombre de la base de datos
     * @param DB_SERVER URL donde está corriendo el servidor MySQL
     * @param DB_PORT Puerto configurado de MySQL
     * @param DB_PARAMS Parámetros generales para la conexión a MySQL
     * @param DB_URL Concatenación de la URL con los valores de Server, Port y Params
     * @param DB_USER Usuario con permisos de acceso a la base de datos (por defecto "root")
     * @param DB_PASS Contraseña del usuario con permisos de acceso
     */
    // CAMBIAR ESTOS PARÁMETROS SEGÚN LAS CONFIGURACIONES DEL SERVICOR
    static String DB_NAME = "banco";
    static String DB_SERVER = "jdbc:mysql://localhost";
    static int DB_PORT = 3306;
    static String DB_PARAMS = "?useSSL=false&autoReconnect=true";
    static String DB_URL = DB_SERVER + ":" + DB_PORT + "/" + DB_NAME + DB_PARAMS;
    static String DB_USER = "null";
    static String DB_PASS = "jamiroquai";

    private static String queryOutput;

    // Método principal del servidor
    public static void main(String[] args) throws IOException {
        ServerSocket srvSocket = null;
        try {
            srvSocket = new ServerSocket(SOCKET_PORT);
        } catch (IOException e) {
            System.err.println(">>> Error: El puerto " + SOCKET_PORT + " ya está en uso y no se puede asignar a un socket");
            System.err.println(">>> Error: IOException " + e.getMessage());
            System.exit(-1);
        }
        System.out.println("Socket creado: " + srvSocket);

        Socket clientSocket = null;
        PrintWriter socketOutput = null;
        BufferedReader socketInput = null;

        try {
            // Espera por una petición del cliente para aceptarla.
            clientSocket = srvSocket.accept();
            System.out.println("Cliente conectado: " + clientSocket);
            // El socket obtiene datos de entrada.
            socketInput = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            // El socket retorna datos de salida.
            socketOutput = new PrintWriter(new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream())),true);

            String clientData = socketInput.readLine();
            System.out.println("Recibe del cliente: " + clientData);
            socketOutput.println("Petición recibida.");

            HashMap<String, String> hashData = toHashMap(clientData);
            String transactionType = (String) hashData.get("transactionType");
            dbConnection(transactionType, hashData);

            socketOutput.println(queryOutput);

            System.out.println("Se realizó la transacción: " + transactionType);
            socketOutput.println("Transacción " + transactionType + "completa");
        } catch (IOException e) {
            System.err.println(">>> Error: El socket no aceptó la petición del cliente " + e.getMessage());
            System.err.println(">>> Error: IOException " + e.getMessage());
        }
        srvSocket.close();
    }

    // Método para la conexión de la base de datos
    private static void dbConnection(String transactionType, HashMap<String, String> petitionHash) {
        try {
            System.out.println("Conectando con la base de datos MySQL");
            Connection db_connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);

            System.out.println("Conexión a MySQL establecida");
            Statement db_statement = db_connection.createStatement();

            StringBuilder sql = new StringBuilder();
            StringBuilder sql2 = new StringBuilder();

            // Selección de operación SQL según el tipo de transacción seleccionado
            switch (transactionType) {
                case "CREATE_CLIENT":
                    sql.append("INSERT INTO ").append("cuenta").
                            append(" (id_cuenta, saldo) VALUES ('").
                            append(petitionHash.get("id_cuenta")).append("', '").
                            append(petitionHash.get("saldo")).append("'); ");

                    sql2.append("INSERT INTO ").append("cliente").
                            append(" (id_cliente, nombre_cliente, apellido_cliente, telefono_cliente, celular_cliente, direccion_cliente, id_cuenta, email_cliente) VALUES ('").
                            append(petitionHash.get("id_cliente")).append("', '").
                            append(petitionHash.get("nombre_cliente")).append("', '").
                            append(petitionHash.get("apellido_cliente")).append("', '").
                            append(petitionHash.get("telefono_cliente")).append("', '").
                            append(petitionHash.get("celular_cliente")).append("', '").
                            append(petitionHash.get("direccion_cliente")).append("', '").
                            append(petitionHash.get("id_cuenta")).append("', '").
                            append(petitionHash.get("email_cliente")).append("'); ");

                    db_statement.executeUpdate(sql.toString());
                    db_statement.executeUpdate(sql2.toString());
                    break;

                case "CHECK_BALANCE":
                    // Sentencias SQL para consultar saldo
                    String cuenta = petitionHash.get("id_cuenta");
                    String saldo = "0";
                    sql.append("SELECT saldo FROM cuenta WHERE id_cuenta = '")
                            .append(cuenta).append("';");

                    ResultSet queryResult = db_statement.executeQuery(sql.toString());
                    while (queryResult.next()) {
                        saldo = queryResult.getString("saldo");
                        queryOutput = "El saldo para la cuenta número " + cuenta + " es de $" + saldo;
                    }
                    queryResult.close();
                    break;

                case "ADD_BALANCE":
                    // Sentencias SQL para añadir saldo
                    String cuentaFunds = petitionHash.get("id_cuenta");
                    int fundsToAdd = Integer.parseInt(petitionHash.get("fundsToAdd"));

                    ResultSet currentFunds = db_statement.executeQuery("SELECT saldo FROM cuenta WHERE id_cuenta = " + cuentaFunds);
                    while (currentFunds.next()) {
                        System.out.println("Fondos disponibles: " + currentFunds.getString("saldo"));
                        fundsToAdd = Integer.parseInt(currentFunds.getString("saldo")) + fundsToAdd;
                    }
                    sql.append("UPDATE cuenta SET saldo = ").append(fundsToAdd).append(" WHERE id_cuenta = ").append(cuentaFunds);
                    db_statement.executeUpdate(sql.toString());
                    queryOutput = "El nuevo saldo para la cuenta número " + cuentaFunds + " es de $" + fundsToAdd;
                    currentFunds.close();
                    break;

                case "WITHDRAW_BALANCE":
                    String cuentaWithdraw = petitionHash.get("id_cuenta");
                    int fundsToWithdraw = Integer.parseInt(petitionHash.get("fundsToWithdraw"));

                    ResultSet currentFundsW = db_statement.executeQuery("SELECT saldo FROM cuenta WHERE id_cuenta = " + cuentaWithdraw);
                    while (currentFundsW.next()) {
                        System.out.println("Fondos disponibles: " + currentFundsW.getString("saldo"));
                        fundsToWithdraw = Integer.parseInt(currentFundsW.getString("saldo")) - fundsToWithdraw;
                    }
                    if (fundsToWithdraw >= 0) {
                        sql.append("UPDATE cuenta SET saldo = ").append(fundsToWithdraw).append(" WHERE id_cuenta = ").append(cuentaWithdraw);
                        db_statement.executeUpdate(sql.toString());
                        queryOutput = "Retiro exitoso, el nuevo saldo para la cuenta número " + cuentaWithdraw + " es de $" + fundsToWithdraw;
                    }  else {
                        queryOutput = "Retiro fallido, no posee fondos suficientes para realizar esta transacción";
                    }

                    currentFundsW.close();
                    break;
            }
            System.out.println("Sentencia SQL ejecutada: " + sql.toString());
            System.out.println("Transacción realizada correctamente.");

            db_statement.close();
            db_connection.close();
        } catch (SQLException e) {
            System.err.println(">>> Error: Conexión a MySQL fallida " + e.getMessage());
        }
    }

    // Método de String a HashMap, basado en https://www.javacodeexamples.com/convert-string-or-string-array-to-hashmap-in-java-example/2350
    private static HashMap<String, String> toHashMap(String value) {
        HashMap<String, String> hashed = new HashMap<String, String>();
        value = value.replaceAll("[{}]", "");
        value = value.replaceAll(",\\s+", ",");
        String[] pairs = value.split(",");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            hashed.put(keyValue[0], keyValue[1]);
        }
        return hashed;
    }
}
