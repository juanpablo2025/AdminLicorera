package org.example.ui.uiUser;


import org.example.manager.userManager.VentaMesaUserManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static org.example.utils.Constants.*;
import static org.example.manager.userManager.ProductoUserManager.*;

public class UnifiedEditorRenderer extends AbstractCellEditor implements TableCellEditor, TableCellRenderer, ActionListener {
    private JButton button;
    private JSpinner spinner;
    private DefaultTableModel tableModel;
    private int editingRow;
    private int editingColumn;
    private VentaMesaUserManager ventaMesaUserManager;

    public UnifiedEditorRenderer(DefaultTableModel model, VentaMesaUserManager manager) {
        // Inicializar el botón
        button = new JButton(X_BTN);
        button.addActionListener(this);

        // Inicializar el spinner
        spinner = new JSpinner(new SpinnerNumberModel(ONE, ONE, ONE, ONE));

        // Referencias externas
        this.tableModel = model;
        this.ventaMesaUserManager = manager;
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

        // Acción del botón: Eliminar una unidad o el producto si solo queda una
        if (editingColumn == FOUR) {  // Verifica si la acción corresponde a la columna del botón
            int selectedRow = editingRow;  // Guardar la fila actual

            // Verificar si el índice de la fila es válido antes de intentar eliminar
            if (selectedRow >= 0 && selectedRow < tableModel.getRowCount()) {
                // Acceder al valor del JSpinner en la columna de cantidad (suponiendo que la columna de cantidad es la columna 1)
                Object cantidadObj = tableModel.getValueAt(selectedRow, ONE);

                // Validar que cantidadObj no sea null y que pueda convertirse a entero
                if (cantidadObj instanceof Integer) {
                    int cantidadActual = (int) cantidadObj;

                    if (cantidadActual > 1) {
                        // Si hay más de una unidad, se reduce en una
                        tableModel.setValueAt(cantidadActual - 1, selectedRow, ONE);
                    } else {
                        // Si solo queda una unidad, se elimina la fila entera
                        removeProductFromCart(selectedRow);  // Eliminar el producto del carrito
                        tableModel.removeRow(selectedRow);  // Eliminar la fila de la tabla
                    }

                    // Redibuja la tabla para reflejar los cambios de forma adecuada sin afectar la edición
                    SwingUtilities.invokeLater(() -> tableModel.fireTableDataChanged());
                }
            }
        }
    }

}

