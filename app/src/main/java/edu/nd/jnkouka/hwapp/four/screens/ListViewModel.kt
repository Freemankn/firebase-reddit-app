package edu.nd.jnkouka.hwapp.four.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.nd.jnkouka.hwapp.four.data.Post
import edu.nd.jnkouka.hwapp.four.data.PostUiModel
import edu.nd.jnkouka.hwapp.four.repositories.FirebaseAuthRepository
import edu.nd.jnkouka.hwapp.four.repositories.FirebaseRedditRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch



class ListViewModel(
    private val redditRepository: FirebaseRedditRepository,
    private val authRepository: FirebaseAuthRepository
) : ViewModel() {

    private val sortMode = MutableStateFlow(SortType.SCORE)

    var currentSort by mutableStateOf(SortType.SCORE)
        private set



    @OptIn(ExperimentalCoroutinesApi::class)
    val posts = sortMode
        .flatMapLatest { sortType ->
            redditRepository.getPosts(sortType)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val uiPosts = posts
        .map { postList ->
            coroutineScope {
                postList.map { post ->
                    async {
                        PostUiModel(
                            post = post,
                            userVote = redditRepository.getUserPostVote(post.id)
                        )
                    }
                }.awaitAll()
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )


    var newPostTitle by mutableStateOf("")

    var newPostBody by mutableStateOf("")

    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        // On initialization, start collecting from Firestore
        viewModelScope.launch {
            redditRepository.getPosts(SortType.SCORE).collect {}
        }
    }


    fun onSort(sortType: SortType) {
        currentSort = sortType
        sortMode.value = sortType
    }

    fun onNewPostTitleChange(title: String) {
        newPostTitle = title
    }

    fun onNewPostBodyChange(text: String) {
        newPostBody = text
    }

    fun addPost() {
        if (!authRepository.isUserSignedIn()) return
        val currentUser = authRepository.currentUser!!
        if (newPostBody.isBlank() || newPostTitle.isBlank()) return


        val post = Post(
            authorId = currentUser.uid,
            authorName = currentUser.email ?: "Anonymous",
            title = newPostTitle,
            body = newPostBody
        )

        viewModelScope.launch {
            try {
                redditRepository.createPost(post)
                newPostBody = ""
                newPostTitle = ""
            } catch (e: Exception) {
                errorMessage = "Failed to add post: ${e.localizedMessage}"
            }
        }
    }

    fun onVote(postId: String, voteType: VoteType) {
        viewModelScope.launch {
            try {
                redditRepository.voteOnPost(postId, voteType)
            } catch (e: Exception) {
                errorMessage = "Failed to vote: ${e.localizedMessage}"
            }
        }
    }

    fun clearError() {
        errorMessage = null
    }



}