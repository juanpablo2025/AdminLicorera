package org.example.ui.uiuser;


import org.example.manager.usermanager.ProductoUserManager;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class UnifiedEditorRenderer extends AbstractCellEditor implements TableCellEditor, TableCellRenderer, ActionListener {
    private final JButton buttonRestar;
    private final JButton buttonEliminar;
    private final JSpinner spinner;

    private JTable currentTable;
    private int editingRow;
    private int editingColumn;

    public UnifiedEditorRenderer() {

        buttonRestar = new JButton("-1");
        buttonRestar.setBackground(new Color(201, 79, 79));
        buttonRestar.setForeground(Color.WHITE);
        buttonRestar.setPreferredSize(new Dimension(60, 30));
        buttonRestar.setFocusPainted(false);
        buttonRestar.setBorderPainted(false);
        buttonRestar.setActionCommand("Restar");
        buttonRestar.addActionListener(this);

        buttonEliminar = new JButton("✖");
        buttonEliminar.setBackground(new Color(140, 20, 20));
        buttonEliminar.setForeground(Color.WHITE);
        buttonEliminar.setPreferredSize(new Dimension(35, 30));
        buttonEliminar.setFocusPainted(false);
        buttonEliminar.setBorderPainted(false);
        buttonEliminar.setActionCommand("Eliminar");
        buttonEliminar.addActionListener(this);

        spinner = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
    }

    @Override
    public Object getCellEditorValue() {
        if (editingColumn == 1) {
            return spinner.getValue();
        }
        return null;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        this.editingRow = row;
        this.editingColumn = column;
        this.currentTable = table;

        if (column == 4) {
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
            panel.setBackground(table.getBackground());
            panel.add(buttonRestar);
            panel.add(buttonEliminar);
            return panel;
        } else if (column == 1) {
            spinner.setValue(value);
            return spinner;
        }
        return null;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (column == 4) {
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));

            JButton btnRestar = new JButton("-1");
            btnRestar.setBackground(new Color(201, 79, 79));
            btnRestar.setForeground(Color.WHITE);
            btnRestar.setFocusable(false);
            btnRestar.setPreferredSize(new Dimension(60, 30));

            JButton btnEliminar = new JButton("✖");
            btnEliminar.setBackground(new Color(140, 20, 20));
            btnEliminar.setForeground(Color.WHITE);
            btnEliminar.setFocusable(false);
            btnEliminar.setPreferredSize(new Dimension(35, 30));

            panel.setBackground(table.getBackground());
            panel.add(btnRestar);
            panel.add(btnEliminar);
            return panel;
        } else if (column == 1) {
            return new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
        }
        return null;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        stopCellEditing();
        DefaultTableModel tableModel = (DefaultTableModel) currentTable.getModel();
        if (editingRow >= 0 && editingRow < tableModel.getRowCount()) {
            Object cantidadObj = tableModel.getValueAt(editingRow, 1);
            String action = e.getActionCommand();

            if ("Restar".equals(action)) {
                if (cantidadObj instanceof Integer) {
                    int cantidadActual = (int) cantidadObj;
                    if (cantidadActual > 1) {
                        tableModel.setValueAt(cantidadActual - 1, editingRow, 1);
                    } else {
                        removeProductFromCart(editingRow);
                        tableModel.removeRow(editingRow);
                    }
                    SwingUtilities.invokeLater(tableModel::fireTableDataChanged);
                }
            } else if ("Eliminar".equals(action)) {
                removeProductFromCart(editingRow);
                tableModel.removeRow(editingRow);
                SwingUtilities.invokeLater(tableModel::fireTableDataChanged);
            }
        }
    }

    private void removeProductFromCart(int row) {
        ProductoUserManager.removeProductFromCart(row);
    }
}

