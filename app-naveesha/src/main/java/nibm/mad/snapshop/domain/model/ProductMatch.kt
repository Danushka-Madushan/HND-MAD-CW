package nibm.mad.snapshop.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ProductMatch(
    val title: String,
    val link: String,
    val source: String,
    val sourceIcon: String
)
