package io.golos.cyber_android.ui.screens.subscriptions

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import io.golos.cyber_android.R
import io.golos.cyber_android.application.App
import io.golos.cyber_android.databinding.FragmentSubscriptionsBinding
import io.golos.cyber_android.ui.screens.subscriptions.di.SubscriptionsFragmentComponent
import io.golos.cyber_android.ui.shared.mvvm.FragmentBaseMVVM
import io.golos.cyber_android.ui.shared.mvvm.view_commands.NavigateBackwardCommand
import io.golos.cyber_android.ui.shared.mvvm.view_commands.NavigateToSearchCommunitiesCommand
import io.golos.cyber_android.ui.shared.mvvm.view_commands.ViewCommand
import io.golos.cyber_android.ui.shared.paginator.Paginator
import io.golos.cyber_android.ui.shared.utils.debounce
import kotlinx.android.synthetic.main.fragment_subscriptions.*
import kotlinx.android.synthetic.main.item_toolbar.*
import kotlinx.android.synthetic.main.view_search_bar.*
import kotlinx.android.synthetic.main.view_search_bar_pure.*
import timber.log.Timber

class SubscriptionsFragment : FragmentBaseMVVM<FragmentSubscriptionsBinding, SubscriptionsViewModel>() {

    private val subscriptionsAdapter: CommunitiesAdapter = CommunitiesAdapter()

    override fun provideViewModelType(): Class<SubscriptionsViewModel> = SubscriptionsViewModel::class.java

    override fun layoutResId(): Int = R.layout.fragment_subscriptions

    override fun inject(key: String) = App.injections.get<SubscriptionsFragmentComponent>(key).inject(this)

    override fun releaseInjection(key: String) = App.injections.release<SubscriptionsFragmentComponent>(key)

    override fun linkViewModel(binding: FragmentSubscriptionsBinding, viewModel: SubscriptionsViewModel) {
        binding.viewModel = viewModel
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()
        setupToolbar()
        viewModel.start()
    }

    override fun processViewCommand(command: ViewCommand) {
        super.processViewCommand(command)
        when (command) {
            is NavigateToSearchCommunitiesCommand -> {

            }
            is NavigateBackwardCommand -> {
                fragmentManager?.popBackStack()
            }
        }
    }

    private fun setupToolbar() {
        toolbarTitle.setText(R.string.subscriptions)
        ivBack.setOnClickListener {
            viewModel.back()
        }
    }

    private fun setupCommunitiesRecommendedList() {
        rvCommunitiesRecommended.layoutManager = LinearLayoutManager(requireContext())
        rvCommunitiesRecommended.adapter = subscriptionsAdapter
        subscriptionsAdapter.nextPageCallback = {
            viewModel.loadMoreRecommendedCommunities()
        }
        subscriptionsAdapter.onPageRetryLoadingCallback = {
            viewModel.loadMoreRecommendedCommunities()
        }
        subscriptionsAdapter.onJoinClickedCallback = {
            viewModel.changeCommunitySubscriptionStatus(it)
        }
    }

    private fun setupSubscriptionsList() {
        rvSubscriptions.layoutManager = LinearLayoutManager(requireContext())
        rvSubscriptions.adapter = subscriptionsAdapter
        subscriptionsAdapter.nextPageCallback = {
            viewModel.loadSubscriptions()
        }
        subscriptionsAdapter.onPageRetryLoadingCallback = {
            viewModel.loadSubscriptions()
        }
        subscriptionsAdapter.onJoinClickedCallback = {
            viewModel.changeCommunitySubscriptionStatus(it)
        }
    }

    private fun setupSearch() {


        searchBar.addTextChangedListener(object : TextWatcher {

            private val querySearchListener: (String) -> Unit = debounce({
                viewModel.onCommunitySearchQueryChanged(it)
            })

            override fun afterTextChanged(s: Editable?) {
                //implement if need
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                //implement if need
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                querySearchListener(s.toString())
            }

        })
    }

