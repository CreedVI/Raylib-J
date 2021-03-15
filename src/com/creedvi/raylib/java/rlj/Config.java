package com.creedvi.raylib.java.rlj;

public class Config{

    public static final String RAYLIB_VERSION = "3.5";

    //------------------------------------------------------------------------------------
    // Module: core - Configuration Flags
    //------------------------------------------------------------------------------------
    private static final String OS = System.getProperty("os.name").toLowerCase();
    public static boolean __WINDOWS__ = (OS.contains("win"));
    public static boolean __APPLE__ = (OS.contains("mac"));
    public static boolean __LINUX__ = (OS.contains("nix") || OS.contains("nux") || OS.contains("aix"));

    // Camera module is included (camera.h) and multiple predefined cameras are available: free, 1st/3rd person, orbital
    final static boolean SUPPORT_CAMERA_SYSTEM = true;
    // Gestures module is included (gestures.h) to support gestures detection: tap, hold, swipe, drag
    final static boolean SUPPORT_GESTURES_SYSTEM = true;
    // Mouse gestures are directly mapped like touches and processed by gestures system
    final static boolean SUPPORT_MOUSE_GESTURES = true;
    // Reconfigure standard input to receive key inputs, works with SSH connection.
    final static boolean SUPPORT_SSH_KEYBOARD_RPI = true;
    // Draw a mouse pointer on screen
    final static boolean SUPPORT_MOUSE_CURSOR_NATIVE = true;
    // Use busy wait loop for timing sync, if not defined, a high-resolution timer is setup and used
    //#define SUPPORT_BUSY_WAIT_LOOP      1
    // Use a half-busy wait loop, in this case frame sleeps for some time and runs a busy-wait-loop at the end
    final static boolean SUPPORT_HALFBUSY_WAIT_LOOP = true;
    // Wait for events passively (sleeping while no events) instead of polling them actively every frame
    public final static boolean SUPPORT_EVENTS_WAITING = false;
    // Allow automatic screen capture of current screen pressing F12, defined in KeyCallback()
    final static boolean SUPPORT_SCREEN_CAPTURE = true;
    // Allow automatic gif recording of current screen pressing CTRL+F12, defined in KeyCallback()
    final static boolean SUPPORT_GIF_RECORDING = true;
    // Support CompressData() and DecompressData() functions
    final static boolean SUPPORT_COMPRESSION_API = true;
    // Support saving binary data automatically to a generated storage.data file. This file is managed internally.
    final static boolean SUPPORT_DATA_STORAGE = true;

    public final static boolean SUPPORT_STANDARD_FILEIO = true;

    // core: Configuration values
    //------------------------------------------------------------------------------------

    public final static int LINUX_MAX_FILEPATH_LENGTH = 4096;
    // Maximum length for filepaths (Linux PATH_MAX default value)
    public final static int MAX_FILEPATH_LENGTH = 512;        // Maximum length supported for filepaths
    public final static int MAX_GAMEPADS = 4;        // Max number of gamepads supported
    final static int MAX_GAMEPAD_AXIS = 8;        // Max number of axis supported (per gamepad)
    final static int MAX_GAMEPAD_BUTTONS = 32;        // Max bumber of buttons supported (per gamepad)
    public final static int MAX_TOUCH_POINTS = 10;        // Maximum number of touch points supported
    public final static int MAX_KEY_PRESSED_QUEUE = 16;        // Max number of characters in the key input queue
    final static String STORAGE_DATA_FILE = "storage.data";       // Automatic storage filename
    final static int MAX_DECOMPRESSION_SIZE = 64;        // Max size allocated for decompression in MB


    //------------------------------------------------------------------------------------
    // Module: rlgl - Configuration Flags
    //------------------------------------------------------------------------------------
    // Support VR simulation functionality (stereo rendering)
    public final static boolean SUPPORT_VR_SIMULATOR = true;

    // rlgl: Configuration values
    //------------------------------------------------------------------------------------

    public final static int DEFAULT_BATCH_BUFFER_ELEMENTS = 8192;    // Default internal render batch limits
    final static int ES2_DEFAULT_BATCH_BUFFER_ELEMENTS = 2048;    // Default internal render batch limits


    final static int DEFAULT_BATCH_BUFFERS = 1;      // Default number of batch buffers (multi-buffering)
    final static int DEFAULT_BATCH_DRAWCALLS = 256;      // Default number of batch draw calls (by state changes:
    // mode, texture)

