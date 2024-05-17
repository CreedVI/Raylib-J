package com.raylib.java;

public class Config{

    /**
     * Raylib configuration flags
     * This file defines all the configuration flags for the different raylib modules.txt
     */

    public static final String RAYLIB_VERSION = "4.2";

    //------------------------------------------------------------------------------------
// Module selection - Some modules could be avoided
// Mandatory modules: rcore, rlgl, utils
//------------------------------------------------------------------------------------
            public static boolean SUPPORT_MODULE_RSHAPES   = true;
            public static boolean SUPPORT_MODULE_RTEXTURES = true;
            public static boolean SUPPORT_MODULE_RTEXT     = true;          // WARNING: It requires SUPPORT_MODULE_RTEXTURES to load sprite font textures
            public static boolean SUPPORT_MODULE_RMODELS   = true;
            public static boolean SUPPORT_MODULE_RAUDIO    = true;

    //------------------------------------------------------------------------------------
    // Module: core - Configuration Flags
    //------------------------------------------------------------------------------------
    private static final String OS = System.getProperty("os.name").toLowerCase();
    public static boolean __WINDOWS__ = (OS.contains("win"));
    public static boolean __APPLE__ = (OS.contains("mac"));
    public static boolean __LINUX__ = (OS.contains("nix") || OS.contains("nux") || OS.contains("aix"));
    public static boolean PLATFORM_DESKTOP = (__WINDOWS__ || __APPLE__ || __LINUX__);

    /**
     * rCamera module is included (core.camera.java) and multiple predefined cameras are available: free, 1st/3rd
     * person, orbital
     */
    static boolean SUPPORT_CAMERA_SYSTEM = true;
    /**
     * Gestures module is included (core.input.gestures.java) to support gestures detection: tap, hold, swipe, drag
     */
    static boolean SUPPORT_GESTURES_SYSTEM = false;
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
    public static boolean SUPPORT_WINMM_HIGHRES_TIMER = false;
    /**
     * Use a half-busy wait loop, in this case frame sleeps for some time and runs a busy-wait-loop at the end
     */
    public static boolean SUPPORT_HALFBUSY_WAIT_LOOP = true;
    /**
     * Wait for events passively (sleeping while no events) instead of polling them actively every frame
     */
    public static boolean SUPPORT_EVENTS_WAITING = false;
    /**
     * Allow automatic screen capture of current screen pressing F12, defined in KeyCallback()
     */
    public static boolean SUPPORT_SCREEN_CAPTURE = false;
    /**
     * Allow automatic gif recording of current screen pressing CTRL+F12, defined in KeyCallback()
     */
    public static boolean SUPPORT_GIF_RECORDING = false;
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

    // Support automatic generated events, loading and recording of those events when required
    public static boolean SUPPORT_EVENTS_AUTOMATION = false;

    // Support custom frame control, only for advance users
    // By default EndDrawing() does this job: draws everything + SwapScreenBuffer() + manage frame timming + PollInputEvents()
    // Enabling this flag allows manual control of the frame processes, use at your own risk
    public static boolean SUPPORT_CUSTOM_FRAME_CONTROL = false;


    // core: Configuration values
    //------------------------------------------------------------------------------------

    public final static int LINUX_MAX_FILEPATH_LENGTH = 4096;
    // Maximum length for filepaths (Linux PATH_MAX default value)
    public final static int MAX_FILEPATH_LENGTH = 512;              // Maximum length supported for file paths
    public final static int MAX_KEYBOARD_KEYS = 512;                // Maximum number of keyboard keys supported
    public final static int MAX_MOUSE_BUTTONS = 8;                  // Maximum number of mouse buttons supported
    public final static int MAX_GAMEPADS = 4;                       // Max number of gamepads supported
    public final static int MAX_GAMEPAD_AXIS = 8;                   // Max number of axis supported (per gamepad)
    public final static int MAX_GAMEPAD_BUTTONS = 32;               // Max number of buttons supported (per gamepad)
    public final static int MAX_TOUCH_POINTS = 10;                  // Maximum number of touch points supported
    public final static int MAX_KEY_PRESSED_QUEUE = 16;             // Max number of characters in the key input queue
    public final static String STORAGE_DATA_FILE = "storage.data";  // Automatic storage filename
    public final static int MAX_DECOMPRESSION_SIZE = 64;                   // Max size allocated for decompression in MB


    //------------------------------------------------------------------------------------
    // Module: rlgl - Configuration Flags
    //------------------------------------------------------------------------------------
    /**
     * Support VR simulation functionality (stereo rendering)
     */
    public static boolean SUPPORT_VR_SIMULATOR = false;

