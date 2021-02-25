cmake_minimum_required(VERSION 3.4.1)

set(CMAKE_VERBOSE_MAKEFILE on)
set(libs "${CMAKE_SOURCE_DIR}/src/main/jniLibs")
include_directories(${CMAKE_SOURCE_DIR}/src/main/cpp/opencv/include)

#--------------------------------------------------- import ---------------------------------------------------#
add_library(libopencv_java4 SHARED IMPORTED )
set_target_properties(libopencv_java4 PROPERTIES
        IMPORTED_LOCATION "${libs}/${ANDROID_ABI}/libopencv_java4.so")

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=gnu++11 -fexceptions -frtti")


ADD_LIBRARY(g2o
        src/main/cpp/Thirdparty/g2o/g2o/types/types_sba.h
        src/main/cpp/Thirdparty/g2o/g2o/types/types_six_dof_expmap.h
        src/main/cpp/Thirdparty/g2o/g2o/types/types_sba.cpp
        src/main/cpp/Thirdparty/g2o/g2o/types/types_six_dof_expmap.cpp
        src/main/cpp/Thirdparty/g2o/g2o/types/types_seven_dof_expmap.cpp
        src/main/cpp/Thirdparty/g2o/g2o/types/types_seven_dof_expmap.h
        src/main/cpp/Thirdparty/g2o/g2o/types/se3quat.h
        src/main/cpp/Thirdparty/g2o/g2o/types/se3_ops.h
        src/main/cpp/Thirdparty/g2o/g2o/types/se3_ops.hpp
        src/main/cpp/Thirdparty/g2o/g2o/core/base_edge.h
        src/main/cpp/Thirdparty/g2o/g2o/core/base_binary_edge.h
        src/main/cpp/Thirdparty/g2o/g2o/core/hyper_graph_action.cpp
        src/main/cpp/Thirdparty/g2o/g2o/core/base_binary_edge.hpp
        src/main/cpp/Thirdparty/g2o/g2o/core/hyper_graph_action.h
        src/main/cpp/Thirdparty/g2o/g2o/core/base_multi_edge.h
        src/main/cpp/Thirdparty/g2o/g2o/core/hyper_graph.cpp
        src/main/cpp/Thirdparty/g2o/g2o/core/base_multi_edge.hpp
        src/main/cpp/Thirdparty/g2o/g2o/core/hyper_graph.h
        src/main/cpp/Thirdparty/g2o/g2o/core/base_unary_edge.h
        src/main/cpp/Thirdparty/g2o/g2o/core/linear_solver.h
        src/main/cpp/Thirdparty/g2o/g2o/core/base_unary_edge.hpp
        src/main/cpp/Thirdparty/g2o/g2o/core/marginal_covariance_cholesky.cpp
        src/main/cpp/Thirdparty/g2o/g2o/core/base_vertex.h
        src/main/cpp/Thirdparty/g2o/g2o/core/marginal_covariance_cholesky.h
        src/main/cpp/Thirdparty/g2o/g2o/core/base_vertex.hpp
        src/main/cpp/Thirdparty/g2o/g2o/core/matrix_structure.cpp
        src/main/cpp/Thirdparty/g2o/g2o/core/batch_stats.cpp
        src/main/cpp/Thirdparty/g2o/g2o/core/matrix_structure.h
        src/main/cpp/Thirdparty/g2o/g2o/core/batch_stats.h
        src/main/cpp/Thirdparty/g2o/g2o/core/openmp_mutex.h
        src/main/cpp/Thirdparty/g2o/g2o/core/block_solver.h
        src/main/cpp/Thirdparty/g2o/g2o/core/block_solver.hpp
        src/main/cpp/Thirdparty/g2o/g2o/core/parameter.cpp
        src/main/cpp/Thirdparty/g2o/g2o/core/parameter.h
        src/main/cpp/Thirdparty/g2o/g2o/core/cache.cpp
        src/main/cpp/Thirdparty/g2o/g2o/core/cache.h
        src/main/cpp/Thirdparty/g2o/g2o/core/optimizable_graph.cpp
        src/main/cpp/Thirdparty/g2o/g2o/core/optimizable_graph.h
        src/main/cpp/Thirdparty/g2o/g2o/core/solver.cpp
        src/main/cpp/Thirdparty/g2o/g2o/core/solver.h
        src/main/cpp/Thirdparty/g2o/g2o/core/creators.h
        src/main/cpp/Thirdparty/g2o/g2o/core/optimization_algorithm_factory.cpp
        src/main/cpp/Thirdparty/g2o/g2o/core/estimate_propagator.cpp
        src/main/cpp/Thirdparty/g2o/g2o/core/optimization_algorithm_factory.h
        src/main/cpp/Thirdparty/g2o/g2o/core/estimate_propagator.h
        src/main/cpp/Thirdparty/g2o/g2o/core/factory.cpp
        src/main/cpp/Thirdparty/g2o/g2o/core/optimization_algorithm_property.h
        src/main/cpp/Thirdparty/g2o/g2o/core/factory.h
        src/main/cpp/Thirdparty/g2o/g2o/core/sparse_block_matrix.h
        src/main/cpp/Thirdparty/g2o/g2o/core/sparse_optimizer.cpp
        src/main/cpp/Thirdparty/g2o/g2o/core/sparse_block_matrix.hpp
        src/main/cpp/Thirdparty/g2o/g2o/core/sparse_optimizer.h
        src/main/cpp/Thirdparty/g2o/g2o/core/hyper_dijkstra.cpp
        src/main/cpp/Thirdparty/g2o/g2o/core/hyper_dijkstra.h
        src/main/cpp/Thirdparty/g2o/g2o/core/parameter_container.cpp
        src/main/cpp/Thirdparty/g2o/g2o/core/parameter_container.h
        src/main/cpp/Thirdparty/g2o/g2o/core/optimization_algorithm.cpp
        src/main/cpp/Thirdparty/g2o/g2o/core/optimization_algorithm.h
        src/main/cpp/Thirdparty/g2o/g2o/core/optimization_algorithm_with_hessian.cpp
        src/main/cpp/Thirdparty/g2o/g2o/core/optimization_algorithm_with_hessian.h
        src/main/cpp/Thirdparty/g2o/g2o/core/optimization_algorithm_levenberg.cpp
        src/main/cpp/Thirdparty/g2o/g2o/core/optimization_algorithm_levenberg.h
        src/main/cpp/Thirdparty/g2o/g2o/core/jacobian_workspace.cpp
        src/main/cpp/Thirdparty/g2o/g2o/core/jacobian_workspace.h
        src/main/cpp/Thirdparty/g2o/g2o/core/robust_kernel.cpp
        src/main/cpp/Thirdparty/g2o/g2o/core/robust_kernel.h
        src/main/cpp/Thirdparty/g2o/g2o/core/robust_kernel_factory.cpp
        src/main/cpp/Thirdparty/g2o/g2o/core/robust_kernel_factory.h
        src/main/cpp/Thirdparty/g2o/g2o/core/robust_kernel_impl.cpp
        src/main/cpp/Thirdparty/g2o/g2o/core/robust_kernel_impl.h
        src/main/cpp/Thirdparty/g2o/g2o/stuff/string_tools.h
        src/main/cpp/Thirdparty/g2o/g2o/stuff/color_macros.h
        src/main/cpp/Thirdparty/g2o/g2o/stuff/macros.h
        src/main/cpp/Thirdparty/g2o/g2o/stuff/timeutil.cpp
        src/main/cpp/Thirdparty/g2o/g2o/stuff/misc.h
        src/main/cpp/Thirdparty/g2o/g2o/stuff/timeutil.h
        src/main/cpp/Thirdparty/g2o/g2o/stuff/os_specific.c
        src/main/cpp/Thirdparty/g2o/g2o/stuff/os_specific.h
        src/main/cpp/Thirdparty/g2o/g2o/stuff/string_tools.cpp
        src/main/cpp/Thirdparty/g2o/g2o/stuff/property.cpp
        src/main/cpp/Thirdparty/g2o/g2o/stuff/property.h
        )

