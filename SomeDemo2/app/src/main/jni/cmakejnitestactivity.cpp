#include <scene.h>
#include "cmakejnitestactivity.h"
//#include "../cpp/externals/assimp/include/assimp/Importer.hpp"
#include "android/log.h"
#include "Importer.hpp"

using namespace Assimp;

#define TAG "cmakejnitestactivity"
#define aiProcessPreset_TargetRealtime_Quality ( \
    aiProcess_CalcTangentSpace              |  \
    aiProcess_GenSmoothNormals              |  \
    aiProcess_JoinIdenticalVertices         |  \
    aiProcess_ImproveCacheLocality          |  \
    aiProcess_LimitBoneWeights              |  \
    aiProcess_RemoveRedundantMaterials      |  \
    aiProcess_SplitLargeMeshes              |  \
    aiProcess_Triangulate                   |  \
    aiProcess_GenUVCoords                   |  \
    aiProcess_SortByPType                   |  \
    aiProcess_FindDegenerates               |  \
    aiProcess_FindInvalidData               |  \
    0 )

JNIEXPORT jint JNICALL
Java_com_example_admin_somedemo_cmakejnidemo_CmakeJniTestActivity_initAssimp(JNIEnv *env,
                                                                             jobject instance) {

    LOGD("chenliang initAssimp jni success go start");
    Assimp::Importer *a = new Assimp::Importer();
    const aiScene *scene = a->ReadFile("/sdcard/cow.dae", 5);
//    __android_log_print(ANDROID_LOG_DEBUG, "cmakejnitestactivity", "chenliang jni end");
//    Importer *a = new Importer();
    LOGD("chenliang aiScene mNumMaterials is %d,mNumLights:%d", scene->mNumMaterials,
         scene->mNumLights);
    LOGD("chenliang initAssimp jni success go end");
    return 123;
}