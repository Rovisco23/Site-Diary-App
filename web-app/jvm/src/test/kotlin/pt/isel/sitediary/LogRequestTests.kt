package pt.isel.sitediary

import org.jdbi.v3.core.Jdbi
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.postgresql.ds.PGSimpleDataSource
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.reactive.function.BodyInserters
import pt.isel.sitediary.model.LogInputModel
import pt.isel.sitediary.repository.transaction.JdbiTransactionManager
import pt.isel.sitediary.utils.Paths
import pt.isel.sitediary.utils.configureWithAppRequirements
import java.util.*

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LogRequestTests {
    private val jdbcDatabaseURL = System.getenv("JDBC_DATABASE_URL")
    private val dataSource = PGSimpleDataSource().apply { setURL(jdbcDatabaseURL) }
    private val transactionManager = JdbiTransactionManager(Jdbi.create(dataSource).configureWithAppRequirements())
    @LocalServerPort
    var port: Int = 0
    private val name = "Joaquimtest1+"
    private val name2 = "Joaquimtest2+"
    private val email = "jtest12345*@gmail.com"
    private val password = "Joaquimtest1+"
    private var id = 0
    private var token = ""
    private var workId = UUID.randomUUID()
    private var logId = 0
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
    private val loginBody = """
        {
            "user": "$name",
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

    @BeforeEach
    fun setup() {
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port").build()
        client.post().uri(Paths.User.SIGN_UP)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(signInBody)
            .exchange()
        client.post().uri(Paths.User.LOGIN)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(loginBody)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.token").value<String> { token = it }
            .jsonPath("$.userId").value<Int> { id = it }
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
        transactionManager.run {
            it.workRepository.deleteWork(id)
            it.usersRepository.deleteUser(name)
            it.usersRepository.deleteUser(name2)
        }
    }

    @Test
    fun `Create Log Test`() {
        val body: MultiValueMap<String, Any> = LinkedMultiValueMap()
        body.add(
            "log",
            LogInputModel(
                workId = workId,
                description = "Isto é um teste de log"
            )
        )
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port").build()
        client.post().uri(Paths.Log.GET_ALL_LOGS)
            .header("Authorization", "Bearer $token")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(body))
            .exchange()
            .expectStatus().isCreated
        transactionManager.run {
            it.logRepository.deleteLog(workId)
        }
    }

    @Test
    fun `Get Logs Test`() {
        val body: MultiValueMap<String, Any> = LinkedMultiValueMap()
        body.add(
            "log",
            LogInputModel(
                workId = workId,
                description = "Isto é um teste de log"
            )
        )
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port").build()
        client.post().uri(Paths.Log.GET_ALL_LOGS)
            .header("Authorization", "Bearer $token")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(body))
            .exchange()
        client.get().uri(Paths.Log.GET_MY_LOGS)
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk
        transactionManager.run {
            it.logRepository.deleteLog(workId)
        }
    }

    @Test
    fun `Get Log By Id Test`() {
        val body: MultiValueMap<String, Any> = LinkedMultiValueMap()
        body.add(
            "log",
            LogInputModel(
                workId = workId,
                description = "Isto é um teste de log"
            )
        )
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port").build()
        client.post().uri(Paths.Log.GET_ALL_LOGS)
            .header("Authorization", "Bearer $token")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(body))
            .exchange()
        client.get().uri(Paths.Log.GET_MY_LOGS)
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectBody()
            .jsonPath("$[0].id").value<Int> { logId = it }
        client.get().uri(Paths.Log.GET_BY_ID, logId)
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk
        transactionManager.run {
            it.logRepository.deleteLog(workId)
        }
    }

    @Test
    fun `Edit Log Test`() {
        val body: MultiValueMap<String, Any> = LinkedMultiValueMap()
        body.add(
            "log",
            LogInputModel(
                workId = workId,
                description = "Isto é um teste de log"
            )
        )
        val editBody: MultiValueMap<String, Any> = LinkedMultiValueMap()
        editBody.add(
            "log",
            LogInputModel(
                workId = workId,
                description = "Isto é um teste de log editada"
            )
        )
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port").build()
        client.post().uri(Paths.Log.GET_ALL_LOGS)
            .header("Authorization", "Bearer $token")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(body))
            .exchange()
        client.get().uri(Paths.Log.GET_MY_LOGS)
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectBody()
            .jsonPath("$[0].id").value<Int> { logId = it }
        client.put().uri(Paths.Log.EDIT_LOG, logId)
            .header("Authorization", "Bearer $token")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(editBody))
            .exchange()
            .expectStatus().isOk
        transactionManager.run {
            it.logRepository.deleteLog(workId)
        }
    }

    @Test
    fun `Get Log Files`() {
        val body: MultiValueMap<String, Any> = LinkedMultiValueMap()
        body.add(
            "log",
            LogInputModel(
                workId = workId,
                description = "Isto é um teste de log"
            )
        )
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port").build()
        client.post().uri(Paths.Log.GET_ALL_LOGS)
            .header("Authorization", "Bearer $token")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(body))
            .exchange()
        client.get().uri(Paths.Log.GET_MY_LOGS)
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectBody()
            .jsonPath("$[0].id").value<Int> { logId = it }
        client.post().uri(Paths.Log.GET_LOG_FILES)
            .header("Authorization", "Bearer $token")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(
                """
                    {
                        "logId": $logId,
                        "workId": "$workId",
                        "files": []
                    }
                """
            )
            .exchange()
            .expectStatus().isOk
        transactionManager.run {
            it.logRepository.deleteLog(workId)
        }
    }
}