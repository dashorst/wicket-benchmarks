/*
 * Copyright (c) 2014, Oracle America, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 *  * Neither the name of Oracle nor the names of its contributors may be used
 *    to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.martijndashorst.wicketbenchmarks;

import java.io.Serializable;

import org.apache.wicket.model.IModel;
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

@State(Scope.Benchmark)
public class LambdaModels {
	public static void main(String[] args) throws Exception {
		Options opt = new OptionsBuilder().include(LambdaModels.class.getName()).warmupIterations(10)
				.measurementIterations(10).measurementTime(TimeValue.seconds(10)).forks(1)
				.jvmArgs("-Xmx4G", "-Xms4G", "-XX:+UseG1GC").mode(Mode.Throughput).build();

		new Runner(opt).run();
	}

	private Account account;

	private IModel<Account> accountModel;

	private IModel<String> aromNameModel;

	private IModel<String> lambdaNameModel;

	private IModel<String> propertyNameModel;

	@Setup
	public void setup() {
		account = new Account();
		accountModel = Model.of(account);

		aromNameModel = new IModel<String>() {
			private static final long serialVersionUID = 1L;

			@Override
			public String getObject() {
				return accountModel.getObject().getPerson().getName();
			}
		};
		lambdaNameModel = accountModel.map(Account::getPerson).map(Person::getName);
		propertyNameModel = PropertyModel.of(accountModel, "person.name");
	}

	@Benchmark
	public void nativeEvaluation() {
		accountModel.getObject().getPerson().getName();
	}

	@Benchmark
	public void abstractReadOnlyModel() {
		aromNameModel.getObject();
	}

	@Benchmark
	public void lambdaModel() {
		lambdaNameModel.getObject();
	}

	@Benchmark
	public void propertyModel() {
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
