package com.dws.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import java.math.BigDecimal;

import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;

import com.dws.challenge.domain.Account;
import com.dws.challenge.service.AccountsService;

import lombok.extern.slf4j.Slf4j;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@WebAppConfiguration
@Slf4j
class AccountsControllerTest {

	private MockMvc mockMvc;

	@Autowired
	private AccountsService accountsService;

	@Autowired
	private WebApplicationContext webApplicationContext;


	@BeforeEach
	void prepareMockMvc() {

		this.mockMvc = webAppContextSetup(this.webApplicationContext).build();

		// Reset the existing accounts before each test.
		accountsService.getAccountsRepository().clearAccounts();
	}

	@Test
	void createAccount() throws Exception {
		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
				.content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isCreated());

		Account account = accountsService.getAccount("Id-123");
		assertThat(account.getAccountId()).isEqualTo("Id-123");
		assertThat(account.getBalance()).isEqualByComparingTo("1000");
	}

	@Test
	void createDuplicateAccount() throws Exception {
		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
				.content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isCreated());

		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
				.content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isBadRequest());
	}

	@Test
	void createAccountNoAccountId() throws Exception {
		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
				.content("{\"balance\":1000}")).andExpect(status().isBadRequest());
	}

	@Test
	void createAccountNoBalance() throws Exception {
		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
				.content("{\"accountId\":\"Id-123\"}")).andExpect(status().isBadRequest());
	}

	@Test
	void createAccountNoBody() throws Exception {
		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON))
		.andExpect(status().isBadRequest());
	}

	@Test
	void createAccountNegativeBalance() throws Exception {
		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
				.content("{\"accountId\":\"Id-123\",\"balance\":-1000}")).andExpect(status().isBadRequest());
	}

	@Test
	void createAccountEmptyAccountId() throws Exception {
		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
				.content("{\"accountId\":\"\",\"balance\":1000}")).andExpect(status().isBadRequest());
	}

	@Test
	void getAccount() throws Exception {
		String uniqueAccountId = "Id-" + System.currentTimeMillis();
		Account account = new Account(uniqueAccountId, new BigDecimal("123.45"));
		this.accountsService.createAccount(account);
		this.mockMvc.perform(get("/v1/accounts/" + uniqueAccountId))
		.andExpect(status().isOk())
		.andExpect(
				content().string("{\"accountId\":\"" + uniqueAccountId + "\",\"balance\":123.45}"));
	}

	/*
	 *  test case for account api request parameter validation
	 */
	@Test
	void balanceTransferInvalidField() throws Exception{

		JSONObject request = new JSONObject();
		request.put("accountFrom","");
		request.put("accountTo","1234");
		request.put("transferAmount","-5.00");

		this.mockMvc.perform(post("/v1/accounts/balanceTransfer").contentType(MediaType.APPLICATION_JSON)
				.content(request.toString())).andExpect(status().isBadRequest());

	}

	/*
	 * test case for account initial balance is 1000 but we are deducting 1500
	 */
	@Test
	void balanceTransferInsufficientBalance() throws Exception{

		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
				.content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isCreated());
		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
				.content("{\"accountId\":\"Id-124\",\"balance\":2000}")).andExpect(status().isCreated());

		JSONObject request = new JSONObject();
		request.put("accountFrom","Id-123");
		request.put("accountTo","Id-124");
		request.put("transferAmount","1500");// sending amount 1500 but balance is 1000
		System.out.println(request.toString());
		this.mockMvc.perform(post("/v1/accounts/balanceTransfer").contentType(MediaType.APPLICATION_JSON)
				.content(request.toString())).andExpect(status().isPreconditionFailed());

	}

	/*
	 * sending invalid account number field
	 */
	@Test
	void balanceTransferInvalidAccount() throws Exception{

		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
				.content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isCreated());
		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
				.content("{\"accountId\":\"Id-124\",\"balance\":2000}")).andExpect(status().isCreated());

		JSONObject request = new JSONObject();
		request.put("accountFrom","Id-12");  // we are sending invalid accountfrom number
		request.put("accountTo","Id-124");
		request.put("transferAmount","1500"); 
		System.out.println(request.toString());
		this.mockMvc.perform(post("/v1/accounts/balanceTransfer").contentType(MediaType.APPLICATION_JSON)
				.content(request.toString())).andExpect(status().isNotFound());

	}

	/*
	 * test case for account successful balance transfer
	 */
	@Test
	void balanceTransferSuccess() throws Exception{

		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
				.content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isCreated());
		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
				.content("{\"accountId\":\"Id-124\",\"balance\":2000}")).andExpect(status().isCreated());

		JSONObject request = new JSONObject();
		request.put("accountFrom","Id-123");
		request.put("accountTo","Id-124");
		request.put("transferAmount","500");
		System.out.println(request.toString());
		MvcResult mvcResult = this.mockMvc.perform(post("/v1/accounts/balanceTransfer").contentType(MediaType.APPLICATION_JSON)
				.content(request.toString())).andExpect(status().isOk()).andReturn();

		assertEquals(mvcResult.getResponse().getContentAsString(),"{\"status\":\"balance transfer processed successfully\"}");

		assertEquals(500,accountsService.getAccount("Id-123").getBalance().intValue());

		assertEquals(2500,accountsService.getAccount("Id-124").getBalance().intValue());
	}

	/*
	 * repeated calls showing low balance scenario
	 */

	@Test
	@RepeatedTest(2)
	void balanceTransferFailureConscutiveCalls() throws Exception{

		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
				.content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isCreated());
		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
				.content("{\"accountId\":\"Id-124\",\"balance\":2000}")).andExpect(status().isCreated());

		JSONObject request = new JSONObject();
		request.put("accountFrom","Id-123");
		request.put("accountTo","Id-124");
		request.put("transferAmount","500");
		System.out.println(request.toString());
		MvcResult mvcResult = this.mockMvc.perform(post("/v1/accounts/balanceTransfer").contentType(MediaType.APPLICATION_JSON)
				.content(request.toString())).andExpect(status().isOk()).andReturn();

		assertEquals(mvcResult.getResponse().getContentAsString(),"{\"status\":\"balance transfer processed successfully\"}");

		assertEquals(500,accountsService.getAccount("Id-123").getBalance().intValue());

		assertEquals(2500,accountsService.getAccount("Id-124").getBalance().intValue());

		request = new JSONObject();
		request.put("accountFrom","Id-123");
		request.put("accountTo","Id-124");
		request.put("transferAmount","600"); // available balance is 500 but we are trying to transfer 600 
		System.out.println(request.toString());

		mvcResult = this.mockMvc.perform(post("/v1/accounts/balanceTransfer").contentType(MediaType.APPLICATION_JSON)
				.content(request.toString())).andExpect(status().isPreconditionFailed()).andReturn();

		assertEquals(mvcResult.getResponse().getContentAsString(),"{\"status\":\"Sender Account low balance\"}");

		// there will be no changes in the sender & receiver account
		assertEquals(500,accountsService.getAccount("Id-123").getBalance().intValue());

		assertEquals(2500,accountsService.getAccount("Id-124").getBalance().intValue());
	}

	/*
	 * concurrent balance transfer to two different accounts from a single account
	 */
	@Test
	void balanceTransferSuccessThread() throws Exception{

		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
				.content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isCreated());
		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
				.content("{\"accountId\":\"Id-124\",\"balance\":2000}")).andExpect(status().isCreated());

		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
				.content("{\"accountId\":\"Id-125\",\"balance\":3000}")).andExpect(status().isCreated());

		JSONObject request = new JSONObject();
		request.put("accountFrom","Id-123");
		request.put("accountTo","Id-124");
		request.put("transferAmount","500");

		Runnable runnable = () -> {
			try {
				this.mockMvc.perform(post("/v1/accounts/balanceTransfer").contentType(MediaType.APPLICATION_JSON)
						.content(request.toString())).andExpect(status().isOk());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		};
		
		JSONObject request2 = new JSONObject();
		request2.put("accountFrom","Id-123");
		request2.put("accountTo","Id-125");
		request2.put("transferAmount","500");
		
		Runnable runnable2 = () -> {
			try {
				this.mockMvc.perform(post("/v1/accounts/balanceTransfer").contentType(MediaType.APPLICATION_JSON)
						.content(request2.toString())).andExpect(status().isOk());
			} catch (Exception e) {
				
			}
		};

		new Thread(runnable).start();
		new Thread(runnable2).start();

		Thread.sleep(5000);

		assertEquals(0,accountsService.getAccount("Id-123").getBalance().intValue());

		assertEquals(2500,accountsService.getAccount("Id-124").getBalance().intValue());
		
		assertEquals(3500,accountsService.getAccount("Id-125").getBalance().intValue());
	}
}
