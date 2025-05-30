/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.components.menu.compose

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import mozilla.components.compose.base.Divider
import mozilla.components.service.fxa.manager.AccountState
import mozilla.components.service.fxa.manager.AccountState.Authenticated
import mozilla.components.service.fxa.manager.AccountState.Authenticating
import mozilla.components.service.fxa.manager.AccountState.AuthenticationProblem
import mozilla.components.service.fxa.manager.AccountState.NotAuthenticated
import mozilla.components.service.fxa.store.Account
import org.mozilla.fenix.R
import org.mozilla.fenix.components.menu.MenuAccessPoint
import org.mozilla.fenix.components.menu.MenuDialogTestTag
import org.mozilla.fenix.components.menu.compose.header.MenuNavHeader
import org.mozilla.fenix.theme.FirefoxTheme
import org.mozilla.fenix.theme.Theme

/**
 * Wrapper column containing the main menu items.
 *
 * @param accessPoint The [MenuAccessPoint] that was used to navigate to the menu dialog.
 * @param account [Account] information available for a synced account.
 * @param accountState The [AccountState] of a Mozilla account.
 * @param showQuitMenu Whether or not the button to delete browsing data and quit
 * should be visible.
 * @param isPrivate Whether or not the browsing mode is in private mode.
 * @param isDesktopMode Whether or not the desktop mode is enabled.
 * @param isPdf Whether or not the current tab is a PDF.
 * @param isTranslationSupported Whether or not translation is supported.
 * @param isWebCompatReporterSupported Whether or not the report broken site feature is supported.
 * @param isExtensionsProcessDisabled Whether or not the extensions process is disabled due to extension errors.
 * @param extensionsMenuItemDescription The label of extensions menu item description.
 * @param scrollState The [ScrollState] used for vertical scrolling.
 * @param onMozillaAccountButtonClick Invoked when the user clicks on Mozilla account button.
 * @param onSettingsButtonClick Invoked when the user clicks on the settings button.
 * @param onNewTabMenuClick Invoked when the user clicks on the new tab menu item.
 * @param onNewPrivateTabMenuClick Invoked when the user clicks on the new private tab menu item.
 * @param onSwitchToDesktopSiteMenuClick Invoked when the user clicks on the switch to desktop site
 * menu toggle.
 * @param onFindInPageMenuClick Invoked when the user clicks on the find in page menu item.
 * @param onToolsMenuClick Invoked when the user clicks on the tools menu item.
 * @param onSaveMenuClick Invoked when the user clicks on the save menu item.
 * @param onExtensionsMenuClick Invoked when the user clicks on the extensions menu item.
 * @param onBookmarksMenuClick Invoked when the user clicks on the bookmarks menu item.
 * @param onHistoryMenuClick Invoked when the user clicks on the history menu item.
 * @param onDownloadsMenuClick Invoked when the user clicks on the downloads menu item.
 * @param onPasswordsMenuClick Invoked when the user clicks on the passwords menu item.
 * @param onCustomizeHomepageMenuClick Invoked when the user clicks on the customize
 * homepage menu item.
 * @param onNewInFirefoxMenuClick Invoked when the user clicks on the release note menu item.
 * @param onQuitMenuClick Invoked when the user clicks on the quit menu item.
 * @param onBackButtonClick Invoked when the user clicks on the back button.
 * @param onForwardButtonClick Invoked when the user clicks on the forward button.
 * @param onRefreshButtonClick Invoked when the user clicks on the refresh button.
 * @param onShareButtonClick Invoked when the user clicks on the share button.
 */
