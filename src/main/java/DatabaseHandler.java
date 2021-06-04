import hotelapp.Hotel;
import hotelapp.HotelData;
import hotelapp.Review;
import hotelapp.Traverser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.sql.*;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class DatabaseHandler {
    private static Logger log = LogManager.getLogger();

    /** Used to configure connection to database. */
    private DatabaseConnector db;

    /** Used to generate password hash salt for user. */
    private Random random;

    /** Used to parse and hold parsed data before it is stored in the database*/
    private HotelData hData;

    /** Makes sure only one database handler is instantiated. */
    private static DatabaseHandler singleton = new DatabaseHandler();

    /** Used to determine if necessary tables are provided. */
    private static final String TABLES_SQL =
            "SHOW TABLES LIKE 'login_users';";

    /** Used to create the users table. */
    private static final String CREATE_SQL =
            "CREATE TABLE login_users (" +
                    "userid INTEGER AUTO_INCREMENT PRIMARY KEY, " +
                    "username VARCHAR(32) NOT NULL UNIQUE, " +
                    "password CHAR(64) NOT NULL, " +
                    "usersalt CHAR(32) NOT NULL);";

    /** Used to insert a new user into the database. */
    private static final String REGISTER_SQL =
            "INSERT INTO login_users (username, password, usersalt) " +
                    "VALUES (?, ?, ?);";

    /** Used to determine if a username already exists. */
    private static final String USER_SQL =
            "SELECT username FROM login_users WHERE username = ?";

    /** Used to retrieve the salt associated with a specific user. */
    private static final String SALT_SQL =
            "SELECT usersalt FROM login_users WHERE username = ?";

    /** Used to authenticate a user. */
    private static final String AUTH_SQL =
            "SELECT username FROM login_users " +
                    "WHERE username = ? AND password = ?";

    /** Used to remove a user from the database. */
    private static final String DELETE_SQL =
            "DELETE FROM login_users WHERE username = ?";

    /** Used to create hotels table. */
    private static final String CREATE_SQL2 =
            "CREATE TABLE hotels (" +
                    "hotel_id INTEGER PRIMARY KEY, " +
                    "city VARCHAR(50) NOT NULL," +
                    "name VARCHAR(1000) NOT NULL, " +
                    "rating float NOT NULL DEFAULT 0, " +
                    "link VARCHAR(500) NOT NULL," +
                    "street VARCHAR(500) NOT NULL," +
                    "province VARCHAR(500)," +
                    "country VARCHAR(500) NOT NULL);";

    /** Used to insert a new hotel into the database. */
    private static final String ADD_HOTELS_SQL =
            "INSERT INTO hotels (hotel_id, city, name, rating, link, street, province, country) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?);";

    /** Used to get hotels according to their city and key words. */
    private static final String GET_HOTELS_SQL =
            "SELECT * FROM hotels WHERE city = ? and name like ?;";

    /** Used to get hotels according to their id. */
    private static final String GET_HOTEL_SQL = "SELECT * FROM hotels " +
            "WHERE hotel_id = ?;";

    /** Used to create reviews table. */
    private static final String CREATE_SQL3 =
            "create table reviews (" +
                    "review_id varchar(50) primary key, " +
                    "hotel_id integer not null," +
                    "FOREIGN KEY (hotel_id) references hotels(hotel_id), " +
                    "rating float, " +
                    "user varchar(50), " +
                    "title varchar(500), " +
                    "text varchar(5000), " +
                    "date date not null," +
                    "likes integer not null default 0);";

    /** Used to insert a new review into the database. */
    private static final String ADD_REVIEWS_SQL =
            "INSERT INTO reviews (review_id, hotel_id, rating, user, title, text, date) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?);";

    /** Used to get search reviews according to the specified key words. */
    private static final String GET_REVIEWS_SQL =
            "SELECT reviews.hotel_id as hotel_id, review_id, name, " +
                    "user, title, text, date, likes FROM reviews inner join " +
                    "hotels on reviews.hotel_id = hotels.hotel_id " +
                    "WHERE text like ? ORDER BY date DESC;";

    /** Used to get reviews according to their hotel id. */
    private static final String GET_HOTEL_REVIEW_SQL =
            "SELECT * FROM reviews inner join hotels on reviews.hotel_id = hotels.hotel_id " +
                    "WHERE reviews.hotel_id = ? ORDER BY date DESC;";

    /** Used to get reviews of a specified user according to review id (id = userName + random character). */
    private static final String GET_USER_REVIEWS_SQL =
            "SELECT * FROM reviews inner join hotels on reviews.hotel_id = hotels.hotel_id " +
                    "WHERE review_id like ? ORDER BY date DESC;";

    /** Used to edit a review according to their id. */
    private static final String EDIT_REVIEW_SQL = "UPDATE reviews SET title = ?, text = ? " +
            "WHERE review_id = ?;";

    /** Used delete a review according to their id. */
    private static final String DELETE_REVIEW_SQL = "DELETE FROM reviews " +
            "WHERE review_id = ?;";

    /** Used to create a table of saved hotels. */
    private static final String CREATE_SQL4 = "create table savedHotels (" +
            "hotel_id integer not null," +
            "FOREIGN KEY (hotel_id) references hotels(hotel_id), " +
            "user varchar(50));";

    /** Used to add a hotel to a user's saved hotels list. */
    private static final String ADD_SAVE_HOTEL_SQL =
            "INSERT INTO savedHotels (hotel_id, user) " +
                    "VALUES (?, ?);";

    /** Used to delete a hotel from a user's saved hotels list. */
    private static final String DELETE_SAVE_HOTEL_SQL =
            "DELETE FROM savedHotels WHERE hotel_id = ? and user = ?;";

    /** Used to get a user's saved hotels list. */
    private static final String GET_SAVED_HOTEL_SQL =
            "SELECT hotels.hotel_id, name, rating, link FROM savedHotels " +
                    "inner join hotels on savedHotels.hotel_id = hotels.hotel_id WHERE user = ?;";

    /** Used to create a table of last logged in. */
    private static final String CREATE_SQL5 = "create table lastLog (" +
            "user varchar(50) UNIQUE," +
            "FOREIGN KEY (user) references login_users(username), " +
            "date varchar(50) not null);";

    /** Used to get the last logged in date from a specified user. */
    private static final String GET_LOG_SQL = "SELECT * FROM lastLog WHERE user = ?;";

    /** Used to add the last logged for a specified user. */
    private static final String ADD_LOG_SQL = "INSERT INTO lastLog (user, date) " +
            "VALUES (?, ?);";

    /** Used to update the last logged for a specified user. */
    private static final String EDIT_LOG_SQL = "UPDATE lastLog SET date = ? " +
            "WHERE user = ?;";

    /** Used to create a table of user's link history. */
    private static final String CREATE_SQL6 = "create table userLink (" +
            "user varchar(50)," +
            "FOREIGN KEY (user) references login_users(username), " +
            "link varchar(500) not null);";

    /** Used to get the link history from a specified user. */
    private static final String GET_HISTORY_SQL = "SELECT * FROM userLink WHERE user = ?;";

    /** Used to add a link to a specified user's history. */
    private static final String ADD_HISTORY_SQL = "INSERT INTO userLink (user, link) " +
            "VALUES (?, ?);";

    /** Used to delete the link history from a specific user. */
    private static final String DELETE_HISTORY_SQL = "DELETE FROM userLink WHERE user = ?;";

    /** Used to create a table of user's review likes. */
    private static final String CREATE_SQL7 = "create table likes (" +
            "user varchar(50), " +
            "FOREIGN KEY (user) references login_users(username), " +
            "review_id varchar(50), " +
            "FOREIGN KEY (review_id) references reviews(review_id), " +
            "primary key (user, review_id));";

    /** Used to check if the specified user has already liked the specific review. */
    private static final String CHECK_LIKES_SQL = "SELECT * FROM likes WHERE user = ? and review_id = ?;";

    /** Used to get the number of likes from a specified review. */
    private static final String GET_LIKES_SQL = "SELECT likes FROM reviews WHERE review_id = ?;";

    /** Used to add a like to a specified user and review. */
    private static final String ADD_LIKES1_SQL = "INSERT INTO likes (user, review_id) " +
            "VALUES (?, ?);";

    /** Used to update the number of likes a review has. */
    private static final String ADD_LIKES2_SQL = "UPDATE reviews SET likes = ? " +
            "WHERE review_id = ?;";

    /** Used to delete a user's like to a specific review. */
    private static final String DELETE_LIKES_SQL = "DELETE FROM likes WHERE user = ? and review_id = ?;";

    /**
     * Initializes a database handler for the Login example. Private constructor
     * forces all other classes to use singleton.
     */
    private DatabaseHandler() {
        Status status = Status.OK;
        random = new Random(System.currentTimeMillis());

        hData = new HotelData();
        //traverse and parse files
        Traverser traverse = new Traverser();
        traverse.traverseDirectories("input/hotels/hotels200.json", "Hotels", hData);
        traverse.traverseDirectories("input/reviews", "Reviews", hData);

        db = new DatabaseConnector("mydb", "root", "8624381m");
        status = db.testConnection() ? setupTables() : Status.CONNECTION_FAILED;

        if (status != Status.OK) {
            log.fatal(status.message());
        }
    }

    /**
     * Gets the single instance of the database handler.
     *
     * @return instance of the database handler
     */
    public static DatabaseHandler getInstance() {
        return singleton;
    }

    /**
     * Checks to see if a String is null or empty.
     * @param text - String to check
     * @return true if non-null and non-empty
     */
    public static boolean isBlank(String text) {
        return (text == null) || text.trim().isEmpty();
    }

    /**
     * Checks if necessary table exists in database, and if not tries to
     * create it.
     *
     */
    private Status setupTables() {
        Status status = Status.ERROR;

        try (Connection connection = db.getConnection()) {
            Statement statement = connection.createStatement();
            if (!statement.executeQuery(TABLES_SQL).next()) {
                // Table missing, must create
                log.debug("Creating tables...");
                statement.executeUpdate(CREATE_SQL);
                statement.executeUpdate(CREATE_SQL2);
                statement.executeUpdate(CREATE_SQL3);
                statement.executeUpdate(CREATE_SQL4);
                statement.executeUpdate(CREATE_SQL5);
                statement.executeUpdate(CREATE_SQL6);
                statement.executeUpdate(CREATE_SQL7);

                // Check if create was successful
                if (!statement.executeQuery(TABLES_SQL).next()) {
                    status = Status.CREATE_FAILED;
                }
                else {
                    //status = addHotels(connection);
                    status = addHotels(connection);
                    if(status == Status.OK) status = addReviews(connection);
                }
            }
            else {
                log.debug("Tables found.");
                status = Status.OK;
            }
        }
        catch (Exception ex) {
            status = Status.CREATE_FAILED;
            log.debug(status, ex);
        }

        return status;
    }

    /**
     * Adds hotels to the database. Each hotel has an id, name, rating,
     * and link. Prepared statements are used to create/add the
     * hotels to the database. Returns Status.OK if everything worked as intended.
     * @param connection connection to the database
     * @return status
     */
    private Status addHotels(Connection connection){
        Status status = Status.OK;
        List<Hotel> hotels = hData.getHotels();

        try {
            for(Hotel h :hotels){
                PreparedStatement statement = connection.prepareStatement(ADD_HOTELS_SQL);
                double rating = getRating(h.getId());
                String link = "https://www.expedia.com/"+h.getCity().replaceAll(" ", "-")+
                        "-Hotels.h"+h.getId()+".Hotel-Information";

                statement.setInt(1, h.getId());
                statement.setString(2, h.getCity());
                statement.setString(3, h.getName());
                statement.setDouble(4, rating);
                statement.setString(5, link);
                statement.setString(6, h.getStreets());
                statement.setString(7, h.getProvince());
                statement.setString(8, h.getCountry());
                statement.executeUpdate();
            }
        }
        catch (Exception ex){
            status = Status.HOTELS_ADDITION_FAILED;
            log.debug(status, ex);
        }

        return status;
    }

    /**
     * Returns an unmodifiable collection of hotels that correspond to
     * the result from the query, given the specified city and word.
     * Utilizes prepared statements to query the database.
     * @param city city to be used in the query
     * @param word word to be used in the query
     * @return unmodifiable collection of hotels
     */
    public Map<String, Map<String, String>> getHotel(String city, String word) {
        Map<String, Map<String, String>> hotels = new HashMap<>();
        try (Connection connection = db.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(GET_HOTELS_SQL)) {
                if(isBlank(word)) word = "";
                statement.setString(1, city);
                statement.setString(2, "%"+word+"%");
                System.out.println(city +" "+ word);
                ResultSet results = statement.executeQuery();

                while (results.next()) {
                    Map<String, String> data = new HashMap<>();

                    data.put("id", results.getString("hotel_id"));
                    data.put("city", results.getString("city"));
                    data.put("name", results.getString("name"));
                    data.put("rating", results.getString("rating"));
                    data.put("link", results.getString("link"));
                    hotels.put(results.getString("hotel_id"), data);
                }
            }
            catch (SQLException e) {
                log.debug(Status.INVALID_HOTEL_SEARCH, e);
            }
        }
        catch (SQLException ex) {
            log.debug(Status.CONNECTION_FAILED, ex);
        }
        return Collections.unmodifiableMap(hotels);
    }

    /**
     * Return info about the hotel with the specified id.
     * The data is returned as an unmodifiable map.
     * @param id hotel's id
     * @return hotel (map containing the data)
     */
    public Map<String, String> getHotel(int id) {
        Map<String, String> hotel = new HashMap<>();
        try (Connection connection = db.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(GET_HOTEL_SQL)) {
                statement.setInt(1, id);
                ResultSet results = statement.executeQuery();
                if(results.next()) {
                    hotel.put("id", results.getString("hotel_id"));
                    hotel.put("city", results.getString("city"));
                    hotel.put("name", results.getString("name"));
                    hotel.put("rating", results.getString("rating"));
                    hotel.put("link", results.getString("link"));
                    hotel.put("street", results.getString("street"));
                    hotel.put("province", results.getString("province"));
                    hotel.put("country", results.getString("country"));
                }
            }
            catch (SQLException e) {
                log.debug(Status.INVALID_HOTEL_SEARCH, e);
            }
        }
        catch (SQLException ex) {
            log.debug(Status.CONNECTION_FAILED, ex);
        }
        return Collections.unmodifiableMap(hotel);
    }

    /**
     * Adds reviews to the database from a file. Each review has an id, hotel id, rating,
     * user, title, text and date. Prepared statements are used to create/add the
     * reviews to the database. Returns Status.OK if everything worked as intended.
     * @param connection connection to the database
     * @return status
     */
    private Status addReviews(Connection connection){
        Status status = Status.OK;
        List<Review> reviews = hData.getReviews();

        try {
            for(Review r : reviews){
                PreparedStatement statement = connection.prepareStatement(ADD_REVIEWS_SQL);

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("E MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH);
                ZonedDateTime d = ZonedDateTime.parse(r.getDate(), formatter);
                String userName = isBlank(r.getUserName()) ? "Anonymous": r.getUserName();
                String title = isBlank(r.getTitle()) ? "No Title": r.getTitle();

                statement.setString(1, r.getReviewId());
                statement.setInt(2, r.getHotelId());
                statement.setDouble(3, r.getRating());
                statement.setString(4, userName);
                statement.setString(5, title);
                statement.setString(6, r.getText());
                statement.setString(7, String.valueOf(d.toLocalDate()));
                statement.executeUpdate();
            }
        }
        catch (Exception ex){
            status = Status.REVIEWS_ADDITION_FAILED;
            log.debug(status, ex);
        }
        return status;
    }

    /**
     * Adds reviews from a user. The data for the review is passed a
     * map containing all the pertinent information. Returns true/false
     * depending on whether the addition to the database was successful.
     * @param reviewData map with review info
     * @return reviewAdded
     */
    public boolean addUserReview(Map<String, String> reviewData){
        boolean reviewAdded = false;
        Status status;
        try (Connection connection = db.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(ADD_REVIEWS_SQL)) {
                    statement.setString(1, reviewData.get("review_id"));
                    statement.setInt(2, Integer.parseInt(reviewData.get("hotel_id")));
                    statement.setDouble(3, Double.parseDouble(reviewData.get("rating")));
                    statement.setString(4, reviewData.get("user"));
                    statement.setString(5, reviewData.get("title"));
                    statement.setString(6, reviewData.get("text"));
                    statement.setString(7, reviewData.get("date"));
                    reviewAdded = (statement.executeUpdate() == 1);
            } catch (Exception ex) {
                status = Status.REVIEWS_ADDITION_FAILED;
                log.debug(status, ex);
            }
        } catch (SQLException ex) {
            log.debug(Status.CONNECTION_FAILED, ex);
        }
        return reviewAdded;
    }

    /**
     * Returns a map of reviews that contain the specified word in
     * their texts. The function uses prepared statements to
     * execute the query. The function takes as a parameter the
     * word to be searched among the reviews.
     * @param word (String to be search in reviews)
     * @return reviews (An unmodifiable list of the reviews)
     */
    public Map<String, Map<String, String>> getReviews(String word) {
        Map<String, Map<String, String>> reviews = new HashMap<>();
        try (Connection connection = db.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(GET_REVIEWS_SQL)) {
                statement.setString(1, "%"+word+"%");
                ResultSet results = statement.executeQuery();

                while (results.next()) {
                    Map<String, String> data = new HashMap<>();
                    data.put("hotel_id", results.getString("hotel_id"));
                    data.put("review_id", results.getString("review_id"));
                    data.put("name", results.getString("name"));
                    data.put("user", results.getString("user"));
                    data.put("title", results.getString("title"));
                    data.put("text", results.getString("text"));
                    data.put("date", results.getString("date"));
                    data.put("likes", results.getString("likes"));
                    reviews.put(results.getString("review_id"), data);
                }
            }
            catch (SQLException e) {
                log.debug(Status.INVALID_REVIEW_SEARCH, e);
            }
        }
        catch (SQLException ex) {
            log.debug(Status.CONNECTION_FAILED, ex);
        }
        System.out.println(reviews);
        return Collections.unmodifiableMap(reviews);
    }

    /**
     * Returns a map containing all the reviews from a specified
     * hotel. The hotel's id is passed to be used in the query.
     * Prepared statements are used to query the database.
     * @param id integer containing the hotel's id
     * @return reviews (an unmodifiable map containing the reviews)
     */
    public Map<String, Map<String, String>> getReview(int id) {
        Map<String, Map<String, String>> reviews = new HashMap<>();
        try (Connection connection = db.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(GET_HOTEL_REVIEW_SQL)) {
                statement.setInt(1, id);
                ResultSet results = statement.executeQuery();
                while (results.next()) {
                    Map<String, String> data = new HashMap<>();
                    data.put("review_id", results.getString("review_id"));
                    data.put("name", results.getString("name"));
                    data.put("user", results.getString("user"));
                    data.put("title", results.getString("title"));
                    data.put("text", results.getString("text"));
                    data.put("date", results.getString("date"));
                    data.put("likes", results.getString("likes"));
                    reviews.put(results.getString("review_id"), data);
                }
            }
            catch (SQLException e) {
                log.debug(Status.INVALID_REVIEW_SEARCH, e);
            }
        }
        catch (SQLException ex) {
            log.debug(Status.CONNECTION_FAILED, ex);
        }
        return Collections.unmodifiableMap(reviews);
    }

    /**
     * Function to return all the reviews from a specified user.
     * The user's name is passed to be used the query. Prepared
     * statements are used to query the database.
     * @param userName String containing user's name
     * @return reviews (an unmodifiable map containing the reviews)
     */
    public Map<String, Map<String, String>> getUserReviews(String userName) {
        Map<String, Map<String, String>> reviews = new HashMap<>();
        try (Connection connection = db.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(GET_USER_REVIEWS_SQL)) {
                statement.setString(1, "%"+userName+"%");
                ResultSet results = statement.executeQuery();

                while (results.next()) {
                    Map<String, String> data = new HashMap<>();
                    data.put("id", results.getString("review_id"));
                    data.put("name", results.getString("name"));
                    data.put("user", results.getString("user"));
                    data.put("title", results.getString("title"));
                    data.put("text", results.getString("text"));
                    data.put("date", results.getString("date"));
                    data.put("likes", results.getString("likes"));
                    reviews.put(results.getString("review_id"), data);
                }
            }
            catch (SQLException e) {
                log.debug(Status.INVALID_REVIEW_SEARCH, e);
            }
        }
        catch (SQLException ex) {
            log.debug(Status.CONNECTION_FAILED, ex);
        }
        System.out.println(Collections.unmodifiableMap(reviews));
        return Collections.unmodifiableMap(reviews);
    }

    /**
     * Function used to edit the title and text from a specified
     * review. Prepared statements are used to execute the query.
     * Returns true/false depending on whether the update was
     * successful.
     * @param title new title
     * @param text new text
     * @param id review's id
     * @return edited
     */
    public boolean editReview(String title, String text, String id) {
        boolean edited = false;
        try (Connection connection = db.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(EDIT_REVIEW_SQL)) {
                statement.setString(1, title);
                statement.setString(2, text);
                statement.setString(3, id);
                edited = (statement.executeUpdate() == 1);
            }
            catch (SQLException e) {
                log.debug(Status.INVALID_REVIEW_SEARCH, e);
            }
        }
        catch (SQLException ex) {
            log.debug(Status.CONNECTION_FAILED, ex);
        }
        return edited;
    }

    /**
     * Function to delete a specific review. The review's
     * id is passed to be used in the query. Prepared
     * statements are used to delete the review. Returns
     * true/false depending on whether the update was
     * successful.
     * @param id the review's id
     * @return delete
     */
    public boolean deleteReview(String id) {
        boolean deleted = false;
        try (Connection connection = db.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(DELETE_REVIEW_SQL)) {
                statement.setString(1, id);
                deleted = (statement.executeUpdate() == 1);
            }
            catch (SQLException e) {
                log.debug(Status.INVALID_REVIEW_SEARCH, e);
            }
        }
        catch (SQLException ex) {
            log.debug(Status.CONNECTION_FAILED, ex);
        }
        return deleted;
    }

    /**
     * Function to add hotels to a user's save hotel list.
     * The user's name and the hotel's id are passed to keep
     * track of the hotels that are saved. Prepared statements
     * are used to execute the update. Returns true/false
     * depending the whether the update was successful.
     * @param user user's name
     * @param id hotel's id
     * @return saved
     */
    public boolean saveHotel(String user, String id) {
        boolean saved = false;
        try (Connection connection = db.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(ADD_SAVE_HOTEL_SQL)) {
                statement.setInt(1, Integer.parseInt(id));
                statement.setString(2, user);
                saved = (statement.executeUpdate() == 1);
            }
            catch (SQLException e) {
                log.debug(Status.INVALID_REVIEW_SEARCH, e);
            }
        }
        catch (SQLException ex) {
            log.debug(Status.CONNECTION_FAILED, ex);
        }
        return saved;
    }

    /**
     * Function to delete hotels from a user's save hotel list.
     * The user's name and the hotel's id are passed to keep
     * track of the hotels that are saved. Prepared statements
     * are used to execute the update. Returns true/false
     * depending the whether the update was successful.
     * @param user user's name
     * @param id hotel's id
     * @return deleted
     */
    public boolean deleteSaveHotel(String user, String id) {
        boolean deleted = false;
        try (Connection connection = db.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(DELETE_SAVE_HOTEL_SQL)) {
                statement.setInt(1, Integer.parseInt(id));
                statement.setString(2, user);
                deleted = (statement.executeUpdate() == 1);
            }
            catch (SQLException e) {
                log.debug(Status.INVALID_REVIEW_SEARCH, e);
            }
        }
        catch (SQLException ex) {
            log.debug(Status.CONNECTION_FAILED, ex);
        }
        return deleted;
    }

    /**
     * Function to return the list of hotels saved by a
     * specified user. The user's name is passed to keep
     * execute the update. Prepared statements
     * are used to execute the update. Returns a map of
     * the saved hotels by the user.
     * @param userName user's name
     * @return hotels (unmodifiable map of saved hotels)
     */
    public Map<String, Map<String, String>> getSavedHotels(String userName) {
        Map<String, Map<String, String>> hotels = new HashMap<>();
        try (Connection connection = db.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(GET_SAVED_HOTEL_SQL)) {
                statement.setString(1, userName);
                ResultSet results = statement.executeQuery();
                while (results.next()) {
                    Map<String, String> data = new HashMap<>();
                    data.put("id", results.getString("hotel_id"));
                    data.put("name", results.getString("name"));
                    data.put("rating", results.getString("rating"));
                    data.put("link", results.getString("link"));
                    hotels.put(results.getString("hotel_id"), data);
                }
            }
            catch (SQLException e) {
                log.debug(Status.INVALID_REVIEW_SEARCH, e);
            }
        }
        catch (SQLException ex) {
            log.debug(Status.CONNECTION_FAILED, ex);
        }
        return Collections.unmodifiableMap(hotels);
    }

    /**
     * Function to keep track of the last time the user
     * logged into the website. Prepared statements are used
     * to execute the update. Returns true/false depending on
     * whether the update was successful.
     * @param userName user's name
     * @param date date the user last logged in
     * @return addedLog
     */
    public boolean addLastLog(String userName, String date) {
        boolean addedLog = false;
        String lastLog = getLastLog(userName);
        try (Connection connection = db.getConnection()) {
            if(lastLog != null){
                try (PreparedStatement statement = connection.prepareStatement(EDIT_LOG_SQL)) {
                    statement.setString(1, date);
                    statement.setString(2, userName);
                    addedLog = (statement.executeUpdate() == 1);
                }
                catch (SQLException e) {
                    log.debug(Status.INVALID_REVIEW_SEARCH, e);
                }
            }
            else{
                try (PreparedStatement statement = connection.prepareStatement(ADD_LOG_SQL)) {
                    statement.setString(1, userName);
                    statement.setString(2, date);
                    addedLog = (statement.executeUpdate() == 1);
                }
                catch (SQLException e) {
                    log.debug(Status.INVALID_REVIEW_SEARCH, e);
                }
            }
        }
        catch (SQLException ex) {
            log.debug(Status.CONNECTION_FAILED, ex);
        }
        return addedLog;
    }

    /**
     * Function get the last time the user logged in.
     * Prepared statements are used to execute the query.
     * Returns the date the user last logged in.
     * @param userName user's name
     * @return date
     */
    public String getLastLog(String userName) {
        String date = null;
        try (Connection connection = db.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(GET_LOG_SQL)) {
                statement.setString(1, userName);
                ResultSet results = statement.executeQuery();
                if (results.next()) {
                    date = results.getString("date");
                }
            }
            catch (SQLException e) {
                log.debug(Status.INVALID_REVIEW_SEARCH, e);
            }
        }
        catch (SQLException ex) {
            log.debug(Status.CONNECTION_FAILED, ex);
        }
        return date;
    }

    /**
     * Function to get the link history of the specified user.
     * Prepared statements are used in this query. The user's
     * name is used to execute the query. Returns a list of links
     * the user has visited.
     * @param userName user's name
     * @return links (unmodifiable list of links)
     */
    public List<String> getUserLinks(String userName) {
        List<String> links = new ArrayList<>();
        try (Connection connection = db.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(GET_HISTORY_SQL)) {
                statement.setString(1, userName);
                ResultSet results = statement.executeQuery();
                while (results.next()) {
                    links.add(results.getString("link"));
                }
            }
            catch (SQLException e) {
                log.debug(Status.INVALID_REVIEW_SEARCH, e);
            }
        }
        catch (SQLException ex) {
            log.debug(Status.CONNECTION_FAILED, ex);
        }
        return Collections.unmodifiableList(links);
    }

    /**
     * Function to add a link to the specified user's history.
     * Prepared statements are used in this update. The user's
     * name and the link are used to execute the update.
     * Returns true/false depending on whether the update was
     * successful.
     * @param userName user's name
     * @param link link to be added
     * @return addedLink
     */
    public boolean addUserLinks(String userName, String link) {
        boolean addedLink = false;
        try (Connection connection = db.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(ADD_HISTORY_SQL)) {
                statement.setString(1, userName);
                statement.setString(2, link);
                if (statement.executeUpdate() == 1) {
                    addedLink = true;
                }
            }
            catch (SQLException e) {
                log.debug(Status.INVALID_REVIEW_SEARCH, e);
            }
        }
        catch (SQLException ex) {
            log.debug(Status.CONNECTION_FAILED, ex);
        }
        return addedLink;
    }

    /**
     * Function to delete the link history of the specified user.
     * Prepared statements are used in this query. The user's
     * name is used to execute the query. Returns true/false
     * depending on whether the update was successful.
     * @param userName user's name
     * @return deleted
     */
    public boolean deleteUserLinks(String userName) {
        boolean deleted = false;
        try (Connection connection = db.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(DELETE_HISTORY_SQL)) {
                statement.setString(1, userName);
                if (statement.executeUpdate() >= 0) {
                    deleted = true;
                }
            }
            catch (SQLException e) {
                log.debug(Status.INVALID_REVIEW_SEARCH, e);
            }
        }
        catch (SQLException ex) {
            log.debug(Status.CONNECTION_FAILED, ex);
        }
        return deleted;
    }

    /**
     * Function checks whether the specified user has liked
     * the specified review. Prepared statements, the user's name
     * and the review's id are used to execute the query. Returns
     * true/false depending on whether the user liked the review.
     * @param userName user's name
     * @param id review's id
     * @return hasLiked
     */
    public boolean checkLike(String userName, String id) {
        boolean hasLiked = false;
        try (Connection connection = db.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(CHECK_LIKES_SQL)) {
                statement.setString(1, userName);
                statement.setString(2, id);
                ResultSet results = statement.executeQuery();
                if(results.next()) hasLiked = true;
            }
            catch (SQLException e) {
                log.debug(Status.INVALID_REVIEW_SEARCH, e);
            }
        }
        catch (SQLException ex) {
            log.debug(Status.CONNECTION_FAILED, ex);
        }
        return hasLiked;
    }

    /**
     * Function gets the number of likes from a specified review.
     * Prepared statements and the review's id are used to execute
     * the query. Returns the number of likes a review has.
     * @param id review's id
     * @return numLikes
     */
    public int getLikes(String id) {
        int numLikes = 0;
        try (Connection connection = db.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(GET_LIKES_SQL)) {
                statement.setString(1, id);
                ResultSet results = statement.executeQuery();
                if(results.next()) numLikes = Integer.parseInt(results.getString("likes"));
            }
            catch (SQLException e) {
                log.debug(Status.INVALID_REVIEW_SEARCH, e);
            }
        }
        catch (SQLException ex) {
            log.debug(Status.CONNECTION_FAILED, ex);
        }
        return numLikes;
    }

    /**
     * Function adds a like from specified user to the specified
     * review. Prepared statements, the user's name
     * and the review's id are used to execute the query. Returns
     * true/false depending on whether the like was added.
     * @param userName user's name
     * @param id review's id
     * @return liked1 and liked2
     */
    public boolean addLike(String userName, String id) {
        boolean liked1 = false;
        boolean liked2 = false;
        if(!(checkLike(userName, id))){
            int likes = getLikes(id);
            try (Connection connection = db.getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement(ADD_LIKES1_SQL)) {
                    statement.setString(1, userName);
                    statement.setString(2, id);
                    if(statement.executeUpdate() == 1) liked1 = true;
                }
                catch (SQLException e) {
                    log.debug(Status.INVALID_REVIEW_SEARCH, e);
                }

                try (PreparedStatement statement = connection.prepareStatement(ADD_LIKES2_SQL)) {
                    statement.setInt(1, (likes + 1));
                    statement.setString(2, id);
                    if(statement.executeUpdate() == 1) liked2 = true;
                }
                catch (SQLException e) {
                    log.debug(Status.INVALID_REVIEW_SEARCH, e);
                }

            }
            catch (SQLException ex) {
                log.debug(Status.CONNECTION_FAILED, ex);
            }
        }
        return (liked1 && liked2);
    }

    /**
     * Calculates the average rating from all the reviews of
     * the specified hotel. It returns 0 if not reviews are found.
     * @param h_id hotel id
     * @return rating
     */
    private double getRating(int h_id){
        double rating = 0;
        Map<Integer, TreeSet<Review>> reviews = hData.getHotelReviews();

        if(reviews.containsKey(h_id)){
            for(Review r: reviews.get(h_id)){
                rating += r.getRating();
            }
            rating /= reviews.get(h_id).size();
            rating = Math.round(rating * 10)/10.0;
        }
        return rating;
    }

    /**
     * Tests if a user already exists in the database. Requires an active
     * database connection.
     *
     * @param connection - active database connection
     * @param user - username to check
     * @return Status.OK if user does not exist in database
     * @throws SQLException
     */
    private Status duplicateUser(Connection connection, String user) {

        assert connection != null;
        assert user != null;

        Status status = Status.ERROR;

        try (PreparedStatement statement = connection.prepareStatement(USER_SQL)) {
            statement.setString(1, user);

            ResultSet results = statement.executeQuery();
            status = results.next() ? Status.DUPLICATE_USER : Status.OK;
        }
        catch (SQLException e) {
            log.debug(e.getMessage(), e);
            status = Status.SQL_EXCEPTION;
        }

        return status;
    }

    /**
     * Tests if a user already exists in the database.
     *
     * @see #duplicateUser(Connection, String)
     * @param user - username to check
     * @return Status.OK if user does not exist in database
     */
    public Status duplicateUser(String user) {
        Status status = Status.ERROR;

        try (Connection connection = db.getConnection()) {
            status = duplicateUser(connection, user);
        }
        catch (SQLException e) {
            status = Status.CONNECTION_FAILED;
            log.debug(e.getMessage(), e);
        }

        return status;
    }

    /**
     * Returns the hex encoding of a byte array.
     *
     * @param bytes - byte array to encode
     * @param length - desired length of encoding
     * @return hex encoded byte array
     */
    public static String encodeHex(byte[] bytes, int length) {
        BigInteger bigint = new BigInteger(1, bytes);
        String hex = String.format("%0" + length + "X", bigint);

        assert hex.length() == length;
        return hex;
    }

    /**
     * Calculates the hash of a password and salt using SHA-256.
     *
     * @param password - password to hash
     * @param salt - salt associated with user
     * @return hashed password
     */
    public static String getHash(String password, String salt) {
        String salted = salt + password;
        String hashed = salted;

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salted.getBytes());
            hashed = encodeHex(md.digest(), 64);
        }
        catch (Exception ex) {
            log.debug("Unable to properly hash password.", ex);
        }

        return hashed;
    }

    /**
     * Registers a new user, placing the username, password hash, and
     * salt into the database if the username does not already exist.
     *
     * @param newuser - username of new user
     * @param newpass - password of new user
     * @return status ok if registration successful
     */
    private Status registerUser(Connection connection, String newuser, String newpass) {

        Status status = Status.ERROR;

        byte[] saltBytes = new byte[16];
        random.nextBytes(saltBytes);

        String usersalt = encodeHex(saltBytes, 32);
        String passhash = getHash(newpass, usersalt);

        try (PreparedStatement statement = connection.prepareStatement(REGISTER_SQL)) {
            statement.setString(1, newuser);
            statement.setString(2, passhash);
            statement.setString(3, usersalt);
            statement.executeUpdate();

            status = Status.OK;
        }
        catch (SQLException ex) {
            status = Status.SQL_EXCEPTION;
            log.debug(ex.getMessage(), ex);
        }

        return status;
    }

    /**
     * Registers a new user, placing the username, password hash, and
     * salt into the database if the username does not already exist.
     *
     * @param newuser - username of new user
     * @param newpass - password of new user
     * @return status.ok if registration successful
     */
    public Status registerUser(String newuser, String newpass) {
        Status status = Status.ERROR;
        log.debug("Registering " + newuser + ".");

        // make sure we have non-null and non-emtpy values for login
        if (isBlank(newuser) || isBlank(newpass)) {
            status = Status.INVALID_LOGIN;
            log.debug(status);
            return status;
        }

        // try to connect to database and test for duplicate user
        //System.out.println(db);

        try (Connection connection = db.getConnection()) {
            status = duplicateUser(connection, newuser);

            // if okay so far, try to insert new user
            if (status == Status.OK) {
                status = registerUser(connection, newuser, newpass);
            }
        }
        catch (SQLException ex) {
            status = Status.CONNECTION_FAILED;
            log.debug(status, ex);
        }

        return status;
    }

    /**
     * Gets the salt for a specific user.
     *
     * @param connection - active database connection
     * @param user - which user to retrieve salt for
     * @return salt for the specified user or null if user does not exist
     * @throws SQLException if any issues with database connection
     */
    private String getSalt(Connection connection, String user) throws SQLException {
        assert connection != null;
        assert user != null;

        String salt = null;

        try (PreparedStatement statement = connection.prepareStatement(SALT_SQL)) {
            statement.setString(1, user);

            ResultSet results = statement.executeQuery();

            if (results.next()) {
                salt = results.getString("usersalt");
            }
        }

        return salt;
    }

    /**
     * Checks if the provided username and password match what is stored
     * in the database. Requires an active database connection.
     *
     * @param username - username to authenticate
     * @param password - password to authenticate
     * @return status.ok if authentication successful
     * @throws SQLException
     */
    private Status authenticateUser(Connection connection, String username,
                                    String password) throws SQLException {

        Status status = Status.ERROR;

        try (PreparedStatement statement = connection.prepareStatement(AUTH_SQL)) {
            String usersalt = getSalt(connection, username);
            String passhash = getHash(password, usersalt);

            statement.setString(1, username);
            statement.setString(2, passhash);

            ResultSet results = statement.executeQuery();
            status = results.next() ? Status.OK : Status.INVALID_LOGIN;
        }
        catch (SQLException e) {
            log.debug(e.getMessage(), e);
            status = Status.SQL_EXCEPTION;
        }

        return status;
    }

    /**
     * Checks if the provided username and password match what is stored
     * in the database. Must retrieve the salt and hash the password to
     * do the comparison.
     *
     * @param username - username to authenticate
     * @param password - password to authenticate
     * @return status.ok if authentication successful
     */
    public Status authenticateUser(String username, String password) {
        Status status = Status.ERROR;

        log.debug("Authenticating user " + username + ".");

        try (Connection connection = db.getConnection()) {
            status = authenticateUser(connection, username, password);
        }
        catch (SQLException ex) {
            status = Status.CONNECTION_FAILED;
            log.debug(status, ex);
        }

        return status;
    }

    /**
     * Removes a user from the database if the username and password are
     * provided correctly.
     *
     * @param username - username to remove
     * @param password - password of user
     * @return status.OK if removal successful
     */
    private Status removeUser(Connection connection, String username, String password) {
        Status status = Status.ERROR;

        try (PreparedStatement statement = connection.prepareStatement(DELETE_SQL)) {
            statement.setString(1, username);

            int count = statement.executeUpdate();
            status = (count == 1) ? Status.OK : Status.INVALID_USER;
        }
        catch (SQLException ex) {
            status = Status.SQL_EXCEPTION;
            log.debug(status, ex);
        }

        return status;
    }

    /**
     * Removes a user from the database if the username and password are
     * provided correctly.
     *
     * @param username - username to remove
     * @param password - password of user
     * @return Status.OK if removal successful
     */
    public Status removeUser(String username, String password) {

        Status status = Status.ERROR;

        log.debug("Removing user " + username + ".");

        try (Connection connection = db.getConnection()) {
            status = authenticateUser(connection, username, password);

            if(status == Status.OK) {
                status = removeUser(connection, username, password);
            }
        }
        catch (Exception ex) {
            status = Status.CONNECTION_FAILED;
            log.debug(status, ex);
        }

        return status;
    }
}