    final static int MAX_MATRIX_STACK_SIZE = 32;      // Maximum size of internal Matrix stack
    final static int MAX_MESH_VERTEX_BUFFERS = 7;      // Maximum vertex buffers (VBO) per mesh
    final static int MAX_SHADER_LOCATIONS = 32;      // Maximum number of shader locations supported
    final static int MAX_MATERIAL_MAPS = 12;      // Maximum number of shader maps supported

    public final static float RL_CULL_DISTANCE_NEAR = 0.01f;      // Default projection matrix near cull distance
    public final static float RL_CULL_DISTANCE_FAR = 1000.0f;      // Default projection matrix far cull distance

    // Default shader vertex attribute names to set location points
    public final static String DEFAULT_SHADER_ATTRIB_NAME_POSITION = "vertexPosition";    // Binded by default to shader
    // location:0
    public final static String DEFAULT_SHADER_ATTRIB_NAME_TEXCOORD = "vertexTexCoord";   // Binded by default to shader
    // location:1
    public final static String DEFAULT_SHADER_ATTRIB_NAME_NORMAL = "vertexNormal";      // Binded by default to shader
    // location:2
    public final static String DEFAULT_SHADER_ATTRIB_NAME_COLOR = "vertexColor";       // Binded by default to shader
    // location:3
    public final static String DEFAULT_SHADER_ATTRIB_NAME_TANGENT = "vertexTangent";     // Binded by default to shader
    // location:4
    public final static String DEFAULT_SHADER_ATTRIB_NAME_TEXCOORD2 = "vertexTexCoord2";   // Binded by default to shader
    // location: 5


    //------------------------------------------------------------------------------------
    // Module: shapes - Configuration Flags
    //------------------------------------------------------------------------------------
    // Draw rectangle shapes using font texture white character instead of default white texture
    // Allows drawing rectangles and text with a single draw call, very useful for GUI systems!
    final static boolean SUPPORT_FONT_TEXTURE = true;
    // Use QUADS instead of TRIANGLES for drawing when possible
    // Some lines-based shapes could still use lines
    final static boolean SUPPORT_QUADS_DRAW_MODE = true;


    //------------------------------------------------------------------------------------
    // Module: textures - Configuration Flags
    //------------------------------------------------------------------------------------
    // Selecte desired fileformats to be supported for image data loading
    final static boolean SUPPORT_FILEFORMAT_PNG = true;
    //#final static int SUPPORT_FILEFORMAT_BMP      1
    //#define SUPPORT_FILEFORMAT_TGA      1
    //#define SUPPORT_FILEFORMAT_JPG      1
    final static boolean SUPPORT_FILEFORMAT_GIF = true;
    //#define SUPPORT_FILEFORMAT_PSD      1
    final static boolean SUPPORT_FILEFORMAT_DDS = true;
    final static boolean SUPPORT_FILEFORMAT_HDR = true;
    //#define SUPPORT_FILEFORMAT_KTX      1
    //#define SUPPORT_FILEFORMAT_ASTC     1
    //#define SUPPORT_FILEFORMAT_PKM      1
    //#define SUPPORT_FILEFORMAT_PVR      1

    // Support image export functionality (.png, .bmp, .tga, .jpg)
    final static boolean SUPPORT_IMAGE_EXPORT = true;
    // Support procedural image generation functionality (gradient, spot, perlin-noise, cellular)
    final static boolean SUPPORT_IMAGE_GENERATION = true;
    // Support multiple image editing functions to scale, adjust colors, flip, draw on images, crop...
    // If not defined, still some functions are supported: ImageFormat(), ImageCrop(), ImageToPOT()
    public final static boolean SUPPORT_IMAGE_MANIPULATION = true;


    //------------------------------------------------------------------------------------
    // Module: text - Configuration Flags
    //------------------------------------------------------------------------------------
    // Default font is loaded on window initialization to be available for the user to render simple text
    // NOTE: If enabled, uses external module functions to load default raylib font
    public final static boolean SUPPORT_DEFAULT_FONT = true;
    // Selected desired font fileformats to be supported for loading
    final static boolean SUPPORT_FILEFORMAT_FNT = true;
    final static boolean SUPPORT_FILEFORMAT_TTF = true;

    // Support text management functions
    // If not defined, still some functions are supported: TextLength(), TextFormat()
    final static boolean SUPPORT_TEXT_MANIPULATION = true;

