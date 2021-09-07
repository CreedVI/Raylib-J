# Commit: tbd

<hr>

* Texture loading and rendering up and running
    - H*ck yes, Brother.
    - Check out the examples! They're pretty neat.
* Fixed an issue that was causing the `MeasureText()` method to return inaccurately.
* Fixed `ToggleFullscreen()` crashing due to a null pointer exception


<hr>


# Commit: b37cde0

<hr>

* fixed issue that was causing the default font to generate incorrectly
    - Pesky binary positions

<hr>

# Commit: 188b803

<hr>

* Updated to Raylib 3.7
* New Features:
    - Working 2D Camera system.
* Other Changes:
    - Switched from Enums to static integers defined in an internal class.
        - I felt this increases code intelligibility. Feedback on this change is welcome.
    - Utils.Files is now Utils.FileIO
        - I think I got that mess figured out.
<hr>

# Release: Raylib-J 0.1-alpha
<hr>

* First available alpha build
* Based on Raylib 3.5
* Modules provided:
    - Core: Near complete adaptation. 
    - RLGL: Near complete adaptation.
    - RayMath: Complete adaptation.
    - Shapes: Complete adaptation.
    - Text: Basic adaptation. Able to draw text using the default font.
    - Textures: Basic adaptation. No use outside of native necessities.