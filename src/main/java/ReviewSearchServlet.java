import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Enumeration;
import java.util.Map;

public class ReviewSearchServlet extends BaseServlet{
    /**
     * Function to handle GET request about the
     * reviews search page. Blanks are show to enter
     * the keyword. If these are valid the
     * the data is sent to the POST request function.
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
            Template template = ve.getTemplate("templates/reviewSearch.html");
            VelocityContext context = new VelocityContext();
            context.put("reviews", session.getAttribute("reviews"));
            context.put("word", session.getAttribute("word"));
            session.removeAttribute("reviews");
            session.removeAttribute("word");

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
     * search about reviews according to give keyword.
     * Once the search is done the data is sent to the GET function.
     * @param request
     * @param response
     * @throws IOException
     */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        if(session.getAttribute("login") != null) {
            String word = (request.getParameter("word") != null) ? request.getParameter("word")
                    : request.getParameter("theWord");

            if(request.getParameter("like") != null){
                PrintWriter out = response.getWriter();
                System.out.println( dbhandler.addLike((String)session.getAttribute("userName"),
                        request.getParameter("like")) );

                Map<String, Map<String, String>> m = dbhandler.getReviews(word);

                JsonObject json_Reviews = new JsonObject();
                Map<String, String> updatedReview = m.get(request.getParameter("like"));

                for(String key: updatedReview.keySet()){
                    json_Reviews.addProperty(key, updatedReview.get(key));
                }
                System.out.println(json_Reviews);
                out.println(json_Reviews);
            }
            else {
                session.setAttribute("word", word);
                session.setAttribute("reviews", dbhandler.getReviews(word));
                response.sendRedirect("/reviewSearch");
            }
        }
        else {
            response.sendRedirect("/login");
        }
    }
}
