package mod;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import application.ModManager;
import debug.ErrorPrint;
import javafx.beans.property.SimpleStringProperty;

/**
 * @author SIMON-FINE Thibaut (alias Bisougai)
 *
 */
public class Mod {
	private SimpleStringProperty fileName;
	private SimpleStringProperty name;
	private SimpleStringProperty versionCompatible;
	private SimpleStringProperty remoteFileID;
	private SimpleStringProperty steamPath;
	private SimpleStringProperty dirPath;
	private boolean missing;
	private Set<String> modifiedFiles = new HashSet<>();
	
	/**
	 * @param filename
	 */
	public Mod(String filename){
		this(filename,null);
	}
	
	/**
	 * @param filename
	 * @param remoteFileID
	 */
	public Mod(String filename, String remoteFileID) {
		try{
			Integer.parseInt(filename);
			this.fileName = new SimpleStringProperty("ugc_"+filename+".mod");
		} catch(Exception e) {
			this.fileName = new SimpleStringProperty(filename);
		}
		
		if(remoteFileID!=null){
			this.remoteFileID = new SimpleStringProperty(remoteFileID);
			this.steamPath = new SimpleStringProperty("https://steamcommunity.com/sharedfiles/filedetails/?id="+this.remoteFileID.get());
		}else{
			this.remoteFileID = new SimpleStringProperty("");
			this.steamPath = new SimpleStringProperty("No remote ID found");
		}
		
		this.versionCompatible = new SimpleStringProperty("?");
		this.name = this.fileName;
		
		try {
			readFileMod();
			this.missing = false;
			setModifiedFiles();
		} catch (IOException e) {
			this.missing = true;
			this.name = new SimpleStringProperty("MOD MISSING");
			this.versionCompatible = new SimpleStringProperty("");
			ErrorPrint.printError("Unable to open "+ModManager.PATH+"mod"+File.separator+filename+" ! File is missing or corrupted !");
			//e.printStackTrace();
		}
	}
	
	/**
	 * @throws IOException
	 */
	private void readFileMod() throws IOException {
		String sep = File.separator;
		Pattern p = Pattern.compile("\\\".*?\\\"");
		Matcher m;
		
		Path pth = Paths.get(ModManager.PATH+"mod"+sep+fileName.get());
		List<String> lines = Files.readAllLines(pth);
		for (String line : lines) {
			String lineWFirstChar = (line.length() > 0) ? line.substring(1, line.length()) : "";
			if(line.matches("\\s*name\\s*=.*") || lineWFirstChar.matches("\\s*name\\s*=.*")) {
				m = p.matcher(line);
				if(m.find())
				    name = new SimpleStringProperty((String) m.group().subSequence(1, m.group().length()-1));
			}else if (line.matches("\\s*path\\s*=.*") || lineWFirstChar.matches("\\s*path\\s*=.*") ||
					line.matches("\\s*archive\\s*=.*") || lineWFirstChar.matches("\\s*archive\\s*=.*")) {
				m = p.matcher(line);
				if(m.find())
				    dirPath = new SimpleStringProperty((String) m.group().subSequence(1, m.group().length()-1));
			}else if (line.matches("\\s*supported_version\\s*=.*") || lineWFirstChar.matches("\\s*supported_version\\s*=.*")) {
				m = p.matcher(line);
				if(m.find())
				    versionCompatible = new SimpleStringProperty((String) m.group().subSequence(1, m.group().length()-1));
			}else if (line.matches("\\s*remote_file_id\\s*=.*") || lineWFirstChar.matches("\\s*remote_file_id\\s*=.*")){
				m = p.matcher(line);
				if(m.find()){
				    remoteFileID = new SimpleStringProperty((String) m.group().subSequence(1, m.group().length()-1));
				    this.steamPath = new SimpleStringProperty("https://steamcommunity.com/sharedfiles/filedetails/?id="+this.remoteFileID.get());
				}
			}
		}
		
	}
	
	private void setModifiedFiles()
	{
		if ((dirPath == null) || dirPath.length().get() < 2)
		{
			return;
		}
		if (dirPath.get().charAt(1) != ':')
		{
			// The path is relataive
			dirPath.set(ModManager.PATH + dirPath.get());
		}
		if (dirPath.get().endsWith(".zip"))
		{
			// TODO
		}
		else
		{
			addModifiedFiles(new File(dirPath.get()), "");
		}
	}
	
	private void addModifiedFiles(File directory, String relativeDirPath)
	{
		File[] files = directory.listFiles();
		for (File file: files)
		{
			if (file.isDirectory())
			{
				String newRelativeDirPath = "".equals(relativeDirPath) ? file.getName() :
					relativeDirPath + File.separator + file.getName();
				addModifiedFiles(file, newRelativeDirPath);
			}
			else if (!file.getPath().endsWith(".mod"))
			{
				modifiedFiles.add(relativeDirPath + File.separator + file.getName());
			}
		}
	}
	
	
	
	//
	// Getters and Setters
	//
	
	/**
	 * @return
	 */
	public String getFileName(){
		return fileName.get();
	}
	
	/**
	 * @return
	 */
	public String getName(){
		return name.get();
	}
	
	/**
	 * @return
	 */
	public String getVersionCompatible(){
		return versionCompatible.get();
	}
	
	/**
	 * @return
	 */
	public String getRemoteFileID(){
		return remoteFileID.get();
	}
	
	/**
	 * @return
	 */
	public String getSteamPath(){
		return steamPath.get();
	}
	
	//
	// Methods
	//
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Mod mod = (Mod) obj;
		return (fileName.get().equals(mod.getFileName()));
	}
	
	/**
	 * @return
	 */
	public boolean isMissing(){
		return missing;
	}

	public Set<String> getModifiedFiles() {
		return modifiedFiles;
	}
}