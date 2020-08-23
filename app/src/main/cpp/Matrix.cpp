//
// Created by Ads on 2017/3/5.
//

#include "Matrix.h"
#include <cmath>
#include <cstring>
#include <string>
#include <algorithm>
#include <include/Common.h>

const float PI= (const float) acos(-1);
#define I(_i, _j) ((_j)+ 4*(_i))
float sTemp[16];

void multiplyMM(float* r, const float* lhs, const float* rhs) {
    for (int i=0 ; i<4 ; i++) {
        const float rhs_i0 = rhs[ I(i,0) ];
        float ri0 = lhs[ I(0,0) ] * rhs_i0;
        float ri1 = lhs[ I(0,1) ] * rhs_i0;
        float ri2 = lhs[ I(0,2) ] * rhs_i0;
        float ri3 = lhs[ I(0,3) ] * rhs_i0;
        for (int j=1 ; j<4 ; j++) {
            const float rhs_ij = rhs[ I(i,j) ];
            ri0 += lhs[ I(j,0) ] * rhs_ij;
            ri1 += lhs[ I(j,1) ] * rhs_ij;
            ri2 += lhs[ I(j,2) ] * rhs_ij;
            ri3 += lhs[ I(j,3) ] * rhs_ij;
        }
        r[ I(i,0) ] = ri0;
        r[ I(i,1) ] = ri1;
        r[ I(i,2) ] = ri2;
        r[ I(i,3) ] = ri3;
    }
}

float length(float x, float y, float z) {
    return (float) sqrt(x * x + y * y + z * z);
}

void setRotateM(float rm[], int rmOffset,
                float a, float x, float y, float z) {
    rm[rmOffset + 3] = 0;
    rm[rmOffset + 7] = 0;
    rm[rmOffset + 11]= 0;
    rm[rmOffset + 12]= 0;
    rm[rmOffset + 13]= 0;
    rm[rmOffset + 14]= 0;
    rm[rmOffset + 15]= 1;
    a *= (float) (PI / 180.0f);
    float s = (float) sin(a);
    float c = (float) cos(a);
    if (1.0f == x && 0.0f == y && 0.0f == z) {
        rm[rmOffset + 5] = c;   rm[rmOffset + 10]= c;
        rm[rmOffset + 6] = s;   rm[rmOffset + 9] = -s;
        rm[rmOffset + 1] = 0;   rm[rmOffset + 2] = 0;
        rm[rmOffset + 4] = 0;   rm[rmOffset + 8] = 0;
        rm[rmOffset + 0] = 1;
    } else if (0.0f == x && 1.0f == y && 0.0f == z) {
        rm[rmOffset + 0] = c;   rm[rmOffset + 10]= c;
        rm[rmOffset + 8] = s;   rm[rmOffset + 2] = -s;
        rm[rmOffset + 1] = 0;   rm[rmOffset + 4] = 0;
        rm[rmOffset + 6] = 0;   rm[rmOffset + 9] = 0;
        rm[rmOffset + 5] = 1;
    } else if (0.0f == x && 0.0f == y && 1.0f == z) {
        rm[rmOffset + 0] = c;   rm[rmOffset + 5] = c;
        rm[rmOffset + 1] = s;   rm[rmOffset + 4] = -s;
        rm[rmOffset + 2] = 0;   rm[rmOffset + 6] = 0;
        rm[rmOffset + 8] = 0;   rm[rmOffset + 9] = 0;
        rm[rmOffset + 10]= 1;
    } else {
        float len = length(x, y, z);
        if (1.0f != len) {
            float recipLen = 1.0f / len;
            x *= recipLen;
            y *= recipLen;
            z *= recipLen;
        }
        float nc = 1.0f - c;
        float xy = x * y;
        float yz = y * z;
        float zx = z * x;
        float xs = x * s;
        float ys = y * s;
        float zs = z * s;
        rm[rmOffset +  0] = x*x*nc +  c;
        rm[rmOffset +  4] =  xy*nc - zs;
        rm[rmOffset +  8] =  zx*nc + ys;
        rm[rmOffset +  1] =  xy*nc + zs;
        rm[rmOffset +  5] = y*y*nc +  c;
        rm[rmOffset +  9] =  yz*nc - xs;
        rm[rmOffset +  2] =  zx*nc - ys;
        rm[rmOffset +  6] =  yz*nc + xs;
        rm[rmOffset + 10] = z*z*nc +  c;
    }
}

