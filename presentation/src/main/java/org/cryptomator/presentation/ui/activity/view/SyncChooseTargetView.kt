package org.cryptomator.presentation.ui.activity.view

import org.cryptomator.presentation.model.CloudFolderModel
import org.cryptomator.presentation.model.VaultModel

interface SyncChooseTargetView : View {

	fun displayDialogUnableToChooseTarget()
	fun displayVaults(vaults: List<VaultModel>)
	fun showChosenLocation(location: CloudFolderModel)
}

