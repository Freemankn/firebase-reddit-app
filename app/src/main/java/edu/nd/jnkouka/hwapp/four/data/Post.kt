package edu.nd.jnkouka.hwapp.four.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

data class Post(
    @DocumentId // Used by Firestore as the unique id of the document.
    val id: String = "",

    // The UID of the FirebaseUser who created the Post
    val authorId: String = "",

    // The email or display name (optional, but helpful for UI)
    val authorName: String = "",

    val score: Int = 0,

    val body: String = "",

    val title: String = "",

    val commentCount: Int = 0,

    // The @ServerTimestamp annotation tells Firestore to generate the time of object creation when
    // the object is uploaded on the server side. This is so all users have consistent timing.
    @ServerTimestamp
    val timestamp: Timestamp? = null
)