@Suppress("LongParameterList")
@Composable
fun MainMenu(
    accessPoint: MenuAccessPoint,
    account: Account?,
    accountState: AccountState,
    showQuitMenu: Boolean,
    isPrivate: Boolean,
    isDesktopMode: Boolean,
    isPdf: Boolean,
    isTranslationSupported: Boolean,
    isWebCompatReporterSupported: Boolean,
    isExtensionsProcessDisabled: Boolean,
    extensionsMenuItemDescription: String,
    scrollState: ScrollState,
    onMozillaAccountButtonClick: () -> Unit,
    onSettingsButtonClick: () -> Unit,
    onNewTabMenuClick: () -> Unit,
    onNewPrivateTabMenuClick: () -> Unit,
    onSwitchToDesktopSiteMenuClick: () -> Unit,
    onFindInPageMenuClick: () -> Unit,
    onToolsMenuClick: () -> Unit,
    onSaveMenuClick: () -> Unit,
    onExtensionsMenuClick: () -> Unit,
    onBookmarksMenuClick: () -> Unit,
    onHistoryMenuClick: () -> Unit,
    onDownloadsMenuClick: () -> Unit,
    onPasswordsMenuClick: () -> Unit,
    onCustomizeHomepageMenuClick: () -> Unit,
    onNewInFirefoxMenuClick: () -> Unit,
    onQuitMenuClick: () -> Unit,
    onBackButtonClick: (longPress: Boolean) -> Unit,
    onForwardButtonClick: (longPress: Boolean) -> Unit,
    onRefreshButtonClick: (longPress: Boolean) -> Unit,
    onShareButtonClick: () -> Unit,
) {
    MenuScaffold(
        header = {
            MenuNavHeader(
                onBackButtonClick = onBackButtonClick,
                onForwardButtonClick = onForwardButtonClick,
                onRefreshButtonClick = onRefreshButtonClick,
                onShareButtonClick = onShareButtonClick,
            )
        },
        scrollState = scrollState,
    ) {
        NewTabsMenuGroup(
            accessPoint = accessPoint,
            isPrivate = isPrivate,
            onNewTabMenuClick = onNewTabMenuClick,
            onNewPrivateTabMenuClick = onNewPrivateTabMenuClick,
        )

        ToolsAndActionsMenuGroup(
            accessPoint = accessPoint,
            isDesktopMode = isDesktopMode,
            isPdf = isPdf,
            isTranslationSupported = isTranslationSupported,
            isWebCompatReporterSupported = isWebCompatReporterSupported,
            isExtensionsProcessDisabled = isExtensionsProcessDisabled,
            extensionsMenuItemDescription = extensionsMenuItemDescription,
            onSwitchToDesktopSiteMenuClick = onSwitchToDesktopSiteMenuClick,
            onFindInPageMenuClick = onFindInPageMenuClick,
            onToolsMenuClick = onToolsMenuClick,
            onSaveMenuClick = onSaveMenuClick,
            onExtensionsMenuClick = onExtensionsMenuClick,
        )

        LibraryMenuGroup(
            onBookmarksMenuClick = onBookmarksMenuClick,
            onHistoryMenuClick = onHistoryMenuClick,
            onDownloadsMenuClick = onDownloadsMenuClick,
            onPasswordsMenuClick = onPasswordsMenuClick,
        )

        if (accessPoint == MenuAccessPoint.Home) {
            HomepageMenuGroup(
                onCustomizeHomepageMenuClick = onCustomizeHomepageMenuClick,
                onNewInFirefoxMenuClick = onNewInFirefoxMenuClick,
            )
        }

        MenuGroup {
            MozillaAccountMenuItem(
                account = account,
                accountState = accountState,
                onClick = onMozillaAccountButtonClick,
            )

            Divider(color = FirefoxTheme.colors.borderSecondary)

            MenuItem(
                label = stringResource(id = R.string.browser_menu_settings),
                beforeIconPainter = painterResource(id = R.drawable.mozac_ic_settings_24),
                onClick = onSettingsButtonClick,
            )
        }

        if (showQuitMenu) {
            QuitMenuGroup(
                onQuitMenuClick = onQuitMenuClick,
            )
        }
    }
}

@Composable
private fun QuitMenuGroup(
    onQuitMenuClick: () -> Unit,
) {
    MenuGroup {
        MenuItem(
            label = stringResource(
                id = R.string.browser_menu_delete_browsing_data_on_quit,
                stringResource(id = R.string.app_name),
            ),
            beforeIconPainter = painterResource(id = R.drawable.mozac_ic_cross_circle_fill_24),
            state = MenuItemState.WARNING,
            onClick = onQuitMenuClick,
        )
    }
}

