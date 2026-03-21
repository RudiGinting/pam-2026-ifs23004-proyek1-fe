package org.delcom.pam_proyek1_ifs23004.network.internships.data

enum class CategoryEnum(val fullName: String, val shortName: String) {
    IT("Information Technology", "IT"),
    MARKETING("Marketing", "Marketing"),
    FINANCE("Finance", "Finance"),
    HR("Human Resources", "HR"),
    DESIGN("Design", "Design"),
    BUSINESS("Business Development", "Business");

    companion object {
        fun getAllFullNames(): List<String> {
            return entries.map { it.fullName }
        }
    }
}

enum class LocationEnum(val value: String) {
    ONSITE("On-site"),
    REMOTE("Remote"),
    HYBRID("Hybrid");

    companion object {
        fun getAllValues(): List<String> {
            return entries.map { it.value }
        }
    }
}