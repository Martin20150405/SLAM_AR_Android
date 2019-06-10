//
// Created by Ads on 2017/3/5.
//

#ifndef ORB_SLAM_AR_MATRIX_H
#define ORB_SLAM_AR_MATRIX_H

struct Quaternion{
    float x,y,z,w;
    void copyTo(float outM[],int offset){
        outM[offset+0]=x;
        outM[offset+1]=y;
        outM[offset+2]=z;
        outM[offset+3]=w;
    }
};

void multiplyMM(float* r, const float* lhs, const float* rhs);

void setRotateM(float rm[], int rmOffset,
                float a, float x, float y, float z);

void rotateM(float rm[], float m[],
             float a, float x, float y, float z);

void frustumM(float m[], int offset,
              float left, float right, float bottom, float top,
              float nearZ, float farZ);

void frustumM_RUB(int w, int h, double fu, double fv, double u0, double v0, double zNear, double zFar ,float projectionMatrix[]);

void setIdentityM(float m[]);

bool invertM(float mInv[], int mInvOffset, float m[],
             int mOffset);

void getRUBViewMatrixFromRDF(float inM[],float outM[]);

void getRUBModelMatrixFromRDF(float inM[],float outM[]);

void getEulerAnglesFromMatrix(float M[],float outM[],int offset);

void quaternionToMatrix(float M[],Quaternion &q);

void matrixToQuaternion(float M[],Quaternion &q);

void quaternionU3DFromMatrix(float M[],Quaternion &q);

void prepareModelM(float inM[],float outM[],const float size, const float x=0, const float y=0, const float z=0);

void logMatrix(float f[]);
#endif //ORB_SLAM_AR_MATRIX_H
