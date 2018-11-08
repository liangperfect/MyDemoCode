/*
 *    Copyright 2016 Anand Muralidhar
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

#include "assimpLoader.h"
#include "myShader.h"
#include "misc.h"
#include <opencv2/opencv.hpp>
#include <cmath>
#include <cstdlib>

unsigned char RD(int i, int j) {
    double a = 0, b = 0, c, d, n = 0;
    while ((c = a * a) + (d = b * b) < 4 && n++ < 880) {
        b = 2 * a * b + j * 8e-9 - .645411;
        a = c - d + i * 8e-9 + .356888;
    }
    return 255 * pow((n - 80) / 800, 3.);
}

unsigned char GR(int i, int j) {
    double a = 0, b = 0, c, d, n = 0;
    while ((c = a * a) + (d = b * b) < 4 && n++ < 880) {
        b = 2 * a * b + j * 8e-9 - .645411;
        a = c - d + i * 8e-9 + .356888;
    }
    return 255 * pow((n - 80) / 800, .7);
}

unsigned char BL(int i, int j) {
    double a = 0, b = 0, c, d, n = 0;
    while ((c = a * a) + (d = b * b) < 4 && n++ < 880) {
        b = 2 * a * b + j * 8e-9 - .645411;
        a = c - d + i * 8e-9 + .356888;
    }
    return 255 * pow((n - 80) / 800, .5);
}

/**
 * Class constructor, loads shaders & gets locations of variables in them
 */
AssimpLoader::AssimpLoader() {
    importerPtr = new Assimp::Importer;
    scene = NULL;
    isObjectLoaded = false;

    // shader related setup -- loading, attribute and uniform locations
    std::string vertexShader = "shaders/modelTextured.vsh";
    std::string fragmentShader = "shaders/modelTextured.fsh";
    shaderProgramID = LoadShaders(vertexShader, fragmentShader);
    vertexAttribute = GetAttributeLocation(shaderProgramID, "vertexPosition");
    vertexUVAttribute = GetAttributeLocation(shaderProgramID, "vertexUV");
    mvpLocation = GetUniformLocation(shaderProgramID, "mvpMat");
    textureSamplerLocation = GetUniformLocation(shaderProgramID, "textureSampler");

    CheckGLError("AssimpLoader::AssimpLoader");
}

/**
 * Class destructor, deletes Assimp importer pointer and removes 3D model from GL
 */
AssimpLoader::~AssimpLoader() {
    Delete3DModel();
    if (importerPtr) {
        delete importerPtr;
        importerPtr = NULL;
    }
    scene = NULL; // gets deleted along with importerPtr
}

/**
 * Generate buffers for vertex positions, texture coordinates, faces -- and load data into them
 */
void AssimpLoader::GenerateGLBuffers() {

    struct MeshInfo newMeshInfo; // this struct is updated for each mesh in the model
    GLuint buffer;

    // For every mesh -- load face indices, vertex positions, vertex texture coords
    // also copy texture index for mesh into newMeshInfo.textureIndex
    for (unsigned int n = 0; n < scene->mNumMeshes; ++n) {

        const aiMesh *mesh = scene->mMeshes[n]; // read the n-th mesh

        // create array with faces
        // convert from Assimp's format to array for GLES
        unsigned int *faceArray = new unsigned int[mesh->mNumFaces * 3];
        unsigned int faceIndex = 0;
        for (unsigned int t = 0; t < mesh->mNumFaces; ++t) {

            // read a face from assimp's mesh and copy it into faceArray
            const aiFace *face = &mesh->mFaces[t];
            memcpy(&faceArray[faceIndex], face->mIndices, 3 * sizeof(unsigned int));
            faceIndex += 3;

        }
        newMeshInfo.numberOfFaces = scene->mMeshes[n]->mNumFaces;

        // buffer for faces
        if (newMeshInfo.numberOfFaces) {

            glGenBuffers(1, &buffer);
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, buffer);
            glBufferData(GL_ELEMENT_ARRAY_BUFFER,
                         sizeof(unsigned int) * mesh->mNumFaces * 3, faceArray,
                         GL_STATIC_DRAW);
            newMeshInfo.faceBuffer = buffer;

        }
        delete[] faceArray;

        // buffer for vertex positions
        if (mesh->HasPositions()) {

            glGenBuffers(1, &buffer);
            glBindBuffer(GL_ARRAY_BUFFER, buffer);
            glBufferData(GL_ARRAY_BUFFER,
                         sizeof(float) * 3 * mesh->mNumVertices, mesh->mVertices,
                         GL_STATIC_DRAW);
            newMeshInfo.vertexBuffer = buffer;

        }

        // buffer for vertex texture coordinates
        // ***ASSUMPTION*** -- handle only one texture for each mesh
        if (mesh->HasTextureCoords(0)) {

            float *textureCoords = new float[2 * mesh->mNumVertices];
            for (unsigned int k = 0; k < mesh->mNumVertices; ++k) {
                textureCoords[k * 2] = mesh->mTextureCoords[0][k].x;
                textureCoords[k * 2 + 1] = mesh->mTextureCoords[0][k].y;
            }
            glGenBuffers(1, &buffer);
            glBindBuffer(GL_ARRAY_BUFFER, buffer);
            glBufferData(GL_ARRAY_BUFFER,
                         sizeof(float) * 2 * mesh->mNumVertices, textureCoords,
                         GL_STATIC_DRAW);
            newMeshInfo.textureCoordBuffer = buffer;
            delete[] textureCoords;

        }

        // unbind buffers
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

        // copy texture index (= texture name in GL) for the mesh from textureNameMap
        aiMaterial *mtl = scene->mMaterials[mesh->mMaterialIndex];
        aiString texturePath;    //contains filename of texture
        if (AI_SUCCESS == mtl->GetTexture(aiTextureType_DIFFUSE, 0, &texturePath)) {
            unsigned int textureId = textureNameMap[texturePath.data];
            newMeshInfo.textureIndex = textureId;
        } else {
            newMeshInfo.textureIndex = 0;
        }

        modelMeshes.push_back(newMeshInfo);
    }
}

