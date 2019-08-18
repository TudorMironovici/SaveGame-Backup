//=============================================================================================================
// File: SaveGameBackup             Creator: Tudor Mironovici   tcm26@njit.edu
//
// Purpose: Loads the database of save game locations, checks if user has save files there, and backs them up.
//=============================================================================================================

import java.util.*;
import java.io.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class SaveGameBackup
{
   //------------------------Global Variables-------------------------
   private static ArrayList<Game> sgDatabase = new ArrayList<Game>();
   private static ArrayList<Game> gamesFound = new ArrayList<Game>();
   private static ArrayList<String> sgDirs = new ArrayList<String>();
   //-----------------------------------------------------------------
   
   public static void main(String[] args) throws FileNotFoundException, IOException, FileNotFoundException, UnsupportedEncodingException
   {
      loadDatabase();
      sgChecker();
      sgSelect();
      sgCopier();
   }
   
   private static void loadDatabase() throws FileNotFoundException, IOException
   { 
      //------------------------------------Variables--------------------------------------
      ArrayList<String> tempLocations = new ArrayList<String>();
      File sgDatabaseLocation = new File(System.getProperty("user.dir")+"\\database.txt");
      File tempDirFile;
      Scanner databaseScanner = new Scanner(sgDatabaseLocation);
      Scanner inputScanner = new Scanner(System.in);
      String databaseVersion = databaseScanner.nextLine();
      String userDir = "C:\\Users\\"+System.getProperty("user.name");
      String newDir = "";
      String tempLocation = "";
      String tempFileType = "";
      String tempName = "";
      String currentInfoString;
      String[] currentInfoParsed;
      boolean directoryOK = false;
      boolean alreadyAsked = false;
      int border = databaseVersion.length();
      //-----------------------------------------------------------------------------------
      
      //---------------
      // Intro border.
      //---------------
      for (int i = 0; i < border; i++)
      {System.out.print("*");}
      System.out.println("\n"+databaseVersion);
      for (int i = 0; i < border; i++)
      {System.out.print("*");}
      
      //---------------------------
      // Loads all known save file 
      // locations into memory. 
      //---------------------------
      while (databaseScanner.hasNextLine())
      {
          currentInfoString = databaseScanner.nextLine();
         
          //-------------------------------------------
          // Cuts the database info into three strings
          // and adds the info to each Game object.
          //-------------------------------------------
          currentInfoParsed = currentInfoString.split(" ");
          currentInfoParsed[0] = currentInfoParsed[0].replace("`", " ");
          if (currentInfoParsed[0].charAt(0) == '%')
          {
            //-------------------------------------------
            // Gets around different computer usernames.
            //-------------------------------------------
            if (currentInfoParsed[0].charAt(1) == 'U')
            {
               tempLocation = userDir+currentInfoParsed[0].substring(2);
            }
            
            //--------------------------------------------------
            // Gets around save data for games on other drives.
            //--------------------------------------------------
            else if (currentInfoParsed[0].charAt(1) == 'G' || currentInfoParsed[0].charAt(1) == 'S' || currentInfoParsed[0].charAt(1) == 'C')
            {
               //------------------------------------------
               // Don't want to format the directory here
               // because you'd have to check for where it
               // might be - the job of sgChecker().
               //------------------------------------------
               tempLocation = currentInfoParsed[0];
               
               //-------------------------------------------------
               // Don't want to just look through all the drives,
               // that would take too long. Asking for and
               // checking directories is faster.
               //-------------------------------------------------
               if (!alreadyAsked)
               {
                  System.out.print("\nSome games save your data in their directories.\nPlease enter the directory of your Steam folder, or any other large folder(s) with a bunch of games in them (type \"done\" when done): ");
                  while (!directoryOK)
                  {
                     newDir = inputScanner.nextLine();
                     tempDirFile = new File(newDir);
                     
                     if (newDir.toLowerCase().equals("done"))
                     {  
                        directoryOK = true;
                        alreadyAsked = true;
                     }
                     else if (tempDirFile.isDirectory())
                     {
                        sgDirs.add(newDir);
                        System.out.print("If you have any other directories you'd like to add, type them here (or type \"done\" if done): ");
                     }
                     else
                     {
                        System.out.print("That's not a valid directory. Please check your spelling and try again: ");
                     }
                  }
               }
               
               if (currentInfoParsed[0].charAt(1) == 'C')
               {
                  tempLocations = new ArrayList<String>();
                  tempLocations.add(currentInfoParsed[0]);
               }
            }
         }
         //----------------------------------------------------
         // if no % present, location is given in database.txt
         //----------------------------------------------------
         else
         {
            tempLocation = currentInfoParsed[0];
         }
         tempFileType = currentInfoParsed[1].replace("FILE", "");         
         tempName = currentInfoParsed[2].replace("`", " ");
         
         if (currentInfoParsed[0].charAt(1) == 'C')
         {
            sgDatabase.add(new Game(tempLocations, tempFileType, tempName));
         }
         else
         {
            sgDatabase.add(new Game(tempLocation, tempFileType, tempName));
         }
      }
      
      System.out.print("\n\n\n\n");
   }
   
   private static void sgChecker()
   {  
      //--------------------Variables---------------------
      ArrayList<File> steamUsers = new ArrayList<File>(); 
      ArrayList<Game> removeList = new ArrayList<Game>();
      ArrayList<Game> addList = new ArrayList<Game>();
      File[] tempSteamUsers;
      File steamCloud;
      File tempDir;
      File tempRoot;
      String tempLocationString = "";
      String tempLocationString2 = "";
      String seekerText = null;
      String steamGameDir = "";
      String steamCloudDir = "";
      String[] tempLocationParsed;
      String[] tempLocationParsed2;
      int userNumIndex = 0;
      //--------------------------------------------------
      
      System.out.println("Searching for games, this might take a while..."); //The logic for it sure did!
      
      for (Game game: sgDatabase)
      {
         //------------------------------------------------------
         // If the user gave a directory to search for,
         // the program will see if it can find all games
         // with %G/S and change properly set their directories.
         //------------------------------------------------------
         if (!game.hasDirectoriesArray() && sgDirs.size() > 0 && game.getDirectory().charAt(0) == '%')
         {
            tempLocationString = game.getDirectory();
            
            //------------------------------------------------
            // Splits so we can grab the top parent directory
            // and search for that, then adds the rest of the 
            // directory to the absolute path.
            //------------------------------------------------
            tempLocationParsed = tempLocationString.split("\\\\");
            for (String possibleRoot: sgDirs)
            {
               tempRoot = new File(possibleRoot);
               
               //---------------------------------------------------
               // If it's a General game, it will do a full search.
               //---------------------------------------------------
               if (game.getDirectory().charAt(1) == 'G')
               {
                  seekerText = seeker(tempRoot, tempLocationParsed[1]);
               }
               
               //-----------------------------------------------------
               // If it's a Steam game that stores saves to the game
               // directory, it will search for the steamapps folder.
               //-----------------------------------------------------
               else if (game.getDirectory().charAt(1) == 'S')
               {
                  if (steamGameDir.equals(""))
                  {
                     steamGameDir = seeker(tempRoot, "steamapps");
                  }
                  seekerText = steamGameDir;
               }
                              
               if (seekerText != null)
               {
                  game.setDirectory(seekerText+tempLocationString.substring(2));
                  break;
               }
            }
         }
         
         //---------------------------------------------------
         // If the user gave a directory to search for,
         // the program will see if it can find all games
         // with steam Cloud saves, which can have multiple 
         // save directories thanks to multiple user folders.
         //---------------------------------------------------
         else if (game.hasDirectoriesArray() && sgDirs.size() > 0)
         {
            //----------------------------------------------------
            // At this point, every game should only have one
            // directory set to it - the default in the database.
            //----------------------------------------------------
            tempLocationString = game.getDirectoriesArray().get(0);
            tempLocationParsed = tempLocationString.split("\\\\");
            
            //-------------------------------
            // Removes the default directory 
            // to avoid any conflicts.
            //-------------------------------
            game.setDirectoriesArray(new ArrayList<String>());
            
            for (String possibleRoot: sgDirs)
            {
               tempRoot = new File(possibleRoot);
               
               //------------------------------------------------
               // If it's a steam game that stores saves in the
               // Cloud, it will search for the userdata folder.
               //------------------------------------------------
               if (steamCloudDir.equals(""))
               {
                  //------------------------------
                  // Steam Cloud saves are always 
                  // under the "userdata" folder.
                  //------------------------------
                  steamCloudDir = seeker(tempRoot, "userdata");
                  if (steamCloudDir !=null)
                  {
                     steamCloud = new File(steamCloudDir);
                     
                     
                     //----------------------------------
                     // Notes all possible user folders.
                     //----------------------------------
                     tempSteamUsers = steamCloud.listFiles();
                     for (File user: tempSteamUsers)
                     {
                        steamUsers.add(user);
                     }
                  }
                  //---------------------------------------
                  // If the "userdata" folder isn't found,
                  // changes steamCloudDir from null to ""
                  // so if can go through the process in
                  // the next possibleRoot.
                  //---------------------------------------
                  else
                  {
                     steamCloudDir = "";
                  }
               }
               
               for (File user: steamUsers)
               {
                  //----------------------------------------
                  // Used to check if the specific user has
                  // a save game location for current game.
                  //----------------------------------------
                  seekerText = seeker(user, tempLocationParsed[1]);
                  
                  //----------------------------------------------------
                  // If seeker finds a steam user with the save folder,
                  // it adds a Game Object that correlates with the 
                  // steam user ID to gamesFound.
                  //----------------------------------------------------
                  if (seekerText != null)
                  {
                     game.addToDirectoriesArray(user.getAbsolutePath()+tempLocationString.substring(2));
                     tempLocationString2 = user.getAbsolutePath();
                     tempLocationParsed2 = tempLocationString2.split("\\\\");
                     
                     //-----------------------------------
                     // If a Steam User was found to have
                     // the save game folder, the game is
                     // noted to be added to the database 
                     // so that the database will have an
                     // accurate length of games.
                     //-----------------------------------
                     addList.add(new Game(user.getAbsolutePath()+tempLocationString.substring(2), game.getFileType(), game.getName()+" - Steam User "+tempLocationParsed2[tempLocationParsed2.length-1]));
                  }
               }
               //------------------------------------
               // If a Steam User was found to have
               // the save game folder, the game is
               // also noted to be removed so that 
               // the database will have an accurate
               // length of games.
               //------------------------------------
               if (game.getDirectoriesArray().size() > 0)
               {
                  removeList.add(game);
               }
            }
         }
         
         //--------------------------------------------
         // If there are multiple possible directories
         // (currently only a feature of games with 
         // cloud saves), it checks if each directory
         // exists, and then formats it properly for
         // for gamesFound. Names are also changed
         // from their database version to distinguish
         // between the "copies" of the games.
         //--------------------------------------------
         if (game.hasDirectoriesArray())
         {
            for (String userDirectory: game.getDirectoriesArray())
            {
               tempDir = new File(userDirectory);
               if (tempDir.exists())
               {
                  tempLocationString = userDirectory;
                  tempLocationParsed = tempLocationString.split("\\\\");
                  for (int i = 0; i < tempLocationParsed.length; i++)
                  {
                     if (tempLocationParsed[i].equals("userdata"))
                     {
                        userNumIndex = i+1;
                        break;
                     }
                  }
                  gamesFound.add(new Game(userDirectory, game.getFileType(), game.getName()+" - Steam User "+tempLocationParsed[userNumIndex]));
                  System.out.println(gamesFound.size()+"/"+sgDatabase.size()+" games found ("+userDirectory+")...");
               }
            }
         }
         //--------------------------------------------
         // Checks and prints if the save files exist.
         //--------------------------------------------
         else
         {
            tempDir = new File(game.getDirectory());
            if (tempDir.exists())
            {
               gamesFound.add(game);
               System.out.println(gamesFound.size()+"/"+sgDatabase.size()+" games found ("+game.getDirectory()+")...");
            }
         }
      }
      //----------------------------
      // Removes all game "copies".
      //----------------------------
      for (Game removableGame: removeList)
      {
         sgDatabase.remove(removableGame);
      }
      
      //------------------------------------
      // Adds the save games that belong to
      // different steam users. 
      //------------------------------------
      for (Game addableGame: addList)
      {
         sgDatabase.add(addableGame);
      }
      
      System.out.println("\nGames Found\n===========");
      for (Game game: gamesFound)
      {
         System.out.println(game.getName());
      }
   }
   
   private static String seeker(File root, String game)
   {
      //----Variables-----
      File[] files;
      File[] steam;
      String sgDirectory;
      //------------------
   
      //-------------------------------------------------
      // Checks if current directory is the game's root.
      //-------------------------------------------------
      if (root.getName().equals(game))
      {
         return root.getAbsolutePath();
      }
      
      //-----------------------------------------
      // Notes all folders in current directory.
      //-----------------------------------------
      files = root.listFiles();
      
      //---------------------------------------
      // If the current directory isn't empty,
      // the program goes into each folder and
      // repeats the process.
      //---------------------------------------
      if (files != null)
      {
         for (File newDir: files)  
         {
            if (newDir.isDirectory())
            {
               sgDirectory = seeker(newDir, game);

               if (sgDirectory != null) 
               {
                  return sgDirectory;
               }
            }
         }
      }
      return null;
   }
   
   private static void sgSelect()
   {
      //-----------------Variables------------------
      Scanner inputReader = new Scanner(System.in);
      Scanner nameReader = new Scanner(System.in);
      String gameName;
      boolean inputOK = false;
      boolean gamesFoundOK = false;
      boolean skipText = false;
      char userInput;
      //--------------------------------------------
      
      System.out.print("\n\nDo you want to back up all found games? (y/n) ");
      //---------------------------------------------
      // Loops until the input is either "y" or "n".
      //---------------------------------------------
      while (!inputOK)
      {
         //---------------------------------------
         // If the user want to back up all found
         // games, program jumps to sgCopier().
         //---------------------------------------
         userInput = inputReader.nextLine().toLowerCase().charAt(0);
         if (userInput == 'y')
         {
            return;
         }
         
         //-------------------------------------------
         // Starts removing games from the gamesFound
         // Array, and then runs sgCopier().
         //-------------------------------------------
         else if (userInput == 'n')
         {
            System.out.print("Please enter the name of the save game(s) you don't want backed up (type \"done\" when done): ");
            while (!gamesFoundOK)
            {
               //--------------------------------------------
               // gameName is turned to lowercase so that
               // capitilization won't matter when comparing
               // names with sgDatabase and gamesFound.
               //--------------------------------------------
               gameName = nameReader.nextLine().toLowerCase();
               skipText = false;
               
               if (gameName.equals("done"))
               {
                  return;
               }
               
               //-------------------------------------------
               // Checks if the given name is a known game.
               //-------------------------------------------
               for (int i = 0; i < sgDatabase.size(); i++)
               {
                  if (gameName.equals(sgDatabase.get(i).getName().toLowerCase()))
                  {
                     //-----------------------------------------
                     // Checks if the given name is found game.
                     //-----------------------------------------
                     for (int j = 0; j <gamesFound.size(); j++)
                     {
                        if (gameName.equals(gamesFound.get(j).getName().toLowerCase()))
                        {
                           System.out.println(gamesFound.get(j).getName()+" was removed from the backup list.");
                           gamesFound.remove(j);
                           if (gamesFound.size() > 0)
                           {
                              System.out.print("Please enter another game name or type \"done\" if done: ");
                              skipText = true;
                           }
                           
                           //--------------------------------------------
                           // If the gamesFound Array is empty, 
                           // nothing gets backed up and program closes.
                           //--------------------------------------------
                           if (gamesFound.size() == 0)
                           {
                              System.out.print("You have removed all games found for backup.");
                              return;
                           }
                              
                        }
                     }
                     if (!skipText)
                     {
                        System.out.print("Sorry, this game wasn't found on your system. Please refer to list above of found games, check your spelling, and try again: ");
                        skipText = true;
                     }
                  }
               }
               if (!skipText)
               {
                  System.out.print("Sorry, this game is not in the database. Please check your spelling any try again: ");
                  skipText = true;
               }
            }
         }
         
         else
         {
            System.out.print("Please enter either \"y\" or \"n\" ");
         }
      }
   }
   
      private static void sgCopier() throws IOException, FileNotFoundException, UnsupportedEncodingException
   {
      //----------------------------------------------Variables----------------------------------------------
      File sgBackup = new File("C:\\Users\\"+System.getProperty("user.name")+"\\Documents\\SaveGameBackup");
      File sgBackupFolder;
      File sgTempDir;
      PrintWriter txtDirFile;
      //-----------------------------------------------------------------------------------------------------
      
      //-----------------------------------
      // Makes the backup folder where all
      // save game folders will be in.
      //-----------------------------------
      sgBackup.mkdir();
      for (Game game: gamesFound)
      {
         //-------------------------
         // Makes the backup folder
         // for the specific game.
         //-------------------------
         sgBackupFolder = new File("C:\\Users\\"+System.getProperty("user.name")+"\\Documents\\SaveGameBackup\\"+game.getName());
         sgBackupFolder.mkdir();
         
         sgTempDir = new File(game.getDirectory());
         copyDirectory(sgTempDir, sgBackupFolder, game.getFileType(), game.getName());
         
         //------------------------------------------
         // Creates a .txt file in the backup folder
         // noting where the file was copied from so
         // the user can put their save games back.
         //------------------------------------------
         txtDirFile = new PrintWriter("C:\\Users\\"+System.getProperty("user.name")+"\\Documents\\SaveGameBackup\\"+game.getName()+"\\Original Savegame Location.txt", "UTF-8");
         txtDirFile.println(game.getDirectory());
         txtDirFile.println("If you want to use this save game again, just copy the "+((game.getFileType().equals("folder")) ? "folder" : "file")+"(s) in this folder to the directory above.");
         txtDirFile.close();
         
         System.out.println("Save "+((game.getFileType().equals("folder")) ? "folder" : "file")+"(s) for "+game.getName()+" can be found in C:\\Users\\"+System.getProperty("user.name")+"\\Documents\\SaveGameBackup\\"+game.getName());
      }
   }
   
   private static void copyDirectory(File sourceDir, File destinationDir, String fileType, String gameName) throws IOException
   {
      //------Variables------
      String[] files;
      File sourceFile;
      File destinationFile;
      int folderCounter = 0;
      //---------------------
      if (sourceDir.isDirectory())
      {
         if (!destinationDir.exists())
         {
            destinationDir.mkdir();
         }
         
         files = sourceDir.list();
         for (String file: files)
         {
            try
            {
               //-------------------------------------------------------------
               // If the file type isn't "folder" AND doesn't match fileType, 
               // skip reading the file so it doesn't get copied over.
               //-------------------------------------------------------------
               if (!fileType.equals("folder") && !fileType.equals(file.substring(file.substring(0).length()-fileType.length())))
               {
                  continue;
               }
               if (fileType.equals("") && getFileExtension(new File(file)) == null)
               {
                  System.out.print("WORKS");
               }
            }
            catch (StringIndexOutOfBoundsException e)
            {
               continue;
            }
            //---------------------------------------------
            // Looks through all directories in the source
            // and copies everything to the destination.
            //---------------------------------------------
            sourceFile = new File(sourceDir, file);
            destinationFile = new File(destinationDir, file);
            copyDirectory(sourceFile, destinationFile, fileType, gameName);
         }
      }
      
      //-------------------
      // Does the copying.
      //-------------------
      else
      {
         Files.copy(sourceDir.toPath(), destinationDir.toPath(), StandardCopyOption.REPLACE_EXISTING);
      }
   }
   private static String getFileExtension(File file) {
        String fileName = file.getName();
        if(fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0)
        return fileName.substring(fileName.lastIndexOf(".")+1);
        else return "";
    }
}