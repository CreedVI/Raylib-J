package com.creedvi.raylib.java.rlj.textures;

import com.creedvi.raylib.java.rlj.core.Color;
import com.creedvi.raylib.java.rlj.raymath.Vector2;
import com.creedvi.raylib.java.rlj.rlgl.RLGL;
import com.creedvi.raylib.java.rlj.shapes.Rectangle;

import static com.creedvi.raylib.java.rlj.rlgl.RLGL.*;
import static com.creedvi.raylib.java.rlj.utils.Tracelog.TraceLogType.LOG_INFO;
import static com.creedvi.raylib.java.rlj.utils.Tracelog.TraceLogType.LOG_WARNING;
import static com.creedvi.raylib.java.rlj.utils.Tracelog.Tracelog;

public class Textures{

    public static Image LoadImage(String path){
        return null;
    }

    //LoadImageRaw

    //LoadImageAnim

    //LoadImageFromMemory

    public static Image UnloadImage(Image image){
        image.data = null;
        return image;
    }

    //ExportImage

    //ExportImageAsCode

    //GenImageColor

    //Support image generation

    //ImageCopy

    public static Image ImageFromImage(Image image, Rectangle rectangle){
        Image result = new Image();

        // TODO: Check rec is valid?
        result.width = (int)rectangle.getWidth();
        result.height = (int)rectangle.getHeight();
        result.data = image.data;
        result.format = image.format;
        result.mipmaps = 1;

        return result;
    }

    //ImageCrop

    //ImageFormat

    //ImageToPOT

    //support image manipulation

    public static void SetTextureFilter(Texture2D texture, int filterMode){
        switch (filterMode){

            case 0:{
                if (texture.mipmaps > 1){
                    // RL_FILTER_MIP_NEAREST - tex filter: POINT, mipmaps filter: POINT (sharp switching between mipmaps)
                    rlTextureParameters(texture.id, RL_TEXTURE_MIN_FILTER, RL_FILTER_MIP_NEAREST);

                    // RL_FILTER_NEAREST - tex filter: POINT (no filter), no mipmaps
                    rlTextureParameters(texture.id, RL_TEXTURE_MAG_FILTER, RL_FILTER_NEAREST);
                }
                else{
                    // RL_FILTER_NEAREST - tex filter: POINT (no filter), no mipmaps
                    rlTextureParameters(texture.id, RL_TEXTURE_MIN_FILTER, RL_FILTER_NEAREST);
                    rlTextureParameters(texture.id, RL_TEXTURE_MAG_FILTER, RL_FILTER_NEAREST);
                }
            }
            break;
            case 1:{
                if (texture.mipmaps > 1){
                    // RL_FILTER_LINEAR_MIP_NEAREST - tex filter: BILINEAR, mipmaps filter: POINT (sharp switching between mipmaps)
                    // Alternative: RL_FILTER_NEAREST_MIP_LINEAR - tex filter: POINT, mipmaps filter: BILINEAR (smooth transition between mipmaps)
                    rlTextureParameters(texture.id, RL_TEXTURE_MIN_FILTER, RL_FILTER_LINEAR_MIP_NEAREST);

                    // RL_FILTER_LINEAR - tex filter: BILINEAR, no mipmaps
                    rlTextureParameters(texture.id, RL_TEXTURE_MAG_FILTER, RL_FILTER_LINEAR);
                }
                else{
                    // RL_FILTER_LINEAR - tex filter: BILINEAR, no mipmaps
                    rlTextureParameters(texture.id, RL_TEXTURE_MIN_FILTER, RL_FILTER_LINEAR);
                    rlTextureParameters(texture.id, RL_TEXTURE_MAG_FILTER, RL_FILTER_LINEAR);
                }
            }
            break;
            case 2:{
                if (texture.mipmaps > 1){
                    // RL_FILTER_MIP_LINEAR - tex filter: BILINEAR, mipmaps filter: BILINEAR (smooth transition between mipmaps)
                    rlTextureParameters(texture.id, RL_TEXTURE_MIN_FILTER, RL_FILTER_MIP_LINEAR);

                    // RL_FILTER_LINEAR - tex filter: BILINEAR, no mipmaps
                    rlTextureParameters(texture.id, RL_TEXTURE_MAG_FILTER, RL_FILTER_LINEAR);
                }
                else{
                    Tracelog(LOG_WARNING, "TEXTURE: [ID " + texture.id
                            + "] No mipmaps available for TRILINEAR texture filtering");

                    // RL_FILTER_LINEAR - tex filter: BILINEAR, no mipmaps
                    rlTextureParameters(texture.id, RL_TEXTURE_MIN_FILTER, RL_FILTER_LINEAR);
                    rlTextureParameters(texture.id, RL_TEXTURE_MAG_FILTER, RL_FILTER_LINEAR);
                }
            }
            break;
            case 3:
                rlTextureParameters(texture.id, RL_TEXTURE_ANISOTROPIC_FILTER, 4);
                break;
            case 4:
                rlTextureParameters(texture.id, RL_TEXTURE_ANISOTROPIC_FILTER, 8);
                break;
            case 5:
                rlTextureParameters(texture.id, RL_TEXTURE_ANISOTROPIC_FILTER, 16);
                break;
            default:
                break;
        }
    }

