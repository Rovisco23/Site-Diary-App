package pt.isel.sitediary

import org.jdbi.v3.core.Jdbi
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.postgresql.ds.PGSimpleDataSource
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.core.io.FileSystemResource
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.reactive.function.BodyInserters
import pt.isel.sitediary.repository.transaction.JdbiTransactionManager
import pt.isel.sitediary.utils.Paths
import pt.isel.sitediary.utils.configureWithAppRequirements
import java.nio.file.Files
import java.util.*

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WorkRequestTests {
    private val jdbcDatabaseURL = System.getenv("JDBC_DATABASE_URL")
    private val dataSource = PGSimpleDataSource().apply { setURL(jdbcDatabaseURL) }
    private val transactionManager = JdbiTransactionManager(Jdbi.create(dataSource).configureWithAppRequirements())
    @LocalServerPort
    var port: Int = 0
    private val name = "Joaquimtest1+"
    private val name2 = "Joaquimtest2+"
    private val email = "jtest12345*@gmail.com"
    private val email2 = "jtest67890*@gmail.com"
    private val password = "Joaquimtest1+"
    private var id = 0
    private var id2 = 0
    private var token = ""
    private var token2 = ""
    private var workId = UUID.randomUUID()
    private val signInBody = """
        {
            "email": "$email",
            "role": "OPERÁRIO",
            "username": "$name",
            "password": "$password",
            "firstName": "Joaquim",
            "lastName": "Test",
            "nif": 123456789,
            "phone": "123456789",
            "district": "Lisboa",
            "parish": "Marvila",
            "county": "Lisboa",
            "associationName": "Associação",
            "associationNum": 1234
        }
    """
    private val signInBody2 = """
        {
            "email": "$email2",
            "role": "OPERÁRIO",
            "username": "$name2",
            "password": "$password",
            "firstName": "Joaquim",
            "lastName": "Test",
            "nif": 123456780,
            "phone": "012345678",
            "district": "Lisboa",
            "parish": "Marvila",
            "county": "Lisboa",
            "associationName": "Associação",
            "associationNum": 12345
        }
    """
    private val loginBody = """
        {
            "user": "$name",
            "password": "$password"
        }
    """
    private val loginBody2 = """
        {
            "user": "$name2",
            "password": "$password"
        }
    """
    private val createWorkBody = """
        {
            "name": "Obra de Teste",
            "type": "RESIDENCIAL",
            "description": "Isto é uma obra de teste",
            "holder": "Teste 1",
            "company": {
                "name": "Companhia de Construção",
                "num": 1234
            },
            "building": "Predio C",
            "address": {
                "location": {
                    "district": "Lisboa",
                    "county": "Lisboa",
                    "parish": "Marvila"
                },
                "street": "Rua de teste",
                "postalCode": "1234-567"
            },
            "technicians": [
                {
                    "name": "Teste 2",
                    "email": "test2@gmail.com",
                    "role": "DIRETOR",
                    "association": {
                        "name": "Associação",
                        "number": 1
                    }
                },
                {
                    "name": "Teste 3",
                    "email": "test3@gmail.com",
                    "role": "FISCALIZAÇÃO",
                    "association": {
                        "name": "Associação",
                        "number": 2
                    }
                },
                {
                    "name": "Teste 4",
                    "email": "test4@gmail.com",
                    "role": "COORDENADOR",
                    "association": {
                        "name": "Associação",
                        "number": 3
                    }
                }
            ]
        }
    """
    private val inviteBody = """
        [
            {
                "email": "$email2",
                "role": "MEMBRO"
            }
        ]
    """
    private val editBody = """
        {
            "name": "Obra de Teste Editada",
            "type": "RESIDENCIAL",
            "description": "Isto é uma obra de teste",
            "licenseHolder": "Teste 1",
            "company": {
                "name": "Companhia de Construção",
                "num": 1234
            },
            "building": "Predio C",
            "address": {
                "location": {
                    "district": "Lisboa",
                    "county": "Lisboa",
                    "parish": "Marvila"
                },
                "street": "Rua de teste",
                "postalCode": "1234-567"
            },
            "technicians": [
                {
                    "name": "Teste 2",
                    "email": "test2@gmail.com",
                    "role": "DIRETOR",
                    "association": {
                        "name": "Associação",
                        "number": 1
                    }
                },
                {
                    "name": "Teste 3",
                    "email": "test3@gmail.com",
                    "role": "FISCALIZAÇÃO",
                    "association": {
                        "name": "Associação",
                        "number": 2
                    }
                },
                {
                    "name": "Teste 4",
                    "email": "test4@gmail.com",
                    "role": "COORDENADOR",
                    "association": {
                        "name": "Associação",
                        "number": 3
                    }
                }
            ]
        }
    """

    @BeforeEach
    fun setup() {
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port").build()
        client.post().uri(Paths.User.SIGN_UP)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(signInBody)
            .exchange()
        client.post().uri(Paths.User.SIGN_UP)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(signInBody2)
            .exchange()
        client.post().uri(Paths.User.LOGIN)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(loginBody)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.token").value<String> { token = it }
            .jsonPath("$.userId").value<Int> { id = it }
        client.post().uri(Paths.User.LOGIN)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(loginBody2)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.token").value<String> { token2 = it }
            .jsonPath("$.userId").value<Int> { id2 = it }
    }

    @AfterEach
    fun cleanup() {
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port").build()
        client.post().uri(Paths.User.LOGOUT)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(
                """
                    {
                        "token": "$token"
                    }
                """
            )
            .exchange()
        client.post().uri(Paths.User.LOGOUT)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(
                """
                    {
                        "token": "$token2"
                    }
                """
            )
            .exchange()
        transactionManager.run {
            it.usersRepository.deleteUser(name)
            it.usersRepository.deleteUser(name2)
        }
    }

    @Test
    fun `Create Work Test`() {
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port").build()
        client.post().uri(Paths.Work.GET_ALL_WORKS)
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer $token")
            .bodyValue(createWorkBody)
            .exchange()
            .expectStatus().isCreated
        transactionManager.run {
            it.workRepository.deleteWork(id)
        }
    }

    @Test
    fun `Get All Works Test`() {
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port").build()
        client.get().uri(Paths.Work.GET_ALL_WORKS)
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk
    }

    @Test
    fun `Get Work By Id Test`() {
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port").build()
        client.post().uri(Paths.Work.GET_ALL_WORKS)
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer $token")
            .bodyValue(createWorkBody)
            .exchange()
        client.get().uri(Paths.Work.GET_ALL_WORKS)
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectBody()
            .jsonPath("$[0].id").value<String> { workId = UUID.fromString(it) }
        client.get().uri(Paths.Work.GET_BY_ID, workId)
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk
        transactionManager.run {
            it.workRepository.deleteWork(id)
        }
    }

    @Test
    fun `Ask Work Verification Test`() {
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port").build()
        client.post().uri(Paths.Work.GET_ALL_WORKS)
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer $token")
            .bodyValue(createWorkBody)
            .exchange()
        client.get().uri(Paths.Work.GET_ALL_WORKS)
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectBody()
            .jsonPath("$[0].id").value<String> { workId = UUID.fromString(it) }
        client.put().uri(Paths.Work.ASK_WORK_VERIFICATION)
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer $token")
            .bodyValue(
                """
                    {
                        "workId": "$workId",
                        "verificationDoc": "Autorização"
                    }
                """
            )
            .exchange()
            .expectStatus().isOk
        transactionManager.run {
            it.workRepository.deleteWork(id)
        }
    }

    @Test
    fun `Invite Members Test`() {
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port").build()
        client.post().uri(Paths.Work.GET_ALL_WORKS)
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer $token")
            .bodyValue(createWorkBody)
            .exchange()
        client.get().uri(Paths.Work.GET_ALL_WORKS)
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectBody()
            .jsonPath("$[0].id").value<String> { workId = UUID.fromString(it) }
        client.post().uri(Paths.Invite.GET_INVITE, workId)
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer $token")
            .bodyValue(inviteBody)
            .exchange()
            .expectStatus().isOk
        client.get().uri(Paths.Invite.GET_INVITE_NUMBER)
            .header("Authorization", "Bearer $token2")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .consumeWith {
                assertTrue(it.responseBody.contentEquals("1".toByteArray()))
            }
        transactionManager.run {
            it.workRepository.deleteWork(id)
        }
    }

    @Test
    fun `Get Invite Test`() {
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port").build()
        client.post().uri(Paths.Work.GET_ALL_WORKS)
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer $token")
            .bodyValue(createWorkBody)
            .exchange()
        client.get().uri(Paths.Work.GET_ALL_WORKS)
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectBody()
            .jsonPath("$[0].id").value<String> { workId = UUID.fromString(it) }
        client.post().uri(Paths.Invite.GET_INVITE, workId)
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer $token")
            .bodyValue(inviteBody)
            .exchange()
        client.get().uri(Paths.Invite.GET_INVITE, workId)
            .header("Authorization", "Bearer $token2")
            .exchange()
            .expectStatus().isOk
        transactionManager.run {
            it.workRepository.deleteWork(id)
        }
    }

    @Test
    fun `Answer Invite Test`() {
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port").build()
        client.post().uri(Paths.Work.GET_ALL_WORKS)
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer $token")
            .bodyValue(createWorkBody)
            .exchange()
        client.get().uri(Paths.Work.GET_ALL_WORKS)
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectBody()
            .jsonPath("$[0].id").value<String> { workId = UUID.fromString(it) }
        client.post().uri(Paths.Invite.GET_INVITE, workId)
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer $token")
            .bodyValue(inviteBody)
            .exchange()
        client.put().uri(Paths.Invite.GET_INVITE_LIST)
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer $token2")
            .bodyValue("""
                {
                    "workId": "$workId",
                    "accepted": true
                }
            """)
            .exchange()
            .expectStatus().isOk
        transactionManager.run {
            it.workRepository.deleteWork(id)
        }
    }

    @Test
    fun `Get Member Profile Test`() {
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port").build()
        client.post().uri(Paths.Work.GET_ALL_WORKS)
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer $token")
            .bodyValue(createWorkBody)
            .exchange()
        client.get().uri(Paths.Work.GET_ALL_WORKS)
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectBody()
            .jsonPath("$[0].id").value<String> { workId = UUID.fromString(it) }
        client.post().uri(Paths.Invite.GET_INVITE, workId)
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer $token")
            .bodyValue(inviteBody)
            .exchange()
        client.put().uri(Paths.Invite.GET_INVITE_LIST)
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer $token2")
            .bodyValue("""
                {
                    "workId": "$workId",
                    "accepted": true
                }
            """)
            .exchange()
            .expectStatus().isOk
        client.get().uri(Paths.Work.GET_MEMBER_PROFILE, workId, name2)
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk
        transactionManager.run {
            it.workRepository.deleteWork(id)
        }
    }

    @Test
    fun `Edit Work Test`() {
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port").build()
        client.post().uri(Paths.Work.GET_ALL_WORKS)
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer $token")
            .bodyValue(createWorkBody)
            .exchange()
        client.get().uri(Paths.Work.GET_ALL_WORKS)
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectBody()
            .jsonPath("$[0].id").value<String> { workId = UUID.fromString(it) }
        client.put().uri(Paths.Work.EDIT_WORK, workId)
            .header("Authorization", "Bearer $token")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(editBody)
            .exchange()
            .expectStatus().isOk
        transactionManager.run {
            it.workRepository.deleteWork(id)
        }
    }

    @Test
    fun `Finish Work Test`() {
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port").build()
        client.post().uri(Paths.Work.GET_ALL_WORKS)
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer $token")
            .bodyValue(createWorkBody)
            .exchange()
        client.get().uri(Paths.Work.GET_ALL_WORKS)
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectBody()
            .jsonPath("$[0].id").value<String> { workId = UUID.fromString(it) }
        client.post().uri(Paths.Work.FINISH_WORK + "?work=$workId")
            .header("Authorization", "Bearer $token")
            .contentType(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
        transactionManager.run {
            it.workRepository.deleteWork(id)
        }
    }

    @Test
    fun `Change Work Image`() {
        val file = Files.createTempFile("test-work-picture", ".png").toFile()
        file.writeBytes(ByteArray(1024) { 0 })
        val fileResource = FileSystemResource(file)
        file.deleteOnExit()
        val body: MultiValueMap<String, Any> = LinkedMultiValueMap()
        body.add("file", fileResource)
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port").build()
        client.post().uri(Paths.Work.GET_ALL_WORKS)
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer $token")
            .bodyValue(createWorkBody)
            .exchange()
        client.get().uri(Paths.Work.GET_ALL_WORKS)
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectBody()
            .jsonPath("$[0].id").value<String> { workId = UUID.fromString(it) }
        client.put().uri(Paths.Work.GET_IMAGE, workId)
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .header("Authorization", "Bearer $token")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(body))
            .exchange()
            .expectStatus().isOk
        transactionManager.run {
            it.workRepository.deleteWork(id)
        }
    }

    @Test
    fun `Remove Work Image`() {
        val file = Files.createTempFile("test-work-picture", ".png").toFile()
        file.writeBytes(ByteArray(1024) { 0 })
        val fileResource = FileSystemResource(file)
        file.deleteOnExit()
        val body: MultiValueMap<String, Any> = LinkedMultiValueMap()
        body.add("file", fileResource)
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port").build()
        client.post().uri(Paths.Work.GET_ALL_WORKS)
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer $token")
            .bodyValue(createWorkBody)
            .exchange()
        client.get().uri(Paths.Work.GET_ALL_WORKS)
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectBody()
            .jsonPath("$[0].id").value<String> { workId = UUID.fromString(it) }
        client.put().uri(Paths.Work.GET_IMAGE, workId)
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .header("Authorization", "Bearer $token")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(body))
            .exchange()
        client.put().uri(Paths.Work.GET_IMAGE, workId)
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .header("Authorization", "Bearer $token")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(LinkedMultiValueMap<String, Any>()))
            .exchange()
            .expectStatus().isOk
        transactionManager.run {
            it.workRepository.deleteWork(id)
        }
    }

    @Test
    fun `Get Work Image`() {
        val file = Files.createTempFile("test-work-picture", ".png").toFile()
        file.writeBytes(ByteArray(1024) { 0 })
        val fileResource = FileSystemResource(file)
        file.deleteOnExit()
        val body: MultiValueMap<String, Any> = LinkedMultiValueMap()
        body.add("file", fileResource)
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port").build()
        client.post().uri(Paths.Work.GET_ALL_WORKS)
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer $token")
            .bodyValue(createWorkBody)
            .exchange()
        client.get().uri(Paths.Work.GET_ALL_WORKS)
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectBody()
            .jsonPath("$[0].id").value<String> { workId = UUID.fromString(it) }
        client.put().uri(Paths.Work.GET_IMAGE, workId)
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .header("Authorization", "Bearer $token")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(body))
            .exchange()
        client.get().uri(Paths.Work.GET_IMAGE, workId)
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk
        transactionManager.run {
            it.workRepository.deleteWork(id)
        }
    }
}