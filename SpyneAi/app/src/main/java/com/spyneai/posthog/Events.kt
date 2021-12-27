package com.spyneai.posthog

object Events {
    val VIDEO_PROCESS_FAILED = "VIDEO_PROCESS_FAILED"
    val VIDEO_PROCESSED = "VIDEO_PROCESSED"
    val VIDEO_PROCESS_CALL_INITIATED = "VIDEO_PROCESS_CALL_INITIATED"
    val VIDEO_NOT_TRIMMED = "VIDEO_NOT_TRIMMED"
    val VIDEO_INVALID_TRIMMED = "VIDEO_INVALID_TRIMMED"
    val VIDEO_TRIMMED= "VIDEO_TRIMMED"
    val VIDEO_CAPTURE_FAILED= "VIDEO_CAPTURE_FAILED"
    val VIDEO_CAPTURED = "VIDEO_CAPTURED"
    val VIDEO_MARKED_UPLOADED = "VIDEO_MARKED_UPLOADED"
    val VIDEO_MARK_DONE_CALL_INITIATED = "VIDEO_MARK_DONE_CALL_INITIATED"
    val IS_VIDEO_GCP_UPLOADED_UPDATED = "IS_VIDEO_GCP_UPLOADED_UPDATED"
    val GOT_VIDEO_PRESIGNED_VIDEO_URL = "GOT_VIDEO_PRESIGNED_VIDEO_URL"
    val GET_VIDEO_PRESIGNED_FAILED = "GET_VIDEO_PRESIGNED_FAILED"
    val GET_VIDEO_PRESIGNED_CALL_INITIATED = "GET_VIDEO_PRESIGNED_CALL_INITIATED"
    val VIDEO_ID_NULL = "VIDEO_ID_NULL"
    val VIDEO_MAX_RETRY = "VIDEO_MAX_RETRY"
    val VIDEO_SELECTED = "VIDEO_SELECTED"
    val ALL_VIDEO_UPLOADED_BREAK = "ALL_VIDEO_UPLOADED_BREAK"
    val VIDEO_CONNECTION_BREAK = "VIDEO_CONNECTION_BREAK"
    val VIDEO_UPLOAD_PARENT_TRIGGERED: String = "VIDEO_UPLOAD_PARENT_TRIGGERED"

    //on Boarding
    val SLIDE_CHANGE = "Android On Boarding Slide Changed"


    //Signup
    val SIGNUP_INTIATED = "Android Signup Initiated"
    val SIGNUP_SUCCEED = "Android Signup Succeed"
    val SIGNUP_FAILED = "Android Signup Failed"

    //Login with password
    val LOGIN_INTIATED = "Android Login Initiated"
    val LOGIN_SUCCEED = "Android Login Succeed"
    val LOGIN_FAILED = "Android Login Failed"


    //login with otp
    val OTP_LOGIN_INTIATED = "Android OTP Login Initiated"
    val OTP_LOGIN_SUCCEED = "Android OTP Login Succeed"
    val OTP_LOGIN_FAILED = "Android OTP Login Failed"

    // otp verification & resend otp
    val OTP_VERIFICATION_INITIATED = "Android OTP Verification Initiated"
    val OTP_VERIFIED = "Android OTP Verified"
    val OTP_VERIFICATION_FAILED = "Android OTP Verification Failed"
    val OTP_RESEND_INITIATED = "Android OTP Resend Initiated"
    val OTP_RESENT = "Android OTP Resent"
    val OTP_RESENT_FAILED = "Android OTP Resent Failed"

    //forgot password
    val FORGOT_PASSWORD_INTIATED = "Android Forgot Password Initiated"
    val FORGOT_PASSWORD_MAIL_SENT = "Android Forgot Password Mail Sent"
    val FORGOT_PASSWORD_FAILED = "Android Forgot Password Failed"

    //home
    val GOT_CATEGORIES = "Android Got Categories"
    val GET_CATEGORIES_FAILED = "Android Get Categories Failed"

    val GOT_ONGOING_ORDERS = "Android Got Ongoing Orders"
    val GET_ONGOING_ORDERS_FAILED = "Android Get Ongoing Orders Failed"

    val GET_COMPLETED_ORDERS = "Android Got Completed Orders"
    val GET_COMPLETED_ORDERS_FAILED = "Android Get Completed Orders Failed"

    //wallet
    val FETCH_CREDITS = "Android Credits Fetched"
    val FETCH_CREDITS_FAILED = "Android Credits Fetch Failed"

    //logout
    val LOG_OUT = "Android Log Out"

    //shoot
    val SHOW_HINT = "Android Show Hint"
    val SHOW_SHADOW_DIALOG = "Android Show Choose Shadow Dialog"
    val CREATE_PROJECT = "Android Create Project"
    val CREATE_PROJECT_FAILED = "Android Create Project Failed"
    val GET_LOCATIONS_FAILED = "Android Get Locations Failed"
    val SKU_PROCESS_STATE_WITH_SHADOW_FAILED = "Android SkuProcessStateWithShadow failed"
    val GET_SUBCATEGORIES = "Android Got Subcategories"
    val GET_SUBCATRGORIES_FAILED = "Android Get Subcategories Failed"
    val GET_OVERLAYS_INTIATED = "Android Get Overlays Initiated"
    val GET_OVERLAYS = "Android Got Overlays"
    val GET_OVERLAYS_FAILED = "Android Get Overlays Failed"
    val CREATE_SKU = "Android Create SKU"
    val CREATE_SKU_FAILED = "Android Create SKU Failed"

