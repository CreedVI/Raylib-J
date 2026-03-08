package com.raylib.java.core;

import com.raylib.java.Raylib;
import com.raylib.java.core.rcamera.Camera2D;
import com.raylib.java.raymath.Raymath;
import com.raylib.java.structs.*;

import static com.raylib.java.core.input.Keyboard.*;

public class Core2DCameraPlatformer{

    /*******************************************************************************************
     *
     *   raylib-j [core] example - 2d camera platformer
     *
     *   This example has been created using raylib-j (version 0.4)
     *   Ported by CreedVI
     *   https://github.com/creedvi/raylib-j
     *
     *   raylib is licensed under an unmodified zlib/libpng license
     *   Original example contributed by arvyy (@arvyy) and reviewed by Ramon Santamaria (@raysan5)
     *   https://github.com/raysan5
     *
     *   Copyright (c) 2019 arvyy (@arvyy)
     *
     ********************************************************************************************/

    static Raylib rlj;
    final static int G = 400;
    final static float PLAYER_JUMP_SPD = 350.0f;
    final static float PLAYER_HOR_SPD = 200.0f;

    static class Player {
        Vector2 position;
        float speed;
        boolean canJump;
    }

    static class EnvItem {
        Rectangle rect;
        boolean blocking;
        Color color;

        public EnvItem(Rectangle rectangle, boolean b, Color color){
            this.rect = rectangle;
            this.blocking = b;
            this.color = color;
        }
    }

    enum CameraUpdater{
        UpdateCameraCenter(0),
        UpdateCameraCenterInsideMap(1),
        UpdateCameraCenterSmoothFollow(2),
        UpdateCameraEvenOutOnLanding(3),
        UpdateCameraPlayerBoundsPush(4);

        final int updater;
        CameraUpdater(int i){
            updater = i;
        }

        public static CameraUpdater getByInt(int cameraOption){
            for (CameraUpdater cu : values()){
                if(cu.updater == cameraOption){
                    return cu;
                }
            }
            return UpdateCameraCenter;
        }
    }

