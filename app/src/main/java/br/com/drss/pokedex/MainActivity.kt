package br.com.drss.pokedex

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import br.com.drss.pokedex.databinding.ActivityMainBinding
import br.com.drss.pokedex.features.details.ui.DETAIL_NAME_ARGUMENT
import br.com.drss.pokedex.features.details.ui.DetailFragment
import br.com.drss.pokedex.features.home.ui.PokemonListFragment

class MainActivity: FragmentActivity(), NavigationManager {

    lateinit var binding: ActivityMainBinding
    private val navigator = Navigator(supportFragmentManager)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun navigateTo(navigationActions: NavigationActions) {
        navigator.navigateTo(navigationActions)
    }
}


interface NavigationManager {

    fun navigateTo(navigationActions: NavigationActions)
}

class Navigator(private val supportFragmentManager: FragmentManager) {

    fun navigateTo(navigationActions: NavigationActions) {
        when(navigationActions) {
            is NavigationActions.DisplayPokeDetails -> {
                val detailFragment = DetailFragment()
                detailFragment.arguments = bundleOf(DETAIL_NAME_ARGUMENT to navigationActions.name)
                supportFragmentManager
                    .beginTransaction()
                    .setCustomAnimations(R.anim.open_slide_enter, R.anim.open_slide_exit, R.anim.close_slide_enter, R.anim.close_slide_exit)
                    .replace(R.id.fragContainer, detailFragment)
                    .addToBackStack(detailFragment::class.simpleName)
                    .commit()
            }
            NavigationActions.PopBack -> {
                supportFragmentManager
                    .popBackStack()
            }
        }
    }
}

sealed class NavigationActions {
    data class DisplayPokeDetails(val name: String): NavigationActions()
    object PopBack: NavigationActions()
}