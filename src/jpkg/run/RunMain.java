package jpkg.run;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import jpkg.Config;
import jpkg.build.BuildMain;

public class RunMain {
	public static void run(String[] args) {
		String s = null;

		
		String buildpath;

		// Where to actually do the building
		if(args.length == 0)
			buildpath = ".";
		else
			buildpath = args[0];
		
		File f = new File(buildpath + "/bin/DEPS");
		
		// Get jarlist
		if(!f.exists() || args.length > 1 && args[1] == "--force-build")
			s = BuildMain.run(new String[] {args[0]});
		
		if(s == null) {
			try {
				
				Scanner sc = new Scanner(f);
				StringBuilder sb = new StringBuilder();
				while(sc.hasNext())
					sb.append(sc.nextLine());
				s = sb.toString();
				sc.close();
				
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				return;
			}
		}
		
		File buildfile = new File(buildpath + "/build.jpk");
		
		Scanner sc = null;
		try {
			sc = new Scanner(buildfile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		Config build = new Config();
		build.populate(sc);
		
		String maintype = build.getConfigFor("maintype");
		
		if(maintype == null) {
			System.err.println("No maintype in selection!");
			return;
		}
		
		BuildMain.executeCommand("java -cp \"" + s + "\" " + maintype, new File(buildpath));
	}
}
