package merger;

import drofff.merger.Mergeable;

class Person extends Mergeable {

	private String id;

	private String name;

	private Integer age;

	private String city;

	public Person() {}

	public Person(Person person) {
		this.id = person.id;
		this.name = person.name;
		this.age = person.age;
		this.city = person.city;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Override
	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getAge() {
		return age;
	}

	public void setAge(Integer age) {
		this.age = age;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}
}