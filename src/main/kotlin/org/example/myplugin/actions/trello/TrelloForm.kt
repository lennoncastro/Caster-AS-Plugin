package org.example.myplugin.actions.trello

import com.intellij.notification.NotificationDisplayType
import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.layout.panel
import org.example.myplugin.utils.StringsBundle
import java.awt.Dimension
import javax.swing.*

class TrelloForm(
    private val project: Project,
    trelloInjector: TrelloInjector
): DialogWrapper(project), TrelloFormView {

    private var nameCombo: ComboBox<Card> = ComboBox<Card>().apply {
        name = "nameCombo"
    }
    private val descriptionPane: JTextPane = JTextPane().apply {
        name = "descriptionPane"
        isEditable = false
    }

    private val presenter: TrelloActionPresenter by lazy {
        trelloInjector.trelloActionPresenter(this, project)
    }

    init {
        init()

        nameCombo.addActionListener {
            val card = nameCombo.selectedItem as Card?

            card?.let {
                descriptionPane.text = it.desc
            }
        }

        presenter.loadCards()
    }

    override fun doOKAction() {
        val card = nameCombo.selectedItem as Card?
        card?.let {
            presenter.moveCard(it)
        }
    }

    override fun createCenterPanel(): JComponent? = panel {
        row(StringsBundle.string("trello.name")) {
            nameCombo(grow)
        }
        row(StringsBundle.string("trello.description")) {
            descriptionPane()
        }
    }.apply {
        minimumSize = Dimension(500, 340)
        preferredSize = Dimension(500, 340)
    }

    override fun showCards(cards: List<Card>) {
        cards.map {
            nameCombo.addItem(it)
        }

        if (cards.isNotEmpty()) {
            nameCombo.selectedIndex = 0
        }
    }

    override fun success() {
        close(OK_EXIT_CODE)

        ApplicationManager.getApplication().invokeLater {
            val notificationGroup = NotificationGroup("success", NotificationDisplayType.BALLOON, true)
            notificationGroup.createNotification(
                    StringsBundle.string("success"),
                    StringsBundle.string("trello.cardMoved"),
                NotificationType.INFORMATION,
                null
            ).notify(project)
        }
    }

    override fun error(error: Throwable) {
        ApplicationManager.getApplication().invokeLater {
            val notificationGroup = NotificationGroup("error", NotificationDisplayType.BALLOON, true)
            notificationGroup.createNotification(
                    StringsBundle.string("error"),
                error.localizedMessage,
                NotificationType.ERROR,
                null
            ).notify(project)
        }
    }
}