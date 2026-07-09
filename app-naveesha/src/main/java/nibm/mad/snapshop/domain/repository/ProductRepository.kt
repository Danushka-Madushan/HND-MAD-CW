package nibm.mad.snapshop.domain.repository

import android.content.Context
import android.net.Uri
import nibm.mad.snapshop.domain.model.ProductMatch

interface ProductRepository {
    suspend fun uploadImage(context: Context, imageUri: Uri): String?
    suspend fun searchImage(imageUrl: String): List<ProductMatch>
    suspend fun distillQuery(top5Titles: List<String>): String?
}
