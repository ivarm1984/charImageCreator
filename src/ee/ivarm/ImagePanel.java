package ee.ivarm;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

public class ImagePanel extends JPanel {

	private static final long serialVersionUID = 1L;

	private BufferedImage image;

	private BufferedImage original;

	private int generation;

	private long fitness;

	public ImagePanel(BufferedImage image) {
		this.original = image;
		this.image = image;
		this.generation = 0;
		this.fitness = Long.MAX_VALUE;
	}

	public void setImage(BufferedImage image) {
		this.image = image;
	}

	public int getGeneration() {
		return generation;
	}

	public void setGeneration(int generation) {
		this.generation = generation;
	}

	public long getFitness() {
		return fitness;
	}

	public void setFitness(long fitness) {
		this.fitness = fitness;
	}

	@Override
	public void paintComponent(Graphics g) {
		g.drawImage(image, 0, 0, null);
		g.drawImage(original, original.getWidth() + 10, 0, null);
	}

}