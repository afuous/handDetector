import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.File;
import java.nio.file.*;
import javax.imageio.ImageIO;
import javax.script.*;
import javax.swing.*;
import org.opencv.core.*;
import org.opencv.highgui.*;
import org.opencv.imgproc.Imgproc;

public class HandDetector extends JPanel implements ActionListener, KeyListener {
	
	public static void main(String[] args) {
		int width = 640;
		int height = 480;
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		JFrame frame = new JFrame("");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null);
		HandDetector handDetector = new HandDetector(frame, width, height);
		frame.getContentPane().add(handDetector);
		frame.addKeyListener(handDetector);
		frame.getContentPane().setPreferredSize(new Dimension(width, height));
		frame.pack();
		frame.setVisible(true);
		
		int delay = 10;
		Timer timer = new Timer(delay, handDetector);
		timer.setInitialDelay(delay);
		timer.start();
	}
	
	final static boolean LEFT = false;
	final static boolean RIGHT = true;
	
	int width;
	int height;
	BufferedImage image;
	VideoCapture camera;
	ScriptEngine engine = null;
	
	public HandDetector(JFrame frame, int width, int height) {
		this.width = width;
		this.height = height;
		this.setSize(width, height);
		camera = new VideoCapture(0);
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent event) {
				camera.release();
			}
		});
		try {
			engine = new ScriptEngineManager().getEngineByName("javascript");
			engine.eval(new String(Files.readAllBytes(Paths.get("script.js"))));
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public void paintComponent(Graphics graphics) {
		super.paintComponent(graphics);
		Graphics2D g = (Graphics2D) graphics;
		if(image != null) {
			g.drawImage(image, 0, 0, null);
		}
		g.setStroke(new BasicStroke(5));
		g.drawRect(0, 0, searchWidth, searchHeight);
		g.drawRect(width - searchWidth, 0, searchWidth, searchHeight);
		g.setColor(Color.red);
		if(handLeft) g.fillRect(searchWidth / 4, searchHeight + (height - searchHeight) / 4, searchWidth / 2, (height - searchHeight) / 2);
		if(handRight) g.fillRect(width - searchWidth * 3 / 4, searchHeight + (height - searchHeight) / 4, searchWidth / 2, (height - searchHeight) / 2);
		String str = null;
		if(handColorLeft == null || backgroundColorRight == null) {
			str = "Hold your left hand up and right hand down and press space";
		}
		else if(backgroundColorLeft == null || handColorRight == null) {
			str = "Hold your right hand up and left hand down and press space";
		}
		if(str != null) {
			g.setFont(new Font("Arial", Font.BOLD, 20));
			FontMetrics metrics = g.getFontMetrics();
			g.drawString(str, (width - metrics.stringWidth(str)) / 2, metrics.getHeight() / 2 + 10);
		}
	}
	
	boolean handLeft = false;
	boolean handRight = false;
	
	public void actionPerformed(ActionEvent event) {
		Mat frame = new Mat();
		if(camera.read(frame)) {
			Core.flip(frame, frame, 1);
			image = matToBufferedImage(frame);
			boolean hadLeft = handLeft;
			boolean hadRight = handRight;
			checkHand();
			try {
				if(handLeft && !hadLeft) engine.eval("leftChange(true)");
				if(!handLeft && hadLeft) engine.eval("leftChange(false)");
				if(handRight && !hadRight) engine.eval("rightChange(true)");
				if(!handRight && hadRight) engine.eval("rightChange(false)");
			}
			catch(Exception ex) {
				ex.printStackTrace();
			}
			repaint();
		}
	}
	
	private BufferedImage matToBufferedImage(Mat mat) {
		int type = mat.channels() > 1 ? BufferedImage.TYPE_3BYTE_BGR : BufferedImage.TYPE_BYTE_GRAY;
		byte[] buffer = new byte[mat.channels() * mat.cols() * mat.rows()];
		mat.get(0, 0, buffer);
		BufferedImage image = new BufferedImage(mat.cols(), mat.rows(), type);
		System.arraycopy(buffer, 0, ((DataBufferByte) image.getRaster().getDataBuffer()).getData(), 0, buffer.length);
		return image;
	}
	
	private void checkHand() {
		final double multiplier = 1.3;
		if(handColorLeft == null || handColorRight == null || backgroundColorLeft == null || backgroundColorRight == null) return;
		
		AverageColor averageColorLeft = getAverageColor(LEFT);
		double handDiffLeft = averageColorLeft.differenceIntensity(handColorLeft);
		double backgroundDiffLeft = averageColorLeft.differenceIntensity(backgroundColorLeft);
		handLeft = handDiffLeft < backgroundDiffLeft * multiplier;
		
		AverageColor averageColorRight = getAverageColor(RIGHT);
		double handDiffRight = averageColorRight.differenceIntensity(handColorRight);
		double backgroundDiffRight = averageColorRight.differenceIntensity(backgroundColorRight);
		handRight = handDiffRight < backgroundDiffRight * multiplier;
	}
	
	final int searchWidth = 150;
	final int searchHeight = 300;
		
	private AverageColor getAverageColor(boolean side) {
		int red = 0;
		int green = 0;
		int blue = 0;
		for(int i = 0; i < searchWidth; i++) {
			for(int j = 0; j < searchHeight; j++) {
				Color color = new Color(image.getRGB(i + (side == LEFT ? 0 : width - searchWidth), j));
				red += color.getRed();
				green += color.getGreen();
				blue += color.getBlue();
			}
		}
		double size = (double) (searchWidth * searchHeight);
		return new AverageColor(red / size, green / size, blue / size);
	}
	
	private class AverageColor {
		
		private double red;
		private double green;
		private double blue;
		
		public AverageColor(double red, double green, double blue) {
			this.red = red;
			this.green = green;
			this.blue = blue;
		}
		
		public double differenceIntensity(AverageColor color) {
			double dRed = Math.abs(this.red - color.red);
			double dGreen = Math.abs(this.green - color.green);
			double dBlue = Math.abs(this.blue - color.blue);
			return 0.02989 * dRed + 0.5870 * dGreen + 0.1140 * dBlue;
		}
	}
	
	AverageColor handColorLeft;
	AverageColor backgroundColorLeft;
	AverageColor handColorRight;
	AverageColor backgroundColorRight;
	
	public void keyPressed(KeyEvent event) {
		if(event.getKeyCode() == KeyEvent.VK_SPACE) {
			if(handColorLeft == null || backgroundColorRight == null) {
				handColorLeft = getAverageColor(LEFT);
				backgroundColorRight = getAverageColor(RIGHT);
			}
			else if(backgroundColorLeft == null || handColorRight == null) {
				backgroundColorLeft = getAverageColor(LEFT);
				handColorRight = getAverageColor(RIGHT);
			}
		}
	}
	public void keyReleased(KeyEvent event) {}
	public void keyTyped(KeyEvent event) {}
}