package mc.alk.arena.objects.arenas;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mc.alk.arena.controllers.MethodController;
import mc.alk.arena.objects.ArenaParams;
import mc.alk.arena.util.CaseInsensitiveMap;

import org.bukkit.plugin.Plugin;

public class ArenaType implements Comparable<ArenaType>{
	static public CaseInsensitiveMap<Class<? extends Arena>> classes = new CaseInsensitiveMap<Class<? extends Arena>>();
	static public CaseInsensitiveMap<ArenaType> types = new CaseInsensitiveMap<ArenaType>();

	public static ArenaType ANY = null;
	public static ArenaType VERSUS = null;
	static int count = 0;

	final String name;
	final Plugin ownerPlugin;
	final int id = count++;
	Set<ArenaType> compatibleTypes = null;

	private ArenaType(final String name,Plugin plugin){
		this.name = name;
		this.ownerPlugin = plugin;
		if (!types.containsKey(name))
			types.put(name,this);

		if (name.equalsIgnoreCase("ANY")) ANY = this;
		else if (name.equalsIgnoreCase("VERSUS")) { VERSUS = this;}
	}
	@Override
	public String toString(){
		return name;
	}

	public boolean matches(ArenaType arenaType) {
		if (this == ANY || arenaType == ANY) return true;
		if (this == arenaType)
			return true;
		return (compatibleTypes==null) ? false : compatibleTypes.contains(arenaType);
	}

	public Collection<String> getInvalidMatchReasons(ArenaType arenaType) {
		List<String> reasons = new ArrayList<String>();
		if (this != arenaType && this!=ANY && arenaType != ANY) reasons.add("Arena type is " + this +". You requested " + arenaType);
		return reasons;
	}

	public String toPrettyString(int min, int max) {
		if (this == ArenaType.VERSUS){
			return min +"v" + max;
		} else {
			return toString();
		}
	}

	public String getCompatibleTypes(){
		if (compatibleTypes == null || compatibleTypes.isEmpty())
			return name;
		StringBuilder sb = new StringBuilder(name);
		for (ArenaType at: compatibleTypes){
			sb.append(", " +at.name);}
		return sb.toString();
	}

	public int ordinal() {
		return id;
	}

	public String getName() {
		return name;
	}

	private void addCompatibleType(ArenaType at) {
		if (compatibleTypes == null){
			compatibleTypes = new HashSet<ArenaType>();
		}
		compatibleTypes.add(at);
	}

	public int compareTo(ArenaType arg0) {
		return this.name.compareTo(arg0.name);
	}

	@Override
	public boolean equals(Object obj){
		if(this == obj)
			return true;
		if((obj == null) || (obj.getClass() != this.getClass()))
			return false;
		return this.name.equals( ((ArenaType)obj).name);
	}
	@Override
	public int hashCode(){
		return name.hashCode();
	}

	public Plugin getPlugin() {
		return ownerPlugin;
	}

	public static ArenaType fromString(final String arenatype) {
		if (arenatype==null)
			return null;
		return types.get(arenatype.toUpperCase());
	}


	public static String getValidList() {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (ArenaType at: types.values()){
			if (!first) sb.append(", ");
			first = false;
			sb.append(at.name);
		}
		return sb.toString();
	}

	public static ArenaType register(String arenaType, Class<? extends Arena> c, Plugin plugin) {
		final String uarenaType = arenaType.toUpperCase();
		if (!classes.containsKey(uarenaType))
			classes.put(uarenaType, c);
		if (!types.containsKey(uarenaType)){
			new ArenaType(arenaType,plugin);
		}
		MethodController.addMethods(c,c.getMethods());
		return types.get(uarenaType);
	}

	/**
	 * Create an arena from a name and parameters
	 * This will not load persistable objects, which must be done by the caller
	 * @param arenaName
	 * @param arenaParams
	 * @return
	 */
	public static Arena createArena(String arenaName, ArenaParams arenaParams) {
		ArenaType arenaType = arenaParams.getType();
		Arena newArena = createArena(arenaType);
		if (newArena == null)
			return null;
		newArena.setName(arenaName);
		newArena.setParameters(arenaParams);
		return newArena;
	}

	private static Arena createArena(ArenaType arenaType){
		Class<?> arenaClass = classes.get(arenaType.name);
		if (arenaClass == null)
			return null;
		Class<?>[] args = {};
		try {
			Constructor<?> constructor = arenaClass.getConstructor(args);
			return (Arena) constructor.newInstance((Object[])args);
		} catch (NoSuchMethodException e){
			System.err.println("If you have custom constructors for your class you must also have a public default constructor");
			System.err.println("Add the following line to your Arena Class '" + arenaClass.getSimpleName()+".java'");
			System.err.println("public " + arenaClass.getSimpleName()+"(){}");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void addCompatibleTypes(String type1, String type2) {
		ArenaType at1 = fromString(type1);
		ArenaType at2 = fromString(type2);
		if (at1 == null || at2==null)
			return;
		at1.addCompatibleType(at2);
		at2.addCompatibleType(at1);
	}


	public static Collection<ArenaType> getTypes() {
		return types.values();
	}

	public static Collection<ArenaType> getTypes(Plugin plugin) {
		List<ArenaType> result = new ArrayList<ArenaType>();
		for (ArenaType type: types.values()){
			if (type.getPlugin().equals(plugin)){
				result.add(type);
			}
		}
		return result;
	}

	public static Class<? extends Arena> getArenaClass(ArenaType arenaType){
		return classes.get(arenaType.getName());
	}



}
