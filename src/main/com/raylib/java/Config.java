package com.raylib.java;

/**
 * Raylib configuration <br/>
 * This module defines all the configuration flags for the different raylib modules
 */
public class Config{

    public static final String RAYLIB_VERSION = "4.5";

    //------------------------------------------------------------------------------------
    // Module selection - Some modules could be avoided
    // Mandatory modules: rcore, rlgl, utils
    //------------------------------------------------------------------------------------
    /**
     * Support raylib module rShapes
     */
    public static boolean SUPPORT_MODULE_RSHAPES   = true;
    /**
     * Support raylib module rTextures
     */
    public static boolean SUPPORT_MODULE_RTEXTURES = true;
    /**
     * Support raylib module rText <br/>
     * WARNING: rText requires SUPPORT_MODULE_RTEXTURES to load sprite font textures
     */
    public static boolean SUPPORT_MODULE_RTEXT     = true;
    /**
     * Support raylib module rModels
     */
    public static boolean SUPPORT_MODULE_RMODELS   = true;
    /**
     * Support raylib module rAudio
     */
    public static boolean SUPPORT_MODULE_RAUDIO    = true;

    //------------------------------------------------------------------------------------
    // Module: core - Configuration Flags
    //------------------------------------------------------------------------------------
    /**
     * Operating system application is running on
     */
    private static final String OS = System.getProperty("os.name").toLowerCase();
    /**
     * Flag for Windows OS
     */
    public static boolean __WINDOWS__ = (OS.contains("win"));
    /**
     * Flag for OSX
     */
    public static boolean __APPLE__ = (OS.contains("mac"));
    /**
     * Flag for GNU/Linux OS
     */
    public static boolean __LINUX__ = (OS.contains("nix") || OS.contains("nux") || OS.contains("aix"));
    /**
     * Flag if application is being run on a desktop platform
     */
    public static boolean PLATFORM_DESKTOP = (__WINDOWS__ || __APPLE__ || __LINUX__);
    /**
     * Flag for Wayland display server (GNU/Linux)
     */
    public static boolean _GLFW_WAYLAND = false;
    /**
     * Flag for X11 display server (GNU/Linux)
     */
    public static boolean _GLFW_X11 = false;

    /**
     * rCamera module is included (core.camera.java) and multiple predefined cameras are available: free, 1st/3rd
     * person, orbital
     */
    static boolean SUPPORT_CAMERA_SYSTEM = true;
    /**
     * Gestures module is included (core.input.gestures.java) to support gestures detection: tap, hold, swipe, drag
     */
    public static boolean SUPPORT_GESTURES_SYSTEM = false;
    /**
     * Mouse gestures are directly mapped like touches and processed by gestures system
     */
    static boolean SUPPORT_MOUSE_GESTURES = false;
    /**
     * Reconfigure standard input to receive key inputs, works with SSH connection.
     */
    static boolean SUPPORT_SSH_KEYBOARD_RPI = false;
    /**
     * Draw a mouse pointer on screen
     */
    public static boolean SUPPORT_MOUSE_CURSOR_POINT = true;
    /**
     * Use busy wait loop for timing sync, if not defined, a high-resolution timer is setup and used
     */
    public static boolean SUPPORT_BUSY_WAIT_LOOP = false;
    /**
     * Use a half-busy wait loop, in this case frame sleeps for some time and runs a busy-wait-loop at the end
     */
    public static boolean SUPPORT_PARTIALBUSY_WAIT_LOOP = true;
    /**
     * Wait for events passively (sleeping while no events) instead of polling them actively every frame
     */
    public static boolean SUPPORT_EVENTS_WAITING = false;
    /**
     * Allow automatic screen capture of current screen pressing F12, defined in KeyCallback()
     */
    public static boolean SUPPORT_SCREEN_CAPTURE = false;
    /**
     * Support CompressData() and DecompressData() functions
     */
    static boolean SUPPORT_COMPRESSION_API = true;
    /**
     * Support saving binary data automatically to a generated storage.data file. This file is managed internally.
     */
    public static boolean SUPPORT_DATA_STORAGE = true;
    /**
     * Support standard file IO functions in utils.File.java
     */
    public static boolean SUPPORT_STANDARD_FILEIO = true;

