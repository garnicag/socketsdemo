package com.bankApp;

import java.net.*;
import java.io.*;
import java.util.HashMap;
import java.util.Scanner;

public class Client {
    // Definición del valor del puerto del socket
    static int SOCKET_PORT = 4400;

    // Creación de socket para conexión con el servidor, entrada y salida de datos
    private static Socket clientSocket = null;
    private static BufferedReader clientInput = null;
    private static PrintWriter clientOutput = null;

    // Datos del cliente
    static String nombre_cliente;
    static String apellido_cliente;
    static String id_cliente;
    static String telefono_cliente;
    static String celular_cliente;
    static String direccion_cliente;
    static String email_cliente;
    static String id_cuenta;
    static String saldo;
    static String fundsToAdd;
    static String fundsToWithdraw;
    private static HashMap<String, String> clientHash = null;

    // Método principal del cliente
    public static void main(String[] args) {
        System.out.println("Politécnico Grancolombiano - Persistencia y datos transaccionales" +
        "\n\nEscoja una opción para realizar transacciones bancarias\n\n" +
        "[1] Crear cliente\n" +
        "[2] Consultar saldo\n" +
        "[3] Consignación de dinero\n" +
        "[4] Retiro de dinero\n");

        Scanner scan = new Scanner(System.in);
        System.out.println("Escoja una opción y confirmar con la tecla enter");

        byte selectedOption = 0;

        if (scan.hasNextByte()) {
            selectedOption = scan.nextByte();
        } else {
            scan.nextLine();
        }
        switch (selectedOption) {
            case 1:
                newClient();
                connectToServer();
                sendData(convertNewClientData().toString());
                closeResources();
                break;
            case 2:
                checkBalance();
                connectToServer();
                sendData(convertCheckBalance().toString());
                closeResources();
                break;
            case 3:
                addBalance();
                connectToServer();
                sendData(convertAddBalance().toString());
                closeResources();
                break;
            case 4:
                withdraw();
                connectToServer();
                sendData(convertWithdraw().toString());
                closeResources();
                break;
            default:
                System.out.println("\nOpción no disponible por el momento");
                break;
        }
        scan.close();
    }

    // Método para ingresar los datos de un nuevo cliente del banco
    private static void newClient() {
        Scanner scan = new Scanner(System.in);
        System.out.println("Registro de nuevo cliente, ingrese los datos");

        System.out.print("Nombre: ");
        nombre_cliente = scan.nextLine();

        System.out.print("Apellido: ");
        apellido_cliente = scan.nextLine();

        System.out.print("Número identificación: ");
        id_cliente = scan.nextLine();

        System.out.print("Teléfono: ");
        telefono_cliente = scan.nextLine();

        System.out.print("Celular: ");
        celular_cliente = scan.nextLine();

        System.out.print("Email: ");
        email_cliente = scan.nextLine();

        System.out.print("Dirección: ");
        direccion_cliente = scan.nextLine();

        System.out.print("Número de cuenta: ");
        id_cuenta = scan.nextLine();

        System.out.print("Saldo inicial: ");
        saldo = scan.nextLine();

        scan.close();
    }

    // Método para consultar el saldo de una cuenta
    private static void checkBalance() {
        Scanner scan = new Scanner(System.in);
        System.out.println("Ingrese el número de cuenta para consultar su saldo");

        System.out.print("Cuenta número: ");
        id_cuenta = scan.nextLine();
    }

    // Método para consignar dinero a una cuenta
    private static void addBalance() {
        Scanner scan = new Scanner(System.in);
        System.out.println("Ingrese el número de cuenta para consignar saldo");

        System.out.print("Cuenta número: ");
        id_cuenta = scan.nextLine();

        System.out.print("Valor a consignar: ");
        fundsToAdd = scan.nextLine();
    }

    // Método para retirar dinero de una cuenta
    private static void withdraw() {
        Scanner scan = new Scanner(System.in);
        System.out.println("Ingrese el número de cuenta para retirar dinero");

        System.out.print("Cuenta número: ");
        id_cuenta = scan.nextLine();

        System.out.print("Valor a retirar: ");
        fundsToWithdraw = scan.nextLine();
    }

    // Método para conectarse al servidor, con entrada y salida de datos
    private static void connectToServer() {
        try {
            clientSocket = new Socket("localhost", SOCKET_PORT);
            clientInput = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            clientOutput = new PrintWriter(new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream())), true);
        } catch (IOException e) {
            System.err.println(">>> Error: No es posible realizar la conexión con el servidor" + e.getMessage());
            System.exit(-1);
        }
    }

    // Método para enviar datos al servidor
    private static void sendData(String values) {
        try {
            clientOutput.println(values);
            values = clientInput.readLine();
            System.out.println("Enviando datos al servidor");
            System.out.println("Respuesta del servidor: " + values);
            System.out.println(clientInput.readLine());
        } catch (IOException e) {
            System.err.println(">>> Error: Fallo al enviar datos al servidor" + e.getMessage());
        }
    }

    // Método que formatea los datos de nuevo usuario a HashMap
    private static HashMap<String, String> convertNewClientData() {
        HashMap<String, String> hashed = new HashMap<String, String>();
        hashed.put("transactionType", "CREATE_CLIENT");
        hashed.put("nombre_cliente", nombre_cliente);
        hashed.put("apellido_cliente", apellido_cliente);
        hashed.put("id_cliente", id_cliente);
        hashed.put("telefono_cliente", telefono_cliente);
        hashed.put("celular_cliente", celular_cliente);
        hashed.put("direccion_cliente", direccion_cliente);
        hashed.put("email_cliente", email_cliente);
        hashed.put("id_cuenta", id_cuenta);
        hashed.put("saldo", saldo);
        return hashed;
    }

    // Método que formatea los datos para consultar el saldo
    private static HashMap<String, String> convertCheckBalance() {
        HashMap<String, String> hashed = new HashMap<String, String>();
        hashed.put("transactionType", "CHECK_BALANCE");
        hashed.put("id_cuenta", id_cuenta);
        return hashed;
    }

    // Método que formatea los datos para consignación de saldo
    private static HashMap<String, String> convertAddBalance() {
        HashMap<String, String> hashed = new HashMap<String, String>();
        hashed.put("transactionType", "ADD_BALANCE");
        hashed.put("id_cuenta", id_cuenta);
        hashed.put("fundsToAdd", fundsToAdd);
        return hashed;
    }

    // Método que formatea los datos para consignación de saldo
    private static HashMap<String, String> convertWithdraw() {
        HashMap<String, String> hashed = new HashMap<String, String>();
        hashed.put("transactionType", "WITHDRAW_BALANCE");
        hashed.put("id_cuenta", id_cuenta);
        hashed.put("fundsToWithdraw", fundsToWithdraw);
        return hashed;
    }

    // Método para cerrar recursos abiertos
    private static void closeResources() {
        clientOutput.close();
        try {
            clientInput.close();
            clientSocket.close();
        } catch (IOException e) {
            System.err.println(">>> Error: No es posible cerrar el uso de recursos" + e.getMessage());
        }
    }
}
