import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.IntBuffer;

import javax.imageio.ImageIO;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWScrollCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL45;
import org.lwjgl.system.MemoryStack;

public class Visualization {
	private long window;
	int wWidth, wHeight;
	
	private static int QueenTexture;
	private Algorithm alg;
	
	private float verticalPos = 0;

	public void run(Algorithm algorithm) {
		this.alg = algorithm;
		
		init();
		loop();
				
		Callbacks.glfwFreeCallbacks(window);
		GLFW.glfwDestroyWindow(window);
				
		GLFW.glfwTerminate();
		GLFW.glfwSetErrorCallback(null).free();
	}

	public void init() {
		GLFWErrorCallback.createPrint(System.err).set();
		
		if (!GLFW.glfwInit())
			throw new IllegalStateException("Couldn't initialize GLFW");
				
		GLFW.glfwDefaultWindowHints();
		GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
		GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE);
		GLFW.glfwWindowHint(GLFW.GLFW_MAXIMIZED, GLFW.GLFW_TRUE);
		
		window = GLFW.glfwCreateWindow(1280, 720, "Queens Problem Visualization", NULL, NULL);
		if (window == NULL)
			throw new IllegalStateException("Unable to create GLFW Window");
		
		try (MemoryStack stack = stackPush())
		{
			IntBuffer pWidth = stack.mallocInt(1);
			IntBuffer pHeight = stack.mallocInt(1);
			
			GLFW.glfwGetWindowSize(window, pWidth, pHeight);
			
			wWidth = pWidth.get(0);
			wHeight = pHeight.get(0);
			
			GLFWVidMode vidmode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
			GLFW.glfwSetWindowPos(window,(vidmode.width() - pWidth.get(0)) / 2,(vidmode.height() - pHeight.get(0)) / 2);
			
			GLFW.glfwMakeContextCurrent(window);
			GLFW.glfwSwapInterval(1);
			GLFW.glfwShowWindow(window);
		}
		GL.createCapabilities();
		
		setAspectRatio(wWidth, wHeight);
		
		GLFW.glfwSetWindowSizeCallback(window, (w, x, y) -> {
			wHeight = y;
			wWidth = x;
			setAspectRatio(x, y);
		});
		
		GLFW.glfwSetScrollCallback(window, new GLFWScrollCallback() {
		    @Override public void invoke (long win, double dx, double dy) {
		    	verticalPos -= (float) dy * 50;
		    	if (verticalPos < 0)
		    		verticalPos = 0;
		    }
		});
		