/**
 * Read textures associated with all materials and load images to GL
 */
bool AssimpLoader::LoadTexturesToGL(std::string modelFilename) {

    // read names of textures associated with all materials
    textureNameMap.clear();

    for (unsigned int m = 0; m < scene->mNumMaterials; ++m) {

        int textureIndex = 0;
        aiString textureFilename;
        aiReturn isTexturePresent = scene->mMaterials[m]->GetTexture(aiTextureType_DIFFUSE,
                                                                     textureIndex,
                                                                     &textureFilename);
        while (isTexturePresent == AI_SUCCESS) {
            //fill map with textures, OpenGL image ids set to 0
            textureNameMap.insert(std::pair<std::string, GLuint>(textureFilename.data, 0));

            // more textures? more than one texture could be associated with a material
            textureIndex++;
            isTexturePresent = scene->mMaterials[m]->GetTexture(aiTextureType_DIFFUSE,
                                                                textureIndex, &textureFilename);
        }
    }

    int numTextures = (int) textureNameMap.size();
    MyLOGI("Total number of textures is %d ", numTextures);

    // create and fill array with texture names in GL
    GLuint *textureGLNames = new GLuint[numTextures];
    glGenTextures(numTextures, textureGLNames);

    // Extract the directory part from the file name
    // will be used to read the texture
    std::string modelDirectoryName = GetDirectoryName(modelFilename);

    // iterate over the textures, read them using OpenCV, load into GL
    std::map<std::string, GLuint>::iterator textureIterator = textureNameMap.begin();
    int i = 0;
    for (; textureIterator != textureNameMap.end(); ++i, ++textureIterator) {

        std::string textureFilename = (*textureIterator).first;  // get filename
        std::string textureFullPath = modelDirectoryName + "/" + textureFilename;
        (*textureIterator).second = textureGLNames[i];      // save texture id for filename in map

        // load the texture using OpenCV
        MyLOGI("Loading texture %s", textureFullPath.c_str());
//        cv::Mat textureImage = cv::imread(textureFullPath);
        AUX_RGBImageRec *textureImage = LoadGLTextures(textureFullPath);
//        AUX_RGBImageRec *textureImage = LoadTGA(textureFullPath);
        if (textureImage && textureImage->notEmpty) {
            // bind the texture
//            glActiveTexture(textureGLNames[i]);
            glBindTexture(GL_TEXTURE_2D, textureGLNames[i]);
            // specify linear filtering
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
//            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
//            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
            // load the OpenCV Mat into GLES
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, textureImage->sizeX,
                         textureImage->sizeY, 0, GL_RGB, GL_UNSIGNED_BYTE,
                         textureImage->data);
            CheckGLError("AssimpLoader::loadGLTexGen");
        } else {
            MyLOGE("Couldn't load texture %s", textureFilename.c_str());

            //Cleanup and return
            delete[] textureGLNames;
            return false;
        }
        /* if (!textureImage.empty()) {

             // opencv reads textures in BGR format, change to RGB for GL
             cv::cvtColor(textureImage, textureImage, CV_BGR2RGB);
             // opencv reads image from top-left, while GL expects it from bottom-left
             // vertically flip the image
             cv::flip(textureImage, textureImage, 0);

             // bind the texture
             glBindTexture(GL_TEXTURE_2D, textureGLNames[i]);
             // specify linear filtering
             glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
             glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
             // load the OpenCV Mat into GLES
             glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, textureImage.cols,
                          textureImage.rows, 0, GL_RGB, GL_UNSIGNED_BYTE,
                          textureImage.data);
             CheckGLError("AssimpLoader::loadGLTexGen");

         } else {

             MyLOGE("Couldn't load texture %s", textureFilename.c_str());

             //Cleanup and return
             delete[] textureGLNames;
             return false;

         }*/
    }

    //Cleanup and return
    delete[] textureGLNames;
    return true;
}

