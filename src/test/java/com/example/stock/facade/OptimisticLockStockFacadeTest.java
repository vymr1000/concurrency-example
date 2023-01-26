package com.example.stock.facade;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.stock.domain.Stock;
import com.example.stock.repository.StockRepository;

@SpringBootTest
public class OptimisticLockStockFacadeTest {
	@Autowired
	private OptimisticLockStockFacade optimisticLockStockFacade;

	@Autowired
	private StockRepository stockRepository;

	@BeforeEach
	public void insert() {
		Stock stock = new Stock(1L, 100L);
		stockRepository.saveAndFlush(stock);
	}

	@AfterEach
	public void delete() {
		stockRepository.deleteAll();
	}

	@Test
	public void 동시에_100명이_주문() throws InterruptedException {
		// 100개의 요청
		int threadCount = 100;
		// 비동기로 실행하는 작업을 단순화하여 사용할 수 있게 하는 Java API ExecutorService
		ExecutorService executorService = Executors.newFixedThreadPool(32);
		// 100개의 요청이 끝날때까지 기다리는 다른 스레드에서 실행중인 작업이 완료될 때까지 기다리도록 하는 클래스 CountDownLatch
		CountDownLatch latch = new CountDownLatch(threadCount);

		for (int i = 0; i < threadCount; i++) {
			executorService.submit(() -> {
				try {
					optimisticLockStockFacade.decrease(1L, 1L);
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				} finally {
					latch.countDown();
				}
			});
		}

		latch.await();
		Stock stock = stockRepository.findById(1L).orElseThrow();
		// 100 - (100 * 1) = 0
		assertEquals(0, stock.getQuantity());
	}
}
