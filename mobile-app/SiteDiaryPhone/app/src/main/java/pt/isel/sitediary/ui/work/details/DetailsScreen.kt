package pt.isel.sitediary.ui.work.details

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import pt.isel.sitediary.R
import pt.isel.sitediary.domain.Address
import pt.isel.sitediary.domain.ConstructionCompany
import pt.isel.sitediary.domain.Location
import pt.isel.sitediary.domain.WorkDetails
import pt.isel.sitediary.domain.WorkState
import pt.isel.sitediary.domain.WorkType
import pt.isel.sitediary.ui.common.DetailItem
import pt.isel.sitediary.ui.common.StatCard
import pt.isel.sitediary.ui.common.nav.BottomNavItem
import pt.isel.sitediary.ui.common.nav.BottomNavigationBar
import pt.isel.sitediary.ui.common.nav.DefaultTopBar
import pt.isel.sitediary.ui.common.nav.TopBarGoBack
import pt.isel.sitediary.ui.theme.SiteDiaryPhoneTheme

@Composable
fun DetailsScreen(
    details: WorkDetails,
    innerPadding: PaddingValues,
    onBackRequested: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .padding(innerPadding)
            .verticalScroll(rememberScrollState())
    ) {
        TopBarGoBack(onBackRequested)
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    // Image section
                    Image(
                        painter = painterResource(id = R.mipmap.work_icon),
                        contentDescription = "Work Image",
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier
                            .width(200.dp)
                            .height(200.dp)
                            .padding(8.dp)
                    )
                    Column {
                        DetailItem(label = "Tipo", value = details.type.name)
                        DetailItem(label = "Estado", value = details.state.toString())
                    }
                }
                // Statistics row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp, end = 12.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatCard(number = details.logs.toString(), label = "Ocorrências")
                    StatCard(number = details.images.toString(), label = "Imagens")
                    StatCard(number = details.docs.toString(), label = "Documentos")
                }
            }
        }
        // Details Section
        DetailItem(label = "Descrição", value = details.description)
        DetailItem(label = "Titular da Licença", value = details.licenseHolder)
        DetailItem(label = "Empresa", value = details.company.toString())
        DetailItem(label = "Endereço", value = details.address.toString())
        DetailItem(label = "Prédio", value = details.building)
    }
}

@Preview
@Composable
fun PreviewDetailsScreen() {
    SiteDiaryPhoneTheme {
        val navController = rememberNavController()
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = { DefaultTopBar() },
            bottomBar = {
                BottomNavigationBar(
                    navController,
                    listOf(
                        BottomNavItem.Work.Details,
                        BottomNavItem.Work.Log,
                        BottomNavItem.Work.CreateLog
                    )
                )
            }
        ) { innerPadding ->
            DetailsScreen(
                details = WorkDetails(
                    name = "Work Name",
                    description = "adsfasg",
                    type = WorkType.RESIDENCIAL,
                    state = WorkState.IN_PROGRESS,
                    licenseHolder = "João Mota",
                    company = ConstructionCompany(
                        name = "Site Diary",
                        num = 1511
                    ),
                    address = Address(
                        location = Location(
                            district = "Lisboa",
                            county = "Lisboa",
                            parish = "Marvila"
                        ),
                        street = "Rua xpto",
                        postalCode = "1234-567",
                    ),
                    building = "Edificio C",
                    technicians = emptyList(),
                    logs = 10,
                    images = 20,
                    docs = 5
                ),
                innerPadding = innerPadding
            )
        }
    }
}