    private fun observeViewModel() {
        viewModel.subscriptionsStateLiveData.observe(viewLifecycleOwner, Observer {
            when (it) {
                SubscriptionsViewModel.SubscriptionsState.UNDEFINED -> setUndefinedSubscriptionsState()
                SubscriptionsViewModel.SubscriptionsState.EMPTY -> setEmptySubscriptionsState()
                SubscriptionsViewModel.SubscriptionsState.EXIST -> setExistSubscriptionsState()
                else -> {
                    Timber.e("undefined subscription state type")
                }
            }
        })
        viewModel.subscriptionsListStateLiveData.observe(viewLifecycleOwner, Observer {
            updateListState(it)
        })
        viewModel.recommendedSubscriptionsListStateLiveData.observe(viewLifecycleOwner, Observer {
            updateListState(it)
        })

        viewModel.generalLoadingProgressVisibilityLiveData.observe(viewLifecycleOwner, Observer {
            if (it) {
                emptyPostProgressLoading.visibility = View.VISIBLE
            } else {
                emptyPostProgressLoading.visibility = View.INVISIBLE
            }
        })

        viewModel.recommendedSubscriptionStatusLiveData.observe(viewLifecycleOwner, Observer {
            subscriptionsAdapter.updateSubscriptionStatus(it)
        })
        viewModel.subscriptionsStatusLiveData.observe(viewLifecycleOwner, Observer {
            subscriptionsAdapter.updateSubscriptionStatus(it)
        })
        viewModel.generalErrorVisibilityLiveData.observe(viewLifecycleOwner, Observer {
            if (it) {
                btnRetry.visibility = View.VISIBLE
            } else {
                btnRetry.visibility = View.INVISIBLE
            }
        })
        viewModel.searchProgressVisibilityLiveData.observe(viewLifecycleOwner, Observer {
            if (it) {
                pbLoading.visibility = View.VISIBLE
            } else {
                pbLoading.visibility = View.INVISIBLE
            }
        })
    }

    private fun updateListState(state: Paginator.State) {
        when (state) {
            is Paginator.State.Data<*> -> {
                subscriptionsAdapter.updateCommunities(state.data as MutableList<Community>)
                subscriptionsAdapter.isFullData = false
                subscriptionsAdapter.isPageError = false
                subscriptionsAdapter.isNewPageProgress = false
            }
            is Paginator.State.FullData<*> -> {
                subscriptionsAdapter.updateCommunities(state.data as MutableList<Community>)
                subscriptionsAdapter.isFullData = true
                subscriptionsAdapter.isPageError = false
                subscriptionsAdapter.isNewPageProgress = false
            }
            is Paginator.State.PageError<*> -> {
                subscriptionsAdapter.updateCommunities(state.data as MutableList<Community>)
                subscriptionsAdapter.isPageError = true
            }
            is Paginator.State.NewPageProgress<*> -> {
                subscriptionsAdapter.updateCommunities(state.data as MutableList<Community>)
                subscriptionsAdapter.isPageError = false
            }
            is Paginator.State.SearchProgress<*> -> {
                subscriptionsAdapter.updateCommunities(state.data as MutableList<Community>)
                subscriptionsAdapter.isNewPageProgress = true
            }
            is Paginator.State.SearchPageError<*> -> {
                subscriptionsAdapter.updateCommunities(state.data as MutableList<Community>)
                subscriptionsAdapter.isNewPageProgress = false
                uiHelper.showMessage(R.string.loading_error)
            }
        }
    }

    private fun setUndefinedSubscriptionsState() {
        layoutSearchBar.visibility = View.INVISIBLE
        rvSubscriptions.visibility = View.INVISIBLE
        clNoSubscriptionsPlaceHolder.visibility = View.INVISIBLE
        tvRecommendedTitle.visibility = View.INVISIBLE
        rvCommunitiesRecommended.visibility = View.INVISIBLE
    }

    private fun setEmptySubscriptionsState() {
        layoutSearchBar.visibility = View.INVISIBLE
        rvSubscriptions.visibility = View.INVISIBLE
        clNoSubscriptionsPlaceHolder.visibility = View.VISIBLE
        tvRecommendedTitle.visibility = View.VISIBLE
        rvCommunitiesRecommended.visibility = View.VISIBLE
        setupCommunitiesRecommendedList()
    }

    private fun setExistSubscriptionsState() {
        layoutSearchBar.visibility = View.VISIBLE
        rvSubscriptions.visibility = View.VISIBLE
        clNoSubscriptionsPlaceHolder.visibility = View.INVISIBLE
        tvRecommendedTitle.visibility = View.INVISIBLE
        rvCommunitiesRecommended.visibility = View.INVISIBLE
        setupSearch()
        setupSubscriptionsList()
    }

    companion object {

        fun newInstance(): Fragment {
            return SubscriptionsFragment()
        }
    }
}
