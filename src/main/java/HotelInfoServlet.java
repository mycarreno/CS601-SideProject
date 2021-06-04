import com.google.gson.JsonObject;
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
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class HotelInfoServlet extends BaseServlet{
    /**
     * Function to handle GET requests used to display a
     * webpage with the data of a hotel. The page displays
     * all the pertinent information about a hotel, its reviews,
     * address, and name. Users can leave a review and save the
     * hotel from this page.
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
            Template template = ve.getTemplate("templates/hotelInfo.html");
            VelocityContext context = new VelocityContext();

            String string_id = StringEscapeUtils.escapeHtml(request.getParameter("id"));
            int id = Integer.parseInt(string_id);
            context.put("hotel", dbhandler.getHotel(id));
            context.put("reviews", dbhandler.getReview(id));
            context.put("reviewAdded", session.getAttribute("reviewAdded"));
            session.removeAttribute("reviewAdded");

            StringWriter writer = new StringWriter();
            template.merge(context, writer);
            out.println(writer.toString());
        }
        else {
            response.sendRedirect("/login");
        }
    }

    /**
     * Function that handles POST request that involve saving this
     * hotel, leaving a review or liking an already present review.
     * linking reviews and saving the hotel use AJAX.
     * @param request
     * @param response
     * @throws IOException
     */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Map<String, String> reviewInfo = new HashMap<>();
        HttpSession session = request.getSession();

        if(session.getAttribute("login") != null) {
            String userName = (String) session.getAttribute("userName");
            String save_id = request.getParameter("saveHotel");
            String delete_id = request.getParameter("deleteHotel");
            String like_review = request.getParameter("like");

            if(save_id == null && delete_id == null && like_review == null) {
                LocalDate date = LocalDate.now();
                byte[] randomBytes = new byte[6];
                Random random = new Random(System.currentTimeMillis());
                random.nextBytes(randomBytes);
                String reviewId = DatabaseHandler.encodeHex(randomBytes, 10);

                reviewInfo.put("review_id", userName + reviewId);
                reviewInfo.put("hotel_id", request.getParameter("id"));
                reviewInfo.put("rating", "0");
                reviewInfo.put("user", userName);
                reviewInfo.put("title", request.getParameter("title"));
                reviewInfo.put("text", request.getParameter("text"));
                reviewInfo.put("date", date.toString());

                if (dbhandler.addUserReview(reviewInfo)) session.setAttribute("reviewAdded", true);
                response.sendRedirect("/hotelInfo?id=" + request.getParameter("id"));
            }
            else if(like_review != null){
                PrintWriter out = response.getWriter();
                System.out.println( dbhandler.addLike((String)session.getAttribute("userName"),
                        request.getParameter("like")) );
                int id = Integer.parseInt(request.getParameter("id"));
                Map<String, Map<String, String>> reviews = dbhandler.getReview(id);

                JsonObject json_Reviews = new JsonObject();
                Map<String, String> updatedReview = reviews.get(request.getParameter("like"));

                for(String key: updatedReview.keySet()){
                    json_Reviews.addProperty(key, updatedReview.get(key));
                }
                System.out.println(json_Reviews);
                out.println(json_Reviews);
            }
            else if (save_id != null){
                PrintWriter out = response.getWriter();
                JsonObject json_Reviews = new JsonObject();
                json_Reviews.addProperty("hotelSaved", dbhandler.saveHotel(userName, save_id));

                System.out.println(json_Reviews);
                System.out.println(json_Reviews.getClass());
                out.println(json_Reviews);
            }
            else {
                dbhandler.deleteSaveHotel(userName, delete_id);
                response.sendRedirect("/welcome");
            }
        }
        else {
            response.sendRedirect("/login");
        }
    }

}
