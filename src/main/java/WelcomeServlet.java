import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class WelcomeServlet extends BaseServlet{
    /**
     * Function to handle GET requests about
     * the user's data: saved hotels, last login,
     * and reviews. Allows for the deletion of reviews and
     * saved hotels and modification of reviews.
     * @param request
     * @param response
     * @throws IOException
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        if(session.getAttribute("login") != null){
            if(!((boolean) session.getAttribute("login"))){
                LocalDateTime date = LocalDateTime.now();
                String newFormat = "HH:mm'am', MM/dd/yyyy";
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(newFormat, Locale.ENGLISH);
                System.out.println(formatter.format(date));
                session.setAttribute("date", dbhandler.getLastLog((String)session.getAttribute("userName")));
                dbhandler.addLastLog((String)session.getAttribute("userName"), formatter.format(date));
                session.setAttribute("login", true);
            }
            response.setContentType("text/html");
            response.setStatus(HttpServletResponse.SC_OK);
            PrintWriter out = response.getWriter();

            VelocityEngine ve = (VelocityEngine) request.getServletContext().getAttribute("templateEngine");
            Template template = ve.getTemplate("templates/welcome.html");
            VelocityContext context = new VelocityContext();
            context.put("userName", session.getAttribute("userName"));
            context.put("userReviews", dbhandler.getUserReviews((String)session.getAttribute("userName")));
            context.put("savedHotels", dbhandler.getSavedHotels((String)session.getAttribute("userName")));
            context.put("date", session.getAttribute("date"));

            StringWriter writer = new StringWriter();
            template.merge(context, writer);
            out.println(writer.toString());
        }
        else {
            response.sendRedirect("/login");
        }
    }

    /**
     * Function to handle the POST request for the
     * welcome servlet that handles the users data.
     * If the user wants to logout then this function will
     * delete all the paramenters pertinent to this session.
     * @param request
     * @param response
     * @throws IOException
     */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        if(session.getAttribute("login") != null) {
            session.removeAttribute("hotelSaved");
            session.removeAttribute("reviewAdded");
            session.removeAttribute("logError");
            session.removeAttribute("regError");
            session.removeAttribute("regScs");
            session.removeAttribute("reviews");
            session.removeAttribute("hotels");
            session.removeAttribute("userName");
            session.removeAttribute("login");
            response.sendRedirect("/login");
        }
        else {
            response.sendRedirect("/login");
        }
    }
}
