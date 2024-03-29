JSS: Java Simple Services
=========================



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



Know Issues and Some Nice-to-have Features
------------------------------------------

- Some Service methods are missing documentation.
- Needs a way to capture/redirect output from the running service.
- The service doesn't restart if it crashes (but I'm not sure that it should restart)



Download
--------

- Binary: [download JAR file]( https://github.com/lgfischer/JSS/raw/master/dist/jss.jar )
- Source code: clone/branch it using https://github.com/lgfischer/JSS.git or [download as zip]( https://github.com/lgfischer/JSS/zipball/master )



More Info
---------

- [Introducing JSS: Java Simple Services](http://coderender.blogspot.com.br/2012/07/introducing-jss-java-simple-services.html)
