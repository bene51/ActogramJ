package actoj.gui;

public class Zoom {

	public static final int[] LEVELS  = new int[] {
		40, 30, 20, 18, 16, 15, 12, 10, 9, 8, 6, 5, 4, 3, 2, 1};

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
		int zoom = LEVELS[current];
		canvas.setZoom(zoom);
	}
}

