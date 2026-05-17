package com.weathersnap

import com.weathersnap.data.remote.api.GeocodingApi
import com.weathersnap.data.remote.dto.GeocodingResponse
import com.weathersnap.data.remote.dto.GeocodingResult
import com.weathersnap.data.repository.CityRepository
import com.weathersnap.domain.model.Result
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class CityRepositoryTest {

    private val api = mockk<GeocodingApi>()
    private lateinit var repository: CityRepository

    private val fakeResult = GeocodingResult(1, "London", "GB", 51.5, -0.1, "England")
    private val fakeResponse = GeocodingResponse(listOf(fakeResult))

    @Before
    fun setup() {
        repository = CityRepository(api)
    }

    @Test
    fun `searchCities returns success with mapped cities`() = runTest {
        coEvery { api.searchCities(any(), any(), any(), any()) } returns fakeResponse
        val result = repository.searchCities("London")
        assertTrue(result is Result.Success)
        val cities = (result as Result.Success).data
        assertEquals(1, cities.size)
        assertEquals("London", cities[0].name)
    }

    @Test
    fun `searchCities uses cache on second call`() = runTest {
        coEvery { api.searchCities(any(), any(), any(), any()) } returns fakeResponse
        repository.searchCities("London")
        repository.searchCities("London")
        // API should only be called once due to caching
        coVerify(exactly = 1) { api.searchCities(any(), any(), any(), any()) }
    }

    @Test
    fun `searchCities returns error on exception`() = runTest {
        coEvery { api.searchCities(any(), any(), any(), any()) } throws RuntimeException("Network error")
        val result = repository.searchCities("London")
        assertTrue(result is Result.Error)
    }
}
