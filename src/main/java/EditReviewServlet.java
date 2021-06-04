import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

public class EditReviewServlet extends BaseServlet{

    /**
     * Function to handle GET request to this particular Servlet.
     * This shows a table to allow the user to edit an update.
     * Once the data is entered and sent, the POST function will
     * handle this ans redirect to this GET function.
     * @param request
     * @param response
     * @throws IOException
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        if(session.getAttribute("login") != null){
            if(request.getParameter("delete") != null){
                dbhandler.deleteReview(request.getParameter("id"));
                response.sendRedirect("/welcome");
            }
            response.setContentType("text/html");
            response.setStatus(HttpServletResponse.SC_OK);
            PrintWriter out = response.getWriter();

            VelocityEngine ve = (VelocityEngine) request.getServletContext().getAttribute("templateEngine");
            Template template = ve.getTemplate("templates/editReview.html");
            VelocityContext context = new VelocityContext();
            context.put("id", request.getParameter("id"));

            StringWriter writer = new StringWriter();
            template.merge(context, writer);
            out.println(writer.toString());
        }
        else {
            response.sendRedirect("/login");
        }
    }

    /**
     * Function to handle POST request that are sent from when submitting
     * data to edit a review. This function takes the input data and
     * sent it to be used to alter the databse accordingly.
     * @param request
     * @param response
     * @throws IOException
     */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        if(session.getAttribute("login") != null) {
            String title = request.getParameter("title");
            String text = request.getParameter("text");
            String id = request.getParameter("id");
            if (dbhandler.editReview(title, text, id)) session.setAttribute("reviewEdited", true);
            response.sendRedirect("/welcome");
        }
        else {
            response.sendRedirect("/login");
        }
    }

}
