package testAPI;

import info.nohoho.weave.Weave;

public class Main {
	public static void main(String[] args) {
		Weave.redefineable("java.lang.Integer", "testAPI.App");
//		Weave.redefineable("java.lang.Integer");
		AppInterface app = new App();
		app.run();
	}
}