    // rlgl: Configuration values
    //------------------------------------------------------------------------------------
    public final static boolean SUPPORT_GL_DETAILS_INFO = false;
    public final static int RL_DEFAULT_BATCH_BUFFER_ELEMENTS = 8192;    // Default internal render batch limits
    final static int ES2_DEFAULT_BATCH_BUFFER_ELEMENTS = 2048;    // Default internal render batch limits


    final static int RL_DEFAULT_BATCH_BUFFERS = 1;      // Default number of batch buffers (multi-buffering)
    public final static int RL_DEFAULT_BATCH_DRAWCALLS = 256;      // Default number of batch draw calls (by state changes:
    // mode, texture)

    public final static int RL_DEFAULT_BATCH_MAX_TEXTURE_UNITS = 4; // Maximum number of textures units that can be activated on batch
    // drawing (SetShaderValueTexture())
    public final static int RL_MAX_MATRIX_STACK_SIZE = 32;      // Maximum size of internal Matrix stack
    public final static int RL_MAX_SHADER_LOCATIONS = 32;      // Maximum number of shader locations supported

    public final static float RL_CULL_DISTANCE_NEAR = 0.01f;      // Default projection matrix near cull distance
    public final static float RL_CULL_DISTANCE_FAR = 1000.0f;      // Default projection matrix far cull distance

    // Default shader vertex attribute names to set location points
    public static final String RL_DEFAULT_SHADER_ATTRIB_NAME_POSITION    = "vertexPosition";   // Binded by default to shader location: 0
    public static final String RL_DEFAULT_SHADER_ATTRIB_NAME_TEXCOORD    = "vertexTexCoord";    // Binded by default to shader location: 1
    public static final String RL_DEFAULT_SHADER_ATTRIB_NAME_NORMAL      = "vertexNormal";      // Binded by default to shader location: 2
    public static final String RL_DEFAULT_SHADER_ATTRIB_NAME_COLOR       =  "vertexColor";       // Binded by default to shader location: 3
    public static final String RL_DEFAULT_SHADER_ATTRIB_NAME_TANGENT     = "vertexTangent";     // Binded by default to shader location: 4
    public static final String RL_DEFAULT_SHADER_ATTRIB_NAME_TEXCOORD2   = "vertexTexCoord2";   // Binded by default to shader location: 5
    public static final String RL_DEFAULT_SHADER_UNIFORM_NAME_MVP        = "mvp";               // model-view-projection matrix
    public static final String RL_DEFAULT_SHADER_UNIFORM_NAME_VIEW       = "matView";           // view matrix
    public static final String RL_DEFAULT_SHADER_UNIFORM_NAME_PROJECTION = "matProjection";     // projection matrix
    public static final String RL_DEFAULT_SHADER_UNIFORM_NAME_MODEL      = "matModel";          // model matrix
    public static final String RL_DEFAULT_SHADER_UNIFORM_NAME_NORMAL     = "matNormal";         // normal matrix (transpose(inverse(matModelView))
    public static final String RL_DEFAULT_SHADER_UNIFORM_NAME_COLOR      = "colDiffuse";        // color diffuse (base tint color, multiplied by texture color)
    public static final String RL_DEFAULT_SHADER_SAMPLER2D_NAME_TEXTURE0 = "texture0";          // texture0 (texture slot active 0)
    public static final String RL_DEFAULT_SHADER_SAMPLER2D_NAME_TEXTURE1 = "texture1";          // texture1 (texture slot active 1)
    public static final String RL_DEFAULT_SHADER_SAMPLER2D_NAME_TEXTURE2 = "texture2";          // texture2 (texture slot active 2)

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


    //------------------------------------------------------------------------------------
    // Module: textures - Configuration Flags
    //------------------------------------------------------------------------------------
    // Selecte desired fileformats to be supported for image data loading
    public static boolean SUPPORT_FILEFORMAT_PNG = true;
    public static boolean SUPPORT_FILEFORMAT_BMP = true;
    public static boolean SUPPORT_FILEFORMAT_TGA = true;
    public static boolean SUPPORT_FILEFORMAT_JPG = true;
    public static boolean SUPPORT_FILEFORMAT_GIF = true;
    public static boolean SUPPORT_FILEFORMAT_PIC = true;
    public static boolean SUPPORT_FILEFORMAT_PSD = true;
    static boolean SUPPORT_FILEFORMAT_DDS = true;
    public static boolean SUPPORT_FILEFORMAT_HDR = true;
    //#define SUPPORT_FILEFORMAT_KTX      1
    //#define SUPPORT_FILEFORMAT_ASTC     1
    //#define SUPPORT_FILEFORMAT_PKM      1
    //#define SUPPORT_FILEFORMAT_PVR      1
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
     * Support text management functions
     * If not defined, still some functions are supported: TextLength(), TextFormat()
     */
    public static boolean SUPPORT_TEXT_MANIPULATION = true;

