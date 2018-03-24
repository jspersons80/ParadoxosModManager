package settings;

import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import org.jdom2.*;
import org.jdom2.input.*;
import org.jdom2.output.*;

import application.ModManager;
import mod.Languages;
import mod.Mod;
import mod.ModList;

/**
 * @author SIMON-FINE Thibaut (alias Bisougai)
 *
 */
public class MyXML {
	private static final String USER_LISTS = "userlists";
	private static final String EXPORTED_LIST = "exportedlist";
	private static final String GAME_ID = "gameID";
	private static final String LIST = "list";
	private static final String NAME = "name";
	private static final String DESCR = "descr";
	private static final String LANG = "lang";
	private static final String MOD = "mod";
	private static final String ID = "id";
	private static final String FILE_NAME = "fileName";
	private static final String REMOTE_ID = "remoteID";
	private static final String MOD_NAME = "modName";
	
	private static final String APP_SETTINGS = "appsettings";
	private static final String GAME = "game";
	private static final String ATTR_STEAMID = "steamid";
	private static final String ATTR_VALUE = "value";
	
	private static Element root;
	private static org.jdom2.Document document;
	private static Element root_exported;
	private static org.jdom2.Document document_exported;
	private String file;
	   
	//public MyXML(){}
	
	/**
	 * @param file
	 * @throws IOException 
	 * @throws JDOMException 
	 * @throws Exception
	 */
	public void readFile(String file) throws JDOMException, IOException {
		SAXBuilder sxb = new SAXBuilder();
		File xml = new File(file);
		if(xml.exists()){
			document = sxb.build(xml);
			root = document.getRootElement();
		}
		else{
			root = new Element(USER_LISTS);
			document = new Document(root);
		}
		
		//Init for export lists
		root_exported = new Element(EXPORTED_LIST);
		root_exported.setAttribute(GAME_ID, ModManager.STEAM_ID.toString());
		document_exported = new Document(root_exported);
		
		this.file = file;
	}
	
	/**
	 * @param file
	 * @throws Exception
	 */
	public void readSettingFile(String file) throws Exception{
		SAXBuilder sxb = new SAXBuilder();
		File xml = new File(file);
		if(xml.exists()){
			document = sxb.build(xml);
			root = document.getRootElement();
		}
		else{
			root = new Element(APP_SETTINGS);
			document = new Document(root);
		}
		
		this.file = file;
	}
	
	/**
	 * @throws Exception
	 */
	public void saveFile() throws Exception{
		XMLOutputter sortie = new XMLOutputter(Format.getPrettyFormat());
		sortie.output(document, new FileOutputStream(file));
	}
	
	
	/**
	 * @return
	 */
	public ArrayList<ModList> getSavedList(){
		ArrayList<ModList> userLists = new ArrayList<ModList>();
		List<Element> modLists = root.getChildren(LIST);
		Iterator<Element> i = modLists.iterator();
		while(i.hasNext()){
			ArrayList<Mod> listMods = new ArrayList<Mod>();
			
			Element oneListElement = (Element) i.next();
			String listName = oneListElement.getAttribute(NAME).getValue();
			String listDescr = oneListElement.getChild(DESCR).getText();
			String listLang;
			try {
				listLang = oneListElement.getChild(LANG).getText();
			} catch (RuntimeException e) {
				listLang = null;
			}
			List<Element> modsElements = oneListElement.getChildren(MOD);
			for (Element modElement : modsElements) {
				List<Attribute> modElementAttr = modElement.getAttributes();
				String fileName="",remoteFileId=null;
				for (Attribute attribute : modElementAttr) {
					switch (attribute.getName()) {
					case ID:
					case FILE_NAME:
						fileName = attribute.getValue();
						break;
					
					case REMOTE_ID:
						remoteFileId = attribute.getValue();
						break;
					
					default:
						break;
					}
				}
					
				Mod oneMod = new Mod(fileName, remoteFileId);
				listMods.add(oneMod);
			}
			
			ModList oneList = new ModList(listName, listDescr,
					Languages.getLanguage(listLang),listMods);
			userLists.add(oneList);
		}
		return userLists;
	}
	
