package com.spyneai.posthog

object Events {
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
    val CREATE_PROJECT = "Android Create Project"
    val CREATE_PROJECT_FAILED = "Android Create Project Failed"
    val GET_SUBCATEGORIES = "Android Got Subcategories"
    val GET_SUBCATRGORIES_FAILED = "Android Get Subcategories Failed"
    val GET_OVERLAYS_INTIATED = "Android Get Overlays Initiated"
    val GET_OVERLAYS = "Android Got Overlays"
    val GET_OVERLAYS_FAILED = "Android Get Overlays Failed"
    val CREATE_SKU = "Android Create SKU"
    val CREATE_SKU_FAILED = "Android Create SKU Failed"

    val IMAGE_CAPTURED = "Android Image Captured"
    val IMAGE_CAPRURE_FAILED = "Android Image Capture Failed"
    val CONFIRMED = "Android Confirmed"
    val RESHOOT = "Android Reshoot"
    val UPLOADED = "Android Uploaded"
    val SKIPED_UPLOADED = "Android Skiped Uploaded"
    val SKIPPED_UPLOAD_FAILED = "Android Skipped Upload Failed"
    val UPLOAD_FAILED = "Android Upload Failed"
    val GET_BACKGROUND = "Android Got Background"
    val TOTAL_FRAMES_UPDATED = "Android Total Frames Updated"
    val TOTAL_FRAMES_UPDATE_FAILED = "Android Total Frames Update Failed"
    val GET_BACKGROUND_FAILED = "Android Get Background Failed"
    val PROCESS_INITIATED = "Android Process Initiated"
    val PROCESS_INITIATED_BY_WORKER = "Android Process Initiated By Worker"
    val PROCESS = "Android Process Completed"
    val PROCESS_FAILED = "Android Process Failed"
    val SHOOT_QUEUED = "Android Shoot Queued"


    //360
    val SHOW_360_HINT = "Show 360 Hint"
    val CREATE_360_PROJECT = "Create 360 Project"
    val CREATE_360_PROJECT_FAILED = "Create Project 360 Failed"
    val CREATE_360_SKU = "Create 360 SKU"
    val CREATE_360_SKU_FAILED = "Create SKU 360 Failed"
    val GET_360_SUBCATEGORIES = "Got 360 Subcategories"
    val GET_360_SUBCATRGORIES_FAILED = "Get Subcategories 360 Failed"


    val PROJECT_STATE_UPDATE_FAILED = "Android Project State Update Failed"
    val PROJECT_STATE_UPDATED = "Android Project State Updated"



}