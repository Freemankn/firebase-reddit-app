package edu.nd.jnkouka.hwapp.four.screens



import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.nd.jnkouka.hwapp.four.data.Comment
import edu.nd.jnkouka.hwapp.four.data.CommentUiModel
import edu.nd.jnkouka.hwapp.four.data.Post
import edu.nd.jnkouka.hwapp.four.repositories.FirebaseAuthRepository
import edu.nd.jnkouka.hwapp.four.repositories.FirebaseRedditRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch



class PostViewModel(
    private val redditRepository: FirebaseRedditRepository,
    private val authRepository: FirebaseAuthRepository
) : ViewModel() {

    private val sortMode = MutableStateFlow(SortType.SCORE)

    var currentSort by mutableStateOf(SortType.SCORE)
        private set

    var currentPost by mutableStateOf<Post?>(null)
        private set

    var currentPostUserVote by mutableStateOf(0)
        private set


    private val currentPostId = MutableStateFlow<String?>(null)


    @OptIn(ExperimentalCoroutinesApi::class)
    val comments = combine(currentPostId, sortMode){
            postId, sortType -> postId to sortType
    }
        .flatMapLatest { (postId, sortType) ->
            if (postId == null) {
                flowOf(emptyList())
            }
            else {
                redditRepository.getComments(postId, sortType)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val uiComments = comments
        .map { commentList ->
            val postId = currentPostId.value
            if (postId == null) {
                emptyList()
            } else {
                coroutineScope {
                    commentList.map { comment ->
                        async {
                            CommentUiModel(
                                comment = comment,
                                userVote = redditRepository.getUserCommentVote(postId,
                                    comment.id
                                )
                            )
                        }
                    }.awaitAll()
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )


    var newCommentText by mutableStateOf("")

    var errorMessage by mutableStateOf<String?>(null)
        private set


    fun loadPost(postId: String){
        currentPostId.value = postId

        viewModelScope.launch {
            redditRepository.getPost(postId).collect { post ->
                currentPost = post
            }
        }

        viewModelScope.launch {
            currentPostUserVote = redditRepository.getUserPostVote(postId)
        }
    }


    fun onSort(sortType: SortType) {
        currentSort = sortType
        sortMode.value = sortType
    }


    fun onNewCommentText(text: String) {
        newCommentText = text
    }

    fun addComment() {
        val postId = currentPostId.value ?: return


        if (!authRepository.isUserSignedIn()) return
        val currentUser = authRepository.currentUser!!
        if (newCommentText.isBlank()) return


        val comment = Comment(
            authorId = currentUser.uid,
            authorName = currentUser.email ?: "Anonymous",
            text = newCommentText
        )

        viewModelScope.launch {
            try {
                redditRepository.createComment(comment, postId)
                newCommentText = ""
            } catch (e: Exception) {
                errorMessage = "Failed to add comment: ${e.localizedMessage}"
            }
        }
    }



    fun onVotePost(voteType: VoteType) {
        val postId = currentPostId.value ?: return

        viewModelScope.launch {
            try {
                redditRepository.voteOnPost(postId, voteType)
                currentPostUserVote = redditRepository.getUserPostVote(postId)
            } catch (e: Exception) {
                errorMessage = "Failed to vote on post: ${e.localizedMessage}"
            }
        }
    }

    fun onVoteComment(commentId: String, voteType: VoteType) {
        val postId = currentPostId.value ?: return

        viewModelScope.launch {
            try {
                redditRepository.voteOnComment(postId, commentId, voteType)
            } catch (e: Exception) {
                errorMessage = "Failed to vote on comment: ${e.localizedMessage}"
            }
        }
    }



    fun clearError() {
        errorMessage = null
    }


}