package br.com.drss.pokedex.features.home.vm

import br.com.drss.pokedex.data.pokemonList
import br.com.drss.pokedex.features.home.repository.*
import br.com.drss.pokedex.features.home.repository.domain.entities.PokemonSummary
import br.com.drss.pokedex.features.home.repository.domain.entities.PokemonType
import br.com.drss.pokedex.features.home.ui.PokemonListViewEvents
import br.com.drss.pokedex.features.home.ui.PokemonListViewModel
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertTrue
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.TestCoroutineDispatcher
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.lang.Exception

@ExperimentalCoroutinesApi
@RunWith(JUnit4::class)
class PokemonListVMTest {

    private val dispatcher = TestCoroutineDispatcher()

    class FakePokeRepo : PokemonRepository {

        override fun getPokemonSummaryList(typeFilters: List<PokemonType>): Flow<OperationStatus<List<PokemonSummary>>> {
            return flow {
                val mutableList = mutableListOf<PokemonSummary>()
                pokemonList.forEach {
                    delay(10)
                    mutableList.add(it.toSummary())
                    emit(Loading(mutableList.toList()))
                }
                emit(Loaded(mutableList.toList()))
            }
        }
    }

    @Test
    fun `Given the ViewModel is initialized When the data finishes loading Then I must have a list of pokemons`() =
        runBlocking {

            val vm = PokemonListViewModel(FakePokeRepo(), dispatcher)

            val firstState = vm.viewState.first()
            assertEquals((firstState).isFetchingItems, true)

            vm.viewState.collect {
                if (!it.isFetchingItems) {
                    assertEquals(pokemonList.size, it.pokemonSummaryList.size)
                }
            }
        }

    @Test
    fun `When there is an error Then I must receive an Error event`(): Unit = runBlocking {
        val repo = object : PokemonRepository {
            override fun getPokemonSummaryList(typeFilters: List<PokemonType>): Flow<OperationStatus<List<PokemonSummary>>> =
                flow {
                    throw Exception("Any random error that might occur")
                }

        }

        dispatcher.pauseDispatcher()
        val viewModel = PokemonListViewModel(repo, dispatcher)

        val deferred = async(Dispatchers.IO) {
            val event = viewModel.viewEvent.first()
            assert(event is PokemonListViewEvents.Error)
        }

        dispatcher.resumeDispatcher()
        deferred.await()
    }

    @Test
    fun `Given I have a pokemon list available When I select one of the Items Then I must display the details`(): Unit = runBlocking {

        dispatcher.pauseDispatcher()
        val viewModel = PokemonListViewModel(FakePokeRepo(), dispatcher)

        val deferred = async(Dispatchers.IO) {
            val state = viewModel.viewState.first {
                it.pokemonSummaryList.isNotEmpty()
            }

            launch(Dispatchers.IO) {
                val event = viewModel.viewEvent.first()
                assert(event is PokemonListViewEvents.Error)
            }
            viewModel.onItemSelected(state.pokemonSummaryList.first())
        }
        dispatcher.resumeDispatcher()
        deferred.await()
    }

    @Test
    fun `Given the item position is not the first Then the scroll must be enabled `() = runBlocking {
        val viewModel = PokemonListViewModel(FakePokeRepo(), dispatcher)

        viewModel.setFirstVisibleItemPosition(10)
        val viewState = viewModel.viewState.single()

        assert(viewState.scrollToTopVisible)

    }
}