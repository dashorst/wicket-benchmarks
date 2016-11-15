package com.martijndashorst.wicketbenchmarks;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LambdaModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

import objectexplorer.MemoryMeasurer;

public class MemoryBenchmark {
	public static void main(String[] args) {
		Account account = new Account();
		IModel<Account> accountModel = LoadableDetachableModel.of(Account::new);
		IModel<Account> newModel = Account::new;

		IModel<String> aromNameModel = new IModel<String>() {
			private static final long serialVersionUID = 1L;

			@Override
			public String getObject() {
				return accountModel.getObject().getPerson().getName();
			}
		};
		IModel<String> directLambdaNameModel = LambdaModel.of(() -> accountModel.getObject().getPerson().getName());
		IModel<String> chainedNameModel = accountModel.map(Account::getPerson).map(Person::getName);
		IModel<String> chainedLambdaNameModel = LambdaModel.of(accountModel, Account::getPerson).map(Person::getName);
		IModel<Object> propertyNameModel = PropertyModel.of(accountModel, "person.name");

		System.out.println("Length account: " + bytes(account).length);
		System.out.println("Length IModel<Account> = Account::new: " + bytes(newModel).length);
		System.out.println("Length Model.of(account): " + bytes(Model.of(account)).length);
		System.out.println("Length LDM.of(Account::new): " + bytes(accountModel).length);
		System.out.println("Length LDM extends LDM<Account>: " + bytes(new LDM()).length);
		System.out.println("Length aromModel: " + bytes(aromNameModel).length);
		System.out.println("Length directLambdaModel: " + bytes(directLambdaNameModel).length);
		System.out.println("Length chainedModel: " + bytes(chainedNameModel).length);
		System.out.println("Length chainedLambdaModel: " + bytes(chainedLambdaNameModel).length);
		System.out.println("Length propertyNameModel: " + bytes(propertyNameModel).length);
		
		System.out.println();
		System.out.println("Memory account: " + MemoryMeasurer.measureBytes(account));
		System.out.println("Memory IModel<Account> = Account::new: " + MemoryMeasurer.measureBytes(newModel));
		System.out.println("Memory Model.of(account): " + MemoryMeasurer.measureBytes(Model.of(account)));
		System.out.println("Memory LDM.of(Account::new): " + MemoryMeasurer.measureBytes(accountModel));
		System.out.println("Memory LDM extends LDM<Account>: " + MemoryMeasurer.measureBytes(new LDM()));
		System.out.println("Memory aromModel: " + MemoryMeasurer.measureBytes(aromNameModel));
		System.out.println("Memory directLambdaModel: " + MemoryMeasurer.measureBytes(directLambdaNameModel));
		System.out.println("Memory chainedModel: " + MemoryMeasurer.measureBytes(chainedNameModel));
		System.out.println("Memory chainedLambdaModel: " + MemoryMeasurer.measureBytes(chainedLambdaNameModel));
		System.out.println("Memory propertyNameModel: " + MemoryMeasurer.measureBytes(propertyNameModel));
	}

	private static Object copy(Object original) {
		Object copy = null;
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ObjectOutputStream oos = new ObjectOutputStream(baos);) {
			oos.writeObject(original);

			ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
			ObjectInputStream ois = new ObjectInputStream(bais);
			copy = ois.readObject();
		} catch (IOException | ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
		return copy;
	}

	private static byte[] bytes(Object model) {
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ObjectOutputStream oos = new ObjectOutputStream(baos);) {
			for (int i = 0; i < 1; i++) {
				oos.writeObject(model);
//				model = copy(model);
			}
			return baos.toByteArray();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public MemoryBenchmark() {
	}
}

class LDM extends LoadableDetachableModel<Account> {
	@Override
	protected Account load() {
		return new Account();
	}
}
