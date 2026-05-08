package com.example.campusflow.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    object Profile : Screen("profile")

    // Student
    object StudentDashboard : Screen("student_dashboard")
    object StudentTimetable : Screen("student_timetable")
    object StudentAttendance : Screen("student_attendance")
    object StudentResults : Screen("student_results")
    object StudentAnalytics : Screen("student_analytics")
    object StudentAnnouncements : Screen("student_announcements")
    object StudentAppeals : Screen("student_appeals")
    object StudentChat : Screen("student_chat/{receiverId}/{receiverName}") {
        fun createRoute(receiverId: String, receiverName: String) = "student_chat/$receiverId/$receiverName"
    }
    object StudentMaterials : Screen("student_materials/{courseId}") {
        fun createRoute(courseId: String) = "student_materials/$courseId"
    }

    // Lecturer
    object LecturerDashboard : Screen("lecturer_dashboard")
    object LecturerSchedule : Screen("lecturer_schedule")
    object LecturerAttendance : Screen("lecturer_attendance")
    object LecturerResults : Screen("lecturer_results")
    object LecturerAppeals : Screen("lecturer_appeals")
    object LecturerChat : Screen("lecturer_chat/{receiverId}/{receiverName}") {
        fun createRoute(receiverId: String, receiverName: String) = "lecturer_chat/$receiverId/$receiverName"
    }
    object LecturerMaterials : Screen("lecturer_materials/{courseId}") {
        fun createRoute(courseId: String) = "lecturer_materials/$courseId"
    }

    // Admin
    object AdminDashboard : Screen("admin_dashboard")
    object AdminManagement : Screen("admin_management")
    object AdminAnnouncements : Screen("admin_announcements")
}