package hotelapp;

public class ArgumentParser {
    /**
     * Method to parse command line arguments. Prints message
     * if command line arguments are invalid.
     * @param args (Command line arguments)
     * @param paths (String array holds paths to data)
     */
    public void argParse(String[] args, String[] paths){
        String wrongFormat = System.lineSeparator() +
                "Please format arguments as follows:" + System.lineSeparator() +
                "-reviews <reviews file path> -hotels <hotels file path>" + System.lineSeparator() +
                "Or as follows:" + System.lineSeparator() +
                "-hotels <hotels file path> -reviews <reviews file path>" + System.lineSeparator();
        //Checks to see if the command line args are correct
        if(args.length == 4){
            if("-hotels".equalsIgnoreCase(args[0])){    //stores values
                paths[0] = args[1];
            }
            else if("-reviews".equalsIgnoreCase(args[0])){
                paths[1] = args[1];
            }
            else {  //prints message if incorrect format
                System.out.println(wrongFormat);
                return;
            }

            if("-hotels".equalsIgnoreCase(args[2])){    //stores values
                paths[0] = args[3];
            }
            else if("-reviews".equalsIgnoreCase(args[2])){
                paths[1] = args[3];
            }
            else{   //prints message if incorrect format
                System.out.println(wrongFormat);
            }
        }
        else{   //prints message if incorrect format
            System.out.println(wrongFormat);
        }
    }
}
