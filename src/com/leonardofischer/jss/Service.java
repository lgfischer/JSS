package com.leonardofischer.jss;

/**
 * Base class that must be implemented/extended to create a service that
 * responds to start/stop/restart/status commands.
 * 
 * You must implement the start() and stop() methods to create a service, and
 * call parseArgs() to handle command-line parameters. All other methods are
 * optional, but recommended to customize the service command line interface.
 * 
 * @author Leonardo Garcia Fischer
 */
public abstract class Service {

    /**
     * The start method of your service. Must be implemented to execute your
     * application logic.
     * 
     * This method is executed in the same thread as the one that you called the
     * method Service.parseArgs() (probably the main thread).
     * 
     * Only return from it when your service finished its execution.
     * 
     * @param commandLineArgs
     *            the list of command line args sent by the user after the
     *            'start' command.
     */
    public abstract void start(String[] commandLineArgs);

    /**
     * The signal sent to a running service asking it to stop its execution and
     * return from the start method.
     * 
     * This method is called from a different thread from the one that the start
     * method is called. You can check this using
     * Thread.currentThread().getName() inside this method and the start method.
     * 
     * Implementations should return from it as soon as possible. You don't need
     * to wait for the method start to finish its execution. Synchronization
     * between these two methods is already implemented.
     * 
     * @param commandLineArgs
     *            the command line args sent by the user.
     */
    public abstract void stop(String[] commandLineArgs);

    /**
     * Used to handle command line args, eventually handling
     * start/stop/restart/status commands. Can be called directly in a instance
     * object of the service, inside a main method.
     * 
     * Actually it is just a shortcut to getServiceController.parseArgs(args).
     * 
     * @param args
     */
    public void parseArgs(String[] args) {
        serviceController.parseArgs(args);
    }

    /**
     * The signal sent to a running service asking for its current status.
     * Should return a string presenting the current status of the service.
     * 
     * The default implementation just returns the string "Service is running".
     * 
     * @param commandLineArgs
     *            the command line arguments after the 'status' command.
     * @return a string describing the current status of the service.
     */
    public String status(String[] commandLineArgs) {
        return "STATUS: the service is running";
    }

    /**
     * This method should return a string describing how to use this service,
     * including the behavior of the start, stop, restart, status and any other
     * command.
     * 
     * Any implementation should care that the actual command args must match
     * the ones returned by ServiceController.getStartCommand() and others.
     * 
     * The default implementation just prints
     * "Usage: java serviceClass {start|stop|restart|status}>"
     * 
     * @return
     */
    public String getCommandLine() {
        String className = this.getClass().getCanonicalName();
        String start = getStartCommand();
        String stop = getStopCommand();
        String restart = getRestartCommand();
        String status = getStatusCommand();
        return "Usage: java " + className + " {" + start + '|' + stop + '|' + restart + '|' + status + "}";
    }

    public void onServiceStarted() {
        printMessage("The service started");
    }

    /**
     * This method is called when a stop/status/restart command is executed, but
     * the service is not running.
     * 
     * The default implementation just prints the error message
     * "Service is not running"
     */
    public void onServiceNotRunning() {
        printErrorMessage("The service is not running");
    }

    /**
     * This method is called when a start/run command is executed, but the
     * service is already running.
     * 
     * The default implementation just prints the error message
     * "It appears that the service is already running".
     */
    public void onServiceAlreadyRunning() {
        String msg = "The service is already running, or another process is using the port " + serviceController.getPort() + ".";
        printErrorMessage(msg);
    }

    public void onServiceStoped() {
        printMessage("The service stoped");
    }

    public void onServiceDidNotStarted() {
        printErrorMessage("ERROR: It seems that the service failed to start");
    }

    public void onServiceDidNotStoped() {
        printErrorMessage("ERROR: The service did not stoped");
    }

    /**
     * This method is called when the user try to run a command that is not
     * handled by the service controller, or even if the user doesn't give a
     * command to execute.
     * 
     * The default implementation just prints the service usage, returned by
     * getCommandLine() method.
     * 
     * @param command
     * @param commandLineArgs
     */
    public void onCommandNotHandled(String command, String[] commandLineArgs) {
        printMessage(getCommandLine());
    }

    /**
     * Should print a message to the output stream.
     * 
     * The default implementation prints the given message to System.out.
     * Implementations of this method may print to any other output (such a
     * file, network, etc), handle it in any different way (such as sending an
     * email) or just ignore the message and return.
     * 
     * @param message
     *            the message that should be printed.
     */
    public void printMessage(String message) {
        System.out.println(message);
    }

    /**
     * Should print a error message to the output stream.
     * 
     * The default implementation prints the given error message to System.err.
     * Implementations of this method may print to any other output (such a
     * file, network, etc), handle it in any different way (such as sending an
     * email) or just ignore the message and return.
     * 
     * @param message
     *            the message that should be printed.
     */
    public void printErrorMessage(String errorMessage) {
        System.err.println(errorMessage);
    }

    /**
     * Returns the ServiceController instance bound to this service.
     * 
     * @return the ServiceController instance
     */
    public ServiceController getServiceController() {
        return serviceController;
    }

    public String getStartCommand() {
        return startCommand;
    }

    public void setStartCommand(String startCommand) {
        this.startCommand = startCommand;
    }

    public String getStopCommand() {
        return stopCommand;
    }

    public void setStopCommand(String stopCommand) {
        this.stopCommand = stopCommand;
    }

    public String getRestartCommand() {
        return restartCommand;
    }

    public void setRestartCommand(String restartCommand) {
        this.restartCommand = restartCommand;
    }

    public String getStatusCommand() {
        return statusCommand;
    }

    public void setStatusCommand(String statusCommand) {
        this.statusCommand = statusCommand;
    }

    public String getRunCommand() {
        return runCommand;
    }

    public void setRunCommand(String runCommand) {
        this.runCommand = runCommand;
    }

    /**
     * The command line string used to start the service as an independent
     * process
     */
    private String startCommand = "start";

    /**
     * The command line string used to execute the service in the current
     * process
     */
    private String runCommand = "run";

    /**
     * The command line string used to stop the service in execution
     */
    private String stopCommand = "stop";

    /**
     * The command line string used to restart the service in a new, independent
     * process
     */
    private String restartCommand = "restart";

    /**
     * The command line string used to get the current status of the service
     */
    private String statusCommand = "status";

    ServiceController serviceController = new ServiceController(this);
}
