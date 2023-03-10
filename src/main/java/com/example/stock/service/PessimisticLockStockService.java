package com.example.stock.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.stock.domain.Stock;
import com.example.stock.repository.StockRepository;
@Service
public class PessimisticLockStockService {
	private final StockRepository stockRepository;

	public PessimisticLockStockService(StockRepository stockRepository) {
		this.stockRepository = stockRepository;
	}

	@Transactional
	public Long updateStock(Stock stock) {
		stockRepository.saveAndFlush(stock);
		return stock.getQuantity();
	}
	/**
	 * decrease() -> updateStock() 호출
	 */
	public void decrease(Long id, Long quantity) {
		/* Pessimistic Lock 메소드 findByIdWithPessimisticLock 호출 */
		Stock stock = stockRepository.findByIdWithPessimisticLock(id);
		stock.decrease(quantity);
		updateStock(stock);
	}
}
