package com.spyneai.posthog

object Events {
    //on Boarding
    val SLIDE_CHANGE = "On Boarding Slide Changed"

    //Signup
    val SIGNUP_INTIATED = "Signup Initiated"
    val SIGNUP_SUCCEED = "Signup Succeed"
    val SIGNUP_FAILED = " Signup Failed"

    //Login with password
    val LOGIN_INTIATED = "Login Initiated"
    val LOGIN_SUCCEED = "Login Succeed"
    val LOGIN_FAILED = " Login Failed"


    //login with otp
    val OTP_LOGIN_INTIATED = "OTP Login Initiated"
    val OTP_LOGIN_SUCCEED = "OTP Login Succeed"
    val OTP_LOGIN_FAILED = " OTP Login Failed"

    // otp verification & resend otp
    val OTP_VERIFICATION_INITIATED = "OTP Verification Initiated"
    val OTP_VERIFIED = "OTP Verified"
    val OTP_VERIFICATION_FAILED = "OTP Verification Failed"
    val OTP_RESEND_INITIATED = "OTP Resend Initiated"
    val OTP_RESENT = "OTP Resent"
    val OTP_RESENT_FAILED = "OTP Resent Failed"

    //forgot password
    val FORGOT_PASSWORD_INTIATED = "Forgot Password Initiated"
    val FORGOT_PASSWORD_MAIL_SENT = "Forgot Password Mail Sent"
    val FORGOT_PASSWORD_FAILED = "Forgot Password Failed"

    //home
    val GOT_CATEGORIES = "Got Categories"
    val GET_CATEGORIES_FAILED = "Get Categories Failed"

    val GOT_ONGOING_ORDERS = "Got Ongoing Orders"
    val GET_ONGOING_ORDERS_FAILED = "Get Ongoing Orders Failed"

    val GET_COMPLETED_ORDERS = "Got Completed Orders"
    val GET_COMPLETED_ORDERS_FAILED = "Get Completed Orders Failed"

    //wallet
    val FETCH_CREDITS = "Credits Fetched"
    val FETCH_CREDITS_FAILED = "Credits Fetch Failed"



}