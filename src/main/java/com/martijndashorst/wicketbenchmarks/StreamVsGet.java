package com.martijndashorst.wicketbenchmarks;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.util.tester.WicketTester;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

/**
 * Tests the difference between using {@code stream()}, {@code get()} and
 * {@code iterate()} for retrieving a child component.
 */
@State(Scope.Benchmark)
public class StreamVsGet {
	public static void main(String[] args) throws RunnerException {
		Options opt = new OptionsBuilder().include(StreamVsGet.class.getName()).warmupIterations(10)
				.measurementIterations(10).measurementTime(TimeValue.seconds(10)).forks(1)
				.jvmArgs("-Xmx4G", "-Xms4G", "-XX:+UseG1GC").mode(Mode.Throughput).build();

		new Runner(opt).run();
	}

	/** Sets up the Wicket thread local necessary to create a page. */
	@SuppressWarnings("unused")
	private WicketTester tester;

	/** The page we use to retrieve the component from. */
	private WebPage page;

	/**
	 * The identifier to retrieve. Always at 50%, because that should be the
	 * average call.
	 */
	private String id;

	@Setup
	public void setup() {
		tester = new WicketTester();

		page = new WebPage() {
			private static final long serialVersionUID = 1L;
		};

		int nr = 100_000;

		for (int i = 0; i < nr; i++) {
			page.add(new WebMarkupContainer(Integer.toString(i)));
		}

		id = Integer.toString(nr / 2);
	}

	@Benchmark
	public void retrieveComponentUsingGet() {
		page.get(id);
	}

	@Benchmark
	public void retrieveComponentUsingIterator() {
		for (Component component : page) {
			if (component.getId().equals(id))
				break;
		}
	}

	@Benchmark
	public void retrieveComponentUsingStream() {
		page.stream().filter(c -> c.getId().equals(id)).findFirst().orElse(null);
	}
}
