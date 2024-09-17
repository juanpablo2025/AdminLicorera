package org.example.ui;

import org.example.manager.CompraManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class UnifiedEditorRenderer extends AbstractCellEditor implements TableCellEditor, TableCellRenderer, ActionListener {
    private JButton button;
    private JSpinner spinner;
    private DefaultTableModel tableModel;
    private int editingRow;
    private int editingColumn;
    private CompraManager compraManager;

    public UnifiedEditorRenderer(DefaultTableModel model, CompraManager manager) {
        // Inicializar el botón
        button = new JButton("X");
        button.addActionListener(this);

        // Inicializar el spinner
        spinner = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));

        // Referencias externas
        this.tableModel = model;
        this.compraManager = manager;
    }

    @Override
    public Object getCellEditorValue() {
        // Retorna el valor adecuado dependiendo de la columna que se está editando
        if (editingColumn == 4) {  // Suponiendo que la columna del botón es la 4
            return button.getText();
        } else if (editingColumn == 1) {  // Suponiendo que la columna del spinner es la 1
            return spinner.getValue();
        }
        return null;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        // Establecer la fila y la columna actuales en edición
        this.editingRow = row;
        this.editingColumn = column;

        // Devolver el componente adecuado dependiendo de la columna
        if (column == 4) {  // Columna del botón
            return button;
        } else if (column == 1) {  // Columna del spinner
            spinner.setValue(value);  // Establece el valor actual del spinner
            return spinner;
        }
        return null;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        // Devolver el componente adecuado dependiendo de la columna
        if (column == 4) {  // Columna del botón
            return button;
        } else if (column == 1) {  // Columna del spinner
            spinner.setValue(value);  // Establece el valor actual del spinner
            return spinner;
        }
        return null;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Acción del botón: Eliminar el producto del carrito y la tabla
        if (editingColumn == 4) {  // Verifica si la acción corresponde a la columna del botón
            compraManager.removeProductFromCart(editingRow);  // Eliminar el producto del carrito
            tableModel.removeRow(editingRow);  // Eliminar la fila de la tabla
            fireEditingStopped();  // Detener la edición
        }
    }
}
