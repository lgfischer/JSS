package com.leonardofischer.jss;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Object responsible for controlling the service (starting, stoping,
 * restarting, etc)
 * 
 * @author Leonardo Garcia Fischer
 * 
 */
public class ServiceController {

    /**
     * The Java binary used to execute the service in a new process. Default to
     * $JAVA_HOME/bin/java
     */
    String javaBin;

    /**
     * The classpath used to start the service in a new process. Default to the
     * same classpath used in the current execution environment.
     */
    String classpath;

    /**
     * The class with the main method that implements the service. Must be a
     * complete name (package + class name). Defaults to the same service class
     * used in the constructor.
     */
    String mainClass;

    /**
     * The socket port that the service will listen for, waiting for commands
     * such as 'status' or 'stop'.
     */
    int port = 6400;

    /**
     * During the restart command, this is the time (in milisseconds) that the
     * current process should wait after sending the stop command to a running
     * service. The new service will be started only after this time has passed.
     * 
     * This time is needed to let the previous running service close the
     * listener socket, so a new one in the same port can be created.
     */
    int restartWaitTime = 1000;

    /**
     * The args being managed while the service is not running.
     */
    String[] serviceArgs;

    /**
     * The service instance that should be controlled.
     */
    Service service;

    /**
     * The thread that listens for commands after the thread start (such as
     * status and stop).
     */
    ServiceListenerThread serviceListener = null;

    /**
     * The lock that keeps the serviceListener to send the finished token before
     * the Service.start() method finishes its execution.
     * 
     * Without this lock, the service may be restarted before the current
     * running service finishes its execution.
     */
    Object executionLock = new Object();

    /**
     * The lock that prevents the ServerSocket from being closed during a
     * command manipulation.
     * 
     * Without this lock, the service may close the server socket before
     * finishing to send the response to a stop command.
     */
    Object shutdownLock = new Object();

    /**
     * Creates a new service controller for the given service.
     * 
     * @param service
     *            the service that will be controlled by this controller. Must
     *            not be null.
     */
    ServiceController(Service service) {
        if (service == null) {
            throw new RuntimeException("service must not be null");
        }
        this.service = service;

        String separator = System.getProperty("file.separator");
        javaBin = System.getProperty("java.home") + separator + "bin" + separator + "java";
        classpath = System.getProperty("java.class.path");
        mainClass = service.getClass().getCanonicalName();
    }

    /**
     * Parse the given command line args and execute the command.
     * 
     * @param args
     */
    void parseArgs(String[] args) {
        if (args == null || args.length == 0) {
            service.printMessage(service.getCommandLine());
            return;
        }
        String command = args[0];
        serviceArgs = new String[args.length - 1];
        for (int i = 1; i < args.length; ++i) {
            serviceArgs[i - 1] = args[i];
        }
        executeCommand(command);
    }

    /**
     * Execute the given command, passing the given arg list to the service
     * 
     * @param command
     * @param serviceArgs
     */
    private void executeCommand(String command) {
        if (service.getStartCommand().equals(command)) {
            startService();
        }
        else if (service.getRunCommand().equals(command)) {
            runService();
        }
        else if (service.getStopCommand().equals(command)) {
            stopService();
        }
        else if (service.getRestartCommand().equals(command)) {
            restartService();
        }
        else if (service.getStatusCommand().equals(command)) {
            showServiceStatus();
        }
        else {
            service.onCommandNotHandled(command, serviceArgs);
        }
    }

    // based on
    // http://stackoverflow.com/questions/1229605/is-this-really-the-best-way-to-start-a-second-jvm-from-java-code
    void startService() {
        List<String> commands = new LinkedList<String>();
        commands.add(javaBin);
        commands.add("-cp");
        commands.add(classpath);
        commands.add(mainClass);
        commands.add(service.getRunCommand());
        for (int i = 0; i < serviceArgs.length; ++i) {
            commands.add(serviceArgs[i]);
        }
        ProcessBuilder processBuilder = new ProcessBuilder(commands);
        try {
            processBuilder.start();
            Thread.sleep(restartWaitTime);
            sendArguments("Service is running");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Starts the service.
     * 
     * Will run a listener thread that will wait for latter commands, such as
     * stop or status.
     */
    void runService() {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            serviceListener = new ServiceListenerThread(this, serverSocket);
            serviceListener.start();
            synchronized (executionLock) {
                service.start(serviceArgs);
            }
            synchronized (shutdownLock) {
                serviceListener.keepListening = false;
                serverSocket.close();
            }
        }
        catch (Exception e) {
            service.onServiceAlreadyRunning();
            service.printErrorMessage("Could not listen on port: " + port + ".");
        }
    }

    /**
     * Executes a thread that listen for commands sent by latter command line
     * executions of the service
     * 
     * @author Leonardo Garcia Fischer
     * 
     */
    private static class ServiceListenerThread extends Thread {
        ServiceController serviceController;
        ServerSocket serverSocket;
        boolean keepListening = true;

        ServiceListenerThread(ServiceController serviceController, ServerSocket serverSocket) {
            this.serviceController = serviceController;
            this.serverSocket = serverSocket;
            this.setName("ServiceCommandListenerThread");
        }

        public void run() {
            Socket clientSocket = null;

            while (keepListening) {
                try {
                    clientSocket = serverSocket.accept();

                    synchronized (serviceController.shutdownLock) {
                        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                        PrintStream out = new PrintStream(clientSocket.getOutputStream(), true);
                        ArrayList<String> argList = new ArrayList<String>(36);
                        String inputLine = in.readLine();
                        while (inputLine != null && !"".equals(inputLine)) {
                            argList.add(inputLine);
                            inputLine = in.readLine();
                        }

                        String command = argList.get(0);

                        String[] args = new String[argList.size() - 1];
                        for (int i = 1; i < args.length; ++i) {
                            args[i - 1] = argList.get(i);
                        }
                        Service service = serviceController.service;
                        if (service.getStopCommand().equals(command) || service.getRestartCommand().equals(command)) {
                            service.stop(args);
                            keepListening = false;
                            synchronized (serviceController.executionLock) {
                                out.println("");
                            }
                        }
                        else if (service.getStatusCommand().equals(command)) {
                            out.println(service.status(args));
                            out.println("");
                        }
                        else {
                            out.println(command);
                            out.println("");
                        }
                        in.close();
                        out.close();
                        clientSocket.close();
                    }
                }
                catch (IOException e) {
                    keepListening = false;
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
            try {
                serverSocket.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Sends the stop command to the running service.
     */
    void stopService() {
        sendArguments(service.getStopCommand());
    }

    private void restartService() {
        stopService();
        try {
            Thread.sleep(restartWaitTime);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        startService();
    }

    void showServiceStatus() {
        sendArguments(service.getStatusCommand());
    }

    private void sendArguments(String command) {
        Socket socket = null;
        PrintWriter out = null;
        BufferedReader in = null;

        String host = null;

        try {
            socket = new Socket((String) host, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            out.println(command);

            for (int i = 0; i < serviceArgs.length; ++i) {
                out.println(serviceArgs[i]);
            }
            out.println("");

            String output = in.readLine();
            while (output != null && !"".equals(output)) {
                service.printMessage(output);
                output = in.readLine();
            }

            out.close();
            in.close();
            socket.close();
        }
        catch (UnknownHostException e) {
            service.printErrorMessage("Don't know about host: " + host);
        }
        catch (IOException e) {
            service.onServiceNotRunning();
        }
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getPort() {
        return this.port;
    }
}
