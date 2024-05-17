<img align="left" src="https://github.com/CreedVI/Raylib-J/blob/main/logo/raylib-j_256x256.png" width=256>

# Raylib-J
A handmade version of Raylib for Java.

**Raylib-J is still in development.**<br>

---
  
## About

Raylib-J is a handwritten binding of [Raylib](https://github.com/raysan5/raylib) in Java using 
[LWJGL3](https://www.lwjgl.org/) to provide the OpenGL framework. 

To quote [@raysan5](https://github.com/raysan5):

*NOTE for ADVENTURERS: raylib is a programming library to enjoy videogames programming; no fancy interface, no visual helpers, 
no debug button... just coding in the most pure spartan-programmers way.*


**Raylib-J is currently up-to-date with the 4.2 release of Raylib**<br>
Raylib-J is meant to be a one-for-one rewrite of Raylib with some quality of life changes including, but not limited 
to: JavaDoc comments, `DrawFPS(int posX, int posY, Color theColorYouWant)`, and `CloseWindow()` being handled 
automatically!

---

## Basic Example

Here's all the code needed to create a window and render some text:

```java
package example;

import com.raylib.java.Raylib;
import com.raylib.java.core.Color;

public class example{

    public static void main(String[] args){
        Raylib rlj = new Raylib();
        rlj.core.InitWindow(800, 600, "Raylib-J Example");

        while (!rlj.core.WindowShouldClose()){
            rlj.core.BeginDrawing();
            rlj.core.ClearBackground(Color.WHITE);
            rlj.text.DrawText("Hello, World!", 800 - (rlj.text.MeasureText("Hello, World!", 20)/2), 300, 20, Color.DARKGRAY);
            rlj.core.EndDrawing();
        }
    }

}
```

More examples like the one above can be found at the [Examples repo](https://github.com/CreedVI/Raylib-J-Examples), or you can 
see the wiki for additional documentation and elaboration!
---

## Using Raylib-J
At the current moment Raylib-J is only available as a .jar file. Check the releases page to download the most up-to-date 
version! 

Raylib-J is split between the following modules:
 * Core: Contains all basic Raylib functions.
 * Models: Load and render models and render 3D Geometry
 * Audio: Load, manipulate, and play audio
 * RLGL: Raylib's OpenGL abstraction layer. 
 * Shapes: Need to draw 2D shapes and check collision between them? Look no further.
 * Text: Manipulate and render text using the default Raylib font, or import your own!
 * Textures: All your texture and image needs.
 * Utils: A Raylib-J specific module that contains things like `rLights`, `FileIO`, and `rEasings`

Check the [Raylib Cheatsheet](https://www.raylib.com/cheatsheet/cheatsheet.html) to see each module's available methods!


---

## Honourable Mentions

Other people helped with this process! Whether that was opening an issue or making a library that was used in place of the original libraries to make a lot of this project possible, or at least much easier to develop. 
Special thanks to the following:
  - [delthas](https://github.com/delthas) for their [JavaMP3 API](https://github.com/delthas/JavaMP3)
  - [brammie15](https://github.com/brammie15) for catching an error that made it into production for an embarrassingly long time
  - [elias94](https://github.com/elias94) for helping get this library running on OSX
  - [mateoox600](https://github.com/mateoox600) for catching a number of issues and helping update the [Examples Repo](https://github.com/CreedVI/Raylib-J-Examples)
  - [Irgendwer01](https://github.com/Irgendwer01) for converting the project to use the Gradle build system.
  - [ejenk0](https://github.com/ejenk0) for helping with smoothing out the Gradle build process and assisting with the update process.
  - Everyone in the Raylib discord!'
  - And you for your time checking out this project!
---

## Development Status:

Want to see what's cooking or where you can help push the library towards the next release? Check the [Roadmap!](https://github.com/CreedVI/Raylib-J/blob/main/ROADMAP.md)

Otherwise here's the quick list:

[X] rCore <br>
[X] rShapes <br>
[X] rTextures <br>
[X] rText <br>
[X] rModels <br>
[o] rAudio <br>
[X] RLGL <br>
[X] Raymath <br>
[X] Physac <br>
[X] easings <br>
[X] rLights <br>

<b>Key:</b>
X - complete |
O - nearing completion |
o - in progress |
p - postponed
