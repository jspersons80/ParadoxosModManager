package mod;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javafx.beans.property.SimpleStringProperty;

/**
 * @author SIMON-FINE Thibaut (alias Bisougai)
 *
 */
public class ModList {
	//
	//Fields and Constructors
	//
	private SimpleStringProperty name;
	private SimpleStringProperty description;
	private Languages language;
	private List<Mod> modlist;
	private List<ModConflict> modConflicts;
	
	private static int MOD_NOT_IN_LIST = -1;

	/**
	 * @param name
	 * @param description
	 * @param modlist
	 */
	public ModList(String name, String description, Languages language, List<Mod> modlist) {
		this.name=new SimpleStringProperty(name);
		this.description=new SimpleStringProperty(description);
		this.language=language;
		this.modlist=modlist;
		computeConflicts(modlist);
	}
	
	//
	// Getters and Setters
	//
	/**
	 * @return
	 */
	public String getName() {
		return name.get();
	}

	/**
	 * @param name
	 */
	public void setName(String name) {
		this.name = new SimpleStringProperty(name);
	}

	/**
	 * @return
	 */
	public String getDescription() {
		return description.get();
	}
	
	/**
	 * @param description
	 */
	public void setDescription(String description){
		this.description = new SimpleStringProperty(description);
	}
	
	public Languages getLanguage() {
		return this.language;
	}
	
	public String getLanguageName() {
		return this.language.getName();
	}
	
	public String getLanguageCode() {
		return this.language.getCode();
	}

	public void setLanguage(Languages language) {
		this.language = language;
	}

	/**
	 * @return
	 */
	public List<Mod> getModlist() {
		return modlist;
	}
	
	/**
	 * @param modList
	 */
	public void setModlist(List<Mod> modList) {
		this.modlist=modList;
		computeConflicts(modList);
	}
	
	public List<ModConflict> getModConflicts() {
		return modConflicts;
	}

	//
	//Methods
	//
	/**
	 * @param mod
	 * @return
	 */
	public int isModInList(Mod mod){
		for (int i = 0; i < modlist.size(); i++) {
			Mod one_mod = modlist.get(i);
			if(one_mod.equals(mod))
				return i;
		}
		return MOD_NOT_IN_LIST;
	}
	
	/**
	 * @param mod
	 * @return
	 */
	public boolean addMod(Mod mod){
		if(isModInList(mod) == MOD_NOT_IN_LIST){
			modlist.add(mod);
			addConflicts(mod);
			return true;
		}
		return false;
	}
	
	/**
	 * @param mods
	 */
	public void addAllMod(List<Mod> mods){
		for (Mod one_mod : mods) {
			this.addMod(one_mod);
		}
	}
	
	/**
	 * @param mod
	 * @return
	 */
	public boolean removeMod(Mod mod){
		int index = isModInList(mod);
		if(index!=MOD_NOT_IN_LIST){
			modlist.remove(index);
			removeConflicts(mod);
		}
		return false;
	}
	
	private void computeConflicts(List<Mod> modList) {
		this.modConflicts = new ArrayList<>();
		for (Mod mod1 : modList) {
			Iterator<Mod> it = modList.iterator();
			boolean mod1Found = false;
			while (it.hasNext()) {
				// We skip mod1 and all the previous mods because they have already been seen
				Mod mod2 = it.next();
				if (mod2.equals(mod1)) {
					mod1Found = true;
					continue;
				}
				if (!mod1Found) {
					continue;
				}

				// Search conflict
				addModsConflict(mod1, mod2);
			}
		}
	}

	private void addConflicts(Mod newMod) {
		for (Mod mod1 : modlist) {
			if (!mod1.equals(newMod)) {
				addModsConflict(mod1, newMod);
			}
		}
	}

	private void removeConflicts(Mod removedMod) {
		Iterator<ModConflict> it = modConflicts.iterator();
		while (it.hasNext()) {
			ModConflict conflict = it.next();
			if (conflict.getMod1().equals(removedMod) ||
					conflict.getMod2().equals(removedMod)) {
				it.remove();
			}
		}
	}

	private void addModsConflict(Mod mod1, Mod mod2) {
		ModConflict modConflict = new ModConflict(mod1, mod2);
		for (String modifiedFile : mod1.getModifiedFiles()) {
			if (mod2.getModifiedFiles().contains(modifiedFile)) {
				modConflict.addConflictFile(modifiedFile);
			}
		}
		if (modConflict.getConflictFiles().size() > 0) {
			modConflicts.add(modConflict);
		}
	}
}