    val VIDEO_SKU_UPDATED = "Video SKU Updated"
    val VIDEO_SKU_UPDATE_FAILED = "Video SKU update Failed"

    val IMAGE_CAPTURED = "Android Image Captured"
    val IMAGE_CAPRURE_FAILED = "Android Image Capture Failed"
    val CONFIRMED = "Android Confirmed"
    val RESHOOT = "Android Reshoot"
    val GET_BACKGROUND = "Android Got Background"
    val TOTAL_FRAMES_UPDATED = "Android Total Frames Updated"
    val TOTAL_FRAMES_UPDATE_FAILED = "Android Total Frames Update Failed"
    val GET_BACKGROUND_FAILED = "Android Get Background Failed"
    val PROCESS_INITIATED = "Android Process Initiated"
    val PROCESS = "Android Process Completed"
    val PROCESS_FAILED = "Android Process Failed"



    //360
    val SHOW_360_HINT = "Show 360 Hint"
    val CREATE_360_PROJECT = "Create 360 Project"
    val CREATE_360_PROJECT_FAILED = "Create Project 360 Failed"
    val CREATE_360_SKU = "Create 360 SKU"
    val CREATE_360_SKU_FAILED = "Create SKU 360 Failed"

    val PROJECT_STATE_UPDATE_FAILED = "Android Project State Update Failed"
    val PROJECT_STATE_UPDATED = "Android Project State Updated"

    val OVERLAY_LOAD_FIALED = "Overlay Load Failed"
    val OVERLAY_CAMERA_FIALED = "Camera Failed"
    val OVERLAY_LOADED = "Overlay Loaded"

    val BLOCKED_WORKER_START_EXCEPTION = "START BLOCKED WORKER"
    val CANCELLED_WORKER_START_EXCEPTION = "START CANCELLED WORKER"

    val SERVICE_STARTED = "SERVICE STARTED"
    val VIDEO_SERVICE_STARTED = "VIDEO SERVICE STARTED"

    val JSON_RESPONSE = "JSON_RESPONSE"

    val GET_PRESIGNED_VIDEO_URL_FAILED = "GET PRESIGNED VIDEO_URL_FAILED"
    val GOT_PRESIGNED_VIDEO_URL = "GOT PRESIGNED VIDEO URL"

    val VIDEO_UPLOADED_TO_GCP = "VIDEO UPLOADED TO GCP"
    val VIDEO_UPLOAD_TO_GCP_FAILED = "VIDEO UPLOAD TO GCP FAILED"

    val MARK_VIDEO_UPLOADED_FAILED = "MARK VIDEO UPLOADED FAILED"
    val MARKED_VIDEO_UPLOADED = "MARKED VIDEO UPLOADED"
    val IMAGE_UPLOADED_TO_GCP = "IMAGE UPLOADED TO GCP"
    val IMAGE_UPLOAD_TO_GCP_FAILED = "IMAGE UPLOAD TO GCP FAILED"
    val MARKED_IMAGE_UPLOADED = "MARKED IMAGE UPLOADED"
    val MARK_IMAGE_UPLOADED_FAILED = "MARK IMAGE UPLOADED FAILED"
    val GOT_PRESIGNED_IMAGE_URL = "GOT PRESIGNED IMAGE URL"

    val GET_PRESIGNED_FAILED = "GET_PRESIGNED_FAILED"

    val MAX_RETRY = "MAX_RETRY"

    val UPLOADING_TO_GCP_INITIATED = "UPLOADING_TO_GCP_INITIATED"
    val IS_PRESIGNED_URL_UPDATED = "IS_PRESIGNED_URL_UPDATED"
    val IS_MARK_GCP_UPLOADED_UPDATED = "IS_MARK_GCP_UPLOADED_UPDATED"
    val IS_MARK_DONE_STATUS_UPDATED = "IS_MARK_DONE_STATUS_UPDATED"

    val GOT_VERSION = "GOT_VERSION"

    val VIDEO_NOT_UPLOADED = "VIDEO_NOT_UPLOADED"
    val VIDEO_UPLOADING_TO_GCP_INITIATED = "VIDEO_UPLOADING_TO_GCP_INITIATED"
    val IS_VIDEO_PRESIGNED_URL_UPDATED = "IS_VIDEO_PRESIGNED_URL_UPDATED"
    val IS_VIDEO_MARK_DONE_STATUS_UPDATED = "IS_VIDEO_MARK_DONE_STATUS_UPDATED"
    val IS_MARK_VIDEO_GCP_UPLOADED_UPDATED = "IS_MARK_VIDEO_GCP_UPLOADED_UPDATED"
    val IMAGE_ROTATION_EXCEPTION = "IMAGE_ROTATION_EXCEPTION"
    val INTERNET_CONNECTED = "INTERNET_CONNECTED"
    val INTERNET_DISCONNECTED = "INTERNET_DISCONNECTED"


    val CHECKIN_CALL_INTIATED="CHECKIN_CALL_INTIATED"
    val CHECKOUT_CALL_INTIATED="CHECKOUT_CALL_INTIATED"
    val CHECKIN_SUCCESS="CHECKIN_SUCCESS"
    val CHECKIN_FAILURE="CHECKIN_FAILURE"
    val CHECKOUT_SUCCESS="CHECKOUT_SUCCESS"
    val CHECKOUT_FAILURE="CHECKOUT_FAILURE"

    val GET_PRESIGNED_CALL_INITIATED = "GET_PRESIGNED_CALL_INITIATED"

    val MARK_DONE_CALL_INITIATED = "MARK_DONE_CALL_INITIATED"

}