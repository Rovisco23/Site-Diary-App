package pt.isel.sitediary.ui.common.nav

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TextSnippet
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import pt.isel.sitediary.R

sealed class BottomNavItem(val route: String, @StringRes val title: Int, val icon: ImageVector) {
    object Main {
        data object Home : BottomNavItem("Obras", R.string.obras, Icons.Default.Home)
        data object Logs :
            BottomNavItem("Registos", R.string.registos, Icons.AutoMirrored.Filled.TextSnippet)

        data object UserProfile : BottomNavItem("Perfil", R.string.perfil, Icons.Default.Person)
    }

    object Work {
        data object Details : BottomNavItem("Visão Geral", R.string.detalhes, Icons.Default.Info)
        data object Log :
            BottomNavItem("Registo", R.string.registo, Icons.AutoMirrored.Filled.TextSnippet)

        data object CreateLog :
            BottomNavItem("Criar Novo Registo", R.string.create_log, Icons.Default.AddCircle)
    }
}