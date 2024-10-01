package pt.isel.sitediary.controllers

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseCookie
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import pt.isel.sitediary.domainmodel.authentication.AuthenticatedUser
import pt.isel.sitediary.domainmodel.user.Password
import pt.isel.sitediary.model.EditProfileInputModel
import pt.isel.sitediary.model.FileModel
import pt.isel.sitediary.model.GetUserModel
import pt.isel.sitediary.model.LoginInputModel
import pt.isel.sitediary.model.LoginOutputModel
import pt.isel.sitediary.model.PendingInputModel
import pt.isel.sitediary.model.SessionInputModel
import pt.isel.sitediary.model.SignUpInputModel
import pt.isel.sitediary.model.TokenModel
import pt.isel.sitediary.service.UserService
import pt.isel.sitediary.utils.Errors
import pt.isel.sitediary.utils.Paths
import pt.isel.sitediary.utils.handleResponse
import java.net.URI

@RestController
@Tag(name = "User", description = "Operations related the User.")
class UserController(private val service: UserService) {

    @PostMapping(Paths.User.SIGN_UP)
    @Operation(summary = "Sign Up", description = "Used to create a user for the application.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201", description = "Successful signup"
            ),
            ApiResponse(
                responseCode = "400", description = "Invalid parameters",
                content = [
                    Content(mediaType = "application/json", schema = Schema(implementation = Errors::class))
                ]
            )
        ]
    )
    fun createUser(@RequestBody user: SignUpInputModel): ResponseEntity<*> {
        val res = service.createUser(user)
        return handleResponse(res) {
            ResponseEntity.created(URI.create("/login")).body(it)
        }
    }

    @PostMapping(Paths.User.LOGIN)
    @Operation(summary = "Login", description = "Endpoint to login user")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "Successful login",
                content = [
                    Content(mediaType = "application/json", schema = Schema(implementation = LoginOutputModel::class))
                ]
            ),
            ApiResponse(
                responseCode = "401", description = "Unauthorized",
                content = [
                    Content(mediaType = "application/json", schema = Schema(implementation = Errors::class))
                ]
            )
        ]
    )
    fun login(@RequestBody u: LoginInputModel, response: HttpServletResponse): ResponseEntity<*> {
        val res = service.login(
            u.user,
            u.password
        )
        return handleResponse(res) {
            val tokenValue = LoginOutputModel(
                userId = it.userId,
                username = it.username,
                token = it.tokenValue,
                role = it.role
            )
            val cookieToken = ResponseCookie
                .from("token", tokenValue.token)
                .path("/")
                .maxAge(it.tokenExpiration.epochSeconds)
                .httpOnly(true)
                .secure(false)
                .build()

            val cookieId = ResponseCookie
                .from("id", tokenValue.userId.toString())
                .path("/")
                .maxAge(it.tokenExpiration.epochSeconds)
                .httpOnly(true)
                .secure(false)
                .build()

            response.addHeader(HttpHeaders.SET_COOKIE, cookieToken.toString())
            response.addHeader(HttpHeaders.SET_COOKIE, cookieId.toString())
            ResponseEntity.ok(tokenValue)
        }
    }

    @GetMapping(Paths.User.SESSION)
    fun checkSession(@RequestBody u: SessionInputModel): ResponseEntity<*> {
        val res = service.checkSession(u.userId, u.token)
        return handleResponse(res) {
            ResponseEntity.status(200).body(it)
        }
    }


    @PostMapping(Paths.User.LOGOUT)
    @Operation(summary = "Logout", description = "Used to logout user")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "Successful logout"
            ),
            ApiResponse(
                responseCode = "401", description = "User not logged in",
                content = [
                    Content(mediaType = "application/json", schema = Schema(implementation = Errors::class))
                ]
            )
        ]
    )
    fun logout(@RequestBody token: TokenModel, response: HttpServletResponse): ResponseEntity<*> {
        val res = service.logout(token.token)
        return handleResponse(res) {
            val cookieToken = Cookie("token", null)
            cookieToken.path = "/"
            cookieToken.maxAge = 0
            cookieToken.isHttpOnly = true
            cookieToken.secure = false

            val cookieId = Cookie("id", null)
            cookieId.path = "/"
            cookieId.maxAge = 0
            cookieId.isHttpOnly = true
            cookieId.secure = false

            response.addCookie(cookieId)
            response.addCookie(cookieToken)
            ResponseEntity.ok(Unit)
        }
    }

    @GetMapping(Paths.User.GET_USER_ID)
    @Operation(summary = "Get profile", description = "Get user profile from user id")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "Successful profile retrieval",
                content = [
                    Content(mediaType = "application/json", schema = Schema(implementation = GetUserModel::class))
                ]
            ),
            ApiResponse(
                responseCode = "404", description = "User does not exist",
                content = [
                    Content(mediaType = "application/json", schema = Schema(implementation = Errors::class))
                ]
            )
        ]
    )
    fun getUserById(@PathVariable id: Int): ResponseEntity<*> {
        val res = service.getUserById(id)
        return handleResponse(res) {
            ResponseEntity.ok(it)
        }
    }

    @GetMapping(Paths.User.GET_USER_BY_USERNAME)
    @Operation(summary = "Get profile", description = "Get user profile using username")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "Successful profile retrieval",
                content = [
                    Content(mediaType = "application/json", schema = Schema(implementation = GetUserModel::class))
                ]
            ),
            ApiResponse(
                responseCode = "404", description = "User does not exist",
                content = [
                    Content(mediaType = "application/json", schema = Schema(implementation = Errors::class))
                ]
            )
        ]
    )
    fun getUserByUsername(
        @PathVariable username: String,
        @Parameter(hidden = true) user: AuthenticatedUser
    ): ResponseEntity<*> {
        val res = service.getUserByUsername(username)
        return handleResponse(res) {
            ResponseEntity.ok(it)
        }
    }

    @PutMapping(Paths.User.GET_USER_ID)
    @Operation(summary = "Edit profile", description = "Edit profile of user")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "Edition of profile accepted",
                content = [
                    Content(mediaType = "application/json", schema = Schema(implementation = GetUserModel::class))
                ]
            ),
            ApiResponse(
                responseCode = "400", description = "Invalid parameters",
                content = [
                    Content(mediaType = "application/json", schema = Schema(implementation = Errors::class))
                ]
            ),
            ApiResponse(
                responseCode = "404", description = "User does not exist",
                content = [
                    Content(mediaType = "application/json", schema = Schema(implementation = Errors::class))
                ]
            )
        ]
    )
    fun editProfile(
        @PathVariable id: Int,
        @RequestBody u: EditProfileInputModel,
        @Parameter(hidden = true) user: AuthenticatedUser
    ) : ResponseEntity<*> {
        val res = service.editProfile(id = id, user = user.user, editUser = u)
        return handleResponse(res) {
            ResponseEntity.ok(Unit)
        }
    }

    @PutMapping(Paths.User.CHANGE_PASSWORD)
    @Operation(summary = "Change Password", description = "Change a user's password")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "Edition of profile accepted",
                content = [
                    Content(mediaType = "application/json", schema = Schema(implementation = GetUserModel::class))
                ]
            ),
            ApiResponse(
                responseCode = "400", description = "Invalid parameters",
                content = [
                    Content(mediaType = "application/json", schema = Schema(implementation = Errors::class))
                ]
            )
        ]
    )
    fun changePassword(
        @RequestBody password: Password,
        @Parameter(hidden = true) user: AuthenticatedUser
    ) : ResponseEntity<*> {
        val res = service.changePassword(password, user.user)
        return handleResponse(res) {
            ResponseEntity.ok(Unit)
        }
    }

    @PutMapping(Paths.User.PROFILE_PICTURE, consumes = ["multipart/form-data"])
    @Operation(
        summary = "Change profile picture",
        description = "Operation used to change the profile picture of a user"
    )
    fun changeProfilePicture(
        @RequestParam("file") file: MultipartFile?,
        @Parameter(hidden = true) user: AuthenticatedUser
    ): ResponseEntity<*> {
        val profilePicture = if (file == null) null else
            FileModel(
                file.bytes,
                file.originalFilename!!,
                file.contentType!!
            )
        val res = service.changeProfilePicture(profilePicture, user.user.id)
        return handleResponse(res) {
            ResponseEntity.ok().header("Location", "/profile").body(Unit)
        }
    }

    @GetMapping(Paths.User.PROFILE_PICTURE)
    @Operation(summary = "Get profile Picture", description = "Gets the profile picture of a user")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "Successful profile picture retrieval",
                content = [
                    Content(schema = Schema(implementation = FileModel::class))
                ]
            )
        ]
    )
    fun getProfilePicture(@Parameter(hidden = true) user: AuthenticatedUser): ResponseEntity<*> {
        val res = service.getProfilePicture(user.user.id)
        return handleResponse(res) {
            if (it == null) ResponseEntity.ok().body(null)
            else ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "Attachment;filename=${it.fileName}")
                .contentType(MediaType.parseMediaType(it.contentType))
                .body(ByteArrayResource(it.file))
        }
    }

    @GetMapping(Paths.User.PROFILE_PICTURE_BY_ID)
    @Operation(summary = "Get profile Picture", description = "Gets the profile picture of a user")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "Successful profile picture retrieval",
                content = [
                    Content(schema = Schema(implementation = FileModel::class))
                ]
            )
        ]
    )
    fun getProfilePictureById(
        @PathVariable id: Int,
        @Parameter(hidden = true) user: AuthenticatedUser
    ): ResponseEntity<*> {
        val res = service.getProfilePicture(id)
        return handleResponse(res) {
            if (it == null) ResponseEntity.ok().body(null)
            else ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "Attachment;filename=${it.fileName}")
                .contentType(MediaType.parseMediaType(it.contentType))
                .body(ByteArrayResource(it.file))
        }
    }

    @GetMapping(Paths.User.PROFILE_PICTURE_BY_USERNAME)
    @Operation(
        summary = "Get profile Picture by Username",
        description = "Gets the profile picture of a user using it's username"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "Successful profile picture retrieval",
                content = [
                    Content(schema = Schema(implementation = FileModel::class))
                ]
            )
        ]
    )
    fun getProfilePictureByUsername(
        @PathVariable username: String,
        @Parameter(hidden = true) user: AuthenticatedUser
    ): ResponseEntity<*> {
        val res = service.getProfilePictureByUsername(username)
        return handleResponse(res) {
            if (it == null) ResponseEntity.ok().body(null)
            else ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "Attachment;filename=${it.fileName}")
                .contentType(MediaType.parseMediaType(it.contentType))
                .body(ByteArrayResource(it.file))
        }
    }

    @GetMapping(Paths.User.PENDING)
    fun getAllPendingCouncils(@Parameter(hidden = true) authUser: AuthenticatedUser): ResponseEntity<*> {
        val res = service.getAllPendingCouncils(authUser.user)
        return handleResponse(res) {
            ResponseEntity.ok().body(it)
        }
    }

    @PutMapping(Paths.User.PENDING)
    fun answerPendingCouncil(
        @RequestBody body: PendingInputModel,
        @Parameter(hidden = true) authUser: AuthenticatedUser
    ): ResponseEntity<*> {
        val res = service.answerPendingCouncil(body, authUser.user)
        return handleResponse(res) {
            ResponseEntity.ok().body(Unit)
        }
    }

    @GetMapping(Paths.User.GET_USER)
    @Operation(summary = "Get all Users", description = "Get all user accounts in the application")
    fun getAllUsers(@Parameter(hidden = true) authUser: AuthenticatedUser): ResponseEntity<*> {
        val res = service.getAllUsers(authUser.user)
        return handleResponse(res) {
            ResponseEntity.ok(it)
        }
    }
}

/*
fun main() {
    val secret = "your_secret_key"
    val algorithm = Algorithm.HMAC256(secret)
    val map = mapOf(
        "id" to 1,
        "username" to "JMota",
        "role" to "admin",
        "token" to "pCRJ4OWSJarFvFV943aebPScJ5u5Y27KWuBbbLbx1mg="
    )
    val jwt = JWT.create().withPayload(map).sign(algorithm)
    println("JWT = $jwt")

    // FRONT-END
    val verifier = JWT.require(algorithm).build()
    val decodedJWT = verifier.verify(jwt)

    val id = decodedJWT.getClaim("id").asInt()
    val username = decodedJWT.getClaim("username").asString()
    val role = decodedJWT.getClaim("role").asString()
    val token = decodedJWT.getClaim("token").asString()

    println("Decoded Info:")
    println("id = $id")
    println("username = $username")
    println("role = $role")
    println("token = $token")
}*/