package net.johnsonlau.reactor.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class App {
	private static final Logger logger = LoggerFactory.getLogger(App.class);

	public static void main(String[] args) {
		logger.info("=============");

		Flux.just(1, 2, 3, 4, 5, 6).subscribe(System.out::print);
		System.out.println();

		Mono.just(1).subscribe(System.out::println);

		Flux.just(1, 2, 3, 4, 5, 6).subscribe(
			    System.out::println,
			    System.err::println,
			    () -> System.out.println("Completed!"));
		
		Mono.error(new Exception("some error")).subscribe(
		        System.out::println,
		        System.err::println,
		        () -> System.out.println("Completed!")
		);
	}
}