@Composable
private fun NewTabsMenuGroup(
    accessPoint: MenuAccessPoint,
    isPrivate: Boolean,
    onNewTabMenuClick: () -> Unit,
    onNewPrivateTabMenuClick: () -> Unit,
) {
    val isNewTabMenuEnabled: Boolean
    val isNewPrivateTabMenuEnabled: Boolean

    when (accessPoint) {
        MenuAccessPoint.Browser,
        MenuAccessPoint.External,
        -> {
            isNewTabMenuEnabled = true
            isNewPrivateTabMenuEnabled = true
        }

        MenuAccessPoint.Home -> {
            isNewTabMenuEnabled = isPrivate
            isNewPrivateTabMenuEnabled = !isPrivate
        }
    }

    MenuGroup {
        MenuItem(
            label = stringResource(id = R.string.library_new_tab),
            beforeIconPainter = painterResource(id = R.drawable.mozac_ic_plus_24),
            state = if (isNewTabMenuEnabled) MenuItemState.ENABLED else MenuItemState.DISABLED,
            onClick = onNewTabMenuClick,
        )

        Divider(color = FirefoxTheme.colors.borderSecondary)

        MenuItem(
            label = stringResource(id = R.string.browser_menu_new_private_tab),
            beforeIconPainter = painterResource(id = R.drawable.mozac_ic_private_mode_circle_fill_24),
            state = if (isNewPrivateTabMenuEnabled) MenuItemState.ENABLED else MenuItemState.DISABLED,
            onClick = onNewPrivateTabMenuClick,
        )
    }
}

@Suppress("LongParameterList", "LongMethod")
@Composable
private fun ToolsAndActionsMenuGroup(
    accessPoint: MenuAccessPoint,
    isDesktopMode: Boolean,
    isPdf: Boolean,
    isTranslationSupported: Boolean,
    isWebCompatReporterSupported: Boolean,
    isExtensionsProcessDisabled: Boolean,
    extensionsMenuItemDescription: String,
    onSwitchToDesktopSiteMenuClick: () -> Unit,
    onFindInPageMenuClick: () -> Unit,
    onToolsMenuClick: () -> Unit,
    onSaveMenuClick: () -> Unit,
    onExtensionsMenuClick: () -> Unit,
) {
    MenuGroup {
        if (accessPoint == MenuAccessPoint.Browser) {
            val labelId: Int
            val iconId: Int
            val menuItemState: MenuItemState

            if (isDesktopMode) {
                labelId = R.string.browser_menu_switch_to_mobile_site
                iconId = R.drawable.mozac_ic_device_mobile_24
                menuItemState = MenuItemState.ACTIVE
            } else {
                labelId = R.string.browser_menu_switch_to_desktop_site
                iconId = R.drawable.mozac_ic_device_desktop_24
                menuItemState = if (isPdf) MenuItemState.DISABLED else MenuItemState.ENABLED
            }

            MenuItem(
                label = stringResource(id = labelId),
                beforeIconPainter = painterResource(id = iconId),
                state = menuItemState,
                onClick = onSwitchToDesktopSiteMenuClick,
            )

            Divider(color = FirefoxTheme.colors.borderSecondary)

            MenuItem(
                label = stringResource(id = R.string.browser_menu_find_in_page_2),
                beforeIconPainter = painterResource(id = R.drawable.mozac_ic_search_24),
                onClick = onFindInPageMenuClick,
            )

            Divider(color = FirefoxTheme.colors.borderSecondary)

            MenuItem(
                label = stringResource(id = R.string.browser_menu_tools),
                beforeIconPainter = painterResource(id = R.drawable.mozac_ic_tool_24),
                description = when {
                    isTranslationSupported && isWebCompatReporterSupported -> stringResource(
                        R.string.browser_menu_tools_description_with_translate_with_report_site_2,
                    )
                    isTranslationSupported -> stringResource(
                        R.string.browser_menu_tools_description_with_translate_without_report_site,
                    )
                    isWebCompatReporterSupported -> stringResource(
                        R.string.browser_menu_tools_description_with_report_site_2,
                    )
                    else -> stringResource(
                        R.string.browser_menu_tools_description_without_report_site,
                    )
                },
                onClick = onToolsMenuClick,
                modifier = Modifier.testTag(MenuDialogTestTag.TOOLS),
                afterIconPainter = painterResource(id = R.drawable.mozac_ic_chevron_right_24),
            )

            Divider(color = FirefoxTheme.colors.borderSecondary)

            MenuItem(
                label = stringResource(id = R.string.browser_menu_save),
                beforeIconPainter = painterResource(id = R.drawable.mozac_ic_save_24),
                description = stringResource(id = R.string.browser_menu_save_description),
                onClick = onSaveMenuClick,
                modifier = Modifier.testTag(MenuDialogTestTag.SAVE),
                afterIconPainter = painterResource(id = R.drawable.mozac_ic_chevron_right_24),
            )

            Divider(color = FirefoxTheme.colors.borderSecondary)
        }

        MenuItem(
            label = stringResource(id = R.string.browser_menu_extensions),
            description = extensionsMenuItemDescription,
            descriptionState = if (isExtensionsProcessDisabled) {
                MenuItemState.WARNING
            } else {
                MenuItemState.ENABLED
            },
            beforeIconPainter = painterResource(id = R.drawable.mozac_ic_extension_24),
            onClick = onExtensionsMenuClick,
            modifier = Modifier.testTag(MenuDialogTestTag.EXTENSIONS),
            afterIconPainter = if (accessPoint != MenuAccessPoint.Home) {
                painterResource(id = R.drawable.mozac_ic_chevron_right_24)
            } else {
                null
            },
        )
    }
}

