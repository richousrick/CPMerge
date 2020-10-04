package richousrick.cpmerge.ref;

import richousrick.cpmerge.merge.MergeThread;
import richousrick.cpmerge.merge.MergedFunction;
import richousrick.cpmerge.parse.PluginInterface;

import java.io.File;
import java.util.ArrayList;

/**
 * TODO Annotate class
 *
 * @author Rikkey Paal
 */
public abstract class Language<D> {

	public abstract PluginInterface<D> generatePluginInstance();

	public ArrayList<MergedFunction<D>> updatedFunctions;

	public MergeThread<D> generateMergeThreadInstance(File f, double minPer) {
		return new MergeThread<>(f, generatePluginInstance(), minPer, this);
	}

	public ArrayList<MergedFunction<D>> getUpdatedFunctions() {
		if (updatedFunctions == null) {
			ArrayList<MergedFunction<?>> func = Helper.getUpdatedFunctions();
			try {
				updatedFunctions = new ArrayList<>();
				for (MergedFunction<?> r : func) {
					updatedFunctions.add((MergedFunction<D>) r);
				}
			} catch (ClassCastException e) {
				Helper.exitProgram(e);
			}
		}
		return updatedFunctions;
	}

}
