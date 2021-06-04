package hotelapp;

import com.google.gson.*;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.TreeSet;

public class Parser {
    /**
     * Method to parse through the file found at the
     * file path given. The method tries to catch any
     * exception thrown and prints a message if it does.
     * Adds the latitude and longitude to the JsonObj to
     * make the 'gson.fromJson()' call pickup all the required
     * fields in one simple call. @params a String and
     * @param filePath (String containing the path of a file to be parse) */
    public void parseHotels(String filePath, HotelData data){
        Gson gson = new Gson();

        try (FileReader reader = new FileReader(filePath)){
            String fileData = new String(Files.readAllBytes(Paths.get(filePath)));
            JsonParser p = new JsonParser();
            JsonObject jo = (JsonObject)p.parse(fileData);
            JsonArray jsonArr = jo.getAsJsonArray("sr");

            for(JsonElement jE: jsonArr){
                String lat = jE.getAsJsonObject().get("ll").getAsJsonObject().get("lat").getAsString();
                String lng = jE.getAsJsonObject().get("ll").getAsJsonObject().get("lng").getAsString();
                jE.getAsJsonObject().addProperty("lat", lat);
                jE.getAsJsonObject().addProperty("lng", lng);
                Hotel h = gson.fromJson(jE, Hotel.class);
                data.hotelAdd(jE.getAsJsonObject().get("id").getAsInt(), h);
            }
        }
        catch (IOException e){
            System.out.println("Could not read file: " + e);
        }
    }

    /**
     * Method to parse through the file found at the
     * file path given. The method tries to catch any
     * exception thrown and prints a message if it does.
     * @param filePath (String containing the path of a file to be parse)
     * */
    public void parseReviews(String filePath, HotelData data){
        Gson gson = new Gson();

        try (FileReader reader = new FileReader(filePath)) {
            String fileData = new String(Files.readAllBytes(Paths.get(filePath)));
            JsonParser p = new JsonParser();
            JsonObject jo = (JsonObject)p.parse(fileData);
            jo = jo.get("reviewDetails").getAsJsonObject().get("reviewCollection").getAsJsonObject();
            JsonArray jsonArr = jo.get("review").getAsJsonArray();

            for(JsonElement review: jsonArr){
                Review r = gson.fromJson(review, Review.class);
                data.reviewAdd(review.getAsJsonObject().get("reviewId").getAsString(), r);
                if(!(data.hotelReviewContains(review.getAsJsonObject().get("hotelId").getAsInt()))){
                    TreeSet<Review> reviewSet = new TreeSet<>();
                    reviewSet.add(r);
                    data.hotelReviewPut(review.getAsJsonObject().get("hotelId").getAsInt(), reviewSet);
                }
                else{
                    data.hotelReviewAdd(review.getAsJsonObject().get("hotelId").getAsInt(), r);
                }
            }
        }
        catch (IOException e){
            System.out.println("Could not read file: " + e);
        }
    }
}

