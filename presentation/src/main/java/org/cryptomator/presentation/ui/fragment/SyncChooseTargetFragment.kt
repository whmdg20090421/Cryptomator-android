package org.cryptomator.presentation.ui.fragment

import androidx.recyclerview.widget.LinearLayoutManager
import org.cryptomator.generator.Fragment
import org.cryptomator.presentation.R
import org.cryptomator.presentation.databinding.FragmentAutoUploadChooseVaultBinding
import org.cryptomator.presentation.model.CloudFolderModel
import org.cryptomator.presentation.model.VaultModel
import org.cryptomator.presentation.presenter.SyncChooseTargetPresenter
import org.cryptomator.presentation.ui.adapter.SharedLocationsAdapter
import javax.inject.Inject

@Fragment
class SyncChooseTargetFragment : BaseFragment<FragmentAutoUploadChooseVaultBinding>(FragmentAutoUploadChooseVaultBinding::inflate) {

	@Inject
	lateinit var presenter: SyncChooseTargetPresenter

	@Inject
	lateinit var locationsAdapter: SharedLocationsAdapter

	override fun setupView() {
		binding.chooseVaultLayout.saveFiles.setText(R.string.screen_sync_choose_target_confirm)
		binding.chooseVaultLayout.saveFiles.setOnClickListener { presenter.onConfirmPressed() }
		setupRecyclerView()
	}

	override fun loadContent() {
		presenter.displayVaults()
	}

	private val locationsAdapterCallback = object : SharedLocationsAdapter.Callback {
		override fun onVaultSelected(vault: VaultModel?) {
			presenter.onVaultSelected(vault)
		}

		override fun onChooseLocationPressed() {
			presenter.onChooseLocationPressed()
		}
	}

	fun displayVaults(vaults: List<VaultModel>) {
		locationsAdapter.clear()
		if (vaults.isNotEmpty()) {
			locationsAdapter.setPreselectedVault(vaults[0])
			presenter.onVaultSelected(vaults[0])
		}
		locationsAdapter.addAll(vaults)
	}

	private fun setupRecyclerView() {
		locationsAdapter.setCallback(locationsAdapterCallback)
		binding.locationsRecyclerView.setHasFixedSize(true)
		binding.locationsRecyclerView.layoutManager = LinearLayoutManager(context())
		binding.locationsRecyclerView.adapter = locationsAdapter
	}

	fun showChosenLocation(folder: CloudFolderModel) {
		locationsAdapter.setSelectedLocation(if (folder.path.isEmpty()) "/" else folder.path)
	}
}

