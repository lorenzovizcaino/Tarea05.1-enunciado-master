package modelo.main;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import modelo.Departamento;
import java.awt.Color;

public class CreateNewDeptDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final JPanel contentPanel = new JPanel();
	private JTextField textFieldUbicacion;
	private JTextField textFieldNombreDept;
	private JButton okButton;
	private JLabel lblError;
	private Departamento departamentoACrearOActualizar = null;
	
	private JLabel lblId;

	public Departamento getResult() {
		return this.departamentoACrearOActualizar;
	}

	public enum TIPO_EDICION {
		EDITAR, CREAR
	};

	private TIPO_EDICION tipo = TIPO_EDICION.CREAR;
	private JTextField textFieldDeptno;

	/**
	 * Create the dialog.
	 */
	public void initComponents() {

		setBounds(100, 100, 598, 300);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(null);

		JLabel lblDeptName = new JLabel("Nombre departamento");
		lblDeptName.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblDeptName.setBounds(39, 34, 208, 24);
		contentPanel.add(lblDeptName);

		textFieldUbicacion = new JTextField();
		textFieldUbicacion.setFont(new Font("Tahoma", Font.PLAIN, 14));
		textFieldUbicacion.setBounds(330, 83, 197, 23);
		contentPanel.add(textFieldUbicacion);
		textFieldUbicacion.setColumns(10);

		JLabel lblDeptLocation = new JLabel("Ubicación");
		lblDeptLocation.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblDeptLocation.setBounds(39, 82, 140, 24);
		contentPanel.add(lblDeptLocation);

		textFieldNombreDept = new JTextField();
		textFieldNombreDept.setFont(new Font("Tahoma", Font.PLAIN, 14));
		textFieldNombreDept.setColumns(10);
		textFieldNombreDept.setBounds(330, 35, 197, 23);
		contentPanel.add(textFieldNombreDept);

		 lblId= new JLabel("Dept. no");
		lblId.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblId.setBounds(39, 135, 140, 24);
		contentPanel.add(lblId);

		textFieldDeptno = new JTextField();
		textFieldDeptno.setFont(new Font("Tahoma", Font.PLAIN, 14));
		textFieldDeptno.setColumns(10);
		textFieldDeptno.setBounds(330, 136, 197, 23);
		contentPanel.add(textFieldDeptno);

		lblError = new JLabel("Error label");
		lblError.setForeground(new Color(255, 0, 0));
		lblError.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblError.setBounds(52, 187, 294, 24);
		lblError.setVisible(false);
		contentPanel.add(lblError);

		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
		getContentPane().add(buttonPane, BorderLayout.SOUTH);

		okButton = new JButton("Guardar");

		okButton.setActionCommand("OK");
		buttonPane.add(okButton);
		getRootPane().setDefaultButton(okButton);

		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				departamentoACrearOActualizar = null;
				CreateNewDeptDialog.this.dispose();

			}
		});
		cancelButton.setActionCommand("Cancel");
		buttonPane.add(cancelButton);

		ActionListener crearBtnActionListener = new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				if (!(textFieldUbicacion.getText().trim().equals(""))
						&& !(textFieldNombreDept.getText().trim().equals(""))) {
					if (departamentoACrearOActualizar == null) {
						// Solo para creación
						departamentoACrearOActualizar = new Departamento();
					}
					departamentoACrearOActualizar.setDname(textFieldNombreDept.getText().trim());
					departamentoACrearOActualizar.setLoc(textFieldUbicacion.getText().trim());
					int deptno = getDeptnoFromTextField();
					if (deptno != -1) {
						departamentoACrearOActualizar.setDeptno(deptno);
						CreateNewDeptDialog.this.dispose();
					} else {
						lblError.setText("Introduzca un entero en número de departamento");
						lblError.setVisible(true);
					}

				}
			}
		};

		this.okButton.addActionListener(crearBtnActionListener);

	}

	public CreateNewDeptDialog(Window owner, String title, ModalityType modalityType, Departamento dept,
			TIPO_EDICION tipo) {
		super(owner, title, modalityType);
		initComponents();
		departamentoACrearOActualizar = dept;
		this.tipo = tipo;
		if (departamentoACrearOActualizar != null) {
			textFieldNombreDept.setText(departamentoACrearOActualizar.getDname());
			textFieldUbicacion.setText(departamentoACrearOActualizar.getLoc());
			textFieldDeptno.setText(String.valueOf(departamentoACrearOActualizar.getDeptno()));

			// No permitir cambio de deptno en edición
			textFieldDeptno.setVisible(tipo != TIPO_EDICION.EDITAR);
			lblId.setVisible(tipo !=TIPO_EDICION.EDITAR);

		}
		lblError.setVisible(false);
		this.setLocationRelativeTo(owner);
	}

	public TIPO_EDICION getTipo() {
		return tipo;
	}

	private int getDeptnoFromTextField() {
		int deptno = -1;
		String textIntroducido = textFieldDeptno.getText().trim();
		try {
			deptno = Integer.parseInt(textIntroducido);
		} catch (Exception nfe) {
			deptno = -1;
		}
		return deptno;
	}
}
