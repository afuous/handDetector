# handDetector

---

## Update

I tried to get this working again but the fact that I originally made this with a now-old version of OpenCV on Windows with Java made that annoying. It seemed easier to rewrite it in Python. To use the hand detector, run `handDetector.py`, which requires OpenCV and numpy. Custom actions can be put in `actions.py`; the defaults are for playing the pong demo. To detect your hand, the program needs to calibrate at first, so put your hands up and down as instructed then press space.

---

A java program written using OpenCV that will execute a specified script whenever the left or right hand is raised or lowered.

## Compiling and Running
You will need to have OpenCV installed for java. To compile, use `javac -cp opencv-2410.jar HandDetector.java`. Use a different classpath if you have a different version of OpenCV. To run, use `java -cp opencv-2410.jar;. HandDetector.java`. It can be packaged into an executable jar file by creating a `manifest.mf` file with the following contents:
```
Main-Class: HandDetector
Class-Path: opencv-2410.jar
```
and running `jar -cfm handDetector.jar manifest.mf *.class` to create the jar file.

Note: if you are running the program in the console, always close the JFrame instead of pressing Control+C. Otherwise, you will have to forcibly terminate the program since the webcam would not be released.
## script.js
This file must define functions called `leftChange` and `rightChange`. If its execution throws any exceptions, they will be printed to the console. When the left hand is raised, `leftChange(true)` is called, and when it is lowered, `leftChange(false)` is called. When the right hand is raised, `rightChange(true)` is called, and when it is lowered, `rightChange(false)` is called. The Javascript implementation used is Java's Nashorn.
## Pong Demo
The webpage is a simple one player pong game that goes along with the sample `script.js`. The javascript sends keystrokes, and the game reads keystrokes to determine movement, so movement of the paddle in the game can be controlled by raising and lowering your hands.
