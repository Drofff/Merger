package drofff.merger;

import java.lang.reflect.Field;
import java.util.Objects;
import java.util.Optional;

import drofff.merger.exception.MergerException;
import drofff.merger.policy.MergeConflictPolicy;

/**
 * Merger class. Provides effective merge of changes applied to the same object.
 * For merger to work - apply new changes on object returned from {@link Mergeable#clone()}
 * or use {@link History#save(Mergeable)} to save current state before applying any changes
 * @param <T>
 */
public class Merger<T extends Mergeable> {

	private T src;
	private T dst;
	private MergeConflictPolicy mergeConflictPolicy = MergeConflictPolicy.SRC_VALUE;

	public Merger(T src, T dst) {
		this.src = src;
		this.dst = dst;
		validateSrcAndDstNotNull();
		validateOrigins();
	}

	private void validateSrcAndDstNotNull() {
		if(src == null) {
			throw new MergerException("Provided src object is null");
		}
		if(dst == null) {
			throw new MergerException("Provided dst object is null");
		}
	}

	private void validateOrigins() {
		if(Objects.nonNull(src.getId()) && Objects.nonNull(dst.getId()) &&
				!src.getId().equals(dst.getId())) {
			throw new MergerException("Objects have different origins");
		}
	}

	public T merge() {
		try {
			mergeFields();
		} catch(IllegalAccessException e) {
			throw new MergerException("Merger error: " + e.getMessage());
		}
		return dst;
	}

	private void mergeFields() throws IllegalAccessException {
		Field[] fields = src.getClass().getDeclaredFields();
		for(Field field : fields) {
			mergeField(field);
		}
	}

	private void mergeField(Field field) throws IllegalAccessException {
		field.setAccessible(true);
		Optional<Object> latestSnapshotOptional = History.getLatestSnapshot(src.getId());
		if(!latestSnapshotOptional.isPresent()) {
			mergeFieldWithoutHistory(field);
		} else {
			Object latestSnapshot = latestSnapshotOptional.get();
			validateSnapshotCompatible(latestSnapshot);
			mergeFieldWithHistory(field, (T) latestSnapshot);
		}
	}

	private void validateSnapshotCompatible(Object snapshot) {
		if(!src.getClass().isAssignableFrom(snapshot.getClass())) {
			throw new MergerException("Not compatible with class in history");
		}
	}

	private void mergeFieldWithoutHistory(Field field) throws IllegalAccessException {
		Object srcFieldValue = field.get(src);
		Object dstFieldValue = field.get(dst);
		if(srcFieldValue != null && dstFieldValue == null) {
			field.set(dst, srcFieldValue);
		} else if(srcFieldValue != null) {
			resolveConflictOnField(field);
		}
	}

	private void mergeFieldWithHistory(Field field, T latestSnapshot) throws IllegalAccessException {
		Object srcFieldValue = field.get(src);
		Object dstFieldValue = field.get(dst);
		Object latestSnapshotFieldValue = field.get(latestSnapshot);
		if(Objects.nonNull(srcFieldValue) && !srcFieldValue.equals(latestSnapshotFieldValue)) {
			if(dstFieldValue.equals(latestSnapshotFieldValue)) {
				field.set(dst, srcFieldValue);
			} else {
				resolveConflictOnField(field);
			}
		}
	}

	private void resolveConflictOnField(Field field) throws IllegalAccessException {
		if(MergeConflictPolicy.SRC_VALUE.equals(mergeConflictPolicy)) {
			Object value = field.get(src);
			field.set(dst, value);
		}
	}

	public MergeConflictPolicy getMergeConflictPolicy() {
		return mergeConflictPolicy;
	}

	public void setMergeConflictPolicy(MergeConflictPolicy mergeConflictPolicy) {
		this.mergeConflictPolicy = mergeConflictPolicy;
	}

}
