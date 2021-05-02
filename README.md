# NewsCheck
An android app made to have news articles fact-checked by the community

It uses NewsAPI (https://newsapi.org/) for news fetching and Google Firebase for handling storage and users(https://firebase.google.com)

You will need to get a key from NewsAPI and setup a Firebase Project in order for the app to work.

To build it, download the project, and open it in Android Studio.
There are two file that you will need to modify :

First, in the app folder, there is a google.json file.
You will need to replace it with the one you can download when you setup your Firebase project.

Then in the app/src/main/res/values folder, there is a keys.xml file.
It consists of a single string ressource, in which you need to place your API key that you received from NewsAPI.

The project should then be all set up, just compile and run it !
