package com.example.stock.service;

import javax.persistence.EntityManager;

import com.example.stock.domain.Stock;
import com.example.stock.repository.StockRepository;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StockService {

	private final StockRepository stockRepository;
	private final EntityManager em;

	public StockService(StockRepository stockRepository, EntityManager em) {
		this.stockRepository = stockRepository;
		this.em = em;
	}
	@Transactional
	public void updateStock(Stock stock) {
		stockRepository.save(stock);
		System.out.println("Entity Manager contains stock true/false =  " + em.contains(stock));
	}

	/**
	 * decrease() -> updateStock() 호출
	 */
	@Transactional
	public void decrease(Long id, Long quantity) {
		Stock stock = stockRepository.findById(id).orElseThrow();
		stock.decrease(quantity);
		updateStock(stock);
	}
}