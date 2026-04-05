package edu.nd.jnkouka.hwapp.four.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import edu.nd.jnkouka.hwapp.four.data.Post
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListScreen(
    listViewModel: ListViewModel,
    onPostClick: (String) -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiPosts by listViewModel.uiPosts.collectAsState()
    val currentSort = listViewModel.currentSort
    val context = LocalContext.current
    val errorMessage = listViewModel.errorMessage

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            listViewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reddit Posts") },
                actions = {
                    IconButton(onClick = { onSignOut() }) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Sign Out")
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = listViewModel.newPostTitle,
                    onValueChange = { listViewModel.onNewPostTitleChange(title = it) },
                    label = { Text("Enter your title") },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = listViewModel.newPostBody,
                    onValueChange = { listViewModel.onNewPostBodyChange(it) },
                    label = { Text("Enter your body text") },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            IconButton(onClick = { listViewModel.addPost() }) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Add Post")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SortColumn(currentSort, listViewModel::onSort)
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(uiPosts) { uiPost ->
                    PostItem(
                        post = uiPost.post,
                        userVote = uiPost.userVote,
                        onVote = listViewModel::onVote,
                        onPostClick = onPostClick
                    )
                }
            }
        }
    }
}

@Composable
fun PostItem(
    post: Post,
    userVote: Int,
    onVote: (String, VoteType) -> Unit,
    onPostClick: (String) -> Unit
) {
    val formattedDate = remember(post.timestamp) {
        post.timestamp?.toDate()?.let {
            SimpleDateFormat("MMM dd, yyyy - HH:mm", Locale.getDefault()).format(it)
        } ?: "Pending..."
    }

    Card(
        onClick = {onPostClick(post.id)},
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = post.title,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.secondary
            )

            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                VoteColumn(
                    score = post.score,
                    userVote = userVote,
                    onUpvote = { onVote(post.id, VoteType.UPVOTE) },
                    onDownvote = { onVote(post.id, VoteType.DOWNVOTE) }
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Posted by ${post.authorName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${post.commentCount} comments",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun VoteColumn(
    score: Int,
    userVote: Int,
    onUpvote: () -> Unit,
    onDownvote: () -> Unit
) {
    val upHighlighted = userVote == 1
    val downHighlighted = userVote == -1


    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconButton(onClick = onUpvote) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                tint = if (upHighlighted) Color.Blue else LocalContentColor.current,
                contentDescription = "Upvote"
            )
        }

        Text(
            text = score.toString(),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        IconButton(onClick = onDownvote) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                tint = if (downHighlighted) Color.Red else LocalContentColor.current,
                contentDescription = "Downvote"
            )
        }
    }
}

@Composable
fun SortColumn(
    currentSort: SortType,
    onSort: (SortType) -> Unit
) {
    Row {
        Text(text = "Sort by:")
        Spacer(modifier = Modifier.width(8.dp))

        Button(
            onClick = { onSort(SortType.SCORE) },
            enabled = currentSort != SortType.SCORE
        ) {
            Text("Score")
        }

        Spacer(modifier = Modifier.width(8.dp))

        Button(
            onClick = { onSort(SortType.TIMESTAMP) },
            enabled = currentSort != SortType.TIMESTAMP
        ) {
            Text("Date")
        }
    }
}