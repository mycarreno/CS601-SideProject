import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterServlet extends BaseServlet{
    /**
     * Function to handle GET request about the
     * registration page. Blanks are show to enter
     * the username and password. If these are valid the
     * the data is sent to the POST request function.
     * @param request
     * @param response
     * @throws IOException
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        session.removeAttribute("logError");
        if(session.getAttribute("login") == null){
            response.setContentType("text/html");
            response.setStatus(HttpServletResponse.SC_OK);
            PrintWriter out = response.getWriter();

            VelocityEngine ve = (VelocityEngine) request.getServletContext().getAttribute("templateEngine");
            Template template = ve.getTemplate("templates/register.html");
            VelocityContext context = new VelocityContext();
            context.put("regError", session.getAttribute("regError"));
            session.removeAttribute("regError");

            StringWriter writer = new StringWriter();
            template.merge(context, writer);
            out.println(writer.toString());
        }
        else {
            response.sendRedirect("/welcome");
        }
    }

    /**
     * Function to register users. The password is validated
     * in the front end and back end. The user is added to the
     * login table and sent to the login servlet.
     * @param request
     * @param response
     * @throws IOException
     */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        Status status = Status.ERROR;
        String userName = request.getParameter("uname");
        String userPass = request.getParameter("pwd");
        if(validatePassword(userPass)){
            status = dbhandler.registerUser(userName, userPass);
        }

        if (status == Status.OK) {
            session.setAttribute("regScs",true);
            response.sendRedirect("/login");
        }
        else {
            System.out.println(status);
            session.removeAttribute("regScs");
            session.setAttribute("regError", true);
            response.sendRedirect("/register");
        }
    }

    /**
     * Function to validate the user's password.
     * The password is match with the requirements,
     * in order to assses if its valid.
     * @param pass
     * @return
     */
    public boolean validatePassword(String pass){
        boolean valid = false;
        if(pass.length() >= 5 && pass.length() <= 10){
            Pattern pattern = Pattern.compile("[a-zA-Z]+");
            Matcher matcher = pattern.matcher(pass);
            if(matcher.find()){
                pattern = Pattern.compile("[0-9]+");
                matcher = pattern.matcher(pass);
                if(matcher.find()){
                    pattern = Pattern.compile("[^a-zA-Z0-9]+");
                    matcher = pattern.matcher(pass);
                    if (matcher.find()) valid = true;
                }
            }
        }
        return valid;
    }
}
