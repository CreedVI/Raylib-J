# Release: Raylib-J v0.4
## The Action Update

<hr>

* [Physac-J](https://github.com/CreedVI/Physac-J) implemented
* Fixed issues when creating font
  * Namely, when generating an SDF font characters now render with correct kerning
* Raylib 4.0 update

<hr>

# Release: Raylib-J v0.3
## The Enhanced Visual Update
<hr>

* Simplified the `src` directory
  * Reduced import lengths  
  * Removed the `Examples` directory
    * All examples will be hosted in the `Raylib-J Examples` repo.
* Fixed up Textures module
  * Image Generation generates whole images
  * Cellular Generation now works
  * Fixed up `ImageFormat`
  * Fixed `ExportImage`
    * Exporting to PNG is working as expected
* Text module up and running
  * Support loading from `.fnt`, `.ttf`, and `.png` files
  * All module methods populated and ready for use
* Added `rLights` to the `utils` package
* `FileIO` fixes
  * Loading files from an external directory is now working

<hr>

# Release: Raylib-J v0.2-alpha
## The Visual Update
<hr>

* Texture loading and rendering up and running
    - H*ck yes, Brother.
    - Check out the examples! They're pretty neat.
* Fixed an issue that was causing the `MeasureText()` method to return inaccurately.
* Fixed `ToggleFullscreen()` crashing due to a null pointer exception
* Gamepad Support

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
        - I think I got that mess figured out. (I did not.)
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
    - Textures: Basic adaptation. No use outside native necessities.