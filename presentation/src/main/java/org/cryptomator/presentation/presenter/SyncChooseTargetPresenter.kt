package org.cryptomator.presentation.presenter

import org.cryptomator.domain.Cloud
import org.cryptomator.domain.CloudFolder
import org.cryptomator.domain.Vault
import org.cryptomator.domain.di.PerView
import org.cryptomator.domain.usecases.GetDecryptedCloudForVaultUseCase
import org.cryptomator.domain.usecases.cloud.GetRootFolderUseCase
import org.cryptomator.domain.usecases.vault.GetVaultListUseCase
import org.cryptomator.generator.Callback
import org.cryptomator.presentation.R
import org.cryptomator.presentation.exception.ExceptionHandlers
import org.cryptomator.presentation.intent.ChooseCloudNodeSettings
import org.cryptomator.presentation.intent.Intents
import org.cryptomator.presentation.intent.UnlockVaultIntent
import org.cryptomator.presentation.model.CloudFolderModel
import org.cryptomator.presentation.model.CloudModel
import org.cryptomator.presentation.model.VaultModel
import org.cryptomator.presentation.model.mappers.CloudFolderModelMapper
import org.cryptomator.presentation.ui.activity.view.SyncChooseTargetView
import org.cryptomator.presentation.workflow.ActivityResult
import org.cryptomator.presentation.workflow.AuthenticationExceptionHandler
import javax.inject.Inject

@PerView
class SyncChooseTargetPresenter @Inject constructor(
	private val getVaultListUseCase: GetVaultListUseCase,
	private val getRootFolderUseCase: GetRootFolderUseCase,
	private val getDecryptedCloudForVaultUseCase: GetDecryptedCloudForVaultUseCase,
	private val cloudFolderModelMapper: CloudFolderModelMapper,
	private val authenticationExceptionHandler: AuthenticationExceptionHandler,
	exceptionMappings: ExceptionHandlers
) : Presenter<SyncChooseTargetView>(exceptionMappings) {

	private var selectedVault: VaultModel? = null
	private var location: CloudFolderModel? = null
	private var authenticationState: AuthenticationState? = null

	fun displayVaults() {
		getVaultListUseCase.run(object : DefaultResultHandler<List<Vault>>() {
			override fun onSuccess(vaults: List<Vault>) {
				if (vaults.isEmpty()) {
					view?.displayDialogUnableToChooseTarget()
				} else {
					val vaultModels = vaults.mapTo(ArrayList()) { VaultModel(it) }
					view?.displayVaults(vaultModels)
				}
			}
		})
	}

	fun onVaultSelected(vault: VaultModel?) {
		selectedVault = vault
	}

	fun onChooseLocationPressed() {
		authenticate(selectedVault)
	}

	fun onConfirmPressed() {
		val chosen = location
		if (chosen == null) {
			showMessage(R.string.screen_sync_choose_target_missing_target)
			return
		}
		finishWithResult(chosen)
	}

	private fun authenticate(vaultModel: VaultModel?, authenticationState: AuthenticationState = AuthenticationState.CHOOSE_LOCATION) {
		this.authenticationState = authenticationState
		vaultModel?.let { onCloudOfVaultAuthenticated(it.toVault()) }
	}

	private fun onCloudOfVaultAuthenticated(authenticatedVault: Vault) {
		if (authenticatedVault.isUnlocked) {
			decryptedCloudFor(authenticatedVault)
		} else {
			if (!isPaused) {
				requestActivityResult(
					ActivityResultCallbacks.vaultUnlockedForSyncChooseTarget(),
					Intents.unlockVaultIntent().withVaultModel(VaultModel(authenticatedVault)).withVaultAction(UnlockVaultIntent.VaultAction.UNLOCK)
				)
			}
		}
	}

	@Callback
	fun vaultUnlockedForSyncChooseTarget(result: ActivityResult) {
		val cloud = result.intent().getSerializableExtra(SINGLE_RESULT) as Cloud
		rootFolderFor(cloud)
	}

	private fun decryptedCloudFor(vault: Vault) {
		getDecryptedCloudForVaultUseCase
			.withVault(vault)
			.run(object : DefaultResultHandler<Cloud>() {
				override fun onSuccess(cloud: Cloud) {
					rootFolderFor(cloud)
				}

				override fun onError(e: Throwable) {
					if (!authenticationExceptionHandler.handleAuthenticationException(
							this@SyncChooseTargetPresenter,
							e,
							ActivityResultCallbacks.decryptedCloudForAfterAuthInSyncChooseTarget(vault)
						)
					) {
						super.onError(e)
					}
				}
			})
	}

	@Callback
	fun decryptedCloudForAfterAuthInSyncChooseTarget(result: ActivityResult, vault: Vault?) {
		val cloud = result.getSingleResult(CloudModel::class.java).toCloud()
		decryptedCloudFor(Vault.aCopyOf(vault).withCloud(cloud).build())
	}

	private fun rootFolderFor(cloud: Cloud) {
		getRootFolderUseCase
			.withCloud(cloud)
			.run(object : DefaultResultHandler<CloudFolder>() {
				override fun onSuccess(folder: CloudFolder) {
					when (authenticationState) {
						AuthenticationState.CHOOSE_LOCATION -> {
							location = cloudFolderModelMapper.toModel(folder)
							selectedVault?.let { navigateToVaultContent(it, location) }
						}
						AuthenticationState.INIT_ROOT -> location = cloudFolderModelMapper.toModel(folder)
						else -> {}
					}
				}
			})
	}

	private fun navigateToVaultContent(vaultModel: VaultModel, decryptedRoot: CloudFolderModel?) {
		requestActivityResult(
			ActivityResultCallbacks.onSyncChooseTargetLocation(vaultModel),
			Intents.browseFilesIntent()
				.withFolder(decryptedRoot)
				.withTitle(vaultModel.name)
				.withChooseCloudNodeSettings(
					ChooseCloudNodeSettings.chooseCloudNodeSettings()
						.withExtraTitle(context().getString(R.string.screen_file_browser_share_destination_title))
						.withExtraToolbarIcon(R.drawable.ic_clear)
						.withButtonText(context().getString(R.string.screen_file_browser_share_button_text))
						.selectingFolders()
						.build()
				)
		)
	}

	@Callback
	fun onSyncChooseTargetLocation(result: ActivityResult, vaultModel: VaultModel?) {
		location = result.singleResult as CloudFolderModel
		location?.let { view?.showChosenLocation(it) }
	}

	enum class AuthenticationState {
		CHOOSE_LOCATION,
		INIT_ROOT
	}

	init {
		unsubscribeOnDestroy(getVaultListUseCase, getRootFolderUseCase, getDecryptedCloudForVaultUseCase)
	}
}

