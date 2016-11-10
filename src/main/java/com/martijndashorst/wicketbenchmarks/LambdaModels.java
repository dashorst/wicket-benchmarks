package com.martijndashorst.wicketbenchmarks;

import java.io.Serializable;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LambdaModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

/**
 * This micro benchmark tries to illustrate the performance difference between
 * various model implementations retrieving a sub-property of a object
 * hierarchy.
 * 
 * <ul>
 * <li>Native: {@code accountModel.getObject().getPerson().getName()}</li>
 * <li>AbstractReadOnlyModel evaluating the above expression</li>
 * <li>LambdaModel:
 * {@code accountModel.map(Account::getPerson).map(Person::getName).getObject()}</li>
 * <li>PropertyModel: {@code PropertyModel.of(accountModel, "person.name")}</li>
 * </ul>
 */
@State(Scope.Benchmark)
public class LambdaModels {

	/**
	 * Main method actually running the benchmark.
	 */
	public static void main(String[] args) throws Exception {
		Options opt = new OptionsBuilder().include(LambdaModels.class.getName()).warmupIterations(10)
				.measurementIterations(10).measurementTime(TimeValue.seconds(10)).forks(1)
				.jvmArgs("-Xmx4G", "-Xms4G", "-XX:+UseG1GC").mode(Mode.Throughput).build();

		new Runner(opt).run();
	}

	/** The root model for traversing the object tree. */
	private IModel<Account> accountModel;

	/**
	 * A model that chains the accountModel and retrieves the name using Java
	 * getters directly.
	 */
	private IModel<String> aromNameModel;

	/**
	 * A model that chains the accountModel and retrieves the name using Lambda
	 * method chaining.
	 */
	private IModel<String> chainedLambdaNameModel;

	/**
	 * A model that directly retrieves the name of the person from the
	 * accountModel.
	 */
	private IModel<String> directLambdaNameModel;

	/**
	 * The old tried and tested property model that evaluates the string
	 * expression against the object tree using Java reflection.
	 */
	private IModel<String> propertyNameModel;

	@Setup
	public void setup() {
		Account account = new Account();
		accountModel = Model.of(account);

		aromNameModel = new IModel<String>() {
			private static final long serialVersionUID = 1L;

			@Override
			public String getObject() {
				return accountModel.getObject().getPerson().getName();
			}
		};
		directLambdaNameModel = LambdaModel.of(() -> accountModel.getObject().getPerson().getName());
		chainedLambdaNameModel = accountModel.map(Account::getPerson).map(Person::getName);
		propertyNameModel = PropertyModel.of(accountModel, "person.name");
	}

	@Benchmark
	public void nativeEvaluation() {
		// retrieve the name of the person of the account
		accountModel.getObject().getPerson().getName();
	}

	@Benchmark
	public void abstractReadOnlyModel() {
		// retrieve the name of the person of the account
		aromNameModel.getObject();
	}

	@Benchmark
	public void directLambdaModel() {
		// retrieve the name of the person of the account
		directLambdaNameModel.getObject();
	}

	@Benchmark
	public void chainedLambdaModel() {
		// retrieve the name of the person of the account
		chainedLambdaNameModel.getObject();
	}

	@Benchmark
	public void propertyModel() {
		// retrieve the name of the person of the account
		propertyNameModel.getObject();
	}
}

class Account implements Serializable {
	private static final long serialVersionUID = 1L;

	private Person person = new Person();

	public Person getPerson() {
		return person;
	}
}

class Person implements Serializable {
	private static final long serialVersionUID = 1L;

	private String name = "John Snow";

	public String getName() {
		return name;
	}
}
