package com.spyneai.videorecording.listener

interface SeekListener {
    enum class Type {
        START,
        END
    }

    fun onSeekStarted()
    fun onSeekEnd(start: Long, end: Long)
    fun onSeek(type: Type, start: Long, end: Long)

}
