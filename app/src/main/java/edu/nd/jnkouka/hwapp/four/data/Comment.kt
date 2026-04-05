package edu.nd.jnkouka.hwapp.four.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

data class Comment(
    @DocumentId
    val id: String = "",

    // The UID of the FirebaseUser who created the Comment
    val authorId: String = "",

    val authorName: String = "",

    val text: String = "",

    val score: Int = 0,

    // The @ServerTimestamp annotation tells Firestore to generate the time of object creation when
    // the object is uploaded on the server side. This is so all users have consistent timing.
    @ServerTimestamp
    val timestamp: Timestamp? = null
)