    /**
     * Support automatic generated events, loading and recording of those events when required
     */
    public static boolean SUPPORT_AUTOMATION_EVENTS = false;

    /**
     * Support custom frame control, only for advance users. <br/>
     * By default EndDrawing() does this job: draws everything + SwapScreenBuffer() + manage frame timming + PollInputEvents() <br/>
     * Enabling this flag allows manual control of the frame processes, use at your own risk
     */
    public static boolean SUPPORT_CUSTOM_FRAME_CONTROL = false;


    // core: Configuration values
    //------------------------------------------------------------------------------------
    /**
     * Maximum length for filepaths (Linux PATH_MAX default value)
     */
    public final static int LINUX_MAX_FILEPATH_LENGTH = 4096;
    /**
     * Maximum length supported for file paths
     */
    public final static int MAX_FILEPATH_LENGTH = 512;
    /**
     * Maximum number of keyboard keys supported
     */
    public final static int MAX_KEYBOARD_KEYS = 512;
    /**
     * Maximum number of mouse buttons supported
     */
    public final static int MAX_MOUSE_BUTTONS = 8;
    /**
     * Max number of gamepads supported
     */
    public final static int MAX_GAMEPADS = 16;
    /**
     * Max number of axis supported (per gamepad)
     */
    public final static int MAX_GAMEPAD_AXIS = 8;
    /**
     * Max number of buttons supported (per gamepad)
     */
    public final static int MAX_GAMEPAD_BUTTONS = 64;
    /**
     * Maximum number of touch points supported
     */
    public final static int MAX_TOUCH_POINTS = 10;
    /**
     * Max number of keys in the key input queue
     */
    public final static int MAX_KEY_PRESSED_QUEUE = 16;
    /**
     * Max number of characters in the char input queue
     */
    public final static int MAX_CHAR_PRESSED_QUEUE = 16;
    /**
     * Automatic storage filename
     */
    public final static String STORAGE_DATA_FILE = "storage.data";
    /**
     * Max size allocated for decompression in MB
     */
    public final static int MAX_DECOMPRESSION_SIZE = 64;


    //------------------------------------------------------------------------------------
    // Module: rlgl - Configuration Flags
    //------------------------------------------------------------------------------------
    /**
     * Support VR simulation functionality (stereo rendering)
     */
    public static boolean SUPPORT_VR_SIMULATOR = false;

    // rlgl: Configuration values
    //------------------------------------------------------------------------------------
    /**
     *
     */
    public final static boolean SUPPORT_GL_DETAILS_INFO = false;
    /**
     *
     */
    public final static boolean FBO_READ_TEXTURE_AS_RGBA = false;

    /**
     * Default internal render batch limits
     */
    public final static int RL_DEFAULT_BATCH_BUFFER_ELEMENTS = 8192;
    /**
     * Default internal render batch limits
     */
    final static int ES2_DEFAULT_BATCH_BUFFER_ELEMENTS = 2048;


    /**
     * Default number of batch buffers (multi-buffering)
     */
    private final static int RL_DEFAULT_BATCH_BUFFERS = 1;
    /**
     * Default number of batch draw calls (by state changes: mode, texture)
     */
    public final static int RL_DEFAULT_BATCH_DRAWCALLS = 256;

    /**
     * Maximum number of textures units that can be activated on batch drawing (SetShaderValueTexture())
     */
    public final static int RL_DEFAULT_BATCH_MAX_TEXTURE_UNITS = 4;
    /**
     * Maximum size of internal Matrix stack
     */
    public final static int RL_MAX_MATRIX_STACK_SIZE = 32;
    /**
     * Maximum number of shader locations supported
     */
    public final static int RL_MAX_SHADER_LOCATIONS = 32;

    /**
     * Default projection matrix near cull distance
     */
    public final static float RL_CULL_DISTANCE_NEAR = 0.01f;
    /**
     * Default projection matrix far cull distance
     */
    public final static float RL_CULL_DISTANCE_FAR = 1000.0f;

    /**
     * Enable/Disable OpenGL debugging context
     */
    public static boolean RLGL_ENABLE_OPENGL_DEBUG_CONTEXT = false;