include_directories(
        src/main/cpp
        src/main/cpp/include
        src/main/cpp/Thirdparty
        src/main/cpp/Thirdparty/g2o
)

add_library(SLAM_CORE SHARED
        src/main/cpp/include/System.h
        src/main/cpp/include/Tracking.h
        src/main/cpp/include/LocalMapping.h
        src/main/cpp/include/LoopClosing.h
        src/main/cpp/include/ORBextractor.h
        src/main/cpp/include/ORBmatcher.h
        src/main/cpp/include/FrameDrawer.h
        src/main/cpp/include/Converter.h
        src/main/cpp/include/MapPoint.h
        src/main/cpp/include/KeyFrame.h
        src/main/cpp/include/Map.h
        src/main/cpp/include/Optimizer.h
        src/main/cpp/include/PnPsolver.h
        src/main/cpp/include/Frame.h
        src/main/cpp/include/KeyFrameDatabase.h
        src/main/cpp/include/Sim3Solver.h
        src/main/cpp/include/Initializer.h
        src/main/cpp/include/Common.h
        src/main/cpp/src/System.cc
        src/main/cpp/src/Tracking.cc
        src/main/cpp/src/LocalMapping.cc
        src/main/cpp/src/LoopClosing.cc
        src/main/cpp/src/ORBextractor.cc
        src/main/cpp/src/ORBmatcher.cc
        src/main/cpp/src/FrameDrawer.cc
        src/main/cpp/src/Converter.cc
        src/main/cpp/src/MapPoint.cc
        src/main/cpp/src/KeyFrame.cc
        src/main/cpp/src/Map.cc
        src/main/cpp/src/Optimizer.cc
        src/main/cpp/src/PnPsolver.cc
        src/main/cpp/src/Frame.cc
        src/main/cpp/src/KeyFrameDatabase.cc
        src/main/cpp/src/Sim3Solver.cc
        src/main/cpp/src/Initializer.cc

        ############DBow2############
        src/main/cpp/Thirdparty/DBoW2/DBoW2/BowVector.h
        src/main/cpp/Thirdparty/DBoW2/DBoW2/FORB.h
        src/main/cpp/Thirdparty/DBoW2/DBoW2/FClass.h
        src/main/cpp/Thirdparty/DBoW2/DBoW2/FeatureVector.h
        src/main/cpp/Thirdparty/DBoW2/DBoW2/ScoringObject.h
        src/main/cpp/Thirdparty/DBoW2/DBoW2/TemplatedVocabulary.h
        src/main/cpp/Thirdparty/DBoW2/DUtils/Random.h
        src/main/cpp/Thirdparty/DBoW2/DBoW2/BowVector.cpp
        src/main/cpp/Thirdparty/DBoW2/DBoW2/FORB.cpp
        src/main/cpp/Thirdparty/DBoW2/DBoW2/FeatureVector.cpp
        src/main/cpp/Thirdparty/DBoW2/DBoW2/ScoringObject.cpp
        src/main/cpp/Thirdparty/DBoW2/DUtils/Random.cpp

        ###Martin
        src/main/cpp/Utils.cpp
        )


find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)

target_link_libraries(SLAM_CORE
#        libopencv_java3 #used for java sdk
#        libopencv_java #same to static libs
        libopencv_java4
        ${log-lib}
        g2o
        )

add_library(
        SLAM_JNI

        SHARED
        src/main/cpp/native-lib.cpp
        src/main/cpp/UIUtils.cpp
        src/main/cpp/Plane.cpp
        src/main/cpp/Matrix.cpp
        )

target_link_libraries(SLAM_JNI
        #        libopencv_java3 #used for java sdk
        #        libopencv_java #same to static libs
        libopencv_java4
        ${log-lib}
        SLAM_CORE
        EGL
        GLESv1_CM
        )

