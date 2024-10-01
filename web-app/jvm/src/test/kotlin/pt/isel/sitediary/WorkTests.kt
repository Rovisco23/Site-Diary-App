package pt.isel.sitediary

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import pt.isel.sitediary.domainmodel.user.Technician
import pt.isel.sitediary.domainmodel.user.User
import pt.isel.sitediary.domainmodel.work.Address
import pt.isel.sitediary.domainmodel.work.Association
import pt.isel.sitediary.domainmodel.work.ConstructionCompany
import pt.isel.sitediary.domainmodel.work.InviteSimplified
import pt.isel.sitediary.domainmodel.work.Location
import pt.isel.sitediary.model.OpeningTermInputModel
import java.util.*

class WorkTests {
    private val invites = listOf(
        InviteSimplified(
            workId = UUID.randomUUID(),
            workTitle = "Obra de teste",
            role = "MEMBRO",
            admin = "João Mota"
        ),
        InviteSimplified(
            workId = UUID.randomUUID(),
            workTitle = "Obra de teste",
            role = "ESPECTADOR",
            admin = "João Mota"
        ),
        InviteSimplified(
            workId = UUID.randomUUID(),
            workTitle = "Obra de teste",
            role = "FISCALIZAÇÃO",
            admin = "João Mota"
        )
    )

    private val openingTerm = OpeningTermInputModel(
        name = "Obra de teste",
        type = "Residencial",
        description = "",
        holder = "João Mota",
        company = ConstructionCompany(
            name = "Construction Company",
            num = 1234
        ),
        building = "Predio C",
        address = Address(
            location = Location(
                parish = "Marvila",
                county = "Lisboa",
                district = "Lisboa"
            ),
            street = "Rua do Ouro",
            postalCode = "1234-567"
        ),
        technicians = listOf(
            Technician(
                name = "João Mota",
                email = "",
                role = "DIRETOR",
                association = Association(
                    name = "Associação",
                    number = 1234
                )
            ),
            Technician(
                name = "João Mota",
                email = "",
                role = "FISCALIZAÇÃO",
                association = Association(
                    name = "Associação",
                    number = 1234
                )
            ),
            Technician(
                name = "João Mota",
                email = "",
                role = "COORDENADOR",
                association = Association(
                    name = "Associação",
                    number = 1234
                )
            )
        ),
        verification = ""
    )

    private val user = User(
        id = 1,
        username = "Test",
        name = "Utilizador de Teste",
        nif = 123456789,
        email = "test@gmail.com",
        phone = "912345678",
        role = "CÂMARA",
        location = Location(
            district = "Lisboa",
            county = "Lisboa",
            parish = "Marvila"
        ),
        association = Association(
            name = "Associação",
            number = 1234
        )
    )

    @Test
    fun `Check parameters of the OpeningTermInputModel test`() {
        assertTrue(!openingTerm.checkParams())
    }

    @Test
    fun `Check parameters of the OpeningTermInputModel are wrong test`() {
        assertTrue(openingTerm.copy(name = "").checkParams())
    }

    @Test
    fun `Check Opening Term has all mandatory technicians`() {
        assertTrue(!openingTerm.checkTechnicians())
    }

    @Test
    fun `Check Opening Term doesn't have all mandatory technicians`() {
        assertTrue(openingTerm.copy(technicians = emptyList()).checkTechnicians())
    }

    @Test
    fun `Check work is a council work`() {
        assertTrue(openingTerm.checkCouncilWork(user))
    }

    @Test
    fun `Check work isn't a council work`() {
        assertTrue(!openingTerm.checkCouncilWork(user.copy(role = "OPERÁRIO")))
    }

    @Test
    fun `Check if the invite is for a Technician`() {
        invites[2].checkTechnician()
    }

    @Test
    fun `Check if the invite isn't for a Technician`() {
        invites[0].checkTechnician()
        invites[1].checkTechnician()
    }
}