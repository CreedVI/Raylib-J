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


**Raylib-J is being built based on the 4.0 release of Raylib**<br>
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

---

## Honourable Mentions

Other libraries were used in place of the original libraries to make a lot of this project possible, or at least much easier to develop. 
Special thanks to the following:
  - [delthas](https://github.com/delthas) for their [JavaMP3 API](https://github.com/delthas/JavaMP3)
---

## Development Status:

[X] rCore <br>
[X] rShapes <br>
[X] rTextures <br>
[X] rText <br>
[o] rModels <br>
[O] rAudio <br>
[X] RLGL <br>
[X] Raymath <br>
[X] Physac <br>
[ ] raygui+ricons <br>
[X] easings <br>
[p] rGestures - Will look into reimplementing later on (08/03/21) <br>
[X] rLights <br>

<b>Key:</b>
X - complete |
O - nearing completion |
o - in progress |
p - postponed
