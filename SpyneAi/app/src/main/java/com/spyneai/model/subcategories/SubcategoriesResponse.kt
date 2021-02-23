import com.spyneai.model.login.Header
import com.spyneai.model.login.MsgInfo

data class SubcategoriesResponse (

	val header : Header,
	val msgInfo : MsgInfo,
	val payload : Payload
)