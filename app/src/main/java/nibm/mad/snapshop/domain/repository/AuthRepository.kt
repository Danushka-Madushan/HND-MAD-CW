package nibm.mad.snapshop.domain.repository

import android.content.Context
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val currentUser: StateFlow<FirebaseUser?>
    suspend fun signInWithGoogle(context: Context): Result<Unit>
    suspend fun signOut()
}
