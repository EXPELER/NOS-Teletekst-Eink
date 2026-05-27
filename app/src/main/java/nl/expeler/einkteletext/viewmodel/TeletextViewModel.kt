package nl.expeler.einkteletext.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import nl.expeler.einkteletext.api.TeletextRepository
import nl.expeler.einkteletext.model.TeletextPage

sealed class TeletextUiState {
    data class Loading(val isInitial: Boolean = false) : TeletextUiState()
    data class Success(val page: TeletextPage, val pageNumber: String) : TeletextUiState()
    data class Error(val message: String) : TeletextUiState()
}

class TeletextViewModel : ViewModel() {

    private val repository = TeletextRepository()

    private val _uiState = MutableStateFlow<TeletextUiState>(TeletextUiState.Loading())
    val uiState: StateFlow<TeletextUiState> = _uiState.asStateFlow()

    private val _currentPage = MutableStateFlow("101")
    val currentPage: StateFlow<String> = _currentPage.asStateFlow()

    private val history = ArrayDeque<String>()
    val canGoBack: Boolean get() = history.isNotEmpty()

    private var isFirstLoad = true

    private val _visited = MutableStateFlow<Set<String>>(emptySet())
    val visited: StateFlow<Set<String>> = _visited.asStateFlow()

    init {
        loadPage("101")
    }

    fun loadPage(page: String) {
        if (page.isBlank()) return
        val previousPage = _currentPage.value
        _currentPage.value = page
        _uiState.value = TeletextUiState.Loading(isInitial = isFirstLoad)
        isFirstLoad = false
        viewModelScope.launch {
            repository.getPage(page).fold(
                onSuccess = {
                    if (previousPage.isNotBlank()) {
                        history.addLast(previousPage)
                        _visited.value = _visited.value + previousPage
                    }
                    _uiState.value = TeletextUiState.Success(it, page)
                },
                onFailure = {
                    _currentPage.value = previousPage
                    _uiState.value = TeletextUiState.Error(it.message ?: "Fout bij laden")
                }
            )
        }
    }

    fun goBack() {
        if (history.isEmpty()) return
        _visited.value = _visited.value + _currentPage.value
        val previous = history.removeLast()
        _currentPage.value = previous
        _uiState.value = TeletextUiState.Loading()
        viewModelScope.launch {
            repository.getPage(previous).fold(
                onSuccess = { _uiState.value = TeletextUiState.Success(it, previous) },
                onFailure = { _uiState.value = TeletextUiState.Error(it.message ?: "Fout bij laden") }
            )
        }
    }

    fun navigatePrevPage() {
        val state = _uiState.value as? TeletextUiState.Success ?: return
        state.page.prevPage.takeIf { it.isNotBlank() }?.let { loadPage(it) }
    }

    fun navigateNextPage() {
        val state = _uiState.value as? TeletextUiState.Success ?: return
        state.page.nextPage.takeIf { it.isNotBlank() }?.let { loadPage(it) }
    }

    fun navigatePrevSubPage() {
        val state = _uiState.value as? TeletextUiState.Success ?: return
        state.page.prevSubPage.takeIf { it.isNotBlank() }?.let { loadPage(it) }
    }

    fun navigateNextSubPage() {
        val state = _uiState.value as? TeletextUiState.Success ?: return
        state.page.nextSubPage.takeIf { it.isNotBlank() }?.let { loadPage(it) }
    }
}
