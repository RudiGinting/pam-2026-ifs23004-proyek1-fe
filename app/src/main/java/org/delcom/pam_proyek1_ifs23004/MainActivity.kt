package org.delcom.pam_proyek1_ifs23004

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint
import org.delcom.pam_proyek1_ifs23004.ui.UIApp
import org.delcom.pam_proyek1_ifs23004.ui.theme.DelcomTheme
import org.delcom.pam_proyek1_ifs23004.ui.viewmodels.AuthViewModel
import org.delcom.pam_proyek1_ifs23004.ui.viewmodels.InternshipViewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val internshipViewModel: InternshipViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DelcomTheme {
                UIApp(
                    internshipViewModel = internshipViewModel,
                    authViewModel = authViewModel
                )
            }
        }
    }
}