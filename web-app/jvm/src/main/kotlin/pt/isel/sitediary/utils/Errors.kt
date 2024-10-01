package pt.isel.sitediary.utils

import org.springframework.http.ResponseEntity

class Errors(val status: Int, val reason: String) {
    companion object {
        fun response(error: Errors) = ResponseEntity
            .status(error.status)
            .header("Content-Type", "application/problem+json")
            .body<Any>(error.reason)

        val samePassword = Errors(400, "A nova palavra passe não pode ser igual à palavra passe atual.")

        val invalidVerificationDoc = Errors(400, "Documento de verificação inválido.")

        val invalidTechnicians = Errors(400, "Para iniciar uma Obra é necessário ter " +
                "Fiscal, Diretor e Coordenador de Obra.")
        val logNotEditable = Errors(400, "Este registo não pode ser editado.")

        val workFinished = Errors(400, "Não é possível fazer um registo numa obra terminada.")

        val notTechnician = Errors(403, "Não pode criar registo se não for um Técnico.")

        val membersMissing = Errors(400, "É necessário ter pelo menos um Fiscal e um Coordenador de Obra.")

        val workAlreadyFinished = Errors(400, "Obra terminada.")

        val forbidden = Errors(403, "Não tem permissões para aceder a este recurso.")

        val userNotFound = Errors(404, "Utilizador não existe.")

        val emailAlreadyInUse = Errors(400, "Email em uso.")

        val invalidNif = Errors(400, "NIF inválido.")

        val invalidParameter = Errors(400, "Parâmetro inválido.")

        val invalidPhoneNumber = Errors(400, "Número de telemóvel inválido.")

        val invalidRole = Errors(400, "Papel de obra inválido.")

        val invalidLocation = Errors(400, "Localização inválida.")

        val invalidLoginParamCombination = Errors(400, "Nome de utilizador ou password errado.")

        val noUserLoggedIn = Errors(401, "Não tem sessão iniciada.")

        val invalidPassword = Errors(
            400,
            "Palavra passe inválida.\n" +
                    "A palavra passe deve ter no mínimo 8 dígitos, uma letra maiúscula, um número e um símbolo."
        )

        val usernameAlreadyInUse = Errors(400, "Nome de utilizador em uso.")

        val workNotFound = Errors(404, "Obra não existe.")

        val notMember = Errors(403, "Não é membro desta obra.")

        val notAdmin = Errors(403, "Não é dono desta obra.")

        val logNotFound = Errors(404, "Registo não existe.")

        val inviteNotFound = Errors(404, "Convite não existe.")

        val memberNotFound = Errors(404, "Não existe nenhum membro nesta obra com esse nome de utilizador.")

        val logDescriptionTooShort = Errors(400, "Observação demasiado curta. A observação deve ter no mínimo 10 caracteres.")
    }
}