    // text: Configuration values
    //------------------------------------------------------------------------------------
    public final static int MAX_TEXT_BUFFER_LENGTH = 1024;        // Size of internal static buffers used on some functions:
    // TextFormat(), TextSubtext(), TextToUpper(), TextToLower(), TextToPascal(), TextSplit()
    public final static int MAX_TEXT_UNICODE_CHARS = 512;        // Maximum number of unicode codepoints: GetCodepoints()
    public final static int MAX_TEXTSPLIT_COUNT = 128;        // Maximum number of substrings to split: TextSplit()


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
    public static boolean SUPPORT_FILEFORMAT_GLTF = true;

    /**
     * Select .vox to be supported for loading
     */
    public static boolean SUPPORT_FILEFORMAT_VOX = true;

    /**
     * Support procedural mesh generation functions, uses external par_shapes.h library
     * NOTE: Some generated meshes DO NOT include generated texture coordinates
     */
    public static boolean SUPPORT_MESH_GENERATION = true;
    public static boolean SUPPORT_CUSTOM_MESH_GEN_PLANE = true;
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
    public final static String AUDIO_DEVICE_FORMAT = "ma_format_f32";    // Device output format (miniaudio: float-32bit)
    final static int AUDIO_DEVICE_CHANNELS = 2;    // Device output channels: stereo
    final static int AUDIO_DEVICE_SAMPLE_RATE = 44100;    // Device output sample rate

    final static int DEFAULT_AUDIO_BUFFER_SIZE = 4096;    // Default audio buffer size for streaming
    public final static int MAX_AUDIO_BUFFER_POOL_CHANNELS = 16;    // Maximum number of audio pool channels

    //------------------------------------------------------------------------------------
    // Module: utils - Configuration Flags
    //------------------------------------------------------------------------------------
    /**
     * Show TRACELOG() output messages
     */
    public static boolean SUPPORT_TRACELOG = true;

    /**
     * Show LOG_DEFAULT output messages
     * NOTE: By default LOG_DEBUG traces not shown
     */
    public static boolean SUPPORT_TRACELOG_DEBUG = false;

    // utils: Configuration values
    //------------------------------------------------------------------------------------
    final static int MAX_TRACELOG_MSG_LENGTH = 128;    // Max length of one trace-log message
    final static int MAX_UWP_MESSAGES = 512;    // Max UWP messages to process

    // rModels: config values
    //------------------------------------------------------------------------------------
    public final static boolean COMPUTE_TANGENTS_METHOD_01 = true;

    public static class ConfigFlag{
        public static final int
        FLAG_VSYNC_HINT = 0x00000040,   // Set to try enabling V-Sync on GPU
        FLAG_FULLSCREEN_MODE = 0x00000002,   // Set to run program in fullscreen
        FLAG_WINDOW_RESIZABLE = 0x00000004,   // Set to allow resizable window
        FLAG_WINDOW_UNDECORATED = 0x00000008,   // Set to disable window decoration (frame and buttons)
        FLAG_WINDOW_HIDDEN = 0x00000080,   // Set to hide window
        FLAG_WINDOW_MINIMIZED = 0x00000200,   // Set to minimize window (iconify)
        FLAG_WINDOW_MAXIMIZED = 0x00000400,   // Set to maximize window (expanded to monitor)
        FLAG_WINDOW_UNFOCUSED = 0x00000800,   // Set to window non focused
        FLAG_WINDOW_TOPMOST = 0x00001000,   // Set to window always on top
        FLAG_WINDOW_ALWAYS_RUN = 0x00000100,   // Set to allow windows running while minimized
        FLAG_WINDOW_TRANSPARENT = 0x00000010,   // Set to allow transparent framebuffer
        FLAG_WINDOW_HIGHDPI = 0x00002000,   // Set to support HighDPI
        FLAG_WINDOW_MOUSE_PASSTHROUGH = 0x00004000, // Set to support mouse passthrough, only supported when FLAG_WINDOW_UNDECORATED
        FLAG_MSAA_4X_HINT = 0x00000020,   // Set to try enabling MSAA 4X
        FLAG_INTERLACED_HINT = 0x00010000;    // Set to try enabling interlaced video format (for V3D)

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
        SUPPORT_WINMM_HIGHRES_TIMER = supportBusyWaitLoop;
    }

    /**
     * Define support for half busy wait loop
     * @param supportHalfbusyWaitLoop flag for support
     */
   public void setSupportHalfbusyWaitLoop(boolean supportHalfbusyWaitLoop){
        SUPPORT_HALFBUSY_WAIT_LOOP = supportHalfbusyWaitLoop;
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
     * Define support for gif recording
     * @param supportGifRecording flag for support
     */
    public void setSupportGifRecording(boolean supportGifRecording){
        SUPPORT_GIF_RECORDING = supportGifRecording;
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
}
