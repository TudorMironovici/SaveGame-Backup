//=============================================================================
// File: Game              Creator: Tudor Mironovici     tcm26@njit.edu
//
// Purpose: the Game object holds info that helps identify the save game file.
//=============================================================================
import java.util.ArrayList; 

public class Game
{
   //--------------Attributes---------------
   private ArrayList<String> directories;
   private String directory;
   private String fileType;
   private String name;
   private boolean directoriesArrayPresent;
   //---------------------------------------
   
   public Game(String directory, String fileType, String name)
   {
      this.directory = directory;
      this.fileType = fileType;
      this.name = name;
      this.directoriesArrayPresent = false;
   }
   
   public Game(ArrayList<String> directories, String fileType, String name)
   {
      this.directories = directories;
      this.fileType = fileType;
      this.name = name;
      this.directoriesArrayPresent = true;
   }
   
   public String getDirectory()
   {
      return directory;
   } 
   
   public void setDirectory(String newDirectory)
   {
      directory = newDirectory;
   }
   
   public String getFileType()
   {
      return fileType;
   }
   
   public void setFileType(String newFileType)
   {
      fileType = newFileType;
   }
   
   public String getName()
   {
      return name;
   }
   
   public String toString()
   {
      return (name+"\t\tdirectory: "+directory+"  |  file type: "+fileType);
   }
   
   public boolean hasDirectoriesArray()
   {
      return directoriesArrayPresent;
   }
   
   public void addToDirectoriesArray(String directory)
   {
      directories.add(directory);
   }
   
   public ArrayList<String> getDirectoriesArray()
   {
      return directories;
   }
   
   public void setDirectoriesArray(ArrayList<String> newDirectories)
   {
      directories = newDirectories; 
   }
   
   public void setDirectoriesArray(int index, String directory)
   {
      directories.set(index, directory);
   }
}