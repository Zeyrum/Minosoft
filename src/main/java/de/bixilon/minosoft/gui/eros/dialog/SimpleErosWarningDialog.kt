/*
 * Minosoft
 * Copyright (C) 2020-2023 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.eros.dialog

import de.bixilon.kutil.concurrent.pool.DefaultThreadPool
import de.bixilon.minosoft.data.language.IntegratedLanguage
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.gui.eros.controller.DialogController
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil.text
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.text.TextFlow
import javafx.stage.Modality

class SimpleErosWarningDialog(
    val title: Any = DEFAULT_TITLE_TEXT,
    val header: Any = DEFAULT_TITLE_TEXT,
    val description: Any? = null,
    val ignoreButtonText: Any = DEFAULT_IGNORE_TEXT,
    val onIgnore: () -> Unit = {},
    val modality: Modality = Modality.WINDOW_MODAL,
) : DialogController() {
    @FXML private lateinit var headerFX: TextFlow
    @FXML private lateinit var descriptionFX: TextFlow
    @FXML private lateinit var ignoreButtonFX: Button

    public override fun show() {
        JavaFXUtil.runLater {
            JavaFXUtil.openModal(title, LAYOUT, this, modality)
            super.show()
        }
    }

    override fun init() {
        headerFX.text = IntegratedLanguage.LANGUAGE.translate(header)
        descriptionFX.text = description?.let { IntegratedLanguage.LANGUAGE.translate(it) } ?: ChatComponent.EMPTY
        ignoreButtonFX.text = IntegratedLanguage.LANGUAGE.translate(ignoreButtonText).message
    }

    override fun postInit() {
        stage.setOnCloseRequest {
            DefaultThreadPool += onIgnore
        }

        stage.scene.root.addEventFilter(KeyEvent.KEY_PRESSED) {
            if (it.code == KeyCode.ESCAPE) {
                ignore()
            }
        }
    }

    @FXML
    fun ignore() {
        DefaultThreadPool += onIgnore
        stage.close()
    }


    companion object {
        private val LAYOUT = "minosoft:eros/dialog/simple_warning.fxml".toResourceLocation()
        private val DEFAULT_TITLE_TEXT = "minosoft:general.dialog.warning".toResourceLocation()
        private val DEFAULT_IGNORE_TEXT = "minosoft:general.ignore".toResourceLocation()
    }
}
