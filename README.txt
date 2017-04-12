COMP2601:   Final Project
Due Date:   April 11, 2017 (11:55pm)

Authors:    Alexei Tipenko (100995947)
            Avery Vine (100999500)

Program:  An android application that uses Google Maps and camera functionality. This application is a game in which two or more players connect to a server and Google Maps. The premise of the game is that each player is trying to find the other player and take a picture of them before the other does. Each player will initially be given some points which they can spend toward game actions that can help track down another player, or aid in preventing themselves from being tracked. Once one of the players is successful in taking the other’s picture, they will send it to them winning the game. The game actions will be split into two categories: offensive and defensive. Offensive actions may include things like briefly showing the opponent’s location, showing the opponent’s previously path travelled or setting up a “uav”-like region that tracks the opponent’s location while they are in that area. Defensive actions may involve disrupting other players’ GPS tracking of the player’s GPS signal, creating a fake GPS signal to fool the enemy, or block the opponent’s camera functionality for a short period of time. 

Server Operation Instructions:
1) Open a terminal window
2) Navigate to the /finalproject/app/src/main/java
3) Enter the following to compile:
	javac edu/carleton/COMP2601/finalproject/Server.java
4) Enter the following to run:
	java edu.carleton.COMP2601.finalproject.Server

Emulator Operation Instructions:
1) Open the Android Studio project
2) Press the Run button, and select an emulator
3) Once the emulator has the app loaded onto it, press the Stop button
4) Reopen the app manually on the emulator
5) Repeat steps 2-4 for as many emulators as you’d like