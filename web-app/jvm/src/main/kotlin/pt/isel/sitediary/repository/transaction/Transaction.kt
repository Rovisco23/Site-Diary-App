package pt.isel.sitediary.repository.transaction

import pt.isel.sitediary.repository.*

interface Transaction {
    val usersRepository: UserRepository
    val workRepository: WorkRepository
    val tokenRepository: TokenRepository
    val addressRepository: AddressRepository
    val logRepository: LogRepository
}