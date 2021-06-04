import org.apache.commons.lang.StringEscapeUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class LinkHistoryServlet extends BaseServlet{
    /**
     * Function to handle the GET request about the
     * link history of an user. The search is done in the
     * POST request function and the data is redirected here
     * to be displayed.
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
            Template template = ve.getTemplate("templates/linkHistory.html");
            VelocityContext context = new VelocityContext();

            context.put("links", dbhandler.getUserLinks((String) session.getAttribute("userName")));

            StringWriter writer = new StringWriter();
            template.merge(context, writer);
            out.println(writer.toString());
        }
        else {
            response.sendRedirect("/login");
        }
    }

    /**
     * Function to obtain the link history of an user. The user
     * is able to clear and the execution of both showing and deleting
     * the history is done here. Once done the data is sent to the GET
     * function.
     * @param request
     * @param response
     * @throws IOException
     */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        if(session.getAttribute("login") != null){
            if(request.getParameter("delete") != null){
                dbhandler.deleteUserLinks((String) session.getAttribute("userName"));
                response.sendRedirect("/linkHistory");
            }
            else if (request.getParameter("add") != null){
                dbhandler.addUserLinks((String) session.getAttribute("userName"), request.getParameter("add"));
                response.sendRedirect(request.getParameter("add"));
            }
        }
        else {
            response.sendRedirect("/login");
        }
    }

}
