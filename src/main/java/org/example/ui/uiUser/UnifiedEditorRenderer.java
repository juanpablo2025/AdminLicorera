package org.example.ui.uiUser;

import org.example.manager.userManager.VentaMesaManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static org.example.utils.Constants.*;

public class UnifiedEditorRenderer extends AbstractCellEditor implements TableCellEditor, TableCellRenderer, ActionListener {
    private JButton button;
    private JSpinner spinner;
    private DefaultTableModel tableModel;
    private int editingRow;
    private int editingColumn;
    private VentaMesaManager ventaMesaManager;

    public UnifiedEditorRenderer(DefaultTableModel model, VentaMesaManager manager) {
        // Inicializar el botón
        button = new JButton(X_BTN);
        button.addActionListener(this);

        // Inicializar el spinner
        spinner = new JSpinner(new SpinnerNumberModel(ONE, ONE, ONE, ONE));

        // Referencias externas
        this.tableModel = model;
        this.ventaMesaManager = manager;
    }

    @Override
    public Object getCellEditorValue() {
        if (editingColumn == ONE) {
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
        if (column == FOUR) {  // Columna del botón
            return button;
        } else if (column == ONE) {  // Columna del spinner
            spinner.setValue(value);  // Establece el valor actual del spinner
            return spinner;
        }
        return null;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (column == FOUR) {  // Botón
            JButton renderButton = new JButton(X_BTN);
            return renderButton;
        } else if (column == ONE) {  // Spinner
            spinner = new JSpinner(new SpinnerNumberModel(ONE, ONE, ONE_HUNDRED, ONE));  // Valores enteros
            return spinner;
        }
        return null;
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        // Detener la edición de la celda actual para evitar problemas de edición
        stopCellEditing();

        // Acción del botón: Eliminar el producto del carrito y la tabla
        if (editingColumn == FOUR) {  // Verifica si la acción corresponde a la columna del botón
            int selectedRow = editingRow;  // Guardar la fila actual

            // Verificar si el índice de la fila es válido antes de intentar eliminar
            if (selectedRow >= ZERO && selectedRow < tableModel.getRowCount()) {
                ventaMesaManager.removeProductFromCart(selectedRow);  // Eliminar el producto del carrito
                tableModel.removeRow(selectedRow);  // Eliminar la fila de la tabla

                // Notificar a la tabla que una fila ha sido eliminada
                fireEditingStopped();  // Detener la edición
            }

            // Redibuja la tabla para reflejar los cambios
            tableModel.fireTableDataChanged();
        }
    }

}

