package org.delcom.pam_proyek1_ifs23004.helper

class ConstHelper {
    // Route Names
    enum class RouteNames(val path: String) {
        // Auth
        AuthLogin(path = "auth/login"),
        AuthRegister(path = "auth/register"),

        // Main Screens
        Home(path = "home"),
        Profile(path = "profile"),

        // Internships (Lowongan Magang)
        Internships(path = "internships"),
        InternshipsAdd(path = "internships/add"),
        InternshipsDetail(path = "internships/{internshipId}"),
        InternshipsEdit(path = "internships/{internshipId}/edit"),

        // Applications (Lamaran Saya)
        MyApplications(path = "my-applications"),
        ApplicationDetail(path = "application/{applicationId}")  // TAMBAHKAN INI
    }
}