    //------------------------------------------------------------------------------------
    // Module: rShapes - Configuration Flags
    //------------------------------------------------------------------------------------

    /**
     * Draw rectangle rShapes using font texture white character instead of default white texture
     * Allows drawing rectangles and text with a single draw call, very useful for GUI systems!
     */
    public static boolean SUPPORT_FONT_TEXTURE = true;

    /**
     * Use QUADS instead of TRIANGLES for drawing when possible
     * Some lines-based rShapes could still use lines
     */
    public static boolean SUPPORT_QUADS_DRAW_MODE = true;

    /**
     * Use Miters for spline rendering
     */
    public static boolean SUPPORT_SPLINE_MITERS = true;


    //------------------------------------------------------------------------------------
    // Module: textures - Configuration Flags
    //------------------------------------------------------------------------------------
    // Select desired file formats to be supported for image data loading
    /**
     * Support loading PNG image files
     */
    public static boolean SUPPORT_FILEFORMAT_PNG = true;
    /**
     * Support loading BMP image files
     */
    public static boolean SUPPORT_FILEFORMAT_BMP = true;
    /**
     * Support loading TGA image files
     */
    public static boolean SUPPORT_FILEFORMAT_TGA = false;
    /**
     * Support loading JPG/JPEG image files
     */
    public static boolean SUPPORT_FILEFORMAT_JPG = false;
    /**
     * Support loading SIG image files
     */
    public static boolean SUPPORT_FILEFORMAT_GIF = true;
    /**
     * Support loading QOI image files
     */
    public static boolean SUPPORT_FILEFORMAT_QOI = true;
    /**
     * Support loading PSD image files
     */
    public static boolean SUPPORT_FILEFORMAT_PSD = false;
    /**
     * Support loading DDS image files
     */
    public static boolean SUPPORT_FILEFORMAT_DDS = true;
    /**
     * Support loading HDR image files
     */
    public static boolean SUPPORT_FILEFORMAT_HDR = false;
    /**
     * Support loading KTX image files
     */
    public static boolean SUPPORT_FILEFORMAT_KTX  = false;
    /**
     * Support loading ASTC image files
     */
    public static boolean SUPPORT_FILEFORMAT_ASTC = false;
    /**
     * Support loading PKM image files
     */
    public static boolean SUPPORT_FILEFORMAT_PKM  = false;
    /**
     * Support loading PVR image files
     */
    public static boolean SUPPORT_FILEFORMAT_PVR  = false;

    /**
     * Support image export functionality (.png, .bmp, .tga, .jpg)
     */
    public static boolean SUPPORT_IMAGE_EXPORT = true;
    /**
     * Support procedural image generation functionality (gradient, spot, perlin-noise, cellular)
     */
    public static boolean SUPPORT_IMAGE_GENERATION = true;
    /**
     * Support multiple image editing functions to scale, adjust colors, flip, draw on images, crop...
     * If not defined, still some functions are supported: ImageFormat(), ImageCrop(), ImageToPOT()
     */
    public static boolean SUPPORT_IMAGE_MANIPULATION = true;


    //------------------------------------------------------------------------------------
    // Module: text - Configuration Flags
    //------------------------------------------------------------------------------------
    /**
     * Default font is loaded on window initialization to be available for the user to render simple text
     * NOTE: If enabled, uses external module functions to load default raylib font
     */
    public static boolean SUPPORT_DEFAULT_FONT = true;
    /**
     * Selected .fnt to be supported for loading
     */
    public static boolean SUPPORT_FILEFORMAT_FNT = true;
    /**
     * Selected .ttf to be supported for loading
     */
    public static boolean SUPPORT_FILEFORMAT_TTF = true;
    /**
     * Selected .bdf to be supported for loading
     */
    public static boolean SUPPORT_FILEFORMAT_BDF = true;

    /**
     * Support text management functions
     * If not defined, still some functions are supported: TextLength(), TextFormat()
     */
    public static boolean SUPPORT_TEXT_MANIPULATION = true;