    public static Texture2D LoadTextureFromImage(Image image) {
        Texture2D texture = new Texture2D();

        if ((image.data != null) && (image.width != 0) && (image.height != 0)){
            texture.id = rlLoadTexture(image.data, image.width, image.height, image.format, image.mipmaps);
        }
        else Tracelog(LOG_WARNING, "IMAGE: Data is not valid to load texture");

        texture.width = image.width;
        texture.height = image.height;
        texture.mipmaps = image.mipmaps;
        texture.format = image.format;

        return texture;
    }

    public static void UnloadTexture(Texture2D texture){
        if (texture.getId() > 0)
        {
            RLGL.rlUnloadTexture(texture.getId());

            Tracelog(LOG_INFO, "TEXTURE: [ID " + texture.getId() + "] Unloaded texture data from VRAM (GPU)");
        }
    }

    // Draw a part of a texture (defined by a rectangle) with 'pro' parameters
    // NOTE: origin is relative to destination rectangle size
    public static void DrawTexturePro(Texture2D texture, Rectangle source, Rectangle dest, Vector2 origin,
                                      float rotation, Color tint){
        // Check if texture is valid
        if (texture.id > 0) {
            float width = (float)texture.width;
            float height = (float)texture.height;

            boolean flipX = false;

            if (source.getWidth() < 0) {
                flipX = true;
                source.setWidth(source.getWidth() * -1);
            }
            if (source.getHeight() < 0){
                source.setY(source.getY() - source.getHeight());
            }

            rlEnableTexture(texture.getId());

            rlPushMatrix();
            rlTranslatef(dest.getX(), dest.getY(), 0.0f);
            rlRotatef(rotation, 0.0f, 0.0f, 1.0f);
            rlTranslatef(-origin.getX(), -origin.getY(), 0.0f);

            rlBegin(RL_QUADS);
            rlColor4ub(tint.getR(), tint.getG(), tint.getB(), tint.getA());
            rlNormal3f(0.0f, 0.0f, 1.0f);                          // Normal vector pointing towards viewer

            // Bottom-left corner for texture and quad
            if (flipX){
                rlTexCoord2f((source.getX() + source.getWidth())/width, source.getY()/height);
            }
            else{
                rlTexCoord2f(source.getX()/width, source.getY()/height);
            }
            rlVertex2f(0.0f, 0.0f);

            // Bottom-right corner for texture and quad
            if (!flipX){
                rlTexCoord2f(source.getX()/width, (source.getY() + source.getHeight())/height);
            }
            else{
                rlTexCoord2f((source.getX() + source.getWidth())/width, (source.getY() + source.getHeight())/height);
            }
            rlVertex2f(0.0f, dest.getHeight());

            // Top-right corner for texture and quad
            if (!flipX){
                rlTexCoord2f((source.getX() + source.getWidth())/width, (source.getY() + source.getHeight())/height);
            }
            else{
                rlTexCoord2f(source.getX()/width, (source.getY() + source.getHeight())/height);
            }
            rlVertex2f(dest.getWidth(), dest.getHeight());

            // Top-left corner for texture and quad
            if (flipX){
                rlTexCoord2f(source.getX()/width, source.getY()/height);
            }
            else{
                rlTexCoord2f((source.getX() + source.getWidth())/width, source.getY()/height);
            }
            rlVertex2f(dest.getWidth(), 0.0f);
            rlEnd();
            rlPopMatrix();

            rlDisableTexture();
        }
    }



}
