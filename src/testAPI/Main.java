package testAPI;

import info.nohoho.weave.Weave;

public class Main {
	public static void main(String[] args) {
		Weave.redefineable("java.util.Scanner", "testAPI.App");
		App app = new App();
		app.run();
	}
}
