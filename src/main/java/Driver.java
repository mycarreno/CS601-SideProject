import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.velocity.app.VelocityEngine;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;

/** Main class for the final project. Feel free to rename.  */
public class Driver {
    private static int PORT = 8080;
    protected static Logger log = LogManager.getLogger();

    public static void main(String[] args) {
        Server server = new Server(PORT);
        log.debug("Just testing");

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.addServlet(LoginServlet.class, "/login");
        context.addServlet(RegisterServlet.class, "/register");
        context.addServlet(WelcomeServlet.class,  "/welcome");
        context.addServlet(HotelSearcherServlet.class, "/hotelSearch");
        context.addServlet(ReviewSearchServlet.class, "/reviewSearch");
        context.addServlet(HotelInfoServlet.class, "/hotelInfo");
        context.addServlet(EditReviewServlet.class, "/editReviewServlet");
        context.addServlet(LinkHistoryServlet.class, "/linkHistory");

        VelocityEngine velocity = new VelocityEngine();
        velocity.init();

        context.setAttribute("templateEngine", velocity);
        server.setHandler(context);

        log.info("Starting server on port " + PORT + "...");

        try {
            server.start();
            server.join();

            log.info("Exiting...");
        }
        catch (Exception ex) {
            log.fatal("Interrupted while running server.", ex);
            System.exit(-1);
        }

    }
}
