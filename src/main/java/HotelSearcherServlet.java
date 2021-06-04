import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

public class HotelSearcherServlet extends BaseServlet{
    /**
     * Function to handle GET request about the search bar
     * used to search through hotels. Once a hotel's city and/or
     * a pertinent keyword is entered, the data is handle here and
     * sent to the POST function below for execution of the search.
     * Once that's done, this is redirected and sent here.
     * @param request
     * @param response
     * @throws IOException
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        if(session.getAttribute("login") != null) {
            response.setContentType("text/html");
            response.setStatus(HttpServletResponse.SC_OK);
            PrintWriter out = response.getWriter();

            VelocityEngine ve = (VelocityEngine) request.getServletContext().getAttribute("templateEngine");
            Template template = ve.getTemplate("templates/hotelSearch.html");
            VelocityContext context = new VelocityContext();
            context.put("hotels", session.getAttribute("hotels"));
            session.removeAttribute("hotels");

            StringWriter writer = new StringWriter();
            template.merge(context, writer);
            out.println(writer.toString());
        }
        else {
            response.sendRedirect("/login");
        }
    }

    /**
     * Function to handle POST request about a particular
     * search about hotels and their cities and pertinent
     * keyword about their names. Once the search is done
     * the data is sent to the GET function.
     * @param request
     * @param response
     * @throws IOException
     */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        if(session.getAttribute("login") != null) {
            String city = request.getParameter("city");
            String word = request.getParameter("word");
            session.setAttribute("hotels", dbhandler.getHotel(city, word));
            response.sendRedirect("/hotelSearch");
        }
        else {
            response.sendRedirect("/login");
        }
    }

}
