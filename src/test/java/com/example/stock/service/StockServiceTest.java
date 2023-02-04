package com.example.stock.service;

import com.example.stock.domain.Stock;
import com.example.stock.repository.StockRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class StockServiceTest {

	@Autowired
	private StockService stockService;

	@Autowired
	private StockRepository stockRepository;

	@BeforeEach
	public void insert() {
		Stock stock = new Stock(1L, 100L);
		stockRepository.save(stock);
	}

	@AfterEach
	public void delete() {
		stockRepository.deleteAll();
	}

	@Test
	public void decrease_test() {
		stockService.decrease(1L, 1L);
		Stock stock = stockRepository.findById(1L).orElseThrow();
		assertEquals(99, stock.getQuantity());
	}

	@Test
	@DisplayName("트랜잭션 적용 안됨")
	public void transaction_test() throws Exception {
		System.out.println("== start ==");
		stockService.decrease(1L, 1L);
		System.out.println("== end ==");
	}

	@Test
	public void single_thread_test() throws InterruptedException {
		// 100개의 요청
		int threadCount = 100;
		// decrease 실행
		for (int i = 0; i < threadCount; i++) {
			stockService.decrease(1L, 1L);
		}
		Stock stock = stockRepository.findById(1L).orElseThrow();
		// 100 - (100 * 1) = 0
		assertEquals(0, stock.getQuantity());
	}

	@Test
	public void multi_thread_test() throws InterruptedException {
		// 100개의 요청
		int threadCount = 100;
		// 비동기로 실행하는 작업을 단순화하여 사용할 수 있게 하는 Java API ExecutorService
		ExecutorService executorService = Executors.newFixedThreadPool(32);
		// 100개의 요청이 끝날때까지 기다리는 다른 스레드에서 실행중인 작업이 완료될 때까지 기다리도록 하는 클래스 CountDownLatch
		CountDownLatch latch = new CountDownLatch(threadCount);

		for (int i = 0; i < threadCount; i++) {
			executorService.submit(() -> {
				try {
					stockService.decrease(1L, 1L);
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