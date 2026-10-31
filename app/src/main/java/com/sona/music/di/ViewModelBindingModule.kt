package com.sona.music.di

import androidx.lifecycle.ViewModel
import com.sona.core.common.di.ViewModelKey
import com.sona.feature.detailmusic.impl.presentation.DetailMusicViewModel
import com.sona.feature.favorites.impl.presentation.FavoritesViewModel
import com.sona.feature.home.impl.presentation.HomeViewModel
import com.sona.feature.musiclist.impl.presentation.MusicListViewModel
import com.sona.feature.navigation.impl.presentation.AppViewModel
import com.sona.feature.player.impl.presentation.PlayerViewModel
import com.sona.feature.profile.impl.presentation.ProfileViewModel
import com.sona.feature.searchmusic.impl.presentation.SearchViewModel
import com.sona.feature.settings.impl.presentation.SettingsViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class ViewModelBindingModule {

    @Binds
    @IntoMap
    @ViewModelKey(AppViewModel::class)
    abstract fun bindAppViewModel(viewModel: AppViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(HomeViewModel::class)
    abstract fun bindHomeViewModel(viewModel: HomeViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SearchViewModel::class)
    abstract fun bindSearchViewModel(viewModel: SearchViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(FavoritesViewModel::class)
    abstract fun bindFavoritesViewModel(viewModel: FavoritesViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ProfileViewModel::class)
    abstract fun bindProfileViewModel(viewModel: ProfileViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SettingsViewModel::class)
    abstract fun bindSettingsViewModel(viewModel: SettingsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(DetailMusicViewModel::class)
    abstract fun bindDetailMusicViewModel(viewModel: DetailMusicViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MusicListViewModel::class)
    abstract fun bindMusicListViewModel(viewModel: MusicListViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(PlayerViewModel::class)
    abstract fun bindPlayerViewModel(viewModel: PlayerViewModel): ViewModel
}
