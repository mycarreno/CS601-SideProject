import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

public class LoginServlet extends BaseServlet{
    /**
     * Function to hadle login GET request. A black for
     * the userename and password are show. The data is collect
     * here and sent to the POST request for execution of validation
     * and login.
     * @param request
     * @param response
     * @throws IOException
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        if(session.getAttribute("login") == null) {
            response.setContentType("text/html");
            response.setStatus(HttpServletResponse.SC_OK);
            PrintWriter out = response.getWriter();

            VelocityEngine ve = (VelocityEngine) request.getServletContext().getAttribute("templateEngine");
            Template template = ve.getTemplate("templates/login.html");
            VelocityContext context = new VelocityContext();
            context.put("logError", session.getAttribute("logError"));
            context.put("regScs", session.getAttribute("regScs"));
            session.removeAttribute("logError");
            session.removeAttribute("regScs");

            StringWriter writer = new StringWriter();
            template.merge(context, writer);
            out.println(writer.toString());
        }
        else {
            response.sendRedirect("/welcome");
        }
    }

    /**
     * Once the user has input the username and password,
     * the data is sent here for authentication and if
     * valid the the user is allowed to login. The call is
     * then redirected to the welcome servlet.
     * @param request
     * @param response
     * @throws IOException
     */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        String userName = request.getParameter("uname");
        String userPass = request.getParameter("pwd");
        Status status = dbhandler.authenticateUser(userName, userPass);

        if (status == Status.OK) {
            session.setAttribute("login", false);
            session.setAttribute("userName", userName);
            response.sendRedirect("/welcome");
        }
        else {
            System.out.println(status);
            session.setAttribute("logError", true);
            response.sendRedirect("/login");
        }
    }
}