    public static void main(String[] args){

        // Initialization
        //--------------------------------------------------------------------------------------
        final int screenWidth = 800;
        final int screenHeight = 450;
        rlj = new Raylib(screenWidth, screenHeight, "raylib-j [core] example - 2d camera");

        Player player = new Player();
        player.position = new Vector2(400, 280);
        player.speed = 0;
        player.canJump = false;
        EnvItem[] envItems = {
                new EnvItem(new Rectangle(0, 0, 1000, 400), false, Color.LIGHTGRAY),
                new EnvItem(new Rectangle(0, 400, 1000, 200 ), true, Color.GRAY),
                new EnvItem(new Rectangle(300, 200, 400, 10), true, Color.GRAY),
                new EnvItem(new Rectangle(250, 300, 100, 10), true, Color.GRAY),
                new EnvItem(new Rectangle(650, 300, 100, 10), true, Color.GRAY)
        };

        int envItemsLength = envItems.length;

        Camera2D camera = new Camera2D();
        camera.target = player.position;
        camera.offset = new Vector2(screenWidth / 2.0f, screenHeight / 2.0f);
        camera.rotation = 0.0f;
        camera.zoom = 1.0f;

        int cameraOption = 0;

        String[] cameraDescriptions = {
            "Follow player center",
            "Follow player center, but clamp to map edges",
            "Follow player center; smoothed",
            "Follow player center horizontally; update player center vertically after landing",
            "Player push camera on getting too close to screen edge"
        };

        rlj.core.SetTargetFPS(60);
        //--------------------------------------------------------------------------------------

        // Main game loop
        while (!rlj.core.WindowShouldClose())
        {
            // Update
            //----------------------------------------------------------------------------------
            float deltaTime = rlj.core.GetFrameTime();

            UpdatePlayer(player, envItems, envItemsLength, deltaTime);

            camera.zoom += (rlj.core.GetMouseWheelMove()*0.05f);

            if (camera.zoom > 3.0f) camera.zoom = 3.0f;
            else if (camera.zoom < 0.25f) camera.zoom = 0.25f;

            if (rlj.core.IsKeyPressed(KEY_R))
            {
                camera.zoom = 1.0f;
                player.position = new Vector2(400, 280);
            }

            if (rlj.core.IsKeyPressed(KEY_C)){
                cameraOption = (cameraOption == 4 ? 0 : (cameraOption + 1));
            }

            switch (CameraUpdater.getByInt(cameraOption)) {
                case UpdateCameraCenterInsideMap ->
                        UpdateCameraCenterInsideMap(camera, player, envItems, envItemsLength);
                case UpdateCameraCenterSmoothFollow ->
                        UpdateCameraCenterSmoothFollow(camera, player, deltaTime);
                case UpdateCameraEvenOutOnLanding ->
                        //todo: fix
                        UpdateCameraEvenOutOnLanding(camera, player, deltaTime);
                case UpdateCameraPlayerBoundsPush ->
                        //todo: fix
                        UpdateCameraPlayerBoundsPush(camera, player);
                default ->
                        UpdateCameraCenter(camera, player);
            }
            //----------------------------------------------------------------------------------

            // Draw
            //----------------------------------------------------------------------------------
            rlj.core.BeginDrawing();

            rlj.core.ClearBackground(Color.LIGHTGRAY);

            rlj.core.BeginMode2D(camera);

            for (EnvItem envItem : envItems) {
                rlj.shapes.DrawRectangleRec(envItem.rect, envItem.color);
            }

            Rectangle playerRect = new Rectangle(player.position.x - 20, player.position.y - 40, 40, 40);
            rlj.shapes.DrawRectangleRec(playerRect, Color.RED);

            rlj.core.EndMode2D();

            rlj.text.DrawText("Controls:", 20, 20, 10, Color.BLACK);
            rlj.text.DrawText("- Right/Left to move", 40, 40, 10, Color.DARKGRAY);
            rlj.text.DrawText("- Space to jump", 40, 60, 10, Color.DARKGRAY);
            rlj.text.DrawText("- Mouse Wheel to Zoom in-out, R to reset zoom", 40, 80, 10, Color.DARKGRAY);
            rlj.text.DrawText("- C to change camera mode", 40, 100, 10, Color.DARKGRAY);
            rlj.text.DrawText("Current camera mode:", 20, 120, 10, Color.BLACK);
            rlj.text.DrawText(cameraDescriptions[cameraOption], 40, 140, 10, Color.DARKGRAY);

            rlj.core.EndDrawing();
            //----------------------------------------------------------------------------------
        }
    }

    private static void UpdatePlayer(Player player, EnvItem[] envItems, int envItemsLength, float delta) {
        if (rlj.core.IsKeyDown(KEY_LEFT)) player.position.x -= PLAYER_HOR_SPD*delta;
        if (rlj.core.IsKeyDown(KEY_RIGHT)) player.position.x += PLAYER_HOR_SPD*delta;
        if (rlj.core.IsKeyDown(KEY_SPACE) && player.canJump) {
            player.speed = -PLAYER_JUMP_SPD;
            player.canJump = false;
        }

        boolean hitObstacle = false;
        for (int i = 0; i < envItemsLength; i++) {
            EnvItem ei = envItems[i];
            Vector2 p = (player.position);
            if (ei.blocking && (ei.rect.x <= p.x) && ei.rect.x + ei.rect.width >= p.x && ei.rect.y >= p.y &&
                    ei.rect.y < p.y + player.speed*delta) {
                hitObstacle = true;
                player.speed = 0.0f;
                p.y = ei.rect.y;
            }
        }

        if (!hitObstacle) {
            player.position.y += player.speed*delta;
            player.speed += G*delta;
            player.canJump = false;
        }
        else player.canJump = true;
    }

    private static void UpdateCameraCenter(Camera2D camera, Player player) {
        camera.offset = new Vector2(800 / 2.0f, 450 / 2.0f);
        camera.target = player.position;
    }

