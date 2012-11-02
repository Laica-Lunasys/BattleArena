package mc.alk.arena.objects.victoryconditions;

import java.lang.reflect.Constructor;
import java.util.LinkedHashMap;

import mc.alk.arena.competition.match.Match;
import mc.alk.arena.controllers.MethodController;
import mc.alk.arena.util.CaseInsensitiveMap;

import org.bukkit.plugin.Plugin;


public class VictoryType {
	static public LinkedHashMap<String,Class<?>> classes = new LinkedHashMap<String,Class<?>>();
	static public CaseInsensitiveMap<VictoryType> types = new CaseInsensitiveMap<VictoryType>();

	public static VictoryType DEFAULT = null;
	static int count =0;
	final String name;
	final Plugin ownerPlugin;
	final int id = count++;

	private VictoryType(final String name,final Plugin plugin){
		this.name = name.toUpperCase();
		this.ownerPlugin = plugin;

		if (!types.containsKey(name))
			types.put(name,this);
		if (name.equalsIgnoreCase("LastManStanding")) DEFAULT = this;
	}

	public static VictoryType fromString(final String type) {
		if (type==null)
			return null;
		return types.get(type);
	}

	public static String getValidList() {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (VictoryType at: types.values()){
			if (!first) sb.append(", ");
			first = false;
			sb.append(at.name);
		}
		return sb.toString();
	}
	@Override
	public String toString(){
		return name;
	}
	public String getName() {
		return name;
	}
	public static VictoryCondition createVictoryCondition(Match match) {
		VictoryType vt = match.getParams().getVictoryType();
		Class<?> vcClass = classes.get(vt.getName());
		if (vcClass == null)
			return null;
		Class<?>[] args = {Match.class};
		try {
			Constructor<?> constructor = vcClass.getConstructor(args);
			VictoryCondition newVC = (VictoryCondition) constructor.newInstance(match);
			return newVC;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void register(Class<? extends VictoryCondition> vc, Plugin plugin) {
		final String vcName = vc.getSimpleName().toUpperCase();
		if (!classes.containsKey(vcName))
			classes.put(vcName, vc);
		if (!types.containsKey(vcName)){
			new VictoryType(vcName,plugin);
		}
		/// Register our methods
		MethodController.addMethods(vc, vc.getMethods());
	}

	public static boolean registered(VictoryCondition vc){
		final String vcName = vc.getClass().getSimpleName().toUpperCase();
		return classes.containsKey(vcName) && types.containsKey(vcName);
	}
	public int ordinal() {
		return id;
	}
}