@Composable
private fun LibraryMenuGroup(
    onBookmarksMenuClick: () -> Unit,
    onHistoryMenuClick: () -> Unit,
    onDownloadsMenuClick: () -> Unit,
    onPasswordsMenuClick: () -> Unit,
) {
    MenuGroup {
        MenuItem(
            label = stringResource(id = R.string.library_bookmarks),
            beforeIconPainter = painterResource(id = R.drawable.mozac_ic_bookmark_tray_fill_24),
            onClick = onBookmarksMenuClick,
        )

        Divider(color = FirefoxTheme.colors.borderSecondary)

        MenuItem(
            label = stringResource(id = R.string.library_history),
            beforeIconPainter = painterResource(id = R.drawable.mozac_ic_history_24),
            onClick = onHistoryMenuClick,
        )

        Divider(color = FirefoxTheme.colors.borderSecondary)

        MenuItem(
            label = stringResource(id = R.string.library_downloads),
            beforeIconPainter = painterResource(id = R.drawable.mozac_ic_download_24),
            onClick = onDownloadsMenuClick,
        )

        Divider(color = FirefoxTheme.colors.borderSecondary)

        MenuItem(
            label = stringResource(id = R.string.browser_menu_passwords),
            beforeIconPainter = painterResource(id = R.drawable.mozac_ic_login_24),
            onClick = onPasswordsMenuClick,
        )
    }
}

@Composable
private fun HomepageMenuGroup(
    onCustomizeHomepageMenuClick: () -> Unit,
    onNewInFirefoxMenuClick: () -> Unit,
) {
    MenuGroup {
        MenuItem(
            label = stringResource(id = R.string.browser_menu_customize_home_1),
            beforeIconPainter = painterResource(id = R.drawable.mozac_ic_grid_add_24),
            onClick = onCustomizeHomepageMenuClick,
        )

        Divider(color = FirefoxTheme.colors.borderSecondary)

        MenuItem(
            label = stringResource(
                id = R.string.browser_menu_new_in_firefox,
                stringResource(id = R.string.app_name),
            ),
            beforeIconPainter = painterResource(id = R.drawable.mozac_ic_whats_new_24),
            onClick = onNewInFirefoxMenuClick,
        )
    }
}

