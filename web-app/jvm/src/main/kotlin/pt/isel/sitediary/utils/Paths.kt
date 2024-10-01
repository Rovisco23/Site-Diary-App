package pt.isel.sitediary.utils

object Paths {

    const val PREFIX = "/api"

    object User {
        const val SIGN_UP = "$PREFIX/signup"
        const val LOGIN = "$PREFIX/login"
        const val LOGOUT = "$PREFIX/logout"
        const val GET_USER_ID = "$PREFIX/users/{id}"
        const val GET_USER = "$PREFIX/users"
        const val SESSION = "$PREFIX/session"
        const val PROFILE_PICTURE = "$PREFIX/profile-picture"
        const val PROFILE_PICTURE_BY_ID = "$PREFIX/profile-picture/{id}"
        const val PROFILE_PICTURE_BY_USERNAME = "$PREFIX/profile-picture-username/{username}"
        const val PENDING = "$PREFIX/pending"
        const val GET_USER_BY_USERNAME = "$PREFIX/users/username/{username}"
        const val CHANGE_PASSWORD = "$PREFIX/change-password"
    }

    object Work {
        const val GET_BY_ID = "$PREFIX/work/{id}"
        const val GET_ALL_WORKS = "$PREFIX/work"
        const val GET_OPENING_TERM = "$PREFIX/opening-term/{id}"
        const val GET_SITE_DIARY = "$PREFIX/site-diary/{id}"
        const val EDIT_WORK = "$PREFIX/work/edit/{id}"
        const val FINISH_WORK = "$PREFIX/finish-work"
        const val GET_IMAGE = "$PREFIX/work-image/{id}"
        const val GET_WORKS_PENDING = "$PREFIX/work-pending"
        const val ANSWER_PENDING = "$PREFIX/work-pending/{id}"
        const val ASK_WORK_VERIFICATION = "$PREFIX/work-verification"
        const val GET_MEMBER_PROFILE = "$PREFIX/member-profile/{id}/{username}"
    }

    object Invite {
        const val GET_INVITE_NUMBER = "$PREFIX/invite-number"
        const val GET_INVITE_LIST = "$PREFIX/invite"
        const val GET_INVITE = "$PREFIX/invite/{id}"
    }

    object Log {
        const val GET_BY_ID = "$PREFIX/logs/{id}"
        const val GET_MY_LOGS = "$PREFIX/my-logs"
        const val GET_ALL_LOGS = "$PREFIX/logs"
        const val GET_LOG_FILES = "$PREFIX/logs-files"
        const val EDIT_LOG = "$PREFIX/logs/{id}"
        const val DELETE_FILES = "$PREFIX/delete-files"
        const val DELETE_FILE = "$PREFIX/delete-file"
    }

}