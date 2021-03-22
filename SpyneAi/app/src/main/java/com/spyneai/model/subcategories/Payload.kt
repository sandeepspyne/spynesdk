import com.google.gson.annotations.SerializedName
import com.spyneai.model.subcategories.Data

data class Payload (
	@SerializedName("data")
	val data : List<Data>
)