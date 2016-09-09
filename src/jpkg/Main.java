package jpkg;

import java.util.Arrays;

import jpkg.build.BuildMain;
import jpkg.fetch.FetchMain;

public final class Main {
	
	public static void main(String[] args) {
		if(args.length < 1) {
			System.err.println("Expected at least one argument!");
			return;
		}
		
		String[] args_sub = Arrays.copyOfRange(args, 1, args.length);
		
		switch(args[1]) {
		
		case "build":
			BuildMain.run(args_sub);
			break;
			
		case "fetch":
			FetchMain.run(args_sub);
			break;
		
		}
	}

}
