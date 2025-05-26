#ifndef EDGE_DETECTOR_H
#define EDGE_DETECTOR_H

#include <jni.h>
#include <opencv2/opencv.hpp>

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT void JNICALL Java_com_example_edgedetectionviewer_GLRenderer_processFrame(
    JNIEnv *env,
    jobject thiz,
    jint texIn,
    jint width,
    jint height
);

#ifdef __cplusplus
}
#endif

#endif // EDGE_DETECTOR_H