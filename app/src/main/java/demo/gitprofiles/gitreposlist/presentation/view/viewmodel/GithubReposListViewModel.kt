package demo.gitprofiles.gitreposlist.presentation.view.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import demo.gitprofiles.gitreposlist.data.network.response.GithubReposListDTO
import demo.gitprofiles.gitreposlist.domain.repository.GitProfileRepository
import demo.gitprofiles.gitreposlist.presentation.UIState
import demo.gitprofiles.gitreposlist.presentation.view.GitHubReposListUIState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import javax.inject.Inject


class GithubReposListViewModel @Inject constructor(
    private val gitProfileRepository: GitProfileRepository
) : ViewModel() {
    private val _reposApiCallUIState = MutableStateFlow(GitHubReposListUIState())
    val reposApiCallUIState: StateFlow<GitHubReposListUIState> = _reposApiCallUIState.asStateFlow()

    init {
        fetchGithubRepos()
    }

    private fun fetchGithubRepos() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                gitProfileRepository.getGitProfiles()
            }.collectLatest { result: UIState<GithubReposListDTO> ->
                when (result) {
                    is UIState.ErrorState -> {
                        _reposApiCallUIState.update {
                            it.copy(isLoading = false)
                        }
                    }

                    is UIState.SuccessState -> {
                        result.data?.let { gRepoListDTO ->
                            _reposApiCallUIState.update {
                                it.copy(isLoading = false, githubApiCallList = gRepoListDTO)
                            }
                        }
                    }

                    is UIState.LoadingState -> {
                        _reposApiCallUIState.update { loadingState ->
                            loadingState.copy(isLoading = true)
                        }
                    }
                }
            }
        }
    }

}