    // text: Configuration values
    //------------------------------------------------------------------------------------
    /**
     * Size of internal static buffers used on some functions: TextFormat(), TextSubtext(), TextToUpper(), TextToLower(), TextToPascal(), TextSplit()
     */
    public final static int MAX_TEXT_BUFFER_LENGTH = 1024;
    /**
     * Maximum number of unicode codepoints: GetCodepoints()
     */
    public final static int MAX_TEXT_UNICODE_CHARS = 512;
    /**
     * Maximum number of substrings to split: TextSplit()
     */
    public final static int MAX_TEXTSPLIT_COUNT = 128;


    //------------------------------------------------------------------------------------
    // Module: models - Configuration Flags
    //------------------------------------------------------------------------------------

    /**
     * Select .obj to be supported for loading
     */
    public static boolean SUPPORT_FILEFORMAT_OBJ = true;

    /**
     * Select .mtl to be supported for loading
     */
    public static boolean SUPPORT_FILEFORMAT_MTL = true;

    /**
     * Select .iqm to be supported for loading
     */
    public static boolean SUPPORT_FILEFORMAT_IQM = true;

    /**
     * Select .gltf to be supported for loading
     */
    public static boolean SUPPORT_FILEFORMAT_M3D = true;

    /**
     * Select .gltf to be supported for loading
     */
    public static boolean SUPPORT_FILEFORMAT_GLTF = true;

    /**
     * Select .vox to be supported for loading
     */
    public static boolean SUPPORT_FILEFORMAT_VOX = true;

    /**
     * Support procedural mesh generation functions, uses par_shapes library <br/>
     * NOTE: Some generated meshes DO NOT include generated texture coordinates
     */
    public static boolean SUPPORT_MESH_GENERATION = true;
    /**
     * Support mesh generation for plane meshes
     */
    public static boolean SUPPORT_CUSTOM_MESH_GEN_PLANE = true;
    /**
     * Support mesh generation for cube meshes
     */
    public static boolean SUPPORT_CUSTOM_MESH_GEN_CUBE = true;


    //------------------------------------------------------------------------------------
    // Module: audio - Configuration Flags
    //------------------------------------------------------------------------------------
    /**
     * Support .wav to be supported for loading
     */
    public static boolean SUPPORT_FILEFORMAT_WAV = true;
    /**
     * Support .ogg to be supported for loading
     */
    public static boolean SUPPORT_FILEFORMAT_OGG = true;
    /**
     * Support .xm to be supported for loading
     */
    public static boolean SUPPORT_FILEFORMAT_XM = true;
    /**
     * Support .mod to be supported for loading
     */
    public static boolean SUPPORT_FILEFORMAT_MOD = true;
    /**
     * Support .mp3 to be supported for loading
     */
    public static boolean SUPPORT_FILEFORMAT_MP3 = true;
    /**
     * Support .flac to be supported for loading
     */
    public static boolean SUPPORT_FILEFORMAT_FLAC = true;

    // audio: Configuration values
    //------------------------------------------------------------------------------------
    /**
     * Device output format (miniaudio: float-32bit)
     */
    public final static String AUDIO_DEVICE_FORMAT = "ma_format_f32";
    /**
     * Device output channels: stereo
     */
    public final static int AUDIO_DEVICE_CHANNELS = 2;
    /**
     * Device output sample rate
     */
    public final static int AUDIO_DEVICE_SAMPLE_RATE = 44100;

    /**
     * Default audio buffer size for streaming
     */
    public final static int DEFAULT_AUDIO_BUFFER_SIZE = 4096;
    /**
     * Maximum number of audio pool channels
     */
    public final static int MAX_AUDIO_BUFFER_POOL_CHANNELS = 16;

    //------------------------------------------------------------------------------------
    // Module: utils - Configuration Flags
    //------------------------------------------------------------------------------------
    /**
     * Show logger() output messages
     */
    public static boolean SUPPORT_TRACELOG = true;

    /**
     * Show LOG_DEFAULT output messages
     * NOTE: By default LOG_DEBUG traces not shown
     */
    public static boolean SUPPORT_TRACELOG_DEBUG = false;

    // utils: Configuration values
    //------------------------------------------------------------------------------------
    /**
     * Max length of one trace-log message
     */
    public final static int MAX_TRACELOG_MSG_LENGTH = 128;
    /**
     * Max UWP messages to process
     */
    public final static int MAX_UWP_MESSAGES = 512;

