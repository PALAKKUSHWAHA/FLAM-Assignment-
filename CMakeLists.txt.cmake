cmake_minimum_required(VERSION 3.4.1)

find_package(OpenCV REQUIRED)

add_library(
    edge-detection
    SHARED
    edge_detector.cpp
)

target_include_directories(
    edge-detection
    PRIVATE
    ${OpenCV_INCLUDE_DIRS}
)

target_link_libraries(
    edge-detection
    android
    log
    GLESv2
    ${OpenCV_LIBS}
)