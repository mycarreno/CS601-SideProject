package hotelapp;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Traverser {
    /**
     * Method to traverse recursively the directories and files.
     * implements a DFS-like approach to traversing the directories.
     * Each file there is parsed and its data added to the rest.
     * Prints a message if the current directory doesn't exists.
     * @param filePath (String that contains the path to traverse)
     * @return a boolean signalling success or failure of task
     * */
    public boolean traverseDirectories(String filePath, String type, HotelData data){
        Parser p = new Parser();
        boolean noWorries = true;
        File aFile = new File(filePath);
        if(aFile.isFile()) {
            if (type.equals("Reviews")) {
                p.parseReviews(filePath, data);
            } else if (type.equals("Hotels")) {
                p.parseHotels(filePath, data);
            }
        }
        else{
            Path directory = Paths.get(filePath);
            try (DirectoryStream<Path> fileList = Files.newDirectoryStream(directory)) {
                for (Path f : fileList) {
                    if (Files.isRegularFile(f)) {
                        if (type.equals("Reviews")) {
                            p.parseReviews(f.toString(), data);
                        } else if (type.equals("Hotels")) {
                            p.parseHotels(f.toString(), data);
                        }
                    } else if (Files.isDirectory(f)) {
                        if (type.equals("Reviews")) {
                            traverseDirectories(f.toString(), type, data);
                        } else if (type.equals("Hotels")) {
                            traverseDirectories(f.toString(), type, data);
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println("File was not found!");
                noWorries = false;
            }
        }
        return noWorries;
    }
}
