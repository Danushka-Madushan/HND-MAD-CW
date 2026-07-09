package nibm.mad.snapshop.data.repository

import android.content.Context
import android.net.Uri
import nibm.mad.snapshop.data.remote.GeminiQueryDistiller
import nibm.mad.snapshop.data.remote.ImgBBUploader
import nibm.mad.snapshop.data.remote.SerpApiSearcher
import nibm.mad.snapshop.domain.model.ProductMatch
import nibm.mad.snapshop.domain.repository.ProductRepository

class ProductRepositoryImpl(
    private val imgBBUploader: ImgBBUploader = ImgBBUploader(),
    private val serpApiSearcher: SerpApiSearcher = SerpApiSearcher(),
    private val geminiQueryDistiller: GeminiQueryDistiller = GeminiQueryDistiller()
) : ProductRepository {

    override suspend fun uploadImage(context: Context, imageUri: Uri): String? {
        return imgBBUploader.uploadImage(context, imageUri)
    }

    override suspend fun searchImage(imageUrl: String): List<ProductMatch> {
        return serpApiSearcher.searchImage(imageUrl)
    }

    override suspend fun distillQuery(top5Titles: List<String>): String? {
        return geminiQueryDistiller.distillQuery(top5Titles)
    }
}