void rotateM(float rm[], float m[],
                           float a, float x, float y, float z) {
    setRotateM(sTemp, 0, a, x, y, z);
    multiplyMM(rm, m, sTemp);
}

void frustumM(float m[], int offset,
              float left, float right, float bottom, float top,
              float nearZ, float farZ) {

    float r_width  = 1.0f / (right - left);
    float r_height = 1.0f / (top - bottom);
    float r_depth  = 1.0f / (nearZ - farZ);
    float x = 2.0f * (nearZ * r_width);
    float y = 2.0f * (nearZ * r_height);
    float A = (right + left) * r_width;
    float B = (top + bottom) * r_height;
    float C = (farZ + nearZ) * r_depth;
    float D = 2.0f * (farZ * nearZ * r_depth);
    m[offset + 0] = x;
    m[offset + 5] = y;
    m[offset + 8] = A;
    m[offset +  9] = B;
    m[offset + 10] = C;
    m[offset + 14] = D;
    m[offset + 11] = -1.0f;
    m[offset +  1] = 0.0f;
    m[offset +  2] = 0.0f;
    m[offset +  3] = 0.0f;
    m[offset +  4] = 0.0f;
    m[offset +  6] = 0.0f;
    m[offset +  7] = 0.0f;
    m[offset + 12] = 0.0f;
    m[offset + 13] = 0.0f;
    m[offset + 15] = 0.0f;
}

void frustumM_RUB(int w, int h, double fu, double fv, double u0, double v0, double zNear, double zFar ,float projectionMatrix[]) {
    // http://www.songho.ca/opengl/gl_projectionmatrix.html
    const double L = -(u0) * zNear / fu;
    const double R = +(w - u0) * zNear / fu;
    const double T = +(v0) * zNear / fv;
    const double B = -(h - v0) * zNear / fv;
    frustumM(projectionMatrix,0,L,R,B,T,zNear,zFar);
}

void setIdentityM(float m[])
{
    m[0] = 1.0f;  m[1] = 0.0f;  m[2] = 0.0f;  m[3] = 0.0f;
    m[4] = 0.0f;  m[5] = 1.0f;  m[6] = 0.0f;  m[7] = 0.0f;
    m[8] = 0.0f;  m[9] = 0.0f; m[10] = 1.0f; m[11] = 0.0f;
    m[12] = 0.0f; m[13] = 0.0f; m[14] = 0.0f; m[15] = 1.0f;
}

/**
     * Inverts a 4 x 4 matrix.
     * <p>
     * mInv and m must not overlap.
     *
     * @param mInv the array that holds the output inverted matrix
     * @param mInvOffset an offset into mInv where the inverted matrix is
     *        stored.
     * @param m the input array
     * @param mOffset an offset into m where the input matrix is stored.
     * @return true if the matrix could be inverted, false if it could not.
     */