    // rModels: config values
    //------------------------------------------------------------------------------------
    /**
     * 
     */
    public final static boolean COMPUTE_TANGENTS_METHOD_01 = true;

    /**
     * Window configuration flags
     */
    public static class ConfigFlag{
        /**
         * Set to try enabling V-Sync on GPU
         */
        public static final int FLAG_VSYNC_HINT = 0x00000040;
        /**
         * Set to run program in fullscreen
         */
        public static final int FLAG_FULLSCREEN_MODE = 0x00000002;
        /**
         * Set to allow resizable window
         */
        public static final int FLAG_WINDOW_RESIZABLE = 0x00000004;
        /**
         * Set to disable window decoration (frame and buttons)
         */
        public static final int FLAG_WINDOW_UNDECORATED = 0x00000008;
        /**
         * Set to hide window
         */
        public static final int FLAG_WINDOW_HIDDEN = 0x00000080;
        /**
         * Set to minimize window (iconify)
         */
        public static final int FLAG_WINDOW_MINIMIZED = 0x00000200;
        /**
         * Set to maximize window (expanded to monitor)
         */
        public static final int FLAG_WINDOW_MAXIMIZED = 0x00000400;
        /**
         * Set to window non focused
         */
        public static final int FLAG_WINDOW_UNFOCUSED = 0x00000800;
        /**
         * Set to window always on top
         */
        public static final int FLAG_WINDOW_TOPMOST = 0x00001000;
        /**
         * Set to allow windows running while minimized
         */
        public static final int FLAG_WINDOW_ALWAYS_RUN = 0x00000100;
        /**
         * Set to allow transparent framebuffer
         */
        public static final int FLAG_WINDOW_TRANSPARENT = 0x00000010;
        /**
         * Set to support HighDPI
         */
        public static final int FLAG_WINDOW_HIGHDPI = 0x00002000;
        /**
         * Set to run program in borderless windowed mode
         */
        public static final int FLAG_BORDERLESS_WINDOWED_MODE = 0x00008000;
        /**
         * Set to support mouse passthrough; only supported when FLAG_WINDOW_UNDECORATED
         */
        public static final int FLAG_WINDOW_MOUSE_PASSTHROUGH = 0x00004000;
        /**
         * Set to try enabling MSAA 4X
         */
        public static final int FLAG_MSAA_4X_HINT = 0x00000020;
        /**
         * Set to try enabling interlaced video format (for V3D)
         */
        public static final int FLAG_INTERLACED_HINT = 0x00010000;

    }

    public Config(){
    }

    /**
     * Define support for Raylib's built in camera system
     * @param supportCameraSystem flag for support
     */
    public void setSupportCameraSystem(boolean supportCameraSystem){
        SUPPORT_CAMERA_SYSTEM = supportCameraSystem;
    }

    /**
     * Define support for Raylib's gesture system
     * @param supportGesturesSystem flag for support
     */
    public void setSupportGesturesSystem(boolean supportGesturesSystem){
        SUPPORT_GESTURES_SYSTEM = supportGesturesSystem;
    }

    /**
     * Define support for Raylib's mouse gesture system
     * @param supportMouseGestures flag for support
     */
    public void setSupportMouseGestures(boolean supportMouseGestures){
        SUPPORT_MOUSE_GESTURES = supportMouseGestures;
    }

    /**
     * Define support for SSH keyboard for PRI
     * @param supportSshKeyboardRpi flag for support
     */
    public void setSupportSshKeyboardRpi(boolean supportSshKeyboardRpi){
        SUPPORT_SSH_KEYBOARD_RPI = supportSshKeyboardRpi;
    }

    /**
     * Define support for native mouse cursor drawing
     * @param supportMouseCursorNative flag for support
     */
    public void setSupportMouseCursorNative(boolean supportMouseCursorNative){
        SUPPORT_MOUSE_CURSOR_POINT = supportMouseCursorNative;
    }

    /**
     * Define support for busy wait loop
     * @param supportBusyWaitLoop flag for support
     */
    public void setSupportBusyWaitLoop(boolean supportBusyWaitLoop){
        SUPPORT_BUSY_WAIT_LOOP = supportBusyWaitLoop;
    }

