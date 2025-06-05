/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Servicios;

/**
 *
 * @author Rafael
 */
import javax.swing.text.*;

public class LimiteCaracteresDocumentFilter extends DocumentFilter {
    private int limite;

    public LimiteCaracteresDocumentFilter(int limite) {
        this.limite = limite;
    }

    @Override
    public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
        if (string == null) return;

        int newLength = fb.getDocument().getLength() + string.length();
        if (newLength <= limite) {
            super.insertString(fb, offset, string, attr);
        }
    }

    @Override
    public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
        if (text == null) return;

        int newLength = fb.getDocument().getLength() - length + text.length();
        if (newLength <= limite) {
            super.replace(fb, offset, length, text, attrs);
        }
    }
}

