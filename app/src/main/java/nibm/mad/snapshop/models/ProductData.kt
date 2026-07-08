package nibm.mad.snapshop.models

import kotlinx.serialization.Serializable

@Serializable
data class ProductMatch(
    val title: String,
    val link: String,
    val source: String,
    val sourceIcon: String
)