    /**
     * Define support for half busy wait loop
     * @param supportHalfbusyWaitLoop flag for support
     */
   public void setSupportHalfbusyWaitLoop(boolean supportHalfbusyWaitLoop){
        SUPPORT_PARTIALBUSY_WAIT_LOOP = supportHalfbusyWaitLoop;
    }

    /**
     * Define support for Event Waiting
     * @param supportEventsWaiting flag for support
     */
    public void setSupportEventsWaiting(boolean supportEventsWaiting){
        SUPPORT_EVENTS_WAITING = supportEventsWaiting;
    }

    /**
     * Define support for screen capture
     * @param supportScreenCapture flag for support
     */
    public void setSupportScreenCapture(boolean supportScreenCapture){
        SUPPORT_SCREEN_CAPTURE = supportScreenCapture;
    }

    /**
     * Define support for Raylib's compression API
     * @param supportCompressionApi flag for support
     */
    public void setSupportCompressionApi(boolean supportCompressionApi){
        SUPPORT_COMPRESSION_API = supportCompressionApi;
    }

    /**
     * Define support for data storage
     * @param supportDataStorage flag for support
     */
    public void setSupportDataStorage(boolean supportDataStorage){
        SUPPORT_DATA_STORAGE = supportDataStorage;
    }

    /**
     * Define support for Raylib's built in file IO
     * @param supportStandardFileio flag for support
     */
    public void setSupportStandardFileio(boolean supportStandardFileio){
        SUPPORT_STANDARD_FILEIO = supportStandardFileio;
    }

    /**
     * Define support for VR simulation
     * @param supportVrSimulator flag for support
     */
    public void setSupportVrSimulator(boolean supportVrSimulator){
        SUPPORT_VR_SIMULATOR = supportVrSimulator;
    }

    /**
     * Define support for font texture
     * @param supportFontTexture flag for support
     */
    public void setSupportFontTexture(boolean supportFontTexture){
        SUPPORT_FONT_TEXTURE = supportFontTexture;
    }

    /**
     * Define support for Quad drawing
     * @param supportQuadsDrawMode flag for support
     */
    public void setSupportQuadsDrawMode(boolean supportQuadsDrawMode){
        SUPPORT_QUADS_DRAW_MODE = supportQuadsDrawMode;
    }

    /**
     * Define support for .png files
     * @param supportFileformatPng flag for support
     */
    public void setSupportFileformatPng(boolean supportFileformatPng){
        SUPPORT_FILEFORMAT_PNG = supportFileformatPng;
    }

    /**
     * Define support for .gif files
     * @param supportFileformatGif flag for support
     */
    public void setSupportFileformatGif(boolean supportFileformatGif){
        SUPPORT_FILEFORMAT_GIF = supportFileformatGif;
    }

    /**
     * Define support for .dds files
     * @param supportFileformatDds flag for support
     */
    public void setSupportFileformatDds(boolean supportFileformatDds){
        SUPPORT_FILEFORMAT_DDS = supportFileformatDds;
    }

    /**
     * Define support for .hdr files
     * @param supportFileformatHdr flag for support
     */
    public void setSupportFileformatHdr(boolean supportFileformatHdr){
        SUPPORT_FILEFORMAT_HDR = supportFileformatHdr;
    }

    /**
     * Define support for image export
     * @param supportImageExport flag for support
     */
    public void setSupportImageExport(boolean supportImageExport){
        SUPPORT_IMAGE_EXPORT = supportImageExport;
    }

    /**
     * Define support for image generation
     * @param supportImageGeneration flag for support
     */
    public void setSupportImageGeneration(boolean supportImageGeneration){
        SUPPORT_IMAGE_GENERATION = supportImageGeneration;
    }

    /**
     * Define support for image manipulation
     * @param supportImageManipulation flag for support
     */
    public void setSupportImageManipulation(boolean supportImageManipulation){
        SUPPORT_IMAGE_MANIPULATION = supportImageManipulation;
    }

    /**
     * Define support for Raylib's default font
     * @param supportDefaultFont flag for support
     */
    public void setSupportDefaultFont(boolean supportDefaultFont){
        SUPPORT_DEFAULT_FONT = supportDefaultFont;
    }