/**
 * Loads a general OBJ with many meshes -- assumes texture is associated with each mesh
 * does not handle material properties (like diffuse, specular, etc.)
 */
bool AssimpLoader::Load3DModel(std::string modelFilename) {
    MyLOGI("Scene will be imported now");
    MyLOGD("chenliang load3DModel is %s", modelFilename.c_str());
    scene = importerPtr->ReadFile(modelFilename, aiProcessPreset_TargetRealtime_Quality);

    // Check if import failed
    if (!scene) {
        std::string errorString = importerPtr->GetErrorString();
        MyLOGE("Scene import failed: %s", errorString.c_str());
        return false;
    }
    MyLOGI("Imported %s successfully.", modelFilename.c_str());

    if (!LoadTexturesToGL(modelFilename)) {
        MyLOGE("Unable to load textures");
        return false;
    }
    MyLOGI("Loaded textures successfully");

    GenerateGLBuffers();
    MyLOGI("Loaded vertices and texture coords successfully");

    isObjectLoaded = true;
    return true;
}

/**
 * Clears memory associated with the 3D model
 */
void AssimpLoader::Delete3DModel() {
    if (isObjectLoaded) {
        // clear modelMeshes stuff
//        for (unsigned int i = 0; i < modelMeshes.size(); ++i) {
//            glDeleteTextures(1, &(modelMeshes[i].textureIndex));
//        }
        modelMeshes.clear();

        MyLOGI("Deleted Assimp object");
        isObjectLoaded = false;
    }
}