bool invertM(float mInv[], int mInvOffset, float m[],
                              int mOffset) {
    // Invert a 4 x 4 matrix using Cramer's Rule

    // transpose matrix
    float src0  = m[mOffset +  0];
    float src4  = m[mOffset +  1];
    float src8  = m[mOffset +  2];
    float src12 = m[mOffset +  3];

    float src1  = m[mOffset +  4];
    float src5  = m[mOffset +  5];
    float src9  = m[mOffset +  6];
    float src13 = m[mOffset +  7];

    float src2  = m[mOffset +  8];
    float src6  = m[mOffset +  9];
    float src10 = m[mOffset + 10];
    float src14 = m[mOffset + 11];

    float src3  = m[mOffset + 12];
    float src7  = m[mOffset + 13];
    float src11 = m[mOffset + 14];
    float src15 = m[mOffset + 15];

    // calculate pairs for first 8 elements (cofactors)
    float atmp0  = src10 * src15;
    float atmp1  = src11 * src14;
    float atmp2  = src9  * src15;
    float atmp3  = src11 * src13;
    float atmp4  = src9  * src14;
    float atmp5  = src10 * src13;
    float atmp6  = src8  * src15;
    float atmp7  = src11 * src12;
    float atmp8  = src8  * src14;
    float atmp9  = src10 * src12;
    float atmp10 = src8  * src13;
    float atmp11 = src9  * src12;

    // calculate first 8 elements (cofactors)
    float dst0  = (atmp0 * src5 + atmp3 * src6 + atmp4  * src7)
                  - (atmp1 * src5 + atmp2 * src6 + atmp5  * src7);
    float dst1  = (atmp1 * src4 + atmp6 * src6 + atmp9  * src7)
                  - (atmp0 * src4 + atmp7 * src6 + atmp8  * src7);
    float dst2  = (atmp2 * src4 + atmp7 * src5 + atmp10 * src7)
                  - (atmp3 * src4 + atmp6 * src5 + atmp11 * src7);
    float dst3  = (atmp5 * src4 + atmp8 * src5 + atmp11 * src6)
                  - (atmp4 * src4 + atmp9 * src5 + atmp10 * src6);
    float dst4  = (atmp1 * src1 + atmp2 * src2 + atmp5  * src3)
                  - (atmp0 * src1 + atmp3 * src2 + atmp4  * src3);
    float dst5  = (atmp0 * src0 + atmp7 * src2 + atmp8  * src3)
                  - (atmp1 * src0 + atmp6 * src2 + atmp9  * src3);
    float dst6  = (atmp3 * src0 + atmp6 * src1 + atmp11 * src3)
                  - (atmp2 * src0 + atmp7 * src1 + atmp10 * src3);
    float dst7  = (atmp4 * src0 + atmp9 * src1 + atmp10 * src2)
                        - (atmp5 * src0 + atmp8 * src1 + atmp11 * src2);

    // calculate pairs for second 8 elements (cofactors)
    float btmp0  = src2 * src7;
    float btmp1  = src3 * src6;
    float btmp2  = src1 * src7;
    float btmp3  = src3 * src5;
    float btmp4  = src1 * src6;
    float btmp5  = src2 * src5;
    float btmp6  = src0 * src7;
    float btmp7  = src3 * src4;
    float btmp8  = src0 * src6;
    float btmp9  = src2 * src4;
    float btmp10 = src0 * src5;
    float btmp11 = src1 * src4;

    // calculate second 8 elements (cofactors)
    float dst8  = (btmp0  * src13 + btmp3  * src14 + btmp4  * src15)
                  - (btmp1  * src13 + btmp2  * src14 + btmp5  * src15);
    float dst9  = (btmp1  * src12 + btmp6  * src14 + btmp9  * src15)
                  - (btmp0  * src12 + btmp7  * src14 + btmp8  * src15);
    float dst10 = (btmp2  * src12 + btmp7  * src13 + btmp10 * src15)
                  - (btmp3  * src12 + btmp6  * src13 + btmp11 * src15);
    float dst11 = (btmp5  * src12 + btmp8  * src13 + btmp11 * src14)
                  - (btmp4  * src12 + btmp9  * src13 + btmp10 * src14);
    float dst12 = (btmp2  * src10 + btmp5  * src11 + btmp1  * src9 )
                  - (btmp4  * src11 + btmp0  * src9  + btmp3  * src10);
    float dst13 = (btmp8  * src11 + btmp0  * src8  + btmp7  * src10)
                  - (btmp6  * src10 + btmp9  * src11 + btmp1  * src8 );
    float dst14 = (btmp6  * src9  + btmp11 * src11 + btmp3  * src8 )
                  - (btmp10 * src11 + btmp2  * src8  + btmp7  * src9 );
    float dst15 = (btmp10 * src10 + btmp4  * src8  + btmp9  * src9 )
                        - (btmp8  * src9  + btmp11 * src10 + btmp5  * src8 );

    // calculate determinant
    float det =
            src0 * dst0 + src1 * dst1 + src2 * dst2 + src3 * dst3;

    if (det == 0.0f) {
        return false;
    }

    // calculate matrix inverse
    float invdet = 1.0f / det;
    mInv[     mInvOffset] = dst0  * invdet;
    mInv[ 1 + mInvOffset] = dst1  * invdet;
    mInv[ 2 + mInvOffset] = dst2  * invdet;
    mInv[ 3 + mInvOffset] = dst3  * invdet;

    mInv[ 4 + mInvOffset] = dst4  * invdet;
    mInv[ 5 + mInvOffset] = dst5  * invdet;
    mInv[ 6 + mInvOffset] = dst6  * invdet;
    mInv[ 7 + mInvOffset] = dst7  * invdet;

    mInv[ 8 + mInvOffset] = dst8  * invdet;
    mInv[ 9 + mInvOffset] = dst9  * invdet;
    mInv[10 + mInvOffset] = dst10 * invdet;
    mInv[11 + mInvOffset] = dst11 * invdet;

    mInv[12 + mInvOffset] = dst12 * invdet;
    mInv[13 + mInvOffset] = dst13 * invdet;
    mInv[14 + mInvOffset] = dst14 * invdet;
    mInv[15 + mInvOffset] = dst15 * invdet;

    return true;
}


