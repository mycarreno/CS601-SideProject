package hotelapp;

import java.util.*;

public class HotelData {
    //maps [key = hotel id, value = hotel info]
    private Map<Integer, Hotel> hotelMap;
    //maps [key = hotel id, value = review info]
    private Map<Integer, TreeSet<Review>> hotelReviewMap;
    //maps [key = review id, value = review info]
    private Map<String, Review> reviewMap;
    //maps [key = word, value = Map[key = word occurrence, value = SortedSet[review info]]]
    private Map<String, Map<Integer, SortedSet<Review>>> wordMap;

    /**
     * Constructor used to initialize class variables
     * */
    public HotelData(){
        hotelMap = new HashMap<>();
        hotelReviewMap = new HashMap<>();
        reviewMap = new HashMap<>();
        wordMap = new HashMap<>();
    }

    public Map<Integer, TreeSet<Review>> getHotelReviews(){
        Map<Integer, TreeSet<Review>> reviews = new HashMap<>();
        for(Integer i: hotelReviewMap.keySet()){
            TreeSet<Review> s = new TreeSet<>();
            s.addAll(hotelReviewMap.get(i));
            reviews.put(i, s);
        }
        return reviews;
    }

    public List<Hotel> getHotels(){
        List<Hotel> hotels = new ArrayList<>();
        for(Integer i: hotelMap.keySet()){
            hotels.add(hotelMap.get(i));
        }
        return hotels;
    }

    public List<Review> getReviews(){
        List<Review> reviews = new ArrayList<>();
        for(String i: reviewMap.keySet()){
            reviews.add(reviewMap.get(i));
        }
        return reviews;
    }

    /**
     * Method to find a hotel given its hotel id.
     * Checks if the hotel id is on record and
     * prints message if it is not. Uses the hotelMap
     * hashMap to find hotels. Prints hotel's info.
     * @param hotelId (int containing hotel id of interest)*/
    public void findHotel(int hotelId){
        if(hotelMap.containsKey(hotelId)){
            System.out.println(hotelMap.get(hotelId));
        }
        else{
            System.out.println(System.lineSeparator() + "Hotel id not found" +
                    System.lineSeparator());
        }
    }

    /**
     * Method to find a hotel given its hotel id.
     * Checks if the hotel id is on record and
     * prints message if it is not. Uses the hotelMap
     * hashMap to find hotels. Returns a string representation
     * of the hotel object.
     * @param hotelId (int containing hotel id of interest)
     * @return result (String of hotel info)
     * */
    public String findHotelString(int hotelId){
        String result = "";
        if(hotelMap.containsKey(hotelId)){
            result = String.valueOf(hotelMap.get(hotelId));
        }
        return result;
    }

    /**
     * Method to find a hotel's reviews given its hotel id.
     * Checks if the hotel id is on record and
     * prints message if it is not. Uses hotelReviewsMap
     * to search for reviews. Prints review's info.
     * @param hotelId (int containing hotel id of interest)
     * */
    public void findReviews(int hotelId){
        if(hotelReviewMap.containsKey(hotelId)){
            for(Review r: hotelReviewMap.get(hotelId)){
                System.out.println(r);
            }
        }
        else{
            System.out.println(System.lineSeparator() + "Hotel id not found" +
                    System.lineSeparator());
        }
    }

    /**
     * Method to find a hotel's reviews given its hotel id.
     * Checks if the hotel id is on record and
     * prints message if it is not. Uses hotelReviewsMap
     * to search for reviews. Prints review's info.
     * @param hotelId (int containing hotel id of interest)
     * */
    public String findReviewsString(int hotelId, int num){
        String result = "";
        if(hotelReviewMap.containsKey(hotelId)){
            int i = 0;
            Iterator<Review> iterator = hotelReviewMap.get(hotelId).iterator();
                while(iterator.hasNext() && i < num){
                    result += String.valueOf(iterator.next()) + "SPLITHERE";;
                    i++;
                }
        }
        return result;
    }

    /**
     * Method to find a word in reviews. Uses the hashMap 'wordMap'
     * to check if the word is any review. If it is, the list of
     * reviews containing this word is printed in descending order of
     * occurrence of this word in their text. If it is not found, then
     * a message is printed to let the user know that this word is not
     * in any review.
     * @param word (String containing the word of interest)
     * */
    public void findWord(String word){
        if(wordMap.containsKey(word)){
            Map<Integer, SortedSet<Review>> map = wordMap.get(word);
            for(Integer i: map.keySet()){
                for(Review r: map.get(i)){
                    System.out.println(r);
                    System.out.println(i);
                }
            }
        }
        else{
            System.out.println(System.lineSeparator() + "The word '" + word + "' was not found!" +
                    System.lineSeparator());
        }
    }

