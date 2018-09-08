package net.johnsonlau.reactor.demo;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

public class AppTest {
	private static final Logger logger = LoggerFactory.getLogger(AppTest.class);

	private Flux<Integer> generateFluxFrom1To6() {
	    return Flux.just(1, 2, 3, 4, 5, 6);
	}
	private Mono<Integer> generateMonoWithError() {
	    return Mono.error(new Exception("some error"));
	}
	//@Test
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

	    StepVerifier.create(
	            Flux.just("flux", "mono")
	                    .flatMap(s -> Flux.fromArray(s.split("\\s*"))
	                            .delayElements(Duration.ofMillis(100)))
	                    .doOnNext(System.out::print)) 
	            .expectNextCount(8) 
	            .verifyComplete();
	    
	    StepVerifier.create(Flux.range(1, 6)
	            .filter(i -> i % 2 == 1)
	            .map(i -> i * i))
	            .expectNext(1, 9, 25)
	            .verifyComplete();
	}
	
	private Flux<String> getZipDescFlux() {
	    String desc = "Zip two sources together, that is to say wait for all the sources to emit one element and combine these elements once into a Tuple2.";
	    return Flux.fromArray(desc.split("\\s+")); 
	}

	//@Test
	public void testSimpleOperators() throws InterruptedException {
	    CountDownLatch countDownLatch = new CountDownLatch(1); 
	    Flux.zip(getZipDescFlux(), Flux.interval(Duration.ofMillis(200)))
			.subscribe(t -> System.out.println(t.getT1()), null, countDownLatch::countDown);
	    countDownLatch.await(10, TimeUnit.SECONDS); 
	}
	
	private String getStringSync() {
	    try {
	        TimeUnit.SECONDS.sleep(2);
	    } catch (InterruptedException e) {
	        e.printStackTrace();
	    }
	    return "Hello, Reactor!";
	}
	
	//@Test
	public void testSyncToAsync() throws InterruptedException {
	    CountDownLatch countDownLatch = new CountDownLatch(1);
	    Mono.fromCallable(() -> getStringSync())
	            .subscribeOn(Schedulers.elastic()) 
	            .subscribe(System.out::println, null, countDownLatch::countDown);
	    logger.info("------");
	    countDownLatch.await(10, TimeUnit.SECONDS);
	    logger.info("======");
	}

	//@Test
	public void testErrorHandling() throws InterruptedException {
	    Flux.range(1, 6)
	            .map(i -> 10/(i-3))
	            //.onErrorReturn(0)
	            //.onErrorResume(e -> Mono.just(new Random().nextInt(6)))
	            //.onErrorMap(original -> new Exception("SLA exceeded", original))
	            //.doOnError(e -> { logger.info("uh oh, exception {}", e.toString()); })
	            .map(i -> i*i)
	            .retry(1)
	            .subscribe(System.out::println, System.err::println);
	    Thread.sleep(100);
	}
	
	@Test
	public void testBackpressure() {
	    Flux.range(1, 6)    // 1
	            .doOnRequest(n -> System.out.println("Request " + n + " values..."))    // 2
	            .subscribe(new BaseSubscriber<Integer>() {  // 3
	                @Override
	                protected void hookOnSubscribe(Subscription subscription) { // 4
	                    System.out.println("Subscribed and make a request...");
	                    request(1); // 5
	                }

	                @Override
	                protected void hookOnNext(Integer value) {  // 6
	                    try {
	                        TimeUnit.SECONDS.sleep(1);  // 7
	                    } catch (InterruptedException e) {
	                        e.printStackTrace();
	                    }
	                    System.out.println("Get value [" + value + "]");    // 8
	                    request(1); // 9
	                }
	            });
	}
}