void getRUBViewMatrixFromRDF(float inM[],float outM[]){
    float tmpM[16],tmpVM[16],tmpTwc[16];
    setIdentityM(tmpM);
    rotateM(tmpVM,tmpM, +180.0f, 1.0f, 0.0f, 0.0f);
    multiplyMM(tmpM,tmpVM,inM);

    invertM(tmpTwc,0,tmpM,0);
    setIdentityM(tmpVM);
    rotateM(tmpM,tmpVM, +180.0f, 1.0f, 0.0f, 0.0f);
    multiplyMM(tmpVM,tmpM,tmpTwc);
    invertM(outM,0,tmpVM,0);
}

void getRUBModelMatrixFromRDF(float inM[],float outM[]){
    float tmpM[16],tmpVM[16];
    setIdentityM(tmpM);
    rotateM(tmpVM,tmpM, +180.0f, 1.0f, 0.0f, 0.0f);
    multiplyMM(outM,tmpVM,inM);
}


void getEulerAnglesFromMatrix(float M[],float outM[],int offset){
    outM[offset+0] =180.0f/PI*atan2(M[1], M[5]);
    outM[offset+1] =180.0f/PI*asin(-M[9]);
    outM[offset+2] =180.0f/PI*atan2(-M[8], M[10]);
}

void quaternionToMatrix(float M[],Quaternion &q){
    float m00;
    float m10;
    float m20;

    float m01;
    float m11;
    float m21;

    float m02;
    float m12;
    float m22;

    float sqw = q.w*q.w;
    float sqx = q.x*q.x;
    float sqy = q.y*q.y;
    float sqz = q.z*q.z;

    // invs (inverse square length) is only required if quaternion is not already normalised
    float invs = 1 / (sqx + sqy + sqz + sqw);
    m00 = ( sqx - sqy - sqz + sqw)*invs ; // since sqw + sqx + sqy + sqz =1/invs*invs
    m11 = (-sqx + sqy - sqz + sqw)*invs ;
    m22 = (-sqx - sqy + sqz + sqw)*invs ;

    float tmp1 = q.x*q.y;
    float tmp2 = q.z*q.w;
    m10 = 2.0f * (tmp1 + tmp2)*invs ;
    m01 = 2.0f * (tmp1 - tmp2)*invs ;

    tmp1 = q.x*q.z;
    tmp2 = q.y*q.w;
    m20 = 2.0f * (tmp1 - tmp2)*invs ;
    m02 = 2.0f * (tmp1 + tmp2)*invs ;
    tmp1 = q.y*q.z;
    tmp2 = q.x*q.w;
    m21 = 2.0f * (tmp1 + tmp2)*invs ;
    m12 = 2.0f * (tmp1 - tmp2)*invs ;

    M[0]=m00;
    M[1]=m10;
    M[2]=m20;
    M[4]=m01;
    M[5]=m11;
    M[6]=m21;
    M[8]=m02;
    M[9]=m12;
    M[10]=m22;
}

