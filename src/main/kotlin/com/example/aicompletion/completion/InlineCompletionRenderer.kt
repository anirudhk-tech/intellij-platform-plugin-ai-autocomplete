package com.example.aicompletion.completion

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.markup.TextAttributes
import java.awt.Color
import java.awt.Font
import java.awt.Graphics
import java.awt.Rectangle
import javax.swing.JComponent
import java.awt.Graphics2D


class InlineCompletionRenderer(private val text: String) : JComponent() {
    override fun paint(g: Graphics) {
        if (g !is Graphics2D) return

        val originalColor = g.color
        g.color = Color(128, 128, 128, 128)
        g.font = Font(Font.MONOSPACED, Font.PLAIN, 12)

        g.drawString(text, 0, 15)
        g.color = originalColor
    }

    override fun getPreferredSize() = Rectangle(
        0, 0,
        text.length * 7,
        20
    ).size
}
