package com.weathersnap

import app.cash.turbine.test
import com.weathersnap.data.repository.CityRepository
import com.weathersnap.data.repository.WeatherRepository
import com.weathersnap.domain.model.City
import com.weathersnap.domain.model.Result
import com.weathersnap.domain.model.WeatherSnapshot
import com.weathersnap.ui.weather.WeatherUiState
import com.weathersnap.ui.weather.WeatherViewModel
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WeatherViewModelTest {

    private val cityRepository = mockk<CityRepository>()
    private val weatherRepository = mockk<WeatherRepository>()
    private lateinit var viewModel: WeatherViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    private val testCity = City(1, "London", "GB", 51.5, -0.1)
    private val testSnapshot = WeatherSnapshot("London", 51.5, -0.1, 20.0, "Clear Sky", 60, 15.0, 1013.0, 0)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = WeatherViewModel(cityRepository, weatherRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Idle`() = runTest {
        viewModel.weatherState.test {
            assertEquals(WeatherUiState.Idle, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `selecting city with success shows weather`() = runTest {
        coEvery { weatherRepository.getWeather(any(), any(), any()) } returns Result.Success(testSnapshot)

        viewModel.weatherState.test {
            assertEquals(WeatherUiState.Idle, awaitItem())
            viewModel.onCitySelected(testCity)
            assertEquals(WeatherUiState.Loading, awaitItem())
            val success = awaitItem()
            assertTrue(success is WeatherUiState.Success)
            assertEquals(testSnapshot, (success as WeatherUiState.Success).snapshot)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `selecting city with error shows error state`() = runTest {
        coEvery { weatherRepository.getWeather(any(), any(), any()) } returns Result.Error("Network error")

        viewModel.weatherState.test {
            awaitItem() // Idle
            viewModel.onCitySelected(testCity)
            awaitItem() // Loading
            val error = awaitItem()
            assertTrue(error is WeatherUiState.Error)
            assertEquals("Network error", (error as WeatherUiState.Error).message)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `query shorter than 3 chars does not expand suggestions`() = runTest {
        viewModel.onQueryChange("Lo")
        assertFalse(viewModel.suggestionState.value.isExpanded)
    }

    @Test
    fun `getCurrentSnapshot returns null when idle`() {
        assertNull(viewModel.getCurrentSnapshot())
    }
}