    /**
     * Define support for .fnt files
     * @param supportFileformatFnt flag for support
     */
    public void setSupportFileformatFnt(boolean supportFileformatFnt){
        SUPPORT_FILEFORMAT_FNT = supportFileformatFnt;
    }

    /**
     * Define support for .ttf files
     * @param supportFileformatTtf flag for support
     */
    public void setSupportFileformatTtf(boolean supportFileformatTtf){
        SUPPORT_FILEFORMAT_TTF = supportFileformatTtf;
    }

    /**
     * Define support for text manipulation
     * @param supportTextManipulation flag for support
     */
    public void setSupportTextManipulation(boolean supportTextManipulation){
        SUPPORT_TEXT_MANIPULATION = supportTextManipulation;
    }

    /**
     * Define support for .obj files
     * @param supportFileformatObj flag for support
     */
    public void setSupportFileformatObj(boolean supportFileformatObj){
        SUPPORT_FILEFORMAT_OBJ = supportFileformatObj;
    }

    /**
     * Define support for .mtl files
     * @param supportFileformatMtl flag for support
     */
    public void setSupportFileformatMtl(boolean supportFileformatMtl){
        SUPPORT_FILEFORMAT_MTL = supportFileformatMtl;
    }

    /**
     * Define support for .iqm files
     * @param supportFileformatIqm flag for support
     */
    public void setSupportFileformatIqm(boolean supportFileformatIqm){
        SUPPORT_FILEFORMAT_IQM = supportFileformatIqm;
    }

    /**
     * Define support for .gltf files
     * @param supportFileformatGltf flag for support
     */
    public void setSupportFileformatGltf(boolean supportFileformatGltf){
        SUPPORT_FILEFORMAT_GLTF = supportFileformatGltf;
    }

    /**
     * Define support for mesh generation
     * @param supportMeshGeneration flag for support
     */
    public void setSupportMeshGeneration(boolean supportMeshGeneration){
        SUPPORT_MESH_GENERATION = supportMeshGeneration;
    }

    /**
     * Define support for .wav files
     * @param supportFileformatWav flag for support
     */
    public void setSupportFileformatWav(boolean supportFileformatWav){
        SUPPORT_FILEFORMAT_WAV = supportFileformatWav;
    }

    /**
     * Define support for .ogg files
     * @param supportFileformatOgg flag for support
     */
    public void setSupportFileformatOgg(boolean supportFileformatOgg){
        SUPPORT_FILEFORMAT_OGG = supportFileformatOgg;
    }

    /**
     * Define support for .xm files
     * @param supportFileformatXm flag for support
     */
    public void setSupportFileformatXm(boolean supportFileformatXm){
        SUPPORT_FILEFORMAT_XM = supportFileformatXm;
    }

    /**
     * Define support for .mod files
     * @param supportFileformatMod flag for support
     */
    public void setSupportFileformatMod(boolean supportFileformatMod){
        SUPPORT_FILEFORMAT_MOD = supportFileformatMod;
    }

    /**
     * Define support for .mp3 files
     * @param supportFileformatMp3 flag for support
     */
    public void setSupportFileformatMp3(boolean supportFileformatMp3){
        SUPPORT_FILEFORMAT_MP3 = supportFileformatMp3;
    }

    /**
     * Define support for .flac files
     * @param supportFileformatFlac flag for support
     */
    public void setSupportFileformatFlac(boolean supportFileformatFlac){
        SUPPORT_FILEFORMAT_FLAC = supportFileformatFlac;
    }

    /**
     * Define support for trace log messages
     * @param supportTracelog flag for support
     */
    public void setSupportTracelog(boolean supportTracelog){
        SUPPORT_TRACELOG = supportTracelog;
    }

    /**
     * Define support for debug trace log messages
     * @param supportTracelogDebug flag for support
     */
    public void setSupportTracelogDebug(boolean supportTracelogDebug){
        SUPPORT_TRACELOG_DEBUG = supportTracelogDebug;
    }

    /**
     * Define support for OpenGL debug messages
     * @param supportOpenGLDebug flag for support
     */
    public void setSupportOpenGLDebug(boolean supportOpenGLDebug) {
        RLGL_ENABLE_OPENGL_DEBUG_CONTEXT = supportOpenGLDebug;
    }
}