    // text: Configuration values
    //------------------------------------------------------------------------------------
    public final static int MAX_TEXT_BUFFER_LENGTH = 1024;        // Size of internal static buffers used on some functions:
    // TextFormat(), TextSubtext(), TextToUpper(), TextToLower(), TextToPascal(), TextSplit()
    final static int MAX_TEXT_UNICODE_CHARS = 512;        // Maximum number of unicode codepoints: GetCodepoints()
    final static int MAX_TEXTSPLIT_COUNT = 128;        // Maximum number of substrings to split: TextSplit()


    //------------------------------------------------------------------------------------
    // Module: models - Configuration Flags
    //------------------------------------------------------------------------------------
    // Selected desired model fileformats to be supported for loading
    final static boolean SUPPORT_FILEFORMAT_OBJ = true;
    final static boolean SUPPORT_FILEFORMAT_MTL = true;
    final static boolean SUPPORT_FILEFORMAT_IQM = true;
    final static boolean SUPPORT_FILEFORMAT_GLTF = true;
    // Support procedural mesh generation functions, uses external par_shapes.h library
    // NOTE: Some generated meshes DO NOT include generated texture coordinates
    final static boolean SUPPORT_MESH_GENERATION = true;


    //------------------------------------------------------------------------------------
    // Module: audio - Configuration Flags
    //------------------------------------------------------------------------------------
    // Desired audio fileformats to be supported for loading
    final static boolean SUPPORT_FILEFORMAT_WAV = true;
    final static boolean SUPPORT_FILEFORMAT_OGG = true;
    final static boolean SUPPORT_FILEFORMAT_XM = true;
    final static boolean SUPPORT_FILEFORMAT_MOD = true;
    final static boolean SUPPORT_FILEFORMAT_MP3 = true;
    //#define SUPPORT_FILEFORMAT_FLAC     1

    // audio: Configuration values
//------------------------------------------------------------------------------------
    final static String AUDIO_DEVICE_FORMAT = "ma_format_f32";    // Device output format (miniaudio: float-32bit)
    final static int AUDIO_DEVICE_CHANNELS = 2;    // Device output channels: stereo
    final static int AUDIO_DEVICE_SAMPLE_RATE = 44100;    // Device output sample rate

    final static int DEFAULT_AUDIO_BUFFER_SIZE = 4096;    // Default audio buffer size for streaming
    final static int MAX_AUDIO_BUFFER_POOL_CHANNELS = 16;    // Maximum number of audio pool channels

    //------------------------------------------------------------------------------------
    // Module: utils - Configuration Flags
    //------------------------------------------------------------------------------------
    // Show TRACELOG() output messages
    // NOTE: By default LOG_DEBUG traces not shown
    final static boolean SUPPORT_TRACELOG = true;
    //#define SUPPORT_TRACELOG_DEBUG      1

    // utils: Configuration values
    //------------------------------------------------------------------------------------
    final static int MAX_TRACELOG_MSG_LENGTH = 128;    // Max length of one trace-log message
    final static int MAX_UWP_MESSAGES = 512;    // Max UWP messages to process

    public enum ConfigFlag{
        FLAG_VSYNC_HINT(0x00000040),   // Set to try enabling V-Sync on GPU
        FLAG_FULLSCREEN_MODE(0x00000002),   // Set to run program in fullscreen
        FLAG_WINDOW_RESIZABLE(0x00000004),   // Set to allow resizable window
        FLAG_WINDOW_UNDECORATED(0x00000008),   // Set to disable window decoration (frame and buttons)
        FLAG_WINDOW_HIDDEN(0x00000080),   // Set to hide window
        FLAG_WINDOW_MINIMIZED(0x00000200),   // Set to minimize window (iconify)
        FLAG_WINDOW_MAXIMIZED(0x00000400),   // Set to maximize window (expanded to monitor)
        FLAG_WINDOW_UNFOCUSED(0x00000800),   // Set to window non focused
        FLAG_WINDOW_TOPMOST(0x00001000),   // Set to window always on top
        FLAG_WINDOW_ALWAYS_RUN(0x00000100),   // Set to allow windows running while minimized
        FLAG_WINDOW_TRANSPARENT(0x00000010),   // Set to allow transparent framebuffer
        FLAG_WINDOW_HIGHDPI(0x00002000),   // Set to support HighDPI
        FLAG_MSAA_4X_HINT(0x00000020),   // Set to try enabling MSAA 4X
        FLAG_INTERLACED_HINT(0x00010000);    // Set to try enabling interlaced video format (for V3D)

        private final int flag;

        ConfigFlag(int i){
            flag = i;
        }

        public int getFlag(){
            return flag;
        }
    }

    public Config(){
    }

}