		QueenTexture = loadTexture("queen.png");
	}
	
	public void loop() {
		while (!GLFW.glfwWindowShouldClose(window)) {
			GL14.glClearColor(0.192f, 0.180f, 0.168f, 1.0f);
			GL14.glClear(GL14.GL_COLOR_BUFFER_BIT);
			if (alg.solutions != null)
				for (int i = 0, y = 0; i < alg.solutions.size(); y+= alg.size*30+50)
					for (int x = 50; i < alg.solutions.size() && x < wWidth - alg.size*30+50; x+= alg.size*30+50, i++)
						drawBoard(x, y-verticalPos, alg.solutions.get(i));
			GLFW.glfwSwapBuffers(window);
			GLFW.glfwPollEvents();
		}
	}
	
	public void drawBoard(float posX, float posY, boolean[][] board) {
		for (int x = 0; x < alg.size; x++) {
			for (int y = 0; y < alg.size; y++) {
				Color color = new Color(118, 150, 86);
				if (x%2 == (y%2 == 0 ? 1 : 0))
					color = new Color(238, 238, 210);
				drawSprite(posX+x*30,posY+y*30,30,30,color,-1);
				if (board[x][y])
					drawSprite(posX+5+x*30,posY+5+y*30,20,20,null,QueenTexture);
			}
		}
	}
	
	public void drawSprite(float posX, float posY, float sizeX, float sizeY, Color color, int texture) {
		if (texture >= 0) {
			GL11.glEnable(GL11.GL_TEXTURE_2D);
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
			GL11.glBegin(GL11.GL_POLYGON);
			GL11.glColor4f(1, 1, 1, 1);
			GL11.glTexCoord2f(0, 0);
			GL11.glVertex2f(posX, posY);
			GL11.glTexCoord2f(0, 1);
			GL11.glVertex2f(posX, posY+sizeY);
			GL11.glTexCoord2f(1, 1);
			GL11.glVertex2f(posX+sizeX, posY+sizeY);
			GL11.glTexCoord2f(1, 0);
			GL11.glVertex2f(posX+sizeX, posY);
			GL11.glEnd();
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			
		} else if (color != null) {
			float r = color.getRed()/255f;
			float g = color.getGreen()/255f;
			float b = color.getBlue()/255f;
			float a = color.getAlpha()/255f;
			
			GL11.glEnable(GL11.GL_COLOR);
			GL11.glBegin(GL11.GL_POLYGON);
			GL11.glColor4f(r, g, b, a);
			GL11.glVertex2f(posX, posY);
			GL11.glColor4f(r, g, b, a);
			GL11.glVertex2f(posX, posY+sizeY);
			GL11.glColor4f(r, g, b, a);
			GL11.glVertex2f(posX+sizeX, posY+sizeY);
			GL11.glColor4f(r, g, b, a);
			GL11.glVertex2f(posX+sizeX, posY);
			GL11.glEnd();
			
		} else {
			GL11.glBegin(GL11.GL_POLYGON);
			GL11.glVertex2f(posX, posY);
			GL11.glVertex2f(posX, posY+sizeY);
			GL11.glVertex2f(posX+sizeX, posY+sizeY);
			GL11.glVertex2f(posX+sizeX, posY);
			GL11.glEnd();
		}
	}
	
	public int loadTexture(String url) {
		BufferedImage image = null;
		try {
			image = ImageIO.read(new File(url));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		int[] pixels = new int[image.getWidth() * image.getHeight()];
		IntBuffer queenBuffer = BufferUtils.createIntBuffer(pixels.length);
		image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());
		
		queenBuffer.clear();
		for (int i = 0; i < pixels.length; i++) {
			int a = (pixels[i] & 0xff000000) >> 24;
	        int r = (pixels[i] & 0xff0000) >> 16;
	        int g = (pixels[i] & 0xff00) >> 8;
	        int b = (pixels[i] & 0xff);
	        queenBuffer.put(a << 24 | b << 16 | g << 8 | r);
		}
		queenBuffer.flip();
		
		GL11.glEnable(GL11.GL_BLEND);
		GL45.glBlendFunc(GL45.GL_SRC_ALPHA, GL45.GL_ONE_MINUS_SRC_ALPHA);
		
		int texture = GL45.glGenTextures();
		GL45.glBindTexture(GL45.GL_TEXTURE_2D, texture);
		GL45.glTexParameteri(GL45.GL_TEXTURE_2D, GL45.GL_TEXTURE_MIN_FILTER, GL45.GL_LINEAR);
		GL45.glTexParameteri(GL45.GL_TEXTURE_2D, GL45.GL_TEXTURE_MAG_FILTER, GL45.GL_LINEAR);
		
		GL45.glTexImage2D(GL45.GL_TEXTURE_2D, 0, GL45.GL_RGBA, image.getWidth(), image.getHeight(), 0, GL45.GL_RGBA, GL45.GL_UNSIGNED_BYTE, queenBuffer);
		GL45.glBindTexture(GL45.GL_TEXTURE_2D, 0);
		return texture;
	}
	
	public void setAspectRatio(int width,int height) {
        // This is your target virtual resolution for the game, the size you built your game to
        int virtual_width=1920;
        int virtual_height=1080;

        float targetAspectRatio = virtual_width/(float)virtual_height;

        // figure out the largest area that fits in this resolution at the desired aspect ratio
        height = (int)(width / targetAspectRatio + 0.5f);

        if (height > wHeight )
        {
           //It doesn't fit our height, we must switch to pillarbox then
            height = wHeight ;
            width = (int)(height * targetAspectRatio + 0.5f);
        }

        // set up the new viewport centered in the backbuffer
        int vp_x = (wWidth  / 2) - (width / 2);
        int vp_y = (wHeight / 2) - (height/ 2);

        GL45.glViewport(vp_x,vp_y,width,height);
        // Now we use glOrtho
        GL45.glMatrixMode(GL45.GL_PROJECTION);
        GL45.glPushMatrix();
        GL45.glLoadIdentity();
        GL45.glOrtho(0, wWidth, wHeight, 0, -1, 1);
        GL45.glMatrixMode(GL45.GL_MODELVIEW);
        GL45.glPushMatrix();
        GL45.glLoadIdentity();

        // Push in scale transformations
        GL45.glMatrixMode(GL45.GL_MODELVIEW);
        GL45.glPushMatrix();

        //Now to calculate the scale considering the screen size and virtual size
        float scale_x = (float)((float)(wWidth) / (float)virtual_width);
        float scale_y = (float)((float)(wHeight) / (float)virtual_height);
        GL45.glScalef(scale_x, scale_y, 1.0f);
    }
}
