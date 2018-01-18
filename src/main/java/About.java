import javax.swing.JFrame;
import java.awt.GridBagLayout;
import java.awt.Image;

import javax.swing.JLabel;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import javax.swing.ImageIcon;

public class About extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = -173859703961811969L;

	private final static Integer WIDTH = 2000/4, HEIGHT = 1224 / 4;
	
	public About() {
		setType(Type.UTILITY);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		getContentPane().setLayout(gridBagLayout);
		
		JLabel lblAboutImage = new JLabel("");
		Image aboutImage = new ImageIcon(About.class.getResource("about.png")).getImage();
		lblAboutImage.setIcon(new ImageIcon(aboutImage.getScaledInstance(WIDTH, HEIGHT, Image.SCALE_DEFAULT)));
		GridBagConstraints gbc_lblAboutImage = new GridBagConstraints();
		gbc_lblAboutImage.gridx = 0;
		gbc_lblAboutImage.gridy = 0;
		getContentPane().add(lblAboutImage, gbc_lblAboutImage);
		
		
		
		this.setMaximumSize(new Dimension(WIDTH, HEIGHT));
		this.setMinimumSize(new Dimension(WIDTH, HEIGHT));
		setSize(WIDTH, HEIGHT);
	}

}
