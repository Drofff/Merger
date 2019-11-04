package merger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import drofff.merger.History;
import drofff.merger.Mergeable;
import drofff.merger.Merger;
import drofff.merger.exception.MergerException;
import drofff.merger.policy.MergeConflictPolicy;

public class MergerTest {

	private Person person;

	private ExpectedException expectedException = ExpectedException.none();

	private static final String NEW_CITY = "New York";

	@Before
	public void init() {
		person = new Person();
		person.setId(UUID.randomUUID().toString());
		person.setName("Michael");
		person.setAge(19);
		person.setCity("Chernivtsi");
		History.save(person);
	}

	@Test
	public void simpleMergeTest() {
		final Integer NEW_AGE = 20;

		Person changedCity = new Person(person);
		changedCity.setCity(NEW_CITY);
		Person changedAge = new Person(person);
		changedAge.setAge(NEW_AGE);

		Merger<Person> merger = new Merger<>(changedCity, changedAge);
		Person mergedPerson = merger.merge();

		assertEquals(NEW_CITY, mergedPerson.getCity());
		assertEquals(NEW_AGE, mergedPerson.getAge());
	}

	@Test
	public void simpleMergeConflictTest() {
		final String NEW_SRC_CITY = "Boston";

		Person srcPerson = new Person(person);
		srcPerson.setCity(NEW_SRC_CITY);

		Person dstPerson = new Person(person);
		dstPerson.setCity(NEW_CITY);

		Merger<Person> merger = new Merger<>(srcPerson, dstPerson);
		Person mergedPerson = merger.merge();

		assertEquals(NEW_SRC_CITY, mergedPerson.getCity());
	}

	@Test
	public void mergeDstFieldNullWithoutHistory() {
		final Integer AGE = 12;
		final String ID = UUID.randomUUID().toString();

		Person srcPerson = new Person(person);
		Person dstPerson = new Person(person);

		srcPerson.setId(ID);
		srcPerson.setAge(AGE);

		dstPerson.setId(ID);
		dstPerson.setAge(null);

		Merger<Person> merger = new Merger<>(srcPerson, dstPerson);
		Person mergedPerson = merger.merge();

		assertEquals(AGE, mergedPerson.getAge());
	}

	@Test
	public void mergeFieldsWithoutHistorySrcConflictPolicy() {
		Person mergedPerson = mergeWithPolicyAndAgeAndCityTest(MergeConflictPolicy.SRC_VALUE, person.getAge(), NEW_CITY);
		assertEquals(NEW_CITY, mergedPerson.getCity());
		assertEquals(person.getAge(), mergedPerson.getAge());
	}

	@Test
	public void mergeFieldsWithoutHistoryDstConflictPolicy() {
		final int NEW_AGE = 19;
		Person mergedPerson = mergeWithPolicyAndAgeAndCityTest(MergeConflictPolicy.DST_VALUE, NEW_AGE, NEW_CITY);
		assertEquals(person.getCity(), mergedPerson.getCity());
		assertEquals(NEW_AGE, (int) mergedPerson.getAge());
	}

	private Person mergeWithPolicyAndAgeAndCityTest(MergeConflictPolicy mergeConflictPolicy, int age, String city) {
		final String ID = UUID.randomUUID().toString();

		Person changedCity = new Person(person);
		changedCity.setCity(city);
		changedCity.setId(ID);

		Person changedAge = new Person(person);
		changedAge.setAge(age);
		changedAge.setId(ID);

		Merger<Person> merger = new Merger<>(changedCity, changedAge);
		merger.setMergeConflictPolicy(mergeConflictPolicy);
		return merger.merge();
	}

	@Test(expected = MergerException.class)
	public void incompatibleMergeTest() {
		Person srcPerson = new Person(person);
		srcPerson.setId(UUID.randomUUID().toString());
		Person dstPerson = new Person(person);
		dstPerson.setId("1");

		Merger<Person> merger = new Merger<>(srcPerson, dstPerson);
		merger.merge();

		expectedException.expectMessage("Objects have different origins");
	}

	@Test(expected = MergerException.class)
	public void incompatibleHistoryTest() {
		final String ID = "1";
		History.save(new Mergeable() {
			@Override
			public String getId() {
				return ID;
			}
		});
		Person srcPerson = new Person(person);
		srcPerson.setId(ID);
		Person dstPerson = new Person(person);
		dstPerson.setId(ID);

		Merger<Person> personMerger = new Merger<>(srcPerson, dstPerson);
		personMerger.merge();

		expectedException.expectMessage("Not compatible with class in history");
	}

	@Test(expected = MergerException.class)
	public void nullInputTest() {
		new Merger<>(null, null);
		expectedException.expectMessage("Provided src object is null");
	}

	@Test
	public void mergeWithNullFieldsWithHistory() {
		Person emptyPerson = new Person();
		emptyPerson.setId(person.getId());
		Person mergedPerson = new Merger<>(emptyPerson, emptyPerson).merge();
		assertEquals(person.getId(), mergedPerson.getId());
	}

	@Test
	public void mergeWithNullFieldsWithoutHistory() {
		Person emptyPerson = new Person();
		Person mergedPerson = new Merger<>(emptyPerson, emptyPerson).merge();
		assertNotNull(mergedPerson);
	}

}
