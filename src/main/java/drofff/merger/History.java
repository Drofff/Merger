package drofff.merger;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import drofff.merger.exception.MergerException;

public class History {

	private History() {}

	private static Map<String, Object> fieldsValues = new HashMap<>();

	public static void save(Mergeable mergeable) {
		if(mergeable.getId() == null) {
			throw new MergerException("Mergeable object id is null");
		}
		fieldsValues.put(mergeable.getId(), mergeable);
	}

	public static Optional<Object> getLatestSnapshot(String objectId) {
		if(fieldsValues.containsKey(objectId)) {
			return Optional.of(fieldsValues.get(objectId));
		}
		return Optional.empty();
	}

}