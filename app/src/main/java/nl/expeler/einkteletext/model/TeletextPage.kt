package nl.expeler.einkteletext.model

import com.google.gson.annotations.SerializedName

data class TeletextPage(
    @SerializedName("prevPage") val prevPage: String = "",
    @SerializedName("nextPage") val nextPage: String = "",
    @SerializedName("prevSubPage") val prevSubPage: String = "",
    @SerializedName("nextSubPage") val nextSubPage: String = "",
    @SerializedName("fastTextLinks") val fastTextLinks: List<FastTextLink> = emptyList(),
    @SerializedName("content") val content: String = ""
)

data class FastTextLink(
    @SerializedName("title") val title: String,
    @SerializedName("page") val page: String
)
