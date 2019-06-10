#include <jni.h>
#include <string>
#include <opencv2/opencv.hpp>
#include "include/System.h"
#include "Common.h"
#include "Plane.h"
#include "UIUtils.h"
#include "Matrix.h"

extern "C" {
std::string modelPath;

ORB_SLAM2::System *slamSys;
Plane *pPlane;
float fx,fy,cx,cy;
double timeStamp;
bool slamInited=false;
vector<ORB_SLAM2::MapPoint*> vMPs;
vector<cv::KeyPoint> vKeys;
cv::Mat Tcw;

const int PLANE_DETECTED=233;
const int PLANE_NOT_DETECTED=1234;

const bool DRAW_STATUS= false;

int processImage(cv::Mat &image,cv::Mat &outputImage,int statusBuf[]){
    timeStamp+=1/30.0;
    //Martin:about 140-700ms to finish last part
    LOGD("Started.");
    cv::Mat imgSmall;
    cv::resize(image,imgSmall,cv::Size(image.cols/2,image.rows/2));
    Tcw= slamSys->TrackMonocular(imgSmall,timeStamp);
    LOGD("finished. %d %d",imgSmall.rows,imgSmall.cols);
    //Martin:about 5ms to finish last part
    //Martin:about 5-10ms to await next image
    int status = slamSys->GetTrackingState();
    vMPs = slamSys->GetTrackedMapPoints();
    vKeys = slamSys->GetTrackedKeyPointsUn();
    if(DRAW_STATUS)
        printStatus(status,outputImage);
    drawTrackedPoints(vKeys,vMPs,outputImage);

    //cv::imwrite(modelPath+"/lala2.jpg",outputImage);
    if(status==2) {
        if(DRAW_STATUS){
            char buf[22];
            if(pPlane){
                sprintf(buf,"Plane Detected");
                cv::putText(outputImage,buf,cv::Point(10,30),cv::FONT_HERSHEY_PLAIN,1.5,cv::Scalar(255,0,255),2,8);
            }
            else {
                sprintf(buf,"Plane not detected.");
                cv::putText(outputImage,buf,cv::Point(10,30),cv::FONT_HERSHEY_PLAIN,1.5,cv::Scalar(255,255,0),2,8);
            }
        }
    }
    return status;
}

JNIEXPORT void JNICALL
Java_com_martin_ads_slamar_NativeHelper_initSLAM(JNIEnv *env, jobject instance,
                                                               jstring path_) {
    const char *path = env->GetStringUTFChars(path_, 0);

    if(slamInited) return;
    slamInited=true;
    modelPath=path;
    env->ReleaseStringUTFChars(path_, path);
    ifstream in;
    in.open(modelPath+"config.txt");
    string vocName,settingName;
    getline(in,vocName);
    getline(in,settingName);
    vocName=modelPath+vocName;
    settingName=modelPath+settingName;

    cv::FileStorage fSettings(settingName, cv::FileStorage::READ);
    fx = fSettings["Camera.fx"];
    fy = fSettings["Camera.fy"];
    cx = fSettings["Camera.cx"];
    cy = fSettings["Camera.cy"];

    timeStamp=0;
    LOGD("%s %c %s %c",vocName.c_str(),vocName[vocName.length()-1],settingName.c_str(),settingName[settingName.length()-1]);
    slamSys=new ORB_SLAM2::System(vocName,settingName,ORB_SLAM2::System::MONOCULAR);
}

JNIEXPORT void JNICALL
Java_com_martin_ads_slamar_NativeHelper_nativeProcessFrameMat(JNIEnv *env, jobject instance,
                                                              jlong matAddrGr, jlong matAddrRgba,
                                                              jintArray statusBuf_) {
    jint *statusBuf = env->GetIntArrayElements(statusBuf_, NULL);

    cv::Mat &mGr = *(cv::Mat *) matAddrGr;
    cv::Mat &mRgba = *(cv::Mat *) matAddrRgba;
    statusBuf[0]=processImage(mGr,mRgba,statusBuf);

    env->ReleaseIntArrayElements(statusBuf_, statusBuf, 0);
}
JNIEXPORT void JNICALL
Java_com_martin_ads_slamar_NativeHelper_detect(JNIEnv *env, jobject instance,
                                               jintArray statusBuf_) {
    jint *statusBuf = env->GetIntArrayElements(statusBuf_, NULL);
    if(!Tcw.empty()){
        pPlane=detectPlane(Tcw,vMPs,50);
        if(pPlane && slamSys->MapChanged())
            pPlane->Recompute();
        statusBuf[1]=pPlane? PLANE_DETECTED:PLANE_NOT_DETECTED;
    }
    env->ReleaseIntArrayElements(statusBuf_, statusBuf, 0);
}

JNIEXPORT void JNICALL
Java_com_martin_ads_slamar_NativeHelper_getM(JNIEnv *env, jobject instance, jfloatArray modelM_) {
    jfloat *modelM = env->GetFloatArrayElements(modelM_, NULL);

    getRUBModelMatrixFromRDF(pPlane->glTpw,modelM);

    env->ReleaseFloatArrayElements(modelM_, modelM, 0);
}
JNIEXPORT void JNICALL
Java_com_martin_ads_slamar_NativeHelper_getV(JNIEnv *env, jobject instance, jfloatArray viewM_) {
    jfloat *viewM = env->GetFloatArrayElements(viewM_, NULL);

    float tmpM[16];
    getColMajorMatrixFromMat(tmpM,Tcw);
    getRUBViewMatrixFromRDF(tmpM,viewM);

    env->ReleaseFloatArrayElements(viewM_, viewM, 0);
}
JNIEXPORT void JNICALL
Java_com_martin_ads_slamar_NativeHelper_getP(JNIEnv *env, jobject instance, jint imageWidth,
                                             jint imageHeight, jfloatArray projectionM_) {
    jfloat *projectionM = env->GetFloatArrayElements(projectionM_, NULL);

    //TODO:/2
    frustumM_RUB(imageWidth/2,imageHeight/2,fx,fy,cx,cy,0.1,1000,projectionM);

    env->ReleaseFloatArrayElements(projectionM_, projectionM, 0);
}
}