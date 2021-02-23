
import com.spyneai.model.login.Header
import com.spyneai.model.login.MsgInfo
import com.spyneai.model.uploadRough.Payload

data class UploadPhotoResponse (
        val header : Header,
        val msgInfo : MsgInfo,
        val payload : Payload
        )