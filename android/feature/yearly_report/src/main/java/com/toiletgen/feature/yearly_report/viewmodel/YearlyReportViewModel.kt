package com.toiletgen.feature.yearly_report.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.toiletgen.core.common.Resource
import com.toiletgen.core.domain.model.YearlyReport
import com.toiletgen.core.domain.usecase.GetYearlyReportUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Year

data class YearlyReportUiState(val report: YearlyReport? = null, val isLoading: Boolean = false, val error: String? = null, val currentPage: Int = 0)

class YearlyReportViewModel(private val getYearlyReportUseCase: GetYearlyReportUseCase) : ViewModel() {
    private val _uiState = MutableStateFlow(YearlyReportUiState())
    val uiState: StateFlow<YearlyReportUiState> = _uiState.asStateFlow()
    init { loadReport(Year.now().value) }
    private fun loadReport(year: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = getYearlyReportUseCase(year)) {
                is Resource.Success -> _uiState.update { it.copy(isLoading = false, report = result.data) }
                is Resource.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
                is Resource.Loading -> {}
            }
        }
    }
    fun nextPage() { _uiState.update { it.copy(currentPage = it.currentPage + 1) } }
    fun prevPage() { _uiState.update { it.copy(currentPage = maxOf(0, it.currentPage - 1)) } }
}
