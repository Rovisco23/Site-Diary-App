package pt.isel.sitediary.repository

import pt.isel.sitediary.domainmodel.user.User
import pt.isel.sitediary.domainmodel.work.*
import pt.isel.sitediary.model.EditWorkInputModel
import pt.isel.sitediary.model.FileModel
import pt.isel.sitediary.model.OpeningTermInputModel
import java.sql.Timestamp
import java.util.*

interface WorkRepository {
    fun createWork(work: WorkInput, createdAt: Timestamp, openingTerm: OpeningTermInputModel, user: User)
    fun getById(id: UUID): Work?
    fun getWorkList(userId: Int): List<WorkSimplified>
    fun getOpeningTerm(workId: UUID): OpeningTerm
    fun inviteMembers(invites: List<Invite>)
    fun getInviteList(userId: Int): List<InviteSimplified>
    fun getInvite(workId: UUID, userId: Int): InviteSimplified?
    fun acceptInvite(inv: InviteSimplified, user: User)
    fun declineInvite(workId: UUID, userId: Int)
    fun getWorkListAdmin(): List<WorkSimplified>
    fun getWorkListCouncil(location: Location, user: User): List<WorkSimplified>
    fun getWorkImage(workId: UUID): FileModel?
    fun checkWorkImageExists(workId: UUID): UUID?
    fun insertWorkImage(workId: UUID, featuredImage: FileModel)
    fun changeWorkImage(workId: UUID, featuredImage: FileModel)
    fun removeWorkImage(workId: UUID)
    fun finishWork(workId: UUID)
    fun inviteMember(id: Int, role: String, workId: UUID)
    fun checkRequiredTechnicians(workId: UUID): Boolean
    fun getNumberOfInvites(id: Int): Int
    fun getAllWorksPending(): List<WorkVerifying>
    fun getWorksPending(location: Location): List<WorkVerifying>
    fun acceptPending(workId: UUID, user: String, dateAuth: Timestamp)
    fun declinePending(workId: UUID, user: String, dateAuth: Timestamp)
    fun getMemberProfile(workId: String, member: String): MemberProfile?
    fun editWork(workId: UUID, editWork: EditWorkInputModel)
    fun checkInvited(id: Int, workId: UUID): Boolean
    fun askWorkVerification(id: UUID, doc: String)
    fun deleteWork(id: Int)
    fun getSiteDiary(workId: UUID): SiteDiary
}