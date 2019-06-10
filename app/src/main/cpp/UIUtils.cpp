//
// Created by Ads on 2017/1/15.
//
#include "UIUtils.h"
#include "Matrix.h"

void addTextToImage(const string &s, cv::Mat &im, const int r, const int g, const int b)
{
    int l = 10;
    cv::putText(im,s,cv::Point(l,im.rows-l),cv::FONT_HERSHEY_PLAIN,1.5,cv::Scalar(255,255,255),2,8);
    cv::putText(im,s,cv::Point(l-1,im.rows-l),cv::FONT_HERSHEY_PLAIN,1.5,cv::Scalar(255,255,255),2,8);
    cv::putText(im,s,cv::Point(l+1,im.rows-l),cv::FONT_HERSHEY_PLAIN,1.5,cv::Scalar(255,255,255),2,8);
    cv::putText(im,s,cv::Point(l-1,im.rows-(l-1)),cv::FONT_HERSHEY_PLAIN,1.5,cv::Scalar(255,255,255),2,8);
    cv::putText(im,s,cv::Point(l,im.rows-(l-1)),cv::FONT_HERSHEY_PLAIN,1.5,cv::Scalar(255,255,255),2,8);
    cv::putText(im,s,cv::Point(l+1,im.rows-(l-1)),cv::FONT_HERSHEY_PLAIN,1.5,cv::Scalar(255,255,255),2,8);
    cv::putText(im,s,cv::Point(l-1,im.rows-(l+1)),cv::FONT_HERSHEY_PLAIN,1.5,cv::Scalar(255,255,255),2,8);
    cv::putText(im,s,cv::Point(l,im.rows-(l+1)),cv::FONT_HERSHEY_PLAIN,1.5,cv::Scalar(255,255,255),2,8);
    cv::putText(im,s,cv::Point(l+1,im.rows-(l+1)),cv::FONT_HERSHEY_PLAIN,1.5,cv::Scalar(255,255,255),2,8);
    cv::putText(im,s,cv::Point(l,im.rows-l),cv::FONT_HERSHEY_PLAIN,1.5,cv::Scalar(r,g,b),2,8);
}

void printStatus(const int &status, cv::Mat &im)
{
    switch(status) {
        case 1:  {addTextToImage("SLAM NOT INITIALIZED",im,255,0,0); break;}
        case 2:  {addTextToImage("SLAM ON",im,0,255,0); break;}
        case 3:  {addTextToImage("SLAM LOST",im,255,0,0); break;}
    }
}

void drawTrackedPoints(const std::vector<cv::KeyPoint> &vKeys, const std::vector<ORB_SLAM2::MapPoint *> &vMPs, cv::Mat &im)
{
    const int N = vKeys.size();
    for(int i=0; i<N; i++) {
        if(vMPs[i]) {
            //TODO:x2
            cv::circle(im,vKeys[i].pt*2,2,cv::Scalar(31,188,210),-1);
        }
    }
}

