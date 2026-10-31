package com.sona.core.common.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

/**
 * Dagger backed factory. Every ViewModel is bound into a map by the app component,
 * so screens never resolve dependencies themselves.
 */
@Singleton
class SonaViewModelFactory @Inject constructor(
    private val creators: Map<Class<out ViewModel>, @JvmSuppressWildcards Provider<ViewModel>>,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val creator = creators[modelClass]
            ?: creators.entries.firstOrNull { (key, _) -> modelClass.isAssignableFrom(key) }?.value
            ?: error("No ViewModel binding found for ${modelClass.name}")
        return creator.get() as T
    }
}
