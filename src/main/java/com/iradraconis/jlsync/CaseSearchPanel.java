package com.iradraconis.jlsync;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CaseSearchPanel extends JPanel {
    
    private JTextField searchField;
    private JList<FuzzySearchUtil.CaseResult> resultsList;
    private DefaultListModel<FuzzySearchUtil.CaseResult> listModel;
    private JScrollPane scrollPane;
    private SearchSelectionListener selectionListener;
    private Timer searchTimer;
    private boolean isSearching = false;
    
    public interface SearchSelectionListener {
        void onCaseSelected(FuzzySearchUtil.CaseResult selectedCase);
    }
    
    public CaseSearchPanel() {
        initializeComponents();
        setupEventListeners();
    }
    
    private void initializeComponents() {
        setLayout(new BorderLayout(0, 5));
        setPreferredSize(new Dimension(380, 200));
        
        searchField = new JTextField();
        searchField.setHorizontalAlignment(JTextField.LEFT);
        searchField.setText("Aktenzeichen oder Name eingeben...");
        searchField.setToolTipText("Aktenzeichen oder Aktenname eingeben");
        searchField.setFont(new Font("Cantarell", Font.PLAIN, 14));
        searchField.setPreferredSize(new Dimension(0, 35));
        searchField.setForeground(Color.WHITE);
        searchField.setBackground(new Color(60, 60, 60));
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(128, 128, 128)),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        
        listModel = new DefaultListModel<>();
        resultsList = new JList<>(listModel);
        resultsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        resultsList.setFont(new Font("Cantarell", Font.PLAIN, 12));
        resultsList.setFixedCellHeight(30);
        resultsList.setBackground(Color.WHITE);
        resultsList.setForeground(Color.BLACK);
        resultsList.setSelectionBackground(new Color(173, 216, 230));
        resultsList.setSelectionForeground(Color.BLACK);
        resultsList.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
        
        scrollPane = new JScrollPane(resultsList);
        scrollPane.setPreferredSize(new Dimension(0, 150));
        scrollPane.setVisible(false);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(128, 128, 128)),
            BorderFactory.createEmptyBorder(2, 2, 2, 2)
        ));
        
        add(searchField, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        
        searchTimer = new Timer(300, e -> performSearch());
        searchTimer.setRepeats(false);
    }
    
    private void setupEventListeners() {
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                scheduleSearch();
            }
            
            @Override
            public void removeUpdate(DocumentEvent e) {
                scheduleSearch();
            }
            
            @Override
            public void changedUpdate(DocumentEvent e) {
                scheduleSearch();
            }
        });
        
        searchField.addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DOWN && scrollPane.isVisible()) {
                    resultsList.requestFocus();
                    if (resultsList.getModel().getSize() > 0) {
                        resultsList.setSelectedIndex(0);
                    }
                } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (resultsList.getSelectedValue() != null) {
                        selectCase(resultsList.getSelectedValue());
                    }
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    hideResults();
                }
            }
            
            @Override
            public void keyTyped(KeyEvent e) {}
            
            @Override
            public void keyReleased(KeyEvent e) {}
        });
        
        resultsList.addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (resultsList.getSelectedValue() != null) {
                        selectCase(resultsList.getSelectedValue());
                    }
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    searchField.requestFocus();
                    hideResults();
                }
            }
            
            @Override
            public void keyTyped(KeyEvent e) {}
            
            @Override
            public void keyReleased(KeyEvent e) {}
        });
        
        resultsList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    FuzzySearchUtil.CaseResult selected = resultsList.getSelectedValue();
                    if (selected != null) {
                        selectCase(selected);
                    }
                }
            }
        });
        
        searchField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (searchField.getText().equals("Aktenzeichen oder Name eingeben...")) {
                    searchField.setText("");
                    searchField.setForeground(Color.WHITE);
                }
            }
            
            @Override
            public void focusLost(java.awt.event.FocusEvent evt) {
                if (searchField.getText().trim().isEmpty()) {
                    searchField.setText("Aktenzeichen oder Name eingeben...");
                    searchField.setForeground(Color.LIGHT_GRAY);
                    hideResults();
                }
            }
        });
        
        searchField.setForeground(Color.LIGHT_GRAY);
    }
    
    private void scheduleSearch() {
        if (isSearching) return;
        
        if (searchTimer.isRunning()) {
            searchTimer.restart();
        } else {
            searchTimer.start();
        }
    }
    
    private void performSearch() {
        String query = searchField.getText().trim();
        
        if (query.isEmpty() || query.equals("Aktenzeichen oder Name eingeben...")) {
            hideResults();
            return;
        }
        
        isSearching = true;
        
        SwingUtilities.invokeLater(() -> {
            try {
                List<FuzzySearchUtil.CaseResult> results = FuzzySearchUtil.fuzzySearch(query, 10);
                
                listModel.clear();
                for (FuzzySearchUtil.CaseResult result : results) {
                    listModel.addElement(result);
                }
                
                if (results.isEmpty()) {
                    hideResults();
                } else {
                    showResults();
                }
                
            } catch (FileNotFoundException e) {
                Logger.getLogger(CaseSearchPanel.class.getName()).log(Level.SEVERE, "Fehler beim Laden der Aktendaten", e);
                hideResults();
            } finally {
                isSearching = false;
            }
        });
    }
    
    private void showResults() {
        scrollPane.setVisible(true);
        revalidate();
        repaint();
    }
    
    private void hideResults() {
        scrollPane.setVisible(false);
        revalidate();
        repaint();
    }
    
    private void selectCase(FuzzySearchUtil.CaseResult selectedCase) {
        searchField.setText(selectedCase.getFileNumber());
        searchField.setForeground(Color.WHITE);
        hideResults();
        
        if (selectionListener != null) {
            selectionListener.onCaseSelected(selectedCase);
        }
    }
    
    public void setSearchSelectionListener(SearchSelectionListener listener) {
        this.selectionListener = listener;
    }
    
    public String getSearchText() {
        String text = searchField.getText().trim();
        if (text.equals("Aktenzeichen oder Name eingeben...")) {
            return "";
        }
        return text;
    }
    
    public void setSearchText(String text) {
        searchField.setText(text);
        searchField.setForeground(Color.WHITE);
    }
    
    public void clearSearch() {
        searchField.setText("Aktenzeichen oder Name eingeben...");
        searchField.setForeground(Color.LIGHT_GRAY);
        hideResults();
    }
    
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        searchField.setEnabled(enabled);
        resultsList.setEnabled(enabled);
    }
}