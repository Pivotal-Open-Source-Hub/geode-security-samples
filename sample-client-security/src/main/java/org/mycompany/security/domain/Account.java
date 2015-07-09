package org.mycompany.security.domain;

public class Account {

	private String accountId;
	private String accountNumber;
	private String accountType;
	private Double balance;
	private Double creditLine;
	
	public Account() {}
	
	public String getAccountId() {
		return accountId;
	}
	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}
	public String getAccountType() {
		return accountType;
	}
	public void setAccountType(String accountType) {
		this.accountType = accountType;
	}
	public Double getBalance() {
		return balance;
	}
	public void setBalance(Double balance) {
		this.balance = balance;
	}
	public Double getCreditLine() {
		return creditLine;
	}
	public void setCreditLine(Double creditLine) {
		this.creditLine = creditLine;
	}

	public String getAccountNumber() {
		return accountNumber;
	}

	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}

}
