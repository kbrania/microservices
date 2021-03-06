package com.example.offerservice;

import java.net.http.HttpClient;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.IdGenerator;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RefreshScope
@RestController
@RequestMapping("offers")
public class OfferController {

	private final HttpClient httpClient = HttpClient.newHttpClient();
	private final OfferRepository repository;
	private final IdGenerator idGenerator;
	private final String message;
	private final ApiGatewayClient apiGatewayClient;

	public OfferController(final OfferRepository repository, final IdGenerator idGenerator,
			final @Value("${message}") String message, final ApiGatewayClient apiGatewayClient) {
		this.repository = repository;
		this.idGenerator = idGenerator;
		this.message = message;
		this.apiGatewayClient = apiGatewayClient;
	}

	@GetMapping
	public Iterable<Offer> getList() {
		log.info(message);
		return repository.findAll();
	}

	@GetMapping("{id}")
	public ResponseEntity<Offer> get(@PathVariable UUID id) {
		return repository.findById(id)
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Offer insert(@RequestBody Offer offer) {
		validateProductId(offer.getProductId());
		validateUserId(offer.getBidderId());
		offer.setId(idGenerator.generateId());
		return repository.insert(offer);
	}

	@PutMapping("{id}")
	public Offer update(@PathVariable UUID id, @RequestBody Offer requestOffer) {
		Offer databaseOffer = repository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Incorrect offer id " + id));
		databaseOffer.setName(requestOffer.getName());
		return repository.save(requestOffer);
	}

	@DeleteMapping("{id}")
	public ResponseEntity<Void> delete(@PathVariable UUID id) {
		repository.deleteById(id);
		return ResponseEntity.noContent().build();
	}

	private void validateProductId(final UUID id) {
		Map<String, Object> product = apiGatewayClient.getProduct(id);
	}

	private void validateUserId(final UUID id) {
		Map<String, Object> user = apiGatewayClient.getUser(id);
	}
}