void matrixToQuaternion(float M[],Quaternion &q){
    float qx,qy,qz,qw;
    float m00=M[0];
    float m10=M[1];
    float m20=M[2];
    float m01=M[4];
    float m11=M[5];
    float m21=M[6];
    float m02=M[8];
    float m12=M[9];
    float m22=M[10];

    float tr = m00 + m11 + m22;

    if (tr > 0) {
        float S = (float) (sqrt(tr+1) * 2); // S=4*qw
        qw = 0.25f * S;
        qx = (m21 - m12) / S;
        qy = (m02 - m20) / S;
        qz = (m10 - m01) / S;
    } else if ((m00 > m11)&(m00 > m22)) {
        float S = (float) (sqrt(1.0 + m00 - m11 - m22) * 2); // S=4*qx
        qw = (m21 - m12) / S;
        qx = 0.25f * S;
        qy = (m01 + m10) / S;
        qz = (m02 + m20) / S;
    } else if (m11 > m22) {
        float S = (float) (sqrt(1.0 + m11 - m00 - m22) * 2); // S=4*qy
        qw = (m02 - m20) / S;
        qx = (m01 + m10) / S;
        qy = 0.25f * S;
        qz = (m12 + m21) / S;
    } else {
        float S = (float) (sqrt(1.0 + m22 - m00 - m11) * 2); // S=4*qz
        qw = (m10 - m01) / S;
        qx = (m02 + m20) / S;
        qy = (m12 + m21) / S;
        qz = 0.25f * S;
    }
    q.x=qx;
    q.y=qy;
    q.z=qz;
    q.w=qw;
}

inline float sign(float f){
    if(f<0) return -1.0f;
    return 1.0f;
}

void quaternionU3DFromMatrix(float M[],Quaternion &q)
{
    // Adapted from: http://www.euclideanspace.com/maths/geometry/rotations/conversions/matrixToQuaternion/index.htm
    float m00=M[0],m10=M[1],m20=M[2];
    float m01=M[4],m11=M[5],m21=M[6];
    float m02=M[8],m12=M[9],m22=M[10];
    q.w = sqrt(std::max(0.0f, 1 + m00 + m11 + m22)) / 2;
    q.x = sqrt(std::max(0.0f, 1 + m00 - m11 - m22)) / 2;
    q.y = sqrt(std::max(0.0f, 1 - m00 + m11 - m22)) / 2;
    q.z = sqrt(std::max(0.0f, 1 - m00 - m11 + m22)) / 2;
    q.x *= sign(q.x * (m21 - m12));
    q.y *= sign(q.y * (m02 - m20));
    q.z *= sign(q.z * (m10 - m01));
}

void prepareModelM(float inM[],float outM[],const float size, const float x, const float y, const float z){
    float M[16],tmpM[16];
    setIdentityM(M);
    rotateM(tmpM,M,180,1,0,0);
    tmpM[12] = -x;
    tmpM[13] = -size-y;
    tmpM[14] = -z;
    multiplyMM(outM,inM,tmpM);
}

void logMatrix(float f[]){
    LOGD("Matrix is:");
    std::string str;
    char buf[255];
    for(int i=0;i<4;i++){
        str="";
        for(int j=i;j<16;j+=4){
            sprintf(buf," %.3lf ",f[j]);
            str+=buf;
        }
        LOGD("%s",str.c_str());
    }
}