    private static void UpdateCameraCenterInsideMap(Camera2D camera, Player player, EnvItem[] envItems, int envItemsLength) {
        camera.target = player.position;
        camera.offset = new Vector2(800 / 2.0f, 450 / 2.0f);
        float minX = 1000, minY = 1000, maxX = -1000, maxY = -1000;

        for (int i = 0; i < envItemsLength; i++) {
            EnvItem ei = envItems[i];
            minX = Math.min(ei.rect.x, minX);
            maxX = Math.max(ei.rect.x + ei.rect.width, maxX);
            minY = Math.min(ei.rect.y, minY);
            maxY = Math.max(ei.rect.y + ei.rect.height, maxY);
        }

        Vector2 max = rlj.core.GetWorldToScreen2D(new Vector2(maxX, maxY), camera);
        Vector2 min = rlj.core.GetWorldToScreen2D(new Vector2(minX, minY), camera);

        if (max.x < 800) camera.offset.x = 800 - (max.x - 800 / 2.0f);
        if (max.y < 450) camera.offset.y = 450 - (max.y - 450 / 2.0f);
        if (min.x > 0) camera.offset.x = 800 / 2.0f - min.x;
        if (min.y > 0) camera.offset.y = 450 / 2.0f - min.y;
    }

    private static void UpdateCameraCenterSmoothFollow(Camera2D camera, Player player, float delta) {
        float minSpeed = 30;
        float minEffectLength = 10;
        float fractionSpeed = 0.8f;

        camera.offset = new Vector2(800 / 2.0f, 450 / 2.0f);
        Vector2 diff = Raymath.Vector2Subtract(player.position, camera.target);
        float length = Raymath.Vector2Length(diff);

        if (length > minEffectLength) {
            float speed = Math.max(fractionSpeed*length, minSpeed);
            camera.target = Raymath.Vector2Add(camera.target, Raymath.Vector2Scale(diff, speed*delta/length));
        }
    }

    private static void UpdateCameraEvenOutOnLanding(Camera2D camera, Player player, float delta) {
        float evenOutSpeed = 700;
        boolean eveningOut = false;
        float evenOutTarget = 0;

        camera.offset = new Vector2(800 / 2.0f, 450 / 2.0f);
        camera.target.x = player.position.x;

        if (eveningOut)
        {
            if (evenOutTarget > camera.target.y)
            {
                camera.target.y += evenOutSpeed*delta;

                if (camera.target.y > evenOutTarget)
                {
                    camera.target.y = evenOutTarget;
                    eveningOut = false;
                }
            }
            else
            {
                camera.target.y -= evenOutSpeed*delta;

                if (camera.target.y < evenOutTarget)
                {
                    camera.target.y = evenOutTarget;
                    eveningOut = false;
                }
            }
        }
        else
        {
            if (player.canJump && (player.speed == 0) && (player.position.y != camera.target.y))
            {
                eveningOut = true;
                evenOutTarget = player.position.y;
            }
        }
    }

    private static void UpdateCameraPlayerBoundsPush(Camera2D camera, Player player) {
        Vector2 bbox = new Vector2(0.2f, 0.2f);

        Vector2 bboxWorldMin =
                rlj.core.GetScreenToWorld2D(new Vector2((1 - bbox.x)*0.5f* 800, (1 - bbox.y)*0.5f* 450), camera);
        Vector2 bboxWorldMax =
                rlj.core.GetScreenToWorld2D(new Vector2((1 + bbox.x)*0.5f* 800, (1 + bbox.y)*0.5f* 450), camera);
        camera.offset = new Vector2((1 - bbox.x)*0.5f * 800, (1 - bbox.y)*0.5f* 450);

        if (player.position.x < bboxWorldMin.x) camera.target.x = player.position.x;
        if (player.position.y < bboxWorldMin.y) camera.target.y = player.position.y;
        if (player.position.x > bboxWorldMax.x) camera.target.x = bboxWorldMin.x + (player.position.x - bboxWorldMax.x);
        if (player.position.y > bboxWorldMax.y) camera.target.y = bboxWorldMin.y + (player.position.y - bboxWorldMax.y);
    }
}