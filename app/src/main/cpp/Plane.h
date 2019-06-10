//
// Created by Ads on 2017/1/15.
//

#ifndef ORB_SLAM_AR_PLANE_H
#define ORB_SLAM_AR_PLANE_H

#include "Common.h"
#include <opencv2/opencv.hpp>
#include "include/System.h"
class Plane
{
public:
    Plane(const std::vector<ORB_SLAM2::MapPoint*> &vMPs, const cv::Mat &Tcw);
    Plane(const float &nx, const float &ny, const float &nz, const float &ox, const float &oy, const float &oz);
    cv::Mat ExpSO3(const float &x, const float &y, const float &z);
    cv::Mat ExpSO3(const cv::Mat &v);
    void Recompute();

    //normal
    cv::Mat n;
    //origin
    cv::Mat o;
    //arbitrary orientation along normal
    float rang;
    //transformation from world to the plane
    cv::Mat Tpw;

    float glTpw[16];
    //MapPoints that define the plane
    std::vector<ORB_SLAM2::MapPoint*> mvMPs;
    //camera pose when the plane was first observed (to compute normal direction)
    cv::Mat mTcw, XC;
};



#endif //ORB_SLAM_AR_PLANE_H
