package novus.gdx.game.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import novus.gdx.game.SwordGame;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		
		config.title = "Kanmon Engine";
		config.width = 832;
		config.height = 640;
		
		new LwjglApplication(new SwordGame(), config);
	}
}
