package jpkg.fetch;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;

import jpkg.Main;
import jpkg.build.BuildMain;
import jpkg.io.SimpleIO;
import static jpkg.sys.ExecCmd.executeCommand;

public class FetchMain {
	
	public static HashMap<String, String> fetched = new HashMap<>();
	
	public static String run(String[] args) {
		// Someone didn't read the man pages
		if(args.length == 0) {
			System.err.print("Expected module to fetch as next argument!");
			return null;
		}
		
		String hosts = Main.mainconfig.getConfigFor("hosts");
		
		String[] branchsplit = args[0].split("#");
		
		String branch = "master";
		
		// Did user specify a branch? If so, then actually listen to them
		if(branchsplit.length == 2)
			branch = branchsplit[1];
		
		String[] hostlist = getHosts(branchsplit[0], hosts);
		String[] w = branchsplit[0].split("/");
		
		// Directory of ${repo-dir}/${user}
		File repodir = new File(Main.mainconfig.getConfigFor("repository-directory")
				+ ((w.length == 1) ? "jpkg/" : w[0]));
		
		String packagename = ((w.length == 1) ? "jpkg/" : w[0]) + w[w.length - 1];
		
		if(fetched.containsKey(packagename)) {
			System.out.println("Package " + packagename + " already fetched, skipping...");
			return fetched.get(packagename);
		}
		
		// If repository doesn't exist, make it
		if(!repodir.exists())
			repodir.mkdirs();
		
		File k;
		
		// Directory of repo if it already exists (the directory ${repo-dir}/${user}/${repo})
		String repo = repodir.getAbsolutePath() + "/" + w[w.length - 1];
		
		
		if((k = new File(repo)).exists()) {		// If repository already exists, just refresh it
			executeCommand("git pull --all", k);

		} else for(String host : hostlist) 		// Otherwise, clone it
			if(executeCommand("git clone " + host, repodir) == 0) break;

		executeCommand("git checkout " + branch, k);	// Checkout right branch because it's probably needed

		new File(repo + "/bin").mkdir();

		// Make a /bin/BRANCH
		System.out.println("Making /bin/BRANCH : " + branch);
		SimpleIO.writeSwallowed(branch, new File(repo + "/bin/BRANCH"), true);

		// Finally, build the repo
		String repo_jar = BuildMain.run(new String[] {repo});

		fetched.put(packagename, repo_jar);
		
		return repo_jar;
	}
	
	public static String[] getHosts(String arg, String hosts) {
		String[] branchsplit = arg.split("#");
		String[] tofetch = branchsplit[0].split("/");
		

		
		// Someone didn't read the URI specifications...
		if(tofetch.length > 2) {
			System.err.println("Invalid fetch target " + arg + "!");
			throw new RuntimeException();
		}
		
		String user = null;
		String proj = null;
		
		if(tofetch.length == 1)
			user = "jpkg";
		else
			user = tofetch[0];
		
		proj = tofetch[tofetch.length - 1];
		
		hosts = hosts.replace("${user}", user)
				.replace("${proj}", proj);
		
		
		return hosts.split(";");
	}
}
