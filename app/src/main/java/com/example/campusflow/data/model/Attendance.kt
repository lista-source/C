package com.example.campusflow.data.model

data class Attendance(
    val id: String = "",
    val studentId: String = "",
    val courseId: String = "",
    val date: Long = System.currentTimeMillis(),
    val isPresent: Boolean = false,
    val location: String = "", // GPS coords
    val verified: Boolean = false,
    val verificationMethod: VerificationMethod = VerificationMethod.GPS,
    val lectureId: String = ""
)

enum class VerificationMethod {
    GPS,
    QR_CODE,
    MANUAL
}
