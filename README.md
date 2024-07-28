# j-Lawyer Sync tool
***

## Description
# jL-Sync-Tool
A synchronisation tool for j-Lawyer that syncs case files to local hard drive.
You need a running j-Lawyer.org installation (server) to sync files. 
Also go to www.j-lawyer.org 




## Installation:
-

## Start script via terminal:
- Open Terminal in Script folder
- MacOS: 
- Linux/Windows: java -jar FILENAME.jar


## How to use: 
- enter Login Data; can be saved for your convenience
(be careful with the password, password is saved in plain text in local .json file)
- "Aktenbestand laden / aktualiseren" and "Adressenbestand laden / aktualisieren" (will be saved for next run of the script)
- Choose Sync-Folder (will be saved for next time).

Cases  will be saved locally in json file to make the tool aware of your case data. It is not necesseray to load the data every time, unless new cases are added to the serverside database. Cases that the tool does not know wont be synced. 

Run
    "Synchronisation starten"


## Features

What the app does:
- new Folder "Akten" is created
- Files from case are downloaded to local folder in "Akten" 
- file types can be excluded from sync process
- app can be used from everywhere, just use a VPN tunnel. The login data stay the same
