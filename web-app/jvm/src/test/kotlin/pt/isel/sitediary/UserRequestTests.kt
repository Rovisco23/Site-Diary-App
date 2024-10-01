package pt.isel.sitediary

import org.jdbi.v3.core.Jdbi
import org.junit.jupiter.api.Assertions.assertTrue
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

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserRequestTests {
    private val jdbcDatabaseURL = System.getenv("JDBC_DATABASE_URL")
    private val dataSource = PGSimpleDataSource().apply { setURL(jdbcDatabaseURL) }
    private val transactionManager = JdbiTransactionManager(Jdbi.create(dataSource).configureWithAppRequirements())
    @LocalServerPort
    var port: Int = 0
    private val name = "Joaquimtest12345*"
    private val editedName = "Joaquimtest12345+"
    private val email = "jtest12345*@gmail.com"
    private val password = "Joaquimtest1+"
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
    private val editBody = """
        {
            "username": "$editedName",
            "firstName": "Joaquim",
            "lastName": "Test",
            "phone": "123456789",
            "location": {
                "district": "Lisboa",
                "county": "Lisboa",
                "parish": "Marvila"
            },
            "association": {
                "name": "Associação",
                "number": 1234
            }  
        }
    """

    @Test
    fun `SignUp test`() {
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port").build()
        client.post().uri(Paths.User.SIGN_UP)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(signInBody)
            .exchange()
            .expectStatus().isCreated
        transactionManager.run {
            it.usersRepository.deleteUser(name)
        }
    }


    @Test
    fun `Login test`() {
        var token = ""
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
            it.usersRepository.deleteUser(name)
        }
    }

    @Test
    fun `Logout test`() {
        var token = ""
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port").build()
        client.post().uri(Paths.User.SIGN_UP)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(signInBody)
            .exchange()
            .expectStatus()
        client.post().uri(Paths.User.LOGIN)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(loginBody)
            .exchange()
            .expectBody()
            .jsonPath("$.token").value<String> { token = it }
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
            .expectStatus().isOk
        transactionManager.run {
            it.usersRepository.deleteUser(name)
        }
    }

    @Test
    fun `Get User By Id Test`() {
        var id = 0
        var token = ""
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port").build()
        client.post().uri(Paths.User.SIGN_UP)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(signInBody)
            .exchange()
        client.post().uri(Paths.User.LOGIN)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(loginBody)
            .exchange()
            .expectBody()
            .jsonPath("$.token").value<String> { token = it }
            .jsonPath("$.userId").value<Int> { id = it }
        client.get().uri(Paths.User.GET_USER_ID, id)
            .exchange()
            .expectStatus().isOk
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
            it.usersRepository.deleteUser(name)
        }
    }

    @Test
    fun `Get User By Username Test`() {
        var token = ""
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port").build()
        client.post().uri(Paths.User.SIGN_UP)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(signInBody)
            .exchange()
        client.post().uri(Paths.User.LOGIN)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(loginBody)
            .exchange()
            .expectBody()
            .jsonPath("$.token").value<String> { token = it }
        client.get().uri(Paths.User.GET_USER_BY_USERNAME, name)
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk
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
            it.usersRepository.deleteUser(name)
        }
    }


    @Test
    fun `Edit User By Username Test`() {
        var id = 0
        var token = ""
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port").build()
        client.post().uri(Paths.User.SIGN_UP)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(signInBody)
            .exchange()
        client.post().uri(Paths.User.LOGIN)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(loginBody)
            .exchange()
            .expectBody()
            .jsonPath("$.token").value<String> { token = it }
            .jsonPath("$.userId").value<Int> { id = it }
        client.put().uri(Paths.User.GET_USER_ID, id)
            .header("Authorization", "Bearer $token")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(editBody)
            .exchange()
            .expectStatus().isOk
        client.get().uri(Paths.User.GET_USER_BY_USERNAME, editedName)
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk
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
            it.usersRepository.deleteUser(editedName)
        }
    }

    @Test
    fun `Change Profile Picture Test`() {
        var id = 0
        var token = ""
        val file = Files.createTempFile("test-profile-picture", ".png").toFile()
        file.writeBytes(ByteArray(1024) { 0 })
        val fileResource = FileSystemResource(file)
        file.deleteOnExit()
        val body: MultiValueMap<String, Any> = LinkedMultiValueMap()
        body.add("file", fileResource)
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port").build()
        client.post().uri(Paths.User.SIGN_UP)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(signInBody)
            .exchange()
        client.post().uri(Paths.User.LOGIN)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(loginBody)
            .exchange()
            .expectBody()
            .jsonPath("$.token").value<String> { token = it }
            .jsonPath("$.userId").value<Int> { id = it }
        client.put().uri(Paths.User.PROFILE_PICTURE)
            .header("Authorization", "Bearer $token")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(body))
            .exchange()
            .expectStatus().isOk
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
            it.usersRepository.removeProfilePicture(id)
            it.usersRepository.deleteUser(name)
        }
    }


    @Test
    fun `Remove Profile Picture Test`() {
        var token = ""
        val file = Files.createTempFile("test-profile-picture", ".png").toFile()
        file.writeBytes(ByteArray(1024) { 0 })
        val fileResource = FileSystemResource(file)
        file.deleteOnExit()
        val body: MultiValueMap<String, Any> = LinkedMultiValueMap()
        body.add("file", fileResource)
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port").build()
        client.post().uri(Paths.User.SIGN_UP)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(signInBody)
            .exchange()
        client.post().uri(Paths.User.LOGIN)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(loginBody)
            .exchange()
            .expectBody()
            .jsonPath("$.token").value<String> { token = it }
        client.put().uri(Paths.User.PROFILE_PICTURE)
            .header("Authorization", "Bearer $token")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(body))
            .exchange()
        val removeBody: MultiValueMap<String, Any?> = LinkedMultiValueMap()
        removeBody.add("file", null)
        client.put().uri(Paths.User.PROFILE_PICTURE)
            .header("Authorization", "Bearer $token")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(LinkedMultiValueMap<String, Any>()))
            .exchange()
            .expectStatus().isOk
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
            it.usersRepository.deleteUser(name)
        }
    }

    @Test
    fun `Get Profile Picture Test`() {
        var id = 0
        var token = ""
        val file = Files.createTempFile("test-profile-picture", ".png").toFile()
        file.writeBytes(ByteArray(1024) { 0 })
        val fileResource = FileSystemResource(file)
        file.deleteOnExit()
        val body: MultiValueMap<String, Any> = LinkedMultiValueMap()
        body.add("file", fileResource)
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port").build()
        client.post().uri(Paths.User.SIGN_UP)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(signInBody)
            .exchange()
        client.post().uri(Paths.User.LOGIN)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(loginBody)
            .exchange()
            .expectBody()
            .jsonPath("$.token").value<String> { token = it }
            .jsonPath("$.userId").value<Int> { id = it }
        client.put().uri(Paths.User.PROFILE_PICTURE)
            .header("Authorization", "Bearer $token")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(body))
            .exchange()
        client.get().uri(Paths.User.PROFILE_PICTURE)
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .consumeWith {
                assertTrue(it.responseBody != null)
            }
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
            it.usersRepository.removeProfilePicture(id)
            it.usersRepository.deleteUser(name)
        }
    }

    @Test
    fun `Get Profile Picture By Id Test`() {
        var id = 0
        var token = ""
        val file = Files.createTempFile("test-profile-picture", ".png").toFile()
        file.writeBytes(ByteArray(1024) { 0 })
        val fileResource = FileSystemResource(file)
        file.deleteOnExit()
        val body: MultiValueMap<String, Any> = LinkedMultiValueMap()
        body.add("file", fileResource)
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port").build()
        client.post().uri(Paths.User.SIGN_UP)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(signInBody)
            .exchange()
        client.post().uri(Paths.User.LOGIN)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(loginBody)
            .exchange()
            .expectBody()
            .jsonPath("$.token").value<String> { token = it }
            .jsonPath("$.userId").value<Int> { id = it }
        client.put().uri(Paths.User.PROFILE_PICTURE)
            .header("Authorization", "Bearer $token")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(body))
            .exchange()
        client.get().uri(Paths.User.PROFILE_PICTURE_BY_ID, id)
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .consumeWith {
                assertTrue(it.responseBody != null)
            }
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
            it.usersRepository.removeProfilePicture(id)
            it.usersRepository.deleteUser(name)
        }
    }

    @Test
    fun `Get Profile Picture By Username Test`() {
        var id = 0
        var token = ""
        val file = Files.createTempFile("test-profile-picture", ".png").toFile()
        file.writeBytes(ByteArray(1024) { 0 })
        val fileResource = FileSystemResource(file)
        file.deleteOnExit()
        val body: MultiValueMap<String, Any> = LinkedMultiValueMap()
        body.add("file", fileResource)
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port").build()
        client.post().uri(Paths.User.SIGN_UP)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(signInBody)
            .exchange()
        client.post().uri(Paths.User.LOGIN)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(loginBody)
            .exchange()
            .expectBody()
            .jsonPath("$.token").value<String> { token = it }
            .jsonPath("$.userId").value<Int> { id = it }
        client.put().uri(Paths.User.PROFILE_PICTURE)
            .header("Authorization", "Bearer $token")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(body))
            .exchange()
        client.get().uri(Paths.User.PROFILE_PICTURE_BY_USERNAME, name)
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .consumeWith {
                assertTrue(it.responseBody != null)
            }
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
            it.usersRepository.removeProfilePicture(id)
            it.usersRepository.deleteUser(name)
        }
    }
}