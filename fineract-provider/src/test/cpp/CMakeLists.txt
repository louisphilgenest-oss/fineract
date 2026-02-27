cmake_minimum_required(VERSION 3.14)
project(fineract_cpp_tests LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# ---------------------------------------------------------------------------
# Google Test via FetchContent
# ---------------------------------------------------------------------------
include(FetchContent)
FetchContent_Declare(
    googletest
    GIT_REPOSITORY https://github.com/google/googletest.git
    GIT_TAG        v1.14.0
)
# Prevent overriding parent project settings
set(gtest_force_shared_crt ON CACHE BOOL "" FORCE)
FetchContent_MakeAvailable(googletest)

enable_testing()

# ---------------------------------------------------------------------------
# Test executable
# ---------------------------------------------------------------------------
add_executable(loan_read_platform_service_impl_test
    org/apache/fineract/portfolio/loanaccount/service/LoanReadPlatformServiceImplTest.cpp
)

target_include_directories(loan_read_platform_service_impl_test PRIVATE
    ${CMAKE_CURRENT_SOURCE_DIR}
)

target_link_libraries(loan_read_platform_service_impl_test
    GTest::gtest_main
    GTest::gmock
)

include(GoogleTest)
gtest_discover_tests(loan_read_platform_service_impl_test)
