package hotelapp;

import java.util.Scanner;

/** The main class for project 1.
 * The main function should take the following 4 command line arguments:
 * -hotels hotelFile -reviews reviewsDirectory
 * (Note: they can also be provided in the opposite order, first -reviews reviewsDirectory,
 * then -hotels hotelFile)
 *
 * The program should read general information about the hotels from the hotelFile (a JSON file)
 * and recursively traverse reviewsDirectory and read hotel review files.
 * The data should be loaded into data structures that allow efficient search.
 * The user should be able to enter commands via keyboard input to interact with hotel data.
 * See project description posted on Canvas for details.
 *
 * You are expected to add other classes and methods to this project.
 */
public class HotelSearch {
    /**
     * Method to allow the user input commands and interact with
     * the hotel and review data found in HotelData.
     * @param info (HotelData object that holds data)
     */
    public void search(HotelData info){
        String inputInfo = "Please format your input as follows" + System.lineSeparator() +
                "To search for hotel:" + System.lineSeparator() +
                "find <hotelId>" + System.lineSeparator() +
                "To search for hotel's reviews:" + System.lineSeparator() +
                "findReviews <hotelId>" + System.lineSeparator() +
                "To search for word in reviews:" + System.lineSeparator() +
                "findWord <word>" + System.lineSeparator();

        System.out.println(inputInfo);
        Scanner scaner = new Scanner(System.in);
        String input = scaner.nextLine();
        String[] inputs = input.split(" ");
        //user can continue to search through the data if no 'q' character is entered
        while (!(input.equals("q"))){
            if (inputs.length == 2) {
                if(inputs[0].equalsIgnoreCase("find")){
                    int id = Integer.parseInt(inputs[1]);
                    info.findHotel(id);
                }
                else if(inputs[0].equalsIgnoreCase("findReviews")){
                    int id = Integer.parseInt(inputs[1]);
                    info.findReviews(id);
                }
                else if(inputs[0].equalsIgnoreCase("findWord")){
                    info.findWord(inputs[1]);
                }
                else{
                    System.out.println(System.lineSeparator() + "The input format is incorrectly" +
                            System.lineSeparator());
                }

            } else {
                System.out.println(System.lineSeparator() + "The input format is incorrectly" +
                        System.lineSeparator());
            }
            //ask the user for more input after a search
            System.out.println(inputInfo);
            input = scaner.nextLine();
            inputs = input.split(" ");
        }
    }

    /**
     * Main method of the program. Uses various classes and methods
     * to validate command line arguments, traverse/parse files, save/sort
     * hotel/review data and interact with hotel/review data. User is allowed
     * to continue interacting with the data until 'q' is inputted.
     * */
    public static void main(String[] args) {
        //validate command line arguments
        String[] paths = new String[2];
        ArgumentParser parser = new ArgumentParser();
        parser.argParse(args, paths);

        //creates a HotelData object to store and sort data
        HotelData info = new HotelData();
        //traverse and parse files
        Traverser traverse = new Traverser();
        boolean hotelsParsed = traverse.traverseDirectories(paths[0], "Hotels", info);
        boolean reviewsParsed = traverse.traverseDirectories(paths[1], "Reviews", info);

        //if parsing through the files is possible then the user can search through the data
        if(hotelsParsed && reviewsParsed) {
            info.populateWordMap();
            HotelSearch search = new HotelSearch();
            search.search(info);
        }
    }
}
