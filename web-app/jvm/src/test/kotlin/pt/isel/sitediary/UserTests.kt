package pt.isel.sitediary

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import pt.isel.sitediary.domainmodel.user.Member
import pt.isel.sitediary.domainmodel.user.checkOwner
import pt.isel.sitediary.domainmodel.user.containsMemberById
import pt.isel.sitediary.domainmodel.work.Association
import pt.isel.sitediary.domainmodel.work.Location
import pt.isel.sitediary.model.EditProfileInputModel
import pt.isel.sitediary.model.SignUpInputModel

class UserTests {
    private val members = listOf(
        Member(1, "user1", "MEMBRO"),
        Member(2, "user2", "ESPECTADOR"),
        Member(3, "user3", "DONO"),
        Member(4, "user4", "FISCALIZAÇÃO"),
    )

    private val signUpModel = SignUpInputModel(
        email = "email",
        role = "role",
        username = "username",
        password = "password",
        firstName = "firstName",
        lastName = "lastName",
        nif = 123456789,
        phone = "911234567",
        district = "Lisboa",
        county = "Lisboa",
        parish = "Marvila",
        associationName = "associationName",
        associationNum = 1
    )

    private val editProfileModel = EditProfileInputModel(
        username = "username",
        firstName = "firstName",
        lastName = "lastName",
        phone = "911234567",
        location = Location("Lisboa", "Lisboa", "Marvila"),
        association = Association("associationName", 1)
    )

    @Test
    fun `Check parameters of the SignUpInputModel test`() {
        assertTrue(!signUpModel.checkParameters())
    }

    @Test
    fun `Check parameters of the SignUpInputModel are wrong test`() {
        assertTrue(signUpModel.copy(email = "").checkParameters())
    }

    @Test
    fun `Check phone number in the right format test`() {
        assertTrue(signUpModel.checkPhoneNumberFormat())
    }

    @Test
    fun `Check phone number is null test`() {
        assertTrue(signUpModel.copy(phone = null).checkPhoneNumberFormat())
    }

    @Test
    fun `Check phone number is blank test`() {
        assertTrue(signUpModel.copy(phone = "").checkPhoneNumberFormat())
    }

    @Test
    fun `Check phone number in the wrong format test`() {
        assertTrue(!signUpModel.copy(phone = "91978324").checkPhoneNumberFormat())
    }

    @Test
    fun `Right size NIF test`() {
        assertTrue(signUpModel.checkNifSize())
    }

    @Test
    fun `Wrong size NIF test`() {
        assertTrue(!signUpModel.copy(nif = 12345678).checkNifSize())
    }

    @Test
    fun `Check phone number in the right format test for edit`() {
        assertTrue(editProfileModel.checkPhoneNumberFormat())
    }

    @Test
    fun `Check phone number is blank test for edit`() {
        assertTrue(signUpModel.copy(phone = "").checkPhoneNumberFormat())
    }

    @Test
    fun `Check phone number in the wrong format test for edit`() {
        assertTrue(!signUpModel.copy(phone = "91978324").checkPhoneNumberFormat())
    }

    @Test
    fun `Member is in the members list test`() {
        assertTrue(members.containsMemberById(1))
    }

    @Test
    fun `Member is not in the members list`() {
        assertTrue(!members.containsMemberById(5))
    }

    @Test
    fun `Member is the owner of the Work`() {
        assertTrue(members.checkOwner(3))
    }

    @Test
    fun `Member is not the owner of the Work`() {
        assertTrue(!members.checkOwner(4))
    }

    @Test
    fun `Member is a technician`() {
        assertTrue(members[2].checkTechnician())
        assertTrue(members[3].checkTechnician())
    }

    @Test
    fun `Member is not a technician`() {
        assertTrue(!members[0].checkTechnician())
        assertTrue(!members[1].checkTechnician())
    }
}