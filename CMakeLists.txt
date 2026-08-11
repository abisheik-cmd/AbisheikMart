cmake_minimum_required(VERSION 3.15)
project(Abisheikmart VERSION 1.0.0 LANGUAGES C CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS OFF)

# Include directories
include_directories(
    ${CMAKE_CURRENT_SOURCE_DIR}/third_party
    ${CMAKE_CURRENT_SOURCE_DIR}/third_party/httplib
    ${CMAKE_CURRENT_SOURCE_DIR}/third_party/json
    ${CMAKE_CURRENT_SOURCE_DIR}/third_party/sqlite
    ${CMAKE_CURRENT_SOURCE_DIR}/src
)

# Source files
set(SOURCES
    third_party/sqlite/sqlite3.c
    src/db/Database.cpp
    src/utils/Crypto.cpp
    src/utils/SessionManager.cpp
    src/controllers/AuthController.cpp
    src/controllers/ProductController.cpp
    src/controllers/CartController.cpp
    src/controllers/OrderController.cpp
    src/main.cpp
)

# Server Executable
add_executable(AbisheikmartServer ${SOURCES})

# Static linking for GCC/MinGW to generate portable standalone executable
if(MINGW)
    target_link_options(AbisheikmartServer PRIVATE -static-libgcc -static-libstdc++ -static)
endif()

# Windows socket library dependencies for cpp-httplib
if(WIN32)
    target_link_libraries(AbisheikmartServer PRIVATE ws2_32 wsock32 iphlpapi)
endif()

# Threading support
find_package(Threads REQUIRED)
target_link_libraries(AbisheikmartServer PRIVATE Threads::Threads)
