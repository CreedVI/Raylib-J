package com.raylib.java.models;

import com.raylib.java.Raylib;
import com.raylib.java.core.rcamera.Camera3D;
import com.raylib.java.structs.*;

import static com.raylib.java.core.input.Keyboard.KEY_LEFT;
import static com.raylib.java.core.input.Keyboard.KEY_RIGHT;
import static com.raylib.java.core.input.Mouse.MouseButton.MOUSE_BUTTON_LEFT;
import static com.raylib.java.core.rcamera.Camera3D.CameraMode.CAMERA_FREE;
import static com.raylib.java.core.rcamera.Camera3D.CameraProjection.CAMERA_PERSPECTIVE;
import static com.raylib.java.models.rModels.MaterialMapIndex.MATERIAL_MAP_DIFFUSE;

public class MeshGeneration {

    static final int NUM_MODELS = 9;
    static Raylib rlj;

    public static void main(String[] args) {
        rlj = new Raylib(800, 600, "mesh generation");

        Camera3D camera = new Camera3D(rlj);
        camera.position = new Vector3(50.0f, 50.0f, 50.0f); // Camera position
        camera.target = new Vector3(0.0f, 10.0f, 0.0f);     // Camera looking at point
        camera.up = new Vector3(0.0f, 1.0f, 0.0f);          // Camera up vector (rotation towards target)
        camera.fovy = 45.0f;                                         // Camera field-of-view Y
        camera.projection = CAMERA_PERSPECTIVE;                      // Camera mode type

        Mesh gen = rlj.models.GenMeshSphere(2, 32, 32);
        Image check = rlj.textures.GenImageChecked(2, 2, 1, 1, Color.RED, Color.GREEN);
        Texture2D meshTexture = rlj.textures.LoadTextureFromImage(check);

        Model[] models = new Model[NUM_MODELS];

        models[0] = rlj.models.LoadModelFromMesh(rlj.models.GenMeshPlane(2, 2, 5, 5));
        models[1] = rlj.models.LoadModelFromMesh(rlj.models.GenMeshCube(2.0f, 1.0f, 2.0f));
        models[2] = rlj.models.LoadModelFromMesh(rlj.models.GenMeshSphere(2, 32, 32));
        models[3] = rlj.models.LoadModelFromMesh(rlj.models.GenMeshHemiSphere(2, 16, 16));
        models[4] = rlj.models.LoadModelFromMesh(rlj.models.GenMeshCylinder(1, 2, 16));
        models[5] = rlj.models.LoadModelFromMesh(rlj.models.GenMeshTorus(0.25f, 4.0f, 16, 32));
        models[6] = rlj.models.LoadModelFromMesh(rlj.models.GenMeshKnot(1.0f, 2.0f, 16, 128));
        models[7] = rlj.models.LoadModelFromMesh(rlj.models.GenMeshPoly(5, 2.0f));
        models[8] = rlj.models.LoadModelFromMesh(MakeMesh());

        for (int i = 0; i < NUM_MODELS; i++) {
            models[i].materials[0].maps[MATERIAL_MAP_DIFFUSE].texture = meshTexture;
        }

        int currentModel = 0;

        rlj.core.SetTargetFPS(60);

        while(!rlj.core.WindowShouldClose()) {
            camera.Update(CAMERA_FREE);

            if (rlj.core.IsMouseButtonPressed(MOUSE_BUTTON_LEFT)) {
                currentModel = (currentModel + 1)%NUM_MODELS; // Cycle between the textures
            }

            if (rlj.core.IsKeyPressed(KEY_RIGHT)) {
                currentModel++;
                if (currentModel >= NUM_MODELS) {
                    currentModel = 0;
                }
            }
            else if (rlj.core.IsKeyPressed(KEY_LEFT)) {
                currentModel--;
                if (currentModel < 0) {
                    currentModel = NUM_MODELS - 1;
                }
            }

            rlj.core.BeginDrawing();
            rlj.core.ClearBackground(Color.RAYWHITE);
            rlj.core.BeginMode3D(camera);

            rlj.models.DrawModel(models[currentModel], new Vector3(), 1.0f, Color.WHITE);

            rlj.core.EndMode3D();

            rlj.text.DrawText("Model: " + currentModel, 10, 10, 30, Color.BLACK);

            rlj.core.EndDrawing();
        }

    }

    static void AllocateMeshData(Mesh mesh, int triangleCount) {
        mesh.vertexCount = triangleCount * 3;
        mesh.triangleCount = triangleCount;

        mesh.vertices = new float[mesh.vertexCount * 3];
        mesh.texcoords = new float[mesh.vertexCount * 2];
        mesh.normals = new float[mesh.vertexCount * 3];
    }

    // generate a simple triangle mesh from code
    static Mesh MakeMesh() {
        Mesh mesh = new Mesh();
        AllocateMeshData(mesh, 1);

        // vertex at the origin
        mesh.vertices[0] = 0;
        mesh.vertices[1] = 0;
        mesh.vertices[2] = 0;
        mesh.normals[0] = 0;
        mesh.normals[1] = 1;
        mesh.normals[2] = 0;
        mesh.texcoords[0] = 0;
        mesh.texcoords[1] = 0;

        // vertex at 1,0,2
        mesh.vertices[3] = 1;
        mesh.vertices[4] = 0;
        mesh.vertices[5] = 2;
        mesh.normals[3] = 0;
        mesh.normals[4] = 1;
        mesh.normals[5] = 0;
        mesh.texcoords[2] = 0.5f;
        mesh.texcoords[3] = 1.0f;

        // vertex at 2,0,0
        mesh.vertices[6] = 2;
        mesh.vertices[7] = 0;
        mesh.vertices[8] = 0;
        mesh.normals[6] = 0;
        mesh.normals[7] = 1;
        mesh.normals[8] = 0;
        mesh.texcoords[4] = 1;
        mesh.texcoords[5] =0;

        rlj.models.UploadMesh(mesh, false);

        return mesh;
    }

}