	/**
	 * @param listName
	 * @throws Exception
	 */
	public void removeList(String listName) throws Exception{
		List<Element> modLists = root.getChildren(LIST);
		Iterator<Element> iE = modLists.iterator();
		while(iE.hasNext()){
			Element oneListElement = (Element) iE.next();
			String listElementName = oneListElement.getAttribute(NAME).getValue();
			if(listElementName.equals(listName)){
				root.removeContent(oneListElement);
				break;
			}
		}
		this.saveFile();
	}
	
	/**
	 * @param list
	 * @throws Exception
	 */
	public void modifyList(ModList list) throws Exception { modifyList(list, null); }
			
	/**
	 * @param list
	 * @param listName
	 * @throws Exception
	 */
	public void modifyList(ModList list, String listName) throws Exception{
		Element oneListElement,listDescrElement,listLangElement,listModElement;
		ArrayList<Mod> listMods;
		if(listName!=null){
			List<Element> modLists = root.getChildren(LIST);
			Iterator<Element> i = modLists.iterator();
			while(i.hasNext()){
				oneListElement = (Element) i.next();
				String listElementName = oneListElement.getAttribute(NAME).getValue();
				if(listElementName.equals(listName)){
					oneListElement.setAttribute(NAME, list.getName());
					
					listDescrElement = oneListElement.getChild(DESCR);
					listDescrElement.setText(list.getDescription());
					
					
					listLangElement = oneListElement.getChild(LANG);
					if (listLangElement != null) {
						listLangElement.setText(list.getLanguageName());
					} else {
						listLangElement = new Element(LANG);
						listLangElement.setText(list.getLanguageName());
						oneListElement.addContent(listLangElement);
					}
					
					oneListElement.removeChildren(MOD);
					listMods = list.getModlist();
					for (Mod mod : listMods) {
						listModElement = new Element(MOD);
						listModElement.setAttribute(MOD_NAME, mod.getName());
						listModElement.setAttribute(FILE_NAME, mod.getFileName());
						listModElement.setAttribute(REMOTE_ID, mod.getRemoteFileID());
						oneListElement.addContent(listModElement);
					}
					break;
				}
			}
		}
		else{
			oneListElement = new Element(LIST);
			oneListElement.setAttribute(NAME, list.getName());
			root.addContent(oneListElement);
			
			listDescrElement = new Element(DESCR);
			listDescrElement.setText(list.getDescription());
			oneListElement.addContent(listDescrElement);
			
			listLangElement = new Element(LANG);
			listLangElement.setText(list.getLanguageName());
			oneListElement.addContent(listLangElement);
			
			listMods = list.getModlist();
			for (Mod mod : listMods) {
				listModElement = new Element(MOD);
				listModElement.setAttribute(FILE_NAME, mod.getFileName());
				listModElement.setAttribute(REMOTE_ID, mod.getRemoteFileID());
				oneListElement.addContent(listModElement);
			}
		}
		this.saveFile();
	}
	
	/**
	 * @param listName
	 * @throws Exception
	 */
	public void exportList(String listName) throws Exception{
		List<Element> modLists = root.getChildren(LIST);
		Iterator<Element> iE_export = modLists.iterator();
		while(iE_export.hasNext()){
			Element oneListElement = (Element) iE_export.next();
			String listElementName = oneListElement.getAttribute(NAME).getValue();
			if(listElementName.equals(listName)){
				root_exported.addContent(oneListElement.detach());
				XMLOutputter sortie = new XMLOutputter(Format.getPrettyFormat());
				String exportFileName = "Export_"+ModManager.GAME+"_"+listName+".xml";
				sortie.output(document_exported, new FileOutputStream(ModManager.xmlDir+File.separator+exportFileName));
				break;
			}
		}
	}
	