@Composable
internal fun MozillaAccountMenuItem(
    account: Account?,
    accountState: AccountState,
    onClick: () -> Unit,
) {
    val label: String
    val description: String?

    when (accountState) {
        NotAuthenticated -> {
            label = stringResource(id = R.string.browser_menu_sign_in)
            description = stringResource(id = R.string.browser_menu_sign_in_caption)
        }

        AuthenticationProblem -> {
            label = stringResource(id = R.string.browser_menu_sign_back_in_to_sync)
            description = stringResource(id = R.string.browser_menu_syncing_paused_caption)
        }

        Authenticated -> {
            label = account?.displayName ?: account?.email
                ?: stringResource(id = R.string.browser_menu_account_settings)
            description = null
        }

        is Authenticating -> {
            label = ""
            description = null
        }
    }

    MenuItem(
        label = label,
        beforeIconPainter = painterResource(id = R.drawable.mozac_ic_avatar_circle_24),
        description = description,
        descriptionState = if (accountState is AuthenticationProblem) {
            MenuItemState.WARNING
        } else {
            MenuItemState.ENABLED
        },
        afterIconPainter = if (accountState is AuthenticationProblem) {
            painterResource(R.drawable.mozac_ic_warning_fill_24)
        } else {
            null
        },
        onClick = onClick,
    )
}

@PreviewLightDark
@Composable
private fun MenuDialogPreview() {
    FirefoxTheme {
        Column(
            modifier = Modifier
                .background(color = FirefoxTheme.colors.layer3),
        ) {
            MainMenu(
                accessPoint = MenuAccessPoint.Browser,
                account = null,
                accountState = NotAuthenticated,
                isPrivate = false,
                isDesktopMode = false,
                isPdf = false,
                isTranslationSupported = true,
                isWebCompatReporterSupported = true,
                showQuitMenu = true,
                isExtensionsProcessDisabled = true,
                extensionsMenuItemDescription = "No extensions enabled",
                scrollState = ScrollState(0),
                onMozillaAccountButtonClick = {},
                onSettingsButtonClick = {},
                onNewTabMenuClick = {},
                onNewPrivateTabMenuClick = {},
                onSwitchToDesktopSiteMenuClick = {},
                onFindInPageMenuClick = {},
                onToolsMenuClick = {},
                onSaveMenuClick = {},
                onExtensionsMenuClick = {},
                onBookmarksMenuClick = {},
                onHistoryMenuClick = {},
                onDownloadsMenuClick = {},
                onPasswordsMenuClick = {},
                onCustomizeHomepageMenuClick = {},
                onNewInFirefoxMenuClick = {},
                onQuitMenuClick = {},
                onBackButtonClick = {},
                onForwardButtonClick = {},
                onRefreshButtonClick = {},
                onShareButtonClick = {},
            )
        }
    }
}

@Preview
@Composable
private fun MenuDialogPrivatePreview() {
    FirefoxTheme(theme = Theme.Private) {
        Column(
            modifier = Modifier
                .background(color = FirefoxTheme.colors.layer3),
        ) {
            MainMenu(
                accessPoint = MenuAccessPoint.Home,
                account = null,
                accountState = NotAuthenticated,
                isPrivate = false,
                isDesktopMode = false,
                isPdf = false,
                isTranslationSupported = true,
                isWebCompatReporterSupported = true,
                showQuitMenu = true,
                isExtensionsProcessDisabled = false,
                extensionsMenuItemDescription = "No extensions enabled",
                scrollState = ScrollState(0),
                onMozillaAccountButtonClick = {},
                onSettingsButtonClick = {},
                onNewTabMenuClick = {},
                onNewPrivateTabMenuClick = {},
                onSwitchToDesktopSiteMenuClick = {},
                onFindInPageMenuClick = {},
                onToolsMenuClick = {},
                onSaveMenuClick = {},
                onExtensionsMenuClick = {},
                onBookmarksMenuClick = {},
                onHistoryMenuClick = {},
                onDownloadsMenuClick = {},
                onPasswordsMenuClick = {},
                onCustomizeHomepageMenuClick = {},
                onNewInFirefoxMenuClick = {},
                onQuitMenuClick = {},
                onBackButtonClick = {},
                onForwardButtonClick = {},
                onRefreshButtonClick = {},
                onShareButtonClick = {},
            )
        }
    }
}
