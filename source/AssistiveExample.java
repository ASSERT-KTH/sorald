

public class AssistiveExample extends javax.swing.JPanel implements com.sun.java.accessibility.util.GUIInitializedListener , java.awt.event.ActionListener , java.awt.event.MouseMotionListener {
    javax.swing.Timer timer;

    static javax.swing.JFrame frame;

    javax.swing.JLabel nameLabel = new javax.swing.JLabel();

    javax.swing.JLabel descriptionLabel = new javax.swing.JLabel();

    javax.swing.JLabel tableLabel = new javax.swing.JLabel();

    javax.swing.JCheckBox selectionCheckBox = new javax.swing.JCheckBox("Selection", false);

    javax.swing.JCheckBox textCheckBox = new javax.swing.JCheckBox("Text", false);

    javax.swing.JCheckBox valueCheckBox = new javax.swing.JCheckBox("Value", false);

    javax.swing.JCheckBox componentCheckBox = new javax.swing.JCheckBox("Component", false);

    javax.swing.JCheckBox actionCheckBox = new javax.swing.JCheckBox("Action", false);

    javax.swing.JCheckBox hypertextCheckBox = new javax.swing.JCheckBox("Hypertext", false);

    javax.swing.JCheckBox iconCheckBox = new javax.swing.JCheckBox("Icon", false);

    javax.swing.JCheckBox tableCheckBox = new javax.swing.JCheckBox("Table", false);

    javax.swing.JCheckBox editableTextCheckBox = new javax.swing.JCheckBox("EditableText", false);

    javax.swing.JLabel classLabel = new javax.swing.JLabel();

    javax.swing.JLabel parentLabel = new javax.swing.JLabel();

    javax.swing.JLabel relationLabel = new javax.swing.JLabel();

    javax.swing.JButton performAction = new javax.swing.JButton("Perform Action");

    public AssistiveExample() {
        AssistiveExample.frame = new javax.swing.JFrame("Assistive Example");
        setLayout(new java.awt.GridLayout(0, 1));
        add(nameLabel);
        add(descriptionLabel);
        add(tableLabel);
        add(new javax.swing.JSeparator());
        add(actionCheckBox);
        add(componentCheckBox);
        add(editableTextCheckBox);
        add(hypertextCheckBox);
        add(iconCheckBox);
        add(selectionCheckBox);
        add(tableCheckBox);
        add(textCheckBox);
        add(valueCheckBox);
        add(classLabel);
        add(parentLabel);
        add(relationLabel);
        add(performAction);
        setBorder(new javax.swing.border.TitledBorder("Accessible Component"));
        performAction.addActionListener(this);
        AssistiveExample.frame.getContentPane().add(this, java.awt.BorderLayout.CENTER);
        AssistiveExample.frame.setBounds(100, 100, 500, 600);
        AssistiveExample.frame.setVisible(true);
        if (com.sun.java.accessibility.util.EventQueueMonitor.isGUIInitialized()) {
            createGUI();
        }else {
            com.sun.java.accessibility.util.EventQueueMonitor.addGUIInitializedListener(this);
        }
        performAction.grabFocus();
    }

    public void guiInitialized() {
        createGUI();
    }

    public void createGUI() {
        com.sun.java.accessibility.util.SwingEventMonitor.addMouseMotionListener(this);
        timer = new javax.swing.Timer(500, this);
    }

    public void mouseMoved(java.awt.event.MouseEvent e) {
        timer.restart();
    }

    public void mouseDragged(java.awt.event.MouseEvent e) {
        timer.restart();
    }

    public void actionPerformed(java.awt.event.ActionEvent e) {
        java.awt.Point currentPosition = com.sun.java.accessibility.util.EventQueueMonitor.getCurrentMousePosition();
        javax.accessibility.Accessible comp = com.sun.java.accessibility.util.EventQueueMonitor.getAccessibleAt(currentPosition);
        if ((e.getActionCommand()) == "Perform Action") {
            javax.accessibility.AccessibleContext context = comp.getAccessibleContext();
            javax.accessibility.AccessibleAction action = context.getAccessibleAction();
            if (action != null)
                action.doAccessibleAction(0);
            else
                java.lang.System.out.println("No accessible action present!");

            return;
        }
        timer.stop();
        updateWindow(comp);
    }

    private void updateWindow(javax.accessibility.Accessible component) {
        if (component == null) {
            return;
        }
        actionCheckBox.setSelected(false);
        selectionCheckBox.setSelected(false);
        textCheckBox.setSelected(false);
        componentCheckBox.setSelected(false);
        valueCheckBox.setSelected(false);
        hypertextCheckBox.setSelected(false);
        iconCheckBox.setSelected(false);
        tableCheckBox.setSelected(false);
        editableTextCheckBox.setSelected(false);
        javax.accessibility.AccessibleContext context = component.getAccessibleContext();
        javax.accessibility.AccessibleRelationSet ars = context.getAccessibleRelationSet();
        nameLabel.setText(("Name: " + (context.getAccessibleName())));
        descriptionLabel.setText(("Desc: " + (context.getAccessibleDescription())));
        relationLabel.setText(("Relation: " + ars));
        if ((context.getAccessibleAction()) != null)
            actionCheckBox.setSelected(true);

        if ((context.getAccessibleSelection()) != null)
            selectionCheckBox.setSelected(true);

        if ((context.getAccessibleText()) != null) {
            textCheckBox.setSelected(true);
            if ((context.getAccessibleText()) instanceof javax.accessibility.AccessibleHypertext)
                hypertextCheckBox.setSelected(true);

        }
        if ((context.getAccessibleComponent()) != null) {
            componentCheckBox.setSelected(true);
            classLabel.setText(("Class: " + (context.getAccessibleComponent())));
            parentLabel.setText(("Parent: " + (context.getAccessibleParent())));
        }
        if ((context.getAccessibleValue()) != null)
            valueCheckBox.setSelected(true);

        if ((context.getAccessibleIcon()) != null)
            iconCheckBox.setSelected(true);

        if (((context.getAccessibleTable()) != null) || ((context.getAccessibleParent()) instanceof javax.swing.JTable)) {
            tableCheckBox.setSelected(true);
            tableLabel.setText(("Table Desc: " + (context.getAccessibleParent().getAccessibleContext().getAccessibleDescription())));
        }else {
            tableLabel.setText("");
        }
        repaint();
    }
}

