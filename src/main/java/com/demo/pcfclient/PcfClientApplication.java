package com.demo.pcfclient;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudfoundry.operations.CloudFoundryOperations;
import org.cloudfoundry.operations.services.CreateServiceInstanceRequest;
import org.cloudfoundry.operations.services.ListServiceOfferingsRequest;
import org.cloudfoundry.operations.services.ServiceInstanceSummary;
import org.cloudfoundry.operations.services.ServicePlan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Predicate;

@SpringBootApplication
public class PcfClientApplication {

	
	private final CloudFoundryOperations operations;
	//private final CloudFoundryDiscoveryClient discoveryClient;

	public PcfClientApplication(CloudFoundryOperations operations)
	{

		this.operations = operations;
		//this.discoveryClient = discoveryClient;
	}

//	@Bean
//	CommandLineRunner demo() {
//		Log log = LogFactory.getLog(getClass());
//		return args ->
//				discoveryClient.getServices().forEach(svc -> {
//					log.info("service = " + svc);
//					discoveryClient.getInstances(svc).forEach(si -> log.info("\tinstance = " + si));
//				});
//	}

	@Bean
	ApplicationRunner run(@Value("file://${JAR_ARTIFACT}") Resource jar){
		return args -> {
			Log log = LogFactory.getLog(getClass());
			System.out.println(" Jar file is "+ jar + " and jar exists "+ jar.exists());
			Assert.isTrue(jar.exists(),"jar must exits");
			final String serviceName = "p-mysql";
			final String instanceName = "st-sql";
			final String appName ="myapp";
			this.operations.services()
					.listInstances().subscribe(x -> System.out.println(" services are "+ x));
			long start = System.currentTimeMillis();
//			Mono<ServiceInstanceSummary> createService =
//			createServiceInstanceIfMissing(serviceName, instanceName,ServicePlan::getFree);
//			//createService.switchIfEmpty(x ->{System.out.println("Bad request")});
//
//			createService.subscribe(
//					x -> log.info(String.format("Application %s has been deployed and bound to the service '%s'." +
//							" It tookd %s ms to deploy. ",appName,instanceName,(System.currentTimeMillis()-start))));

		};
	}

	private void pushApplications(String applicatoinName, int replicas, Resource jar, boolean start)
	{

	}

	private Mono<ServiceInstanceSummary> createServiceInstanceIfMissing(String serviceName, String instanceName,
						Predicate<ServicePlan> servicePlanPredicate) {

		Flux<ServiceInstanceSummary> existing =
		this.operations.services()
				.listInstances()
				.filter(si -> {
					System.out.println(" Filtering based on ************* "+ si.getName());
					return si.getName().equalsIgnoreCase(instanceName);});

		Flux<ServiceInstanceSummary> create = this.operations.services().listServiceOfferings(
				ListServiceOfferingsRequest.builder().build())
				.filter(so ->so.getLabel().equalsIgnoreCase(serviceName))
				.flatMap( so -> {
					ServicePlan free = so.getServicePlans().stream().filter(servicePlanPredicate)
							.findFirst()
							.orElseThrow(()->new RuntimeException("Could not find a service called "+serviceName));

					CreateServiceInstanceRequest createServiceInstanceRequest =
					CreateServiceInstanceRequest.builder().planName(free.getName())
							.serviceName(serviceName)
							.serviceInstanceName(instanceName)
							.build();
					return 	operations.services().createInstance(createServiceInstanceRequest);

				})
				.thenMany(existing);


		return
		existing.switchIfEmpty(create)
				.singleOrEmpty();
	}


	public static void main(String[] args) {
		SpringApplication.run(PcfClientApplication.class, args);
	}
}
