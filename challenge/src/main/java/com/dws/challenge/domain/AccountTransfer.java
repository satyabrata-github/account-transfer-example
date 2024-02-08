package com.dws.challenge.domain;

import java.math.BigDecimal;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@ToString
@Setter
@NoArgsConstructor
public class AccountTransfer {

	@NotNull
	@NotEmpty
	private String accountFrom;

	@NotNull
	@NotEmpty
	private String accountTo;
	
	@NotNull
	@Positive(message = "Transfer Amount is invalid")
	private BigDecimal transferAmount;
}
