package com.github.israelkli.intellijplugincopyfilewithproblems.settings

import com.intellij.openapi.options.Configurable
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import javax.swing.*

class PluginConfigurable : Configurable {

    private val errorsCheckBox = JCheckBox("Errors")
    private val warningsCheckBox = JCheckBox("Warnings")
    private val weakWarningsCheckBox = JCheckBox("Weak warnings")
    private val infoCheckBox = JCheckBox("Info / hints")
    private val notificationGroup = ButtonGroup()
    private val noneRadio = JRadioButton("None")
    private val statusBarRadio = JRadioButton("Status bar")
    private val balloonRadio = JRadioButton("Balloon")
    private val fallbackGroup = ButtonGroup()
    private val inlineRadio = JRadioButton("Inline in file (may break syntax)")
    private val summaryRadio = JRadioButton("Append summary at end")
    private var panel: JPanel? = null

    override fun createComponent(): JComponent {
        notificationGroup.add(noneRadio)
        notificationGroup.add(statusBarRadio)
        notificationGroup.add(balloonRadio)

        fallbackGroup.add(inlineRadio)
        fallbackGroup.add(summaryRadio)

        val panel = JPanel(GridBagLayout()).apply {
            val c = GridBagConstraints()
            c.anchor = GridBagConstraints.WEST
            c.fill = GridBagConstraints.HORIZONTAL
            c.insets = Insets(2, 8, 2, 8)

            c.gridx = 0; c.gridy = 0; c.gridwidth = 2
            add(JLabel("Severity levels to include:"), c)

            c.gridwidth = 1; c.gridy = 1; c.gridx = 0
            add(errorsCheckBox, c)
            c.gridy = 1; c.gridx = 1
            add(warningsCheckBox, c)
            c.gridy = 2; c.gridx = 0
            add(weakWarningsCheckBox, c)
            c.gridy = 2; c.gridx = 1
            add(infoCheckBox, c)

            c.gridy = 3; c.gridx = 0; c.gridwidth = 2
            add(Box.createVerticalStrut(8), c)

            c.gridy = 4
            add(JLabel("Comment-incompatible languages (e.g., JSON):"), c)

            c.gridy = 5; c.gridx = 0; c.gridwidth = 2
            add(summaryRadio, c)
            c.gridy = 6; c.gridx = 0; c.gridwidth = 2
            add(inlineRadio, c)

            c.gridy = 7; c.gridx = 0; c.gridwidth = 2
            add(Box.createVerticalStrut(8), c)

            c.gridy = 8; c.gridx = 0; c.gridwidth = 2
            add(JLabel("Notification after copy:"), c)

            c.gridy = 9; c.gridx = 0; c.gridwidth = 1
            add(statusBarRadio, c)
            c.gridy = 9; c.gridx = 1
            add(balloonRadio, c)
            c.gridy = 10; c.gridx = 0; c.gridwidth = 2
            add(noneRadio, c)
        }

        this.panel = panel
        reset()
        return panel
    }

    override fun isModified(): Boolean {
        val state = PluginSettings.getInstance().state
        return errorsCheckBox.isSelected != state.severityFilterErrors ||
                warningsCheckBox.isSelected != state.severityFilterWarnings ||
                weakWarningsCheckBox.isSelected != state.severityFilterWeakWarnings ||
                infoCheckBox.isSelected != state.severityFilterInfo ||
                (summaryRadio.isSelected && state.commentIncompatibleFallback != "APPEND_SUMMARY") ||
                (inlineRadio.isSelected && state.commentIncompatibleFallback != "INLINE_ANYWAY") ||
                (noneRadio.isSelected && state.notificationLevel != "NONE") ||
                (statusBarRadio.isSelected && state.notificationLevel != "STATUS_BAR") ||
                (balloonRadio.isSelected && state.notificationLevel != "BALLOON")
    }

    override fun apply() {
        val state = PluginSettings.getInstance().state
        state.severityFilterErrors = errorsCheckBox.isSelected
        state.severityFilterWarnings = warningsCheckBox.isSelected
        state.severityFilterWeakWarnings = weakWarningsCheckBox.isSelected
        state.severityFilterInfo = infoCheckBox.isSelected
        state.commentIncompatibleFallback = when {
            summaryRadio.isSelected -> "APPEND_SUMMARY"
            else -> "INLINE_ANYWAY"
        }
        state.notificationLevel = when {
            noneRadio.isSelected -> "NONE"
            balloonRadio.isSelected -> "BALLOON"
            else -> "STATUS_BAR"
        }
    }

    override fun reset() {
        val state = PluginSettings.getInstance().state
        errorsCheckBox.isSelected = state.severityFilterErrors
        warningsCheckBox.isSelected = state.severityFilterWarnings
        weakWarningsCheckBox.isSelected = state.severityFilterWeakWarnings
        infoCheckBox.isSelected = state.severityFilterInfo
        when (state.commentIncompatibleFallback) {
            "APPEND_SUMMARY" -> summaryRadio.isSelected = true
            else -> inlineRadio.isSelected = true
        }
        when (state.notificationLevel) {
            "NONE" -> noneRadio.isSelected = true
            "BALLOON" -> balloonRadio.isSelected = true
            else -> statusBarRadio.isSelected = true
        }
    }

    override fun getDisplayName(): String = "Copy with Inline Issues"
}
