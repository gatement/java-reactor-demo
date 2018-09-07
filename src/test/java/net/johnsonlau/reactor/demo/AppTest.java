package net.johnsonlau.reactor.demo;

import org.junit.Test;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

public class AppTest {
	private Flux<Integer> generateFluxFrom1To6() {
	    return Flux.just(1, 2, 3, 4, 5, 6);
	}
	private Mono<Integer> generateMonoWithError() {
	    return Mono.error(new Exception("some error"));
	}
	@Test
	public void testViaStepVerifier() {
	    StepVerifier.create(generateFluxFrom1To6())
	            .expectNext(1, 2, 3, 4, 5, 6)
	            .expectComplete()
	            .verify();
	    
	    StepVerifier.create(generateMonoWithError())
	            .expectErrorMessage("some error")
	            .verify();

		StepVerifier.create(Flux.range(1, 6)
				.map(i -> i * i)) 
				.expectNext(1, 4, 9, 16, 25, 36)
				.verifyComplete();
	}
}