Plane* detectPlane(const cv::Mat Tcw, const std::vector<ORB_SLAM2::MapPoint*> &vMPs, const int iterations)
{
    // Retrieve 3D points
    vector<cv::Mat> vPoints;
    vPoints.reserve(vMPs.size());
    vector<ORB_SLAM2::MapPoint*> vPointMP;
    vPointMP.reserve(vMPs.size());

    for(size_t i=0; i<vMPs.size(); i++)
    {
        ORB_SLAM2::MapPoint* pMP=vMPs[i];
        if(pMP)
        {
            if(pMP->Observations()>5)
            {
                vPoints.push_back(pMP->GetWorldPos());
                vPointMP.push_back(pMP);
            }
        }
    }

    const int N = vPoints.size();

    if(N<50)
        return NULL;


    // Indices for minimum set selection
    vector<size_t> vAllIndices;
    vAllIndices.reserve(N);
    vector<size_t> vAvailableIndices;

    for(int i=0; i<N; i++)
    {
        vAllIndices.push_back(i);
    }

    float bestDist = 1e10;
    vector<float> bestvDist;

    //RANSAC
    for(int n=0; n<iterations; n++)
    {
        vAvailableIndices = vAllIndices;

        cv::Mat A(3,4,CV_32F);
        A.col(3) = cv::Mat::ones(3,1,CV_32F);

        // Get min set of points
        for(short i = 0; i < 3; ++i)
        {
            int randi = DUtils::Random::RandomInt(0, vAvailableIndices.size()-1);

            int idx = vAvailableIndices[randi];

            A.row(i).colRange(0,3) = vPoints[idx].t();

            vAvailableIndices[randi] = vAvailableIndices.back();
            vAvailableIndices.pop_back();
        }

        cv::Mat u,w,vt;
        cv::SVDecomp(A,w,u,vt,cv::SVD::MODIFY_A | cv::SVD::FULL_UV);

        const float a = vt.at<float>(3,0);
        const float b = vt.at<float>(3,1);
        const float c = vt.at<float>(3,2);
        const float d = vt.at<float>(3,3);

        vector<float> vDistances(N,0);

        const float f = 1.0f/sqrt(a*a+b*b+c*c+d*d);

        for(int i=0; i<N; i++)
        {
            vDistances[i] = fabs(vPoints[i].at<float>(0)*a+vPoints[i].at<float>(1)*b+vPoints[i].at<float>(2)*c+d)*f;
        }

        vector<float> vSorted = vDistances;
        sort(vSorted.begin(),vSorted.end());

        int nth = max((int)(0.2*N),20);
        const float medianDist = vSorted[nth];

        if(medianDist<bestDist)
        {
            bestDist = medianDist;
            bestvDist = vDistances;
        }
    }

    // Compute threshold inlier/outlier
    const float th = 1.4*bestDist;
    vector<bool> vbInliers(N,false);
    int nInliers = 0;
    for(int i=0; i<N; i++)
    {
        if(bestvDist[i]<th)
        {
            nInliers++;
            vbInliers[i]=true;
        }
    }

    vector<ORB_SLAM2::MapPoint*> vInlierMPs(nInliers,NULL);
    int nin = 0;
    for(int i=0; i<N; i++)
    {
        if(vbInliers[i])
        {
            vInlierMPs[nin] = vPointMP[i];
            nin++;
        }
    }

    return new Plane(vInlierMPs,Tcw);
}



void initProjectionMatrix(int w, int h, double fu, double fv, double u0, double v0, double zNear, double zFar ,float projectionMatrix[]){
    // http://www.songho.ca/opengl/gl_projectionmatrix.html
    const double L = -(u0) * zNear / fu;
    const double R = +(w-u0) * zNear / fu;
    const double T = -(v0) * zNear / fv;
    const double B = +(h-v0) * zNear / fv;

    std::fill_n(projectionMatrix,4*4,0);

    projectionMatrix[0*4+0] = 2 * zNear / (R-L);
    projectionMatrix[1*4+1] = 2 * zNear / (T-B);
    projectionMatrix[2*4+0] = (R+L)/(L-R);
    projectionMatrix[2*4+1] = (T+B)/(B-T);
    projectionMatrix[2*4+2] = (zFar +zNear) / (zFar - zNear);
    projectionMatrix[2*4+3] = 1.0;
    projectionMatrix[3*4+2] =  (2*zFar*zNear)/(zNear - zFar);
}


void getColMajorMatrixFromMat(float M[],cv::Mat &Tcw){
    M[0] = Tcw.at<float>(0,0);
    M[1] = Tcw.at<float>(1,0);
    M[2] = Tcw.at<float>(2,0);
    M[3]  = 0.0;
    M[4] = Tcw.at<float>(0,1);
    M[5] = Tcw.at<float>(1,1);
    M[6] = Tcw.at<float>(2,1);
    M[7]  = 0.0;
    M[8] = Tcw.at<float>(0,2);
    M[9] = Tcw.at<float>(1,2);
    M[10] = Tcw.at<float>(2,2);
    M[11]  = 0.0;
    M[12] = Tcw.at<float>(0,3);
    M[13] = Tcw.at<float>(1,3);
    M[14] = Tcw.at<float>(2,3);
    M[15]  = 1.0;
}

