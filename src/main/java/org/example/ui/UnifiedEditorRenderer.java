package org.example.ui;

import org.example.manager.VentaManager;

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
    private VentaManager ventaManager;

    public UnifiedEditorRenderer(DefaultTableModel model, VentaManager manager) {
        // Inicializar el botón
        button = new JButton("X");
        button.addActionListener(this);

        // Inicializar el spinner
        spinner = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));

        // Referencias externas
        this.tableModel = model;
        this.ventaManager = manager;
    }

    @Override
    public Object getCellEditorValue() {
        if (editingColumn == 1) {
            return spinner.getValue(); // Retorna el valor actualizado del spinner
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
        if (column == 4) {  // Botón
            JButton renderButton = new JButton("X");
            return renderButton;
        } else if (column == 1) {  // Spinner
            spinner = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));  // Valores enteros
            return spinner;
        }
        return null;
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        // Detener la edición de la celda actual para evitar problemas de edición
        stopCellEditing();

        // Acción del botón: Eliminar el producto del carrito y la tabla
        if (editingColumn == 4) {  // Verifica si la acción corresponde a la columna del botón
            int selectedRow = editingRow;  // Guardar la fila actual

            // Verificar si el índice de la fila es válido antes de intentar eliminar
            if (selectedRow >= 0 && selectedRow < tableModel.getRowCount()) {
                ventaManager.removeProductFromCart(selectedRow);  // Eliminar el producto del carrito
                tableModel.removeRow(selectedRow);  // Eliminar la fila de la tabla

                // Notificar a la tabla que una fila ha sido eliminada
                fireEditingStopped();  // Detener la edición
            }

            // Redibuja la tabla para reflejar los cambios
            tableModel.fireTableDataChanged();
        }
    }

}

