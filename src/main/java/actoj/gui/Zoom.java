package actoj.gui;

public class Zoom {

	public static final double[] LEVELS  = new double[] {
		40, 30, 20, 18, 16, 15, 12,
		10, 9, 8, 6, 5, 4, 3, 2, 1,
		0.5, 0.33, 0.25, 0.2, 0.1};

	public static final int DEFAULT_ZOOM = 9; // i.e. LEVELS[9] = 8
	
	private int current = DEFAULT_ZOOM;
	private final ImageCanvas canvas;

	public Zoom(ImageCanvas canvas) {
		this.canvas = canvas;
	}

	public void zoomIn() {
		current = Math.min(current + 1, LEVELS.length);
		updateCanvas();
	}

	public void zoomOut() {
		current = Math.max(0, current - 1);
		updateCanvas();
	}

	public void zoom(int idx) {
		current = idx;
		updateCanvas();
	}

	public int getZoomIndex() {
		return current;
	}

	private void updateCanvas() {
		double zoom = LEVELS[current];
		canvas.setZoom(zoom);
	}
}

