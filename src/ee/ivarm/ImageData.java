package ee.ivarm;

import java.util.List;

public class ImageData implements Comparable<ImageData> {

	private List<CharPreferences> preferences;

	private Long fitness;

	public ImageData(List<CharPreferences> preferences) {
		this.preferences = preferences;
	}

	public void setFitness(Long fitness) {
		this.fitness = fitness;
	}

	public Long getFitness() {
		return fitness;
	}

	public List<CharPreferences> getPreferences() {
		return preferences;
	}

	public void setPreferences(List<CharPreferences> preferences) {
		this.preferences = preferences;
	}

	@Override
	public int compareTo(ImageData o) {
		return fitness.compareTo(o.getFitness());
	}

	@Override
	public String toString() {
		return fitness.toString();
	}

}
