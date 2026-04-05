package edu.nd.jnkouka.hwapp.four.repositories


import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import edu.nd.jnkouka.hwapp.four.data.Post
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.DocumentSnapshot.ServerTimestampBehavior
import com.google.firebase.firestore.FieldValue
import edu.nd.jnkouka.hwapp.four.data.Comment
import edu.nd.jnkouka.hwapp.four.screens.SortType
import edu.nd.jnkouka.hwapp.four.screens.VoteType
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow


class FirebaseRedditRepository(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {

    private val postsCollection = firestore.collection("posts")

    suspend fun createPost(post: Post) {
        // Generate the ID manually so we can persist it immediately and correctly
        val userId = auth.currentUser?.uid ?: return
        val docRef = postsCollection.document() // this generates the id
        val voteRef = docRef.collection("votes").document(userId)
        val postWithId =
            post.copy(
                id = docRef.id, // this sets our new post's ID to the generated
                score = 1) // one with a score = 1
        docRef.set(postWithId).await()
        voteRef.set(mapOf("value" to 1)).await()
    }

    suspend fun voteOnPost(postId: String, voteType: VoteType) {
        val userId = auth.currentUser?.uid ?: return
        val postRef = postsCollection.document(postId)
        val voteRef = postRef.collection("votes").document(userId)
        firestore.runTransaction { transaction ->
            val postSnap = transaction.get(postRef)
            val voteSnap = transaction.get(voteRef)

            val oldScore = postSnap.getLong("score")?.toInt() ?: 0
            val oldVote = if (!voteSnap.exists()) 0 else voteSnap.getLong("value")?.toInt() ?: 0

            val newVote = when (voteType) {
                VoteType.UPVOTE -> {
                    when (oldVote) {
                        1 -> 0
                        -1 -> 1
                        else -> 1
                    }
                }

                VoteType.DOWNVOTE -> {
                    when (oldVote) {
                        -1 -> 0
                        1 -> -1
                        else -> -1
                    }
                }
            }
            val delta = newVote - oldVote
            val newScore = oldScore + delta

            transaction.update(postRef, "score", newScore)

            when (newVote) {
                0 -> transaction.delete(voteRef)
                else -> transaction.set(voteRef, mapOf("value" to newVote))
            }
        }.await()
    }

    suspend fun getUserPostVote(postId: String): Int {
        val userId = auth.currentUser?.uid ?: return 0
        val voteRef = postsCollection.document(postId).collection("votes").document(userId)
        val voteSnap = voteRef.get().await()
        return if (voteSnap.exists()) {
            voteSnap.getLong("value")?.toInt() ?: 0
        } else {
            0
        }
    }


    fun getPosts(sortBy : SortType): Flow<List<Post>> = callbackFlow {
        val query = when (sortBy) {
            SortType.TIMESTAMP -> postsCollection.orderBy("timestamp", Query.Direction.DESCENDING)
            else -> postsCollection.orderBy("score", Query.Direction.DESCENDING)
        }
        val subscription = query.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val post = snapshot.toObjects(
                        Post::class.java,
                        ServerTimestampBehavior.ESTIMATE
                    )
                    trySend(post)
                }
            }
        awaitClose { // stay open and keep listening for events until the flow is closed
            subscription.remove() // when the flow is closed, remove the listener
        }
    }

    fun getPost(postId: String): Flow<Post?> = callbackFlow {
        val subscription = postsCollection.document(postId).
        addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val post = snapshot.toObject(Post::class.java)
                trySend(post)
            } else {
                trySend(null)
            }
        }
        awaitClose{
            subscription.remove()
        }
    }



    suspend fun createComment(comment: Comment, postId: String) {
        val userId = auth.currentUser?.uid ?: return

        val commentRef = postsCollection.document(postId)
            .collection("comments")
            .document()

        val voteRef = commentRef.collection("votes").document(userId)

        val commentWithId = comment.copy(
            id = commentRef.id,
            score = 1
        )

        firestore.runBatch {
            batch ->
            batch.set(commentRef, commentWithId)
            batch.set(voteRef, mapOf("value" to 1))
            batch.update(
                // Increment the post's comment count
                postsCollection.document(postId),
                "commentCount",
                FieldValue.increment(1)
            )
        }.await()
    }

    suspend fun voteOnComment(postId: String, commentId: String, voteType: VoteType) {
        val userId = auth.currentUser?.uid ?: return
        val postRef = postsCollection.document(postId).collection("comments").document(commentId)
        val voteRef = postRef.collection("votes").document(userId)
        firestore.runTransaction { transaction ->
            val postSnap = transaction.get(postRef)
            val voteSnap = transaction.get(voteRef)

            val oldScore = postSnap.getLong("score")?.toInt() ?: 0
            val oldVote = if (!voteSnap.exists()) 0 else voteSnap.getLong("value")?.toInt() ?: 0

            val newVote = when (voteType) {
                VoteType.UPVOTE -> {
                    when (oldVote) {
                        1 -> 0
                        -1 -> 1
                        else -> 1
                    }
                }

                VoteType.DOWNVOTE -> {
                    when (oldVote) {
                        -1 -> 0
                        1 -> -1
                        else -> -1
                    }
                }
            }
            val delta = newVote - oldVote
            val newScore = oldScore + delta

            transaction.update(postRef, "score", newScore)

            when (newVote) {
                0 -> transaction.delete(voteRef)
                else -> transaction.set(voteRef, mapOf("value" to newVote))
            }
        }.await()
    }


    suspend fun getUserCommentVote(postId: String, commentId: String): Int {
        val userId = auth.currentUser?.uid ?: return 0
        val voteRef = postsCollection.document(postId).collection("comments")
            .document(commentId)
            .collection("votes")
            .document(userId)
        val voteSnap = voteRef.get().await()
        return if (voteSnap.exists()) {
            voteSnap.getLong("value")?.toInt() ?: 0
        } else {
            0
        }
    }

    fun getComments(postId: String, sortBy : SortType): Flow<List<Comment>> = callbackFlow {
        val query = when (sortBy) {
            SortType.TIMESTAMP -> postsCollection.document(postId)
                    .collection("comments")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
            else -> postsCollection
                .document(postId)
                .collection("comments")
                .orderBy("score", Query.Direction.DESCENDING)
        }
        val subscription = query.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val comments = snapshot.toObjects(
                        Comment::class.java,
                        ServerTimestampBehavior.ESTIMATE
                    )
                    trySend(comments)
                }
            }
        awaitClose {
            subscription.remove()
        }
    }
}

