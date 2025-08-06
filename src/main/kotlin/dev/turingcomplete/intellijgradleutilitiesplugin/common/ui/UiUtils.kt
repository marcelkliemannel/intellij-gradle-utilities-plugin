package dev.turingcomplete.intellijgradleutilitiesplugin.common.ui

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.ui.ClickListener
import com.intellij.ui.HyperlinkLabel
import com.intellij.ui.ScrollPaneFactory
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextArea
import com.intellij.ui.render.RenderingUtil
import com.intellij.ui.scale.JBUIScale
import com.intellij.util.ui.GridBag
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.components.BorderLayoutPanel
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.datatransfer.StringSelection
import java.awt.event.InputEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JLabel
import javax.swing.JTable
import javax.swing.UIManager

internal object UiUtils {
  // -- Properties ---------------------------------------------------------- //
  // -- Initialization ------------------------------------------------------ //
  // -- Exposed Methods ----------------------------------------------------- //

  fun createDefaultGridBag() =
    GridBag()
      .setDefaultAnchor(GridBagConstraints.NORTHWEST)
      .setDefaultInsets(0, 0, 0, 0)
      .setDefaultFill(GridBagConstraints.NONE)

  fun createCopyToClipboardButton(value: () -> String): JLabel {
    return object : JLabel(AllIcons.Actions.Copy) {

      init {
        object : ClickListener() {
            override fun onClick(e: MouseEvent, clickCount: Int): Boolean {
              CopyPasteManager.getInstance().setContents(StringSelection(value()))
              return true
            }
          }
          .installOn(this)

        toolTipText = "Copy to Clipboard"
      }
    }
  }

  fun createLink(title: String, url: String): HyperlinkLabel {
    return HyperlinkLabel(title).apply { setHyperlinkTarget(url) }
  }

  // -- Private Methods ----------------------------------------------------- //
  // -- Inner Type ---------------------------------------------------------- //

  object Table {
    fun createContextMenuMouseListener(
      place: String,
      actionGroup: () -> ActionGroup?,
    ): MouseAdapter {
      return object : MouseAdapter() {
        override fun mousePressed(e: MouseEvent) {
          handleMouseEvent(e)
        }

        override fun mouseReleased(e: MouseEvent) {
          handleMouseEvent(e)
        }

        private fun handleMouseEvent(e: InputEvent) {
          if (e is MouseEvent && e.isPopupTrigger) {
            actionGroup()?.let {
              ActionManager.getInstance()
                .createActionPopupMenu(place, it)
                .component
                .show(e.getComponent(), e.x, e.y)
            }
          }
        }
      }
    }

    fun JBLabel.formatCell(table: JTable, isSelected: Boolean): JBLabel {
      this.foreground = RenderingUtil.getForeground(table, isSelected)
      this.background = RenderingUtil.getBackground(table, isSelected)
      componentOrientation = table.componentOrientation
      font = table.font
      isEnabled = table.isEnabled
      border = JBUI.Borders.empty(2, 3)
      return this
    }
  }

  // -- Inner Type ---------------------------------------------------------- //

  object Panel {
    class TextAreaPanel(value: String) : BorderLayoutPanel() {

      init {
        minimumSize = Dimension(150, 50)
        preferredSize = Dimension(550, 300)

        val textArea = JBTextArea(value).apply { isEditable = false }

        addToCenter(
          ScrollPaneFactory.createScrollPane(textArea).apply {
            minimumSize = this@TextAreaPanel.minimumSize
            preferredSize = this@TextAreaPanel.preferredSize
          }
        )
      }
    }
  }
}

fun GridBag.overrideLeftInset(leftInset: Int): GridBag {
  this.insets(this.insets.top, leftInset, this.insets.bottom, this.insets.right)
  return this
}

fun GridBag.overrideBottomInset(bottomInset: Int): GridBag {
  this.insets(this.insets.top, this.insets.left, bottomInset, this.insets.right)
  return this
}

fun GridBag.overrideTopInset(topInset: Int): GridBag {
  this.insets(topInset, this.insets.left, this.insets.bottom, this.insets.right)
  return this
}

fun JBLabel.copyable(): JBLabel {
  setCopyable(true)
  return this
}

fun JBLabel.xlFont(): JBLabel {
  font = font.deriveFont(UIManager.getFont("Label.font").size + JBUIScale.scale(2f))
  return this
}

fun JBLabel.xxlFont(): JBLabel {
  font = font.deriveFont(UIManager.getFont("Label.font").size + JBUIScale.scale(4f))
  return this
}
