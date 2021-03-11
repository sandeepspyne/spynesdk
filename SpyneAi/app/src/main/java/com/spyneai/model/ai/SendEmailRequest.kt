package com.spyneai.model.ai

data class SendEmailRequest (
        val beforeList : ArrayList<String>,
        val afterList : ArrayList<String>,
        val imageGif : String,
        val emailId : String
)