	/**
	 * @param xml
	 * @return
	 * @throws Exception
	 */
	public String importList(String xml) throws Exception{
		SAXBuilder sxb = new SAXBuilder();
		Document importDocument = sxb.build(xml);
		Element importRoot = importDocument.getRootElement();
		if(importRoot.getAttribute(GAME_ID).getValue().equals(ModManager.STEAM_ID.toString())){
			List<Element> modLists = importRoot.getChildren(LIST);
			Iterator<Element> i = modLists.iterator();
			while(i.hasNext()){
				ArrayList<Mod> listMods = new ArrayList<Mod>();
				
				Element oneListElement = (Element) i.next();
				String listName = oneListElement.getAttribute(NAME).getValue();
				String listDescr = oneListElement.getChild(DESCR).getText();
				String listLang = oneListElement.getChild(LANG).getText();
				List<Element> modsElements = oneListElement.getChildren(MOD);
				for (Element modElement : modsElements) {
					List<Attribute> modElementAttr = modElement.getAttributes();
					String fileName="",remoteFileId=null;
					for (Attribute attribute : modElementAttr) {
						switch (attribute.getName()) {
						case ID:
						case FILE_NAME:
							fileName = attribute.getValue();
							break;
						
						case REMOTE_ID:
							remoteFileId = attribute.getValue();
							break;
						
						default:
							break;
						}
					}
						
					Mod oneMod = new Mod(fileName, remoteFileId);
					listMods.add(oneMod);
				}
				
				ModList oneList = new ModList("[Imported]"+listName, listDescr,
						Languages.getLanguage(listLang), listMods);
				modifyList(oneList);
			}
			return "Import done.";
		}
		return "Import procedure aborted, this list is not for the current game !";
	}
	
	/**
	 * @throws DataConversionException 
	 * 
	 */
	public HashMap<String, String> getGameSettings(Integer gameID) throws DataConversionException{
		HashMap<String, String> params = new HashMap<>();
		List<Element> gameLists = root.getChildren(GAME);
		Iterator<Element> i = gameLists.iterator();
		while(i.hasNext()){
			Element oneListElement = (Element) i.next();
			
			if (gameID==oneListElement.getAttribute(ATTR_STEAMID).getIntValue()) {
				List<Element> gameParamsElements = oneListElement.getChildren();
				for (Element element : gameParamsElements) {
					params.put(element.getName(), element.getAttributeValue(ATTR_VALUE));
				}
				break;
			}
		}
		return params;
	}
	
	/**
	 * @throws DataConversionException 
	 * 
	 */
	public String getOneGameSetting(Integer gameID, String attrName) throws DataConversionException{
		List<Element> gameLists = root.getChildren(GAME);
		Iterator<Element> i = gameLists.iterator();
		while(i.hasNext()){
			Element oneListElement = (Element) i.next();
			
			if (gameID==oneListElement.getAttribute(ATTR_STEAMID).getIntValue()) {
				List<Element> gameParamsElements = oneListElement.getChildren();
				for (Element element : gameParamsElements) {
					if(element.getName().equals(attrName)){
						String value = element.getAttributeValue(ATTR_VALUE);
						return value;
					}
				}
			}
		}
		return null;
	}
	
	public void modifyGameSettings(Integer gameID, String attrName, String value) throws Exception {
		List<Element> gameLists = root.getChildren(GAME);
		Iterator<Element> i = gameLists.iterator();
		Boolean flag_nogame=true, flag_noattrparam=true;
		while(i.hasNext()){
			Element oneListElement = (Element) i.next();
			
			if(gameID == oneListElement.getAttribute(ATTR_STEAMID).getIntValue()) {
				flag_nogame=false;
				List<Element> gameParamsElements = oneListElement.getChildren();
				Iterator<Element> j = gameParamsElements.iterator();
				
				while(j.hasNext()) {
					Element oneParamElement = (Element) j.next();
					
					if(attrName == oneParamElement.getName()){
						flag_noattrparam=false;
						oneParamElement.setAttribute(ATTR_VALUE, value);
						
						break;
					}
				}
				
				if(flag_noattrparam){
					Element newParamElement = new Element(attrName);
					newParamElement.setAttribute(ATTR_VALUE, value);
					oneListElement.addContent(newParamElement);
				}
				
				break;
			}
		}
		
		if(flag_nogame) {
			Element newGameElement = new Element(GAME);
			newGameElement.setAttribute(ATTR_STEAMID,gameID.toString());
			root.addContent(newGameElement);
			Element newParamElement = new Element(attrName);
			newParamElement.setAttribute(ATTR_VALUE,value);
			newGameElement.addContent(newParamElement);
		}
		
		saveFile();
	}
}