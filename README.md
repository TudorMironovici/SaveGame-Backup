# What I learned
- How to deal with manipulating files (copying to another directory, creating, changing).
- How to search for directories recursively.
- How to copy all files, or certain file types, in a directory recursively.
- How to optimize searching (such as searching for commonly used directories if their location isn't standard on every system,
and checking if the specific directory you're looking for exists with <File>.exists()). <br />




# SaveGame Backup
`Background:` <br />
This program's creation was out of a bit of frustration for the lack of software like it. A few years back, Razer (a popular gaming-
oriented hardware company) had a save game backup feature in their Razer Cortex software. However, before installing the program I 
did a quick google search to see if I was remembering this feature correctly. Sadly, the feature was removed around 2017. So I looked
for an alternative piece of software online, and the only thing I found was a piece of software called GameSave Manager (the name of
this software definitely takes inspiration from said software) and GameSave Manager seemed to be a promising alternative to Razer 
Cortex. However, after installing it, I read the readme. The program I thought would be relatively simple seemed to have a lot of
issues - most stemming from the Windows User Account Control safety feature. So, I decided to run it with administrative privileges
to avoid any UAC-related errors. However, GameSave Manager would always crash when getting to my Minecraft save files. I don't blame
the creator of GameSave Manager for this too much, as I thoroughly enjoyed my time in Minecraft, with my save game files reaching
several gigabytes. But that's beside the point - I saw an issue that could be a fundamental flaw (thanks to the UAC-related issues
seeming have been with the software for a while) so I decided to try and make a program like it in Java. A few weeks - and a LOT of 
Googling - later, I have a "tech demo" that I was able to use successfully on my computer. Most of the work needed would mainly be in
the database. I put a handful of games in the database, as finding save game locations is surprisingly time consuming. To overcome
this shortcoming, the database is a simple .txt file that can be easily edited so that anyone can add their own games with formatting
documented down below. <br />

`About:` <br />
SaveGame Backup 1.0 only supports a limited amount of Windows operating systems (Windows 7, Windows 8, Windows 8.1, Windows 10).
SaveGame Backup is a program that backs up all your save games to a __SaveGameBackup__ folder in your Documents. Unlike GameSave 
Manager, this program requires no Windows User Account Control privileges, and it can handle large save games. Each game has a folder
inside the __SaveGameBackup__ folder, with instructions on how to put your save game backups back to use. SaveGame Backup comes with a
__database.txt__, which has formatting that is listed below: <br />
- %C = steam Cloud save location. (__...\Steam\userdata__)
- %S = Steam game location. (__...\Steam\steamapps__)
- %U = current User folder. (__C:\Users\\...__)
- %G = General game that has an unknown installation location.
- \` = used as a space character. (__C:\Program\`Files\`(x86)\\...__) <br />
- _folder_ = used as a key word to grab all files in a directory.

%C was created such that once the Steam folder is found, it can locate the __userdata__ folder, and then loads the directory into 
memory. Once the __userdata__ folder is loaded into memory, the program looks for all the user folders inside, and loads those into
memory as an ArrayList. The program will check each user's folder for games marked with %C locations. <br />
%S was created such that once the Steam folder is found, it can locate the __steamapps__ folder, and then loads the directory into
memory. After storing the __steamapps__ directory into memory, it adds the rest of the directory String from the database onto the 
Game object's Directory attribute. %S must always have __\common\\...__ after it in the database if the save game is stored where the
Steam game is stored, as %S only finds the __steamapps__ folder. This was done in case some games have their save game data in some
other folder under __steamapps__, and because looking for a folder named __common__ rather than __steamapps__ could lead to false 
positives. <br />
%U was created due to all user folders having user-specific names in Windows. The program pulls the current user's username and adds
__C:\Users\<current username>__ to the front of every %U game location. <br />
%G was created due to some games allowing the user to store the game in a custom directory, and the save games being in the game's
directory. The program looks for whatever the first folder in the directory given (after %G in __database.txt__) and adds the proper 
directory to the front of the database information. <br />
\` was created as a stand-in for the space character for the directories and names stored in __database.txt__. The program replaces the
\` characters to space characters after the database information has been split into directory, file type, and game name. This was done
so that visually, the directory, file type, and game name all look like their own seperate objects, split up by spaces. <br />
_folder_ was created to let the program know that all the files in a folder are important for backup. <br />

`Adding Games to the Database`:
Before adding, please read the above section to understand the formatting of the database. Simply add a new line to __database.txt__ 
with a directory (feel free to use % keys), a file type, and a game name - each seperated by a singular space. <br />
Example: %G\\.minecraft\saves folder Minecraft <br />

`Things for the Future:` <br />
AS A FOREWARNING: The Fall semester is about to start, and I might not have as much time to work on this program. This 1.0 release is
meant to be just a beginning to something, as this would never come out if I keep on working on all the brainstormed ideas I get. <br />
- One thing that definitely has to improve would be the database. It has a very limited amount of games as of now. <br />
- Adding some form of GUI elements, as it's something I've never done, and this isn't the era of MS DOS anymore.
- Adding a timed backup feature, where every day/week/month/etc. the program auto-runs and creates new backups of all the games. To avoid
writing over previous save games, each folder could have a time-stamp on it to ensure each folder is unique, and has a meaningful name.
I would need to figure out how to run this in the background with next to no impact on system performance, and how to make searching for
games efficient, as the program would've already found the directories for most games on the system, but it still needs to search as new
games could have been installed since the last backup.
