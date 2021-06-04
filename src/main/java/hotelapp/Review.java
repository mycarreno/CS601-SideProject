package hotelapp;

import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Review implements Comparable<Review>{
    private int hotelId;    //hotel id
    private String reviewId;    //review id
    @SerializedName(value = "ratingOverall")
    private int avgRating;  //review average rating
    private String title;   //review title
    private String reviewText;  //review text
    private String userNickname;    //review's user nickname
    @SerializedName(value = "reviewSubmissionTime")
    private Date datePosted;    //date or review's post
    private Map<String, Integer> wordOccurrence = new HashMap<>();   //hashMap to store words in review

    public int getHotelId(){
        return hotelId;
    }

    public String getReviewId(){
        return reviewId;
    }

    public int getRating(){
        return avgRating;
    }

    public String getTitle(){
        return title;
    }

    public String getText(){
        return reviewText;
    }

    public String getUserName(){
        return userNickname;
    }

    public String getDate(){
        return datePosted.toString();
    }

    /**
     * Get method that returns the number of times a 'word'
     * appears on the review.
     * @param word (a String containing the word of interest)
     * @return an int (the number of times a word appears in the review's text)
     * */
    public int getOccurrence(String word){
        return wordOccurrence.get(word);
    }

    /**
     * Get method that returns a String containing all
     * the words in the review. Iterates over the
     * wordOccurrence hashMap to account for the words
     * in the review
     * @return a string with all the words in the review's text
     * */
    public String getWords(){
        String s = "";
        for(String w: wordOccurrence.keySet()){
            s += " " + w;
        }
        return s;
    }

    /**
     * Method used to parse through a review and track the
     * occurrence of words.
     * */
    public void mapWords(){
        String text = reviewText.replaceAll("[^a-zA-Z ]","");
        String[] wordsFound = text.split(" ");

        for(String w: wordsFound) {
            w = w.toLowerCase();
            if(w.trim().length() > 0) {
                if (!(wordOccurrence.containsKey(w))) {
                    wordOccurrence.put(w, 1);
                } else {
                    wordOccurrence.replace(w, (wordOccurrence.get(w) + 1));
                }
            }
        }
    }

    /**
     * toString method to print object in a particular format
     * @return a String containing data about review
     * */
    public String toString(){
        return "Hotel ID: " + hotelId + System.lineSeparator() +
                "Review ID: " + reviewId + System.lineSeparator() +
                "Average Rating: " + avgRating + System.lineSeparator() +
                "Date Posted: " + datePosted + System.lineSeparator() +
                "User Nickname: " + userNickname + System.lineSeparator() +
                "Title: " + title + System.lineSeparator() +
                "Review Text: " + reviewText + System.lineSeparator();
    }

    /**
     * Method to compare between objects of this class. Used the default
     * compareTo method to print the reviews in the opposite order as normal
     * (i.e. newer reviews first). If the reviews have the same date, then
     * they are compared by reviewId (i.e. larger ids first).
     * @param r (i.e. a review)
     * @return int that determines what the order of reviews
     */
    @Override
    public int compareTo(Review r) {
        int position = r.datePosted.compareTo(this.datePosted);
        if(position == 0){
            position = this.reviewId.compareTo(r.reviewId);
        }
        return position;
    }
}
