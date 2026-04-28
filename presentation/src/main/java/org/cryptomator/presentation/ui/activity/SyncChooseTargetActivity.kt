package org.cryptomator.presentation.ui.activity

import androidx.fragment.app.Fragment
import org.cryptomator.generator.Activity
import org.cryptomator.presentation.R
import org.cryptomator.presentation.databinding.ActivityLayoutBinding
import org.cryptomator.presentation.model.CloudFolderModel
import org.cryptomator.presentation.model.VaultModel
import org.cryptomator.presentation.presenter.SyncChooseTargetPresenter
import org.cryptomator.presentation.ui.activity.view.SyncChooseTargetView
import org.cryptomator.presentation.ui.dialog.NotEnoughVaultsDialog
import org.cryptomator.presentation.ui.fragment.SyncChooseTargetFragment
import javax.inject.Inject

@Activity
class SyncChooseTargetActivity : BaseActivity<ActivityLayoutBinding>(ActivityLayoutBinding::inflate), //
	SyncChooseTargetView, //
	NotEnoughVaultsDialog.Callback {

	@Inject
	lateinit var presenter: SyncChooseTargetPresenter

	override fun setupView() {
		setupToolbar()
	}

	private fun setupToolbar() {
		binding.mtToolbar.toolbar.setTitle(R.string.screen_sync_choose_target_title)
		setSupportActionBar(binding.mtToolbar.toolbar)
		supportActionBar?.let {
			it.setDisplayHomeAsUpEnabled(true)
			it.setHomeAsUpIndicator(R.drawable.ic_clear)
		}
	}

	override fun onMenuItemSelected(itemId: Int): Boolean = when (itemId) {
		android.R.id.home -> {
			finish()
			true
		}
		else -> super.onMenuItemSelected(itemId)
	}

	override fun createFragment(): Fragment = SyncChooseTargetFragment()

	override fun displayDialogUnableToChooseTarget() {
		NotEnoughVaultsDialog
			.withContext(context())
			.andTitle(R.string.dialog_unable_to_sync_title)
			.show()
	}

	override fun displayVaults(vaults: List<VaultModel>) {
		syncChooseTargetFragment().displayVaults(vaults)
	}

	override fun showChosenLocation(location: CloudFolderModel) {
		syncChooseTargetFragment().showChosenLocation(location)
	}

	override fun onNotEnoughVaultsOkClicked() {
		finish()
	}

	override fun onNotEnoughVaultsCreateVaultClicked() {
		val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
		launchIntent?.let { startActivity(it) }
		finish()
	}

	private fun syncChooseTargetFragment(): SyncChooseTargetFragment =
		getCurrentFragment(R.id.fragment_container) as SyncChooseTargetFragment
}

