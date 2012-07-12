jServices
=========

Introduction
------------

Java Simple Services is a Java wraper that simplify the construction of background process easily. By implementing only one abstract class and creating a default main method on it, your application can run in backgroud, be stoped and you can query for its status without any complex code.

How to Use
----------

Implement the com.leonardofischer.jss.Service. You must implement the void start(String[] commandLineArgs) and void stop(String[] commandLineArgs) methods. Also, you need to implement the main method on your Service implementation, such as the following:

    package your.application.package;
    public class ApplicationService extends Service {

        public void start(String[] args) {
            // your service implementation goes here
        }

        public void stop(String[] args) {
            // your service stop method goes here
        }

        public static void main(String[] args) {
            ApplicationService appService = new ApplicationService();
            appService.parseArgs(args);
        }
    }

Then start your application using:

    java your.application.package.ApplicationService start

To stop your application, execute:

    java your.application.package.ApplicationService stop

Know Issues
-----------

- Some Service methods are missing documentation.

Download
--------

[Binary JAR file]( https://github.com/lgfischer/JSS/raw/master/dist/jss.jar )