    /**
     * Method to find a word in reviews. Uses the hashMap 'wordMap'
     * to check if the word is any review. If it is, the list of
     * reviews containing this word is stored in 'result' and returned.
     * @param word (String containing the word of interest)
     * @return result (String containing the reviews)
     * */
    public String findWordString(String word, int num){
        String result = "";
        int j = 0;
        if(wordMap.containsKey(word)){
            Map<Integer, SortedSet<Review>> map = wordMap.get(word);
            for(Integer i: map.keySet()){
                for(Review r: map.get(i)){
                    if(j < num){
                        result += r.toString() + "SPLITHERE";
                        j++;
                    }
                }
            }
        }
        return result;
    }

    /**
     * Method to check is a word is present in any reviews.
     * Uses the hashMap 'wordMap' to check.
     * @param word (String containing the word of interest)
     * @return (boolean)
     * */
    public boolean isWordPresent(String word){
        return wordMap.containsKey(word);
    }

    /**
     * Method used to fill the hashMap 'wordMap' used to
     * maintain a universal list of the words found in the reviews.
     * If a word is not already in the hashMap then its added to it.
     * If it is found, then its review is added to the set of review
     * that also contain this word in their text.
     * */
    public void populateWordMap(){
        for(String id: reviewMap.keySet()){
            reviewMap.get(id).mapWords();
            String words = reviewMap.get(id).getWords();
            String[] wordList = words.split(" ");

            for(String word: wordList){
                word = word.toLowerCase();
                if(word.trim().length() > 0) {
                    if (!(wordMap.containsKey(word))) {
                        TreeSet<Review> reviews = new TreeSet<>();
                        reviews.add(reviewMap.get(id));
                        TreeMap<Integer, SortedSet<Review>> tMap = new TreeMap<>(Collections.reverseOrder());
                        tMap.put(reviewMap.get(id).getOccurrence(word), reviews);
                        wordMap.put(word, tMap);
                    } else {
                        Map<Integer, SortedSet<Review>> tMap = wordMap.get(word);
                        if(!(tMap.containsKey(reviewMap.get(id).getOccurrence(word)))){
                            TreeSet<Review> reviews = new TreeSet<>();
                            reviews.add(reviewMap.get(id));
                            tMap.put(reviewMap.get(id).getOccurrence(word), reviews);
                        }
                        else{
                            SortedSet<Review> reviews = tMap.get(reviewMap.get(id).getOccurrence(word));
                            reviews.add(reviewMap.get(id));
                        }
                    }
                }
            }
        }
    }

    /**
     * Method check if a given hotel is present in
     * the database. Returns true if the hotel is
     * present, false otherwise.
     * @param id (Hotel id to be added)
     * @return (boolean)
     */
    public boolean isHotelPresent(String id){
        return hotelMap.containsKey(Integer.parseInt(id));
    }

    /**
     * Method to add a hotel to the hotelMap
     * @param id (Hotel id to be added)
     * @param h (Hotel object to be added)
     */
    public void hotelAdd(int id, Hotel h){
        hotelMap.put(id, h);
    }

    /**
     * Method to add a Review to the reviewMap
     * @param id (Hotel id to be added)
     * @param r (Review object to be added)
     */
    public void reviewAdd(String id, Review r){
        reviewMap.put(id, r);
    }

    /**
     * Method to add a Review to the reviewMap
     * @param id (Hotel id to be added)
     * @return true if id found otherwise false
     */
    public boolean hotelReviewContains(int id){
        return hotelReviewMap.containsKey(id);
    }

    /**
     * Method to add a TreeSet of reviews to the hotelReviewMap
     * @param id (Hotel id to be added)
     * @param tSet (TreeSet of reviews to be added)
     */
    public void hotelReviewPut(int id, TreeSet<Review> tSet){
        TreeSet<Review> newSet = new TreeSet<>(tSet);
        hotelReviewMap.put(id, newSet);
    }

    /**
     * Method to add a review to an existent entry in hotelReviewMap
     * @param id (Hotel id to be added)
     * @param r (Review to be added)
     */
    public void hotelReviewAdd(int id, Review r){
        if(hotelReviewContains(id)){
            hotelReviewMap.get(id).add(r);
        }
    }
}
