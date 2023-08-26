# Release: Raylib-J v0.5
## The Next Dimension

<hr>

* Support for rModels is complete!
  * Load all your favourite  `.obj`, `.iqm`, `.gltf`, and `.glb` models!
  * Animations for `.iqm` models!
  * Mesh Generation!
* Improved FileIO!
  * Depending on JRE/JDK previous implementation would not stream all data before returning. This has been fixed.
* Overhaul on GLFW wrapped methods!
  * In previous versions methods that rely on GLFW to report system info, manage window, etc. did not function properly. All these should be fixed.
    * Setting a custom icon for your app works now, too. 
* Various fixes to improve the developer experience.

<hr>

This update has been a long time in the works. Life's done its best to keep me from getting this done - I've started a new job with far too many hours in a work day, moved halfway across the world, and all the other standard life drama.  

I'm gonna take a weekend to relax before getting to work on the next stop on the [Roadmap](https://github.com/CreedVI/Raylib-J/blob/main/ROADMAP.md)!

See y'all in the Discord, or in the next release!

-Creed

<hr>


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