AssimpLoader::AUX_RGBImageRec *AssimpLoader::LoadTGA(std::string filename) {
    AUX_RGBImageRec *TGAImage = new AUX_RGBImageRec;
    GLubyte TGAheader[12] = {0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0}; //Uncompressed TGA Header，图像类型码 置2
    GLubyte TGAcompare[12];        //Used To Compare TGA Header
    GLubyte header[6];         //First 6 Useful Bytes From The Header
    GLuint bytesPerPixel;        //Holds Number Of Bytes Per Pixel Used In The TGA File
    GLuint imageSize;         //Used To Store The Image Size When Setting Aside Ram
    GLuint temp;          //Temporary Variable

    FILE *file = fopen(filename.c_str(), "rb");      //Open The TGA File

    if (file == NULL ||          //Does File Even Exist?
        fread(TGAcompare, 1, sizeof(TGAcompare), file) != sizeof(TGAcompare) ||
        // Are There 12 Bytes To Read?
        memcmp(TGAheader, TGAcompare, sizeof(TGAheader)) != 0 ||
        //Does The Header Match What We Want?
        fread(header, 1, sizeof(header), file) !=
        sizeof(header))    //If So Read Next 6 Header Bytes
    {
        if (file == NULL) {        //Did The File Even Exist? *Added Jim Strong*
            TGAImage->notEmpty = false;
//            return false;
        }       //Return False
        else {
            fclose(file);         //If Anything Failed, Close The File
            TGAImage->notEmpty = false;
//            return false;         //Return False
        }
    }

    TGAImage->sizeX =
            header[1] * 256 + header[0];   // 宽和高以16位字保存，所以要读高位和地位Width (highbyte*256+lowbyte)
    TGAImage->sizeY =
            header[3] * 256 + header[2];   //Determine The TGAHeight (highbyte*256+lowbyte)


    if (TGAImage->sizeX <= 0 ||        //Is The Width Less Than Or Equal To Zero
        TGAImage->sizeY <= 0 ||        //Is The Height Less Than Or Equal To Zero
        (header[4] != 24 && header[4] != 32))     //Is The TGA 24 or 32 Bit? RGB或RGBA
    {
        fclose(file);          //If Anything Failed, Close The File
        TGAImage->notEmpty = false;
//        return false;          //Return False
    }

    bytesPerPixel = header[4] / 8;      //Divide By 8 To Get The Bytes Per Pixel
    imageSize = TGAImage->sizeX * TGAImage->sizeY *
                bytesPerPixel; //Calculate The Memory Required For The TGA Data

    TGAImage->data = (GLubyte *) malloc(imageSize);  // ReserveMemory To Hold The TGA Data

    if (TGAImage->data == NULL ||       //Does The Storage Memory Exist?
        fread(TGAImage->data, 1, imageSize, file) !=
        imageSize) // Does The ImageSize Match The Memory Reserved?
    {
        if (TGAImage->data != NULL)      //Was Image Data Loaded
            free(TGAImage->data);      //If So, Release The Image Data

        fclose(file);          //Close The File
        TGAImage->notEmpty = false;
//        return false;          //Return False
    }

    for (GLuint i = 0; i < int(imageSize); i += bytesPerPixel)  // LoopThrough The Image Data
    {              //Swaps The 1st And 3rd Bytes ('R'ed and 'B'lue)
        temp = TGAImage->data[i];       //Temporarily Store The Value At Image Data 'i'
        TGAImage->data[i] = TGAImage->data[i + 2]; // Set The1st Byte To The Value Of The 3rd Byte
        TGAImage->data[i + 2] = temp;     //Set The 3rd Byte To The Value In 'temp' (1st Byte Value)
    }

    fclose(file);           //Close The File

    return TGAImage;
}

/**
 * Renders the 3D model by rendering every mesh in the object
 */
void AssimpLoader::Render3DModel(glm::mat4 *mvpMat) {

    if (!isObjectLoaded) {
        return;
    }

    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

    glUseProgram(shaderProgramID);
    glUniformMatrix4fv(mvpLocation, 1, GL_FALSE, (const GLfloat *) mvpMat);

    glActiveTexture(GL_TEXTURE0);
    glUniform1i(textureSamplerLocation, 0);

    unsigned int numberOfLoadedMeshes = modelMeshes.size();

    // render all meshes
    for (unsigned int n = 0; n < numberOfLoadedMeshes; ++n) {

        // Texture
        if (modelMeshes[n].textureIndex) {
            glBindTexture(GL_TEXTURE_2D, modelMeshes[n].textureIndex);
        }

        // Faces
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, modelMeshes[n].faceBuffer);

        // Vertices
        glBindBuffer(GL_ARRAY_BUFFER, modelMeshes[n].vertexBuffer);
        glEnableVertexAttribArray(vertexAttribute);
        glVertexAttribPointer(vertexAttribute, 3, GL_FLOAT, 0, 0, 0);

        // Texture coords
        glBindBuffer(GL_ARRAY_BUFFER, modelMeshes[n].textureCoordBuffer);
        glEnableVertexAttribArray(vertexUVAttribute);
        glVertexAttribPointer(vertexUVAttribute, 2, GL_FLOAT, 0, 0, 0);

        glDrawElements(GL_TRIANGLES, modelMeshes[n].numberOfFaces * 3, GL_UNSIGNED_INT, 0);

        // unbind buffers
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

    }

    CheckGLError("AssimpLoader::renderObject() ");

}

AssimpLoader::AUX_RGBImageRec *AssimpLoader::LoadGLTextures(std::string path) {

    AUX_RGBImageRec *rec = new AUX_RGBImageRec;
    unsigned int imgW = 512;
    unsigned int imgH = 512;
    unsigned int colorNum = 3;
    rec->sizeX = imgW;
    rec->sizeY = imgH;
    rec->data = (unsigned char *) malloc(imgW * imgH * colorNum);

    for (int i = 0; i < imgH; ++i) {
        for (int j = 0; j < imgW; ++j) {
            rec->data[i * imgW + j * 3 + 0] = RD(i, j) & 255;
            rec->data[i * imgW + j * 3 + 1] = GR(i, j) & 255;
            rec->data[i * imgW + j * 3 + 2] = BL(i, j) & 255;
        }
    }
    return rec;

}
